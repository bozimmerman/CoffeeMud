package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_Benediction extends Prayer
{
	public String ID() { return "Prayer_Benediction"; }
	public String name(){ return "Benediction";}
	public String displayText(){ return "(Benediction)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null)return;

		MOB mob=(MOB)affected;
		int pts=adjustedLevel(invoker())/5;
		CharStats chk=new DefaultCharStats(0);
		chk.setCurrentClass(mob.charStats().getCurrentClass());
		for(int c=0;c<mob.charStats().numClasses();c++)
			mob.charStats().getMyClass(c).affectCharStats(mob,chk);
		int num=0;
		for(int i=CharStats.MAX_STRENGTH_ADJ;i<CharStats.MAX_STRENGTH_ADJ+CharStats.NUM_BASE_STATS;i++)
			if(chk.getStat(i)>0) num++;
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			if(chk.getStat(CharStats.MAX_STRENGTH_ADJ+i)>0)
				affectableStats.setStat(i,affectableStats.getStat(i)+(pts/num));
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your benediction fades.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) filled with benediction!":"^S<S-NAME> "+prayWord(mob)+" for a benediction over <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for a benediction over <T-YOUPOSS>  but there is no answer.");


		// return whether it worked
		return success;
	}
}
