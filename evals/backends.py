"""Optional CLI adapters. Evaluation artifacts do not depend on these launchers.

A generator reaches the previewer over MCP, registered as `android_design_preview` for that one run only,
passing ANDROID_DESIGN_NO_OPEN=1 (and ANDROID_DESIGN_PREVIEW_JAR when provided) so each run supervises its
own isolated headless worker without opening browser windows. A judge never sees the MCP server.
"""
import json
import shutil
import subprocess
import tempfile
import time
import tomllib
from contextlib import ExitStack
from pathlib import Path

from preview_app import JAR_ENV, NO_OPEN_ENV, SERVER_NAME

BACKENDS = ('agy', 'claude', 'codex')

# Per-run Antigravity workspace plugin name so global ~/.gemini/config/mcp_config.json never shadows the isolated server.
AGY_PLUGIN = 'eval-preview'
AGY_MCP_SERVER = 'eval-preview_preview'

CHECK_TOOLS = ('check_proposal', 'check_design')
RETRY_TOOLS = ('open_proposal', 'check_proposal', 'check_design')


def _default_model(backend, home=None):
    """Best-effort metadata lookup for the CLI's configured default model."""
    home = Path.home() if home is None else Path(home)
    try:
        if backend == 'agy':
            data = json.loads((home / '.gemini' / 'antigravity-cli' / 'settings.json').read_text())
            value = data.get('model') if isinstance(data, dict) else None
            return value.strip() if isinstance(value, str) and value.strip() else None
        if backend == 'codex':
            data = tomllib.loads((home / '.codex' / 'config.toml').read_text())
            value = data.get('model') if isinstance(data, dict) else None
            return value.strip() if isinstance(value, str) and value.strip() else None
    except (OSError, ValueError, tomllib.TOMLDecodeError):
        return None
    return None


def _normalize_preview_tool(name):
    if not isinstance(name, str) or not name:
        return ''
    if '/' in name:
        name = name.rsplit('/', 1)[-1]
    if name.startswith('mcp__'):
        return name.split('__')[-1]
    for prefix in (f'mcp_{AGY_MCP_SERVER}_', f'mcp_{SERVER_NAME}_', f'{AGY_MCP_SERVER}_', f'{SERVER_NAME}_'):
        if name.startswith(prefix):
            return name[len(prefix):]
    return name


def _parse_tool_payload(output_text):
    if not isinstance(output_text, str) or not output_text.strip():
        return None
    text = output_text.strip()
    try:
        parsed = json.loads(text)
    except ValueError:
        brace = text.find('{')
        if brace < 0:
            return None
        try:
            parsed, _ = json.JSONDecoder().raw_decode(text[brace:])
        except ValueError:
            return None
    if isinstance(parsed, dict):
        return parsed
    if isinstance(parsed, list):
        extracted = _extract_text(parsed).strip()
        if not extracted:
            return None
        try:
            inner = json.loads(extracted)
        except ValueError:
            return None
        return inner if isinstance(inner, dict) else None
    return None


def _is_compile_or_validation_failure(base_name, output_text, is_error):
    if base_name not in RETRY_TOOLS:
        return False
    if is_error:
        return True
    payload = _parse_tool_payload(output_text)
    return isinstance(payload, dict) and (payload.get('valid') is False or payload.get('compiled') is False)


def _record_preview_tool(stats, tool_name, output_text, is_error):
    base = _normalize_preview_tool(tool_name)
    if base in CHECK_TOOLS:
        stats['check_calls'] += 1
    if _is_compile_or_validation_failure(base, output_text, is_error):
        stats['compile_retries'] += 1


def _extract_text(content):
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        return '\n'.join(
            block.get('text', '') for block in content if isinstance(block, dict) and isinstance(block.get('text'), str)
        )
    return ''


def _consume_claude_events(events, stats, pending_tools):
    reply = None
    for event in events:
        if not isinstance(event, dict):
            continue
        etype = event.get('type')
        if etype == 'assistant':
            for block in (event.get('message') or {}).get('content') or []:
                if isinstance(block, dict) and block.get('type') == 'tool_use':
                    tid = block.get('id')
                    if tid:
                        pending_tools[tid] = block.get('name') or ''
        elif etype == 'user':
            for block in (event.get('message') or {}).get('content') or []:
                if isinstance(block, dict) and block.get('type') == 'tool_result':
                    tname = pending_tools.get(block.get('tool_use_id'), '')
                    _record_preview_tool(stats, tname, _extract_text(block.get('content')), bool(block.get('is_error')))
        elif etype == 'result':
            reply = event
    return reply


