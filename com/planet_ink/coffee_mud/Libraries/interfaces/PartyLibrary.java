package com.planet_ink.coffee_mud.Libraries.interfaces;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.party.PartyStore.PartySnapshot;

/** Player party membership shared by sessions during one server run. */
public interface PartyLibrary extends CMLibrary
{
	public enum CreateResult
	{
		CREATED, ALREADY_MEMBER, NOT_PLAYER, IN_COMBAT
	}

	/** Recognizes player characters even when they have no active session. */
	public boolean isPlayerCharacter(MOB character);

	/** Creates a party with this character as its leader and only member. */
	public CreateResult createParty(MOB character);

	/** Returns a read-only roster, or null when the character has no party. */
	public PartySnapshot findParty(MOB character);

	/** Leaves the party, returning false if the character had no membership. */
	public boolean leaveParty(MOB character);
}
