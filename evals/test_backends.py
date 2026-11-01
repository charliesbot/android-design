import json
from pathlib import Path
import subprocess
import unittest
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
        self.assertEqual(seen['config'], {'mcpServers': {'android_design_preview': {'command': BINARY, 'args': []}}})
        self.assertIn('--strict-mcp-config', seen['cmd'])
        self.assertEqual(seen['cmd'][seen['cmd'].index('--allowedTools') + 1], 'mcp__android_design_preview__*')

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
        self.assertEqual(overrides, [
            'mcp_servers.android_design_preview.command="/Apps/AndroidDesignPreview"',
            'mcp_servers.android_design_preview.args=[]',
            'mcp_servers.android_design_preview.default_tools_approval_mode="approve"',
        ])
        self.assertEqual(cmd[-1], '-')

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
