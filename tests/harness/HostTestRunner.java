import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.planet_ink.coffee_mud.Areas.interfaces.Area;
import com.planet_ink.coffee_mud.Common.FakeSession;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Tests.interfaces.CMTest;
import com.planet_ink.coffee_mud.application.MUD;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerStats;

/** Runs registered in-game tests in a disposable host. Launched by run_tests.py. */
public final class HostTestRunner
{
	static final class Result
	{
		final String name;
		final String failure;
		final double seconds;
		final boolean timedOut;

		Result(final String name, final String failure, final long start, final boolean timedOut)
		{
			this.name = name;
			this.failure = failure;
			this.seconds = (System.nanoTime() - start) / 1_000_000_000.0;
			this.timedOut = timedOut;
		}
	}

	/** A recording session visible through StdMOB.session(), with no socket. */
	private static final class RecordingSession extends FakeSession
	{
		@Override
		public boolean isFake()
		{
			return false;
		}
	}

	private static final class Context implements AutoCloseable
	{
		final Area area = CMClass.getAreaType("StdArea");
		final Room room = CMClass.getLocale("StdRoom");
		final MOB actor = CMClass.getMOB("StdMOB");
		final RecordingSession session = new RecordingSession();

		Context()
		{
			area.setName("CITestArea");
			room.setRoomID("CITestArea#0");
			room.setArea(area);
			actor.setName("CITestRunner");
			actor.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
			actor.baseCharStats().setMyRace(CMClass.getRace("Human"));
			actor.baseCharStats().setCurrentClass(CMClass.getCharClass("Archon"));
			actor.baseCharStats().setCurrentClassLevel(100);
			actor.basePhyStats().setLevel(100);
			actor.recoverCharStats();
			actor.recoverPhyStats();
			actor.recoverMaxState();
			actor.resetToMaxState();
			actor.setStartRoom(room);
			actor.setLocation(room);
			room.addInhabitant(actor);
			session.initialize(null, "MEMORY");
			session.setMob(actor);
			actor.setSession(session);
		}

		@Override
		public void close()
		{
			actor.makePeace(true);
			actor.setSession(null);
			session.setMob(null);
			actor.destroy();
			room.destroy();
			area.destroy();
		}
	}

	static List<CMTest> selectTests(final Enumeration<CMTest> registered,
			final boolean byGroup, final String selector)
	{
		final List<CMTest> selected = new ArrayList<CMTest>();
		while(registered.hasMoreElements())
		{
			final CMTest test = registered.nextElement();
			if(!byGroup)
			{
				if(test.ID().equalsIgnoreCase(selector))
					selected.add(test);
			}
			else
			{
				boolean excluded = false;
				for(final String group : test.getTestGroups())
				{
					if(group.equalsIgnoreCase("ci_exclude"))
						excluded = true;
				}
				for(final String group : test.getTestGroups())
				{
					if(group.equalsIgnoreCase(selector))
					{
						if(excluded)
							System.out.println("CI EXCLUDE " + test.ID() + ": ci_exclude");
						else
							selected.add(test);
						break;
					}
				}
			}
		}
		if(selected.isEmpty())
			throw new IllegalArgumentException("No tests found for " + (byGroup ? "group " : "test ") + selector);
		Collections.sort(selected, Comparator.comparing(CMTest::ID));
		return selected;
	}

	static Result runOne(final CMTest test, final MOB actor, final String selector, final long timeoutMillis)
			throws InterruptedException
	{
		final long start = System.nanoTime();
		final ExecutorService worker = Executors.newSingleThreadExecutor(task -> {
			final Thread thread = new Thread(task, "CI-" + test.ID());
			thread.setDaemon(true);
			return thread;
		});
		final Future<String> pending = worker.submit(() -> {
			String failure = null;
			try
			{
				test.cleanupTest();
				failure = test.doTest(actor, 0, selector.toUpperCase(Locale.ROOT), new ArrayList<String>());
			}
			catch(final Throwable error)
			{
				error.printStackTrace();
				failure = error.toString();
			}
			finally
			{
				try
				{
					test.cleanupTest();
				}
				catch(final Throwable error)
				{
					error.printStackTrace();
					failure = (failure == null ? "" : failure + "; ") + "Cleanup failed: " + error;
				}
			}
			return failure;
		});
		try
		{
			return new Result(test.ID(), pending.get(timeoutMillis, TimeUnit.MILLISECONDS), start, false);
		}
		catch(final TimeoutException error)
		{
			return new Result(test.ID(), "Timed out after " + timeoutMillis + " ms", start, true);
		}
		catch(final ExecutionException error)
		{
			error.printStackTrace();
			return new Result(test.ID(), error.toString(), start, false);
		}
		finally
		{
			pending.cancel(true);
			worker.shutdownNow();
		}
	}

