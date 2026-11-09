package com.planet_ink.coffee_mud.Tests;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import com.planet_ink.coffee_mud.Commands.Party;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerStats;
import com.planet_ink.coffee_mud.Libraries.CMParties;
import com.planet_ink.coffee_mud.Libraries.DirtyLanguage;
import com.planet_ink.coffee_mud.Libraries.interfaces.PartyLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.party.PartyStore.PartySnapshot;

/** Exercises commands using fresh party state and players that record messages. */
public class PartyCommandTest extends StdTest
{
	@Override
	public String ID()
	{
		return "PartyCommandTest";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all", "party", "exec_integration"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		try
		{
			runCases();
			return null;
		}
		catch(final AssertionError | Exception e)
		{
			return e.toString();
		}
	}

	public static void main(final String[] args) throws Exception
	{
		runInStandaloneHost();
		System.out.println("PASS: Party command fixture.");
	}

	private static void runCases()
	{
		noPartyShowsCreationGuidance();
		createAndInspectParty();
		duplicateCreationPreservesSharedMembership();
		differentPlayersCreateSeparateParties();
		replacementPlayerFindsExistingParty();
		invalidCommandsPreserveMembership();
		npcCannotCreateParty();
		combatBlocksCreationButAllowsLeaving();
		shutdownClearsMembership();
	}

	private static void noPartyShowsCreationGuidance()
	{
		final CMParties service = new CMParties();
		final Party command = partyCommand(service);
		final Actor alice = new Actor("Alice", true);

		execute(command, alice, "PARTY");

		check(service.findParty(alice.mob) == null, "Inspection should preserve empty membership.");
		check(alice.messages.contains(command.L("You are not in a party. Use PARTY CREATE to start one.")),
			"A player without a party should receive creation guidance.");
	}

	private static void createAndInspectParty()
	{
		final CMParties service = new CMParties();
		final Party command = partyCommand(service);
		final Actor alice = new Actor("Alice", true);

		execute(command, alice, "PARTY", "CREATE");
		final PartySnapshot created = service.findParty(alice.mob);

		check(created != null, "PARTY CREATE should create the actor's party.");
		check("Alice".equals(created.getLeader()), "The creator should lead the party.");
		check(!command.canBeOrdered(), "Another actor should not be able to order PARTY commands.");

		alice.messages.clear();
		execute(command, alice, "PARTY");

		check(alice.messages.contains(command.L("Your party leader is @x1.\n\rMembers are @x2.", "Alice", "Alice")),
			"The roster should show Alice as leader and member.");
	}

	private static void duplicateCreationPreservesSharedMembership()
	{
		final CMParties service = new CMParties();
		final Party firstCommand = partyCommand(service);
		final Party secondCommand = partyCommand(service);
		final Actor alice = new Actor("Alice", true);

		execute(firstCommand, alice, "PARTY", "CREATE");
		final PartySnapshot original = service.findParty(alice.mob);
		execute(secondCommand, alice, "PARTY", "CREATE");

		check(original != null, "The first command should create a party.");
		check(service.findParty(alice.mob).getId() == original.getId(),
			"Both command instances should use the same membership.");
		check(alice.messages.contains(firstCommand.L("You are already in a party. Use PARTY LEAVE before creating another.")),
			"Duplicate creation should explain the existing membership.");
	}

	private static void differentPlayersCreateSeparateParties()
	{
		final CMParties service = new CMParties();
		final Party command = partyCommand(service);
		final Actor alice = new Actor("Alice", true);
		final Actor bob = new Actor("Bob", true);

		execute(command, alice, "PARTY", "CREATE");
		execute(command, bob, "PARTY", "CREATE");
		final PartySnapshot aliceParty = service.findParty(alice.mob);
		final PartySnapshot bobParty = service.findParty(bob.mob);

		check(aliceParty != null && bobParty != null, "Both players should have a party.");
		check(aliceParty.getId() != bobParty.getId(), "Players should create separate parties.");
	}

	private static void replacementPlayerFindsExistingParty()
	{
		final CMParties service = new CMParties();
		final Party command = partyCommand(service);
		final Actor originalPlayer = new Actor("Alice", true);
		final Actor replacementPlayer = new Actor("alice", true);

		execute(command, originalPlayer, "PARTY", "CREATE");
		final PartySnapshot original = service.findParty(originalPlayer.mob);
		final PartySnapshot found = service.findParty(replacementPlayer.mob);

		check(original != null && found != null, "Both player objects should find membership.");
		check(found.getId() == original.getId(), "The canonical character name should find the same party.");
	}

	private static void invalidCommandsPreserveMembership()
	{
		final CMParties service = new CMParties();
		final Party command = partyCommand(service);
		final Actor alice = new Actor("Alice", true);

		execute(command, alice, "PARTY", "CREATE");
		final PartySnapshot original = service.findParty(alice.mob);
		execute(command, alice, "PARTY", "CREATE", "Bob");
		execute(command, alice, "PARTY", "UNKNOWN");

		check(original != null, "The fixture should create Alice's party.");
		check(service.findParty(alice.mob).getId() == original.getId(),
			"Invalid commands should preserve membership.");
	}

