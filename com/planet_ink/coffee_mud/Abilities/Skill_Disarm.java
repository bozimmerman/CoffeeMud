package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		malicious=true;

		baseEnvStats().setLevel(9);

		addQualifyingClass(new Fighter().ID(),9);
		addQualifyingClass(new Ranger().ID(),9);
		addQualifyingClass(new Paladin().ID(),9);
		addQualifyingClass(new Thief().ID(),9);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Disarm();
	}

	public boolean invoke(MOB mob, Vector commands)
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
		if((mob.getVictim().fetchWieldedItem()==null)||(!(mob.getVictim().fetchWieldedItem() instanceof Weapon))||((mob.getVictim().fetchWieldedItem() instanceof Natural)))
		{
			mob.tell("He is not wielding a weapon!");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		Item weapon=mob.fetchWieldedItem();
		int oldAtt=mob.envStats().attackAdjustment();
		mob.envStats().setAttackAdjustment(oldAtt-25);
		boolean success=profficiencyCheck(-(mob.getVictim().charStats().getStrength()))&&(TheFight.isHit(mob,mob.getVictim()));
		mob.envStats().setAttackAdjustment(oldAtt);
		if(!success)
		{
			String str="<S-NAME> attempt(s) to disarm <T-NAME> and fail(s)!";
			FullMsg msg=new FullMsg(mob,mob.getVictim(),this,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str,Affect.VISUAL_WNOISE,str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		else
		{
			String str="<S-NAME> disarm(s) <T-NAME>!";
			Item hisWeapon=mob.getVictim().fetchWieldedItem();
			FullMsg msg=new FullMsg(mob,hisWeapon,null,Affect.HANDS_DROP,Affect.HANDS_DROP,Affect.VISUAL_WNOISE,str);
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
		return success;
	}

}