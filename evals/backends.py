"""Optional CLI adapters. Evaluation artifacts do not depend on these launchers.

A generator reaches the previewer app over MCP, registered as `android_design_preview` for that one run only, with its
own socket so the run starts a fresh instance of `mcp_binary` and never forwards to an app that is already running. A
judge never sees the app.
"""
import json
from contextlib import ExitStack
from pathlib import Path
import subprocess
import tempfile

from preview_app import SERVER_NAME

BACKENDS = ('claude', 'codex')

# The app's override for its instance socket (InstanceSocket.SOCKET_ENV in app/).
SOCKET_ENV = 'ANDROID_DESIGN_PREVIEW_SOCKET'


def execute(backend, prompt, cwd, *, role, images=(), schema=None, model=None, timeout=3600, mcp_binary=None):
    if backend not in BACKENDS:
        raise ValueError(f'Unknown backend: {backend}')
    if role not in ('generator', 'judge'):
        raise ValueError(f'Unknown role: {role}')
    if role == 'generator' and not mcp_binary:
        raise ValueError('A generator needs mcp_binary, the previewer app it designs with')
    with tempfile.TemporaryDirectory(prefix='android-design-backend-') as temporary, ExitStack() as stack:
        temporary = Path(temporary)
        final = temporary / 'final.txt'
        socket = None
        if role == 'generator':
            # Unix socket paths are limited to about 104 bytes on macOS, so the socket lives under /tmp, not the usual
            # temp dir.
            socket = str(Path(stack.enter_context(tempfile.TemporaryDirectory(prefix='adp-', dir='/tmp'))) / 's.sock')
        if backend == 'claude':
            command = ['claude', '-p', '--output-format', 'json']
            if role == 'generator':
                config = temporary / 'mcp.json'
                config.write_text(json.dumps({'mcpServers': {SERVER_NAME: {'command': str(mcp_binary), 'args': [], 'env': {SOCKET_ENV: socket}}}}))
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
        else:
            command = ['codex', 'exec', '--skip-git-repo-check',
                       '--sandbox', 'read-only' if role == 'judge' else 'workspace-write',
                       '--output-last-message', str(final)]
            if role == 'generator':
                command += ['-c', f'mcp_servers.{SERVER_NAME}.command={json.dumps(str(mcp_binary))}',
                            '-c', f'mcp_servers.{SERVER_NAME}.args=[]',
                            '-c', f'mcp_servers.{SERVER_NAME}.default_tools_approval_mode="approve"',
                            '-c', f'mcp_servers.{SERVER_NAME}.env.{SOCKET_ENV}={json.dumps(socket)}']
            for image in images:
                command += ['--image', str(Path(image).resolve())]
            if schema:
                schema_path = temporary / 'schema.json'
                schema_path.write_text(json.dumps(schema))
                command += ['--output-schema', str(schema_path)]
            command += ['-']
        if model:
            command += ['--model', model]
        try:
            done = subprocess.run(command, input=prompt, cwd=cwd, capture_output=True,
                                  text=True, timeout=timeout)
        except (OSError, subprocess.TimeoutExpired) as error:
            raise RuntimeError(f'{backend} {role} failed: {error}') from error
        if done.returncode:
            raise RuntimeError(f'{backend} {role} exited {done.returncode}: {(done.stderr or done.stdout)[-2000:]}')
        result = {'backend': backend, 'model': model, 'cost': None, 'turns': None}
        if backend == 'claude':
            try:
                reply = json.loads(done.stdout)
                if not isinstance(reply, dict):
                    raise ValueError('expected an object')
                if reply.get('is_error'):
                    raise RuntimeError(f'claude {role}: {reply.get("result", "failed")}')
                structured = reply.get('structured_output')
                message = json.dumps(structured) if structured is not None else reply.get('result', '')
                result.update(cost=reply.get('total_cost_usd'), turns=reply.get('num_turns'))
            except ValueError as error:
                raise RuntimeError('claude returned an invalid response envelope') from error
        else:
            message = final.read_text() if final.exists() else ''
        if not isinstance(message, str) or not message.strip():
            raise RuntimeError(f'{backend} {role} returned no final message')
        result['message'] = message
        return result