def _parse_claude(stdout, role, model):
    reply = None
    saw_stream = False
    stats = {'check_calls': 0, 'compile_retries': 0}
    pending_tools = {}
    try:
        parsed = json.loads(stdout)
        if isinstance(parsed, dict):
            reply = parsed
            saw_stream = parsed.get('type') == 'result'
        elif isinstance(parsed, list):
            saw_stream = True
            reply = _consume_claude_events(parsed, stats, pending_tools)
    except ValueError:
        events = []
        for raw in stdout.splitlines():
            line = raw.strip()
            if not line:
                continue
            try:
                event = json.loads(line)
            except ValueError:
                continue
            if isinstance(event, dict):
                events.append(event)
        if events:
            saw_stream = True
            reply = _consume_claude_events(events, stats, pending_tools)
    if not isinstance(reply, dict):
        raise RuntimeError('claude returned an invalid response envelope')
    if reply.get('is_error'):
        raise RuntimeError(f'claude {role}: {reply.get("result", "failed")}')
    structured = reply.get('structured_output')
    message = json.dumps(structured) if structured is not None else reply.get('result', '')
    usage = reply.get('usage') if isinstance(reply.get('usage'), dict) else {}
    model_usage = reply.get('modelUsage') if isinstance(reply.get('modelUsage'), dict) else None
    resolved_model = (
        model
        or reply.get('model')
        or (', '.join(sorted(model_usage)) if model_usage else None)
    )
    fields = {
        'model': resolved_model,
        'cost': reply.get('total_cost_usd'),
        'turns': reply.get('num_turns'),
        'input_tokens': usage.get('input_tokens'),
        'output_tokens': usage.get('output_tokens'),
        'cache_read_tokens': usage.get('cache_read_input_tokens', usage.get('cache_read_tokens')),
        'cache_creation_tokens': usage.get('cache_creation_input_tokens', usage.get('cache_creation_tokens')),
    }
    if role == 'generator' and saw_stream:
        fields['check_calls'] = stats['check_calls']
        fields['compile_retries'] = stats['compile_retries']
    return message, fields


def _parse_codex(stdout, final_path, role, model):
    message = final_path.read_text() if final_path.exists() else ''
    saw_events = False
    agent_messages = 0
    stats = {'check_calls': 0, 'compile_retries': 0}
    usage_totals = {}
    event_model = None
    for raw in stdout.splitlines():
        line = raw.strip()
        if not line:
            continue
        try:
            event = json.loads(line)
        except ValueError:
            continue
        if not isinstance(event, dict):
            continue
        saw_events = True
        if isinstance(event.get('model'), str) and event['model'].strip():
            event_model = event['model'].strip()
        etype = event.get('type')
        if etype == 'item.completed':
            item = event.get('item') if isinstance(event.get('item'), dict) else {}
            itype = item.get('type')
            if itype == 'agent_message':
                agent_messages += 1
            elif itype == 'mcp_tool_call':
                res = item.get('result') if isinstance(item.get('result'), dict) else {}
                err = bool(item.get('error')) or bool(res.get('isError'))
                _record_preview_tool(stats, item.get('tool') or '', _extract_text(res.get('content')), err)
        elif etype == 'turn.completed':
            u = event.get('usage') if isinstance(event.get('usage'), dict) else {}
            mapping = (
                ('input_tokens', 'input_tokens'),
                ('output_tokens', 'output_tokens'),
                ('cache_read_tokens', 'cached_input_tokens'),
                ('cache_creation_tokens', 'cache_write_input_tokens'),
                ('thinking_tokens', 'reasoning_output_tokens'),
            )
            for dst, src in mapping:
                if isinstance(u.get(src), int):
                    usage_totals[dst] = usage_totals.get(dst, 0) + u[src]
    fields = {'model': model or event_model or _default_model('codex')}
    if saw_events:
        fields['turns'] = max(agent_messages, 1)
        fields.update(usage_totals)
        if role == 'generator':
            fields['check_calls'] = stats['check_calls']
            fields['compile_retries'] = stats['compile_retries']
    return message, fields


