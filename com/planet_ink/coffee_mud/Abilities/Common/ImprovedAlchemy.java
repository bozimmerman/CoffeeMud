package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.ItemKeyPair;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2021-2022 Bo Zimmerman

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
public class ImprovedAlchemy extends Alchemy
{
	@Override
	public String ID()
	{
		return "ImprovedAlchemy";
	}

	private final static String localizedName = CMLib.lang().L("Improved Alchemy");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"IBREW","IALCHEMY"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String parametersFile()
	{
		return "improvedalchemy.txt";
	}

	@Override
	protected int getAlchemyDuration(final MOB mob, final Ability theSpell, final int asLevel)
	{
		int duration=super.getAlchemyDuration(mob, theSpell, asLevel);
		final int adj = this.spellLevel(mob, theSpell, asLevel) - super.spellLevel(mob, theSpell, asLevel);
		if(adj > 0)
			duration += (adj*3);
		if(duration<10)
			duration=10;
		return duration;
	}

	@Override
	protected int spellLevel(final MOB mob, final Ability A, int asLevel)
	{
		if(asLevel > 0)
		{
			final int alvl = this.spellLevelAdjustment(mob, A);
			final int casterLevel = A.adjustedLevel(mob,asLevel);
			if(asLevel > casterLevel+alvl)
				asLevel = casterLevel+alvl;
			final int levelCap = mob.phyStats().level() + CMProps.getIntVar(CMProps.Int.EXPRATE);
			if(asLevel >  levelCap+alvl)
				asLevel = levelCap+alvl;
			final int lowLvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
			if(lowLvl > asLevel+alvl)
				asLevel = lowLvl+alvl;
			return asLevel;
		}
		return asLevel;
	}

	@Override
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, int asLevel, final int autoGenerate, final boolean forceLevels, final List<Item> crafted)
	{
		if(autoGenerate>0)
			return super.autoGenInvoke(mob, commands, givenTarget, auto, asLevel, autoGenerate, forceLevels, crafted);
		if(super.checkStop(mob, commands))
			return true;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,0);
		if((commands.size()>2)
		&&(!"LIST".startsWith(commands.get(0).toUpperCase()))
		&&(asLevel == 0)
		&&(CMath.isInteger(commands.get(commands.size()-1))))
			asLevel = Math.max(0, CMath.s_int(commands.remove(commands.size()-1)));
		return super.autoGenInvoke(mob, commands, givenTarget, auto, asLevel, autoGenerate, forceLevels, crafted);
	}
}
