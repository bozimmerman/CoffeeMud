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
   Copyright 2003-2018 Bo Zimmerman

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

public class Prayer_Purify extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Purify";
	}

	private final static String	localizedName	= CMLib.lang().L("Purify");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public int classificationCode()
	{
		return ((affecting() instanceof Food) && (!canBeUninvoked())) ? Ability.ACODE_PROPERTY : Ability.ACODE_PRAYER | Ability.DOMAIN_RESTORATION;
	}

	@Override
	public void affectPhyStats(Physical affecting, PhyStats stats)
	{
		if((affecting instanceof Decayable)&&(((Decayable)affecting).decayTime()>0))
			((Decayable)affecting).setDecayTime(0);
		super.affectPhyStats(affecting,stats);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		if((!(target instanceof Food))
			&&(!(target instanceof Drink)))
		{
			mob.tell(L("You cannot purify @x1!",target.name(mob)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
									auto?"":L("^S<S-NAME> purify <T-NAMESELF>@x1.^?",inTheNameOf(mob)),
									auto?"":L("^S<S-NAME> purifies <T-NAMESELF>@x1.^?",inTheNameOf(mob)),
									auto?"":L("^S<S-NAME> purifies <T-NAMESELF>@x1.^?",inTheNameOf(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				boolean doneSomething=false;
				if((target instanceof Drink)&&(((Drink)target).liquidType()!=RawMaterial.RESOURCE_FRESHWATER))
				{
					((Drink)target).setLiquidType(RawMaterial.RESOURCE_FRESHWATER);
					doneSomething=true;
					target.basePhyStats().setAbility(0);
					target.recoverPhyStats();
				}
				if(target.numEffects()>0) // personal affects
				{
					doneSomething=true;
					target.delAllEffects(true);
				}
				if((target instanceof Pill)
				&&(!((Pill)target).getSpellList().equals("Prayer_Sober")))
				{
					doneSomething=true;
					((Pill)target).setSpellList("Prayer_Sober");
				}
				if((target instanceof Potion)
				&&(!((Potion)target).getSpellList().equals("Prayer_Sober")))
				{
					doneSomething=true;
					((Potion)target).setSpellList("Prayer_Sober");
				}
				if(doneSomething)
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 appears purified!",target.name()));
				target.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for purification, but nothing happens.",prayWord(mob)));
		// return whether it worked
		return success;
	}
}
