package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Fighter_SideKick extends StdAbility
{
	public String ID() { return "Fighter_SideKick"; }
	public String name(){ return "Side Kick";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.MALICIOUS;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_MOB)
		   &&(affected!=null)
		   &&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())
			&&(Sense.aliveAwakeMobile(mob,true))
			&&(mob.rangeToTarget()==0)
			&&(mob.charStats().getBodyPart(Race.BODY_LEG)>1)
			&&(mob.location()!=null)
			&&(!anyWeapons(mob)))
			{
				if(Dice.rollPercentage()>95)
					helpProfficiency(mob);
				MOB elligibleTarget=null;
				for(int m=0;m<mob.location().numInhabitants();m++)
				{
					MOB M=mob.location().fetchInhabitant(m);
					if((M!=null)
					&&(M.getVictim()==mob)
					&&(mob.getVictim()!=M)
					&&(M.rangeToTarget()==0))
					{
						elligibleTarget=M;
						break;
					}
				}
				if(elligibleTarget!=null)
				{
					Weapon naturalWeapon=CMClass.getWeapon("GenWeapon");
					naturalWeapon.setName("a side kick");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					naturalWeapon.recoverEnvStats();
					MUDFight.postAttack(mob,elligibleTarget,naturalWeapon);
				}
			}
		}
		return true;
	}

	public boolean anyWeapons(MOB mob)
	{
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			   &&((I.amWearingAt(Item.WIELD))
			      ||(I.amWearingAt(Item.HELD))))
				return true;
		}
		return false;
	}
}
