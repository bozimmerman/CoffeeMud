package com.planet_ink.coffee_mud.core.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores party membership by stable character key, independent of a live session. */
public interface PartyStore
{
	/** Creates a party, or returns null if the character already belongs to one. */
	PartySnapshot create(String characterKey);

	/** Returns the current party view, or null if the character has no party. */
	PartySnapshot findByMember(String characterKey);

	/** Removes the character from their party. Returns false if they had no party. */
	boolean leave(String characterKey);

	final class PartySnapshot
	{
		private final long id;
		private final String leader;
		private final List<String> members;

		public PartySnapshot(final long id, final String leader, final List<String> members)
		{
			this.id = id;
			this.leader = leader;
			this.members = Collections.unmodifiableList(new ArrayList<String>(members));
		}

		public long getId()
		{
			return id;
		}

		public String getLeader()
		{
			return leader;
		}

		public List<String> getMembers()
		{
			return members;
		}
	}
}
