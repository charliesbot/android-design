"""Optional CLI adapters. Evaluation artifacts do not depend on these launchers.

A generator invokes the `android-design-preview` CLI from the explore skill's `scripts/` placed on `PATH`, passing
`ANDROID_DESIGN_NO_OPEN=1` and v7 bootstrap environment variables (`ANDROID_DESIGN_PREVIEW_SRC`,
`ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR`, etc.). Judges never receive the
preview CLI or shell access.
"""
import json
import os
import re
import shlex
import subprocess
import tempfile
import time
import tomllib
from pathlib import Path

from preview_app import (
    BOOTSTRAP_DIR_ENV,
    CACHE_DIR_ENV,
    CLIENT_TIMEOUT_ENV,
    NO_OPEN_ENV,
    SERVER_CP_ENV,
    SERVER_NAME,
    SRC_ENV,
)

BACKENDS = ('agy', 'claude', 'codex')

# Legacy v4 MCP names kept for parsing historical transcripts alongside v5 CLI invocations.
AGY_MCP_SERVER = 'eval-preview_preview'
CHECK_TOOLS = ('check_proposal', 'check_design')
RETRY_TOOLS = ('open_proposal', 'check_proposal', 'check_design')

PREVIEW_CLI_NAME = 'android-design-preview'
SKILL_SNAPSHOT_NAMES = ('android-design', 'android-design-explore')
SHELL_READ_COMMANDS = frozenset({
    'cat', 'sed', 'head', 'tail', 'nl', 'awk', 'grep', 'rg', 'cut', 'fold', 'pr', 'less', 'more',
})
CODEX_READONLY_COMMANDS = frozenset({
    'cat', 'head', 'tail', 'nl', 'ls', 'rg', 'grep', 'wc', 'sed',
})
SHELL_WRAPPERS = frozenset({'sh', 'bash', 'zsh', 'dash', 'ksh'})
SHELL_OPERATORS = frozenset({'|', '&&', '||', ';'})
NON_FILE_SKILL_DIRS = frozenset({'references', 'scripts', 'assets'})


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
    payload = _parse_tool_payload(output_text)
    if isinstance(payload, dict):
        if payload.get('valid') is False or payload.get('compiled') is False:
            return True
        if payload.get('valid') is True or payload.get('compiled') is True:
            return False
    return bool(is_error)


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


def _new_stats():
    return {
        'check_calls': 0,
        'compile_retries': 0,
        'skill_bytes_read': 0,
        'skill_read_calls': 0,
        'skill_files_read': {},
        'shell_commands': [],
    }


def _skill_fields(stats):
    return {
        'skill_bytes_read': stats['skill_bytes_read'],
        'skill_read_calls': stats['skill_read_calls'],
        'skill_unique_files': len(stats['skill_files_read']),
        'skill_files_read': dict(sorted(stats['skill_files_read'].items())),
    }


def _skill_rel_path(raw_path, cwd=None):
    """Return canonical '<skill>/...' path when raw_path points to a file inside the case's skills/ snapshot."""
    if not isinstance(raw_path, str):
        return None
    cleaned = raw_path.strip().strip('\'"')
    if not cleaned or cleaned.startswith('-'):
        return None
    p = Path(cleaned)
    if cwd is not None:
        cwd_root = Path(cwd).resolve()
        skills_root = cwd_root / 'skills'
        try:
            resolved = p.resolve() if p.is_absolute() else (cwd_root / p).resolve()
            rel = resolved.relative_to(skills_root)
        except (OSError, ValueError):
            return None
        if len(rel.parts) < 2 or rel.parts[0] not in SKILL_SNAPSHOT_NAMES:
            return None
        if resolved.is_dir():
            return None
        if not resolved.exists() and rel.parts[-1] in NON_FILE_SKILL_DIRS:
            return None
        return rel.as_posix()
    parts = p.parts
    for idx, part in enumerate(parts):
        if part == 'skills' and idx + 2 < len(parts) and parts[idx + 1] in SKILL_SNAPSHOT_NAMES:
            rel_parts = parts[idx + 1:]
            if rel_parts[-1] in NON_FILE_SKILL_DIRS:
                return None
            return '/'.join(rel_parts)
    return None


