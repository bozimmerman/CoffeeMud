package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_TwoWeaponFighting extends StdSkill
{
	public String ID() { return "Skill_TwoWeaponFighting"; }
	public String name(){ return "Two Weapon Fighting";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_MARTIALLORE;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

    protected Weapon lastSecondary=null;
    protected Weapon lastPrimary=null;
    protected boolean attackedSinceLastTick=false;

    protected Weapon getFirstWeapon(MOB mob)
	{
		if((lastPrimary!=null)
		&&(lastPrimary.amWearingAt(Wearable.WORN_WIELD))
		&&(!lastPrimary.amWearingAt(Wearable.WORN_HELD))
		&&(lastPrimary.container()==null))
			return lastPrimary;
		Weapon weapon=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item instanceof Weapon)
			    &&(item.amWearingAt(Wearable.WORN_WIELD))
				&&(!item.amWearingAt(Wearable.WORN_HELD))
			    &&(item.container()==null))
			{ weapon=(Weapon)item; break; }
		}
		lastPrimary=weapon;
		return weapon;
	}

	private Weapon getSecondWeapon(MOB mob)
	{
		if((lastSecondary!=null)
		&&(lastSecondary.amWearingAt(Wearable.WORN_HELD))
		&&(!lastSecondary.amWearingAt(Wearable.WORN_WIELD))
		&&(lastSecondary.container()==null))
			return lastSecondary;
		Weapon weapon=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item instanceof Weapon)
			    &&(item.amWearingAt(Wearable.WORN_HELD))
				&&(!item.amWearingAt(Wearable.WORN_WIELD))
			    &&(item.container()==null))
			{ weapon=(Weapon)item; break; }
		}
        lastSecondary=weapon;
		return weapon;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;

			if((getSecondWeapon(mob)!=null)&&(getFirstWeapon(mob)!=null)&&(mob.isInCombat()))
			{
                int xlvl=super.getXLEVELLevel(invoker());
				affectableStats.setSpeed(affectableStats.speed()+1.0+(0.1*(float)xlvl));
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(affectableStats.attackAdjustment()/(5+xlvl)));
                affectableStats.setDamage(affectableStats.damage()-(affectableStats.damage()/(20+xlvl)));
			}
		}
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.target() instanceof MOB)
		&&(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK))
			attackedSinceLastTick=true;
		super.executeMsg(host, msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((mob!=null)&&(mob.isInCombat()))
			{
				if(CMath.bset(mob.getBitmap(),MOB.ATT_AUTODRAW))
					CMLib.commands().postDraw(mob,true,true);

				Item primaryWeapon=getFirstWeapon(mob);
				Item weapon=getSecondWeapon(mob);
				if((weapon!=null) // try to wield anything!
				&&(primaryWeapon!=null)
				&&attackedSinceLastTick
				&&(mob.rangeToTarget()>=0)
				&&(mob.rangeToTarget()>=weapon.minRange())
				&&(mob.rangeToTarget()<=weapon.maxRange())
				&&(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				&&(!mob.amDead())
				&&(mob.curState().getHitPoints()>0)
				&&(CMLib.flags().isStanding(mob))
				&&(proficiencyCheck(mob,0,false))
				&&(!mob.getVictim().amDead()))
				{
					primaryWeapon.setRawWornCode(Wearable.WORN_HELD);
					weapon.setRawWornCode(Wearable.WORN_WIELD);
					mob.recoverEnvStats();
					CMLib.combat().postAttack(mob,mob.getVictim(),weapon);
					weapon.setRawWornCode(Wearable.WORN_HELD);
					primaryWeapon.setRawWornCode(Wearable.WORN_WIELD);
					mob.recoverEnvStats();
					if(CMLib.dice().rollPercentage()==1)
						helpProficiency(mob);
				}
			}
			attackedSinceLastTick=false;
		}
		return super.tick(ticking,tickID);
	}
}
