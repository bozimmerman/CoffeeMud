package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.system.*;
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

public class Chant_Hibernation extends Chant
{
	public String ID() { return "Chant_Hibernation"; }
	public String name(){ return "Hibernation";}
	public String displayText(){return "(Hibernating)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	private CharState oldState=null;
	private int roundsHibernating=0;


	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(!mob.amDead())
			{
				if(mob.location()!=null)
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> end(s) <S-HIS-HER> hibernation.");
				else
					mob.tell("Your hibernation ends.");
			}
		}
	}

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if((msg.amISource(mob))
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
		&&((Util.bset(msg.sourceCode(),CMMsg.MASK_MOVE))||(Util.bset(msg.sourceCode(),CMMsg.MASK_HANDS))||(Util.bset(msg.sourceCode(),CMMsg.MASK_MOUTH))))
			unInvoke();
		return;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);
		MOB mob=(MOB)affected;

		if((msg.amISource(mob)
		&&(!Util.bset(msg.sourceMajor(),CMMsg.MASK_GENERAL))
		&&(!Util.bset(msg.sourceCode(),CMMsg.MASK_CHANNEL))
		&&(msg.sourceMajor()>0)))
		{
			if(roundsHibernating<10)
			{
				mob.tell("You can't withdraw from hibernation just yet.");
				return false;
			}
			else
				unInvoke();
		}
		return super.okMessage(myHost,msg);
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		MOB mob=(MOB)affected;

		if(tickID!=MudHost.TICK_MOB) return true;
		if(!profficiencyCheck(null,0,false)) return true;

		if((!mob.isInCombat())
		&&(Sense.isSleeping(mob)))
		{
			roundsHibernating++;
			double man=new Integer((mob.charStats().getStat(CharStats.INTELLIGENCE)+mob.charStats().getStat(CharStats.WISDOM))).doubleValue();
			mob.curState().adjMana((int)Math.round((man*.1)+(mob.envStats().level()/2)),mob.maxState());
			mob.curState().setHunger(oldState.getHunger());
			mob.curState().setThirst(oldState.getThirst());
			if(!Sense.isGolem(mob))
			{
				double hp=new Integer(mob.charStats().getStat(CharStats.CONSTITUTION)).doubleValue();
				MUDFight.postHealing(mob,mob,this,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,(int)Math.round((hp*.1)+(mob.envStats().level()/2)),null);
			}
			double move=new Integer(mob.charStats().getStat(CharStats.STRENGTH)).doubleValue();
			mob.curState().adjMovement((int)Math.round((move*.1)+(mob.envStats().level()/2)),mob.maxState());
		}
		else
		{
			unInvoke();
			return false;
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat())
		{
			mob.tell("You can't hibernate while in combat!");
			return false;
		}
		if(!Sense.isSitting(mob))
		{
			mob.tell("You must be in a sitting, restful position to hibernate.");
			return false;
		}
		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_SLEEP|CMMsg.MASK_MAGIC,"<S-NAME> begin(s) to hibernate...");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				oldState=mob.curState();
				beneficialAffect(mob,mob,Integer.MAX_VALUE-1000);
				helpProfficiency(mob);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) to hibernate, but lose(s) concentration.");

		// return whether it worked
		return success;
	}
}