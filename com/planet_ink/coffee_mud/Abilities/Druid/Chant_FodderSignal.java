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

public class Chant_FodderSignal extends Chant
{
	public String ID() { return "Chant_FodderSignal"; }
	public String name(){ return "Fodder Signal";}
	public String displayText(){return "(Fodder Signal)";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("The fodder signal stops flashing.");

		super.unInvoke();

	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);

		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GLOWING);
		affectableStats.setArmor(affectableStats.armor()+10);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(tickID!=MudHost.TICK_MOB) return false;
		if((affecting()!=null)&&(affecting() instanceof MOB))
		{
			MOB dummy=(MOB)affecting();
			Room room=dummy.location();
			if(room!=null)
			{
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB M=room.fetchInhabitant(i);
					if((M!=null)
					&&(M!=dummy)
					&&(M.isMonster())
					&&(!M.isInCombat())
					&&(!dummy.getGroupMembers(new HashSet()).contains(M))
					&&(Sense.canBeSeenBy(dummy,M)))
					{
						if(room.show(M,dummy,CMMsg.MASK_MOVE|CMMsg.MSG_NOISE,"<S-NAME> howl(s) in anger at <T-NAMESELF>!"))
							MUDFight.postAttack(M,dummy,M.fetchWieldedItem());
					}
				}
			}
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"A horrible angering flag is emitting from <S-NAME>!");
					maliciousAffect(mob,target,asLevel,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) at <T-NAMESELF>, but the magic fades.");
		// return whether it worked
		return success;
	}
}
