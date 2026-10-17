import json
import tempfile
import unittest
from pathlib import Path

import checks


def render(name, **result):
    return {'name': name, 'viewport': [411, 923], 'result': {'pass': True, 'complete': True, **result}}


class CaseDir:
    """A synthetic case directory with the files checks.py reads."""

    def __init__(self, tmp, *, lint=None, renders=None, index=None):
        self.path = Path(tmp)
        proposal = self.path / 'exploration/project/proposals/a'
        proposal.mkdir(parents=True)
        (self.path / 'case.json').write_text(json.dumps({'exploration': str(self.path / 'exploration'), 'proposal': 'a'}))
        if index is not None:
            (proposal / 'index.html').write_text(index)
        if lint is not None:
            (self.path / 'lint.json').write_text(json.dumps(lint))
        if renders is not None:
            (self.path / 'renders').mkdir()
            (self.path / 'renders/checks.json').write_text(json.dumps({'renders': renders}))


GOOD = '<m3e-theme color="#6750A4" variant="tonal-spot" scheme="light"></m3e-theme>'


class ChecksTests(unittest.TestCase):
    def run_checks(self, **kwargs):
        with tempfile.TemporaryDirectory() as tmp:
            return checks.run(CaseDir(tmp, **kwargs).path)

    def test_all_pass(self):
        out = self.run_checks(lint={'ok': True}, renders=[render('light'), render('dark')], index=GOOD)
        self.assertEqual(set(out), {'C-lint', 'C-review', 'C-palette'})
        self.assertTrue(all(v['pass'] for v in out.values()), out)

    def test_lint_failure_carries_the_lint_output(self):
        out = self.run_checks(lint={'ok': False, 'output': 'index.html:12 raw color #fff'}, index=GOOD)
        self.assertFalse(out['C-lint']['pass'])
        self.assertIn('raw color', out['C-lint']['detail'])
        self.assertFalse(out['C-review']['pass'])

    def test_lint_that_never_ran_fails(self):
        self.assertFalse(self.run_checks(index=GOOD)['C-lint']['pass'])

    def test_review_fails_when_any_render_fails(self):
        bad = render('tablet')
        bad['result'] = {'pass': False, 'complete': True, 'pageOverflow': {'fails': True}}
        out = self.run_checks(lint={'ok': True}, renders=[render('light'), bad], index=GOOD)
        self.assertFalse(out['C-review']['pass'])
        self.assertIn('tablet', out['C-review']['detail'])
        self.assertIn('pageOverflow', out['C-review']['detail'])

    def test_review_names_the_state_view_that_failed(self):
        bad = render('queue-light')
        bad['result'] = {'pass': False, 'complete': True, 'contrast': {'fails': [{'el': 'div', 'ratio': 1, 'need': 4.5}], 'passed': 3}}
        out = self.run_checks(lint={'ok': True}, renders=[render('light'), render('queue-tablet'), bad], index=GOOD)
        self.assertFalse(out['C-review']['pass'])
        self.assertEqual(out['C-review']['detail'], 'queue-light: contrast')

    def test_review_fails_when_a_declared_state_could_not_open(self):
        bad = render('queue-light')
        bad['result'] = {'pass': False, 'complete': False, 'error': 'proposalState is missing: index.html declares the state "queue"'}
        out = self.run_checks(lint={'ok': True}, renders=[render('light'), bad], index=GOOD)
        self.assertFalse(out['C-review']['pass'])
        self.assertIn('queue-light: proposalState is missing', out['C-review']['detail'])

    def test_incomplete_review_is_unverified_not_passed(self):
        out = self.run_checks(lint={'ok': True}, renders=[render('light'), render('dark', complete=False)], index=GOOD)
        self.assertFalse(out['C-review']['pass'])
        self.assertIn('unverified', out['C-review']['detail'])
        self.assertIn('dark', out['C-review']['detail'])

    def test_missing_review_fails(self):
        self.assertFalse(self.run_checks(lint={'ok': True}, index=GOOD)['C-review']['pass'])

    def test_palette_rejects_expressive_and_vibrant(self):
        for variant in ('expressive', 'vibrant', 'Vibrant'):
            html = f'<m3e-theme color="#6750A4" variant="{variant}"></m3e-theme>'
            out = self.run_checks(lint={'ok': True}, index=html)
            with self.subTest(variant=variant):
                self.assertFalse(out['C-palette']['pass'])
                self.assertIn(variant, out['C-palette']['detail'])

    def test_palette_accepts_other_variants_or_none(self):
        for html in (GOOD, '<m3e-theme color="#6750A4"></m3e-theme>'):
            self.assertTrue(self.run_checks(index=html)['C-palette']['pass'])

    def test_palette_fails_without_a_proposal(self):
        self.assertFalse(self.run_checks()['C-palette']['pass'])



class StatesCheckTests(unittest.TestCase):
    PAGE = '<meta name="p-states" content="{}">' + GOOD

    def run_states(self, index, shots=()):
        with tempfile.TemporaryDirectory() as tmp:
            case = CaseDir(tmp, lint={'ok': True}, renders=[render('light')], index=index)
            for name in shots:
                (case.path / 'renders' / f'{name}.png').write_bytes(b'png')
            return checks.run(case.path, 'phone', ['C-states'])['C-states']

    def shots(self, *names):
        return [f'{n}-{v}' for n in names for v in ('light', 'tablet')]

    def test_not_run_unless_the_case_asks_for_it(self):
        with tempfile.TemporaryDirectory() as tmp:
            self.assertNotIn('C-states', checks.run(CaseDir(tmp, lint={'ok': True}, index=GOOD).path))

    def test_passes_with_three_states_and_their_renders(self):
        out = self.run_states(self.PAGE.format('lyrics queue credits'), self.shots('lyrics', 'queue', 'credits'))
        self.assertTrue(out['pass'], out)

    def test_fails_without_p_states(self):
        out = self.run_states(GOOD)
        self.assertFalse(out['pass'])
        self.assertIn('p-states', out['detail'])

    def test_fails_with_fewer_than_three_states(self):
        out = self.run_states(self.PAGE.format('lyrics queue'), self.shots('lyrics', 'queue'))
        self.assertFalse(out['pass'])
        self.assertIn('at least three', out['detail'])

    def test_fails_when_a_state_render_is_missing(self):
        out = self.run_states(self.PAGE.format('lyrics queue credits'), self.shots('lyrics', 'queue') + ['credits-light'])
        self.assertFalse(out['pass'])
        self.assertIn('credits-tablet.png', out['detail'])


if __name__ == '__main__':
    unittest.main()
