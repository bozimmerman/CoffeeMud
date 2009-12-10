package com.planet_ink.coffee_mud.Abilities.Prayers;
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
public class Prayer_Conviction extends Prayer
{
	public String ID() { return "Prayer_Conviction"; }
	public String name(){ return "Conviction";}
	public String displayText(){ return "(Conviction)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		if(invoker==null)return;

		MOB mob=(MOB)affected;
		if((mob.getWorshipCharID().length()>0)
		&&(mob.getWorshipCharID().equals(invoker().getWorshipCharID())))
		{
			int xlvl=super.getXLEVELLevel(invoker());
			if(xlvl>7) xlvl=7;
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(affectableStats.attackAdjustment()/(8-xlvl)));
			affectableStats.setDamage(affectableStats.damage()+3+xlvl);
			affectableStats.setArmor(affectableStats.armor()+(affectableStats.armor()/(8-xlvl)));
		}
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your enhanced conviction fades.");
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.getWorshipCharID().length()==0)
                return Ability.QUALITY_INDIFFERENT;
            if(target instanceof MOB)
            {
                if(!((MOB)target).getWorshipCharID().equals(mob.getWorshipCharID()))
                    return Ability.QUALITY_INDIFFERENT;
                return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_OTHERS);
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(mob.getWorshipCharID().length()==0)
		{
			mob.tell("You must worship a god for this prayer to work.");
			return false;
		}
		if(!target.getWorshipCharID().equals(mob.getWorshipCharID()))
		{
			mob.tell(target.name()+" must worship your god for this prayer to work.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> become(s) filled with conviction!":"^S<S-NAME> "+prayWord(mob)+" for <T-YOUPOSS> religious conviction!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for <T-YOUPOSS> conviction, but there is no answer.");


		// return whether it worked
		return success;
	}
}
