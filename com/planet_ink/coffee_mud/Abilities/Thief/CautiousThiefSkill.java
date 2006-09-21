package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

public class CautiousThiefSkill extends ThiefSkill {
	public String ID() { return "CautiousThiefSkill"; }
	public String name(){ return "a cautious Thief Skill";}
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,"CAUTIOUS");}

}
