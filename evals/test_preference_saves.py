import io
import json
import tempfile
import threading
import time
import unittest
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from unittest.mock import patch

from preference import serve


class PreferenceSavesTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.path = self.root / 'selections.json'
        self.path.write_text(json.dumps({'existing': {'note': 'keep'}}))
        self.handler = serve.make_handler(self.root)

    def post(self, case, note):
        handler = object.__new__(self.handler)
        payload = json.dumps({'case': case, 'note': note}).encode()
        handler.path = '/api/select'
        handler.headers = {'Content-Length': str(len(payload))}
        handler.rfile = io.BytesIO(payload)
        responses = []
        handler.send = lambda status, body, content_type: responses.append(status)
        try:
            handler.do_POST()
        except OSError:
            responses.append('uncaught write error')
        return responses

    def test_concurrent_saves_keep_both_cases_and_existing_picks(self):
        original_read = Path.read_text
        first_read = threading.Event()
        second_started = threading.Event()

        def delayed_read(path, *args, **kwargs):
            content = original_read(path, *args, **kwargs)
            if path == self.path and threading.current_thread().name.endswith('_0'):
                first_read.set()
                self.assertTrue(second_started.wait(2))
                time.sleep(0.05)
            return content

        def second_save():
            self.assertTrue(first_read.wait(2))
            second_started.set()
            return self.post('finance', 'second')

        with patch.object(Path, 'read_text', delayed_read), ThreadPoolExecutor(2) as pool:
            first = pool.submit(self.post, 'music', 'first')
            second = pool.submit(second_save)
            self.assertEqual(first.result(), [200])
            self.assertEqual(second.result(), [200])
        self.assertEqual(set(json.loads(self.path.read_text())), {'existing', 'music', 'finance'})

    def test_failed_atomic_replace_keeps_previous_file_and_returns_error(self):
        before = self.path.read_bytes()
        with patch('os.replace', side_effect=OSError('disk failure')):
            self.assertEqual(self.post('music', 'new'), [500])
        self.assertEqual(self.path.read_bytes(), before)
        self.assertEqual(list(self.root.iterdir()), [self.path])

    def test_write_failure_returns_error_without_truncating_existing_file(self):
        before = self.path.read_bytes()
        with patch('json.dump', side_effect=OSError('disk full')):
            self.assertEqual(self.post('music', 'new'), [500])
        self.assertEqual(self.path.read_bytes(), before)
        self.assertEqual(list(self.root.iterdir()), [self.path])


if __name__ == '__main__':
    unittest.main()