def _unwrap_outer_shell(command):
    """Unwrap one outer `/bin/zsh -lc "..."` or `bash -c "..."` harness wrapper into a raw shell string."""
    if isinstance(command, list):
        tokens = [str(part) for part in command if part is not None]
        if len(tokens) >= 3 and Path(tokens[0]).name in SHELL_WRAPPERS:
            for i, tok in enumerate(tokens[1:], start=1):
                if tok.startswith('-') and 'c' in tok[1:] and i + 1 < len(tokens):
                    return tokens[i + 1]
        return ' '.join(shlex.quote(t) for t in tokens)
    if not isinstance(command, str):
        return ''
    text = command.strip()
    try:
        parts = shlex.split(text)
    except ValueError:
        return text
    if len(parts) >= 3 and Path(parts[0]).name in SHELL_WRAPPERS:
        for i, tok in enumerate(parts[1:], start=1):
            if tok.startswith('-') and 'c' in tok[1:] and i + 1 < len(parts) and i + 1 == len(parts) - 1:
                return parts[i + 1]
    return text


def _scan_shell(raw_cmd):
    """Parse a raw shell command string into (segments, operators, violations).

    Detects unquoted redirections, command substitutions, backticks, subshells, newlines, and backgrounding.
    """
    violations = []
    if not isinstance(raw_cmd, str) or not raw_cmd.strip():
        return [], [], ['empty command']

    segments = []
    operators = []
    current_tokens = []
    buf = []
    in_token = False
    in_single = False
    in_double = False
    i = 0
    n = len(raw_cmd)

    def flush_token():
        nonlocal in_token
        if in_token or buf:
            current_tokens.append(''.join(buf))
            buf.clear()
            in_token = False

    def flush_segment():
        flush_token()
        if current_tokens:
            segments.append(list(current_tokens))
            current_tokens.clear()

    while i < n:
        ch = raw_cmd[i]
        if in_single:
            if ch == "'":
                in_single = False
            else:
                buf.append(ch)
            i += 1
            continue
        if in_double:
            if ch == '\\' and i + 1 < n:
                buf.append(raw_cmd[i + 1])
                i += 2
                continue
            if ch == '"':
                in_double = False
                i += 1
                continue
            if ch == '`':
                violations.append('backtick command substitution')
            elif ch == '$' and i + 1 < n and raw_cmd[i + 1] == '(':
                violations.append('$(...) command substitution')
            buf.append(ch)
            i += 1
            continue

        # Unquoted context
        if ch in ('\n', '\r'):
            violations.append('multi-line shell command')
            i += 1
            continue
        if ch == '\\' and i + 1 < n:
            in_token = True
            buf.append(raw_cmd[i + 1])
            i += 2
            continue
        if ch == "'":
            in_token = True
            in_single = True
            i += 1
            continue
        if ch == '"':
            in_token = True
            in_double = True
            i += 1
            continue
        if ch == '`':
            violations.append('backtick command substitution')
            i += 1
            continue
        if ch == '$' and i + 1 < n and raw_cmd[i + 1] == '(':
            violations.append('$(...) command substitution')
            i += 2
            continue
        if ch in ('(', ')'):
            violations.append('subshell or process substitution')
            i += 1
            continue
        if ch in ('>', '<'):
            violations.append(f'shell redirection ({ch})')
            i += 1
            continue
        if ch == '|':
            flush_segment()
            if i + 1 < n and raw_cmd[i + 1] == '|':
                operators.append('||')
                i += 2
            else:
                operators.append('|')
                i += 1
            continue
        if ch == '&':
            flush_segment()
            if i + 1 < n and raw_cmd[i + 1] == '&':
                operators.append('&&')
                i += 2
            else:
                violations.append('background operator (&)')
                i += 1
            continue
        if ch == ';':
            flush_segment()
            operators.append(';')
            i += 1
            continue
        if ch.isspace():
            flush_token()
            i += 1
            continue
        in_token = True
        buf.append(ch)
        i += 1

    if in_single or in_double:
        violations.append('unclosed quote')
    flush_segment()
    return segments, operators, violations


