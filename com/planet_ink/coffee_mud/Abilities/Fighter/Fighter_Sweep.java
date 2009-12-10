package com.planet_ink.coffee_mud.Abilities.Fighter;
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
public class Fighter_Sweep extends FighterSkill
{
	public String ID() { return "Fighter_Sweep"; }
	public String name(){ return "Sweep";}
	private static final String[] triggerStrings = {"SWEEP"};
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_DIRTYFIGHTING;}
	public int usageType(){return USAGE_MOVEMENT;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		
		float f=(float)CMath.mul(0.1,(float)getXLEVELLevel(invoker()));
		affectableStats.setAttackAdjustment((int)Math.round(CMath.div(affectableStats.attackAdjustment(),2.0-f)));
		affectableStats.setDamage((int)Math.round(CMath.div(affectableStats.damage(),3.0-f)));
	}
	public int castingQuality(MOB mob, Environmental target)
	{
		if((mob!=null)&&(target!=null))
		{
			HashSet h=properTargets(mob,target,false);
			for(Iterator e=((HashSet)h.clone()).iterator();e.hasNext();)
			{
				MOB m=(MOB)e.next();
				if((m.rangeToTarget()<0)||(m.rangeToTarget()>0))
					h.remove(m);
			}
			if(h.size()<2) return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to sweep!");
			return false;
		}
		if(!mob.isInCombat())
		{
			mob.tell("You must be in combat to sweep!");
			return false;
		}
		HashSet h=properTargets(mob,givenTarget,false);
		for(Iterator e=((HashSet)h.clone()).iterator();e.hasNext();)
		{
			MOB m=(MOB)e.next();
			if((m.rangeToTarget()<0)||(m.rangeToTarget()>0))
				h.remove(m);
		}

		if(h.size()==0)
		{
			mob.tell("There aren't enough enough targets in range!");
			return false;
		}

		Item w=mob.fetchWieldedItem();
		if((w==null)||(!(w instanceof Weapon)))
		{
			mob.tell("You need a weapon to sweep!");
			return false;
		}
		Weapon wp=(Weapon)w;
		if(wp.weaponType()!=Weapon.TYPE_SLASHING)
		{
			mob.tell("You cannot sweep with "+wp.name()+"!");
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
            CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"^F^<FIGHT^><S-NAME> sweep(s)!^</FIGHT^>^?");
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
                mob.location().send(mob,msg);
				invoker=mob;
				mob.addEffect(this);
				mob.recoverEnvStats();
				for(Iterator e=h.iterator();e.hasNext();)
				{
					MOB target=(MOB)e.next();
					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_OK_ACTION|(auto?CMMsg.MASK_ALWAYS:0),null);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						CMLib.combat().postAttack(mob,target,w);
					}
				}
				mob.delEffect(this);
				mob.recoverEnvStats();
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> fail(s) to sweep.");

		// return whether it worked
		return success;
	}
}
