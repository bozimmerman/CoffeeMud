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

public class Fighter_CritStrike extends StdAbility
{
	private int oldDamage=0;

	public Fighter_CritStrike()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Critical Strike";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(5);

		addQualifyingClass(new Fighter().ID(),5);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_CritStrike();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}


	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(affect.amISource(mob))
		{
			switch(affect.sourceType())
			{
			case Affect.STRIKE:
				switch(affect.sourceCode())
				{
				case Affect.STRIKE_HANDS:
					if((mob.isInCombat())
					&&(!mob.amDead())
					&&(affect.target() instanceof MOB)
					&&(affect.tool() != null)
					&&(affect.tool() instanceof Weapon)
					&&(((Weapon)affect.tool()).weaponClassification!=Weapon.CLASS_NATURAL))
					{
						oldDamage=mob.envStats().damage();
						if((mob.envStats().level()>=envStats().level())
						&&(profficiencyCheck(-15)))
						{
							mob.envStats().setDamage(oldDamage+(int)Math.round(Util.mul(affect.tool().envStats().damage(),(Util.div(profficiency(),100.0)))));
							helpProfficiency(mob);
						}
					}
					break;
				}
				break;

			}
		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if(affect.amISource(mob))
		{
			switch(affect.sourceType())
			{
			case Affect.STRIKE:
				switch(affect.sourceCode())
				{
				case Affect.STRIKE_HANDS:
					if((mob.isInCombat())
					&&(!mob.amDead())
					&&(affect.target() instanceof MOB)
					&&(affect.tool() != null)
					&&(affect.tool() instanceof Weapon)
					&&(((Weapon)affect.tool()).weaponClassification!=Weapon.CLASS_NATURAL))
						mob.envStats().setDamage(oldDamage);
					break;
				}
				break;

			}
		}
	}
}
