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

public class Prayer_Forgive extends Prayer
{
	public String ID() { return "Prayer_Forgive"; }
	public String name(){return "Forgive";}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	protected int canAffectCode(){return 0;}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Behavior B=null;
		if(mob.location()!=null) B=CoffeeUtensils.getLegalBehavior(mob.location());

		String name=Util.combine(commands,0);
		if(name.trim().length()==0)
		{
			mob.tell("Forgive whom?");
			return false;
		}
		Vector warrants=new Vector();
		if(B!=null)
		{
			warrants.addElement(new Integer(Law.MOD_GETWARRANTSOF));
			warrants.addElement(name);
			if(!B.modifyBehavior(CoffeeUtensils.getLegalObject(mob.location()),mob,warrants))
				warrants.clear();
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if((success)&&(warrants.size()>0))
		{
			FullMsg msg=new FullMsg(mob,mob.location(),this,affectType(auto),auto?"":"^S<S-NAME> "+prayForWord(mob)+" to forgive "+name+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int i=0;i<warrants.size();i++)
				{
					LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
					W.setCrime("pardoned");
					W.setOffenses(0);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> "+prayForWord(mob)+" to forgive "+name+", but nothing happens.");


		// return whether it worked
		return success;
	}
}
