package com.planet_ink.coffee_mud.Commands;

import java.util.List;
import java.util.Locale;

import com.planet_ink.coffee_mud.Libraries.interfaces.PartyLibrary;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.party.PartyStore.PartySnapshot;

public class Party extends StdCommand
{
	private final String[] access = I(new String[] {"PARTY"});

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected PartyLibrary parties()
	{
		return CMLib.parties();
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
	{
		final PartyLibrary parties = parties();
		if(!parties.isPlayerCharacter(mob))
		{
			mob.tell(L("Only player characters can use parties."));
			return false;
		}
		if(commands.size() == 1)
		{
			final PartySnapshot party = parties.findParty(mob);
			if(party == null)
				mob.tell(L("You are not in a party. Use PARTY CREATE to start one."));
			else
				mob.tell(L("Your party leader is @x1.\n\rMembers are @x2.",
					party.getLeader(), CMParms.toListString(party.getMembers())));
			return false;
		}
		if(commands.size() != 2)
		{
			mob.tell(L("Use PARTY, PARTY CREATE, or PARTY LEAVE."));
			return false;
		}
		switch(commands.get(1).toUpperCase(Locale.ROOT))
		{
		case "CREATE":
			switch(parties.createParty(mob))
			{
			case CREATED:
				mob.tell(L("You created a party. You are its leader."));
				break;
			case ALREADY_MEMBER:
				mob.tell(L("You are already in a party. Use PARTY LEAVE before creating another."));
				break;
			case IN_COMBAT:
				mob.tell(L("Wait until combat ends before creating a party."));
				break;
			case NOT_PLAYER:
				mob.tell(L("Only player characters can use parties."));
				break;
			}
			break;
		case "LEAVE":
			if(parties.leaveParty(mob))
				mob.tell(L("You left the party."));
			else
				mob.tell(L("You are not in a party."));
			break;
		default:
			mob.tell(L("Use PARTY, PARTY CREATE, or PARTY LEAVE."));
			break;
		}
		return false;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> commands)
	{
		return 0.0;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> commands)
	{
		return 0.0;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}
}
