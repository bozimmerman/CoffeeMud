package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Thiefness extends CombatAbilities
{
	public String ID(){return "Thiefness";}
	public long flags(){return Behavior.FLAG_TROUBLEMAKING;}
	private int tickDown=0;


	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		combatMode=COMBAT_RANDOM;
		makeClass(mob,getParmsMinusCombatMode(),"Thief");
		newCharacter(mob);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=MudHost.TICK_MOB) return true;
		if(!canActAtAll(ticking)) return true;
		if(!(ticking instanceof MOB)) return true;
		MOB mob=(MOB)ticking;
		if((--tickDown)<=0)
		if((Dice.rollPercentage()<10)&&(mob.location()!=null))
		{
			tickDown=2;
			MOB victim=null;
			if(mob.isInCombat())
				victim=mob.getVictim();
			else
			for(int i=0;i<mob.location().numInhabitants();i++)
			{
				MOB potentialVictim=mob.location().fetchInhabitant(i);
				if((potentialVictim!=null)
				   &&(potentialVictim!=mob)
				   &&(!potentialVictim.isMonster())
				   &&(Sense.canBeSeenBy(potentialVictim,mob)))
					victim=potentialVictim;
			}
			if((victim!=null)
			&&(!CMSecurity.isAllowed(victim,victim.location(),"CMROOMS"))
			&&(!CMSecurity.isAllowed(victim,victim.location(),"ORDER")))
			{
				Vector V=new Vector();
				Ability A=mob.fetchAbility((Dice.rollPercentage()>50)?(mob.isInCombat()?"Thief_Mug":"Thief_Steal"):"Thief_Swipe");
				if(A!=null)
				{
					if(!A.ID().equalsIgnoreCase("Thief_Swipe"))
					{
						Item I=null;
						for(int i=0;i<victim.inventorySize();i++)
						{
							Item potentialI=victim.fetchInventory(i);
							if((potentialI!=null)
							&&(potentialI.amWearingAt(Item.INVENTORY))
							&&(Sense.canBeSeenBy(potentialI,mob)))
								I=potentialI;
						}
						if(I!=null)
							V.addElement(I.ID());
					}
					if(!A.ID().equalsIgnoreCase("Thief_Mug"))
						V.addElement(victim.name());
					A.setProfficiency(Dice.roll(1,50,A.adjustedLevel(mob,0)*15));
					A.invoke(mob,V,null,false,0);
				}
			}
		}
		return true;
	}
}
