package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;

public class StealingThiefSkill extends ThiefSkill {
	public String ID() { return "StealingThiefSkill"; }
	public String name(){ return "a stealing Thief Skill";}
    private static final int EXPERTISE_STAGES=10;
    private static final String[] EXPERTISE={"STEALING"};
    private static final String[] EXPERTISES={"STEALING","CAUTIOUS"};
    private static final String[] EXPERTISE_NAME={"Theft Mastery"};
    protected boolean IS_CAUTIOUS_ALSO(){return false;}
    static
    {
        for(int i=1;i<=EXPERTISE_STAGES;i++)
            CMLib.expertises().addDefinition(EXPERTISE[0]+i,EXPERTISE_NAME[0]+" "+CMath.convertToRoman(i),
                    "","+DEX "+(9+i)+" -LEVEL +>="+(1+(5*i)),0,1,0,0,0);
    }
    public void setMiscText(String newText)
    {
        super.setMiscText(newText);
        if(IS_CAUTIOUS_ALSO())
            registerExpertiseUsage(EXPERTISES,EXPERTISE_STAGES,false,null);
        else
            registerExpertiseUsage(EXPERTISE,EXPERTISE_STAGES,false,null);
    }
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,EXPERTISE[0]);}
}
