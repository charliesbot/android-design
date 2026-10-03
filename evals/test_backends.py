import json
from pathlib import Path
import subprocess
import unittest
from unittest.mock import patch

import backends


class BackendTests(unittest.TestCase):
    def test_claude_normalizes_reply_without_permission_bypass(self):
        reply = {'result': 'finished', 'total_cost_usd': 0.2, 'num_turns': 3}
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, json.dumps(reply), '')) as call:
            result = backends.execute('claude', 'brief', Path('/tmp'), role='generator')
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
                backends.execute('claude', 'brief', Path('/tmp'), role='generator')

    def test_claude_error_envelope_is_rejected(self):
        with patch('backends.subprocess.run', return_value=subprocess.CompletedProcess([], 0, '{"is_error":true,"result":"denied"}', '')):
            with self.assertRaisesRegex(RuntimeError, 'denied'):
                backends.execute('claude', 'brief', Path('/tmp'), role='generator')

    def test_unknown_backend_is_rejected_before_launch(self):
        with patch('backends.subprocess.run') as call:
            with self.assertRaises(ValueError):
                backends.execute('unknown', 'brief', Path('/tmp'), role='generator')
        call.assert_not_called()
