package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_DetectScrying extends Spell
{
	public String ID() { return "Spell_DetectScrying"; }
	public String name(){return "Detect Scrying";}
	public int quality(){ return INDIFFERENT;}
	protected int canTargetCode(){return CAN_MOBS;}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) softly to <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer str=new StringBuffer("");
				if(target.session()!=null)
				for(int s1=0;s1<Sessions.size();s1++)
				{
					Session S1=Sessions.elementAt(s1);
					if(target.session().amBeingSnoopedBy(S1))
						str.append(S1.mob().name()+" is snooping on <T-NAME>.  ");
				}
				Ability A=target.fetchEffect("Spell_Scry");
				if((A!=null)&&(A.invoker()!=null))
					str.append(A.invoker().name()+" is scrying on <T-NAME>.");
				A=target.fetchEffect("Spell_Claireaudience");
				if((A!=null)&&(A.invoker()!=null))
					str.append(A.invoker().name()+" is listening to <T-NAME>.");
				A=target.fetchEffect("Spell_Clairevoyance");
				if((A!=null)&&(A.invoker()!=null))
					str.append(A.invoker().name()+" is watching <T-NAME>.");
				if(str.length()==0)
					str.append("There doesn't seem to be anyone scrying on <T-NAME>.");
				CommonMsgs.say(mob,target,str.toString(),false,false);
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> incant(s) to <T-NAMESELF>, but the spell fizzles.");

		return success;
	}
}
