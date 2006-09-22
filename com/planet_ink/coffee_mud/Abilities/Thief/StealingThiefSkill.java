package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

public class StealingThiefSkill extends ThiefSkill {
	public String ID() { return "StealingThiefSkill"; }
	public String name(){ return "a stealing Thief Skill";}
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,"STEALING");}
}
