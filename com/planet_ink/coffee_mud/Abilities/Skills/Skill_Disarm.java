package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Disarm extends StdAbility
{

	public Skill_Disarm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Disarm";
		displayText="(in a dark realm of thievery)";
		miscText="";

		triggerStrings.addElement("DISARM");

		canBeUninvoked=true;
		isAutoinvoked=false;

		canTargetCode=Ability.CAN_MOBS;
		canAffectCode=0;
		
		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(11);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Disarm();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to do this!");
			return false;
		}
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to disarm!");
			return false;
		}
		if(mob.fetchWieldedItem()==null)
		{
			mob.tell("You need a weapon to disarm someone!");
			return false;
		}
		if((mob.getVictim().fetchWieldedItem()==null)
		   ||(!(mob.getVictim().fetchWieldedItem() instanceof Weapon))
		   ||((((Weapon)mob.getVictim().fetchWieldedItem()).weaponClassification()==Weapon.CLASS_NATURAL)))
		{
			mob.tell("He is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=mob.getVictim().envStats().level()-mob.envStats().level();
		if(levelDiff>0) 
			levelDiff=levelDiff*5;
		else 
			levelDiff=0;
		boolean hit=(auto)||(CoffeeUtensils.normalizeAndRollLess(mob.adjustedAttackBonus()+mob.getVictim().adjustedArmor()));
		boolean success=profficiencyCheck(-levelDiff,auto)&&(hit);
		Item hisWeapon=mob.getVictim().fetchWieldedItem();
		if((success)
		   &&(hisWeapon!=null)
		   &&((hisWeapon.rawProperLocationBitmap()==Item.WIELD)
			  ||(hisWeapon.rawProperLocationBitmap()==Item.WIELD+Item.HELD)))
		{
			mob.location().show(mob,mob.getVictim(),Affect.MSG_NOISYMOVEMENT,auto?"<T-NAME> is disarmed!":"<S-NAME> disarm(s) <T-NAMESELF>!");
			FullMsg msg=new FullMsg(mob.getVictim(),hisWeapon,null,Affect.MSG_DROP,null);
			if(mob.location().okAffect(msg))
				mob.location().send(mob.getVictim(),msg);
		}
		else
			maliciousFizzle(mob,mob.getVictim(),"<S-NAME> attempt(s) to disarm <T-NAMESELF> and fail(s)!");
		return success;
	}

}