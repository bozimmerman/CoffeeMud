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
   Copyright 2017-2018 Bo Zimmerman

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

public class Skill_SecretWriting extends Skill_Write
{
	@Override
	public String ID()
	{
		return "Skill_SecretWriting";
	}

	private final static String	localizedName	= CMLib.lang().L("Secret Writing");

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

	private static final String[]	triggerStrings	= I(new String[] { "SECRETWRITING", "SECRETWRITE", "SWRITE", "SWR" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_CALLIGRAPHY;
	}

	@Override
	public int overrideMana()
	{
		return Ability.COST_NORMAL;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final String inkID = "InvisibleInk";
		final boolean delEffect=mob.fetchEffect(inkID) == null;
		Language setSpeakingL=null;
		Language notSpeakingL=null;
		try
		{
			if(delEffect)
			{
				final Ability A=CMClass.findAbility(inkID);
				if(A instanceof Language)
				{
					A.setProficiency(proficiency);
					mob.addNonUninvokableEffect(A);
				}
			}
			notSpeakingL = (Language)mob.fetchEffect(inkID);
			if(notSpeakingL != null)
				notSpeakingL.setBeingSpoken(notSpeakingL.ID(), true);
			setSpeakingL = CMLib.utensils().getLanguageSpoken(mob);
			if((setSpeakingL != null)&&(!setSpeakingL.ID().equals(inkID)))
				setSpeakingL.setBeingSpoken(setSpeakingL.ID(), false);
			
			return super.invoke(mob, commands, givenTarget, auto, asLevel);
		}
		finally
		{
			if(notSpeakingL != null)
				notSpeakingL.setBeingSpoken(notSpeakingL.ID(), false);
			if(delEffect)
			{
				Ability A=mob.fetchEffect(inkID);
				if(A!=null)
					mob.delEffect(A);
			}
			if(setSpeakingL != null)
				setSpeakingL.setBeingSpoken(setSpeakingL.ID(), true);
		}
	}
}