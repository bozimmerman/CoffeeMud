package com.planet_ink.coffee_mud.Abilities.SuperPowers;
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

public class Power_OctoArms extends SuperPower
{
	public String ID() { return "Power_OctoArms"; }
	public String name(){ return "Octo-Arms";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected  int canTargetCode(){return 0;}
	public int quality(){return Ability.MALICIOUS;}
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
			&&(mob.charStats().getBodyPart(Race.BODY_ARM)>2))
			{
				if(Dice.rollPercentage()>95)
					helpProfficiency(mob);
				int arms=mob.charStats().getBodyPart(Race.BODY_ARM)-2;
				Weapon naturalWeapon=CMClass.getWeapon("GenWeapon");
				naturalWeapon.setName("a huge snaking arm");
				naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
				naturalWeapon.setMaterial(EnvResource.RESOURCE_STEEL);
				naturalWeapon.baseEnvStats().setDamage(mob.baseEnvStats().damage());
				naturalWeapon.recoverEnvStats();
				for(int i=0;i<arms;i++)
					MUDFight.postAttack(mob,mob.getVictim(),naturalWeapon);
			}
		}
		return true;
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==invoker)
			affectableStats.alterBodypart(Race.BODY_ARM,4);
	}
}
