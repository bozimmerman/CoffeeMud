package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class ThiefSkill extends StdAbility
{
	public ThiefSkill()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Thief Skill";
		displayText="(in a dark realm of thievery)";
		miscText="";
		canAffectCode=0;
	}

	public int classificationCode()
	{
		return Ability.THIEF_SKILL;
	}

	public Environmental newInstance()
	{
		return new ThiefSkill();
	}

}
