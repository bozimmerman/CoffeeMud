package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;

public class CautiousThiefSkill extends ThiefSkill {
	public String ID() { return "CautiousThiefSkill"; }
	public String name(){ return "a cautious Thief Skill";}
    private static final int EXPERTISE_STAGES=10;
    private static final String[] EXPERTISE={"CAUTIOUS"};
    private static final String[] EXPERTISE_NAME={"Cautiousness"};
    static
    {
        for(int i=1;i<=EXPERTISE_STAGES;i++)
            CMLib.expertises().addDefinition(EXPERTISE[0]+i,EXPERTISE_NAME[0]+" "+CMath.convertToRoman(i),
                    "","+WIS "+(11+i)+" -LEVEL +>="+(9+(5*i)),0,1,0,0,0);
    }
    public void setMiscText(String newText)
    {
        super.setMiscText(newText);
        registerExpertiseUsage(EXPERTISE,EXPERTISE_STAGES,false,null);
    }
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,EXPERTISE[0]);}
}
