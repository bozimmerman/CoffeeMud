package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftedItem;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2012-2025 Bo Zimmerman

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
public class SpellCraftingSkill extends CraftingSkill
{
	@Override
	public String ID()
	{
		return "SpellCraftingSkill";
	}

	private final static String	localizedName	= CMLib.lang().L("Spell Crafting Skill");

	@Override
	public String name()
	{
		return localizedName;
	}

	public SpellCraftingSkill()
	{
		super();
	}

	protected String getCraftableSpellName(final List<String> commands)
	{
		String spellName=null;
		if((commands.size()>0))
			spellName=CMParms.combine(commands,0);
		else
		{
			final List<List<String>> recipes=loadRecipes();
			final List<String> V=recipes.get(CMLib.dice().roll(1,recipes.size(),-1));
			spellName=V.get(RecipeDriven.RCP_FINALNAME);
		}
		return spellName;
	}

	protected List<String> getCraftableSpellRow(final String spellName)
	{
		List<String> spellFound=null;
		final List<List<String>> recipes=loadRecipes();
		for(final List<String> V : recipes)
		{
			if (V.get(RecipeDriven.RCP_FINALNAME).equalsIgnoreCase(spellName))
			{
				spellFound = V;
				break;
			}
		}
		if (spellFound == null)
		{
			for (final List<String> V : recipes)
			{
				if (CMLib.english().containsString(V.get(RecipeDriven.RCP_FINALNAME), spellName))
				{
					spellFound = V;
					break;
				}
			}
		}
		if (spellFound == null)
		{
			for (final List<String> V : recipes)
			{
				if (V.get(RecipeDriven.RCP_FINALNAME).toLowerCase().indexOf(spellName.toLowerCase()) >= 0)
				{
					spellFound = V;
					break;
				}
			}
		}
		return spellFound;
	}

	protected Ability getCraftableSpellRecipeSpell(final List<String> commands)
	{
		Ability theSpell=null;
		final String spellName=getCraftableSpellName(commands);
		if(spellName!=null)
		{
			theSpell=CMClass.getAbility(commands.get(0));
			if(theSpell==null)
			{
				final List<String> spellFound=getCraftableSpellRow(spellName);
				if(spellFound!=null)
					theSpell=CMClass.getAbility(spellFound.get(RecipeDriven.RCP_FINALNAME));
			}
		}
		return theSpell;
	}

	protected int getCraftableSpellLevel(final List<String> commands, final int asLevel)
	{
		int level = 1;
		Ability theSpell=null;
		final String spellName=getCraftableSpellName(commands);
		if(spellName!=null)
		{
			if(asLevel > 0)
			{
				theSpell=CMClass.getAbility(commands.get(0));
				if(theSpell!=null)
				{
					final int lowlevel = CMLib.ableMapper().lowestQualifyingLevel(theSpell.ID());
					if(asLevel >= lowlevel)
						return asLevel;
					return lowlevel;
				}
			}
			final List<String> spellFound=getCraftableSpellRow(spellName);
			if(spellFound!=null)
				level = CMath.s_int(spellFound.get(RecipeDriven.RCP_LEVEL));
			else
			{
				theSpell=CMClass.getAbility(commands.get(0));
				if(theSpell!=null)
					level = CMLib.ableMapper().lowestQualifyingLevel(theSpell.ID());
			}
		}
		return level;
	}
}
