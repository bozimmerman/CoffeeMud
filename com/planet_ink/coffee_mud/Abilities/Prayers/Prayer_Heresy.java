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

public class Prayer_Heresy extends Prayer
{
	public String ID() { return "Prayer_Heresy"; }
	public String name(){return "Heresy";}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	protected int canAffectCode(){return 0;}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int overrideMana(){return 100;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Behavior B=null;
		if(mob.location()!=null) B=CoffeeUtensils.getLegalBehavior(mob.location());

		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		MOB oldVictim=mob.getVictim();
		if((success)&&(B!=null))
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> accuse(s) <T-NAMESELF> of heresy"+againstTheGods(mob)+"!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					MOB D=null;
					if(mob.getWorshipCharID().length()>0) D=CMMap.getDeity(mob.getWorshipCharID());
					if(D==null)
					{
						D=CMClass.getMOB("StdDeity");
						D.setName("the gods");
					}
					Vector V=new Vector();
					V.addElement(new Integer(Law.MOD_ADDWARRANT));
					V.addElement(D);//victim first
					V.addElement("");//crime locs
					V.addElement("");//crime flags
					V.addElement("heresy against <T-NAME>");//the crime
					int low=CMAble.lowestQualifyingLevel(ID());
					int me=CMAble.qualifyingClassLevel(mob,this);
					int lvl=(me-low)/5;
					if(lvl<0) lvl=0;
					if(lvl>Law.ACTION_HIGHEST) lvl=Law.ACTION_HIGHEST;
					V.addElement(Law.ACTION_DESCS[lvl]);//sentence
					V.addElement("Angering "+D.name()+" will bring doom upon us all!");
					B.modifyBehavior(CoffeeUtensils.getLegalObject(mob.location()),target,V);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> accuse(s) <T-NAMESELF> of heresy"+againstTheGods(mob)+", but nothing happens.");
		mob.setVictim(oldVictim);
		if(oldVictim==null) mob.makePeace();

		// return whether it worked
		return success;
	}
}
