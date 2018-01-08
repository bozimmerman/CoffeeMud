package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
public class StdClanCard extends StdClanItem
{
	@Override 
	public String ID()
	{
		return "StdClanCard";
	}

	public StdClanCard()
	{
		super();

		setName("a clan membership card");
		basePhyStats.setWeight(1);
		setDisplayText("a membership card belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setClanItemType(ClanItem.ClanItemType.ANTI_PROPAGANDA);
		material=RawMaterial.RESOURCE_PAPER;
		recoverPhyStats();
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.target()==owner())
		&&(msg.tool() instanceof ClanItem)
		&&(owner() instanceof MOB)
		&&(((MOB)owner()).isMonster())
		&&(((ClanItem)msg.tool()).getClanItemType()==ClanItem.ClanItemType.PROPAGANDA)
		&&(!((ClanItem)msg.tool()).clanID().equals(clanID()))
		&&(CMLib.flags().isInTheGame((MOB)owner(),true))
		&&(msg.source()!=owner())
		&&(CMLib.flags().isInTheGame(msg.source(),true)))
		{
			if(msg.source().location().show((MOB)msg.target(),msg.source(),msg.tool(),CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> reject(s) <O-NAME> from <T-NAME>.")))
			{
				CMLib.commands().postSay((MOB)msg.target(),msg.source(),L("How dare you!  Give me those!"),false,true);
				if(msg.source().location().show((MOB)msg.target(),msg.source(),null,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> takes(s) @x1 away from <T-NAME> and destroys it!",msg.tool().name())))
				{
					Item I=null;
					for(int i=msg.source().numItems();i>=0;i--)
					{
						I=msg.source().getItem(i);
						if((I instanceof ClanItem)
						&&(I!=msg.tool())
						&&(((ClanItem)I).clanID().equals(((ClanItem)msg.tool()).clanID())))
							I.destroy();
					}
				}
				return false;
			}

		}
		return super.okMessage(host,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_CLANITEM)
		&&(owner() instanceof MOB)
		&&(clanID().length()>0)
		&&(((MOB)owner()).isMonster())
		&&(!CMLib.flags().isAnimalIntelligence((MOB)owner()))
		&&(((MOB)owner()).getStartRoom()!=null)
		&&(((MOB)owner()).location()!=null)
		&&(((MOB)owner()).getStartRoom().getArea()==((MOB)owner()).location().getArea()))
		{
			if(((MOB)owner()).getClanRole(clanID())==null)
			{
				final Room R=((MOB)owner()).location();
				final LegalBehavior B=CMLib.law().getLegalBehavior(R);
				if(B!=null)
				{
					final String rulingClan=B.rulingOrganization();
					if((rulingClan!=null)&&(rulingClan.length()>0)
					&&(rulingClan.equals(clanID())))
					{
						int roleID=0;
						final Clan C=CMLib.clans().getClan(clanID());
						if(C!=null)
							roleID=C.getGovernment().getAutoRole();
						((MOB)owner()).setClan(clanID(),roleID);
					}
				}
			}

		}
		return true;
	}
}
