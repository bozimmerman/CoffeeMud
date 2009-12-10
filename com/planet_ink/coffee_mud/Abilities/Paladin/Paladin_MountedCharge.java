package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Paladin_MountedCharge extends StdAbility
{
	public String ID() { return "Paladin_MountedCharge"; }
	public String name(){ return "Mounted Charge";}
	private static final String[] triggerStrings = {"MOUNTEDCHARGE","MCHARGE"};
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ANIMALAFFINITY;}
	public int usageType(){return USAGE_MOVEMENT;}
	public int minRange(){return 1;}
	public int maxRange(){return 99;}
	public boolean done=false;

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK))
			done=true;
		super.executeMsg(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
			if(done) unInvoke();
		return super.tick(ticking,tickID);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		int xlvl=adjustedLevel(invoker(),0);
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(4*xlvl));
		affectableStats.setArmor(affectableStats.armor()+(4*xlvl));
		affectableStats.setDamage(affectableStats.damage()+xlvl);
	}

	public int castingQuality(MOB mob, Environmental target)
	{
		if((mob!=null)&&(target!=null))
		{
			if((mob.isInCombat())&&(mob.rangeToTarget()<=0))
				return Ability.QUALITY_INDIFFERENT;
			if(mob.riding()==null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		boolean notInCombat=!mob.isInCombat();
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((mob.isInCombat())
		&&(mob.rangeToTarget()<=0))
		{
			mob.tell("You can not charge while in melee!");
			return false;
		}

		if(mob.riding()==null)
		{
			mob.tell("You must be mounted to use this skill.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MALICIOUS|CMMsg.MSG_ADVANCE,"<S-NAME> ride(s) hard at <T-NAMESELF>!");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(mob.getVictim()==target)
				{
					mob.setAtRange(0);
					target.setAtRange(0);
					beneficialAffect(mob,mob,asLevel,2);
					mob.recoverEnvStats();
					if(notInCombat)
					{
						done=true;
						CMLib.combat().postAttack(mob,target,mob.fetchWieldedItem());
					}
					else
						done=false;
					if(mob.getVictim()==null) mob.setVictim(null); // correct range
					if(target.getVictim()==null) target.setVictim(null); // correct range
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> ride(s) at <T-NAMESELF>, but miss(es).");

		// return whether it worked
		return success;
	}
}
