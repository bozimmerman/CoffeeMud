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
public class Spell_Dream extends Spell
{
	public String ID() { return "Spell_Dream"; }
	public String name(){return "Dream";}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{

		if(commands.size()<1)
		{
			mob.tell("Invoke a dream about what?");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),"^S<S-NAME> invoke(s) a dreamy spell.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if(Sense.canAccess(mob,R))
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB inhab=R.fetchInhabitant(i);
						if((inhab!=null)&&(Sense.isSleeping(inhab)))
						{
							msg=new FullMsg(mob,inhab,this,affectType(auto),null);
							if(R.okMessage(mob,msg))
								inhab.tell("You dream "+Util.combine(commands,0)+".");
						}
					}
				}
			}

		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to invoke a dream, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}
