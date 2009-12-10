package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Chant_Feralness extends Chant
{
	public String ID() { return "Chant_Feralness"; }
	public String name(){ return "Feralness";}
	public String displayText(){return "(Feralness)";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ANIMALAFFINITY;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	int hpAdjustment=0;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).charStats().getMyRace()!=((MOB)affected).baseCharStats().getMyRace()))
		{
			int adjLvl=adjustedLevel(invoker(),0);
			int xlvl=getXLEVELLevel(invoker());
			double bonus=CMath.mul(0.1,xlvl);
			if((((MOB)affected).fetchWieldedItem()==null))
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + (2*adjLvl));
				affectableStats.setDamage(affectableStats.damage()*(2+xlvl));
			}
			affectableStats.setDamage(affectableStats.damage()+(int)Math.round(CMath.div(affectableStats.damage(),4.0-bonus)));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(CMath.div(affectableStats.attackAdjustment(),4.0-bonus)));
			affectableStats.setArmor(affectableStats.armor()+adjLvl);
		}
	}

	public void affectCharState(MOB affectedMOB, CharState affectedMaxState)
	{
		super.affectCharState(affectedMOB,affectedMaxState);
		if((affected instanceof MOB)&&(((MOB)affected).charStats().getMyRace()!=((MOB)affected).baseCharStats().getMyRace()))
			affectedMaxState.setHitPoints(affectedMaxState.getHitPoints()+hpAdjustment+(2*getXLEVELLevel(invoker())));
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected instanceof MOB)
		&&(!Druid_ShapeShift.isShapeShifted((MOB)affected)))
			unInvoke();
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		int lostpoints=mob.maxState().getHitPoints()-mob.curState().getHitPoints();
		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.tell("You don't feel quite so feral.");
			if(lostpoints>=mob.curState().getHitPoints())
				mob.curState().setHitPoints(1);
			else
				mob.curState().adjHitPoints(-lostpoints,mob.maxState());
		}
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                if(Druid_ShapeShift.isShapeShifted(((MOB)target)))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already feral.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"<T-NAME> go(es) feral!":"^S<S-NAME> chant(s) to <S-NAMESELF> and become(s) feral!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!Druid_ShapeShift.isShapeShifted(mob))
				{
					Ability A=mob.fetchAbility("Druid_ShapeShift");
					if(A!=null) A.invoke(mob,new Vector(),null,false,asLevel);
				}
				if(!Druid_ShapeShift.isShapeShifted(mob))
				{
					mob.tell("You failed to shapeshift.");
					return false;
				}
				hpAdjustment=(int)Math.round(CMath.div(target.maxState().getHitPoints(),5.0));
				success=beneficialAffect(mob,target,asLevel,0);
				target.curState().setHitPoints(target.curState().getHitPoints()+hpAdjustment);
				target.recoverMaxState();
				target.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-NAMESELF>, but nothing happens");

		// return whether it worked
		return success;
	}
}
