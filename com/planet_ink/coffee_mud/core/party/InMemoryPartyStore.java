package com.planet_ink.coffee_mud.core.party;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Server-run storage for the create, inspect, and leave membership slice. */
public final class InMemoryPartyStore implements PartyStore
{
	private final Map<String, PartySnapshot> partiesByMember = new HashMap<String, PartySnapshot>();
	private long nextPartyId = 1;

	@Override
	public synchronized PartySnapshot create(final String characterKey)
	{
		Objects.requireNonNull(characterKey);
		if(partiesByMember.containsKey(characterKey))
			return null;
		final PartySnapshot party = new PartySnapshot(nextPartyId++, characterKey, Collections.singletonList(characterKey));
		partiesByMember.put(characterKey, party);
		return party;
	}

	@Override
	public synchronized PartySnapshot findByMember(final String characterKey)
	{
		return partiesByMember.get(Objects.requireNonNull(characterKey));
	}

	@Override
	public synchronized boolean leave(final String characterKey)
	{
		return partiesByMember.remove(Objects.requireNonNull(characterKey)) != null;
	}
}
