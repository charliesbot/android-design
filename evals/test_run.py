import json
import re
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest import mock

import judge

import run


def fake_case(directory, *, files=()):
    """A case directory shaped like prepare() leaves it, without running git or node."""
    exploration = directory / 'design-explorations/x'
    proposal = exploration / 'project/proposals/a'
    proposal.mkdir(parents=True)
    (proposal / 'index.html').write_text('<m3e-theme variant="tonal-spot"></m3e-theme>')
    (directory / 'case.json').write_text(json.dumps({'exploration': str(exploration), 'proposal': 'a', 'title': 'music'}))
    return proposal


class PrepareTest(unittest.TestCase):
    def test_prepare_builds_a_fixture_repo_with_an_exploration_and_assets(self):
        case = next(iter(run.select_cases('music')))
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp) / 'music'
            exploration = run.prepare(case, directory)
            log = subprocess.run(['git', 'log', '--oneline'], cwd=directory, capture_output=True, text=True)
            self.assertEqual(log.returncode, 0)
            self.assertEqual(len(log.stdout.strip().splitlines()), 1)
            self.assertTrue((directory / 'skill/SKILL.md').is_file())
            self.assertTrue((exploration / 'project/proposals/a/index.html').is_file())
            self.assertTrue((exploration / 'project/proposals/a/assets/album_art.jpg').is_file())
            info = json.loads((directory / 'case.json').read_text())
            self.assertEqual(info['exploration'], str(exploration))
            self.assertEqual(run.proposal_dir(directory), exploration / 'project/proposals/a')

    def test_prompt_names_the_exploration_and_the_skill_copy(self):
        case = next(iter(run.select_cases('music')))
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp) / 'music'
            exploration = run.prepare(case, directory)
            text = run.prompt_for(case, directory)
            self.assertIn(str(exploration), text)
            self.assertIn(str(directory / 'skill'), text)
            self.assertIn(case['prompt'], text)
            self.assertNotIn('{{', text)

    def test_evals_json_has_no_compose_wording_or_parked_cases(self):
        cases = json.loads((run.HERE / 'evals.json').read_text())['evals']
        self.assertEqual([c['name'] for c in cases], ['music', 'finance', 'settings', 'trek'])
        for case in cases:
            self.assertNotRegex(case['prompt'], r'R\.drawable|Compose|Kotlin|EvalScreen')
            self.assertIn('1920x1080', case['prompt'])
            codes = [a['id'] for a in case['assertions'] if a['type'] == 'code']
            self.assertEqual(codes, ['C-lint', 'C-review', 'C-palette'])
            self.assertIn('R-adaptive', [a['id'] for a in case['assertions']])

    def test_render_names_match_the_skill_render(self):
        source = (run.SKILL_DIR / 'gallery/render.mjs').read_text()
        names = re.findall(r"\{ name: '(\w+)', viewport", source)
        self.assertEqual(tuple(names), run.RENDER_NAMES)


