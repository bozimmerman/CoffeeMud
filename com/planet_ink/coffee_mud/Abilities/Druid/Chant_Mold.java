package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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

@SuppressWarnings("rawtypes")
public class Chant_Mold extends Chant
{
	@Override public String ID() { return "Chant_Mold"; }
	@Override public String name(){ return "Mold";}
	@Override public String displayText(){return "(Mold)";}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override protected int canAffectCode(){return CAN_ITEMS;}
	@Override protected int canTargetCode(){return CAN_MOBS;}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Item)))
			return;
		final Item item=(Item)affected;
		super.unInvoke();

		if(canBeUninvoked())
			item.destroy();
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Physical target=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;
		if(((target instanceof Item)&&(!(target instanceof Food)))
		   ||(target instanceof Room)
		   ||(target instanceof Exit))
		{
			mob.tell(_("You can't cast this on @x1.",target.name(mob)));
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					if(target instanceof Item)
					{
						final Ability A=CMClass.getAbility("Disease_Lockjaw");
						if(A!=null)
						{
							A.setInvoker(mob);
							target.addNonUninvokableEffect(A);
						}
						maliciousAffect(mob,target,asLevel,(CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH)*3),-1);
					}
					else
					if(target instanceof MOB)
					for(int i=0;i<((MOB)target).numItems();i++)
					{
						final Item I=((MOB)target).getItem(i);
						if((I!=null)&&(I instanceof Food))
						{
							final Ability A=CMClass.getAbility("Disease_Lockjaw");
							if(A!=null)
							{
								A.setInvoker(mob);
								I.addNonUninvokableEffect(A);
							}
							maliciousAffect(mob,I,asLevel,(CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH)*3),-1);
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades."));
		// return whether it worked
		return success;
	}
}