	static int exitCode(final List<Result> results)
	{
		if(results.isEmpty())
			return 1;
		for(final Result result : results)
		{
			if(result.failure != null)
				return 1;
		}
		return 0;
	}

	private static String xml(final String text)
	{
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&apos;");
	}

	private static void writeResults(final List<Result> results) throws Exception
	{
		int failures = 0;
		for(final Result result : results)
			if(result.failure != null)
				failures++;
		try(PrintWriter out = new PrintWriter(Files.newBufferedWriter(Paths.get("../results.xml"), StandardCharsets.UTF_8)))
		{
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<testsuite name=\"CoffeeMud host\" tests=\"" + results.size() + "\" failures=\"" + failures + "\">");
			for(final Result result : results)
			{
				out.println("  <testcase name=\"" + xml(result.name) + "\" time=\"" + result.seconds + "\">");
				if(result.failure != null)
					out.println("    <failure message=\"" + xml(result.failure) + "\"/>");
				out.println("  </testcase>");
			}
			out.println("</testsuite>");
			if(out.checkError())
				throw new IllegalStateException("Could not write results.xml");
		}
		System.out.println("CI SUMMARY tests=" + results.size() + " failures=" + failures);
	}

	private static void commandSmoke(final Context context, final String expectedTest)
	{
		if(!CMClass.getCommand("Test").securityCheck(context.actor))
			throw new AssertionError("The temporary test character must have TEST permission.");
		context.actor.doCommand(new ArrayList<String>(Arrays.asList("TEST", "LIST")), 0);
		if(!context.session.getAfkMessage().toLowerCase(Locale.ROOT).contains(expectedTest.toLowerCase(Locale.ROOT)))
			throw new AssertionError("TEST LIST did not discover " + expectedTest);
	}

	private static void runInHost(final String[] args)
	{
		final List<Result> results = new ArrayList<Result>();
		final long start = System.nanoTime();
		boolean ready = false;
		try
		{
			final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(Long.parseLong(args[4]));
			while(CMLib.hosts().isEmpty() || !CMLib.hosts().get(0).isAcceptingConnections())
			{
				if(System.nanoTime() >= deadline)
					throw new IllegalStateException("Server startup timed out");
				Thread.sleep(50);
			}
			ready = true;
			final List<CMTest> selected = selectTests(CMClass.tests(), args[0].equals("group"), args[1]);
			try(Context context = new Context())
			{
				commandSmoke(context, selected.get(0).ID());
			}
			results.add(new Result("HostCommandSmoke", null, start, false));
			for(final CMTest test : selected)
			{
				final Result result;
				final Context context = new Context();
				try
				{
					result = runOne(test, context.actor, args[1], Long.parseLong(args[3]) * 1000);
					results.add(result);
					System.out.println("CI " + (result.failure == null ? "PASS " : "FAIL ") + result.name
							+ (result.failure == null ? "" : ": " + result.failure));
					if(result.timedOut)
						break; // Stop the suite; the interrupted test may still own world state.
				}
				finally
				{
					// A timed-out test may still reference these objects. End the host instead.
					if(results.isEmpty() || !results.get(results.size() - 1).timedOut)
						context.close();
				}
			}
		}
		catch(final Throwable error)
		{
			error.printStackTrace();
			results.add(new Result("Harness", error.toString(), start, false));
		}
		try
		{
			writeResults(results); // Keep results even if shutdown subsequently stalls.
			if(ready)
				MUD.globalShutdown(null, true, null);
		}
		catch(final Throwable error)
		{
			error.printStackTrace();
			results.add(new Result("Shutdown", error.toString(), start, false));
			try
			{
				writeResults(results);
			}
			catch(final Exception reportError)
			{
				reportError.printStackTrace();
			}
		}
		System.exit(exitCode(results));
	}

	public static void main(final String[] args)
	{
		if(args.length != 5)
			throw new IllegalArgumentException("Expected selection kind, selector, port, test timeout, startup timeout");
		new Thread(new ThreadGroup("0-CI"), () -> runInHost(args), "CI-tests").start();
		MUD.main(new String[] {"CITestHost", "BOOT=coffeemud.ini", "PORT=" + args[2]});
	}
}