def _segment_command_and_args(segment):
    idx = 0
    while idx < len(segment) and '=' in segment[idx] and not segment[idx].startswith(('-', '/', '.')):
        idx += 1
    if idx >= len(segment):
        return '', []
    name, args = Path(segment[idx]).name, segment[idx + 1:]
    # The skill runs its bundled copy as `sh <skill-dir>/scripts/android-design-preview ...`.
    if name in ('sh', 'bash') and args and Path(args[0]).name == PREVIEW_CLI_NAME:
        return PREVIEW_CLI_NAME, args[1:]
    return name, args


def _sed_has_inplace_flag(args):
    for arg in args:
        if arg == '--':
            break
        if arg == '--in-place' or arg.startswith('--in-place='):
            return True
        if arg.startswith('-') and not arg.startswith('--') and 'i' in arg[1:]:
            return True
    return False


def audit_agy_shell_command(command):
    """Antigravity may run ONLY a single `android-design-preview` invocation (no chains, pipes, or other commands)."""
    raw = _unwrap_outer_shell(command)
    segments, operators, violations = _scan_shell(raw)
    if violations:
        return f'disallowed shell syntax ({", ".join(violations)}): {raw}'
    if operators or len(segments) != 1:
        return f'chained or piped shell command is not allowed: {raw}'
    cmd_name, _ = _segment_command_and_args(segments[0])
    if cmd_name != PREVIEW_CLI_NAME:
        return f'disallowed command {cmd_name!r} (only {PREVIEW_CLI_NAME} is allowed): {raw}'
    return None


def audit_codex_shell_command(command):
    """Codex may run `android-design-preview` and read-only file commands (`cat`, `head`, `tail`, `nl`, `ls`,
    `rg`, `grep`, `wc`, and `sed` without `-i`), alone or piped into each other."""
    raw = _unwrap_outer_shell(command)
    segments, operators, violations = _scan_shell(raw)
    if violations:
        return f'disallowed shell syntax ({", ".join(violations)}): {raw}'
    if not segments:
        return f'empty shell command: {raw}'
    if len(operators) >= len(segments):
        return f'malformed shell operators: {raw}'
    for seg in segments:
        cmd_name, args = _segment_command_and_args(seg)
        if cmd_name == PREVIEW_CLI_NAME:
            continue
        if cmd_name not in CODEX_READONLY_COMMANDS:
            return f'disallowed command {cmd_name!r}: {raw}'
        if cmd_name == 'sed' and _sed_has_inplace_flag(args):
            return f'sed in-place edit (-i) is not allowed: {raw}'
    return None


def _extract_shell_skill_paths(command, cwd=None):
    raw = _unwrap_outer_shell(command)
    segments, _, _ = _scan_shell(raw)
    matched = []
    for seg in segments:
        cmd_name, args = _segment_command_and_args(seg)
        if cmd_name not in SHELL_READ_COMMANDS:
            continue
        for arg in args:
            rel = _skill_rel_path(arg, cwd)
            if rel:
                matched.append(rel)
    return matched


def _is_harness_permission_refusal(output_text, is_error):
    if not is_error or not isinstance(output_text, str):
        return False
    if '{' in output_text:
        return False
    lowered = output_text.lower()
    return (
        'requires approval' in lowered
        or 'requested permissions' in lowered
        or 'permission denied' in lowered
    )


def _truncated_json_valid_flag(output_text):
    if not isinstance(output_text, str) or '{' not in output_text:
        return None
    head = output_text[output_text.find('{'):][:400]
    match = re.search(r'"(?:valid|compiled)"\s*:\s*(true|false)', head)
    if not match:
        return None
    return match.group(1) == 'true'


def _record_cli_preview_command(stats, command, output_text, is_error):
    if _is_harness_permission_refusal(output_text, is_error):
        return
    raw = _unwrap_outer_shell(command)
    segments, _, _ = _scan_shell(raw)
    for seg in segments:
        cmd_name, args = _segment_command_and_args(seg)
        if cmd_name == PREVIEW_CLI_NAME and args and args[0] == 'check':
            stats['check_calls'] += 1
            payload = _parse_tool_payload(output_text)
            if isinstance(payload, dict):
                if payload.get('valid') is False or payload.get('compiled') is False:
                    stats['compile_retries'] += 1
            else:
                valid_flag = _truncated_json_valid_flag(output_text)
                if valid_flag is False or (valid_flag is None and is_error):
                    stats['compile_retries'] += 1


