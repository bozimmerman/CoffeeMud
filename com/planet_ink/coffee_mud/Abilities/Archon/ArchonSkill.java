package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class ArchonSkill extends StdAbility
{
	public String ID() { return "ArchonSkill"; }
	public String name(){ return "an Archon Skill";}
	public String displayText(){return "(in the realms of greatest power)";}
	public boolean putInCommandlist(){return false;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public int classificationCode()
	{ return Ability.SKILL;	}


}
