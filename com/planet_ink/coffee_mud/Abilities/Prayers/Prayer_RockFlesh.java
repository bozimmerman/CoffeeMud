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
   Copyright 2003-2025 Bo Zimmerman

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
public class Prayer_RockFlesh extends Prayer implements MendingSkill
{
	@Override
	public String ID()
	{
		return "Prayer_RockFlesh";
	}

	private final static String localizedName = CMLib.lang().L("Rock Flesh");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_RESTORATION;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean supportsMending(final Physical item)
	{
		if(!(item instanceof MOB))
			return false;
		return (item.fetchEffect("Spell_FleshStone")!=null)||(item.fetchEffect("Prayer_FleshRock")!=null);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{

		final Physical target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)
			return false;

		Ability revokeThis=null;
		for(int a=0;a<target.numEffects();a++) // personal affects
		{
			final Ability A=target.fetchEffect(a);
			if((A!=null)&&(A.canBeUninvoked())
			   &&((A.ID().equalsIgnoreCase("Spell_FleshStone"))
				  ||(A.ID().equalsIgnoreCase("Prayer_FleshRock"))))
			{
				revokeThis=A;
				break;
			}
		}

		if(revokeThis==null)
		{
			if(auto)
				mob.tell(L("Nothing happens."));
			else
				mob.tell(mob,target,null,L("<T-NAME> can not be affected by this prayer."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?"":L("^S<S-NAME> @x1 to dispel @x2 from <T-NAMESELF>.^?",prayForWord(mob),revokeThis.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				revokeThis.unInvoke();
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 on <T-YOUPOSS> behalf, but flub(s) it.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
