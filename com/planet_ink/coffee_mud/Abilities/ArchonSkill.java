package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class ArchonSkill extends StdAbility
{
	public ArchonSkill()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="an Archon Skill";
		displayText="(in the realms of greatest power)";
		miscText="";
		putInCommandlist=false;
		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Archon().ID(),1);
		recoverEnvStats();
	}
	
	public int classificationCode()
	{
		return Ability.SKILL;
	}
	
	public Environmental newInstance()
	{
		return new ArchonSkill();
	}
	
}
