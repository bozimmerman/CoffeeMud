package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

public class StealthyThiefSkill extends ThiefSkill {
	public String ID() { return "StealthyThiefSkill"; }
	public String name(){ return "a stealthy Thief Skill";}
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,"STEALTH");}
}
