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
public class Spell_ConjureNexus extends Spell
{
	public String ID() { return "Spell_ConjureNexus"; }
	public String name(){return "Conjure Nexus";}
	protected int canTargetCode(){return 0;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	public void unInvoke()
	{
		if((canBeUninvoked())&&(invoker()!=null)&&(affected!=null)&&(affected instanceof Room))
			invoker().tell("The Nexus in '"+((Room)affected).displayText()+"' dissipates.");
		super.unInvoke();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof Room)))
			return false;
		Room R=(Room)affected;
		if(tickID==MudHost.TICK_MOB)
		for(int m=0;m<R.numInhabitants();m++)
		{
			MOB mob=R.fetchInhabitant(m);
			if(mob!=null)
			{
				int oldHP=mob.curState().getHitPoints();
				int oldMV=mob.curState().getMovement();
				int oldHU=mob.curState().getHunger();
				int oldTH=mob.curState().getThirst();
				mob.curState().recoverTick(mob,mob.maxState());
				mob.curState().setHitPoints(oldHP);
				mob.curState().setMovement(oldMV);
				mob.curState().setHunger(oldHU);
				mob.curState().setThirst(oldTH);
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
	    try
	    {
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(Sense.canAccess(mob,R))
				for(int a=0;a<R.numEffects();a++)
				{
					Ability A=R.fetchEffect(a);
					if((A!=null)&&(A.ID().equals(ID())))
					{
						A.unInvoke();
						break;
					}
				}
			}
	    }catch(NoSuchElementException nse){}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,mob.location(),this,affectType(auto),auto?"":"^S<S-NAME> summon(s) the Nexus of mana!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob.location(),asLevel,0);
			}

		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to summon a Nexus, but fail(s).");


		// return whether it worked
		return success;
	}
}
