package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_TwoWeaponFighting extends StdAbility
{
	private boolean middleOfTheFight=false;
	private boolean everyOther=false;
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
			if(mob.rangeToTarget()>=0)
			{
				Weapon weapon=getSecondWeapon((MOB)affected);
				if((weapon!=null)
				   &&(mob.rangeToTarget()>=0)
				   &&(mob.rangeToTarget()>=weapon.minRange())
				   &&(mob.rangeToTarget()<=weapon.maxRange()))
				{
					affectableStats.setSpeed(affectableStats.speed()/2.0);
					if(middleOfTheFight)
					{
						affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()/2);
						Item weapon1=mob.fetchWieldedItem();
						if(weapon1!=null)
							affectableStats.setDamage(affectableStats.damage()-weapon1.envStats().damage()+weapon.envStats().damage());
					}
				}
			}
		}
	}
	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if((affect.amISource(mob))
		&&(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
		&&(profficiencyCheck(0,false))
		&&(mob.isInCombat())
		&&(!mob.amDead())
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Weapon)
		&&(((Weapon)affect.tool()).amWearingAt(Item.WIELD)||(((Item)affect.tool()).amWearingAt(Item.INVENTORY)))
		&&(affect.target() instanceof MOB)
		&&(mob.envStats().level()>=envStats().level()))
		{
			Weapon weapon=getSecondWeapon(mob);
			if((weapon!=null)
			   &&(mob.rangeToTarget()>=0)
			   &&(mob.rangeToTarget()>=weapon.minRange())
			   &&(mob.rangeToTarget()<=weapon.maxRange()))
			{
				if(mob.fetchAffect(mob.numAffects()-1)!=this)
				{
					mob.delAffect(this);
					mob.addAffect(this);
				}
				middleOfTheFight=true;
				mob.recoverEnvStats();
				ExternalPlay.postAttack(mob,(MOB)affect.target(),weapon);
				middleOfTheFight=false;
				mob.recoverEnvStats();
				helpProfficiency(mob);
			}
		}
	}
}
