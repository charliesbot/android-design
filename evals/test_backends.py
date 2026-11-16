import json
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import backends

BINARY = '/Apps/AndroidDesignPreview'


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

    def test_claude_generator_reaches_the_app_over_a_strict_mcp_config(self):
        seen = {}

        def execute(cmd, **kwargs):
            seen['cmd'] = cmd
            config = Path(cmd[cmd.index('--mcp-config') + 1])
            seen['config'] = json.loads(config.read_text())
            return subprocess.CompletedProcess(cmd, 0, json.dumps({'result': 'ok'}), '')

        with patch('backends.subprocess.run', side_effect=execute):
            backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)
        server = seen['config']['mcpServers']['android_design_preview']
        self.assertEqual(set(server), {'command', 'args', 'env'})
        self.assertEqual((server['command'], server['args']), (BINARY, []))
        self.assertEqual(list(server['env']), ['ANDROID_DESIGN_PREVIEW_SOCKET'])
        self.assertTrue(server['env']['ANDROID_DESIGN_PREVIEW_SOCKET'].startswith('/tmp/adp-'))
        self.assertIn('--strict-mcp-config', seen['cmd'])
        self.assertEqual(seen['cmd'][seen['cmd'].index('--allowedTools') + 1], 'mcp__android_design_preview__*')

    def test_claude_generator_may_write_only_inside_its_case_folder(self):
        # Like Codex's workspace-write: `claude -p` refuses file writes it was not allowed.
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, '{"result": "ok"}', '')) as call:
            backends.execute('claude', 'brief', Path('/tmp/case'), role='generator', mcp_binary=BINARY)
        cmd = call.call_args.args[0]
        allowed = cmd[cmd.index('--allowedTools') + 1:cmd.index('--allowedTools') + 3]
        self.assertEqual(allowed[1], f'Edit(/{Path("/tmp/case").resolve()}/**)')
        self.assertNotIn('--dangerously-skip-permissions', cmd)
        self.assertNotIn('--permission-mode', cmd)

    def test_claude_judge_gets_no_mcp_server(self):
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, '{"result": "ok"}', '')) as call:
            backends.execute('claude', 'judge', Path('/tmp'), role='judge', images=['/tmp/a.png'])
        self.assertNotIn('--mcp-config', call.call_args.args[0])

    def test_codex_generator_registers_the_app_as_an_approved_mcp_server(self):
        def execute(cmd, **kwargs):
            Path(cmd[cmd.index('--output-last-message') + 1]).write_text('done')
            return subprocess.CompletedProcess(cmd, 0, '', '')

        with patch('backends.subprocess.run', side_effect=execute) as call:
            backends.execute('codex', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)
        cmd = call.call_args.args[0]
        overrides = [cmd[i + 1] for i, part in enumerate(cmd) if part == '-c']
        socket = overrides[3].split('=', 1)[1].strip('"')
        self.assertEqual(overrides, [
            'mcp_servers.android_design_preview.command="/Apps/AndroidDesignPreview"',
            'mcp_servers.android_design_preview.args=[]',
            'mcp_servers.android_design_preview.default_tools_approval_mode="approve"',
            f'mcp_servers.android_design_preview.env.ANDROID_DESIGN_PREVIEW_SOCKET="{socket}"',
        ])
        self.assertTrue(socket.startswith('/tmp/adp-'))
        self.assertEqual(cmd[-1], '-')

    def test_each_generator_run_gets_its_own_short_socket_and_cleans_it_up(self):
        paths = []

        def execute(cmd, **kwargs):
            config = json.loads(Path(cmd[cmd.index('--mcp-config') + 1]).read_text())
            path = Path(config['mcpServers']['android_design_preview']['env']['ANDROID_DESIGN_PREVIEW_SOCKET'])
            self.assertTrue(path.parent.is_dir())
            paths.append(path)
            return subprocess.CompletedProcess(cmd, 0, json.dumps({'result': 'ok'}), '')

        with patch('backends.subprocess.run', side_effect=execute):
            for _ in range(2):
                backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)
        self.assertNotEqual(paths[0], paths[1])
        for path in paths:
            self.assertLess(len(str(path)), 100)
            self.assertFalse(path.parent.exists())

    def test_codex_judge_gets_no_mcp_server(self):
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

    def test_codex_jsonl_extracts_usage_turns_and_compile_retries_vs_layout_findings(self):
        events = [
            {'type': 'thread.started', 'thread_id': 't-1'},
            {'type': 'item.completed', 'item': {'id': 'i0', 'type': 'agent_message', 'text': 'Opening'}},
            {'type': 'item.completed', 'item': {
                'id': 'i1', 'type': 'mcp_tool_call', 'server': 'android_design_preview',
                'tool': 'open_proposal', 'result': {'content': [{'type': 'text', 'text': json.dumps({'valid': False})}]},
            }},
            {'type': 'item.completed', 'item': {
                'id': 'i2', 'type': 'mcp_tool_call', 'server': 'android_design_preview',
                'tool': 'check_proposal', 'result': {'content': [{'type': 'text', 'text': json.dumps({'pass': False, 'screens': {}})}]},
            }},
            {'type': 'item.completed', 'item': {
                'id': 'i3', 'type': 'mcp_tool_call', 'server': 'android_design_preview',
                'tool': 'check_design', 'error': {'message': 'compile error'}, 'result': {'content': [{'type': 'text', 'text': 'Unresolved reference'}], 'isError': True},
            }},
            {'type': 'item.completed', 'item': {
                'id': 'i4', 'type': 'mcp_tool_call', 'server': 'android_design_preview',
                'tool': 'check_design', 'result': {'content': [{'type': 'text', 'text': json.dumps({'compiled': True, 'pass': True})}]},
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
        self.assertEqual(result['compile_retries'], 2)

    def test_agy_generator_mounts_isolated_plugin_and_cleans_up(self):
        events = [
            {'event': 'init', 'init': {'cwd': '/tmp', 'tools': []}},
            {'event': 'step_update', 'step_update': {'step_index': 1, 'state': 'DONE', 'step_type': 'agent_response'}},
            {'event': 'step_update', 'step_update': {
                'step_index': 2, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'call_mcp_tool',
                'tool_info': {
                    'name': 'call_mcp_tool',
                    'parameters': {'ServerName': 'eval-preview_preview', 'ToolName': 'check_design'},
                    'output': json.dumps({'compiled': False, 'errors': ['missing import']}),
                },
            }},
            {'event': 'step_update', 'step_update': {'step_index': 3, 'state': 'DONE', 'step_type': 'agent_response'}},
            {'event': 'step_update', 'step_update': {
                'step_index': 4, 'state': 'DONE', 'step_type': 'tool', 'tool_name': 'call_mcp_tool',
                'tool_info': {
                    'name': 'call_mcp_tool',
                    'parameters': {'ServerName': 'eval-preview_preview', 'ToolName': 'check_design'},
                    'output': json.dumps({'compiled': True, 'pass': False}),
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
                seen['stdin'] = kwargs.get('stdin')
                pdir = case_dir / '.agents/plugins/eval-preview'
                seen['plugin'] = json.loads((pdir / 'plugin.json').read_text())
                seen['mcp'] = json.loads((pdir / 'mcp_config.json').read_text())
                return subprocess.CompletedProcess(cmd, 0, stdout, '')

            with patch('backends.Path.home', return_value=Path(home_tmp)), \
                    patch('backends.subprocess.run', side_effect=fake_run):
                result = backends.execute('agy', 'Use android_design_preview to inspect.', case_dir,
                                          role='generator', mcp_binary=BINARY)

            self.assertFalse((case_dir / '.agents').exists())

        self.assertEqual(seen['stdin'], subprocess.DEVNULL)
        self.assertEqual(seen['plugin'], {'name': 'eval-preview'})
        preview = seen['mcp']['mcpServers']['preview']
        self.assertEqual(preview['command'], BINARY)
        self.assertTrue(preview['env']['ANDROID_DESIGN_PREVIEW_SOCKET'].startswith('/tmp/adp-'))
        cmd = seen['cmd']
        prompt_arg = cmd[cmd.index('-p') + 1]
        self.assertIn('eval-preview_preview', prompt_arg)
        settings = json.loads(cmd[cmd.index('--settings') + 1])
        self.assertIn(f'write_file({case_dir})', settings['permissions']['allow'])
        self.assertIn('mcp(eval-preview_preview/*)', settings['permissions']['allow'])
        self.assertIn('command(*)', settings['permissions']['deny'])
        self.assertIn('mcp(android_design_preview/*)', settings['permissions']['deny'])
        self.assertNotIn('--dangerously-skip-permissions', cmd)
        self.assertEqual(result['model'], 'Gemini 3.8 Flash (High)')
        self.assertEqual(result['turns'], 3)
        self.assertEqual(result['input_tokens'], 21000)
        self.assertEqual(result['output_tokens'], 950)
        self.assertEqual(result['thinking_tokens'], 400)
        self.assertEqual(result['cache_read_tokens'], 14000)
        self.assertEqual(result['check_calls'], 2)
        self.assertEqual(result['compile_retries'], 1)

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

    def test_claude_generator_streams_ndjson_and_tracks_check_calls_and_compile_retries(self):
        events = [
            {'type': 'assistant', 'message': {'content': [
                {'type': 'tool_use', 'id': 'u1', 'name': 'mcp__android_design_preview__open_proposal', 'input': {}},
                {'type': 'tool_use', 'id': 'u2', 'name': 'mcp__android_design_preview__check_proposal', 'input': {}},
            ]}},
            {'type': 'user', 'message': {'content': [
                {'type': 'tool_result', 'tool_use_id': 'u1', 'content': json.dumps({'valid': False})},
                {'type': 'tool_result', 'tool_use_id': 'u2', 'content': [{'type': 'text', 'text': json.dumps({'pass': False})}]},
            ]}},
            {'type': 'assistant', 'message': {'content': [
                {'type': 'tool_use', 'id': 'u3', 'name': 'mcp__android_design_preview__check_design', 'input': {}},
                {'type': 'tool_use', 'id': 'u4', 'name': 'mcp__android_design_preview__check_design', 'input': {}},
            ]}},
            {'type': 'user', 'message': {'content': [
                {'type': 'tool_result', 'tool_use_id': 'u3', 'is_error': True, 'content': 'compile error'},
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
        self.assertEqual(result['check_calls'], 3)
        self.assertEqual(result['compile_retries'], 2)
        self.assertEqual(result['model'], 'claude-sonnet-4-5')
        self.assertEqual(result['cost'], 0.55)
        self.assertEqual(result['turns'], 4)
        self.assertEqual(result['input_tokens'], 2400)
        self.assertEqual(result['output_tokens'], 610)
        self.assertEqual(result['cache_read_tokens'], 1000)

        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, json.dumps(events), '')):
            array_result = backends.execute('claude', 'brief', Path('/tmp'), role='generator', mcp_binary=BINARY)
        self.assertEqual(array_result['check_calls'], 3)
        self.assertEqual(array_result['compile_retries'], 2)
        self.assertEqual(array_result['message'], 'Proposal checked.')

        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, '{"result":"ok"}', '')) as judge_call:
            backends.execute('claude', 'judge', Path('/tmp'), role='judge', images=['/tmp/a.png'])
        judge_cmd = judge_call.call_args.args[0]
        self.assertEqual(judge_cmd[judge_cmd.index('--output-format') + 1], 'json')
        self.assertNotIn('--verbose', judge_cmd)
