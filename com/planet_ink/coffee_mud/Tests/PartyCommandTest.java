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
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.PartyLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.party.PartyStore.PartySnapshot;

/** Exercises command input with isolated party state and recording player objects. */
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
		return new String[] {"all", "party"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		try
		{
			runCases();
			return null;
		}
		catch(final InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return "Party command test interrupted.";
		}
		catch(final AssertionError | Exception e)
		{
			return e.toString();
		}
	}

	public static void main(final String[] args) throws Exception
	{
		final FutureTask<Void> test = new FutureTask<Void>(() -> {
			CMProps.instance();
			CMLib.initialize();
			final LanguageLibrary language = (LanguageLibrary)Proxy.newProxyInstance(
				LanguageLibrary.class.getClassLoader(), new Class<?>[] {LanguageLibrary.class}, (proxy, method, values) -> {
					if(method.getName().equals("commandWordTranslation"))
						return values[0];
					if(method.getName().equals("fullSessionTranslation"))
						return CMStrings.replaceVariables((String)values[1], (String[])values[2]);
					if(method.getName().equals("L"))
					{
						final int textIndex = method.getParameterTypes()[0] == Class.class ? 1 : 0;
						return CMStrings.replaceVariables((String)values[textIndex], (String[])values[textIndex + 1]);
					}
					throw new UnsupportedOperationException(method.getName());
				});
			CMLib.registerLibrary(language);
			runCases();
			return null;
		});
		final Thread worker = new Thread(new ThreadGroup("0-PartyCommandTest"), test, "PartyCommandTest");
		worker.setDaemon(true);
		worker.start();
		test.get(10, TimeUnit.SECONDS);
		System.out.println("PASS: Party command fixture.");
	}

	private static Party command(final PartyLibrary service)
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

	private static void runCases() throws Exception
	{
		final CMParties service = new CMParties();
		final Party firstCommand = command(service);
		final Party secondCommand = command(service);
		final Actor alice = new Actor("Alice", true);
		final Actor bob = new Actor("Bob", true);
		try
		{
			firstCommand.execute(alice.mob, new ArrayList<String>(Arrays.asList("PARTY")), 0);
			check(service.findParty(alice.mob) == null, "Inspecting an empty party must preserve membership.");
			check(alice.messages.contains(firstCommand.L("You are not in a party. Use PARTY CREATE to start one.")),
				"A player without a party should receive creation guidance.");
			firstCommand.execute(alice.mob, new ArrayList<String>(Arrays.asList("PARTY", "CREATE")), 0);
			final PartySnapshot original = service.findParty(alice.mob);
			check(original != null, "PARTY CREATE should create the actor's party.");
			check("Alice".equals(original.getLeader()), "The actor should lead the created party.");
			secondCommand.execute(alice.mob, new ArrayList<String>(Arrays.asList("PARTY", "CREATE")), 0);
			check(service.findParty(alice.mob).getId() == original.getId(), "A second command instance must find existing membership.");
			check(alice.messages.contains(firstCommand.L("You are already in a party. Use PARTY LEAVE before creating another.")),
				"Duplicate creation should explain existing membership.");
			secondCommand.execute(bob.mob, new ArrayList<String>(Arrays.asList("PARTY", "CREATE")), 0);
			final PartySnapshot bobParty = service.findParty(bob.mob);
			check(bobParty != null && bobParty.getId() != original.getId(), "Different actors should create separate parties.");
			alice.messages.clear();
			secondCommand.execute(alice.mob, new ArrayList<String>(Arrays.asList("PARTY")), 0);
			check(alice.messages.contains(firstCommand.L("Your party leader is @x1.\n\rMembers are @x2.", "Alice", "Alice")),
				"The roster should identify Alice as leader and member of her own party.");
			final Actor reconnected = new Actor("alice", true);
			check(service.findParty(reconnected.mob).getId() == original.getId(), "A new actor object should find canonical character membership.");
			firstCommand.execute(alice.mob, new ArrayList<String>(Arrays.asList("PARTY", "CREATE", "Bob")), 0);
			firstCommand.execute(alice.mob, new ArrayList<String>(Arrays.asList("PARTY", "UNKNOWN")), 0);
			check(service.findParty(alice.mob).getId() == original.getId(), "Invalid commands must preserve membership.");
			final Actor npc = new Actor("Guard", false);
			firstCommand.execute(npc.mob, new ArrayList<String>(Arrays.asList("PARTY", "CREATE")), 0);
			check(service.findParty(npc.mob) == null, "An NPC must not gain party membership.");
			final Actor fighter = new Actor("Carol", true);
			fighter.inCombat = true;
			firstCommand.execute(fighter.mob, new ArrayList<String>(Arrays.asList("PARTY", "CREATE")), 0);
			check(service.findParty(fighter.mob) == null, "An active fighter must not create a party.");
			check(fighter.messages.contains(firstCommand.L("Wait until combat ends before creating a party.")),
				"A fighter should receive the combat restriction message.");
			alice.inCombat = true;
			firstCommand.execute(alice.mob, new ArrayList<String>(Arrays.asList("PARTY", "LEAVE")), 0);
			check(service.findParty(alice.mob) == null, "Leaving a party during combat should clear membership.");
			check(alice.inCombat, "Leaving a party must preserve combat participation.");
			firstCommand.execute(alice.mob, new ArrayList<String>(Arrays.asList("PARTY", "LEAVE")), 0);
			check(service.findParty(bob.mob).getId() == bobParty.getId(), "Alice leaving must preserve Bob's party.");
			check(!firstCommand.canBeOrdered(), "PARTY must not be orderable by another actor.");
		}
		finally
		{
			service.shutdown();
		}
		check(service.findParty(bob.mob) == null, "Service shutdown should clear server-run party state.");
	}

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
			stats = player ? (PlayerStats)Proxy.newProxyInstance(PlayerStats.class.getClassLoader(),
				new Class<?>[] {PlayerStats.class}, (proxy, method, args) -> { throw new UnsupportedOperationException(method.getName()); }) : null;
			mob = (MOB)Proxy.newProxyInstance(MOB.class.getClassLoader(), new Class<?>[] {MOB.class}, this);
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args)
		{
			switch(method.getName())
			{
			case "Name":
			case "name": return name;
			case "playerStats": return stats;
			case "isInCombat": return Boolean.valueOf(inCombat);
			case "tell": messages.add((String)args[0]); return null;
			default: throw new UnsupportedOperationException(method.getName());
			}
		}
	}

	private static void check(final boolean condition, final String message)
	{
		if(!condition)
			throw new AssertionError(message);
	}
}
