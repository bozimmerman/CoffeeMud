package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Flank extends ThiefSkill
{
	public String ID() { return "Thief_Flank"; }
	public String name(){ return "Flank";}
	public String displayText(){ return "(Flanking)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"FLANK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT;}
	protected MOB target=null;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDamage(affectableStats.damage()+5);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+50);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB))||(target==null))
			return false;
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob.location()!=target.location())
			unInvoke();
		if(mob.getVictim()!=target)
			unInvoke();
		if(mob.rangeToTarget()>0)
			unInvoke();
		if(target.getVictim()==mob)
			unInvoke();
		return true;
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(target==null))
			return true;

		MOB mob=(MOB)affected;
		if(mob.location()!=target.location())
			unInvoke();
		if(mob.getVictim()!=target)
			unInvoke();
		if(mob.rangeToTarget()>0)
			unInvoke();
		if(target.getVictim()==mob)
			unInvoke();
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((canBeUninvoked())&&(mob!=null)&&(target!=null)&&(!mob.amDead()))
			mob.tell("You are no longer flanking "+target.name()+".");
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to flank!");
			return false;
		}
		MOB target=mob.getVictim();
		if(target.getVictim()==mob)
		{
			mob.tell("You can't flank someone who is attacking you!");
			return false;
		}

		if((!Sense.aliveAwakeMobile(mob,true)||(Sense.isSitting(mob))))
		{
			mob.tell("You need to stand up!");
			return false;
		}

		if(mob.rangeToTarget()>0)
		{
			mob.tell("You are too far away to flank "+mob.getVictim().name()+"!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_THIEF_ACT,auto?"":"<S-NAME> flank(s) <T-NAMESELF>!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target=mob.getVictim();
				beneficialAffect(mob,mob,asLevel,0);
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to flank <T-NAMESELF>, but flub(s) it.");
		return success;
	}
}
