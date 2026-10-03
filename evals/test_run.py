import os
import json
import tempfile
import unittest
from pathlib import Path
from unittest import mock

import judge

import run


class PrepareTest(unittest.TestCase):
    def test_project_points_at_the_sdk_without_android_home(self):
        # A runner started from a shell without ANDROID_HOME must still render: the re-render
        # cannot rely on the agent having written local.properties.
        with tempfile.TemporaryDirectory() as tmp:
            sdk = Path(tmp) / "sdk"
            sdk.mkdir()
            case = {"name": "music", "surface": "phone", "files": []}
            env = {k: v for k, v in os.environ.items() if k not in ("ANDROID_HOME", "ANDROID_SDK_ROOT")}
            with mock.patch.dict(os.environ, env, clear=True), mock.patch.object(run, "DEFAULT_SDK", sdk):
                project = run.prepare(case, Path(tmp) / "case")
            self.assertEqual((project / "local.properties").read_text().strip(), f"sdk.dir={sdk}")

class ArtifactPipelineTests(unittest.TestCase):
    def test_prepare_run_never_launches_an_agent_and_refuses_overwrite(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / 'run'
            case = {'name': 'example', 'surface': 'phone', 'prompt': 'A screen', 'assertions': []}
            with mock.patch.object(run.subprocess, 'run') as launch:
                run.prepare_run([case], out)
                launch.assert_not_called()
            self.assertTrue((out / 'example/prompt.md').is_file())
            self.assertTrue((out / 'manifest.json').is_file())
            with self.assertRaises(FileExistsError):
                run.prepare_run([case], out)

    def test_failed_render_does_not_copy_stale_images(self):
        with tempfile.TemporaryDirectory() as tmp:
            case_dir = Path(tmp)
            project = case_dir / 'project'
            for folder in (project / 'renders', case_dir / 'renders'):
                folder.mkdir(parents=True)
                (folder / 'light.png').write_bytes(b'old')
            with mock.patch.object(run, 'restore_harness'), mock.patch.object(run.subprocess, 'run', return_value=mock.Mock(returncode=1, stdout='', stderr='build failed')):
                images, error = run.render({'surface':'phone'}, case_dir)
            self.assertEqual(images, [])
            self.assertIn('build failed', error)
            self.assertFalse((case_dir / 'renders/light.png').exists())

    def test_missing_judge_is_pending_not_passed(self):
        case = {'name':'music','surface':'phone','assertions':[{'type':'rubric','id':'R-one'}]}
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp)
            directory = out / 'music'
            (directory / 'project').mkdir(parents=True)
            (out / 'rubric.md').write_text('Rubric')
            image = directory / 'renders/light.png'
            image.parent.mkdir()
            image.write_bytes(b'png')
            with mock.patch.object(run, 'render', return_value=([str(image)], None)), mock.patch.object(run, 'run_checks', return_value={'C-build':{'pass':True}}), mock.patch.object(run, 'judge') as grade:
                result = run.evaluate_case(case, out)
            grade.assert_not_called()
            self.assertEqual(result['status'], 'pending')
            self.assertIsNone(result['judge']['R-one']['pass'])
            self.assertEqual(run.exit_code([result]), 2)

    def test_import_refuses_changed_artifacts_and_wrong_request(self):
        with tempfile.TemporaryDirectory() as tmp:
            directory = Path(tmp)
            (directory / 'project').mkdir()
            (directory / 'renders').mkdir()
            (directory / 'renders/light.png').write_bytes(b'first')
            case = {'name':'music','surface':'phone','assertions':[{'type':'rubric','id':'R-one'}]}
            request = run.make_request(case, directory, 'Rubric')
            ballot = {'request_id':request['request_id'],'items':[{'id':'R-one','pass':True,'reason':'Clear'}]}
            (directory / 'judge-request.json').write_text(json.dumps(request))
            (directory / 'judge-ballot.json').write_text(json.dumps(ballot))
            self.assertTrue(run.import_ballot(directory)['R-one']['pass'])
            (directory / 'renders/light.png').write_bytes(b'changed')
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
