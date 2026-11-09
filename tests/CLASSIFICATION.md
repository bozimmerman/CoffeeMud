# Test execution classifications

Each concrete registered Java test has one `exec_` group in addition to its existing groups. Existing group membership and test bodies are preserved. `StdTest` and `PropTest` are support classes and remain unclassified.

These labels describe execution requirements. They are not runtime measurements or a guarantee that every group is ready to gate pull requests. The test workflow runs `exec_core`, `exec_integration`, `exec_randomized`, and `exec_stress` as separate jobs, applying the `ci_exclude` filter. The launcher's default selection remains `party`.

| Group | Purpose |
| --- | --- |
| `exec_core` | Utility and isolated membership regressions; candidates for short checks. The current host launcher still boots the engine. |
| `exec_integration` | Assertions using engine services, registered objects, room fixtures, commands, or JVM-dependent memory inspection. |
| `exec_randomized` | Generated inputs or repeated simulations; runtime and results may vary. Seeds are not currently recorded consistently. |
| `exec_stress` | Repeated concurrent operations; requires a longer deadline. |
| `exec_world` | Checks against existing world data. An empty world can provide little or no coverage. |
| `exec_benchmark` | Timing benchmarks, large exploratory reports, or expensive content analysis. |
| `exec_manual` | Interactive tools, reports without correctness assertions, demonstrations, or environment-specific utilities. |
| `exec_placeholder` | Concrete tests whose current body immediately returns success without assertions. |

## CI exclusion label

`ci_exclude` is an independent label for tests with known failures that should be excluded from CI. It does not replace their execution or feature groups.

| Test | Reason |
| --- | --- |
| `Spaceangles` | Offset case 5 compares approximately 360 degrees with 0 degrees without handling wraparound. |
| `Spaceinterception` | The shared interception solver rejects valid faster-pursuer scenarios. |
| `Spaceinterception2` | Encounters the same solver failure before the guided-torpedo simulation. |

The host runner filters `ci_exclude` tests from every group selection, ignoring case, and logs each matching excluded test as `CI EXCLUDE`. A group with no remaining tests fails rather than passing without coverage. Explicit `--test` selection still runs an excluded test for investigation. The in-game `TEST` command retains its existing behavior and does not apply this CI filter.

## Selecting a group

Use the existing exact group selector:

```text
python tests/run_tests.py --group exec_core --skip-standalone
python tests/run_tests.py --group exec_integration --skip-standalone
python tests/run_tests.py --group exec_stress --skip-standalone --test-timeout 180 --timeout 300
```

The same labels work in-game, for example `TEST exec_core` and `TEST LIST exec_core`. The launcher runs standalone party fixtures first unless `--skip-standalone` is supplied.

A label does not change assertion handling, provision missing world data, seed randomness, skip placeholders, or supply interactive input. In particular, manual and benchmark groups should be selected deliberately rather than added wholesale to CI. `exec_manual` includes `Yahoomsgrabber`, an importer that can write journal records when supplied arguments and its external archive; it is not an automated regression test.

`Cmuniqsortsvec` took about 125 seconds on a local JDK 17 run, exceeding the default 30-second test deadline. Other group runtimes have not yet been established. `Spaceinterception2` can print unsuccessful scenario counts without returning a failure; its classification does not repair that existing behavior.

## Inventory

### `exec_benchmark` (4)

`Spacemovereport1`, `Spacemovereport2`, `Statcreationspeed`, `Timsvalue`.

### `exec_core` (7)

`Cmparms`, `MiniJSONTest`, `MiniTSONTest`, `Parseany`, `PartyMembershipTest`, `Validmisctext`, `XMLManagerTest`.

### `exec_integration` (35)

`ClockConversion`, `FormationTest`, `LongSet`, `MonkeyGrip`, `PartyCommandTest`, `PropFightspellcast`, `PropHaveadjuster`, `PropHaveenabler`, `PropHaveresister`, `PropHavespellcast`, `PropHavezapper`, `PropHereadjuster`, `PropHerespellcast`, `PropHidden`, `PropReqalignments`, `PropReqcapacity`, `PropResistance`, `PropRideadjuster`, `PropRideenabler`, `PropRideresister`, `PropRidespellcast`, `PropRidezapper`, `PropSpelladder`, `PropUsespellcast`, `PropUsespellcast2`, `PropWearadjuster`, `PropWearenabler`, `PropWearresister`, `PropWearspellcast`, `PropWearzapper`, `Spaceangles`, `Spacecollisions`, `Spaceturns`, `Wearing`, `ZapperAnyclasslevel`.

### `exec_manual` (18)

`Citycheck`, `Dumpmobbitmaps`, `Escapefilterbug`, `Horsedraggers`, `Levelcharts`, `Matches`, `Metaflags`, `Notrandom`, `Numwords`, `Protocol`, `Racelangs`, `Racemixing`, `Raceweaps`, `Randomnames`, `Recover`, `Spacesectors`, `Spacesectorsmap`, `Yahoomsgrabber`.

### `exec_placeholder` (9)

`PropReqclasses`, `PropReqentry`, `PropReqheight`, `PropReqlevels`, `PropReqnomob`, `PropReqpkill`, `PropReqraces`, `PropReqstat`, `PropReqtattoo`.

### `exec_randomized` (6)

`DateMasks`, `Spacebasics`, `Spaceinterception`, `Spaceinterception2`, `Spacemoves`, `Spacemoves2`.

### `exec_stress` (1)

`Cmuniqsortsvec`.

### `exec_world` (2)

`AreaTrails`, `Spaceintersect`.

## Harness checks

`tests/test_launcher.py` and `tests/harness/HostTestRunnerContractTest.java` validate test infrastructure. They are separate from the registered `CMTest` groups and continue to run through their existing commands.
