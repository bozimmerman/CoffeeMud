# Automated CoffeeMud tests

From the repository root, with JDK 17 and Python 3.9 or newer installed:

```text
python tests/run_tests.py --verify-harness
```

No Python packages or Ant installation are needed. Set `JAVA_HOME` to your JDK 17 directory, or put its `java` and `javac` on PATH.

The launcher compiles fresh classes with the project's Java 8 source/target settings. It runs `PartyMembershipTest` and `PartyCommandTest` standalone, then starts CoffeeMud and runs the registered `party` test group. It also checks that a temporary Archon character can discover those tests through the real `TEST LIST` command.

The host runner calls the same `CMTest.doTest` methods as the in-game `TEST` command. A null result passes. A failure message, exception, cleanup failure, or timeout fails the run. The command's boolean return value is not a test verdict, so the runner collects the test results directly.

## What the fixture creates

Each invocation gets a new directory under ignored `test-output/`:

- A fresh FakeDB containing only the supplied schema before boot. Existing local player records are excluded.
- Copies of the engine resources and configuration with optional network services disabled.
- A loopback listener on an available port. This is separate from the local demo server.
- A temporary room, player character, and recording session for each host test. These are created through Java and are not saved as player accounts.

CoffeeMud creates its default empty-database world during boot. Tests needing additional rooms, players, or content should create and clean up their own fixtures. Start with a known test group; legacy `all` includes tests with other setup requirements.

Results and logs are retained in that run's directory. Successful runs remove their disposable runtime directory. Failed runs retain it for debugging. Use `--keep-runtime` to retain a successful runtime too.

```text
python tests/run_tests.py --group party
python tests/run_tests.py --test PartyCommandTest
python tests/run_tests.py --test FormationTest --keep-runtime
```

Group names and test IDs are matched exactly, ignoring case. An unknown or empty selection fails. The two fast party fixtures run before any selected host suite.

Host group selections omit tests tagged `ci_exclude` and log the exclusions. A selection with no remaining tests fails. Use `--test <ID>` to investigate an excluded test explicitly; the in-game `TEST` command is unaffected.

Tests also have `exec_` groups describing their execution requirements, alongside the existing feature groups. See [execution classifications and the complete inventory](CLASSIFICATION.md) for the categories, limitations, and selection examples. These labels do not change the default CI selection.

Use `--skip-standalone` to run only the registered host selection. This lets another epic reuse the host harness without depending on the standalone party fixtures.

## Failure handling

`--verify-harness` checks pass/fail aggregation, exception handling, cleanup, cancellation, and empty selections. It also launches a deliberate failing test in a separate process and requires exit status 1. Expected exception traces in `harness-contracts.log` come from these negative checks.

Each registered test has a 30-second default deadline. Host startup has a 120-second deadline, and the entire host process has a 240-second deadline. Override these with `--test-timeout`, `--startup-timeout`, and `--timeout`. A timed-out test stops the suite because it may still hold world state. The outer launcher kills the Java process if it exceeds the whole-process deadline.

The host runner writes `results.xml` in JUnit format. The launcher requires a nonempty, consistent report as well as a successful process exit. Startup failure or a missing report cannot produce a green result.

Run the launcher's own regression checks with:

```text
python -m unittest discover -s tests -p test_launcher.py -v
```

## GitHub Actions

### Local equivalents of the CI jobs

With JDK 17 selected, run these as independent jobs. Each host invocation compiles fresh classes and creates its own disposable runtime and result directory. Core also runs the harness contracts and standalone party fixtures.

```text
python -m unittest discover -s tests -p test_launcher.py -v
python tests/run_tests.py --group exec_core --verify-harness
python tests/run_tests.py --group exec_integration --skip-standalone
python tests/run_tests.py --group exec_randomized --skip-standalone --test-timeout 180 --timeout 900
python tests/run_tests.py --group exec_stress --skip-standalone --test-timeout 180 --timeout 300
```

The stress test needs more than the default 30-second per-test deadline. Group runs filter `ci_exclude`; this job selection does not include world, benchmark, manual, or placeholder groups.

`test-coffeemud.yaml` runs a launcher-check job and four independent matrix jobs for core, integration, randomized, and stress checks on pushes, pull requests, and manual runs. All jobs use Windows and Python 3.13; the Java suites use JDK 17. Core also verifies the Java harness contracts and standalone party fixtures. Matrix fail-fast is disabled so a failure in one suite does not cancel the others. Each suite uploads its own logs and XML results even after a failed test step, retained for seven days. This workflow is separate from the existing distribution/release workflow.

To add coverage, write a `StdTest` with a `getTestGroups()` category and add your assertions to `doTest`. Existing tests remain available through `TEST <group>` in-game. Keep socket login, reconnect, and client rendering checks separate from this programmatic test context.
