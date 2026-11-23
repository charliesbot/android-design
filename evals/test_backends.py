import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import backends

BINARY = '/Apps/plugin/bin/android-design-preview'


class BackendTests(unittest.TestCase):
    def test_claude_normalizes_reply_without_permission_bypass(self):
        reply = {'result': 'finished', 'total_cost_usd': 0.2, 'num_turns': 3}
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, json.dumps(reply), '')) as call:
            result = backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)
        self.assertEqual(result['message'], 'finished')
        self.assertEqual(result['cost'], 0.2)
        self.assertNotIn('--dangerously-skip-permissions', call.call_args.args[0])
        self.assertEqual(call.call_args.kwargs['input'], 'brief')

    def test_codex_uses_final_message_and_read_only_judging(self):
        def execute(cmd, **kwargs):
            Path(cmd[cmd.index('--output-last-message') + 1]).write_text('{"items": []}')
            self.assertIn('read-only', cmd)
            self.assertIn('--image', cmd)
            return subprocess.CompletedProcess(cmd, 0, '', '')
        with patch('backends.subprocess.run', side_effect=execute):
            result = backends.execute('codex', 'judge', Path('/tmp'), role='judge', images=['/tmp/a.png'], schema={'type': 'object'})
        self.assertEqual(result['message'], '{"items": []}')
        self.assertIsNone(result['cost'])

    def test_failed_process_is_not_a_successful_generation(self):
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 1, '', 'denied')):
            with self.assertRaisesRegex(RuntimeError, 'denied'):
                backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)

    def test_claude_error_envelope_is_rejected(self):
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, '{"is_error":true,"result":"denied"}', '')):
            with self.assertRaisesRegex(RuntimeError, 'denied'):
                backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)

    def test_claude_generator_uses_cli_allowlist_and_v6_env_and_strips_stale_jar(self):
        with tempfile.TemporaryDirectory() as tmp:
            bdir = Path(tmp) / 'bootstrap'
            bdir.mkdir()
            (bdir / 'server.cp').write_text('-cp\na.jar\n')
            (bdir / 'robolectric.cp').write_text('-cp\nb.jar\n')
            custom_cache = Path(tmp) / 'eval-cache'
            with patch.dict('backends.os.environ', {'ANDROID_DESIGN_PREVIEW_JAR': '/stale/legacy.jar'}), \
                    patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, '{"result": "ok"}', '')) as call:
                backends.execute(
                    'claude',
                    'brief',
                    Path('/tmp/case'),
                    role='generator',
                    mcp_binary=BINARY,
                    preview_src='/tmp/repo/app',
                    bootstrap_dir=bdir,
                    preview_backend='robolectric',
                    cache_dir=custom_cache,
                    client_timeout_ms=120000,
                )
        cmd = call.call_args.args[0]
        env = call.call_args.kwargs['env']
        self.assertNotIn('--mcp-config', cmd)
        self.assertNotIn('--strict-mcp-config', cmd)
        self.assertNotIn('--dangerously-skip-permissions', cmd)
        self.assertNotIn('--permission-mode', cmd)
        idx = cmd.index('--allowedTools')
        self.assertEqual(
            cmd[idx + 1:idx + 4],
            [
                'Bash(android-design-preview:*)',
                f'Edit(/{Path("/tmp/case").resolve()}/**)',
                f'Write(/{Path("/tmp/case").resolve()}/**)',
            ],
        )
        self.assertIn(str(custom_cache.resolve()), cmd)
        self.assertEqual(env['PATH'].split(os.pathsep)[0], str(Path(BINARY).resolve().parent))
        self.assertEqual(env['ANDROID_DESIGN_NO_OPEN'], '1')
        self.assertNotIn('ANDROID_DESIGN_PREVIEW_JAR', env)
        self.assertEqual(env['ANDROID_DESIGN_PREVIEW_SRC'], '/tmp/repo/app')
        self.assertEqual(env['ANDROID_DESIGN_PREVIEW_BOOTSTRAP_DIR'], str(bdir))
        self.assertEqual(env['ANDROID_DESIGN_PREVIEW_SERVER_CP'], str(bdir / 'server.cp'))
        self.assertEqual(env['ANDROID_DESIGN_PREVIEW_ROBOLECTRIC_CP'], str(bdir / 'robolectric.cp'))
        self.assertEqual(env['ANDROID_DESIGN_PREVIEW_BACKEND'], 'robolectric')
        self.assertEqual(env['ANDROID_DESIGN_CACHE_DIR'], str(custom_cache))
        self.assertEqual(env['ANDROID_DESIGN_CLIENT_TIMEOUT_MS'], '120000')

    def test_claude_judge_gets_no_mcp_or_bash(self):
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, '{"result": "ok"}', '')) as call:
            backends.execute('claude', 'judge', Path('/tmp'), role='judge', images=['/tmp/a.png'])
        cmd = call.call_args.args[0]
        self.assertNotIn('--mcp-config', cmd)
        self.assertEqual(cmd[cmd.index('--allowedTools') + 1], 'Read')

    def test_codex_generator_sets_cli_path_env_and_workspace_sandbox_without_mcp(self):
        def execute(cmd, **kwargs):
            Path(cmd[cmd.index('--output-last-message') + 1]).write_text('done')
            return subprocess.CompletedProcess(cmd, 0, '', '')

        with patch('backends.subprocess.run', side_effect=execute) as call:
            backends.execute(
                'codex',
                'brief',
                Path('/tmp'),
                role='generator',
                mcp_binary=BINARY,
                preview_src='/tmp/repo/app',
                preview_backend='robolectric',
            )
        cmd = call.call_args.args[0]
        env = call.call_args.kwargs['env']
        overrides = [cmd[i + 1] for i, part in enumerate(cmd) if part == '-c']
        self.assertIn('sandbox_workspace_write.network_access=true', overrides)
        roots_override = next(o for o in overrides if o.startswith('sandbox_workspace_write.writable_roots='))
        roots = json.loads(roots_override.split('=', 1)[1])
        self.assertIn(str((Path.home() / '.gradle').resolve()), roots)
        self.assertFalse(any('mcp_servers' in o for o in overrides))
        self.assertEqual(env['PATH'].split(os.pathsep)[0], str(Path(BINARY).resolve().parent))
        self.assertEqual(env['ANDROID_DESIGN_NO_OPEN'], '1')
        self.assertEqual(env['ANDROID_DESIGN_PREVIEW_SRC'], '/tmp/repo/app')
        self.assertEqual(env['ANDROID_DESIGN_PREVIEW_BACKEND'], 'robolectric')
        self.assertEqual(cmd[-1], '-')

    def test_codex_judge_gets_no_sandbox_network_override(self):
        def execute(cmd, **kwargs):
            Path(cmd[cmd.index('--output-last-message') + 1]).write_text('{}')
            return subprocess.CompletedProcess(cmd, 0, '', '')

        with patch('backends.subprocess.run', side_effect=execute) as call:
            backends.execute('codex', 'judge', Path('/tmp'), role='judge')
        self.assertNotIn('-c', call.call_args.args[0])

    def test_generator_without_the_app_is_rejected_before_launch(self):
        for backend in backends.BACKENDS:
            with patch('backends.subprocess.run') as call, self.assertRaisesRegex(ValueError, 'mcp_binary'):
                backends.execute(backend, 'brief', Path('/tmp'), role='generator')
            call.assert_not_called()

    def test_unknown_backend_is_rejected_before_launch(self):
        with patch('backends.subprocess.run') as call:
            with self.assertRaises(ValueError):
                backends.execute('unknown', 'brief', Path('/tmp'), role='generator')
        call.assert_not_called()

    def test_claude_extracts_tokens_model_usage_and_wall_seconds(self):
        reply = {
            'result': 'finished',
            'total_cost_usd': 0.42,
            'num_turns': 7,
            'usage': {
                'input_tokens': 1200,
                'output_tokens': 340,
                'cache_read_input_tokens': 800,
                'cache_creation_input_tokens': 150,
            },
            'modelUsage': {'claude-opus-4-1': {'inputTokens': 1200}},
        }
        with patch('backends.time.monotonic', side_effect=[10.0, 22.346]), \
                patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, json.dumps(reply), '')):
            result = backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)
        self.assertEqual(result['model'], 'claude-opus-4-1')
        self.assertEqual(result['turns'], 7)
        self.assertEqual(result['wall_seconds'], 12.35)
        self.assertEqual(result['input_tokens'], 1200)
        self.assertEqual(result['output_tokens'], 340)
        self.assertEqual(result['cache_read_tokens'], 800)
        self.assertEqual(result['cache_creation_tokens'], 150)

    def test_codex_jsonl_tracks_cli_check_calls_compile_retries_and_usage(self):
        events = [
            {'type': 'thread.started', 'thread_id': 't-1'},
            {'type': 'item.completed', 'item': {'id': 'i0', 'type': 'agent_message', 'text': 'Checking'}},
            {'type': 'item.completed', 'item': {
                'id': 'i1', 'type': 'command_execution',
                'command': '/bin/zsh -lc "android-design-preview check design/proposal.explore.kt"',
                'aggregated_output': json.dumps({'ok': False, 'valid': False, 'pass': False, 'errors': [{'message': 'Unresolved'}]}),
                'exit_code': 1, 'status': 'completed',
            }},
            {'type': 'item.completed', 'item': {
                'id': 'i2', 'type': 'command_execution',
                'command': 'android-design-preview check design/proposal.explore.kt --direction Main --view light',
                'aggregated_output': json.dumps({'ok': False, 'valid': True, 'pass': False}),
                'exit_code': 1, 'status': 'completed',
            }},
            {'type': 'item.completed', 'item': {
                'id': 'i3', 'type': 'command_execution',
                'command': 'android-design-preview check design/proposal.explore.kt',
                'aggregated_output': json.dumps({'ok': True, 'valid': True, 'pass': True}),
                'exit_code': 0, 'status': 'completed',
            }},
            {'type': 'item.completed', 'item': {'id': 'i5', 'type': 'agent_message', 'text': 'Done'}},
            {'type': 'turn.completed', 'usage': {
                'input_tokens': 5000, 'cached_input_tokens': 3000, 'cache_write_input_tokens': 0,
                'output_tokens': 410, 'reasoning_output_tokens': 120,
            }},
        ]
        stdout = '\n'.join(json.dumps(e) for e in events) + '\n'

        def fake_run(cmd, **kwargs):
            self.assertIn('--json', cmd)
            Path(cmd[cmd.index('--output-last-message') + 1]).write_text('Done')
            return subprocess.CompletedProcess(cmd, 0, stdout, '')

        with tempfile.TemporaryDirectory() as home, \
                patch('backends.Path.home', return_value=Path(home)), \
                patch('backends.subprocess.run', side_effect=fake_run):
            (Path(home) / '.codex').mkdir()
            (Path(home) / '.codex/config.toml').write_text('model = "gpt-6-astra"\n')
            result = backends.execute('codex', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)

        self.assertEqual(result['model'], 'gpt-6-astra')
        self.assertEqual(result['turns'], 2)
        self.assertEqual(result['input_tokens'], 5000)
        self.assertEqual(result['output_tokens'], 410)
        self.assertEqual(result['cache_read_tokens'], 3000)
        self.assertEqual(result['thinking_tokens'], 120)
        self.assertEqual(result['check_calls'], 3)
        self.assertEqual(result['compile_retries'], 1)

    def test_agy_generator_runs_cli_without_temp_mcp_plugin_and_tracks_checks(self):
        events = [
            {'event': 'init', 'init': {'cwd': '/tmp', 'tools': []}},
            {'event': 'step_update', 'step_update': {'step_index': 1, 'state': 'DONE', 'step_type': 'agent_response'}},
            {'event': 'step_update', 'step_update': {
                'step_index': 2, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'run_command',
                'tool_info': {
                    'name': 'run_command',
                    'parameters': {'CommandLine': 'android-design-preview check design/proposal.explore.kt'},
                    'output': json.dumps({'ok': False, 'valid': False, 'errors': [{'message': 'missing import'}]}),
                },
            }},
            {'event': 'step_update', 'step_update': {'step_index': 3, 'state': 'DONE', 'step_type': 'agent_response'}},
            {'event': 'step_update', 'step_update': {
                'step_index': 4, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'run_command',
                'tool_info': {
                    'name': 'run_command',
                    'parameters': {'CommandLine': 'android-design-preview check design/proposal.explore.kt --direction Main --view light'},
                    'output': json.dumps({'ok': False, 'valid': True, 'pass': False}),
                },
            }},
            {'event': 'step_update', 'step_update': {'step_index': 5, 'state': 'DONE', 'step_type': 'agent_response'}},
            {'event': 'result', 'result': {
                'status': 'SUCCESS', 'response': 'Design ready.', 'num_turns': 1,
                'usage': {'input_tokens': 21000, 'output_tokens': 950, 'thinking_tokens': 400, 'cache_read_tokens': 14000},
            }},
        ]
        stdout = '\n'.join(json.dumps(e) for e in events) + '\n'
        seen = {}

        with tempfile.TemporaryDirectory() as case_tmp, tempfile.TemporaryDirectory() as home_tmp:
            case_dir = Path(case_tmp).resolve()
            (Path(home_tmp) / '.gemini/antigravity-cli').mkdir(parents=True)
            (Path(home_tmp) / '.gemini/antigravity-cli/settings.json').write_text(json.dumps({'model': 'Gemini 3.8 Flash (High)'}))

            def fake_run(cmd, **kwargs):
                seen['cmd'] = cmd
                seen['env'] = kwargs.get('env')
                seen['stdin'] = kwargs.get('stdin')
                seen['had_agents'] = (case_dir / '.agents').exists()
                return subprocess.CompletedProcess(cmd, 0, stdout, '')

            with patch('backends.Path.home', return_value=Path(home_tmp)), \
                    patch('backends.subprocess.run', side_effect=fake_run):
                result = backends.execute('agy', 'Use android-design-preview.', case_dir,
                                          role='generator', mcp_binary=BINARY, mcp_jar='/tmp/w.jar')

        self.assertFalse(seen['had_agents'])
        self.assertEqual(seen['stdin'], subprocess.DEVNULL)
        self.assertEqual(seen['env']['PATH'].split(os.pathsep)[0], str(Path(BINARY).resolve().parent))
        self.assertEqual(seen['env']['ANDROID_DESIGN_NO_OPEN'], '1')
        self.assertEqual(seen['env']['ANDROID_DESIGN_PREVIEW_JAR'], '/tmp/w.jar')
        cmd = seen['cmd']
        settings = json.loads(cmd[cmd.index('--settings') + 1])
        self.assertIn(f'write_file({case_dir})', settings['permissions']['allow'])
        self.assertIn('command(*)', settings['permissions']['allow'])
        self.assertIn('mcp(*)', settings['permissions']['deny'])
        self.assertEqual(result['model'], 'Gemini 3.8 Flash (High)')
        self.assertEqual(result['turns'], 3)
        self.assertEqual(result['check_calls'], 2)
        self.assertEqual(result['compile_retries'], 1)

    def test_agy_transcript_audit_allows_single_preview_cli_and_rejects_other_commands_or_chains(self):
        self.assertIsNone(backends.audit_agy_shell_command('android-design-preview check design/proposal.explore.kt --direction "Editorial split" --view light'))
        self.assertIsNone(backends.audit_agy_shell_command('android-design-preview tree design/proposal.explore.kt --direction Main --view desktop'))

        disallowed = [
            'python3 gen.py',
            'cat skills/android-design/SKILL.md',
            'android-design-preview check a.kt && python3 gen.py',
            'android-design-preview check a.kt && android-design-preview status',
            'android-design-preview check a.kt ; ls',
            'android-design-preview check a.kt | head -n 5',
            'android-design-preview check $(echo a.kt)',
            'android-design-preview check `echo a.kt`',
            'android-design-preview check a.kt > out.json',
        ]
        for bad in disallowed:
            self.assertIsNotNone(backends.audit_agy_shell_command(bad), f'should fail agy audit: {bad}')

        # Verify execute() actually raises RuntimeError when an agy transcript runs a disallowed shell command.
        bad_events = [
            {'event': 'step_update', 'step_update': {
                'step_index': 1, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'run_command',
                'tool_info': {'name': 'run_command', 'parameters': {'CommandLine': 'android-design-preview status && python3 -c "print(1)"'}, 'output': 'ok'},
            }},
            {'event': 'result', 'result': {'status': 'SUCCESS', 'response': 'Done.'}},
        ]
        stdout = '\n'.join(json.dumps(e) for e in bad_events) + '\n'
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, stdout, '')):
            with self.assertRaisesRegex(RuntimeError, 'agy script-ban audit failed'):
                backends.execute('agy', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)

    def test_audits_allow_the_skills_bundled_preview_script(self):
        bundled = [
            'sh /work/skills/android-design-explore/scripts/android-design-preview check design/proposal.explore.kt',
            '/work/skills/android-design-explore/scripts/android-design-preview status',
        ]
        for ok in bundled:
            self.assertIsNone(backends.audit_agy_shell_command(ok), f'should pass agy audit: {ok}')
            self.assertIsNone(backends.audit_codex_shell_command(ok), f'should pass codex audit: {ok}')

        for bad in ('sh gen.sh', 'sh -c "python3 gen.py"', 'sh'):
            self.assertIsNotNone(backends.audit_agy_shell_command(bad), f'should fail agy audit: {bad}')
            self.assertIsNotNone(backends.audit_codex_shell_command(bad), f'should fail codex audit: {bad}')

    def test_codex_transcript_audit_allows_readonly_and_preview_cli_and_rejects_writes_or_interpreters(self):
        allowed = [
            'android-design-preview check design/proposal.explore.kt --direction "Editorial split" --view light',
            '/bin/zsh -lc "cat skills/android-design/SKILL.md"',
            "/bin/zsh -lc \"sed -n '1,80p' skills/android-design/SKILL.md\"",
            'rg -n "TopAppBar|NavigationRail" skills/android-design/references/components.md',
            '/bin/bash -lc "cat skills/android-design/SKILL.md | nl -ba | head -n 40"',
            'ls -la design/assets',
            'wc -l design/proposal.explore.kt',
            'grep -n "seed" DESIGN.md | tail -n 5',
        ]
        for ok in allowed:
            self.assertIsNone(backends.audit_codex_shell_command(ok), f'should pass codex audit: {ok}')

        disallowed = [
            "sed -i 's/foo/bar/' design/proposal.explore.kt",
            "sed -i'' 's/foo/bar/' design/proposal.explore.kt",
            'sed --in-place s/a/b/ design/proposal.explore.kt',
            'cat a.kt > design/proposal.explore.kt',
            'cat a.kt >> design/proposal.explore.kt',
            'cat a.kt | tee design/proposal.explore.kt',
            'python3 -c "open(\'x\',\'w\').write(\'hi\')"',
            'node script.js',
            'cat $(echo skills/android-design/SKILL.md)',
            'cat `echo skills/android-design/SKILL.md`',
            '(cat skills/android-design/SKILL.md)',
            'android-design-preview status && python3 gen.py',
            'cat DESIGN.md ; rm -f DESIGN.md',
        ]
        for bad in disallowed:
            self.assertIsNotNone(backends.audit_codex_shell_command(bad), f'should fail codex audit: {bad}')

        # Verify execute() raises RuntimeError when a codex transcript runs a disallowed shell command.
        bad_events = [
            {'type': 'item.completed', 'item': {
                'id': 'c1', 'type': 'command_execution',
                'command': "/bin/zsh -lc \"sed -i 's/a/b/' design/proposal.explore.kt\"",
                'aggregated_output': '', 'exit_code': 0, 'status': 'completed',
            }},
            {'type': 'item.completed', 'item': {'id': 'm1', 'type': 'agent_message', 'text': 'Done'}},
        ]
        stdout = '\n'.join(json.dumps(e) for e in bad_events) + '\n'

        def fake_run(cmd, **kwargs):
            Path(cmd[cmd.index('--output-last-message') + 1]).write_text('Done')
            return subprocess.CompletedProcess(cmd, 0, stdout, '')

        with patch('backends.subprocess.run', side_effect=fake_run):
            with self.assertRaisesRegex(RuntimeError, 'codex script-ban audit failed'):
                backends.execute('codex', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)

    def test_agy_judge_uses_read_only_settings_structured_output_and_rejects_errors(self):
        schema = {'type': 'object', 'properties': {'items': {'type': 'array'}}}
        good_out = '\n'.join([
            json.dumps({'event': 'init', 'init': {'model': 'gemini-3.8-flash-high'}}),
            json.dumps({'event': 'result', 'result': {
                'status': 'SUCCESS',
                'structured_output': {'items': [{'id': 'R-one', 'pass': True, 'reason': 'Clear'}]},
                'num_turns': 1,
                'usage': {'input_tokens': 3000, 'output_tokens': 120, 'cache_read_tokens': 500},
            }}),
        ]) + '\n'

        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, good_out, '')) as call:
            result = backends.execute('agy', 'judge', Path('/tmp'), role='judge',
                                      images=['/tmp/renders/a.png'], schema=schema)
        cmd = call.call_args.args[0]
        self.assertEqual(cmd[cmd.index('--add-dir') + 1], str(Path('/tmp/renders').resolve()))
        self.assertEqual(json.loads(cmd[cmd.index('--json-schema') + 1]), schema)
        settings = json.loads(cmd[cmd.index('--settings') + 1])
        self.assertEqual(settings['permissions']['allow'], ['read_file(*)'])
        self.assertIn('mcp(*)', settings['permissions']['deny'])
        self.assertIn('write_file(*)', settings['permissions']['deny'])
        self.assertEqual(json.loads(result['message']), {'items': [{'id': 'R-one', 'pass': True, 'reason': 'Clear'}]})
        self.assertEqual(result['model'], 'gemini-3.8-flash-high')
        self.assertIsNone(result['check_calls'])

        denied_out = json.dumps({'event': 'result', 'result': {
            'status': 'SUCCESS', 'response': '', 'denied_actions': [{'action': 'read_file'}],
        }})
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, denied_out, '')):
            with self.assertRaisesRegex(RuntimeError, 'permission denied'):
                backends.execute('agy', 'judge', Path('/tmp'), role='judge')

        error_out = json.dumps({'event': 'result', 'result': {'status': 'ERROR', 'error': 'quota exceeded'}})
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, error_out, '')):
            with self.assertRaisesRegex(RuntimeError, 'quota exceeded'):
                backends.execute('agy', 'judge', Path('/tmp'), role='judge')

    def test_claude_generator_streams_ndjson_and_tracks_cli_and_legacy_checks(self):
        events = [
            {'type': 'assistant', 'message': {'content': [
                {'type': 'tool_use', 'id': 'u1', 'name': 'Bash', 'input': {'command': 'android-design-preview check design/proposal.explore.kt'}},
                {'type': 'tool_use', 'id': 'u2', 'name': 'Bash', 'input': {'command': 'android-design-preview check design/proposal.explore.kt --direction Main --view light'}},
            ]}},
            {'type': 'user', 'message': {'content': [
                {'type': 'tool_result', 'tool_use_id': 'u1', 'is_error': True, 'content': json.dumps({'ok': False, 'valid': False, 'errors': [{'message': 'compile error'}]})},
                {'type': 'tool_result', 'tool_use_id': 'u2', 'is_error': True, 'content': json.dumps({'ok': False, 'valid': True, 'pass': False})},
            ]}},
            {'type': 'assistant', 'message': {'content': [
                {'type': 'tool_use', 'id': 'u3', 'name': 'Bash', 'input': {'command': 'android-design-preview check design/proposal.explore.kt'}},
                {'type': 'tool_use', 'id': 'u4', 'name': 'mcp__android_design_preview__check_design', 'input': {}},
            ]}},
            {'type': 'user', 'message': {'content': [
                {'type': 'tool_result', 'tool_use_id': 'u3', 'content': json.dumps({'ok': True, 'valid': True, 'pass': True})},
                {'type': 'tool_result', 'tool_use_id': 'u4', 'content': json.dumps({'compiled': True, 'pass': True})},
            ]}},
            {
                'type': 'result',
                'result': 'Proposal checked.',
                'total_cost_usd': 0.55,
                'num_turns': 4,
                'usage': {'input_tokens': 2400, 'output_tokens': 610, 'cache_read_input_tokens': 1000},
                'modelUsage': {'claude-sonnet-4-5': {'inputTokens': 2400}},
            },
        ]
        ndjson = '\n'.join(json.dumps(e) for e in events) + '\n'
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, ndjson, '')) as call:
            result = backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)

        cmd = call.call_args.args[0]
        self.assertEqual(cmd[cmd.index('--output-format') + 1], 'stream-json')
        self.assertIn('--verbose', cmd)
        self.assertEqual(result['check_calls'], 4)
        self.assertEqual(result['compile_retries'], 1)
        self.assertEqual(result['model'], 'claude-sonnet-4-5')

        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, json.dumps(events), '')):
            array_result = backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)
        self.assertEqual(array_result['check_calls'], 4)
        self.assertEqual(array_result['compile_retries'], 1)
        self.assertEqual(array_result['message'], 'Proposal checked.')

        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, '{"result":"ok"}', '')) as judge_call:
            backends.execute('claude', 'judge', Path('/tmp'), role='judge', images=['/tmp/a.png'])
        judge_cmd = judge_call.call_args.args[0]
        self.assertEqual(judge_cmd[judge_cmd.index('--output-format') + 1], 'json')
        self.assertNotIn('--verbose', judge_cmd)

    def test_claude_tracks_skill_bytes_read_unique_files_and_resume(self):
        with tempfile.TemporaryDirectory() as case_tmp:
            case_dir = Path(case_tmp).resolve()
            skill_a = case_dir / 'skills/android-design/SKILL.md'
            skill_b = case_dir / 'skills/android-design-explore/references/compose-signatures.md'
            other = case_dir / 'DESIGN.md'
            outside = Path('/Users/example/.claude/skills/android-design/SKILL.md')
            part1 = 'alpha ' * 10
            part2 = 'beta ' * 6
            sigs = 'fun TopAppBar()\n'
            events = [
                {'type': 'assistant', 'message': {'content': [
                    {'type': 'tool_use', 'id': 'r1', 'name': 'Read', 'input': {'file_path': str(skill_a), 'offset': 1, 'limit': 50}},
                    {'type': 'tool_use', 'id': 'r2', 'name': 'Read', 'input': {'file_path': 'skills/android-design/SKILL.md', 'offset': 51, 'limit': 50}},
                    {'type': 'tool_use', 'id': 'r3', 'name': 'Read', 'input': {'file_path': str(skill_b)}},
                    {'type': 'tool_use', 'id': 'r4', 'name': 'Read', 'input': {'file_path': str(other)}},
                    {'type': 'tool_use', 'id': 'r5', 'name': 'Read', 'input': {'file_path': str(outside)}},
                    {'type': 'tool_use', 'id': 'r6', 'name': 'Read', 'input': {'file_path': str(skill_a)}},
                ]}},
                {'type': 'user', 'message': {'content': [
                    {'type': 'tool_result', 'tool_use_id': 'r1', 'content': part1},
                    {'type': 'tool_result', 'tool_use_id': 'r2', 'content': [{'type': 'text', 'text': part2}]},
                    {'type': 'tool_result', 'tool_use_id': 'r3', 'content': sigs},
                    {'type': 'tool_result', 'tool_use_id': 'r4', 'content': 'design doc content'},
                    {'type': 'tool_result', 'tool_use_id': 'r5', 'content': 'global skill content'},
                    {'type': 'tool_result', 'tool_use_id': 'r6', 'is_error': True, 'content': 'read failed'},
                ]}},
                {
                    'type': 'result',
                    'session_id': 'sess-abc-123',
                    'result': 'Updated.',
                    'num_turns': 2,
                    'usage': {'input_tokens': 500, 'output_tokens': 120},
                },
            ]
            ndjson = '\n'.join(json.dumps(e) for e in events) + '\n'
            with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, ndjson, '')) as call:
                result = backends.execute('claude', 'revise', case_dir, role='generator',
                                          mcp_binary=BINARY, resume_session='sess-prev')
            cmd = call.call_args.args[0]
            self.assertEqual(cmd[cmd.index('--resume') + 1], 'sess-prev')
            expected_a = len(part1.encode('utf-8')) + len(part2.encode('utf-8'))
            expected_b = len(sigs.encode('utf-8'))
            self.assertEqual(result['session_id'], 'sess-abc-123')
            self.assertEqual(result['skill_bytes_read'], expected_a + expected_b)
            self.assertEqual(result['skill_read_calls'], 3)
            self.assertEqual(result['skill_unique_files'], 2)
            self.assertEqual(result['skill_files_read'], {
                'android-design/SKILL.md': {'reads': 2, 'bytes': expected_a},
                'android-design-explore/references/compose-signatures.md': {'reads': 1, 'bytes': expected_b},
            })

    def test_codex_tracks_skill_bytes_read_from_shell_commands(self):
        with tempfile.TemporaryDirectory() as case_tmp:
            case_dir = Path(case_tmp).resolve()
            out1 = 'line 1\nline 2\n'
            out2 = 'multi file output!!'
            out3 = 'piped slice'
            events = [
                {'type': 'item.completed', 'item': {
                    'id': 'c1', 'type': 'command_execution',
                    'command': "/bin/zsh -lc \"sed -n '1,40p' skills/android-design/SKILL.md\"",
                    'aggregated_output': out1, 'exit_code': 0, 'status': 'completed',
                }},
                {'type': 'item.completed', 'item': {
                    'id': 'c2', 'type': 'command_execution',
                    'command': 'cat skills/android-design/SKILL.md skills/android-design-explore/SKILL.md',
                    'aggregated_output': out2, 'exit_code': 0, 'status': 'completed',
                }},
                {'type': 'item.completed', 'item': {
                    'id': 'c3', 'type': 'command_execution',
                    'command': f'/bin/bash -lc "head -n 20 {case_dir}/skills/android-design/references/layout.md | nl -ba"',
                    'aggregated_output': out3, 'exit_code': 0, 'status': 'completed',
                }},
                {'type': 'item.completed', 'item': {
                    'id': 'c4', 'type': 'command_execution',
                    'command': 'ls skills/android-design',
                    'aggregated_output': 'SKILL.md\nreferences\n', 'exit_code': 0, 'status': 'completed',
                }},
                {'type': 'item.completed', 'item': {
                    'id': 'c5', 'type': 'command_execution',
                    'command': 'cat DESIGN.md',
                    'aggregated_output': 'design md', 'exit_code': 0, 'status': 'completed',
                }},
                {'type': 'item.completed', 'item': {
                    'id': 'c6', 'type': 'command_execution',
                    'command': 'cat skills/android-design/missing.md',
                    'aggregated_output': 'No such file', 'exit_code': 1, 'status': 'failed',
                }},
                {'type': 'item.completed', 'item': {'id': 'm1', 'type': 'agent_message', 'text': 'Done'}},
                {'type': 'turn.completed', 'usage': {'input_tokens': 1000, 'output_tokens': 200}},
            ]
            stdout = '\n'.join(json.dumps(e) for e in events) + '\n'

            def fake_run(cmd, **kwargs):
                Path(cmd[cmd.index('--output-last-message') + 1]).write_text('Done')
                return subprocess.CompletedProcess(cmd, 0, stdout, '')

            with patch('backends.subprocess.run', side_effect=fake_run):
                result = backends.execute('codex', 'brief', case_dir, role='generator', mcp_binary=BINARY)

            total_expected = len(out1.encode()) + len(out2.encode()) + len(out3.encode())
            self.assertEqual(result['skill_bytes_read'], total_expected)
            self.assertEqual(result['skill_read_calls'], 3)
            self.assertEqual(result['skill_unique_files'], 3)
            self.assertEqual(
                sum(v['bytes'] for v in result['skill_files_read'].values()),
                result['skill_bytes_read'],
            )
            self.assertEqual(result['skill_files_read']['android-design/SKILL.md']['reads'], 2)
            self.assertEqual(result['skill_files_read']['android-design-explore/SKILL.md']['reads'], 1)
            self.assertEqual(result['skill_files_read']['android-design/references/layout.md']['bytes'], len(out3.encode()))

    def test_agy_tracks_skill_bytes_read_from_read_file_steps(self):
        with tempfile.TemporaryDirectory() as case_tmp:
            case_dir = Path(case_tmp).resolve()
            content_a = 'M3 rules body with utf8 \u00e9'
            content_b = 'Explore steps'
            events = [
                {'event': 'init', 'init': {'cwd': str(case_dir), 'model': 'gemini-3.8-flash-high'}},
                {'event': 'step_update', 'step_update': {
                    'step_index': 1, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'read_file',
                    'tool_info': {
                        'name': 'read_file',
                        'parameters': {'Path': str(case_dir / 'skills/android-design/SKILL.md')},
                        'output': content_a,
                    },
                }},
                {'event': 'step_update', 'step_update': {
                    'step_index': 2, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'read_file',
                    'tool_info': {
                        'name': 'read_file',
                        'parameters': {'Path': 'skills/android-design-explore/SKILL.md'},
                        'output': content_b,
                    },
                }},
                {'event': 'step_update', 'step_update': {
                    'step_index': 3, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'read_file',
                    'tool_info': {
                        'name': 'read_file',
                        'parameters': {'Path': str(case_dir / 'design/proposal.explore.kt')},
                        'output': 'val preview = designBoard {}',
                    },
                }},
                {'event': 'step_update', 'step_update': {
                    'step_index': 4, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'read_file',
                    'tool_info': {
                        'name': 'read_file',
                        'parameters': {'Path': str(case_dir / 'skills/android-design/references/layout.md')},
                        'output': 'permission denied',
                        'error': 'denied',
                    },
                }},
                {'event': 'result', 'result': {
                    'status': 'SUCCESS', 'response': 'Ready.', 'num_turns': 1,
                    'usage': {'input_tokens': 5000, 'output_tokens': 300},
                }},
            ]
            stdout = '\n'.join(json.dumps(e) for e in events) + '\n'
            with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, stdout, '')):
                result = backends.execute('agy', 'brief', case_dir, role='generator', mcp_binary=BINARY)

            bytes_a = len(content_a.encode('utf-8'))
            bytes_b = len(content_b.encode('utf-8'))
            self.assertEqual(result['skill_bytes_read'], bytes_a + bytes_b)
            self.assertEqual(result['skill_read_calls'], 2)
            self.assertEqual(result['skill_unique_files'], 2)
            self.assertEqual(result['skill_files_read'], {
                'android-design/SKILL.md': {'reads': 1, 'bytes': bytes_a},
                'android-design-explore/SKILL.md': {'reads': 1, 'bytes': bytes_b},
            })

    def test_resume_session_rejected_for_non_claude_backends(self):
        for backend in ('agy', 'codex'):
            with patch('backends.subprocess.run') as call, self.assertRaisesRegex(ValueError, 'resume_session'):
                backends.execute(backend, 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY, resume_session='s1')
            call.assert_not_called()

    def test_claude_generator_grants_read_access_to_preview_cache_dir(self):
        events = [{'type': 'result', 'result': 'Done.', 'num_turns': 1, 'usage': {}}]
        ndjson = '\n'.join(json.dumps(e) for e in events) + '\n'
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, ndjson, '')) as call:
            backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)
        cmd = call.call_args.args[0]
        cache_dir = str((Path.home() / '.cache' / 'android-design-preview').resolve())
        self.assertIn('--add-dir', cmd)
        self.assertEqual(cmd[cmd.index('--add-dir') + 1], cache_dir)
        self.assertIn('Read', cmd)

    def test_claude_truncated_json_and_permission_refusals_do_not_inflate_compile_retries(self):
        truncated_valid = (
            'Exit code 1\n'
            '{"ok":false,"valid":true,"pass":false,"scoped":false,"findings":[{"detail":"1.05:1"'
            '\n\n... [4500 characters truncated] ...\n\n'
            ',"images":[]}'
        )
        truncated_invalid = (
            'Exit code 1\n'
            '{"ok":false,"valid":false,"pass":false,"errors":[{"message":"unresolved ref"'
            '\n\n... [4500 characters truncated] ...\n\n'
            '}]}'
        )
        permission_refusal = (
            'This Bash command contains multiple operations. '
            'The following part requires approval: echo ---'
        )
        events = [
            {'type': 'assistant', 'message': {'content': [
                {'type': 'tool_use', 'id': 'u1', 'name': 'Bash', 'input': {'command': 'android-design-preview check design/proposal.explore.kt'}},
                {'type': 'tool_use', 'id': 'u2', 'name': 'Bash', 'input': {'command': 'android-design-preview check design/proposal.explore.kt'}},
                {'type': 'tool_use', 'id': 'u3', 'name': 'Bash', 'input': {'command': 'android-design-preview check design/proposal.explore.kt && echo --- && android-design-preview check design/proposal.explore.kt'}},
            ]}},
            {'type': 'user', 'message': {'content': [
                {'type': 'tool_result', 'tool_use_id': 'u1', 'is_error': True, 'content': truncated_valid},
                {'type': 'tool_result', 'tool_use_id': 'u2', 'is_error': True, 'content': truncated_invalid},
                {'type': 'tool_result', 'tool_use_id': 'u3', 'is_error': True, 'content': permission_refusal},
            ]}},
            {'type': 'result', 'result': 'Checked.', 'num_turns': 2, 'usage': {}},
        ]
        ndjson = '\n'.join(json.dumps(e) for e in events) + '\n'
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, ndjson, '')):
            result = backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)
        self.assertEqual(result['check_calls'], 2)
        self.assertEqual(result['compile_retries'], 1)

    def test_agy_tracks_skill_bytes_read_from_summary_output(self):
        with tempfile.TemporaryDirectory() as case_tmp:
            case_dir = Path(case_tmp).resolve()
            events = [
                {'event': 'step_update', 'step_update': {
                    'step_index': 1, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'read_file',
                    'tool_info': {
                        'name': 'read_file',
                        'parameters': {'Path': str(case_dir / 'skills/android-design/SKILL.md')},
                        'output': '196 lines, 24584 bytes',
                    },
                }},
                {'event': 'step_update', 'step_update': {
                    'step_index': 2, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'read_file',
                    'tool_info': {
                        'name': 'read_file',
                        'parameters': {'Path': str(case_dir / 'skills/android-design-explore/SKILL.md')},
                        'output': '69 lines, 5120 bytes',
                    },
                }},
                {'event': 'result', 'result': {
                    'status': 'SUCCESS', 'response': 'Ready.', 'num_turns': 1,
                    'usage': {'input_tokens': 5000, 'output_tokens': 300},
                }},
            ]
            stdout = '\n'.join(json.dumps(e) for e in events) + '\n'
            with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, stdout, '')):
                result = backends.execute('agy', 'brief', case_dir, role='generator', mcp_binary=BINARY)
            self.assertEqual(result['skill_bytes_read'], 24584 + 5120)
            self.assertEqual(result['skill_files_read'], {
                'android-design/SKILL.md': {'reads': 1, 'bytes': 24584},
                'android-design-explore/SKILL.md': {'reads': 1, 'bytes': 5120},
            })
