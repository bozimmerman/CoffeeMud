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

public class Chant_ControlPlant extends Chant
{
	public String ID() { return "Chant_ControlPlant"; }
	public String name(){ return "Control Plant";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}

	public static Ability isPlant(Item I)
	{
		if((I!=null)&&(I.rawSecretIdentity().length()>0))
		{
			for(int a=0;a<I.numEffects();a++)
			{
				Ability A=I.fetchEffect(a);
				if((A!=null)
				&&(A.invoker()!=null)
				&&(A instanceof Chant_SummonPlants))
					return A;
			}
		}
		return null;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item myPlant=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(myPlant==null) return false;

		if(isPlant(myPlant)==null)
		{
			mob.tell("You can't control "+myPlant.name()+".");
			return false;
		}

		if(myPlant.rawSecretIdentity().equals(mob.Name()))
		{
			mob.tell("You already control "+myPlant.name()+".");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,myPlant,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=isPlant(myPlant);
				if(A!=null)	A.setInvoker(mob);
				mob.tell("You wrest control of "+myPlant.name()+" from "+myPlant.secretIdentity()+".");
				myPlant.setSecretIdentity(mob.Name());
			}

		}
		else
			beneficialVisualFizzle(mob,myPlant,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
