package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
		recoverEnvStats();
		canAffectCode=0;
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