def _parse_agy(stdout, role, model):
    terminal = None
    init_model = None
    agent_steps = 0
    stats = {'check_calls': 0, 'compile_retries': 0}
    for raw in stdout.splitlines():
        line = raw.strip()
        if not line:
            continue
        try:
            obj = json.loads(line)
        except ValueError:
            continue
        if not isinstance(obj, dict):
            continue
        kind = obj.get('event')
        if kind == 'init':
            init = obj.get('init') if isinstance(obj.get('init'), dict) else {}
            if isinstance(init.get('model'), str) and init['model'].strip():
                init_model = init['model'].strip()
        elif kind == 'step_update':
            su = obj.get('step_update') if isinstance(obj.get('step_update'), dict) else {}
            if su.get('state') == 'DONE':
                stype = su.get('step_type')
                if stype == 'agent_response':
                    agent_steps += 1
                elif stype == 'tool':
                    info = su.get('tool_info') if isinstance(su.get('tool_info'), dict) else {}
                    if info.get('name') == 'call_mcp_tool':
                        params = info.get('parameters') if isinstance(info.get('parameters'), dict) else {}
                        tname = params.get('ToolName') or ''
                    else:
                        tname = su.get('tool_name') or info.get('name') or ''
                    _record_preview_tool(stats, tname, info.get('output') or '', bool(info.get('error')))
        elif kind == 'result':
            terminal = obj.get('result') if isinstance(obj.get('result'), dict) else None
        elif 'status' in obj and terminal is None:
            terminal = obj
    if not isinstance(terminal, dict):
        raise RuntimeError('agy returned an invalid response envelope')
    status = terminal.get('status')
    if status != 'SUCCESS':
        raise RuntimeError(f'agy {role} {status}: {terminal.get("error") or "failed"}')
    structured = terminal.get('structured_output')
    message = json.dumps(structured) if structured is not None else (terminal.get('response') or '')
    if terminal.get('denied_actions') and (not isinstance(message, str) or not message.strip()):
        raise RuntimeError(f'agy {role} permission denied: {terminal["denied_actions"]}')
    usage = terminal.get('usage') if isinstance(terminal.get('usage'), dict) else {}
    fields = {
        'model': model or init_model or _default_model('agy'),
        'turns': agent_steps if agent_steps > 0 else terminal.get('num_turns'),
        'input_tokens': usage.get('input_tokens'),
        'output_tokens': usage.get('output_tokens'),
        'thinking_tokens': usage.get('thinking_tokens'),
        'cache_read_tokens': usage.get('cache_read_tokens'),
    }
    if role == 'generator':
        fields['check_calls'] = stats['check_calls']
        fields['compile_retries'] = stats['compile_retries']
    return message, fields


def _cleanup_agy_plugin(plugin_dir, plugins_dir, agents_dir, had_plugins, had_agents):
    shutil.rmtree(plugin_dir, ignore_errors=True)
    if not had_plugins and plugins_dir.is_dir():
        try:
            plugins_dir.rmdir()
        except OSError:
            pass
    if not had_agents and agents_dir.is_dir():
        try:
            agents_dir.rmdir()
        except OSError:
            pass


