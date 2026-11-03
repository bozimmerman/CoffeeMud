package com.planet_ink.coffee_mud.Tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.party.InMemoryPartyStore;
import com.planet_ink.coffee_mud.core.party.PartyStore;
import com.planet_ink.coffee_mud.core.party.PartyStore.PartySnapshot;

public class PartyMembershipTest extends StdTest
{
	@Override
	public String ID()
	{
		return "PartyMembershipTest";
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
			return "Party membership test interrupted.";
		}
		catch(final AssertionError | Exception e)
		{
			return e.toString();
		}
	}

	public static void main(final String[] args) throws Exception
	{
		runCases();
		System.out.println("PASS: Party membership fixture.");
	}

	private static void runCases() throws Exception
	{
		createAndFindSeparateParties();
		leaveAndCreateAgain();
		freshStoreHasNoMembership();
		rosterViewIsImmutable();
		concurrentCreationKeepsOneParty();
	}

	private static void createAndFindSeparateParties()
	{
		final PartyStore store = new InMemoryPartyStore();
		check(store.findByMember("Alice") == null, "Alice should start without a party.");
		final PartySnapshot aliceParty = store.create("Alice");
		check(aliceParty != null, "Alice should be able to create a party.");
		check("Alice".equals(aliceParty.getLeader()), "The creator should be the leader.");
		check(aliceParty.getMembers().equals(Collections.singletonList("Alice")), "The creator should be the first member.");
		check(store.create("Alice") == null, "Duplicate creation should be rejected.");
		final PartySnapshot bobParty = store.create("Bob");
		check(bobParty != null && bobParty.getId() != aliceParty.getId(), "Bob should have a separate party.");
		// A new session can supply a different String instance for the same key.
		final PartySnapshot found = store.findByMember(new String("Alice"));
		check(found != null && found.getId() == aliceParty.getId(), "Lookup by the same key should find Alice's original party.");
	}

	private static void leaveAndCreateAgain()
	{
		final PartyStore store = new InMemoryPartyStore();
		final PartySnapshot first = store.create("Alice");
		final PartySnapshot other = store.create("Bob");
		check(first != null && other != null, "The fixture should create both parties.");
		check(store.leave("Alice"), "Alice should be able to leave.");
		check(store.findByMember("Alice") == null, "Leaving should clear Alice's membership.");
		check(!store.leave("Alice"), "A second leave should report no membership.");
		final PartySnapshot replacement = store.create("Alice");
		check(replacement != null && replacement.getId() != first.getId(), "A recreated party should have a new ID.");
		final PartySnapshot remaining = store.findByMember("Bob");
		check(remaining != null && remaining.getId() == other.getId(), "Alice leaving should preserve Bob's party.");
	}

	private static void freshStoreHasNoMembership()
	{
		final PartyStore oldStore = new InMemoryPartyStore();
		check(oldStore.create("Alice") != null, "The original store should contain a party.");
		final PartyStore freshStore = new InMemoryPartyStore();
		check(freshStore.findByMember("Alice") == null, "A fresh store should start with no memberships.");
	}

	private static void rosterViewIsImmutable()
	{
		final PartyStore store = new InMemoryPartyStore();
		final PartySnapshot party = store.create("Alice");
		check(party != null, "The fixture should create a party.");
		try
		{
			party.getMembers().set(0, "Bob");
			throw new AssertionError("The roster should reject edits by callers.");
		}
		catch(final UnsupportedOperationException expected)
		{
			check(party.getMembers().equals(Collections.singletonList("Alice")), "The roster should still contain Alice.");
		}
		final List<String> backingMembers = new ArrayList<String>(party.getMembers());
		final PartySnapshot snapshot = new PartySnapshot(party.getId(), party.getLeader(), backingMembers);
		backingMembers.clear();
		check(snapshot.getMembers().equals(party.getMembers()), "A snapshot should own its copy of the roster.");
	}

	private static void concurrentCreationKeepsOneParty() throws Exception
	{
		final PartyStore store = new InMemoryPartyStore();
		final ExecutorService workers = Executors.newFixedThreadPool(2);
		final CyclicBarrier start = new CyclicBarrier(2);
		final Callable<PartySnapshot> attempt = () -> {
			start.await(5, TimeUnit.SECONDS);
			return store.create("Alice");
		};
		try
		{
			final Future<PartySnapshot> first = workers.submit(attempt);
			final Future<PartySnapshot> second = workers.submit(attempt);
			final PartySnapshot firstResult = first.get(10, TimeUnit.SECONDS);
			final PartySnapshot secondResult = second.get(10, TimeUnit.SECONDS);
			check((firstResult == null) != (secondResult == null), "Exactly one concurrent creation should succeed.");
			final PartySnapshot winner = firstResult != null ? firstResult : secondResult;
			final PartySnapshot found = store.findByMember("Alice");
			check(found != null && found.getId() == winner.getId(), "Membership should point to the winning party.");
		}
		finally
		{
			workers.shutdownNow();
		}
	}

	private static void check(final boolean condition, final String message)
	{
		if(!condition)
			throw new AssertionError(message);
	}
}
