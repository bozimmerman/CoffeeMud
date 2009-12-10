package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Spell_ConjureNexus extends Spell
{
	public String ID() { return "Spell_ConjureNexus"; }
	public String name(){return "Conjure Nexus";}
	protected int canTargetCode(){return 0;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

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
		if(tickID==Tickable.TICKID_MOB)
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
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(CMLib.flags().canAccess(mob,R))
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

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,mob.location(),this,verbalCastCode(mob,mob.location(),auto),auto?"":"^S<S-NAME> summon(s) the Nexus of mana!^?");
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
