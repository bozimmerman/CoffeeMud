package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_SummonMarker extends Spell
{
	public String ID() { return "Spell_SummonMarker"; }
	public String name(){return "Summon Marker";}
	protected int canTargetCode(){return 0;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public void unInvoke()
	{

		if((canBeUninvoked())&&(invoker()!=null)&&(affected!=null)&&(affected instanceof Room))
			invoker().tell("Your marker in '"+((Room)affected).displayText()+"' dissipates.");
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if(Sense.canAccess(mob,R))
			for(int a=0;a<R.numEffects();a++)
			{
				Ability A=R.fetchEffect(a);
				if((A!=null)
				   &&(A.ID().equals(ID()))
				   &&(A.invoker()==mob))
				{
					A.unInvoke();
					break;
				}
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,mob.location(),this,affectType(auto),auto?"":"^S<S-NAME> summon(s) <S-HIS-HER> marker energy to this place!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,mob.location(),CMMsg.MSG_OK_VISUAL,"The spot <S-NAME> pointed to glows for brief moment.");
				beneficialAffect(mob,mob.location(),0,(adjustedLevel(mob,asLevel)*240)+450);
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to summon <S-HIS-HER> marker energy, but fail(s).");


		// return whether it worked
		return success;
	}
}
