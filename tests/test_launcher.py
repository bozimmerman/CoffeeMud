"""Regression checks for report validation and database isolation."""

import importlib.util
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch


spec = importlib.util.spec_from_file_location("run_tests", Path(__file__).with_name("run_tests.py"))
launcher = importlib.util.module_from_spec(spec)
spec.loader.exec_module(launcher)


class LauncherTest(unittest.TestCase):
    def directory(self):
        output = launcher.REPO / "test-output"
        output.mkdir(exist_ok=True)
        return tempfile.TemporaryDirectory(prefix="launcher-", dir=output)

    def report(self, text):
        with self.directory() as directory:
            path = Path(directory) / "results.xml"
            path.write_text(text, encoding="utf-8")
            return launcher.check_report(path)

    def test_valid_report(self):
        self.assertEqual(1, self.report('<testsuite tests="1" failures="0"><testcase name="ok"/></testsuite>'))

    def test_empty_or_missing_report_fails(self):
        with self.directory() as directory:
            with self.assertRaises(RuntimeError):
                launcher.check_report(Path(directory) / "missing.xml")
        with self.assertRaises(RuntimeError):
            self.report('<testsuite tests="0"/>')

    def test_inconsistent_count_fails(self):
        with self.assertRaises(RuntimeError):
            self.report('<testsuite tests="2"><testcase name="only one"/></testsuite>')

    def test_failure_cannot_be_hidden_by_summary(self):
        with self.assertRaises(RuntimeError):
            self.report('<testsuite tests="1" failures="0"><testcase><failure message="broken"/></testcase></testsuite>')
        with self.assertRaises(RuntimeError):
            self.report('<testsuite tests="1" errors="1"><testcase/></testsuite>')

    def test_runtime_never_copies_existing_database(self):
        with self.directory() as directory:
            source = Path(directory) / "source"
            runtime = Path(directory) / "runtime"
            for folder in ("resources/fakedb", "web", "guides/database", "lib"):
                (source / folder).mkdir(parents=True)
            (source / "resources/fakedb/players").write_text("existing players", encoding="utf-8")
            (source / "guides/database/fakedb.schema").write_text("test schema", encoding="utf-8")
            (source / "coffeemud.ini").write_text("DBCLASS=other\nDBSERVICE=production\n", encoding="utf-8")
            with patch.object(launcher, "REPO", source):
                launcher.prepare_runtime(runtime)
            self.assertEqual(["fakedb.schema"], [p.name for p in (runtime / "resources/fakedb").iterdir()])
            config = (runtime / "coffeemud.ini").read_text(encoding="utf-8")
            self.assertIn("DBSERVICE=jdbc:fakedb:resources/fakedb\n", config)
            self.assertIn("BIND=127.0.0.1\n", config)
            self.assertNotIn("production", config)
            self.assertEqual("existing players", (source / "resources/fakedb/players").read_text(encoding="utf-8"))

    def test_cleanup_rejects_source_directory(self):
        with self.directory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            with self.assertRaises(RuntimeError):
                launcher.remove_runtime(source, root)
            self.assertTrue(source.exists())

    def test_process_failure_is_not_success(self):
        with self.directory() as directory:
            path = Path(directory)
            with self.assertRaisesRegex(RuntimeError, "exited with 3"):
                launcher.run_step("expected failure", [sys.executable, "-c", "raise SystemExit(3)"],
                                  path, path / "failure.log", 5)

    def test_process_timeout_fails(self):
        with self.directory() as directory:
            path = Path(directory)
            with self.assertRaisesRegex(RuntimeError, "timed out"):
                launcher.run_step("expected timeout", [sys.executable, "-c", "import time; time.sleep(60)"],
                                  path, path / "timeout.log", 0.1)


if __name__ == "__main__":
    unittest.main()
