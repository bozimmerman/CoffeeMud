package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_FenceLoot extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_FenceLoot";
	}

	private final static String localizedName = CMLib.lang().L("Fence Loot");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings = I(new String[] { "FENCE", "FENCELOOT" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	protected Map<Item,Ability> addBackMap=new HashMap<Item,Ability>();
	
	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_SELL)
		&&(msg.tool() instanceof Item))
		{
			Ability A=((Item)msg.tool()).fetchEffect("Prop_PrivateProperty");
			if(A!=null)
			{
				((Item)msg.tool()).delEffect(A);
				addBackMap.put((Item)msg.tool(), A);
			}
		}
		return super.okMessage(myHost, msg);
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			mob.tell(L("You must specify an item to fence, and possibly a ShopKeeper (unless it is implied)."));
			return false;
		}

		commands.add(0,"SELL"); // will be instantly deleted by parseshopkeeper
		final Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,L("Fence what to whom?"));
		if(shopkeeper==null)
			return false;
		if(commands.size()==0)
		{
			mob.tell(L("Fence what?"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,shopkeeper,this,CMMsg.MSG_SPEAK,auto?"":L("<S-NAME> fence(s) stolen loot to <T-NAMESELF>."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				addBackMap.clear();
				final Ability A=(Ability)this.copyOf();
				A.setSavable(false);
				mob.addEffect(A);
				try
				{
					mob.recoverCharStats();
					commands.add(0,CMStrings.capitalizeAndLower("SELL"));
					mob.doCommand(commands,MUDCmdProcessor.METAFLAG_FORCED);
					commands.add(shopkeeper.name());
				}
				finally
				{
					mob.delEffect(A);
					mob.recoverCharStats();
				}
				for(Item I : addBackMap.keySet())
				{
					if(mob.isMine(I))
					{
						I.addEffect(addBackMap.get(I));
					}
				}
				addBackMap.clear();
				mob.recoverCharStats();
			}
		}
		else
			beneficialWordsFizzle(mob,shopkeeper,L("<S-NAME> attempt(s) to fence stolen loot to <T-NAMESELF>, but make(s) <T-HIM-HER> too nervous."));

		// return whether it worked
		return success;
	}
}