def _skill_output_byte_count(output_text):
    text = output_text or ''
    summary = re.fullmatch(r'\s*\d+\s+lines?,\s*(\d+)\s+bytes?\s*', text)
    if summary:
        return int(summary.group(1))
    return len(text.encode('utf-8'))


def _record_skill_read(stats, paths, output_text, is_error):
    if is_error or not paths:
        return
    unique_in_call = list(dict.fromkeys(p for p in paths if p))
    if not unique_in_call:
        return
    byte_count = _skill_output_byte_count(output_text)
    stats['skill_bytes_read'] += byte_count
    stats['skill_read_calls'] += 1
    files = stats['skill_files_read']
    if len(unique_in_call) == 1:
        rel = unique_in_call[0]
        entry = files.setdefault(rel, {'reads': 0, 'bytes': 0})
        entry['reads'] += 1
        entry['bytes'] += byte_count
    else:
        share, rem = divmod(byte_count, len(unique_in_call))
        for idx, rel in enumerate(unique_in_call):
            entry = files.setdefault(rel, {'reads': 0, 'bytes': 0})
            entry['reads'] += 1
            entry['bytes'] += share + (1 if idx < rem else 0)


def _consume_claude_events(events, stats, pending_tools, cwd=None):
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
                        tinput = block.get('input') if isinstance(block.get('input'), dict) else {}
                        pending_tools[tid] = (block.get('name') or '', tinput)
        elif etype == 'user':
            for block in (event.get('message') or {}).get('content') or []:
                if isinstance(block, dict) and block.get('type') == 'tool_result':
                    tname, tinput = pending_tools.get(block.get('tool_use_id'), ('', {}))
                    output_text = _extract_text(block.get('content'))
                    is_error = bool(block.get('is_error'))
                    _record_preview_tool(stats, tname, output_text, is_error)
                    if tname in ('Read', 'read_file', 'view_file'):
                        raw_path = (
                            tinput.get('file_path')
                            or tinput.get('Path')
                            or tinput.get('path')
                            or tinput.get('AbsolutePath')
                        )
                        rel = _skill_rel_path(raw_path, cwd)
                        if rel:
                            _record_skill_read(stats, [rel], output_text, is_error)
                    elif tname in ('Bash', 'run_command', 'shell'):
                        cmd = tinput.get('command') or tinput.get('CommandLine')
                        if cmd:
                            stats['shell_commands'].append(cmd)
                            _record_cli_preview_command(stats, cmd, output_text, is_error)
                            _record_skill_read(stats, _extract_shell_skill_paths(cmd, cwd), output_text, is_error)
        elif etype == 'result':
            reply = event
    return reply


def _parse_claude(stdout, role, model, cwd=None):
    reply = None
    saw_stream = False
    stats = _new_stats()
    pending_tools = {}
    try:
        parsed = json.loads(stdout)
        if isinstance(parsed, dict):
            reply = parsed
            saw_stream = parsed.get('type') == 'result'
        elif isinstance(parsed, list):
            saw_stream = True
            reply = _consume_claude_events(parsed, stats, pending_tools, cwd=cwd)
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
            reply = _consume_claude_events(events, stats, pending_tools, cwd=cwd)
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
    if isinstance(reply.get('session_id'), str) and reply['session_id'].strip():
        fields['session_id'] = reply['session_id'].strip()
    if role == 'generator' and saw_stream:
        fields['check_calls'] = stats['check_calls']
        fields['compile_retries'] = stats['compile_retries']
        fields.update(_skill_fields(stats))
    return message, fields


