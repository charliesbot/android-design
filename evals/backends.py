"""Optional CLI adapters. Evaluation artifacts do not depend on these launchers."""
import json
from pathlib import Path
import subprocess
import tempfile

BACKENDS = ('claude', 'codex')


def execute(backend, prompt, cwd, *, role, images=(), schema=None, model=None, timeout=3600):
    if backend not in BACKENDS:
        raise ValueError(f'Unknown backend: {backend}')
    if role not in ('generator', 'judge'):
        raise ValueError(f'Unknown role: {role}')
    with tempfile.TemporaryDirectory(prefix='android-design-backend-') as temporary:
        temporary = Path(temporary)
        final = temporary / 'final.txt'
        if backend == 'claude':
            command = ['claude', '-p', '--output-format', 'json']
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
