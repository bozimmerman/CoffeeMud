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
public class Chant_NeutralizePoison extends Chant implements MendingSkill
{
	public String ID() { return "Chant_NeutralizePoison"; }
	public String name(){ return "Neutralize Poison";}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PRESERVING;}

	public boolean supportsMending(Environmental E)
	{ 
		if(!(E instanceof MOB)) return false;
		boolean canMend=returnOffensiveAffects(E).size()>0;
		return canMend;
	}
	
	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON))
				offenders.addElement(A);
		}
		return offenders;
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(target instanceof MOB)
            {
                Vector offensiveAffects=returnOffensiveAffects(target);
                if(offensiveAffects.size()==0)
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		Vector offensiveAffects=returnOffensiveAffects(target);

		if((success)&&((offensiveAffects.size()>0)
					   ||((target instanceof Drink)&&(((Drink)target).liquidHeld()==RawMaterial.RESOURCE_POISON))))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> look(s) cleansed of any poisons.":"^S<S-NAME> chant(s) for <T-NAME> to be cleansed of poisons.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.elementAt(a)).unInvoke();
				if((target instanceof Drink)&&(((Drink)target).liquidHeld()==RawMaterial.RESOURCE_POISON))
				{
					((Drink)target).setLiquidHeld(RawMaterial.RESOURCE_FRESHWATER);
					target.baseEnvStats().setAbility(0);
				}
				if((!CMLib.flags().stillAffectedBy(target,offensiveAffects,false))
				&&(target instanceof MOB))
				{
					((MOB)target).tell("You feel much better!");
					((MOB)target).recoverCharStats();
					((MOB)target).recoverMaxState();
				}
				target.recoverEnvStats();
			}
		}
		else
			beneficialWordsFizzle(mob,target,auto?"":"<S-NAME> chant(s) for <T-NAME>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