class ArtifactPipelineTests(unittest.TestCase):
    def test_prepare_run_never_launches_an_agent_and_refuses_overwrite(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / 'run'
            case = next(iter(run.select_cases('finance')))
            run.prepare_run([case], out)
            self.assertTrue((out / 'finance/prompt.md').is_file())
            self.assertTrue((out / 'manifest.json').is_file())
            with self.assertRaises(FileExistsError):
                run.prepare_run([case], out)

    def test_lint_failure_leaves_no_renders_and_clears_stale_ones(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)
            (directory / 'renders').mkdir()
            (directory / 'renders/light.png').write_bytes(b'old')
            failed = subprocess.CompletedProcess([], 2, '', 'proposals/a/style.css:3: Raw color')
            with mock.patch.object(run.subprocess, 'run', return_value=failed):
                images, error = run.render(directory)
            self.assertEqual(images, [])
            self.assertIsNone(error)
            self.assertFalse((directory / 'renders/light.png').exists())
            record = json.loads((directory / 'lint.json').read_text())
            self.assertFalse(record['ok'])
            self.assertIn('Raw color', record['output'])

    def test_chrome_failure_is_an_infrastructure_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)
            crashed = subprocess.CompletedProcess([], 1, '', 'Chrome not found')
            with mock.patch.object(run.subprocess, 'run', return_value=crashed):
                images, error = run.render(directory)
            self.assertEqual(images, [])
            self.assertIn('Chrome not found', error)

    def test_render_calls_the_skill_render_and_copies_its_output(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)
            exploration = Path(run.case_info(directory)['exploration'])
            calls = []

            def fake_run(command, **kwargs):
                calls.append(command)
                out = exploration / 'renders' / 'a'
                out.mkdir(parents=True)
                for name in run.RENDER_NAMES:
                    (out / f'{name}.png').write_bytes(b'png')
                (out / 'checks.json').write_text('{"renders": []}')
                (out / 'view.html').write_text('big')
                return subprocess.CompletedProcess(command, 0, 'a: pass', '')

            with mock.patch.object(run.subprocess, 'run', side_effect=fake_run):
                images, error = run.render(directory)
            self.assertIsNone(error)
            self.assertEqual(calls[0][2:], ['render', str(exploration), 'a'])
            self.assertEqual(len(images), 6)
            self.assertTrue(run.render_complete(images))
            self.assertTrue((directory / 'renders/checks.json').is_file())
            self.assertFalse((directory / 'renders/view.html').exists())
            self.assertTrue(json.loads((directory / 'lint.json').read_text())['ok'])

    def test_missing_judge_is_pending_not_passed(self):
        case = {'name': 'music', 'surface': 'phone', 'assertions': [{'type': 'rubric', 'id': 'R-one'}]}
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp)
            directory = out / 'music'
            fake_case(directory)
            (out / 'rubric.md').write_text('Rubric')
            images = []
            (directory / 'renders').mkdir()
            for name in run.RENDER_NAMES:
                image = directory / 'renders' / f'{name}.png'
                image.write_bytes(b'png')
                images.append(str(image))
            ok = {'C-lint': {'pass': True}}
            with mock.patch.object(run, 'render', return_value=(images, None)), \
                    mock.patch.object(run, 'run_checks', return_value=ok), mock.patch.object(run, 'judge') as grade:
                result = run.evaluate_case(case, out)
            grade.assert_not_called()
            self.assertEqual(result['status'], 'pending')
            self.assertIsNone(result['judge']['R-one']['pass'])
            self.assertEqual(run.exit_code([result]), 2)

    def test_failed_check_fails_the_case(self):
        case = {'name': 'music', 'surface': 'phone', 'assertions': [{'type': 'rubric', 'id': 'R-one'}]}
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp)
            directory = out / 'music'
            fake_case(directory)
            (out / 'rubric.md').write_text('Rubric')
            (directory / 'renders').mkdir()
            bad = {'C-lint': {'pass': False, 'detail': 'lint'}}
            with mock.patch.object(run, 'render', return_value=([], None)), mock.patch.object(run, 'run_checks', return_value=bad):
                result = run.evaluate_case(case, out)
            self.assertEqual(result['status'], 'failed')
            self.assertEqual(run.exit_code([result]), 1)

    def test_judge_request_lists_the_renders(self):
        case = next(iter(run.select_cases('music')))
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)
            (directory / 'renders').mkdir()
            for name in run.RENDER_NAMES:
                (directory / 'renders' / f'{name}.png').write_bytes(b'png')
            (directory / 'renders/checks.json').write_text('{}')
            request = run.make_request(case, directory, (run.HERE / 'rubric.md').read_text())
            self.assertEqual([Path(p).stem for p in request['images']], sorted(run.RENDER_NAMES))
            for name in run.RENDER_NAMES:
                self.assertIn(f'- {name}: ', request['prompt'])
            self.assertIn('R-adaptive', request['item_ids'])
            self.assertNotIn('C-lint', request['item_ids'])
            self.assertIn('desktop', request['prompt'])
            hashed = set(request['artifacts'])
            self.assertTrue(any(k.endswith('proposals/a/index.html') for k in hashed))
            self.assertTrue(any(k.endswith('renders/desktop.png') for k in hashed))

    def test_import_refuses_changed_artifacts_and_wrong_request(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            proposal = fake_case(directory)
            (directory / 'renders').mkdir()
            (directory / 'renders/light.png').write_bytes(b'first')
            case = {'name': 'music', 'surface': 'phone', 'assertions': [{'type': 'rubric', 'id': 'R-one'}]}
            request = run.make_request(case, directory, 'Rubric')
            ballot = {'request_id': request['request_id'], 'items': [{'id': 'R-one', 'pass': True, 'reason': 'Clear'}]}
            (directory / 'judge-request.json').write_text(json.dumps(request))
            (directory / 'judge-ballot.json').write_text(json.dumps(ballot))
            self.assertTrue(run.import_ballot(directory)['R-one']['pass'])
            (directory / 'renders/light.png').write_bytes(b'changed')
            with self.assertRaisesRegex(ValueError, 'changed'):
                run.import_ballot(directory)
            (directory / 'renders/light.png').write_bytes(b'first')
            self.assertTrue(run.import_ballot(directory)['R-one']['pass'])
            (proposal / 'index.html').write_text('edited')
            with self.assertRaisesRegex(ValueError, 'changed'):
                run.import_ballot(directory)

    def test_artifact_judging_uses_the_same_brief_and_prompt_as_calibration(self):
        case = next(c for c in run.select_cases('finance'))
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            (directory / 'renders').mkdir()
            (directory / 'renders/light.png').write_bytes(b'png')
            rubric = (run.HERE / 'rubric.md').read_text()
            request = run.make_request(case, directory, rubric)
            self.assertIn(case['prompt'], request['prompt'])
            self.assertEqual(request['prompt'], judge.prompt_for(
                case['name'], request['images'], request['item_ids']))

    def test_case_selection_rejects_unknown_names(self):
        with self.assertRaises(ValueError):
            run.select_cases('typo')


if __name__ == "__main__":
    unittest.main()
