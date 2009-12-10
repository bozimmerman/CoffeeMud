package com.planet_ink.coffee_mud.Abilities.Misc;
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
public class Undead_EnergyDrain extends StdAbility
{
	public String ID() { return "Undead_EnergyDrain"; }
	public String name(){ return "Energy Drain";}
	public String displayText(){ return "(Drained of Energy)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"DRAINENERGY"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	public int levelsDown=1;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(levelsDown<0) return;
		int attacklevel=affectableStats.attackAdjustment()/affectableStats.level();
		affectableStats.setLevel(affectableStats.level()-levelsDown);
		if(affectableStats.level()<=0)
		{
			levelsDown=-1;
			CMLib.combat().postDeath(invoker(),(MOB)affected,null);
		}
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(attacklevel*levelsDown));
	}

	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		if(affected==null) return;
		int hplevel=affectableState.getHitPoints()/affected.baseEnvStats().level();
		affectableState.setHitPoints(affectableState.getHitPoints()-(hplevel*levelsDown));
		int manalevel=affectableState.getMana()/affected.baseEnvStats().level();
		affectableState.setMana(affectableState.getMana()-(manalevel*levelsDown));
		int movelevel=affectableState.getMovement()/affected.baseEnvStats().level();
		affectableState.setMovement(affectableState.getMovement()-(movelevel*levelsDown));
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setClassLevel(affectableStats.getCurrentClass(),baseEnvStats().level()-levelsDown-affectableStats.combinedSubLevels());
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("The energy drain is lifted.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=null;
		Ability reAffect=null;
		if(mob.isInCombat())
		{
			if(mob.rangeToTarget()>0)
			{
				mob.tell("You are too far away to touch!");
				return false;
			}
			MOB victim=mob.getVictim();
				reAffect=victim.fetchEffect("Undead_WeakEnergyDrain");
			if(reAffect==null)
				reAffect=victim.fetchEffect("Undead_EnergyDrain");
			if(reAffect!=null) target=victim;
		}
		if(target==null)
			target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			str=auto?"":"^S<S-NAME> extend(s) an energy draining hand to <T-NAMESELF>!^?";
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_UNDEAD|(auto?CMMsg.MASK_ALWAYS:0),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> drained!");
					if(reAffect!=null)
					{
						if(reAffect instanceof Undead_EnergyDrain)
							((Undead_EnergyDrain)reAffect).levelsDown++;
						((StdAbility)reAffect).setTickDownRemaining(((StdAbility)reAffect).getTickDownRemaining()+mob.envStats().level());
						mob.recoverEnvStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
					}
					else
						success=maliciousAffect(mob,target,asLevel,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to drain <T-NAMESELF>, but fail(s).");

		return success;
	}
}
