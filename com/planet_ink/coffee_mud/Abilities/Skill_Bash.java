package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Skill_Bash extends StdAbility
{
	public Skill_Bash()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Bash";
		displayText="(Dented)";
		miscText="";

		triggerStrings.addElement("BASH");

		canBeUninvoked=true;
		isAutoinvoked=false;

		malicious=true;

		baseEnvStats().setLevel(4);

		addQualifyingClass(new Fighter().ID(),4);
		addQualifyingClass(new Ranger().ID(),4);
		addQualifyingClass(new Paladin().ID(),4);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Bash();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;


		Item thisSheild=mob.fetchWornItem(Item.HELD);
		if((thisSheild==null)||((thisSheild!=null)&&(!(thisSheild instanceof Shield))))
		{
			mob.tell("You must have a sheild to perform a bash.");
			return false;
		}

		if((Sense.isSitting(target)||Sense.isSleeping(target)))
		{
			mob.tell(target.name()+" must stand up first!");
			return false;
		}

		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		String str=null;
		if(success)
		{
			str="<S-NAME> BASH(es) <T-NAME>!";
			FullMsg msg=new FullMsg(mob,target,null,Affect.STRIKE_JUSTICE,str,Affect.STRIKE_JUSTICE,str,Affect.VISUAL_WNOISE,str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				TheFight.doAttack(mob,target,new sheildWeapon(thisSheild));
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to bash <T-NAME>, but end(s) up looking silly.");

		return success;
	}

	private class sheildWeapon extends Weapon
	{
		public sheildWeapon()
		{
			super();
			myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
			name="fingernails and teeth";
			displayText="Those hands and claws look fit to kill.";
			miscText="";
			description="Looks like natural fighting ability.";
			baseEnvStats().setAbility(0);
			baseEnvStats().setLevel(0);
			baseEnvStats().setWeight(0);
			baseEnvStats().setAttackAdjustment(0);
			baseEnvStats().setDamage(1);
			weaponType=Weapon.TYPE_BASHING;
			weaponClassification=Weapon.CLASS_BLUNT;
			recoverEnvStats();
		}

		public sheildWeapon(Item shield)
		{
			super();
			myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
			name=shield.name();
			displayText=shield.displayText();
			miscText="";
			description=shield.description();
			baseEnvStats().setAbility(0);
			baseEnvStats().setLevel(0);
			baseEnvStats().setWeight(0);
			baseEnvStats().setAttackAdjustment(0);
			baseEnvStats().setDamage(shield.envStats().level());
			weaponType=Weapon.TYPE_BASHING;
			recoverEnvStats();
		}

		public Environmental newInstance()
		{
			return new sheildWeapon();
		}
	}
}
