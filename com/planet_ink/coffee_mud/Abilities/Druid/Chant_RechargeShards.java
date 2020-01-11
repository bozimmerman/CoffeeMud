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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class Chant_RechargeShards extends Chant
{

	@Override
	public String ID()
	{
		return "Chant_RechargeShards";
	}

	private final static String localizedName = CMLib.lang().L("Recharge Shards");

	private static int RECHARGE_AMT = 5;

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int overrideMana()
	{
		return 100;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(!(target instanceof Wand))
		{
			mob.tell(L("You can't recharge that."));
			return false;
		}

		if((((Wand)target).getEnchantType()!=-1)
		&&(((Wand)target).getEnchantType()!=Ability.ACODE_CHANT))
		{
			mob.tell(mob,target,null,L("You can't recharge <T-NAME> with this chant."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L(auto?"<T-NAME> appear(s) recharged!":"^S<S-NAME> re-enchant(s) <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(((Wand)target).usesRemaining() >= ((Wand)target).maxUses())
				{
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glow(s) brightly then disintigrates!"));
					target.destroy();
				}
				else
				{
					boolean willBreak = false;
					if((((Wand)target).usesRemaining()+RECHARGE_AMT) > ((Wand)target).maxUses())
					{
						willBreak = true;
						((Wand)target).setUsesRemaining(((Wand)target).maxUses());
					}
					else
					{
						((Wand)target).setUsesRemaining(((Wand)target).usesRemaining()+RECHARGE_AMT);
					}
					if(!(willBreak))
					{
						mob.location().show(mob, target, CMMsg.MSG_OK_VISUAL, L("<T-NAME> glow(s) brightly!"));
					}
					else
					{
						mob.location().show(mob, target, CMMsg.MSG_OK_VISUAL, L("<T-NAME> glow(s) brightly and begins to hum.  It clearly cannot hold more magic."));
					}
				}
			}

		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) for enchanting power, but nothing happens."));

		// return whether it worked
		return success;
	}
}
