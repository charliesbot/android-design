import json
import tempfile
import unittest
from pathlib import Path

import checks


def render(screen, view, *failures):
    return {'screen': screen, 'view': view, 'png': f'{screen}-{view}.png', 'tree': f'{screen}-{view}.tree.json',
            'pass': not failures, 'failures': list(failures)}


def report(renders, *, valid=True, errors=(), variant='tonal-spot'):
    return {'proposal': '/case/design/proposal.proposal.json', 'design': '/case/DESIGN.md',
            'theme': {'seed': '#B5523B', 'variant': variant, 'contrast': 0.0},
            'valid': valid, 'errors': list(errors), 'renders': renders,
            'pass': valid and all(r['pass'] for r in renders)}


def case_dir(tmp, data=None):
    """A synthetic case directory holding the report.json the app's `render` leaves."""
    path = Path(tmp)
    if data is not None:
        (path / 'renders').mkdir()
        (path / 'renders/report.json').write_text(json.dumps(data))
    return path


GOOD = [render('plates', 'light'), render('plates', 'dark')]


class ChecksTests(unittest.TestCase):
    def run_checks(self, data=None):
        with tempfile.TemporaryDirectory() as tmp:
            return checks.run(case_dir(tmp, data))

    def test_all_pass(self):
        out = self.run_checks(report(GOOD))
        self.assertEqual(set(out), {'C-valid', 'C-review', 'C-palette'})
        self.assertTrue(all(v['pass'] for v in out.values()), out)

    def test_invalid_proposal_carries_the_first_errors(self):
        errors = [{'path': 'screens.plates.components[3]', 'message': 'unknown component Carousel'},
                  {'path': 'navigation.start', 'message': 'no screen named home'}]
        out = self.run_checks(report([], valid=False, errors=errors))
        self.assertFalse(out['C-valid']['pass'])
        self.assertIn('components[3]: unknown component Carousel', out['C-valid']['detail'])
        self.assertIn('navigation.start', out['C-valid']['detail'])
        self.assertFalse(out['C-review']['pass'])

    def test_only_the_first_errors_are_reported(self):
        errors = [{'path': f'p{i}', 'message': f'm{i}'} for i in range(10)]
        detail = self.run_checks(report([], valid=False, errors=errors))['C-valid']['detail']
        self.assertIn('p0', detail)
        self.assertNotIn('p9', detail)

    def test_a_proposal_that_was_never_rendered_fails_every_report_check(self):
        out = self.run_checks()
        for key in ('C-valid', 'C-review', 'C-palette'):
            self.assertFalse(out[key]['pass'], key)
        self.assertIn('report.json', out['C-valid']['detail'])

    def test_render_log_explains_a_missing_report(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = case_dir(tmp)
            (directory / 'render.log').write_text('no such file: proposal.json')
            self.assertIn('no such file', checks.run(directory)['C-valid']['detail'])

    def test_review_names_each_failing_screen_view_and_rule(self):
        bad = render('plates', 'tablet', 'touch-target: open_plate 40x40', 'contrast: desc 3.1:1')
        out = self.run_checks(report([*GOOD, bad, render('detail', 'dark', 'font: title Roboto')]))
        self.assertFalse(out['C-review']['pass'])
        self.assertEqual(out['C-review']['detail'],
                         'plates/tablet: touch-target; plates/tablet: contrast; detail/dark: font')

    def test_review_fails_a_failed_render_without_listed_rules(self):
        bad = render('plates', 'dark')
        bad['pass'] = False
        out = self.run_checks(report([render('plates', 'light'), bad]))
        self.assertFalse(out['C-review']['pass'])
        self.assertEqual(out['C-review']['detail'], 'plates/dark: failed')

    def test_review_with_no_renders_is_not_a_pass(self):
        out = self.run_checks(report([]))
        self.assertFalse(out['C-review']['pass'])
        self.assertIn('no renders', out['C-review']['detail'])

    def test_palette_rejects_expressive_and_vibrant(self):
        for variant in ('expressive', 'vibrant', 'Vibrant'):
            out = self.run_checks(report(GOOD, variant=variant))
            with self.subTest(variant=variant):
                self.assertFalse(out['C-palette']['pass'])
                self.assertIn(variant, out['C-palette']['detail'])

    def test_palette_accepts_the_other_variants(self):
        for variant in ('tonal-spot', 'neutral', 'monochrome', 'fidelity', 'content'):
            with self.subTest(variant=variant):
                self.assertTrue(self.run_checks(report(GOOD, variant=variant))['C-palette']['pass'])

    def test_palette_fails_when_the_report_has_no_theme(self):
        data = report(GOOD)
        del data['theme']
        self.assertFalse(self.run_checks(data)['C-palette']['pass'])

    def test_an_unreadable_report_is_a_failure_not_a_crash(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = case_dir(tmp, report(GOOD))
            (directory / 'renders/report.json').write_text('{not json')
            self.assertFalse(checks.run(directory)['C-valid']['pass'])


class WearSurfaceTests(unittest.TestCase):
    def test_wear_gets_valid_and_review_but_not_palette(self):
        data = report([render('main', 'watch'), render('main', 'watch-large')], variant='vibrant')
        with tempfile.TemporaryDirectory() as tmp:
            out = checks.run(case_dir(tmp, data), 'wear')
        self.assertEqual(set(out), {'C-valid', 'C-review'})
        self.assertTrue(all(v['pass'] for v in out.values()), out)

    def test_wear_review_names_the_failing_watch_view(self):
        data = report([render('main', 'watch'), render('main', 'watch-large', 'touch-target: end 40x40')])
        with tempfile.TemporaryDirectory() as tmp:
            out = checks.run(case_dir(tmp, data), 'wear')
        self.assertEqual(out['C-review']['detail'], 'main/watch-large: touch-target')

    def test_a_wear_case_that_was_never_rendered_fails_its_checks(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = checks.run(case_dir(tmp), 'wear')
        self.assertEqual(set(out), {'C-valid', 'C-review'})
        self.assertFalse(any(v['pass'] for v in out.values()))

    def test_unknown_surface_is_rejected_and_phone_is_the_default(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = case_dir(tmp, report(GOOD))
            with self.assertRaises(ValueError):
                checks.run(directory, 'tv')
            self.assertEqual(set(checks.run(directory)), {'C-valid', 'C-review', 'C-palette'})
        self.assertEqual(checks.SURFACES, {'phone', 'wear'})


if __name__ == '__main__':
    unittest.main()
