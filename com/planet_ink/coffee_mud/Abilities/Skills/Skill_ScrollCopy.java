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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Skill_ScrollCopy extends StdSkill
{
	public String ID() { return "Skill_ScrollCopy"; }
	public String name(){ return "Memorize";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"MEMORIZE"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_CALLIGRAPHY;}
    protected int overrideMana(){return 0;} //-1=normal, Integer.MAX_VALUE=all, Integer.MAX_VALUE-100

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if(commands.size()<2)
		{
			mob.tell("Memorize what from what?");
			return false;
		}
		Item target=mob.fetchCarried(null,CMParms.combine(commands,1));
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+CMParms.combine(commands,1)+"' here.");
			return false;
		}

		if(!(target instanceof Scroll))
		{
			mob.tell("You can't memorize from that.");
			return false;
		}

		if(((Scroll)target).usesRemaining()<1)
		{
			mob.tell("The scroll appears to be faded.");
			return false;
		}

		Vector theSpells=((Scroll)target).getSpells();
		Ability thisSpell=null;
		for(int a=0;a<theSpells.size();a++)
		{
			Ability A=(Ability)theSpells.elementAt(a);
			if(CMLib.english().containsString(A.name(),((String)commands.elementAt(0))))
			{
				thisSpell=A;
				break;
			}
		}

		if(thisSpell==null)
		{
			mob.tell("That is not written on "+target.name()+".");
			return false;
		}

		thisSpell=(Ability)thisSpell.copyOf();
		MOB T=CMClass.getMOB("Teacher");
		T.setName(target.name());
		T.charStats().setStat(CharStats.STAT_GENDER,'N');
		while(T.numLearnedAbilities()>0)
		{
			Ability A=T.fetchAbility(0);
			if(A!=null)
				T.delAbility(A);
		}
		thisSpell.setProficiency(50);
		T.addAbility(thisSpell);
		if(!thisSpell.canBeLearnedBy(T,mob))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,target,this,CMMsg.MSG_HANDS,"<S-NAME> memorize(s) '"+thisSpell.name()+"' from <T-NAME>."))
            {
				thisSpell.teach(T,mob);
                if((mob.fetchAbility(thisSpell.ID())!=null)
                &&(CMLib.ableMapper().qualifyingClassLevel(mob,this)>=0)
                &&(CMLib.ableMapper().qualifyingClassLevel(mob,thisSpell)>=0))
                {
                	if((thisSpell.practicesRequired(mob)>0)||(thisSpell.trainsRequired(mob)>0))
                    {
                        int xp=(int)Math.round(100.0*CMath.div(CMLib.ableMapper().lowestQualifyingLevel(thisSpell.ID()),CMLib.ableMapper().qualifyingClassLevel(mob,this)));
                        if(xp>=0)
                            CMLib.leveler().postExperience(mob,null,null,xp,false);
                    }
                }
            }
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_HANDS,"<S-NAME> attempt(s) to memorize '"+thisSpell.name()+"' from "+target.name()+", but fail(s).");
		return success;
	}

}
