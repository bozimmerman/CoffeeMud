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
public class Prayer_MassCureDisease extends Prayer
{
	public String ID() { return "Prayer_MassCureDisease"; }
	public String name(){ return "Mass Cure Disease";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public static Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&(A instanceof DiseaseAffect))
				offenders.addElement(A);
		}
		return offenders;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,mob.location(),this,affectType(auto),auto?"A healing glow surrounds this place.":"^S<S-NAME> "+prayWord(mob)+" to cure disease here.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(Enumeration e=mob.location().getArea().getProperMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB target=R.fetchInhabitant(m);
						if(target!=null)
						{
							Vector offensiveAffects=returnOffensiveAffects(target);
							if(offensiveAffects.size()>0)
							{
								for(int a=offensiveAffects.size()-1;a>=0;a--)
									((Ability)offensiveAffects.elementAt(a)).unInvoke();
								if(!Sense.stillAffectedBy(target,offensiveAffects,false))
									target.tell("You feel much better!");
							}
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,mob.location(),auto?"":"<S-NAME> "+prayWord(mob)+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
