package com.planet_ink.coffee_mud.Abilities.Paladin;
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

public class Paladin_Defend extends StdAbility
{
	public String ID() { return "Paladin_Defend"; }
	public String name(){ return "All Defence";}
	private static final String[] triggerStrings = {"DEFENCE"};
	public int quality(){return Ability.OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean fullRound=false;
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(invoker==null))
			return true;

		MOB mob=(MOB)affected;
		if(invoker.location()!=mob.location())
			unInvoke();
		else
		{
			// preventing distracting player from doin anything else
			if(msg.amISource(invoker)
			&&(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK))
			{
				invoker.location().show(invoker,msg.target(),CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> defend(s) <S-HIM-HERSELF> against <T-NAME>.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected==null)||(!(affected instanceof MOB))||(invoker==null))
			return;
		if((msg.amITarget(affected))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Weapon))
			fullRound=false;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor() - 20);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==MudHost.TICK_MOB)
		{
			if(fullRound)
			{
				MOB mob=(MOB)affected;
				if(!mob.isInCombat())
					unInvoke();
				if(mob.location()!=null)
				{
					if(mob.location().show(mob,null,this,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> successful defence <S-HAS-HAVE> allowed <S-HIM-HER> to disengage."))
					{
						MOB victim=mob.getVictim();
						if((victim!=null)&&(victim.getVictim()==mob))
							victim.makePeace();
						mob.makePeace();
						unInvoke();
					}
				}
			}
			fullRound=true;
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!Sense.aliveAwakeMobile(mob,false))
			return false;

		Ability A=mob.fetchEffect(ID());
		if(A!=null)
		{
			A.unInvoke();
			mob.tell("You end your all-out defensive posture.");
			return true;
		}
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to defend!");
			return false;
		}

		if((!auto)&&(mob.getAlignment()<650))
		{
			mob.tell("You don't feel worthy of a good defense.");
			return false;
		}
		if(!super.invoke(mob,commands,mob,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_CAST_SOMANTIC_SPELL,"^S<S-NAME> assume(s) an all-out defensive posture.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				fullRound=false;
				beneficialAffect(mob,mob,Integer.MAX_VALUE);
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to assume an all-out defensive posture, but fail(s).");


		// return whether it worked
		return success;
	}
}