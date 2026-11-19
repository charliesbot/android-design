import json
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest import mock

import multiturn


class MultiTurnTests(unittest.TestCase):
    def test_revisions_cover_ten_distinct_steps(self):
        self.assertGreaterEqual(len(multiturn.REVISIONS), 10)
        for title, body in multiturn.REVISIONS:
            self.assertTrue(title.strip())
            self.assertTrue(body.strip())
            self.assertNotIn('\u2014', title)
            self.assertNotIn('\u2014', body)

    def test_prepare_multiturn_sets_up_fixture_skills_and_assets(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / 'mt'
            proposal = multiturn.prepare_multiturn(out)
            self.assertTrue(proposal.is_file())
            self.assertTrue((out / 'DESIGN.md').is_file())
            self.assertTrue((out / 'skills/android-design/SKILL.md').is_file())
            self.assertTrue((out / 'skills/android-design-explore/SKILL.md').is_file())
            for asset in multiturn.FIXTURE_ASSETS:
                self.assertTrue((out / 'design/assets' / asset).is_file())
            log = subprocess.run(['git', 'log', '--oneline'], cwd=out, capture_output=True, text=True)
            self.assertEqual(log.returncode, 0)
            self.assertEqual(len(log.stdout.strip().splitlines()), 1)

    def test_aggregate_multiturn_computes_totals_and_per_turn_stats(self):
        turns = [
            multiturn._turn_record(1, 'Layout shift', {
                'model': 'claude-sonnet-4-6',
                'wall_seconds': 30.0,
                'turns': 5,
                'input_tokens': 200,
                'cache_read_tokens': 10000,
                'cache_creation_tokens': 5000,
                'output_tokens': 800,
                'cost': 0.12,
                'check_calls': 2,
                'compile_retries': 1,
                'skill_bytes_read': 50000,
                'skill_read_calls': 3,
                'skill_unique_files': 2,
                'skill_files_read': {
                    'android-design/SKILL.md': {'reads': 2, 'bytes': 40000},
                    'android-design-explore/SKILL.md': {'reads': 1, 'bytes': 10000},
                },
            }),
            multiturn._turn_record(2, 'Typography emphasis', {
                'model': 'claude-sonnet-4-6',
                'wall_seconds': 20.0,
                'turns': 3,
                'input_tokens': 100,
                'cache_read_tokens': 18000,
                'cache_creation_tokens': 2000,
                'output_tokens': 400,
                'cost': 0.08,
                'check_calls': 1,
                'compile_retries': 0,
                'skill_bytes_read': 5000,
                'skill_read_calls': 1,
                'skill_unique_files': 1,
                'skill_files_read': {
                    'android-design/SKILL.md': {'reads': 1, 'bytes': 5000},
                },
            }),
        ]
        report = multiturn.aggregate_multiturn(
            turns,
            commit_sha='abc1234',
            pipeline='v4',
            final_checks={'C-valid': {'pass': True}, 'C-review': {'pass': True}},
        )
        self.assertEqual(report['turns_count'], 2)
        self.assertEqual(report['model'], 'claude-sonnet-4-6')
        self.assertEqual(report['commit_sha'], 'abc1234')
        self.assertTrue(report['final_checks_pass'])
        self.assertEqual(report['totals']['wall_seconds'], 50.0)
        self.assertEqual(report['totals']['total_input_tokens'], 35300)
        self.assertEqual(report['totals']['output_tokens'], 1200)
        self.assertEqual(report['totals']['total_tokens'], 36500)
        self.assertEqual(report['totals']['check_calls'], 3)
        self.assertEqual(report['totals']['compile_retries'], 1)
        self.assertEqual(report['totals']['skill_bytes_read'], 55000)
        self.assertEqual(report['totals']['skill_read_calls'], 4)
        self.assertEqual(report['totals']['skill_unique_files'], 2)
        self.assertEqual(report['totals']['skill_files_read']['android-design/SKILL.md'], {'reads': 3, 'bytes': 45000})
        self.assertEqual(report['per_turn']['wall_seconds'], {'mean': 25.0, 'median': 25.0})
        self.assertEqual(report['per_turn']['total_tokens'], {'mean': 18250.0, 'median': 18250.0})
        self.assertEqual(report['per_turn']['output_tokens'], {'mean': 600.0, 'median': 600.0})
        summary = multiturn.format_summary(report)
        self.assertIn('Skill bytes read: `55000`', summary)
        self.assertNotIn('\u2014', summary)

    def test_run_multiturn_resumes_single_claude_session_across_turns(self):
        calls = []

        def fake_execute(backend, prompt, cwd, **kwargs):
            calls.append((backend, kwargs.get('resume_session'), prompt))
            turn_idx = len(calls)
            return {
                'backend': 'claude',
                'model': 'claude-sonnet-4-6',
                'session_id': 'sess-fixed-1',
                'wall_seconds': 10.0 + turn_idx,
                'turns': 2,
                'input_tokens': 50,
                'cache_read_tokens': 5000 * turn_idx,
                'cache_creation_tokens': 1000,
                'output_tokens': 300,
                'cost': round(0.05 * turn_idx, 2),
                'check_calls': 1,
                'compile_retries': 0,
                'skill_bytes_read': 40000 if turn_idx == 1 else 0,
                'skill_read_calls': 2 if turn_idx == 1 else 0,
                'skill_unique_files': 2 if turn_idx == 1 else 0,
                'skill_files_read': {'android-design/SKILL.md': {'reads': 1, 'bytes': 40000}} if turn_idx == 1 else {},
                'message': f'Applied revision {turn_idx}.',
            }

        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / 'run'
            with mock.patch.object(multiturn, 'execute', side_effect=fake_execute), \
                    mock.patch.object(multiturn.eval_run, 'render', return_value=(['/tmp/a.png'], None)), \
                    mock.patch.object(multiturn, 'run_checks', return_value={'C-valid': {'pass': True}}):
                report = multiturn.run_multiturn(
                    out, max_turns=3, binary='/tmp/bin', jar='/tmp/w.jar',
                )
            self.assertEqual([c[0] for c in calls], ['claude', 'claude', 'claude'])
            self.assertEqual([c[1] for c in calls], [None, 'sess-fixed-1', 'sess-fixed-1'])
            self.assertEqual(report['turns_count'], 3)
            self.assertEqual(report['totals']['check_calls'], 3)
            self.assertEqual(report['totals']['cost'], 0.15)
            self.assertEqual(report['per_turn']['cost'], {'mean': 0.05, 'median': 0.05})
            self.assertEqual(report['totals']['skill_bytes_read'], 40000)
            self.assertTrue((out / 'multiturn-results.json').is_file())
            self.assertTrue((out / 'multiturn-summary.md').is_file())
            saved = json.loads((out / 'multiturn-results.json').read_text())
            self.assertEqual(saved['turns_count'], 3)


if __name__ == '__main__':
    unittest.main()
