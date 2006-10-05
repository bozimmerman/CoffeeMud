package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;

public class DeceptiveThiefSkill extends ThiefSkill {
	public String ID() { return "DeceptiveThiefSkill"; }
	public String name(){ return "a deceptive Thief Skill";}
    private static final int EXPERTISE_STAGES=10;
    private static final String[] EXPERTISE={"DETRAP"};
    private static final String[] EXPERTISE_NAME={"Deceptive"};
    public void initializeClass()
    {
        super.initializeClass();
        if(CMLib.expertises().getDefinition(EXPERTISE[0]+EXPERTISE_STAGES)==null)
            for(int i=1;i<=EXPERTISE_STAGES;i++)
                CMLib.expertises().addDefinition(EXPERTISE[0]+i,EXPERTISE_NAME[0]+" "+CMath.convertToRoman(i),
                        "","+CHA "+(13+i)+" -LEVEL +>="+(13+(5*i)),0,1,0,0,0);
        if(!ID().equals("DeceptiveThiefSkill"))
            registerExpertiseUsage(EXPERTISE,EXPERTISE_STAGES,false,null);
    }
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,EXPERTISE[0]);}
}
