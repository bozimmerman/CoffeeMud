package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_Yearning extends Chant
{
	public String ID() { return "Chant_Yearning"; }
	public String name(){ return "Yearning";}
	public String displayText(){return "(Sexual Yearnings)";}
	public int quality(){return Ability.MALICIOUS;}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		int wis=affectableStats.getStat(CharStats.WISDOM);
		wis=wis-5;
		if(wis<1) wis=1;
		affectableStats.setStat(CharStats.WISDOM,wis);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your yearning subsides.");
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		// the sex rules
		if(!(affected instanceof MOB)) return;
		MOB myChar=(MOB)affected;

		if((msg.amISource(myChar))
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&(msg.tool()!=null)
		&&(msg.tool().ID().equals("Social"))
		&&(msg.tool().Name().equals("MATE <T-NAME>")
			||msg.tool().Name().equals("SEX <T-NAME>"))
		&&(myChar.location()==((MOB)msg.target()).location())
		&&(myChar.numWearingHere(Item.ON_LEGS)==0)
		&&(((MOB)msg.target()).numWearingHere(Item.ON_LEGS)==0)
		&&(myChar.numWearingHere(Item.ON_WAIST)==0)
		&&(((MOB)msg.target()).numWearingHere(Item.ON_WAIST)==0))
			unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|CMMsg.MASK_MALICIOUS,auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) to yearn for something!");
					maliciousAffect(mob,target,(Integer.MAX_VALUE/2),-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");


		// return whether it worked
		return success;
	}
}