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

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(9);

		addQualifyingClass("Fighter",9);
		addQualifyingClass("Ranger",9);
		addQualifyingClass("Paladin",9);
		addQualifyingClass("Thief",9);
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
		if(mob.fetchWieldedItem()==null)
		{
			mob.tell("You need a weapon to disarm someone!");
			return false;
		}
		if((mob.getVictim().fetchWieldedItem()==null)
		   ||(!(mob.getVictim().fetchWieldedItem() instanceof Weapon))
		   ||((((Weapon)mob.getVictim().fetchWieldedItem()).weaponType()==Weapon.TYPE_NATURAL)))
		{
			mob.tell("He is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Item weapon=mob.fetchWieldedItem();
		int oldAtt=mob.envStats().attackAdjustment();
		mob.envStats().setAttackAdjustment(oldAtt-25);
		boolean success=profficiencyCheck(-(mob.getVictim().charStats().getStrength()*2),auto)&&(ExternalPlay.isHit(mob,mob.getVictim()));
		mob.envStats().setAttackAdjustment(oldAtt);
		if(success)
		{
			Item hisWeapon=mob.getVictim().fetchWieldedItem();
			FullMsg msg=new FullMsg(mob,hisWeapon,null,Affect.MSG_DROP,auto?"<T-NAME> is disarmed!":"<S-NAME> disarm(s) <T-NAMESELF>!");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
			maliciousFizzle(mob,mob.getVictim(),"<S-NAME> attempt(s) to disarm <T-NAMESELF> and fail(s)!");
		return success;
	}

}