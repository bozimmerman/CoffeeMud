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

public class Chant_SummonInsects extends Chant
{
	public String ID() { return "Chant_SummonInsects"; }
	public String name(){ return "Summon Insects";}
	public String displayText(){return "(In a swarm of insects)";}
	public int quality(){return Ability.MALICIOUS;}
	public int maxRange(){return 5;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	Room castingLocation=null;
	public long flags(){return Ability.FLAG_SUMMONING;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB vic=(MOB)affected;
			if(vic.location()!=castingLocation)
				unInvoke();
			else
			if((!vic.amDead())&&(vic.location()!=null))
				MUDFight.postDamage(invoker,vic,this,Dice.roll(1,3,0),CMMsg.TYP_OK_VISUAL,-1,"<T-NAME> <T-IS-ARE> stung by the swarm!");
		}
		return super.tick(ticking,tickID);
	}
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to escape the insect swarm!");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}

		HashSet h=properTargets(mob,givenTarget,auto);

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			if(h==null)
			{
				mob.location().show(mob,null,this,affectType(auto),auto?"A swarm of stinging insects appear, then flutter away!":"^S<S-NAME> chant(s) into the sky.  A swarm of stinging insects appear.  Finding no one to sting, they flutter away.^?");
				return false;
			}
			if(mob.location().show(mob,null,this,affectType(auto),auto?"A swarm of stinging insects appear, then flutter away!":"^S<S-NAME> chant(s) into the sky.  A swarm of stinging insects appears and attacks!^?"))
			for(Iterator f=h.iterator();f.hasNext();)
			{
				MOB target=(MOB)f.next();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				if((mob.location().okMessage(mob,msg))
				   &&(target.fetchEffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if((msg.value()<=0)&&(target.location()==mob.location()))
					{
						castingLocation=mob.location();
						success=maliciousAffect(mob,target,asLevel,(mob.envStats().level()*10),-1);
						target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> become(s) enveloped by the swarm of stinging insects!");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but the magic fizzles.");


		// return whether it worked
		return success;
	}
}
