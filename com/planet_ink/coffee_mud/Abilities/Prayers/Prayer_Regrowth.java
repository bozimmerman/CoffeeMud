package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Misc.Amputation;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2004 Jeremy Vyska</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 * <p>Company: http://www.falserealities.com</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class Prayer_Regrowth extends Prayer
{
	public String ID() { return "Prayer_Regrowth"; }
	public String name(){ return "Regrowth";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_HEALING;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	private static Vector limbsToRegrow = null;

	public Prayer_Regrowth()
	{
		if(limbsToRegrow==null)
		{
			limbsToRegrow = new Vector();
			limbsToRegrow.addElement("EYE");
			limbsToRegrow.addElement("LEG");
			limbsToRegrow.addElement("FOOT");
			limbsToRegrow.addElement("ARM");
			limbsToRegrow.addElement("HAND");
			limbsToRegrow.addElement("EAR");
			limbsToRegrow.addElement("NOSE");
			limbsToRegrow.addElement("TAIL");
			limbsToRegrow.addElement("WING");
			limbsToRegrow.addElement("ANTENEA");
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)return false;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		    return false;
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) surrounded by a bright light.":"^S<S-NAME> "+prayWord(mob)+" over <T-NAMESELF> for restorative healing.^?");
			if(mob.location().okMessage(mob,msg))
			{
		        mob.location().send(mob,msg);
		        Ability A=target.fetchEffect("Amputation");
		        if(A!=null)
		        {
					Amputation Amp=(Amputation)A;
					Vector missing = Amp.missingLimbNameSet();
					String LookingFor = null;
					boolean found = false;
					String missLimb=null;
					for(int i=0;i<limbsToRegrow.size();i++)
					{
						LookingFor = (String)limbsToRegrow.elementAt(i);
						for(int j=0;j<missing.size();j++)
						{
							missLimb = (String)missing.elementAt(j);
							if(missLimb.toUpperCase().indexOf(LookingFor)>=0)
							{
								found = true;
								break;
							}
						}
						if(found) break;
					}
					if((found)&&(missLimb!=null))
						Amp.unamputate(target, Amp, missLimb.toLowerCase());
					target.recoverCharStats();
					target.recoverEnvStats();
					target.recoverMaxState();
		        }
				mob.location().recoverRoomStats();
			}
		}
		else
		    beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" over <T-NAMESELF>, but "+hisHerDiety(mob)+" does not heed.");
		// return whether it worked
		return success;
	}
}
