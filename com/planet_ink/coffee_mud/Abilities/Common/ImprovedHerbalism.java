package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftedItem;
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
   Copyright 2024-2025 Bo Zimmerman

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
public class ImprovedHerbalism extends Herbalism
{
	@Override
	public String ID()
	{
		return "ImprovedHerbalism";
	}

	private final static String localizedName = CMLib.lang().L("Improved Herbalism");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"IHBREW","IHERBALISM"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String getRecipeFilename()
	{
		return "improvedherbalism.txt";
	}

	@Override
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, int asLevel, final int autoGenerate, final boolean forceLevels, final List<CraftedItem> crafted)
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
		{
			asLevel = Math.max(0, CMath.s_int(commands.remove(commands.size()-1)));
			final String spellname = super.getCraftableSpellName(commands);
			final Ability theSpell=CMClass.getAbility(spellname);
			if(theSpell!=null)
			{
				final int lowlevel = CMLib.ableMapper().lowestQualifyingLevel(theSpell.ID());
				if(asLevel > lowlevel)
				{
					mob.tell(L("That can not be brewed below level @x1.",""+lowlevel));
					return false;
				}
			}
			final int maxLevel = mob.phyStats().level()+(2*getXLEVELLevel(mob));
			if(asLevel > maxLevel)
			{
				mob.tell(L("You can not brew a potion above level @x1.",""+maxLevel));
			}
		}
		return super.autoGenInvoke(mob, commands, givenTarget, auto, asLevel, autoGenerate, forceLevels, crafted);
	}
}
