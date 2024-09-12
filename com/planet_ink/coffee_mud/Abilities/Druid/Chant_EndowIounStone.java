package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Chant_EndowIounStone extends Chant implements ItemCraftor
{

	@Override
	public String ID()
	{
		return "Chant_EndowIounStone";
	}

	private final static String	localizedName	= CMLib.lang().L("Endow Ioun Stone");

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
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_DEEPMAGIC;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING;
	}

	public static ItemCraftor skillA = null;
	public ItemCraftor getCraftingSkill()
	{
		if(skillA == null)
		{
			final ItemCraftor A = (ItemCraftor)CMClass.getAbility("GenCraftSkill");
			if(A==null)
				return null;
			A.setStat("CLASS9", ID());
			A.setStat("TRIGSTR", "CHANT \"Endow Ioun Stones\"");
			A.setStat("FILENAME", "endowiounstone.txt");
			A.setStat("VERB", "endowing");
			A.setStat("MATLIST", "PRECIOUS");
			A.setStat("CANMEND", "true");
			A.setStat("CANREFIT", "false");
			A.setStat("CANBUNDLE", "false");
			A.setStat("SOUND", "true");
			A.setStat("CANSIT", "true");
			//A.setStat("SOUND", "true");
			skillA=A;
		}
		return skillA;
	}


	@Override
	public List<List<String>> fetchRecipes()
	{
		return getCraftingSkill().fetchRecipes();
	}

	@Override
	public String getRecipeFormat()
	{
		return getCraftingSkill().getRecipeFormat();
	}
	@Override
	public String getRecipeFilename()
	{
		return getCraftingSkill().getRecipeFilename();
	}

	@Override
	public List<String> matchingRecipeNames(final String recipeName, final boolean beLoose)
	{
		return getCraftingSkill().matchingRecipeNames(recipeName, beLoose);
	}

	@Override
	public Pair<String, Integer> getDecodedItemNameAndLevel(final List<String> recipe)
	{
		return getCraftingSkill().getDecodedItemNameAndLevel(recipe);
	}

	@Override
	public List<List<String>> fetchMyRecipes(final MOB mob)
	{
		return getCraftingSkill().fetchMyRecipes(mob);
	}

	@Override
	public List<Integer> myResources()
	{
		return getCraftingSkill().myResources();
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return getCraftingSkill().getDecodedComponentsDescription(mob, recipe);
	}

	@Override
	public CraftorType getCraftorType()
	{
		return CraftorType.Magic;
	}

	@Override
	public CraftedItem craftAnyItem(final int material)
	{
		return getCraftingSkill().craftAnyItem(material);
	}

	@Override
	public CraftedItem craftAnyItemNearLevel(final int minlevel, final int maxlevel)
	{
		return getCraftingSkill().craftAnyItemNearLevel(minlevel, maxlevel);
	}

	@Override
	public int[] getCraftableLevelRange()
	{
		return getCraftingSkill().getCraftableLevelRange();
	}

	@Override
	public List<CraftedItem> craftAllItemSets(final int material, final boolean forceLevels)
	{
		return getCraftingSkill().craftAllItemSets(material, forceLevels);
	}

	@Override
	public List<CraftedItem> craftAllItemSets(final boolean forceLevels)
	{
		return getCraftingSkill().craftAllItemSets(forceLevels);
	}

	@Override
	public CraftedItem craftItem(final String recipeName)
	{
		return getCraftingSkill().craftItem(recipeName);
	}

	@Override
	public CraftedItem craftItem(final String recipeName, final int material, final boolean forceLevels, final boolean noSafety)
	{
		return getCraftingSkill().craftItem(recipeName, material, forceLevels, noSafety);
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		return getCraftingSkill().mayICraft(I);
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return getCraftingSkill().supportsDeconstruction();
	}

	@Override
	public double getItemWeightMultiplier(final boolean bundling)
	{
		return getCraftingSkill().getItemWeightMultiplier(bundling);
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands==null)
			return getCraftingSkill().invoke(mob, commands, givenTarget, auto, asLevel);
		if(commands.size()>0)
		{
			final String cmd = commands.get(0).toLowerCase();
			if((commands.size()==1)
			&&(cmd.equalsIgnoreCase("stop")))
				return getCraftingSkill().invoke(mob, commands, givenTarget, auto, asLevel);
			if(commands.size()>1)
			{
				if(cmd.equalsIgnoreCase("info"))
					return getCraftingSkill().invoke(mob, commands, givenTarget, auto, asLevel);
			}
			if(cmd.equalsIgnoreCase("list"))
				return getCraftingSkill().invoke(mob, commands, givenTarget, auto, asLevel);
			if(cmd.equalsIgnoreCase("info"))
				return getCraftingSkill().invoke(mob, commands, givenTarget, auto, asLevel);
			if(cmd.equalsIgnoreCase("mend"))
				return getCraftingSkill().invoke(mob, commands, givenTarget, auto, asLevel);
			if(cmd.equalsIgnoreCase("refit"))
				return getCraftingSkill().invoke(mob, commands, givenTarget, auto, asLevel);
		}
		if(commands.size()<2)
		{
			final StringBuilder features=new StringBuilder(
				"Endow what magic into what?  You must specify a recipe and a stone to endow with ioun magic.  ");
			features.append(" You can also enter \"list\" for a list");
			features.append(", \"info\" for details");
			features.append(", \"mend <item>\" to mend broken items, \"scan\" to scan for mendable items");
			features.append(", \"refit <item>\" to resize wearables");
			features.append(", or \"stop\" to cancel.");
			commonTelL(mob,features.toString());
			return false;
		}
		final String targetName = CMParms.combine(commands,1);
		while(commands.size()>1)
			commands.remove(1);
		final Item targetI = super.getTarget(mob, null, givenTarget, CMParms.parse(targetName), Wearable.FILTER_UNWORNONLY);
		if(targetI == null)
			return false;
		if((!(targetI instanceof RawMaterial))
		||((targetI.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS))
		{
			mob.tell(L("@x1 is not a precious stone.",targetI.name(mob)));
			return false;
		}
		if(targetI.numEffects()>0)
		{
			mob.tell(L("@x1 is already magical.",targetI.name(mob)));
			return false;
		}

		final ItemCraftor.CraftedItem testI = this.getCraftingSkill().craftItem(commands.get(0));
		if(testI == null)
		{
			mob.tell(L("@x1 is an unknown recipe.  Try LIST.",commands.get(0)));
			return false;
		}
		final int lvl = testI.item.basePhyStats().level();
		testI.item.destroy();
		int experienceToLose = 1100 + (50 * lvl);
		if(mob.getExperience()<experienceToLose)
		{
			mob.tell(L("You lack the experience to make something so powerful."));
			return false;
		}
		final Room R = mob.location();
		final PairList<Item,Long> restoreables = new PairArrayList<Item,Long>(R.numItems());
		for(final Enumeration<Item> r = R.items();r.hasMoreElements();)
		{
			final Item I = r.nextElement();
			if((I!=null)
			&&(I instanceof RawMaterial)
			&&((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
			&&(I!=targetI))
				restoreables.add(I,Long.valueOf(I.expirationDate()));
		}
		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;
		try
		{
			for(final Pair<Item,Long> I : restoreables)
				R.delItem(I.first);
			R.moveItemTo(targetI);
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			return getCraftingSkill().invoke(mob, commands, givenTarget, auto, asLevel);
		}
		finally
		{
			if(!targetI.amDestroyed())
				mob.moveItemTo(targetI);
			for(final Pair<Item,Long> I : restoreables)
			{
				R.addItem(I.first);
				I.first.setExpirationDate(I.second.longValue());
			}
		}
	}
}
