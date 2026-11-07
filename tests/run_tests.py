#!/usr/bin/env python3
"""Compile and run CoffeeMud tests using a fresh, disposable server directory."""

import argparse
import os
from pathlib import Path
import re
import shutil
import socket
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET


REPO = Path(__file__).resolve().parents[1]


def positive_int(value):
    number = int(value)
    if number <= 0:
        raise argparse.ArgumentTypeError("must be positive")
    return number


def java_tool(name):
    suffix = ".exe" if os.name == "nt" else ""
    if os.environ.get("JAVA_HOME"):
        candidate = Path(os.environ["JAVA_HOME"]) / "bin" / (name + suffix)
        if candidate.is_file():
            return str(candidate)
        raise RuntimeError(f"JAVA_HOME has no {name}: {candidate}")
    found = shutil.which(name)
    if not found:
        raise RuntimeError(f"Install JDK 17 and put {name} on PATH, or set JAVA_HOME.")
    return found


def run_step(label, command, cwd, log, timeout, expected=0):
    print(label, flush=True)
    with log.open("wb") as output:
        try:
            result = subprocess.run(command, cwd=cwd, stdout=output,
                                    stderr=subprocess.STDOUT, timeout=timeout)
        except subprocess.TimeoutExpired as error:
            raise RuntimeError(f"{label} timed out after {timeout}s. See {log}") from error
    if result.returncode != expected:
        tail = log.read_text(encoding="utf-8", errors="replace").splitlines()[-35:]
        print("\n".join(tail), file=sys.stderr)
        raise RuntimeError(f"{label} exited with {result.returncode}, expected {expected}. See {log}")


def prepare_runtime(runtime):
    for directory in ("resources", "web", "guides", "lib"):
        shutil.copytree(REPO / directory, runtime / directory,
                        ignore=shutil.ignore_patterns("fakedb", "*.class", "__pycache__"))
    database = runtime / "resources" / "fakedb"
    database.mkdir()
    shutil.copyfile(REPO / "guides/database/fakedb.schema", database / "fakedb.schema")

    settings = {
        "MUD_NAME": "CITestHost", "BIND": "127.0.0.1",
        "RUNWEBSERVERS": "FALSE", "RUNSMTPSERVER": "FALSE",
        "RUNI3SERVER": "FALSE", "RUNGVSERVER": "FALSE", "RUNCM1SERVER": "FALSE",
        "DBCLASS": "com.planet_ink.fakedb.Driver", "DBSERVICE": "jdbc:fakedb:resources/fakedb",
        "DBUSER": "", "DBPASS": "", "LOGNAME": "ci",
    }
    ini = (REPO / "coffeemud.ini").read_text(encoding="utf-8")
    for key, value in settings.items():
        pattern = rf"^{key}=.*$"
        if re.search(pattern, ini, flags=re.MULTILINE):
            ini = re.sub(pattern, f"{key}={value}", ini, flags=re.MULTILINE)
        else:
            ini += f"\n{key}={value}\n"
    (runtime / "coffeemud.ini").write_text(ini, encoding="utf-8")


def compile_sources(runtime, run_dir):
    sources = sorted((REPO / "com").rglob("*.java"))
    sources += sorted((REPO / "tests/harness").glob("*.java"))
    source_list = run_dir / "sources.txt"
    source_list.write_text("\n".join(f'"{source.as_posix()}"' for source in sources), encoding="utf-8")
    classpath = os.pathsep.join(str(runtime / "lib" / jar) for jar in ("js.jar", "jzlib.jar"))
    run_step(f"Compiling {len(sources)} Java sources...",
             [java_tool("javac"), "-encoding", "UTF-8", "-source", "8", "-target", "8",
              "-cp", classpath, "-d", str(runtime), "@" + str(source_list)],
             REPO, run_dir / "compile.log", 180)
    return os.pathsep.join([str(runtime), classpath])


