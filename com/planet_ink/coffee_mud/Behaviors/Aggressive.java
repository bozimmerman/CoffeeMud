package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class Aggressive extends StdBehavior
{
	public String ID(){return "Aggressive";}
	public long flags(){return Behavior.FLAG_POTENTIALLYAGGRESSIVE|Behavior.FLAG_TROUBLEMAKING;}
	protected int tickWait=0;
	protected int tickDown=0;


	public boolean grantsAggressivenessTo(MOB M)
	{
		return CMLib.masking().maskCheck(getParms(),M);
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=CMParms.getParmInt(newParms,"delay",0);
		tickDown=tickWait;
	}

	public static boolean startFight(MOB monster,
									 MOB mob,
									 boolean fightMOBs)
	{
		if((mob!=null)
		&&(monster!=null)
		&&(mob!=monster)
		&&((!mob.isMonster())||(fightMOBs))
		&&(monster.location()!=null)
		&&(monster.location().isInhabitant(mob))
		&&(monster.location().getArea().getMobility())
		&&(!CMLib.flags().isATrackingMonster(mob))
		&&(!CMLib.flags().isATrackingMonster(monster))
		&&(canFreelyBehaveNormal(monster))
		&&(CMLib.flags().canBeSeenBy(mob,monster))
		&&(!CMSecurity.isAllowed(mob,mob.location(),"ORDER"))
		&&(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")))
		{
			// special backstab sneak attack!
			if(CMLib.flags().isHidden(monster))
			{
				Ability A=monster.fetchAbility("Thief_BackStab");
				if(A!=null)
				{
					A.setProfficiency(CMLib.dice().roll(1,50,A.adjustedLevel(mob,0)*15));
					A.invoke(monster,mob,false,0);
				}
			}
			// normal attack
			CMLib.combat().postAttack(monster,mob,monster.fetchWieldedItem());
			return true;
		}
		return false;
	}
	public static boolean pickAFight(MOB observer, String zapStr, boolean mobKiller)
	{
		if(!canFreelyBehaveNormal(observer)) return false;
		if(observer.location().getArea().getMobility())
		for(int i=0;i<observer.location().numInhabitants();i++)
		{
			MOB mob=observer.location().fetchInhabitant(i);
			if((mob!=null)
            &&(mob!=observer)
            &&(CMLib.masking().maskCheck(zapStr,mob))
            &&(startFight(observer,mob,mobKiller)))			
                return true;
		}
		return false;
	}

	public static void tickAggressively(Tickable ticking,
										int tickID,
										boolean mobKiller,
										String zapStr)
	{
		if(tickID!=Tickable.TICKID_MOB) return;
		if(ticking==null) return;
		if(!(ticking instanceof MOB)) return;
		pickAFight((MOB)ticking,zapStr,mobKiller);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Tickable.TICKID_MOB) return true;
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			tickAggressively(ticking,tickID,(getParms().toUpperCase().indexOf("MOBKILL")>=0),getParms());
		}
		return true;
	}
}
