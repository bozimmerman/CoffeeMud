package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.BuildingSkill.Flag;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaterialLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class ClanShipwrighting extends Shipwright
{
	@Override
	public String ID()
	{
		return "ClanShipwrighting";
	}

	private final static String	localizedName	= CMLib.lang().L("Clan Ship Building");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CLANSHIPBUILD", "CLANSHIPBUILDING", "CLANSHIPWRIGHT","CSHIPBUILD", "CSHIPBUILDING", "CSHIPWRIGHT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected void doShipTransfer(final BoardableShip buildingI, final MOB buyer)
	{
		final MOB shopKeeper = CMClass.getMOB("StdShopkeeper");
		try
		{
			((ShopKeeper)shopKeeper).setWhatIsSoldMask(ShopKeeper.DEAL_CSHIPSELLER);
			final CMMsg msg=CMClass.getMsg(buyer,buildingI,shopKeeper,CMMsg.MSG_GET,null);
			buildingI.executeMsg(buyer, msg);
		}
		finally
		{
			shopKeeper.destroy();
		}
	}

	@Override
	protected boolean canMend(final MOB mob, final Environmental E, final boolean quiet)
	{
		if(!super.canMend(mob,E,quiet))
			return false;
		if(E instanceof PrivateProperty)
		{
			final PrivateProperty P=(PrivateProperty)E;
			if(P.getOwnerName().length()>0)
			{
				if(mob!=null)
				{
					final Pair<Clan,Integer> role = mob.getClanRole(P.getOwnerName());
					if(role == null)
					{
						if(!quiet)
							commonTell(mob,L("You aren't authorized to do that."));
						return false;
					}
				}
				else
				{
					final Clan C=CMLib.clans().fetchClanAnyHost(P.getOwnerName());
					if(C==null)
						return false;
				}
			}
		}
		return true;
	}

	@Override
	protected String getIdentifierCommandWord()
	{
		return "clanshipwright";
	}

	@Override
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto,
								 final int asLevel, final int autoGenerate, final boolean forceLevels, final List<Item> crafted)
	{
		if((mob != null)&&(!auto))
		{
			Clan foundC=null;
			for(final Pair<Clan,Integer> pairC : mob.clans())
			{
				if(pairC.first.getAuthority(pairC.second.intValue(), Clan.Function.PROPERTY_OWNER) != Clan.Authority.CAN_NOT_DO)
					foundC=pairC.first;
			}
			if(foundC==null)
			{
				commonTell(mob,L("You aren't authorized to build ships for a clan."));
				return false;
			}
		}
		return super.autoGenInvoke(mob, commands, givenTarget, auto, asLevel, autoGenerate, forceLevels, crafted);
	}
}
