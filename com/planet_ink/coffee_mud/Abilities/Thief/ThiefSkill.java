package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class ThiefSkill extends StdAbility
{
	public String ID() { return "ThiefSkill"; }
	public String name(){ return "a Thief Skill";}
	public int quality(){return Ability.INDIFFERENT;}
	public int classificationCode(){	return Ability.THIEF_SKILL;}
	public Environmental newInstance(){	return new ThiefSkill();}
}