def _parse_codex(stdout, final_path, role, model, cwd=None):
    message = final_path.read_text() if final_path.exists() else ''
    saw_events = False
    agent_messages = 0
    stats = _new_stats()
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
            elif itype in ('command_execution', 'local_shell_call'):
                cmd = item.get('command')
                output = item.get('aggregated_output')
                if not isinstance(output, str):
                    output = _extract_text(item.get('output'))
                exit_code = item.get('exit_code')
                is_err = (exit_code is not None and exit_code != 0) or item.get('status') == 'failed'
                if cmd:
                    stats['shell_commands'].append(cmd)
                    _record_cli_preview_command(stats, cmd, output, is_err)
                    _record_skill_read(stats, _extract_shell_skill_paths(cmd, cwd), output, is_err)
            elif itype in ('read_file', 'file_read'):
                raw_path = item.get('path') or item.get('file_path')
                output = item.get('content') if isinstance(item.get('content'), str) else _extract_text(item.get('output'))
                is_err = bool(item.get('error')) or item.get('status') == 'failed'
                rel = _skill_rel_path(raw_path, cwd)
                if rel:
                    _record_skill_read(stats, [rel], output, is_err)
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
    if role == 'generator':
        for cmd in stats['shell_commands']:
            violation = audit_codex_shell_command(cmd)
            if violation:
                raise RuntimeError(f'codex script-ban audit failed: {violation}')
    fields = {'model': model or event_model or _default_model('codex')}
    if saw_events:
        fields['turns'] = max(agent_messages, 1)
        fields.update(usage_totals)
        if role == 'generator':
            fields['check_calls'] = stats['check_calls']
            fields['compile_retries'] = stats['compile_retries']
            fields.update(_skill_fields(stats))
    return message, fields


def _parse_agy(stdout, role, model, cwd=None):
    terminal = None
    init_model = None
    agent_steps = 0
    stats = _new_stats()
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
                    params = info.get('parameters') if isinstance(info.get('parameters'), dict) else {}
                    if info.get('name') == 'call_mcp_tool':
                        tname = params.get('ToolName') or ''
                    else:
                        tname = su.get('tool_name') or info.get('name') or ''
                    output_text = info.get('output') or ''
                    is_error = bool(info.get('error'))
                    _record_preview_tool(stats, tname, output_text, is_error)
                    if tname in ('read_file', 'view_file'):
                        raw_path = (
                            params.get('Path')
                            or params.get('path')
                            or params.get('file_path')
                            or params.get('AbsolutePath')
                        )
                        rel = _skill_rel_path(raw_path, cwd)
                        if rel:
                            _record_skill_read(stats, [rel], output_text, is_error)
                    elif tname in ('run_command', 'shell'):
                        cmd = params.get('CommandLine') or params.get('command')
                        if cmd:
                            stats['shell_commands'].append(cmd)
                            _record_cli_preview_command(stats, cmd, output_text, is_error)
                            _record_skill_read(stats, _extract_shell_skill_paths(cmd, cwd), output_text, is_error)
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
    if role == 'generator':
        for cmd in stats['shell_commands']:
            violation = audit_agy_shell_command(cmd)
            if violation:
                raise RuntimeError(f'agy script-ban audit failed: {violation}')
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
        fields.update(_skill_fields(stats))
    return message, fields


