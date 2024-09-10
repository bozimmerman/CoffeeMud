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
public class Chant_EndowGemstones extends Chant implements RecipeDriven
{

	@Override
	public String ID()
	{
		return "Chant_EndowGemstones";
	}

	private final static String	localizedName	= CMLib.lang().L("Endow Gemstones");

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


	@Override
	public List<List<String>> fetchRecipes()
	{
		final String filename = getRecipeFilename();
		@SuppressWarnings("unchecked")
		List<List<String>> V=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+filename);
		if(V==null)
		{
			final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+filename,null,CMFile.FLAG_LOGERRORS).text();
			V=new ReadOnlyList<List<String>>(CMLib.utensils().loadRecipeList(str.toString(), true));
			if(V.size()==0)
				Log.errOut(ID(),"Recipes not found!");
			Resources.submitResource("PARSED_RECIPE: "+filename,V);
		}
		return V;
	}

	@Override
	public String getRecipeFormat()
	{
		return"ITEM_NAME\tITEM_LEVEL\tBUILDER_MASK\tXLEVEL\tEXPERTISE";
	}

	protected static final int	RCP_FINALNAME	= 0;
	protected static final int	RCP_LEVEL		= 1;
	protected static final int	RCP_MASK		= 2;
	protected static final int	RCP_XLEVEL		= 3;
	protected static final int	RCP_EXPERTISE	= 4;

	@Override
	public String getRecipeFilename()
	{
		return "endowgemstones.txt";
	}

	@Override
	public List<String> matchingRecipeNames(final String recipeName, final boolean beLoose)
	{
		final List<String> matches = new Vector<String>();
		for(final List<String> list : fetchRecipes())
		{
			final String name=list.get(RecipeDriven.RCP_FINALNAME);
			if(name.equalsIgnoreCase(recipeName)
			||(beLoose && (name.toUpperCase().startsWith(recipeName.toUpperCase()))))
				matches.add(name);
		}
		if((matches.size()==0)&&(beLoose))
		{
			for(final List<String> list : fetchRecipes())
			{
				final String name=list.get(RecipeDriven.RCP_FINALNAME);
				if(name.toUpperCase().indexOf(recipeName.toUpperCase())>=0)
					matches.add(name);
			}
		}
		return matches;
	}

	@Override
	public Pair<String, Integer> getDecodedItemNameAndLevel(final List<String> recipe)
	{
		return new Pair<String,Integer>(recipe.get( RecipeDriven.RCP_FINALNAME ),
				Integer.valueOf(CMath.s_int(recipe.get( RecipeDriven.RCP_LEVEL ))));
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
		final boolean list = (commands.size()>0) && "list".startsWith(commands.get(0).toLowerCase());
		if(list)
		{
			final int[] cols = new int[] {
					CMLib.lister().fixColWidth(5, mob.session()),
					CMLib.lister().fixColWidth(35, mob.session()),
					CMLib.lister().fixColWidth(30, mob.session())
			};
			final StringBuilder str = new StringBuilder("");
			str.append("^H")
				.append(CMStrings.padRight(L("Effect"), cols[1]))
				.append(CMStrings.padRight(L("Lvl"), cols[0]))
				.append(CMStrings.padRight(L("Expertise"), cols[2]))
				.append("\n\r");
			boolean toggle = false;
			for(final List<String> recipe : this.fetchRecipes())
			{
				if(recipe.size()<=RCP_EXPERTISE)
					continue;
				toggle = !toggle;
				final int lvl = CMath.s_int(recipe.get(RCP_LEVEL));
				final int xlvl = CMath.s_int(recipe.get(RCP_XLEVEL));
				final String name = recipe.get(RCP_FINALNAME);
				final String expertise = recipe.get(RCP_EXPERTISE);
				//final String mask = recipe.get(RCP_FINALNAME);
				final ExpertiseDefinition def = (expertise.length()>0)?CMLib.expertises().findDefinition(expertise, true):null;
				if(lvl <= adjustedLevel(mob,asLevel))
				{
					str.append(toggle?"^W":"^w")
					.append(CMStrings.padRight(name, cols[1]))
					.append(CMStrings.padRight(""+xlvl, cols[0]))
					.append(CMStrings.padRight((def==null)?"":def.name(), cols[2]))
					.append("\n\r");
				}
			}
			mob.tell(str.toString()+"^N");
			return true;
		}
		else
		if(commands.size()<2)
		{
			//final Ability pA = CMClass.findAbility("Prop_WearAdjuster");
			//pA.setMiscText(effect);
			//final String s = pA.getStat("STAT-LEVEL");
			mob.tell(L("Endow which effect onto which item?  Try LIST."));
			return false;
		}

		final String itemName = CMParms.combine(commands,1);
		final Physical target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,itemName,Wearable.FILTER_UNWORNONLY);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",itemName));
			return false;
		}
		if((!(target instanceof Item))
		||((((Item)target).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS))
		{
			mob.tell(mob,target,null,L("You can't endow <T-NAME> with this chant."));
			return false;
		}
		final Ability A = target.fetchEffect("Prop_ItemSlotFiller");
		if((A == null)
		|| (!(A instanceof AbilityContainer))
		|| ((A.text().toLowerCase().indexOf("gemsetting")<0) && (A.text().toLowerCase().indexOf("jewelrycharm")<0)))
		{
			mob.tell(mob,target,null,L("<T-NAME> is not properly cut by a jeweler and can not yet be endowed by this chant."));
			return false;
		}
		if(((AbilityContainer)A).numAbilities()>0)
		{
			mob.tell(mob,target,null,L("<T-NAME> is already endowed."));
			return false;
		}
		final Item gemI=(Item)target;

		List<String> matches = this.matchingRecipeNames(commands.get(0), false);
		if(matches.size()==0)
			matches = this.matchingRecipeNames(commands.get(0), true);
		String recipeName = null;
		for(final List<String> recipe : this.fetchRecipes())
		{
			final String name = recipe.get(RCP_FINALNAME);
			final int lvl = CMath.s_int(recipe.get(RCP_LEVEL));
			if(lvl <= adjustedLevel(mob,asLevel))
			{
				recipeName = name;
				break;
			}
		}
		if(recipeName == null)
		{
			mob.tell(L("'@x1' does not match an endowment.  Try LIST."));
			return false;
		}
		List<String> foundRecipe = null;
		for(final List<String> recipe : this.fetchRecipes())
		{
			final String name = recipe.get(RCP_FINALNAME);
			if(name.equals(recipeName))
			{
				foundRecipe=recipe;
				break;
			}
		}
		final int xlvl = CMath.s_int(foundRecipe.get(RCP_XLEVEL));
		final String mask = foundRecipe.get(RCP_MASK);
		final String expertise = foundRecipe.get(RCP_EXPERTISE);
		final ExpertiseDefinition def = (expertise.length()>0)?CMLib.expertises().findDefinition(expertise, true):null;
		if(def != null)
		{
			if(mob.fetchExpertise(def.ID())==null)
			{
				mob.tell(L("You must have the @x1 expertise to do that.",def.name()));
				return false;
			}
		}

		int experienceToLose=490 + (10 * xlvl);
		if((mob.getExperience()-experienceToLose)<0)
		{
			mob.tell(L("You don't have enough experience to endow this chant on @x1.",target.name(mob)));
			return false;
		}
		// lose all the mana!
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,null,CMMsg.MSG_OK_VISUAL,L("<T-NAME> glow(s) brightly!"));
				gemI.basePhyStats().setDisposition(target.basePhyStats().disposition()|PhyStats.IS_BONUS);
				gemI.basePhyStats().setLevel(gemI.basePhyStats().level()+xlvl);
				final Ability adjusterA = CMClass.getAbility("Prop_WearAdjuster");
				adjusterA.setMiscText(mask);
				((AbilityContainer)A).addAbility(adjusterA);
				gemI.recoverPhyStats();
			}
		}
		else
		{
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			experienceToLose=-CMLib.leveler().postExperience(mob,"ABILITY:"+ID(),null,null,-experienceToLose, false);
			mob.tell(L("You lose @x1 experience points for the effort.",""+experienceToLose));
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));
		}
		// return whether it worked
		return success;
	}
}
