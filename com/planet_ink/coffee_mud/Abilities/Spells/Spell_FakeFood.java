package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2002-2026 Bo Zimmerman

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
public class Spell_FakeFood extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_FakeFood";
	}

	private final static String localizedName = CMLib.lang().L("Fake Food");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] skills = new String[] { "FoodPrep", "Cooking", "MasterCooking", "Baking" };

	private static Set<String> matSet = null;

	public Pair<String,Integer> getRandomCraftedFoodAndMat()
	{
		if(matSet == null)
		{
			final Set<String> matSet = new TreeSet<String>();
			for(final String skill : skills)
			{
				final Ability A = CMClass.getAbility(skill);
				if(A instanceof ItemCraftor)
				{
					for(final List<String> recipe : ((ItemCraftor)A).fetchRecipes())
					{
						for(int i=3;i<recipe.size();i++)
						{
							final String s = recipe.get(i).trim();
							if(!CMath.isInteger(s))
								matSet.add(s.toLowerCase());
						}
					}
				}
			}
			Spell_FakeFood.matSet = matSet;
		}
		int attempts = 1000;
		while(--attempts > 0)
		{
			final String skillID = skills[CMLib.dice().roll(1,  skills.length, -1)];
			final Ability A = CMClass.getAbility(skillID);
			if(A instanceof ItemCraftor)
			{
				final ItemCraftor iA = (ItemCraftor)A;
				final List<List<String>> recipes = iA.fetchRecipes();
				final List<String> recipe = recipes.get(CMLib.dice().roll(1, recipes.size(), -1));
				if((recipe.size()>1)&&(recipe.get(1).equalsIgnoreCase("food")))
				{
					String name = recipe.get(0);
					String pctName = "";
					for(int i=3;i<recipe.size();i++)
					{
						final String s = recipe.get(i).trim();
						if(!CMath.isInteger(s))
						{
							pctName = recipe.get(i);
							break;
						}
					}
					final int x=name.indexOf('%');
					if(x>=0)
						name =  new StringBuffer(name).replace(x,x+1,pctName).toString();
					name = name.toLowerCase().trim();
					if(!matSet.contains(name))
					{
						// determine mat?
						return new Pair<String, Integer>(CMStrings.capitalizeAndLower(name), Integer.valueOf(0));
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> invoke(s) a spell dramatically.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Food F=(Food)CMClass.getItem("GenFood");
				final Pair<String,Integer> randoFood = this.getRandomCraftedFoodAndMat();
				if(randoFood != null)
				{
					F.setName(randoFood.first);
					F.setDisplayText(L("@x1 sits here.",randoFood.first));
					F.setMaterial(randoFood.second.intValue());
				}
				else
				switch(CMLib.dice().roll(1,5,0))
				{
				case 1:
					F.setName(L("a shiny apple"));
					F.setDisplayText(L("A shiny red apple sits here."));
					F.setDescription(L("It looks tasty and crisp!"));
					F.setMaterial(RawMaterial.RESOURCE_APPLES);
					break;
				case 2:
					F.setName(L("a nice peach"));
					F.setDisplayText(L("A nice peach sits here."));
					F.setDescription(L("It looks tasty!"));
					F.setMaterial(RawMaterial.RESOURCE_PEACHES);
					break;
				case 3:
					F.setName(L("a big pot pie"));
					F.setDisplayText(L("A big pot pie has been left here."));
					F.setDescription(L("It sure looks good!"));
					F.setMaterial(RawMaterial.RESOURCE_MEAT);
					break;
				case 4:
					F.setName(L("a juicy steak"));
					F.setDisplayText(L("A juicy steak has been left here."));
					F.setDescription(L("It sure looks good!"));
					F.setMaterial(RawMaterial.RESOURCE_BEEF);
					break;
				case 5:
					F.setName(L("a bit of food"));
					F.setDisplayText(L("A bit of food has been left here."));
					F.setDescription(L("It sure looks good!"));
					F.setMaterial(RawMaterial.RESOURCE_MEAT);
					break;
				}
				F.setNourishment(0);
				F.setBaseValue(0);
				for(int f=0;f<5;f++)
				{
					final Food F2=(Food)F.copyOf();
					F2.recoverPhyStats();
					mob.location().addItem(F2,ItemPossessor.Expire.Resource);
					mob.location().show(mob,null,F2,CMMsg.MSG_OK_VISUAL,L("<O-NAME> appears!"));
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> dramatically attempt(s) to invoke a spell, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