def execute(backend, prompt, cwd, *, role, images=(), schema=None, model=None, timeout=3600, mcp_binary=None, mcp_jar=None):
    if backend not in BACKENDS:
        raise ValueError(f'Unknown backend: {backend}')
    if role not in ('generator', 'judge'):
        raise ValueError(f'Unknown role: {role}')
    if role == 'generator' and not mcp_binary:
        raise ValueError('A generator needs mcp_binary, the preview supervisor it designs with')
    with tempfile.TemporaryDirectory(prefix='android-design-backend-') as temporary, ExitStack() as stack:
        temporary = Path(temporary)
        final = temporary / 'final.txt'
        server_env = {NO_OPEN_ENV: '1'}
        if mcp_jar is not None:
            server_env[JAR_ENV] = str(mcp_jar)
        stdin_input = prompt
        if backend == 'claude':
            command = (
                ['claude', '-p', '--output-format', 'stream-json', '--verbose']
                if role == 'generator'
                else ['claude', '-p', '--output-format', 'json']
            )
            if role == 'generator':
                config = temporary / 'mcp.json'
                config.write_text(json.dumps({'mcpServers': {SERVER_NAME: {'command': str(mcp_binary), 'args': [], 'env': server_env}}}))
                # `claude -p` refuses writes nobody allowed, so the generator may edit its case folder only,
                # like Codex's workspace-write. `//` makes the rule's path absolute.
                command += ['--mcp-config', str(config), '--strict-mcp-config',
                            '--allowedTools', f'mcp__{SERVER_NAME}__*', f'Edit(/{Path(cwd).resolve()}/**)']
            if role == 'judge':
                command += ['--tools', 'Read', '--allowedTools', 'Read']
                for folder in sorted({str(Path(p).resolve().parent) for p in images}):
                    command += ['--add-dir', folder]
            if schema:
                command += ['--json-schema', json.dumps(schema)]
        elif backend == 'codex':
            command = ['codex', 'exec', '--json', '--skip-git-repo-check',
                       '--sandbox', 'read-only' if role == 'judge' else 'workspace-write',
                       '--output-last-message', str(final)]
            if role == 'generator':
                command += ['-c', f'mcp_servers.{SERVER_NAME}.command={json.dumps(str(mcp_binary))}',
                            '-c', f'mcp_servers.{SERVER_NAME}.args=[]',
                            '-c', f'mcp_servers.{SERVER_NAME}.default_tools_approval_mode="approve"']
                for k, v in server_env.items():
                    command += ['-c', f'mcp_servers.{SERVER_NAME}.env.{k}={json.dumps(v)}']
            for image in images:
                command += ['--image', str(Path(image).resolve())]
            if schema:
                schema_path = temporary / 'schema.json'
                schema_path.write_text(json.dumps(schema))
                command += ['--output-schema', str(schema_path)]
            command += ['-']
        else:
            stdin_input = None
            cwd_path = Path(cwd).resolve()
            if role == 'generator':
                agents_dir = cwd_path / '.agents'
                plugins_dir = agents_dir / 'plugins'
                plugin_dir = plugins_dir / AGY_PLUGIN
                had_agents = agents_dir.exists()
                had_plugins = plugins_dir.exists()
                plugin_dir.mkdir(parents=True, exist_ok=True)
                stack.callback(_cleanup_agy_plugin, plugin_dir, plugins_dir, agents_dir, had_plugins, had_agents)
                (plugin_dir / 'plugin.json').write_text(json.dumps({'name': AGY_PLUGIN}))
                (plugin_dir / 'mcp_config.json').write_text(json.dumps({
                    'mcpServers': {
                        'preview': {
                            'command': str(mcp_binary),
                            'args': [],
                            'env': server_env,
                        }
                    }
                }))
                agy_prompt = (
                    prompt.replace(SERVER_NAME, AGY_MCP_SERVER)
                    + f'\n\nUse MCP server `{AGY_MCP_SERVER}` for every previewer tool call '
                    + f'(global `{SERVER_NAME}` is disabled for this isolated run).'
                )
                settings = {
                    'allowNonWorkspaceAccess': True,
                    'artifactReviewPolicy': 'always-proceed',
                    'permissions': {
                        'allow': ['read_file(*)', f'write_file({cwd_path})', f'mcp({AGY_MCP_SERVER}/*)'],
                        'deny': ['command(*)', f'mcp({SERVER_NAME}/*)', 'read_url(*)', 'execute_url(*)'],
                    },
                }
                command = ['agy', '-p', agy_prompt, '--output-format', 'stream-json',
                           '--disable-slash-commands', '--settings', json.dumps(settings)]
            else:
                settings = {
                    'allowNonWorkspaceAccess': True,
                    'artifactReviewPolicy': 'always-proceed',
                    'permissions': {
                        'allow': ['read_file(*)'],
                        'deny': ['write_file(*)', 'command(*)', 'mcp(*)', 'read_url(*)', 'execute_url(*)'],
                    },
                }
                command = ['agy', '-p', prompt, '--output-format', 'stream-json',
                           '--disable-slash-commands', '--settings', json.dumps(settings)]
                for folder in sorted({str(Path(p).resolve().parent) for p in images}):
                    command += ['--add-dir', folder]
            if schema:
                command += ['--json-schema', json.dumps(schema)]
        if model:
            command += ['--model', model]
        run_kwargs = {'cwd': cwd, 'capture_output': True, 'text': True, 'timeout': timeout}
        if stdin_input is not None:
            run_kwargs['input'] = stdin_input
        else:
            run_kwargs['stdin'] = subprocess.DEVNULL
        started = time.monotonic()
        try:
            done = subprocess.run(command, **run_kwargs)
        except (OSError, subprocess.TimeoutExpired) as error:
            raise RuntimeError(f'{backend} {role} failed: {error}') from error
        wall_seconds = round(time.monotonic() - started, 2)
        if done.returncode:
            raise RuntimeError(f'{backend} {role} exited {done.returncode}: {(done.stderr or done.stdout)[-2000:]}')
        result = {
            'backend': backend,
            'model': model,
            'cost': None,
            'turns': None,
            'wall_seconds': wall_seconds,
            'input_tokens': None,
            'output_tokens': None,
            'thinking_tokens': None,
            'cache_read_tokens': None,
            'cache_creation_tokens': None,
            'check_calls': None,
            'compile_retries': None,
        }
        if backend == 'claude':
            message, parsed_fields = _parse_claude(done.stdout, role, model)
        elif backend == 'codex':
            message, parsed_fields = _parse_codex(done.stdout, final, role, model)
        else:
            message, parsed_fields = _parse_agy(done.stdout, role, model)
        result.update(parsed_fields)
        if not isinstance(message, str) or not message.strip():
            raise RuntimeError(f'{backend} {role} returned no final message')
        result['message'] = message
        return result
