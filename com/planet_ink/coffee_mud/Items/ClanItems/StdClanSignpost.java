package com.planet_ink.coffee_mud.Items.ClanItems;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2026-2026 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class StdClanSignpost extends StdClanItem
{
	@Override
	public String ID()
	{
		return "StdClanSignpost";
	}

	protected static final int	STR_TO_LOOSEN			= 30;
	protected static final long	GET_RESET_MILLIS		= TimeManager.MILI_MINUTE;

	protected int		counter			= STR_TO_LOOSEN;
	protected long		lastAttemptMs	= 0;

	public StdClanSignpost()
	{
		super();

		setName("a clan signpost");
		basePhyStats().setWeight(25);
		setDisplayText("a clan signpost is planted firmly in the ground here.");
		setDescription("");
		secretIdentity = "";
		baseGoldValue = 10;
		setClanItemType(ClanItem.ClanItemType.SIGNPOST);
		material = RawMaterial.RESOURCE_WOOD;
		basePhyStats().setSensesMask(basePhyStats().sensesMask() | PhyStats.SENSE_ITEMNOTGET);
		CMLib.flags().setReadable(this, true);
		recoverPhyStats();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this))
		&&((msg.targetMinor() == CMMsg.TYP_PUSH)
		   ||(msg.targetMinor() == CMMsg.TYP_PULL)))
		{
			msg.source().tell(L("You can't move @x1.", name(msg.source())));
			return false;
		}
		if(StdClanItem.stdOkMessage(this, msg))
		{
			if((msg.amITarget(this))
			&&((msg.targetMinor() == CMMsg.TYP_WRITE)
			   ||(msg.targetMinor() == CMMsg.TYP_REWRITE)))
			{
				final MOB mob = msg.source();
				if(clanID().length() > 0)
				{
					final Pair<Clan,Integer> role = mob.getClanRole(clanID());
					if((role == null)||(role.first==null))
					{
						mob.tell(L("Only members of @x1 may write on @x2.", clanID(), name(mob)));
						return false;
					}
					final Clan C = role.first;
					if((C.getAuthority(role.second.intValue(), Clan.Function.CLAN_BENEFITS) !=Authority.CAN_NOT_DO)
					||(C.getAuthority(role.second.intValue(), Clan.Function.ENCHANT) !=Authority.CAN_NOT_DO))
					{
						mob.tell(L("Only authorized members of @x1 may write on @x2.", clanID(), name(mob)));
						return false;
					}
				}
				return super.okMessage(myHost, msg);
			}
			if((msg.amITarget(this))
			&&(msg.targetMinor() == CMMsg.TYP_GET))
			{
				final MOB mob = msg.source();
				if(mob.isMonster())
				{
					final Room R = mob.location();
					if(R != null)
						R.show(mob, this, CMMsg.MSG_OK_ACTION, L("<S-NAME> attempt(s) to pull <T-NAME> free, but cannot."));
					return false;
				}
				final long now = System.currentTimeMillis();
				if((now - lastAttemptMs) > GET_RESET_MILLIS)
					counter = STR_TO_LOOSEN;
				counter -= mob.charStats().getStat(CharStats.STAT_STRENGTH);
				lastAttemptMs = now;
				if(counter <= 0)
				{
					final int savedSenses = basePhyStats().sensesMask();
					try
					{
						basePhyStats().setSensesMask(basePhyStats().sensesMask() & (~PhyStats.SENSE_ITEMNOTGET));
						phyStats().setSensesMask(phyStats().sensesMask() & (~PhyStats.SENSE_ITEMNOTGET));
						return super.okMessage(myHost, msg);
					}
					finally
					{
						basePhyStats().setSensesMask(savedSenses);
						recoverPhyStats();
					}
				}
				final Room R = mob.location();
				if(R != null)
					R.show(mob, this, CMMsg.MSG_OK_ACTION, L("<S-NAME> strain(s) against <T-NAME>, and it loosens slightly but holds fast."));
				return false;
			}
			return super.okMessage(myHost, msg);
		}
		return false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!StdClanItem.standardTick(this, tickID))
			return false;
		return super.tick(ticking, tickID);
	}
}