def execute(backend, prompt, cwd, *, role, images=(), schema=None, model=None, timeout=3600,
            mcp_binary=None, preview_src=None, bootstrap_dir=None,
            cache_dir=None, client_timeout_ms=None, resume_session=None):
    if backend not in BACKENDS:
        raise ValueError(f'Unknown backend: {backend}')
    if role not in ('generator', 'judge'):
        raise ValueError(f'Unknown role: {role}')
    if role == 'generator' and not mcp_binary:
        raise ValueError('A generator needs mcp_binary, the preview CLI executable it designs with')
    if resume_session and backend != 'claude':
        raise ValueError(f'resume_session is only supported on claude, not {backend}')
    with tempfile.TemporaryDirectory(prefix='android-design-backend-') as temporary:
        temporary = Path(temporary)
        final = temporary / 'final.txt'
        proc_env = dict(os.environ)
        if role == 'generator':
            bin_dir = str(Path(mcp_binary).resolve().parent)
            proc_env['PATH'] = bin_dir + os.pathsep + proc_env.get('PATH', '')
            proc_env[NO_OPEN_ENV] = '1'
            if preview_src is not None:
                proc_env[SRC_ENV] = str(preview_src)
            if bootstrap_dir is not None:
                bdir = Path(bootstrap_dir)
                proc_env[BOOTSTRAP_DIR_ENV] = str(bdir)
                proc_env.pop(SERVER_CP_ENV, None)
                if (bdir / 'server.cp').is_file():
                    proc_env[SERVER_CP_ENV] = str(bdir / 'server.cp')
            if cache_dir is not None:
                proc_env[CACHE_DIR_ENV] = str(cache_dir)
            if client_timeout_ms is not None:
                proc_env[CLIENT_TIMEOUT_ENV] = str(client_timeout_ms)

        stdin_input = prompt
        cwd_path = Path(cwd).resolve()
        if backend == 'claude':
            command = (
                ['claude', '-p', '--output-format', 'stream-json', '--verbose']
                if role == 'generator'
                else ['claude', '-p', '--output-format', 'json']
            )
            if resume_session:
                command += ['--resume', str(resume_session)]
            if role == 'generator':
                # `claude -p` enforces the script ban directly via --allowedTools: only
                # `android-design-preview` shell commands, edits/writes inside the case folder,
                # and reading files including rendered PNGs in ~/.cache/android-design-preview.
                default_cache = (Path.home() / '.cache' / 'android-design-preview').resolve()
                default_cache.mkdir(parents=True, exist_ok=True)
                command += [
                    '--allowedTools',
                    f'Bash({PREVIEW_CLI_NAME}:*)',
                    f'Edit(/{cwd_path}/**)',
                    f'Write(/{cwd_path}/**)',
                    'Read',
                    '--add-dir',
                    str(default_cache),
                ]
                if cache_dir is not None:
                    resolved_cache = Path(cache_dir).resolve()
                    resolved_cache.mkdir(parents=True, exist_ok=True)
                    if resolved_cache != default_cache and default_cache not in resolved_cache.parents:
                        command += ['--add-dir', str(resolved_cache)]
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
                default_cache = (Path.home() / '.cache' / 'android-design-preview').resolve()
                gradle_dir = (Path.home() / '.gradle').resolve()
                writable_roots = [str(default_cache)]
                if cache_dir is not None:
                    rc = str(Path(cache_dir).resolve())
                    if rc not in writable_roots:
                        writable_roots.append(rc)
                writable_roots.append(str(gradle_dir))
                command += [
                    '-c', 'sandbox_workspace_write.network_access=true',
                    '-c', f'sandbox_workspace_write.writable_roots={json.dumps(writable_roots)}',
                ]
            for image in images:
                command += ['--image', str(Path(image).resolve())]
            if schema:
                schema_path = temporary / 'schema.json'
                schema_path.write_text(json.dumps(schema))
                command += ['--output-schema', str(schema_path)]
            command += ['-']
        else:
            stdin_input = None
            if role == 'generator':
                settings = {
                    'allowNonWorkspaceAccess': True,
                    'artifactReviewPolicy': 'always-proceed',
                    'permissions': {
                        'allow': ['read_file(*)', f'write_file({cwd_path})', 'command(*)'],
                        'deny': ['mcp(*)', 'read_url(*)', 'execute_url(*)'],
                    },
                }
                command = ['agy', '-p', prompt, '--output-format', 'stream-json',
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
        run_kwargs = {'cwd': cwd, 'env': proc_env, 'capture_output': True, 'text': True, 'timeout': timeout}
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
            'skill_bytes_read': None,
            'skill_read_calls': None,
            'skill_unique_files': None,
            'skill_files_read': None,
        }
        if backend == 'claude':
            message, parsed_fields = _parse_claude(done.stdout, role, model, cwd=cwd)
        elif backend == 'codex':
            message, parsed_fields = _parse_codex(done.stdout, final, role, model, cwd=cwd)
        else:
            message, parsed_fields = _parse_agy(done.stdout, role, model, cwd=cwd)
        result.update(parsed_fields)
        if not isinstance(message, str) or not message.strip():
            raise RuntimeError(f'{backend} {role} returned no final message')
        result['message'] = message
        return result
