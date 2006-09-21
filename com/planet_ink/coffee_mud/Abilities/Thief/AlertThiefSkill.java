package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

public class AlertThiefSkill extends ThiefSkill {
	public String ID() { return "AlertThiefSkill"; }
	public String name(){ return "an alert Thief Skill";}
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,"ALERT");}
}
