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
        self.assertEqual([c['name'] for c in cases], ['music', 'finance', 'settings', 'trek', 'recipes', 'workout', 'player', 'reminder'])
        for case in cases:
            self.assertNotRegex(case['prompt'], r'R\.drawable|Compose|Kotlin|EvalScreen')
            ids = [a['id'] for a in case['assertions']]
            codes = [a['id'] for a in case['assertions'] if a['type'] == 'code']
            if case['surface'] == 'phone':
                self.assertIn('1920x1080', case['prompt'])
                self.assertEqual(codes, ['C-valid', 'C-review', 'C-palette'])
                self.assertIn('R-adaptive', ids)
            for target in (f['to'] for f in case['files']):
                self.assertIn(target, case['prompt'])

    def test_wear_cases_use_the_wear_checks_and_items_and_no_phone_only_ones(self):
        cases = [c for c in json.loads((run.HERE / 'evals.json').read_text())['evals'] if c['surface'] == 'wear']
        self.assertEqual([c['name'] for c in cases], ['workout', 'player', 'reminder'])
        rubric = (run.HERE / 'rubric.md').read_text()
        phone_only = {'R-fill', 'R-surfaces', 'R-font', 'R-platform', 'R-dark', 'R-adaptive', 'R-idea'}
        wear = {'R-wear-round', 'R-wear-glance', 'R-wear-hero', 'R-wear-sizes', 'R-wear-color'}
        for case in cases:
            ids = [a['id'] for a in case['assertions']]
            self.assertEqual([a['id'] for a in case['assertions'] if a['type'] == 'code'], ['C-valid', 'C-review'])
            self.assertTrue({'R-hierarchy', 'R-controls', 'R-text'} | wear <= set(ids), case['name'])
            self.assertFalse(phone_only & set(ids), case['name'])
            own = [i for i in ids if i.startswith(f"R-{case['name']}-")]
            self.assertTrue(own, case['name'])
            self.assertIn('204x204', case['prompt'])
            self.assertIn('227x227', case['prompt'])
            self.assertEqual(case['files'], [])
            for key in ids:
                if key.startswith('R-') and key not in own:
                    self.assertIn(f'**{key}:**', rubric)

    def test_wear_prompt_targets_the_watches_and_leaves_no_phone_views(self):
        for case in run.select_cases('workout,music'):
            with tempfile.TemporaryDirectory() as tmp:
                directory = Path(tmp) / case['name']
                run.prepare(case, directory)
                text = run.prompt_for(case, directory)
            self.assertNotIn('{{', text)
            if case['surface'] == 'wear':
                self.assertIn('204x204', text)
                self.assertIn('227x227', text)
                self.assertIn('both watch views', text)
                for phone in ('411x923', '1920x1080', 'foldable', 'five views'):
                    self.assertNotIn(phone, text)
            else:
                self.assertIn('411x923', text)
                self.assertIn('all five views', text)
                self.assertNotIn('227x227', text)

    def test_a_wear_case_is_checked_without_the_palette_check_and_judged_with_its_own_items(self):
        case = next(iter(run.select_cases('workout')))
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp)
            directory = out / 'workout'
            fake_case(directory)
            (out / 'rubric.md').write_text('Rubric')
            with mock.patch('subprocess.run', fake_render(directory, screens=('main',), views=('watch', 'watch-large'))):
                result = run.evaluate_case(case, out)
            self.assertEqual(set(result['checks']), {'C-valid', 'C-review'})
            self.assertEqual(result['surface'], 'wear')
            self.assertEqual(set(result['judge']), {a['id'] for a in case['assertions'] if a['type'] == 'rubric'})
            self.assertIn('R-wear-round', result['judge'])
            self.assertNotIn('R-adaptive', result['judge'])
            request = json.loads((directory / 'judge-request.json').read_text())
            self.assertIn('main/watch-large', request['prompt'])
            self.assertEqual(result['status'], 'pending')

    def test_wear_calibration_labels_match_their_sources_and_cases(self):
        src = run.HERE / 'calibration/wear-src'
        labels = json.loads((run.HERE / 'calibration/labels.json').read_text())['images']
        planned = [label for label in labels if label.get('images', [''])[0].startswith('wear-')]
        cases = {c['name']: c for c in json.loads((run.HERE / 'evals.json').read_text())['evals']}
        catalog = 'https://github.com/charliesbot/android-design/catalogs/wear-m3-expressive/v1'
        for label in planned:
            name = label['images'][0].removeprefix('wear-').removesuffix('-watch.png')
            proposal = json.loads((src / f'{name}.proposal.json').read_text())
            self.assertTrue(all(s['catalogId'] == catalog for s in proposal['screens'].values()), name)
            ids = {a['id'] for a in cases[label['case']]['assertions']}
            self.assertLessEqual(set(label['expect']), ids, name)
            self.assertTrue(all((run.HERE / 'calibration' / image).is_file() for image in label['images']), name)
        self.assertEqual(len(planned), len(list(src.glob('*.proposal.json'))))


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
