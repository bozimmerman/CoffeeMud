import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Tests.StdTest;
import com.planet_ink.coffee_mud.Tests.interfaces.CMTest;

/** Checks CI verdicts and cleanup without booting a server. */
public final class HostTestRunnerContractTest
{
	private static final class Example extends StdTest
	{
		final Callable<String> action;
		final CountDownLatch cleaned = new CountDownLatch(1);
		int cleanupCount;
		boolean failCleanup;
		String[] groups = {"example"};

		Example(final Callable<String> action)
		{
			this.action = action;
		}

		@Override
		public String[] getTestGroups()
		{
			return groups;
		}

		@Override
		public String doTest(final MOB mob, final int flags, final String what, final List<String> commands)
		{
			try
			{
				return action.call();
			}
			catch(final Exception error)
			{
				throw new IllegalStateException(error);
			}
		}

		@Override
		public void cleanupTest()
		{
			if(++cleanupCount == 2)
			{
				cleaned.countDown();
				if(failCleanup)
					throw new AssertionError("cleanup failure");
			}
		}
	}

	private static void check(final boolean condition, final String message)
	{
		if(!condition)
			throw new AssertionError(message);
	}

	public static void main(final String[] args) throws Exception
	{
		if(args.length == 1 && args[0].equals("--fail"))
		{
			final HostTestRunner.Result failure = HostTestRunner.runOne(new Example(() -> "deliberate failure"), null, "example", 1000);
			System.exit(HostTestRunner.exitCode(Collections.singletonList(failure)));
		}

		final Example passing = new Example(() -> null);
		final HostTestRunner.Result pass = HostTestRunner.runOne(passing, null, "example", 1000);
		check(pass.failure == null && passing.cleanupCount == 2, "Passing tests must clean up before and after.");
		check(HostTestRunner.exitCode(Collections.singletonList(pass)) == 0, "A passing suite must exit successfully.");
		check(HostTestRunner.exitCode(Collections.emptyList()) != 0, "An empty suite must fail.");

		final HostTestRunner.Result failed = HostTestRunner.runOne(new Example(() -> "expected failure"), null, "example", 1000);
		check("expected failure".equals(failed.failure), "Preserve returned failure messages.");
		check(HostTestRunner.exitCode(Arrays.asList(pass, failed)) != 0, "One failure must fail the suite.");

		final Example throwing = new Example(() -> { throw new AssertionError("assertion failure"); });
		check(HostTestRunner.runOne(throwing, null, "example", 1000).failure.contains("assertion failure"), "Assertions must fail.");
		check(throwing.cleanupCount == 2, "A throwing test must still clean up.");

		final Example dirty = new Example(() -> null);
		dirty.failCleanup = true;
		check(HostTestRunner.runOne(dirty, null, "example", 1000).failure.contains("Cleanup failed"), "Cleanup errors must fail.");

		final Example blocked = new Example(() -> { new CountDownLatch(1).await(); return null; });
		check(HostTestRunner.runOne(blocked, null, "example", 100).timedOut, "A blocked test must time out.");
		check(blocked.cleaned.await(1, TimeUnit.SECONDS), "A cancelled cooperative test must clean up.");

		final List<CMTest> registered = Collections.singletonList(passing);
		check(HostTestRunner.selectTests(Collections.enumeration(registered), true, "EXAMPLE").size() == 1, "Group lookup ignores case.");
		final Example excluded = new Example(() -> null);
		excluded.groups = new String[] {"example", "CI_EXCLUDE"};
		final List<CMTest> mixed = Arrays.asList(excluded, passing);
		final List<CMTest> filtered = HostTestRunner.selectTests(Collections.enumeration(mixed), true, "example");
		check(filtered.size() == 1 && filtered.get(0) == passing, "Group selection must omit ci_exclude tests, ignoring case.");
		check(HostTestRunner.selectTests(Collections.enumeration(Collections.<CMTest>singletonList(excluded)), false, excluded.ID()).get(0) == excluded,
				"Explicit test selection must allow investigating excluded tests.");
		try
		{
			HostTestRunner.selectTests(Collections.enumeration(Collections.<CMTest>singletonList(excluded)), true, "example");
			throw new AssertionError("An entirely excluded group must fail rather than pass without tests.");
		}
		catch(final IllegalArgumentException expected)
		{
			check(expected.getMessage().contains("No tests found"), "Report an empty filtered group clearly.");
		}
		try
		{
			HostTestRunner.selectTests(Collections.enumeration(registered), true, "missing");
			throw new AssertionError("An unknown group must fail.");
		}
		catch(final IllegalArgumentException expected)
		{
			check(expected.getMessage().contains("No tests found"), "Report an unknown group clearly.");
		}
		System.out.println("PASS: Host runner contracts (including expected failures and timeout).");
	}
}
