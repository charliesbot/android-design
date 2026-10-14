import json
import tempfile
import unittest
from pathlib import Path

import checks


def render(name, **result):
    return {'name': name, 'viewport': [411, 923], 'result': {'pass': True, 'complete': True, **result}}


class CaseDir:
    """A synthetic case directory with the files checks.py reads."""

    def __init__(self, tmp, *, publish=None, renders=None, index=None):
        self.path = Path(tmp)
        proposal = self.path / 'exploration/project/proposals/a'
        proposal.mkdir(parents=True)
        (self.path / 'case.json').write_text(json.dumps({'exploration': str(self.path / 'exploration'), 'proposal': 'a'}))
        if index is not None:
            (proposal / 'index.html').write_text(index)
        if publish is not None:
            (self.path / 'publish.json').write_text(json.dumps(publish))
        if renders is not None:
            (self.path / 'renders').mkdir()
            (self.path / 'renders/review.json').write_text(json.dumps({'renders': renders}))


GOOD = '<m3e-theme color="#6750A4" variant="tonal-spot" scheme="light"></m3e-theme>'


class ChecksTests(unittest.TestCase):
    def run_checks(self, **kwargs):
        with tempfile.TemporaryDirectory() as tmp:
            return checks.run(CaseDir(tmp, **kwargs).path)

    def test_all_pass(self):
        out = self.run_checks(publish={'ok': True}, renders=[render('light'), render('dark')], index=GOOD)
        self.assertEqual(set(out), {'C-publish', 'C-review', 'C-palette'})
        self.assertTrue(all(v['pass'] for v in out.values()), out)

    def test_publish_failure_carries_the_lint_output(self):
        out = self.run_checks(publish={'ok': False, 'output': 'index.html:12 raw color #fff'}, index=GOOD)
        self.assertFalse(out['C-publish']['pass'])
        self.assertIn('raw color', out['C-publish']['detail'])
        self.assertFalse(out['C-review']['pass'])

    def test_publish_that_never_ran_fails(self):
        self.assertFalse(self.run_checks(index=GOOD)['C-publish']['pass'])

    def test_review_fails_when_any_render_fails(self):
        bad = render('tablet')
        bad['result'] = {'pass': False, 'complete': True, 'pageOverflow': {'fails': True}}
        out = self.run_checks(publish={'ok': True}, renders=[render('light'), bad], index=GOOD)
        self.assertFalse(out['C-review']['pass'])
        self.assertIn('tablet', out['C-review']['detail'])
        self.assertIn('pageOverflow', out['C-review']['detail'])

    def test_incomplete_review_is_unverified_not_passed(self):
        out = self.run_checks(publish={'ok': True}, renders=[render('light'), render('dark', complete=False)], index=GOOD)
        self.assertFalse(out['C-review']['pass'])
        self.assertIn('unverified', out['C-review']['detail'])
        self.assertIn('dark', out['C-review']['detail'])

    def test_missing_review_fails(self):
        self.assertFalse(self.run_checks(publish={'ok': True}, index=GOOD)['C-review']['pass'])

    def test_palette_rejects_expressive_and_vibrant(self):
        for variant in ('expressive', 'vibrant', 'Vibrant'):
            html = f'<m3e-theme color="#6750A4" variant="{variant}"></m3e-theme>'
            out = self.run_checks(publish={'ok': True}, index=html)
            with self.subTest(variant=variant):
                self.assertFalse(out['C-palette']['pass'])
                self.assertIn(variant, out['C-palette']['detail'])

    def test_palette_accepts_other_variants_or_none(self):
        for html in (GOOD, '<m3e-theme color="#6750A4"></m3e-theme>'):
            self.assertTrue(self.run_checks(index=html)['C-palette']['pass'])

    def test_palette_fails_without_a_proposal(self):
        self.assertFalse(self.run_checks()['C-palette']['pass'])


if __name__ == '__main__':
    unittest.main()
