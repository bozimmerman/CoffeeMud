package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class StdClanPamphlet extends StdClanItem
{
	public String ID(){	return "StdClanPamphlet";}
	protected int tradeTime=-1;

	public StdClanPamphlet()
	{
		super();

		setName("a clan pamphlet");
		baseEnvStats.setWeight(1);
		setDisplayText("a pamphlet belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setCIType(ClanItem.CI_PROPAGANDA);
		material=RawMaterial.RESOURCE_PAPER;
		recoverEnvStats();
	}

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
			String rulingClan=null;
			Room R=((MOB)owner()).location();
            if((((MOB)owner()).getClanID().length()>0)
			||(((--tradeTime)<=0)))
			{
                LegalBehavior B=CMLib.law().getLegalBehavior(R);
                if(B!=null) rulingClan=B.rulingOrganization();
			}
			if((rulingClan!=null)&&(rulingClan.length()>0)
            &&(!rulingClan.equals(clanID()))
            &&(((MOB)owner()).getClanID().equals(rulingClan)))
				((MOB)owner()).setClanID("");
			if(tradeTime<=0)
			{
				MOB mob=(MOB)owner();
				tradeTime=CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY);
				if((mob.getClanID().length()==0)
				&&(rulingClan!=null)
				&&(rulingClan.length()>0)
                &&(!rulingClan.equals(clanID()))
				&&(CMLib.flags().canSpeak(mob))
				&&(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				&&(R!=null))
				{
					MOB M=R.fetchInhabitant(CMLib.dice().roll(1,R.numInhabitants(),-1));
					if((M!=null)
					&&(M!=mob)
					&&(M.isMonster())
					&&(M.getClanID().equals(rulingClan))
					&&(!CMLib.flags().isAnimalIntelligence(M))
					&&(CMLib.flags().canBeSeenBy(M,mob))
					&&(CMLib.flags().canBeHeardBy(M,mob)))
					{
						CMLib.commands().postSay(mob,M,"Hey, take a look at this.",false,false);
						ClanItem I=(ClanItem)copyOf();
						mob.addInventory(I);
						CMMsg newMsg=CMClass.getMsg(mob,M,I,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
						if(mob.location().okMessage(mob,newMsg)&&(!((Item)I).amDestroyed()))
							mob.location().send(mob,newMsg);
						if(!M.isMine(I)) 
                            ((Item)I).destroy();
                        else
                        if(mob.isMine(I))
                            ((Item)I).destroy();
					}
				}
			}
		}
		return true;
	}
}
