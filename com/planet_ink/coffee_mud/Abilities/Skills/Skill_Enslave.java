package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Skill_Enslave extends StdAbility
{
	public String ID() { return "Skill_Enslave"; }
	public String name(){ return "Enslave";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"ENSLAVE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
	public boolean isAutoInvoked(){return false;}
	public int classificationCode(){return Ability.PROPERTY;}

	public EnglishParser.geasStep STEP=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((STEP!=null)&&(STEP.que!=null)&&(STEP.que.size()==0))
				mob.tell("You have completed your masters task.");
			else
				mob.tell("You have been released from your masters task.");

			if((mob.isMonster())
			&&(!mob.amDead())
			&&(mob.location()!=null)
			&&(mob.location()!=mob.getStartRoom()))
				MUDTracker.wanderAway(mob,true,true);
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0))
			MUDFight.postPanic(mob,msg);
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(STEP!=null)
		&&(msg.sourceMessage()!=null)
		&&((msg.target()==null)||(msg.target() instanceof MOB))
		&&(msg.sourceMessage().length()>0))
		{
			int start=msg.sourceMessage().indexOf("'");
			int end=msg.sourceMessage().lastIndexOf("'");
			if((start>0)&&(end>(start+1)))
				STEP.sayResponse(msg.source(),(MOB)msg.target(),msg.sourceMessage().substring(start+1,end));
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		if((tickID==MudHost.TICK_MOB)&&(STEP!=null))
		{
			if((STEP.que!=null)&&(STEP.que.size()==0))
			{
				if(((MOB)ticking).isInCombat())
					return true; // let them finish fighting.
				unInvoke();
				return !canBeUninvoked();
			}
			if(STEP.que!=null)	STEP.step();
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isMonster())
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> sigh(s).");
			CommonMsgs.say(mob,null,"You know, if I had any ambitions, I would enslave myself so I could do interesting things!",false,false);
			return false;
		}

		if(commands.size()<2)
		{
			mob.tell("You need to specify a target creature.");
			return false;
		}
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target.charStats().getStat(CharStats.INTELLIGENCE)<5)
		{
			mob.tell(target.name()+" is too stupid to understand your instructions!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISE,auto?"":"^S<S-NAME> enslave(s) <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Ability A=(Ability)copyOf();
				A.setMiscText(Util.combine(commands,0));
				target.addNonUninvokableEffect(A);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to enslave on <T-NAMESELF>, but fails.");

		// return whether it worked
		return success;
	}
}
