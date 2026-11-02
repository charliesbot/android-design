import shutil
import tempfile
import unittest
from pathlib import Path

import trigger_eval


def write_skill(directory, name, description='Old description.'):
    directory.mkdir(parents=True)
    (directory / 'SKILL.md').write_text(f'---\nname: {name}\ndescription: >\n  {description}\n---\n\n# Body\n')


class SetDescriptionTest(unittest.TestCase):
    def test_replaces_the_description_and_keeps_name_and_body(self):
        root = Path(tempfile.mkdtemp())
        self.addCleanup(shutil.rmtree, root)
        write_skill(root / 'android-design', 'android-design')
        trigger_eval.set_description(root / 'android-design' / 'SKILL.md', 'New line one.\nNew line two.\n')
        self.assertEqual(
            (root / 'android-design' / 'SKILL.md').read_text(),
            '---\nname: android-design\ndescription: >\n  New line one.\n  New line two.\n---\n\n# Body\n',
        )


class BuildSandboxTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(tempfile.mkdtemp())
        self.addCleanup(shutil.rmtree, self.root)
        self.installed = self.root / 'installed'
        write_skill(self.installed / 'android-design', 'android-design', 'Installed copy.')
        write_skill(self.installed / 'adaptive', 'adaptive')
        (self.installed / 'synced').mkdir()
        write_skill(self.installed / 'android-design-explore', 'android-design-explore', 'Installed copy.')
        write_skill(self.root / 'repo' / 'android-design', 'android-design', 'Repo copy.')
        write_skill(self.root / 'repo' / 'android-design-explore', 'android-design-explore', 'Repo explore.')

    def test_copies_the_repo_skills_and_links_the_other_installed_ones(self):
        sandbox = trigger_eval.build_sandbox(self.root / 'repo', self.installed, 'android-design', 'Candidate.')
        self.addCleanup(shutil.rmtree, sandbox)
        skills = sandbox / '.claude' / 'skills'
        self.assertEqual(
            sorted(p.name for p in skills.iterdir()), ['adaptive', 'android-design', 'android-design-explore'],
        )
        self.assertTrue((skills / 'adaptive').is_symlink())
        self.assertFalse((skills / 'android-design').is_symlink())
        self.assertIn('Candidate.', (skills / 'android-design' / 'SKILL.md').read_text())
        self.assertIn('Repo explore.', (skills / 'android-design-explore' / 'SKILL.md').read_text())
        self.assertIn('Installed copy.', (self.installed / 'android-design' / 'SKILL.md').read_text())

    def test_keeps_the_repo_description_without_a_candidate(self):
        sandbox = trigger_eval.build_sandbox(self.root / 'repo', self.installed, 'android-design')
        self.addCleanup(shutil.rmtree, sandbox)
        self.assertIn('Repo copy.', (sandbox / '.claude' / 'skills' / 'android-design' / 'SKILL.md').read_text())


class LoadsSkillTest(unittest.TestCase):
    def test_skill_tool_with_or_without_a_plugin_prefix(self):
        self.assertTrue(trigger_eval.loads_skill({'name': 'Skill', 'input': {'skill': 'android-design'}}, 'android-design'))
        self.assertTrue(trigger_eval.loads_skill({'name': 'Skill', 'input': {'skill': 'x:android-design'}}, 'android-design'))

    def test_a_sibling_skill_does_not_count(self):
        call = {'name': 'Skill', 'input': {'skill': 'android-design-explore'}}
        self.assertFalse(trigger_eval.loads_skill(call, 'android-design'))

    def test_reading_the_skill_file_counts(self):
        call = {'name': 'Read', 'input': {'file_path': '/tmp/s/.claude/skills/android-design/SKILL.md'}}
        self.assertTrue(trigger_eval.loads_skill(call, 'android-design'))
        other = {'name': 'Read', 'input': {'file_path': '/tmp/s/.claude/skills/android-design-explore/SKILL.md'}}
        self.assertFalse(trigger_eval.loads_skill(other, 'android-design'))


class PassesTest(unittest.TestCase):
    def test_two_of_three_for_positives_and_at_most_one_for_near_misses(self):
        self.assertTrue(trigger_eval.passes(True, 2, 3))
        self.assertFalse(trigger_eval.passes(True, 1, 3))
        self.assertTrue(trigger_eval.passes(False, 1, 3))
        self.assertFalse(trigger_eval.passes(False, 2, 3))


if __name__ == '__main__':
    unittest.main()
