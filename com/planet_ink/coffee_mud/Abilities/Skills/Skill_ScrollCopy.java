package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Skill_ScrollCopy extends StdAbility
{
	public String ID() { return "Skill_ScrollCopy"; }
	public String name(){ return "Scroll Copy";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"COPY","SCROLLCOPY"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if(commands.size()<2)
		{
			mob.tell("Copy what from what?");
			return false;
		}
		Item target=mob.fetchCarried(null,Util.combine(commands,1));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}

		if(!(target instanceof Scroll))
		{
			mob.tell("You can't copy from that.");
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
			if(EnglishParser.containsString(A.name(),((String)commands.elementAt(0))))
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
		T.charStats().setStat(CharStats.GENDER,'N');
		while(T.numLearnedAbilities()>0)
		{
			Ability A=T.fetchAbility(0);
			if(A!=null)
				T.delAbility(A);
		}
		thisSpell.setProfficiency(50);
		T.addAbility(thisSpell);
		if(!thisSpell.canBeLearnedBy(T,mob))
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(mob.location().show(mob,null,target,CMMsg.MSG_HANDS,"<S-NAME> cop(ys) '"+thisSpell.name()+"' from <O-NAME>."))
				thisSpell.teach(T,mob);
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_HANDS,"<S-NAME> attempt(s) to copy '"+thisSpell.name()+"' from "+target.name()+", but fail(s).");
		return success;
	}

}
