package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;

public class StealthyThiefSkill extends ThiefSkill {
	public String ID() { return "StealthyThiefSkill"; }
	public String name(){ return "a stealthy Thief Skill";}
    private static final int EXPERTISE_STAGES=10;
    private static final String[] EXPERTISE={"STEALTH"};
    private static final String[] EXPERTISE_NAME={"Stealthy"};
    public void initializeClass()
    {
        super.initializeClass();
        if(CMLib.expertises().getDefinition(EXPERTISE[0]+EXPERTISE_STAGES)==null)
            for(int i=1;i<=EXPERTISE_STAGES;i++)
                CMLib.expertises().addDefinition(EXPERTISE[0]+i,EXPERTISE_NAME[0]+" "+CMath.convertToRoman(i),
                        "","+DEX "+(9+i)+" -LEVEL +>="+(1+(5*i)),0,1,0,0,0);
        if(!ID().equals("StealthyThiefSkill"))
            registerExpertiseUsage(EXPERTISE,EXPERTISE_STAGES,false,null);
    }
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,EXPERTISE[0]);}
}
