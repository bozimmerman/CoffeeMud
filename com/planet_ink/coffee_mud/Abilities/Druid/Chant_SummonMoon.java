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
public class Chant_SummonMoon extends Chant
{
	public String ID() { return "Chant_SummonMoon"; }
	public String name(){ return "Summon Moon";}
	public String displayText(){return "(Summon Moon)";}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_MOONSUMMONING;}
	public long flags(){return FLAG_WEATHERAFFECTING;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null) return;
		if(canBeUninvoked())
		{
			Room R=CMLib.map().roomLocation(affected);
			if((R!=null)&&(CMLib.flags().isInTheGame(affected,true)))
				R.showHappens(CMMsg.MSG_OK_VISUAL,"The summoned moon sets.");
		}
		super.unInvoke();

	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			if((R.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_DUSK)
			&&(R.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_NIGHT))
				unInvoke();
		}
		return true;
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            Room R=mob.location();
            if((R!=null)&&(!R.getArea().getClimateObj().canSeeTheMoon(R,null)))
            {
                if((R.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_DUSK)
                &&(R.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_NIGHT))
                    return Ability.QUALITY_INDIFFERENT;
                if((R.domainType()&Room.INDOORS)==0)
                    return Ability.QUALITY_INDIFFERENT;
                if(R.fetchEffect(ID())!=null)
                    return Ability.QUALITY_INDIFFERENT;
                return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if((target.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_DUSK)
		&&(target.getArea().getTimeObj().getTODCode()!=TimeClock.TIME_NIGHT))
		{
			mob.tell("You can only start this chant at night.");
			return false;
		}
		if((target.domainType()&Room.INDOORS)==0)
		{
			mob.tell("This chant only works indoors.");
			return false;
		}

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already under the summoned moon.");
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
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to the sky.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The Moon pierces into the room!");
					beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the sky, but the magic fades.");
		// return whether it worked
		return success;
	}
}
