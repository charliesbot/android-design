import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

import calibrate
import report
import run

HERE = Path(__file__).resolve().parent


class ArtifactCliTests(unittest.TestCase):
    def test_promotion_rejects_pending_judgments(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            (directory / 'results.json').write_text(json.dumps({'cases':[{'name':'music','surface':'phone','status':'pending','checks':{'C-build':{'pass':True}},'judge':{'R-one':{'pass':None}}}]}))
            result = subprocess.run([sys.executable, str(HERE / 'promote.py'), str(directory)], capture_output=True, text=True)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn('incomplete', result.stderr)

    def test_scoring_without_launching_backend(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp)
            directory = out / 'music'
            (directory / 'renders').mkdir(parents=True)
            (directory / 'renders/light.png').write_bytes(b'fixture')
            case = {'name':'music','surface':'phone','assertions':[{'id':'R-one','type':'rubric'}]}
            request = run.make_request(case, directory, 'test rubric')
            (directory / 'judge-request.json').write_text(json.dumps(request))
            ballot = {'request_id':request['request_id'],'items':[{'id':'R-one','pass':True,'reason':'Test fixture verdict'}]}
            (directory / 'judge-ballot.json').write_text(json.dumps(ballot))
            (out / 'manifest.json').write_text(json.dumps({'version':1,'cases':[case]}))
            result = {'name':'music','surface':'phone','status':'pending','request_id':request['request_id'],
                      'checks':{'C-build':{'pass':True}},'judge':{'R-one':{'pass':None}},'renders':['light.png'],
                      'agent':{'cost':None,'message':'fixture'},'minutes':0}
            (out / 'results.json').write_text(json.dumps({'cases':[result]}))
            with patch.object(sys, 'argv', ['run.py','score','--out',str(out),'--judge-label','test fixture','--no-open']), patch('backends.execute') as backend:
                self.assertEqual(run.main(), 0)
                backend.assert_not_called()
            self.assertEqual(json.loads((out/'results.json').read_text())['cases'][0]['status'], 'passed')

    def test_calibration_requests_bind_every_fixture_including_jpegs(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / 'calibration'
            with patch.object(sys, 'argv', ['calibrate.py', 'prepare', '--out', str(out)]):
                self.assertEqual(calibrate.main(), 0)
            labels = json.loads((out / 'labels.json').read_text())
            self.assertEqual(len(labels), 7)
            jpeg_directory = None
            for index, label in enumerate(labels):
                directory = out / str(index)
                request = json.loads((directory / 'judge-request.json').read_text())
                image = directory / 'renders' / Path(label['image']).name
                self.assertEqual(request['images'], [str(image.resolve())])
                self.assertIn(str(image.relative_to(directory)), request['artifacts'])
                ballot = {'request_id': request['request_id'], 'items': [
                    {'id': item, 'pass': True, 'reason': 'Fixture verdict'}
                    for item in request['item_ids']]}
                (directory / 'judge-ballot.json').write_text(json.dumps(ballot))
                run.import_ballot(directory)
                if image.suffix == '.jpg':
                    jpeg_directory = directory
            self.assertIsNotNone(jpeg_directory)
            image = next((jpeg_directory / 'renders').glob('*.jpg'))
            image.write_bytes(image.read_bytes() + b'changed')
            with self.assertRaisesRegex(ValueError, 'artifacts changed'):
                run.import_ballot(jpeg_directory)

    def test_pending_is_visible_in_report(self):
        self.assertIn('Pending:', report.failures({'R-one':{'pass':None,'reason':'not graded'}}))
        self.assertEqual(report.score({'R-one':{'pass':None}}), (0,1))
