package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_Spellcraft extends StdSkill
{
	public String ID() { return "Skill_Spellcraft"; }
	public String name(){ return "Spellcraft";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
    public int classificationCode(){ return Ability.ACODE_SKILL|Ability.DOMAIN_ARCANELORE;}
	public String lastID="";
	public int craftType(){return Ability.ACODE_SPELL;}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
		   return;
		MOB mob=(MOB)affected;
		if((msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS))
		&&(!msg.amISource(mob))
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().length()>0)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==craftType())
		&&(!lastID.equalsIgnoreCase(msg.tool().ID()))
		&&(mob.location()!=null)
		&&(mob.location().isInhabitant(msg.source()))
		&&(CMLib.flags().canBeSeenBy(msg.source(),mob))
		&&(msg.source().fetchAbility(msg.tool().ID())!=null))
        {
            boolean hasAble=(mob.fetchAbility(ID())!=null);
            int lowestLevel=CMLib.ableMapper().lowestQualifyingLevel(msg.tool().ID());
            int myLevel=0;
            if(hasAble) myLevel=adjustedLevel(mob,0)-lowestLevel+1;
            int lvl=(mob.envStats().level()/3)+getXLEVELLevel(mob);
            if(myLevel<lvl) myLevel=lvl;
    		if(((!hasAble)||proficiencyCheck(mob,0,false))&&(lowestLevel<=myLevel))
    		{
    			Ability A=(Ability)copyOf();
    			A.setMiscText(msg.tool().ID());
    			lastID=msg.tool().ID();
    			msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),A,CMMsg.MSG_OK_VISUAL,"<T-NAME> casts '"+msg.tool().name()+"'.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
    			helpProficiency(mob);
    		}
        }
	}
}
