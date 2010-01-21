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
public class Chant_AcidRain extends Chant
{
	public String ID() { return "Chant_AcidRain"; }
	public String name(){ return "Acid Rain";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ROOMS;}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;}
    public long flags(){return Ability.FLAG_EARTHBASED;}
    
	public boolean isRaining(Room R)
	{
		if((R.getArea().getClimateObj().weatherType(R)==Climate.WEATHER_RAIN)
		||(R.getArea().getClimateObj().weatherType(R)==Climate.WEATHER_SLEET)
		||(R.getArea().getClimateObj().weatherType(R)==Climate.WEATHER_THUNDERSTORM))
			return true;
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			if(isRaining(R))
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=R.fetchInhabitant(i);
				if(M!=null)
				{
					if(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_ACID))
						CMLib.combat().postDamage(invoker(),M,this,CMLib.dice().roll(1,M.envStats().level()+(2*super.getXLEVELLevel(invoker())),1),CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,"The acid rain <DAMAGE> <T-NAME>!");
	                if((!M.isInCombat())&&(M!=invoker)&&(M.location().isInhabitant(invoker))&&(CMLib.flags().canBeSeenBy(invoker,M)))
	                    CMLib.combat().postAttack(M,invoker,M.fetchWieldedItem());
				}
			}
		}
		return true;
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(!isRaining(mob.location()))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Room target=mob.location();
		if(target==null) return false;
		if(!isRaining(target))
		{
			mob.tell("This chant requires some rain.");
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
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) to the rain.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					for(int i=0;i<target.numInhabitants();i++)
					{
						MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(mob!=M))
							mob.location().show(mob,M,CMMsg.MASK_MALICIOUS|CMMsg.TYP_OK_VISUAL,null);
					}
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"Acid rain starts pouring from the sky!");
					maliciousAffect(mob,target,asLevel,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the rain, but the magic fades.");
		// return whether it worked
		return success;
	}
}
