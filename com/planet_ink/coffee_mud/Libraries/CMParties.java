package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_mud.Libraries.interfaces.PartyLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.party.InMemoryPartyStore;
import com.planet_ink.coffee_mud.core.party.PartyStore;
import com.planet_ink.coffee_mud.core.party.PartyStore.PartySnapshot;

/** Adapts CoffeeMud player characters to the server-run party store. */
public class CMParties extends StdLibrary implements PartyLibrary
{
	private volatile PartyStore store = new InMemoryPartyStore();

	@Override
	public String ID()
	{
		return "CMParties";
	}

	@Override
	public boolean isPlayerCharacter(final MOB character)
	{
		return character != null && character.playerStats() != null;
	}

	private String characterKey(final MOB character)
	{
		return CMStrings.capitalizeAndLower(character.Name());
	}

	@Override
	public CreateResult createParty(final MOB character)
	{
		if(!isPlayerCharacter(character))
			return CreateResult.NOT_PLAYER;
		if(character.isInCombat())
			return CreateResult.IN_COMBAT;
		if(store.create(characterKey(character)) == null)
			return CreateResult.ALREADY_MEMBER;
		return CreateResult.CREATED;
	}

	@Override
	public PartySnapshot findParty(final MOB character)
	{
		if(!isPlayerCharacter(character))
			return null;
		return store.findByMember(characterKey(character));
	}

	@Override
	public boolean leaveParty(final MOB character)
	{
		return isPlayerCharacter(character) && store.leave(characterKey(character));
	}

	@Override
	public boolean shutdown()
	{
		store = new InMemoryPartyStore();
		return super.shutdown();
	}
}
