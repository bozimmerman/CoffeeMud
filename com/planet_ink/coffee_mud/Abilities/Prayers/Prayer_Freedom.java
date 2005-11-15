package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Prayer_Freedom extends Prayer
{
	public String ID() { return "Prayer_Freedom"; }
	public String name(){ return "Freedom";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public static Vector returnOffensiveAffects(MOB caster, Environmental fromMe)
	{
		MOB newMOB=CMClass.getMOB("StdMOB");
		Vector offenders=new Vector();

		FullMsg msg=new FullMsg(newMOB,null,null,CMMsg.MSG_SIT,null);
		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if(A!=null)
			{
				try
				{
					newMOB.recoverEnvStats();
					A.affectEnvStats(newMOB,newMOB.envStats());
					int clas=A.classificationCode()&Ability.ALL_CODES;
					if((!Sense.aliveAwakeMobile(newMOB,true))
					   ||(Util.bset(A.flags(),Ability.FLAG_BINDING))
					   ||(!A.okMessage(newMOB,msg)))
					if((A.invoker()==null)
					||((clas!=Ability.SPELL)&&(clas!=Ability.CHANT)&&(clas!=Ability.PRAYER)&&(clas!=Ability.SONG))
					||((A.invoker()!=null)
						&&(A.invoker().envStats().level()<=caster.envStats().level()+1)))
						  offenders.addElement(A);
				}
				catch(Exception e)
				{}
			}
		}
        newMOB.destroy();
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		Vector offensiveAffects=returnOffensiveAffects(mob,target);

		if((success)&&(offensiveAffects.size()>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> feel(s) lightly touched.":"^S<S-NAME> "+prayForWord(mob)+" to deliver a light unbinding touch to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.elementAt(a)).unInvoke();
				if(!Sense.stillAffectedBy(target,offensiveAffects,false))
					target.tell("You feel less constricted!");
			}
		}
		else
			this.beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF>, but nothing happens.");
		// return whether it worked
		return success;
	}
}
