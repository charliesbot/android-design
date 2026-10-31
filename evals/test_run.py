import json
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest import mock

import judge

import run

BINARY = '/Apps/AndroidDesignPreview'
VIEWS = ('light', 'dark', 'foldable', 'tablet', 'desktop')


def fake_case(directory):
    """A case directory shaped like prepare() leaves it, without running git."""
    design = directory / 'design'
    design.mkdir(parents=True)
    proposal = design / 'proposal.proposal.json'
    proposal.write_text('{"format": 1}')
    (directory / 'case.json').write_text(json.dumps({'proposal': str(proposal), 'title': 'music'}))
    return proposal


def write_report(directory, screens=('plates',), views=VIEWS, *, valid=True, failing=()):
    """What the app's `render` leaves in <case>/renders/: a PNG and a tree per screen and view, and report.json."""
    out = directory / 'renders'
    out.mkdir(exist_ok=True)
    entries = []
    for screen in screens:
        for view in views:
            (out / f'{screen}-{view}.png').write_bytes(b'png')
            (out / f'{screen}-{view}.tree.json').write_text('{}')
            bad = (screen, view) in failing
            entries.append({'screen': screen, 'view': view, 'png': f'{screen}-{view}.png',
                            'tree': f'{screen}-{view}.tree.json', 'pass': not bad,
                            'failures': ['touch-target: open 40x40'] if bad else []})
    report = {'proposal': 'p', 'design': None, 'theme': {'seed': '#B5523B', 'variant': 'tonal-spot', 'contrast': 0.0},
              'valid': valid, 'errors': [], 'renders': entries, 'pass': valid and not failing}
    (out / 'report.json').write_text(json.dumps(report))
    return [str((out / e['png']).resolve()) for e in entries]


def fake_render(directory, **kwargs):
    def fake_run(command, **options):
        write_report(directory, **kwargs)
        return subprocess.CompletedProcess(command, 0, 'ok', '')
    return fake_run


