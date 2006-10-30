package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Spell extends StdAbility
{
	public String ID() { return "Spell"; }
	public String name(){ return "a Spell";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"CAST","CA","C"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SPELL;}

    private static final int EXPERTISE_STAGES=10;
    private static final String[] EXPERTISE={"RANGED","REDUCED","POWER","EXTENDED"};
    private static final String[] EXPERTISE_NAME={"Ranged","Reduced","Power","Extended"};
    private static final String[][] EXPERTISE_STATS={{"DEX","CON"},
                                                     {"WIS","STR"},
                                                     {"CHA","STR"},
                                                     {"CON","STR"},
    };
    private static final int[] EXPERTISE_LEVELS={19,16,17,18};
    public void initializeClass()
    {
        super.initializeClass();
        if((!ID().equals("Spell"))&&(!CMath.bset(flags(),Ability.FLAG_CLANMAGIC)))
        {
            String sdomain=shortDomainName();
            String fdomain=fullDomainName();
            if(CMLib.expertises().getDefinition(EXPERTISE[0]+sdomain+EXPERTISE_STAGES)==null)
            for(int e=0;e<EXPERTISE.length;e++)
            {
                if(CMLib.expertises().getDefinition(EXPERTISE[e]+sdomain+EXPERTISE_STAGES)==null)
                    for(int i=1;i<=EXPERTISE_STAGES;i++)
                        CMLib.expertises().addDefinition(EXPERTISE[e]+sdomain+i,EXPERTISE_NAME[e]+" "+CMStrings.capitalizeAndLower(sdomain)+" "+CMath.convertToRoman(i),
                                ((i==1)?"":"-EXPERTISE \""+EXPERTISE[e]+sdomain+(i-1)+"\"")+" -SKILLFLAG \"+"+fdomain+"\" ",
                                    " +"+EXPERTISE_STATS[e][0]+" "+(9+i)
                                   +" +"+EXPERTISE_STATS[e][1]+" "+(9+i)
                                   +" -LEVEL +>="+(EXPERTISE_LEVELS[e]+(5*i))
                                   ,0,1,0,0,0);
            }
        }
    }
    protected int getXLevel(MOB mob){ return getExpertiseLevel(mob,EXPERTISE[0]+shortDomainName());}
    
    protected final String fullDomainName()
    {
        return Ability.DOMAIN_DESCS[((classificationCode()&Ability.ALL_DOMAINS)>>5)];
    }
    protected final String shortDomainName()
    {
        String s=fullDomainName();
        int x=s.indexOf("/");
        if(x<0) return s;
        return s.substring(0,x);
    }
    
    public int maxRange()
    {
        int max=super.maxRange();
        if(invoker==null) return max;
        int level=super.getExpertiseLevel(invoker,"RANGED"+shortDomainName());
        if(level<=0) return max;
        return max+(int)Math.round(CMath.mul(max,CMath.mul(level,0.2)));
    }
    
    
    protected int[] buildCostArray(MOB mob, int consumed)
    {
        int[] cost=super.buildCostArray(mob,consumed);
        if((cost[USAGE_MANA]>0)&&(cost[USAGE_MANA]<mob.maxState().getMana()))
        {
            int minimum=CMProps.getMinManaException(ID());
            if(minimum==Integer.MIN_VALUE) minimum=CMProps.getIntVar(CMProps.SYSTEMI_MANAMINCOST);
            if(minimum<0) minimum=5;
            int level=super.getExpertiseLevel(mob,"REDUCED"+shortDomainName());
            if(level>0)
            {
                cost[USAGE_MANA]-=level;
                if(cost[USAGE_MANA]<minimum) cost[USAGE_MANA]=minimum;
            }
        }
        return cost;
    }
    
    public int adjustedLevel(MOB caster, int asLevel)
    {
        if(caster==null) return 1;
        if(asLevel<=0)
            return(super.adjustedLevel(caster,asLevel)+super.getExpertiseLevel(caster,"POWER"+shortDomainName()));
        return super.adjustedLevel(caster,asLevel);
    }
    
    public void startTickDown(MOB invokerMOB, Environmental affected, int tickTime)
    {
        int level=super.getExpertiseLevel(invokerMOB,"EXTENDED"+shortDomainName());
        if(level<=0) 
            super.startTickDown(invokerMOB,affected,tickTime);
        else
            super.startTickDown(invokerMOB,affected,tickTime+(int)Math.round(CMath.mul(tickTime,CMath.mul(level,0.20))));
    }
    
	public boolean maliciousAffect(MOB mob,
								   Environmental target,
								   int asLevel,
								   int tickAdjustmentFromStandard,
								   int additionAffectCheckCode)
	{
		boolean truefalse=super.maliciousAffect(mob,target,asLevel,tickAdjustmentFromStandard,additionAffectCheckCode);
		if(truefalse
		&&(target!=null)
		&&(target instanceof MOB)
		&&(mob!=target)
		&&(!((MOB)target).isMonster())
		&&(CMLib.dice().rollPercentage()==1)
		&&(((MOB)target).charStats().getCurrentClass().baseClass().equals("Mage")))
		{
			MOB tmob=(MOB)target;
			int num=0;
			for(int i=0;i<tmob.numEffects();i++)
			{
				Ability A=tmob.fetchEffect(i);
				if((A!=null)
				&&(A instanceof Spell)
				&&(A.abstractQuality()==Ability.QUALITY_MALICIOUS))
				{
					num++;
					if(num>5)
					{
						Ability A2=CMClass.getAbility("Disease_Magepox");
						if((A2!=null)&&(target.fetchEffect(A2.ID())==null))
							A2.invoke(mob,target,true,asLevel);
						break;
					}
				}
			}
		}
		return truefalse;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
        if((!auto)&&(mob.isMine(this))&&(mob.location()!=null))
        {
            if(super.getExpertiseLevel(invoker,"RANGED"+shortDomainName())>0) 
                invoker=mob;
    		if((!mob.isMonster())
    		&&(!disregardsArmorCheck(mob))
    		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_CLOTH))
    		&&(CMLib.dice().rollPercentage()<50))
    		{
    			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
    			return false;
    		}
        }
        return true;
	}
}
