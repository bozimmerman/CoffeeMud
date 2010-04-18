package com.planet_ink.coffee_mud.CharClasses;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Wizard extends Mage
{
    public String ID(){return "Wizard";}
    public String name(){return "Wizard";}
    public String baseClass(){return "Mage";}
	protected boolean grantSomeSpells(){return false;}

    public void initializeClass()
    {
        super.initializeClass();
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_Spellcraft",50,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Skill_ScrollCopy",100,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_Scribe",75,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Papermaking",75,true);
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_ReadMagic",100,true);


        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_MagicMissile",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_ResistMagicMissiles",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_Shield",false);
        CMLib.ableMapper().addCharAbilityMapping(ID(),1,"Spell_IronGrip",false);

        for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
        {
            Ability A=(Ability)a.nextElement();
            if((A!=null)
            &&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
            {
                int level=CMLib.ableMapper().getQualifyingLevel(ID(),true,A.ID());
                if(level>0)
                {
	                AbilityMapper.AbilityMapping able=CMLib.ableMapper().getAbleMap(ID(),A.ID());
	                if((able!=null)
	                &&(!CMLib.ableMapper().getDefaultGain(ID(),true,A.ID())))
	                {
	                    able.costOverrides=new Integer[]{Integer.valueOf(0),Integer.valueOf(0),Integer.valueOf(0),Integer.valueOf(0)};
	                    able.defaultProficiency=100;
	                }
                }
            }
        }
    }

    public int availabilityCode(){return 0;}
    //public int availabilityCode(){return Area.THEME_FANTASY;}
    public String getOtherBonusDesc()
    {
        return "Can memorize any spell for casting without expending a training point.";
    }
    public String getOtherLimitsDesc()
    {
        return "Unable to learn spells permanently; can only memorize them.";
    }

    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        super.executeMsg(myHost,msg);
        if((myHost==null)||(!(myHost instanceof MOB)))
           return;
        MOB mob=(MOB)myHost;
        if(msg.amISource(mob)&&(msg.tool()!=null))
        {
            if(msg.tool() instanceof Ability)
            {
                Ability A=mob.fetchAbility(msg.tool().ID());
                if((A!=null)&&(!CMLib.ableMapper().getDefaultGain(ID(),false,A.ID()))
                &&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL))
                {
                    mob.delAbility(A);
                    mob.recoverMaxState();
                }
            }
            else
            if(msg.tool().ID().equalsIgnoreCase("Skill_ScrollCopy"))
            {

            }
        }
    }

    public void affectCharState(MOB mob, CharState state)
    {
        super.affectCharState(mob,state);
        if(mob.baseCharStats().getCurrentClass().ID().equals(ID()))
        {
            Ability A=null;
            for(int a=0;a<mob.numLearnedAbilities();a++)
            {
                A=mob.fetchAbility(a);
                if((A!=null)
                &&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
                &&(!CMLib.ableMapper().getDefaultGain(ID(),false,A.ID())))
                {
                    int[] cost=A.usageCost(mob,true);
                    int manaCost=cost[Ability.USAGEINDEX_MANA];
                    if(manaCost>0)
                    {
                        if(state.getMana()<manaCost)
                        {
                            mob.delAbility(A);
                            a--;
                        }
                        else
                            state.setMana(state.getMana()-manaCost);
                    }
                }
            }
            if(mob.curState().getMana()>state.getMana())
            	mob.curState().setMana(state.getMana());
        }
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        if(!(myHost instanceof MOB))
            return super.okMessage(myHost,msg);
        MOB myChar=(MOB)myHost;
        if((msg.tool()==null)||(!(msg.tool() instanceof Ability)))
           return super.okMessage(myChar,msg);
        if(msg.amISource(myChar)
        &&(myChar.isMine(msg.tool())))
        {
            if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
            &&(!CMLib.ableMapper().getDefaultGain(ID(),true,msg.tool().ID())))
            {
                if(CMLib.dice().rollPercentage()>
                   (myChar.charStats().getStat(CharStats.STAT_INTELLIGENCE)*((myChar.charStats().getCurrentClass().ID().equals(ID()))?1:2)))
                {
                    myChar.location().show(myChar,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fizzle(s) a spell.");
                    return false;
                }
            }
        }
        return super.okMessage(myChar,msg);
    }
}
