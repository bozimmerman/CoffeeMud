package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Skill_Dodge extends StdAbility
{
	public Skill_Dodge()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dodge";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(2);

		addQualifyingClass(new Fighter().ID(),2);
		addQualifyingClass(new Thief().ID(),6);
		addQualifyingClass(new Ranger().ID(),2);
		addQualifyingClass(new Paladin().ID(),2);
		addQualifyingClass(new Bard().ID(),7);
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

		if(affect.amITarget(mob))
		{
			switch(affect.targetCode())
			{
			case Affect.STRIKE_HANDS:
				if(invoker()!=null)
				{
					int pctDodge=mob.charStats().getDexterity()*2;
					if((Dice.rollPercentage()<pctDodge)&&(profficiencyCheck(0)))
					{
						FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> dodge(s) the attack by <T-NAME>!");
						mob.location().send(mob,msg);
						helpProfficiency(mob);
						return false;
					}
				}
				break;
			default:
				break;
			}
		}
		return true;
	}
}
