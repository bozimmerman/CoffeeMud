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

public class Chant_Drifting extends Chant
{
	public String ID() { return "Chant_Drifting"; }
	public String name(){return "Drifting";}
	public String displayText(){return "(Drifting)";}
	public int quality(){return OK_SELF;};
	protected int canAffectCode(){return CAN_MOBS;}
	public long flags(){return Ability.FLAG_MOVING;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amISource(mob))
		{
			if((msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			||(msg.sourceMinor()==CMMsg.TYP_RETREAT))
			{
				mob.tell("You can't seem to drift accurately enough to advance or retreat!");
				return false;
			}
			else
			if((!Sense.isFalling(mob))
			&&(msg.sourceMinor()==CMMsg.TYP_ENTER)
			&&(msg.target() instanceof Room)
			&&(mob.location()!=null)
			&&((mob.location().getRoomInDir(Directions.UP)==msg.target())
			   ||(mob.location().getRoomInDir(Directions.DOWN)==msg.target())))
			{
				mob.tell("You can not seem to direct your flying that way.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected instanceof MOB)
		&&(tickID==MudHost.TICK_MOB)
		&&(!Sense.isFalling(affected))
		&&(((MOB)affected).riding()==null)
		&&(((MOB)affected).location()!=null)
		&&((((MOB)affected).location().domainType()&Room.INDOORS)==0))
		{
			Ability A=CMClass.getAbility("Falling");
			A.setAffectedOne(null);
			A.setProfficiency(100);
			A.invoke(null,null,affected,true,0);
			affected.recoverEnvStats();
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected.fetchEffect("Falling")==null)&&(!Sense.isFalling(affected)))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			Ability A=mob.fetchEffect("Falling");
			if(A!=null) A.unInvoke();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> float(s) back down.");
			CommonMsgs.stand(mob,true);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already "+name()+".");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) <S-HIM-HERSELF> off the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=beneficialAffect(mob,target,asLevel,0);
					target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> start(s) drifting up!");
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s), but fail(s) to leave the ground.");
		// return whether it worked
		return success;
	}
}
