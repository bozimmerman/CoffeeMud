package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Skill_Parry extends StdAbility
{
	public Skill_Parry()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Parry";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(3);

		addQualifyingClass(new Fighter().ID(),3);
		addQualifyingClass(new Ranger().ID(),3);
		addQualifyingClass(new Paladin().ID(),3);
		addQualifyingClass(new Thief().ID(),9);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Parry();
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
				if((invoker()!=null)&&(affect.tool()!=null)&&(affect.tool() instanceof Item))
				{
					Item attackerWeapon=(Item)affect.tool();
					if((mob.fetchWieldedItem()!=null)
					&&((attackerWeapon!=null)&&((attackerWeapon instanceof Weapon))&&(!(attackerWeapon instanceof Natural))))
					{
						int pctParry=mob.charStats().getDexterity()*2;
						if((Dice.rollPercentage()<pctParry)&&(profficiencyCheck(0)))
						{
							FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> parry(s) "+affect.source().fetchWieldedItem().name()+" attack from <T-NAME>!");
							mob.location().send(mob,msg);
							helpProfficiency(mob);
							return false;
						}
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