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

public class Chant_Tether extends Chant
{
	public String ID() { return "Chant_Tether"; }
	public String name(){ return "Tether";}
	public String displayText(){ return "(Tether)";}
	public int quality(){ return BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Room tetheredTo=null;
	public Room lastRoom=null;


	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((lastRoom!=mob.location())
			&&(lastRoom!=null))
				tetheredTo=lastRoom;
			lastRoom=mob.location();

			if(msg.amISource(mob)
			&&(msg.target()==null)
			&&(msg.tool()==null)
			&&(tetheredTo!=mob.location())
			&&(msg.sourceMinor()==CMMsg.TYP_DEATH)
			&&(mob.curState().getHitPoints()>0))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> pulled back by the tether!");
				tetheredTo.bringMobHere(mob,false);
				return false;
			}
		}
		return super.okMessage(host,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your tether has left you.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((lastRoom!=mob.location())
			&&(lastRoom!=null))
				tetheredTo=lastRoom;
			lastRoom=mob.location();
			if(mob.fetchEffect("Falling")!=null)
			{
				mob.tell("The tether keeps you from falling!");
				mob.delEffect(mob.fetchEffect("Falling"));
			}
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already tethered.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) magically tethered!":"^S<S-NAME> chant(s) about a magical tether!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				lastRoom=mob.location();
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) about a magical tether, but the magic fades.");


		// return whether it worked
		return success;
	}
}