class PrepareTest(unittest.TestCase):
    def test_prepare_builds_a_fixture_repo_with_design_assets_and_no_starter(self):
        case = next(iter(run.select_cases('music')))
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp) / 'music'
            proposal = run.prepare(case, directory)
            log = subprocess.run(['git', 'log', '--oneline'], cwd=directory, capture_output=True, text=True)
            self.assertEqual(log.returncode, 0)
            self.assertEqual(len(log.stdout.strip().splitlines()), 1)
            self.assertTrue((directory / 'skills/android-design/SKILL.md').is_file())
            self.assertTrue((directory / 'skills/android-design-explore/SKILL.md').is_file())
            self.assertEqual(proposal, directory / 'design/proposal.proposal.json')
            self.assertTrue((directory / 'design/assets/album_art.jpg').is_file())
            self.assertEqual(sorted(p.name for p in (directory / 'design').iterdir()), ['assets'])
            self.assertFalse((directory / 'DESIGN.md').exists())
            self.assertFalse(proposal.exists())
            info = json.loads((directory / 'case.json').read_text())
            self.assertEqual(info, {'proposal': str(proposal), 'title': 'music'})
            self.assertEqual(run.proposal_path(directory), proposal)

    def test_prompt_names_the_proposal_the_design_file_and_both_skill_copies(self):
        case = next(iter(run.select_cases('music')))
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp) / 'music'
            proposal = run.prepare(case, directory)
            text = run.prompt_for(case, directory)
            self.assertIn(str(proposal), text)
            self.assertIn(str(directory / 'DESIGN.md'), text)
            self.assertIn(str(directory / 'skills/android-design'), text)
            self.assertIn(str(directory / 'skills/android-design-explore'), text)
            self.assertIn(case['prompt'], text)
            self.assertNotIn('{{', text)

    def test_evals_json_has_no_compose_wording_or_parked_cases(self):
        cases = json.loads((run.HERE / 'evals.json').read_text())['evals']
        self.assertEqual([c['name'] for c in cases], ['music', 'finance', 'settings', 'trek'])
        for case in cases:
            self.assertNotRegex(case['prompt'], r'R\.drawable|Compose|Kotlin|EvalScreen')
            self.assertIn('1920x1080', case['prompt'])
            codes = [a['id'] for a in case['assertions'] if a['type'] == 'code']
            self.assertEqual(codes, ['C-valid', 'C-review', 'C-palette'])
            self.assertIn('R-adaptive', [a['id'] for a in case['assertions']])
            for target in (f['to'] for f in case['files']):
                self.assertIn(target, case['prompt'])


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

    def test_render_runs_the_app_headlessly_into_the_case_renders(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            proposal = fake_case(directory)
            calls = []

            def fake_run(command, **kwargs):
                calls.append(command)
                return fake_render(directory, screens=('plates', 'detail'))(command, **kwargs)

            with mock.patch.object(run, 'app_binary', return_value=Path(BINARY)), \
                    mock.patch.object(run.subprocess, 'run', side_effect=fake_run):
                images, error = run.render(directory)
            self.assertIsNone(error)
            self.assertEqual(calls[0], [BINARY, 'render', str(proposal), '--out', str(directory / 'renders')])
            self.assertEqual(len(images), 10)
            self.assertTrue(all(p.endswith('.png') for p in images))
            self.assertTrue(run.render_complete(run.report_renders(directory)))

    def test_render_passes_the_design_file_the_agent_wrote(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)
            (directory / 'DESIGN.md').write_text('---\nversion: alpha\n---\n')
            with mock.patch.object(run, 'app_binary', return_value=Path(BINARY)), \
                    mock.patch.object(run.subprocess, 'run', side_effect=fake_render(directory)) as call:
                run.render(directory)
            command = call.call_args.args[0]
            self.assertEqual(command[command.index('--design') + 1], str(directory / 'DESIGN.md'))

    def test_render_clears_stale_renders(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)
            write_report(directory, views=('light',))
            stale = directory / 'renders/old.png'
            stale.write_bytes(b'old')
            with mock.patch.object(run, 'app_binary', return_value=Path(BINARY)), \
                    mock.patch.object(run.subprocess, 'run', side_effect=fake_render(directory, views=('dark',))):
                images, error = run.render(directory)
            self.assertFalse(stale.exists())
            self.assertEqual([Path(p).name for p in images], ['plates-dark.png'])

    def test_an_invalid_proposal_is_not_an_infrastructure_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)

            def invalid(command, **kwargs):
                write_report(directory, views=(), valid=False)
                return subprocess.CompletedProcess(command, 2, '', 'invalid')

            with mock.patch.object(run, 'app_binary', return_value=Path(BINARY)), \
                    mock.patch.object(run.subprocess, 'run', side_effect=invalid):
                images, error = run.render(directory)
            self.assertEqual(images, [])
            self.assertIsNone(error)
            self.assertFalse(run.render_complete(run.report_renders(directory)))

    def test_a_crash_without_a_report_is_an_infrastructure_error(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)
            crashed = subprocess.CompletedProcess([], 1, '', 'JVM crashed')
            with mock.patch.object(run, 'app_binary', return_value=Path(BINARY)), \
                    mock.patch.object(run.subprocess, 'run', return_value=crashed):
                images, error = run.render(directory)
            self.assertEqual(images, [])
            self.assertIn('JVM crashed', error)

    def test_a_missing_app_stops_the_render_with_the_install_hint(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)
            hint = RuntimeError('Run `./gradlew :app:installApp`')
            with mock.patch.object(run, 'app_binary', side_effect=hint), self.assertRaisesRegex(RuntimeError, 'installApp'):
                run.render(directory)

    def test_generation_hands_the_app_to_the_agent_over_mcp(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            (directory / 'prompt.md').write_text('brief')
            reply = {'message': 'done'}
            with mock.patch.object(run, 'app_binary', return_value=Path(BINARY)), \
                    mock.patch.object(run, 'execute', return_value=reply) as agent:
                run.generate(directory, 'claude')
            self.assertEqual(agent.call_args.kwargs['mcp_binary'], Path(BINARY))
            self.assertEqual(agent.call_args.kwargs['role'], 'generator')

    def test_missing_judge_is_pending_not_passed(self):
        case = {'name': 'music', 'surface': 'phone', 'assertions': [{'type': 'rubric', 'id': 'R-one'}]}
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp)
            directory = out / 'music'
            fake_case(directory)
            (out / 'rubric.md').write_text('Rubric')
            images = write_report(directory)
            ok = {'C-valid': {'pass': True}}
            with mock.patch.object(run, 'render', return_value=(images, None)), \
                    mock.patch.object(run, 'run_checks', return_value=ok), mock.patch.object(run, 'judge') as grade:
                result = run.evaluate_case(case, out)
            grade.assert_not_called()
            self.assertEqual(result['status'], 'pending')
            self.assertIsNone(result['judge']['R-one']['pass'])
            self.assertEqual(run.exit_code([result]), 2)
            self.assertEqual(len(result['renders']), 5)
            self.assertEqual(set(result['renders'][0]), {'screen', 'view', 'png', 'pass'})

    def test_failed_check_fails_the_case(self):
        case = {'name': 'music', 'surface': 'phone', 'assertions': [{'type': 'rubric', 'id': 'R-one'}]}
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp)
            directory = out / 'music'
            fake_case(directory)
            (out / 'rubric.md').write_text('Rubric')
            bad = {'C-valid': {'pass': False, 'detail': 'invalid'}}
            with mock.patch.object(run, 'render', return_value=([], None)), mock.patch.object(run, 'run_checks', return_value=bad):
                result = run.evaluate_case(case, out)
            self.assertEqual(result['status'], 'failed')
            self.assertEqual(run.exit_code([result]), 1)
            self.assertEqual(result['renders'], [])

    def test_judge_request_lists_each_render_with_its_screen_and_view(self):
        case = next(iter(run.select_cases('music')))
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            fake_case(directory)
            images = write_report(directory, screens=('player', 'queue'))
            (directory / 'DESIGN.md').write_text('---\nversion: alpha\n---\n')
            request = run.make_request(case, directory, (run.HERE / 'rubric.md').read_text())
            self.assertEqual(request['images'], sorted(images))
            for screen in ('player', 'queue'):
                for view in VIEWS:
                    self.assertIn(f'- {screen}/{view}: ', request['prompt'])
            self.assertIn('R-adaptive', request['item_ids'])
            self.assertNotIn('C-valid', request['item_ids'])
            self.assertNotIn('.tree.json', request['prompt'])
            hashed = set(request['artifacts'])
            self.assertIn('design/proposal.proposal.json', hashed)
            self.assertIn('DESIGN.md', hashed)
            self.assertIn('renders/player-desktop.png', hashed)
            self.assertFalse(any(k.endswith('.tree.json') for k in hashed))

    def test_import_refuses_changed_artifacts_and_wrong_request(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            proposal = fake_case(directory)
            (directory / 'renders').mkdir()
            (directory / 'renders/plates-light.png').write_bytes(b'first')
            case = {'name': 'music', 'surface': 'phone', 'assertions': [{'type': 'rubric', 'id': 'R-one'}]}
            request = run.make_request(case, directory, 'Rubric')
            ballot = {'request_id': request['request_id'], 'items': [{'id': 'R-one', 'pass': True, 'reason': 'Clear'}]}
            (directory / 'judge-request.json').write_text(json.dumps(request))
            (directory / 'judge-ballot.json').write_text(json.dumps(ballot))
            self.assertTrue(run.import_ballot(directory)['R-one']['pass'])
            (directory / 'renders/plates-light.png').write_bytes(b'changed')
            with self.assertRaisesRegex(ValueError, 'changed'):
                run.import_ballot(directory)
            (directory / 'renders/plates-light.png').write_bytes(b'first')
            self.assertTrue(run.import_ballot(directory)['R-one']['pass'])
            proposal.write_text('{"format": 2}')
            with self.assertRaisesRegex(ValueError, 'changed'):
                run.import_ballot(directory)
            proposal.write_text('{"format": 1}')
            (directory / 'DESIGN.md').write_text('added after judging')
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
