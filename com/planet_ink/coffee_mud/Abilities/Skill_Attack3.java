package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Skill_Attack3 extends StdAbility
{
	private boolean temporarilyDisable=false;

	public Skill_Attack3()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Third Attack";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(12);

		addQualifyingClass(new Fighter().ID(),12);
		addQualifyingClass(new Ranger().ID(),12);
		addQualifyingClass(new Paladin().ID(),12);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Attack3();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;
		if(!temporarilyDisable)
		{
			if(affect.amISource(mob))
			{
				switch(affect.sourceType())
				{
				case Affect.STRIKE:
					switch(affect.sourceCode())
					{
					case Affect.STRIKE_HANDS:
						if((mob.isInCombat())&&(!mob.amDead())&&(affect.target() instanceof MOB))
						{
							if((mob.envStats().level()>=envStats().level())
							&&(profficiencyCheck(0)))
							{
								Item weapon=new Natural();
								if((affect.tool()!=null)&&(affect.tool() instanceof Item))
									weapon=(Item)affect.tool();
								temporarilyDisable=true;
								TheFight.doAttack(mob,(MOB)affect.target(),weapon);
								temporarilyDisable=false;
								helpProfficiency(mob);
							}
						}
						break;
					}
					break;

				}
			}
		}
	}
}