	private static void npcCannotCreateParty()
	{
		final CMParties service = new CMParties();
		final Party command = partyCommand(service);
		final Actor guard = new Actor("Guard", false);

		execute(command, guard, "PARTY", "CREATE");

		check(service.findParty(guard.mob) == null, "An NPC should not gain party membership.");
	}

	private static void combatBlocksCreationButAllowsLeaving()
	{
		final CMParties service = new CMParties();
		final Party command = partyCommand(service);
		final Actor alice = new Actor("Alice", true);
		final Actor bob = new Actor("Bob", true);
		final Actor carol = new Actor("Carol", true);

		execute(command, alice, "PARTY", "CREATE");
		execute(command, bob, "PARTY", "CREATE");
		final PartySnapshot bobParty = service.findParty(bob.mob);
		carol.inCombat = true;
		execute(command, carol, "PARTY", "CREATE");

		check(service.findParty(carol.mob) == null, "A fighter should not create a party.");
		check(carol.messages.contains(command.L("Wait until combat ends before creating a party.")),
			"A fighter should receive the combat restriction message.");

		alice.inCombat = true;
		execute(command, alice, "PARTY", "LEAVE");
		execute(command, alice, "PARTY", "LEAVE");

		check(service.findParty(alice.mob) == null, "Leaving should clear Alice's membership.");
		check(alice.inCombat, "Leaving a party should preserve combat participation.");
		check(bobParty != null && service.findParty(bob.mob).getId() == bobParty.getId(),
			"Alice leaving should preserve Bob's party.");
	}

	private static void shutdownClearsMembership()
	{
		final CMParties service = new CMParties();
		final Party command = partyCommand(service);
		final Actor alice = new Actor("Alice", true);

		execute(command, alice, "PARTY", "CREATE");
		check(service.findParty(alice.mob) != null, "The fixture should create a party before shutdown.");
		service.shutdown();

		check(service.findParty(alice.mob) == null, "Shutdown should clear server-run membership.");
	}

	// Each command uses this case's service instead of the running server's party state.
	private static Party partyCommand(final PartyLibrary service)
	{
		return new Party()
		{
			@Override
			protected PartyLibrary parties()
			{
				return service;
			}
		};
	}

	private static void execute(final Party command, final Actor actor, final String... words)
	{
		command.execute(actor.mob, new ArrayList<String>(Arrays.asList(words)), 0);
	}

	/*
	 * Eclipse starts main without a CoffeeMud host. Commands still need a language
	 * service for their messages. CoffeeMud identifies host libraries by the first
	 * character of the thread group's name, with '0' representing the main host.
	 * TEST PARTY already runs inside a host and calls runCases directly.
	 */
	private static void runInStandaloneHost() throws Exception
	{
		final FutureTask<Void> tests = new FutureTask<Void>(() -> {
			CMProps.instance();
			CMLib.initialize();
			CMLib.registerLibrary(new TestLanguage());
			runCases();
			return null;
		});

		final Thread worker = new Thread(new ThreadGroup("0-PartyCommandTest"), tests, "PartyCommandTest");
		worker.setDaemon(true);
		worker.start();
		tests.get(10, TimeUnit.SECONDS);
	}

	/** Supplies plain messages and substitutes values such as @x1 in those messages. */
	// CoffeeMud's library registry accepts public implementation classes.
	public static final class TestLanguage extends DirtyLanguage
	{
		@Override
		public String commandWordTranslation(final String word)
		{
			return word;
		}

		@Override
		public String fullSessionTranslation(final Class<?> owner, final String text, final String... values)
		{
			return CMStrings.replaceVariables(text, values);
		}

		@Override
		public String L(final String text, final String... values)
		{
			return CMStrings.replaceVariables(text, values);
		}

		@Override
		public String L(final Class<?> owner, final String text, final String... values)
		{
			return CMStrings.replaceVariables(text, values);
		}
	}

	/** Records the small part of a player or NPC that the PARTY command uses. */
	private static final class Actor implements InvocationHandler
	{
		private final String name;
		private final PlayerStats stats;
		private final List<String> messages = new ArrayList<String>();
		private boolean inCombat;
		private final MOB mob;

		private Actor(final String name, final boolean player)
		{
			this.name = name;
			if(player)
			{
				stats = (PlayerStats)Proxy.newProxyInstance(PlayerStats.class.getClassLoader(),
					new Class<?>[] {PlayerStats.class}, (proxy, method, args) -> {
						throw new UnsupportedOperationException(method.getName());
					});
			}
			else
				stats = null;

			mob = (MOB)Proxy.newProxyInstance(MOB.class.getClassLoader(), new Class<?>[] {MOB.class}, this);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args)
		{
			switch(method.getName())
			{
			case "Name":
			case "name":
				return name;
			case "playerStats":
				return stats;
			case "isInCombat":
				return Boolean.valueOf(inCombat);
			case "tell":
				messages.add((String)args[0]);
				return null;
			default:
				throw new UnsupportedOperationException(method.getName());
			}
		}
	}

	private static void check(final boolean condition, final String message)
	{
		if(!condition)
			throw new AssertionError(message);
	}
}
