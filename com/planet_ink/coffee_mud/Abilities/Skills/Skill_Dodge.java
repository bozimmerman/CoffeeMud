package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Dodge extends StdAbility
{
	boolean lastTime=false;
	public Skill_Dodge()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dodge";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(7);

		addQualifyingClass("Fighter",7);
		addQualifyingClass("Thief",6);
		addQualifyingClass("Ranger",7);
		addQualifyingClass("Paladin",7);
		addQualifyingClass("Bard",7);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Dodge();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob)&&(Sense.aliveAwakeMobile(mob,true))&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK))
		{
			FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_QUIETMOVEMENT,"<S-NAME> dodge(s) the attack by <T-NAME>!");
			if((profficiencyCheck(mob.charStats().getDexterity()-90,false))
			&&(!lastTime)
			&&(mob.location().okAffect(msg)))
			{
				lastTime=true;
				mob.location().send(mob,msg);
				helpProfficiency(mob);
				return false;
			}
			else
				lastTime=false;
		}
		return true;
	}
}
