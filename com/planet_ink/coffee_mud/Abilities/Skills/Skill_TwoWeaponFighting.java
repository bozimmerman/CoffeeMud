package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Skill_TwoWeaponFighting extends StdAbility
{
	public String ID() { return "Skill_TwoWeaponFighting"; }
	public String name(){ return "Two Weapon Fighting";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	private Weapon lastWeapon=null;
	private Weapon lastPrimary=null;


	private Weapon getFirstWeapon(MOB mob)
	{
		if((lastPrimary!=null)
		&&(lastPrimary.amWearingAt(Item.WIELD))
		&&(!lastPrimary.amWearingAt(Item.HELD))
		&&(lastPrimary.container()==null))
			return lastPrimary;
		Weapon weapon=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item instanceof Weapon)
			    &&(item.amWearingAt(Item.WIELD))
				&&(!item.amWearingAt(Item.HELD))
			    &&(item.container()==null))
			{ weapon=(Weapon)item; break; }
		}
		lastPrimary=weapon;
		return weapon;
	}

	private Weapon getSecondWeapon(MOB mob)
	{
		if((lastWeapon!=null)
		&&(lastWeapon.amWearingAt(Item.HELD))
		&&(!lastWeapon.amWearingAt(Item.WIELD))
		&&(lastWeapon.container()==null))
			return lastWeapon;
		Weapon weapon=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item instanceof Weapon)
			    &&(item.amWearingAt(Item.HELD))
				&&(!item.amWearingAt(Item.WIELD))
			    &&(item.container()==null))
			{ weapon=(Weapon)item; break; }
		}
		lastWeapon=weapon;
		return weapon;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;

			if((getSecondWeapon(mob)!=null)&&(getFirstWeapon(mob)!=null))
			{
				if((affectableStats.speed()>=2.0)&&(lastWeapon!=null))
					affectableStats.setSpeed(affectableStats.speed()-1.0);
				else
				if(lastWeapon!=null)
					affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(affectableStats.attackAdjustment()/5));
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(mob.isInCombat())
			{
				if(Util.bset(mob.getBitmap(),MOB.ATT_AUTODRAW))
					CommonMsgs.draw(mob,true,true);

				Item primaryWeapon=getFirstWeapon(mob);
				Item weapon=getSecondWeapon(mob);
				if((weapon!=null) // try to wield anything!
				&&(primaryWeapon!=null)
				&&(mob.rangeToTarget()>=0)
				&&(mob.rangeToTarget()>=weapon.minRange())
				&&(mob.rangeToTarget()<=weapon.maxRange())
				&&(Sense.aliveAwakeMobile(mob,true))
				&&(!mob.amDead())
				&&(mob.curState().getHitPoints()>0)
				&&(!Sense.isSitting(mob))
				&&(profficiencyCheck(mob,0,false))
				&&(!mob.getVictim().amDead()))
				{
					primaryWeapon.setRawWornCode(Item.HELD);
					weapon.setRawWornCode(Item.WIELD);
					mob.recoverEnvStats();
					MUDFight.postAttack(mob,mob.getVictim(),weapon);
					weapon.setRawWornCode(Item.HELD);
					primaryWeapon.setRawWornCode(Item.WIELD);
					mob.recoverEnvStats();
					if(Dice.rollPercentage()==1)
						helpProfficiency(mob);
				}
			}
		}
		return super.tick(ticking,tickID);
	}
}
