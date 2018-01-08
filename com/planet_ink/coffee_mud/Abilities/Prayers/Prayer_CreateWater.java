package com.planet_ink.coffee_mud.Abilities.Prayers;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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

public class Prayer_CreateWater extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_CreateWater";
	}

	private final static String localizedName = CMLib.lang().L("Create Water");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;
		if((!(target instanceof Drink))||((target.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID))
		{
			mob.tell(L("You can not create water inside @x1.",target.name(mob)));
			return false;
		}
		final Drink D=(Drink)target;
		if(D.containsDrink()&&(D.liquidType()!=RawMaterial.RESOURCE_FRESHWATER))
		{
			mob.tell(L("@x1 already contains another liquid, and must be emptied first.",target.name(mob)));
			return false;
		}
		if(D.containsDrink()&&(D.liquidRemaining()>=D.liquidHeld()))
		{
			mob.tell(L("@x1 is full.",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> @x1 over <T-NAME> for water.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().recoverPhyStats();
				D.setLiquidType(RawMaterial.RESOURCE_FRESHWATER);
				D.setLiquidRemaining(D.liquidHeld());
				if(target.owner() instanceof Room)
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 fills up with water!",target.name()));
				else
					mob.tell(L("@x1 fills up with water!",target.name(mob)));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 over <T-NAME> for water, but there is no answer.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
