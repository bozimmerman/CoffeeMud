package com.planet_ink.coffee_mud.Commands;
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
   Copyright 2000-2007 Bo Zimmerman

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
public class Report extends BaseAbleLister
{
	public Report(){}

	private String[] access={"REPORT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			StringBuffer buf=new StringBuffer(
							    "say \"I have "+mob.curState().getHitPoints()
							   +"/"+mob.maxState().getHitPoints()+" hit points, "
							   +mob.curState().getMana()+"/"+mob.maxState().getMana()
							   +" mana, "+mob.curState().getMovement()
							   +"/"+mob.maxState().getMovement()+" move");
			if((!CMSecurity.isDisabled("EXPERIENCE"))
			&&!mob.charStats().getCurrentClass().expless()
			&&!mob.charStats().getMyRace().expless()
			&&(mob.getExpNeededLevel()<Integer.MAX_VALUE))
			   buf.append(", and need "+mob.getExpNeededLevel()+" to level");
			buf.append(".\"");
			Command C=CMClass.getCommand("Say");
			if(C!=null) C.execute(mob,CMParms.parse(buf.toString()));
		}
		else
		{
			int level=parseOutLevel(commands);
			String s=CMParms.combine(commands,1).toUpperCase();
            StringBuffer say=new StringBuffer("");
            if("AFFECTS".startsWith(s)||(s.equalsIgnoreCase("ALL")))
            {
                
                StringBuffer aff=new StringBuffer("\n\r^!I am affected by:^? ");
                Command C=CMClass.getCommand("Affect");
                if(C!=null) C.execute(mob,CMParms.makeVector(aff));
                say.append(aff.toString());
            }
			if("STATS".startsWith(s)||(s.equalsIgnoreCase("ALL")))
			{
				StringBuffer stats=new StringBuffer("");
		        int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
		        CharStats CT=mob.charStats();
		        stats.append("^cStr: ^w"+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_STRENGTH)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_STRENGTH_ADJ))+", ");
		        stats.append("^cInt: ^w"+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_INTELLIGENCE)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ))+", ");
		        stats.append("^cDex: ^w"+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_DEXTERITY)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ))+", ");
		        stats.append("^cWis: ^w"+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_WISDOM)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_WISDOM_ADJ))+", ");
		        stats.append("^cCon: ^w"+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_CONSTITUTION)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ))+", ");
		        stats.append("^cCha: ^w"+CMStrings.padRight(Integer.toString(CT.getStat(CharStats.STAT_CHARISMA)),2)+"/"+(max+CT.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)));
				say.append("\n\r^NMy stats:^? "+stats.toString());
			}
            if(s.equalsIgnoreCase("ALL"))
            {
                
                Vector V=new Vector();
                V.addElement(new Integer(Ability.ACODE_THIEF_SKILL));
                V.addElement(new Integer(Ability.ACODE_SKILL));
                V.addElement(new Integer(Ability.ACODE_COMMON_SKILL));
                V.addElement(new Integer(Ability.ACODE_SPELL));
                V.addElement(new Integer(Ability.ACODE_PRAYER));
                V.addElement(new Integer(Ability.ACODE_SUPERPOWER));
                V.addElement(new Integer(Ability.ACODE_CHANT));
                V.addElement(new Integer(Ability.ACODE_SONG));
                say.append("\n\r^NMy skills:^? "+getAbilities(mob,V,Ability.ALL_ACODES,false,level));
            }
            else
            if("SPELLS".startsWith(s))
                say.append("\n\r^NMy spells:^? "+getAbilities(mob,Ability.ACODE_SPELL,-1,false,level));
            else
			if("SKILLS".startsWith(s))
			{
				Vector V=new Vector();
				V.addElement(new Integer(Ability.ACODE_THIEF_SKILL));
				V.addElement(new Integer(Ability.ACODE_SKILL));
				V.addElement(new Integer(Ability.ACODE_COMMON_SKILL));
				say.append("\n\r^NMy skills:^? "+getAbilities(mob,V,Ability.ALL_ACODES,false,level));
			}
            else
			if("PRAYERS".startsWith(s))
				say.append("\n\r^NMy prayers:^? "+getAbilities(mob,Ability.ACODE_PRAYER,-1,false,level));
            else
			if(("POWERS".startsWith(s))||("SUPER POWERS".startsWith(s)))
				say.append("\n\r^NMy super powers:^? "+getAbilities(mob,Ability.ACODE_SUPERPOWER,-1,false,level));
            else
			if("CHANTS".startsWith(s))
				say.append("\n\r^NMy chants:^? "+getAbilities(mob,Ability.ACODE_CHANT,-1,false,level));
            else
			if("SONGS".startsWith(s))
				say.append("\n\r^NMy songs:^? "+getAbilities(mob,Ability.ACODE_SONG,-1,false,level));
            
            
            if(say.length()==0)
				mob.tell("'"+s+"' is unknown.  Try SPELLS, SKILLS, PRAYERS, CHANTS, SONGS, STATS, or ALL.");
            else
                CMLib.commands().postSay(mob,null,say.toString(),false,false);
		}
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