def check_report(report):
    if not report.is_file():
        raise RuntimeError("The host exited without results.xml. This is a failed run.")
    suite = ET.parse(report).getroot()
    cases = suite.findall("testcase")
    if not cases or int(suite.attrib["tests"]) != len(cases):
        raise RuntimeError("The host report is empty or inconsistent.")
    if int(suite.attrib.get("failures", "0")) or int(suite.attrib.get("errors", "0")):
        raise RuntimeError("The host report contains failures.")
    if any(case.find("failure") is not None or case.find("error") is not None for case in cases):
        raise RuntimeError("A test failed despite a successful host exit.")
    return len(cases)


def remove_runtime(runtime, run_dir):
    # Only delete the disposable directory created by this invocation.
    resolved = runtime.resolve()
    if resolved.parent != run_dir.resolve() or resolved.name != "runtime":
        raise RuntimeError("Unexpected runtime path. Cleanup stopped.")
    shutil.rmtree(resolved)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    selection = parser.add_mutually_exclusive_group()
    selection.add_argument("--group", help="registered test group (default: party)")
    selection.add_argument("--test", help="one registered test ID, matched exactly")
    parser.add_argument("--verify-harness", action="store_true", help="also check failure, cleanup, and timeout reporting")
    parser.add_argument("--keep-runtime", action="store_true", help="keep the disposable runtime after a successful run")
    parser.add_argument("--skip-standalone", action="store_true", help="run only the selected registered host tests")
    parser.add_argument("--timeout", type=positive_int, default=240, help="whole host process deadline in seconds")
    parser.add_argument("--test-timeout", type=positive_int, default=30, help="deadline per registered test in seconds")
    parser.add_argument("--startup-timeout", type=positive_int, default=120, help="host startup deadline in seconds")
    args = parser.parse_args()

    output = REPO / "test-output"
    output.mkdir(exist_ok=True)
    run_dir = Path(tempfile.mkdtemp(prefix="run-", dir=output))
    runtime = run_dir / "runtime"
    runtime.mkdir()
    print(f"Results and logs: {run_dir}", flush=True)
    try:
        prepare_runtime(runtime)
        classpath = compile_sources(runtime, run_dir)
        java = [java_tool("java"), "-Djava.awt.headless=true", "-Dfile.encoding=UTF-8",
                "-Xmx512m", "-cp", classpath]
        if args.verify_harness:
            run_step("Checking harness contracts...", java + ["HostTestRunnerContractTest"],
                     runtime, run_dir / "harness-contracts.log", 20)
            run_step("Checking that a deliberate failure exits with status 1...",
                     java + ["HostTestRunnerContractTest", "--fail"],
                     runtime, run_dir / "expected-failure.log", 20, expected=1)

        # Keep these quick checks useful before adding more world-dependent tests.
        fixtures = () if args.skip_standalone else ("PartyMembershipTest", "PartyCommandTest")
        for fixture in fixtures:
            run_step(f"Running {fixture} standalone...",
                     java + ["com.planet_ink.coffee_mud.Tests." + fixture],
                     runtime, run_dir / (fixture + ".log"), 30)

        with socket.socket() as reservation:
            reservation.bind(("127.0.0.1", 0))
            port = reservation.getsockname()[1]
        kind, selector = ("test", args.test) if args.test else ("group", args.group or "party")
        run_step(f"Running registered {kind} {selector} in a fresh host...",
                 java + ["HostTestRunner", kind, selector, str(port),
                         str(args.test_timeout), str(args.startup_timeout)],
                 runtime, run_dir / "host.log", args.timeout)
        count = check_report(run_dir / "results.xml")
        print(f"PASS: {count} host checks and {len(fixtures)} standalone fixtures.", flush=True)
        if not args.keep_runtime:
            remove_runtime(runtime, run_dir)
        return 0
    except (OSError, RuntimeError, ValueError, ET.ParseError) as error:
        print(f"FAIL: {error}\nDiagnostics retained in {run_dir}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
