package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_TwoWeaponFighting extends StdAbility
{
	private boolean middleOfTheFight=false;
	private Weapon lastWeapon=null;
	public Skill_TwoWeaponFighting()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Two Weapon Fighting";
		displayText="";
		miscText="";

		quality=Ability.BENEFICIAL_SELF;
		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(8);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_TwoWeaponFighting();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	private Weapon getSecondWeapon(MOB mob)
	{
		if((lastWeapon!=null)
		&&(lastWeapon.amWearingAt(Item.HELD))
		&&(!lastWeapon.amWearingAt(Item.WIELD))
		&&(lastWeapon.location()==null))
			return lastWeapon;
		Weapon weapon=null;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item item=mob.fetchInventory(i);
			if((item instanceof Weapon)
			    &&(item.amWearingAt(Item.HELD))
				&&(!item.amWearingAt(Item.WIELD))
			    &&(item.location()==null))
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
			
			if(affectableStats.speed()>=2.0) affectableStats.setSpeed(affectableStats.speed()-1.0);
			if(middleOfTheFight)
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(affectableStats.attackAdjustment()/2));
				Item w=mob.fetchWieldedItem();
				if((w!=null)&&(lastWeapon!=null))
					affectableStats.setDamage(affectableStats.damage()-w.envStats().damage()+lastWeapon.envStats().damage());
			}
		}
	}
	
	public boolean tick(int tickID)
	{
		if((tickID==Host.MOB_TICK)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(mob.isInCombat())
			{
				Item weapon=getSecondWeapon(mob);
				if((weapon!=null) // try to wield anything!
				&&(mob.rangeToTarget()>=0)
				&&(mob.rangeToTarget()>=weapon.minRange())
				&&(mob.rangeToTarget()<=weapon.maxRange())
				&&((mob.getBitmap()&MOB.ATT_AUTOMELEE)==0)
				&&(Sense.aliveAwakeMobile(mob,true))
				&&(!mob.amDead())
				&&(mob.curState().getHitPoints()>0)
				&&(!Sense.isSitting(mob)))
				{
					if(mob.fetchAffect(mob.numAffects()-1)!=this)
					{
						mob.delAffect(this);
						mob.addAffect(this);
					}
					middleOfTheFight=true;
					mob.recoverEnvStats();
					ExternalPlay.postAttack(mob,mob.getVictim(),weapon);
					middleOfTheFight=false;
					mob.recoverEnvStats();
					helpProfficiency(mob);
				}
			}
		}
		return super.tick(tickID);
	}
}
