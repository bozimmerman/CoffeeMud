package com.planet_ink.coffee_mud.Abilities.Skills;
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

public class Skill_Haggle extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_Haggle";
	}

	private final static String localizedName = CMLib.lang().L("Haggle");

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

	private static final String[] triggerStrings =I(new String[] {"HAGGLE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB,affectableStats);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+10+getXLEVELLevel(invoker()));
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String cmd="";
		if(commands.size()>0)
			cmd=(commands.get(0)).toUpperCase();

		if((commands.size()<2)||((!cmd.equals("BUY")&&(!cmd.equals("SELL")))))
		{
			mob.tell(L("You must specify BUY, SELL, an item, and possibly a ShopKeeper (unless it is implied)."));
			return false;
		}

		final Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,CMStrings.capitalizeAndLower(cmd)+" what to whom?");
		if(shopkeeper==null)
			return false;
		if(commands.size()==0)
		{
			mob.tell(L("@x1 what?",CMStrings.capitalizeAndLower(cmd)));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,shopkeeper,this,CMMsg.MSG_SPEAK,auto?"":L("<S-NAME> haggle(s) with <T-NAMESELF>."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				final Ability A=(Ability)this.copyOf();
				A.setSavable(false);
				try
				{
					mob.addEffect(A);
					mob.recoverCharStats();
					commands.add(0,CMStrings.capitalizeAndLower(cmd));
					mob.doCommand(commands,MUDCmdProcessor.METAFLAG_FORCED);
					commands.add(shopkeeper.name());
				}
				finally
				{
					mob.delEffect(A);
				}
				mob.recoverCharStats();
			}
		}
		else
			beneficialWordsFizzle(mob,shopkeeper,L("<S-NAME> haggle(s) with <T-NAMESELF>, but <S-IS-ARE> unconvincing."));

		// return whether it worked
		return success;
	}
}
