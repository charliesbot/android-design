import json
import unittest
import judge


class JudgeContractTests(unittest.TestCase):
    def test_valid_boolean_ballot(self):
        result = judge.parse('{"items":[{"id":"R-one","pass":false,"reason":"Too small"}]}', ['R-one'])
        self.assertIs(result['R-one']['pass'], False)

    def test_malformed_ballots_are_rejected(self):
        for items in (
            [],
            [{'id':'R-one','pass':'false','reason':'bad'}],
            [{'id':'R-one','pass':True,'reason':''}],
            [{'id':'R-other','pass':True,'reason':'wrong id'}],
            [{'id':'R-one','pass':True,'reason':'ok'}] * 2,
        ):
            with self.subTest(items=items), self.assertRaises(ValueError):
                judge.parse(json.dumps({'items':items}), ['R-one'])

    def test_fenced_json_is_accepted(self):
        result = judge.parse('```json\n{"items":[{"id":"R-one","pass":true,"reason":"Clear"}]}\n```', ['R-one'])
        self.assertTrue(result['R-one']['pass'])

    def test_even_or_zero_votes_rejected(self):
        for count in (0, 2, -1):
            with self.assertRaises(ValueError):
                judge.validate_runs(count)
