package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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
	}
	
	public int classificationCode()
	{
		return Ability.SKILL;
	}
	
	public Environmental newInstance()
	{
		return new ThiefSkill();
	}
	
}
