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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2022-2024 Bo Zimmerman

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
public class Thief_SilentWear extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_SilentWear";
	}

	private final static String localizedName = CMLib.lang().L("Silent Wear");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
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
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;
	}

	private static final String[] triggerStrings =I(new String[] {"SILENTWEAR","SWEAR"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	public int code=0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(final int newCode)
	{
		code=newCode;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("What would you like to wear?"));
			return false;
		}
		final Item item=super.getTarget(mob,null,givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(item==null)
			return false;
		if((item.rawProperLocationBitmap()&(Wearable.WORN_WIELD|Wearable.WORN_WIELD))==0)
		{
			mob.tell(L("That must be held or wielded, not worn."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		try
		{
			commands.add(0, "WEAR");
			if(success)
			{
				final CMMsg msg=CMClass.getMsg(mob,item,this,CMMsg.MSG_THIEF_ACT,L("<S-NAME> wear(s) <T-NAME>."),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					CMClass.getCommand("Wear").execute(mob, commands, MUDCmdProcessor.METAFLAG_QUIETLY);
				}
			}
			else
			{
				beneficialVisualFizzle(mob,item,L("<S-NAME> attempt(s) to wear <T-NAME> quietly, but fail(s)."));
				CMClass.getCommand("Wear").execute(mob, commands, 0);
			}
		}
		catch(final IOException e)
		{
		}
		return success;
	}
}
