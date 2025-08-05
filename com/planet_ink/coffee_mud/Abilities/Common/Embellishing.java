package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
   Copyright 2018-2025 Bo Zimmerman

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
public class Embellishing extends CommonSkill implements RecipeDriven
{
	@Override
	public String ID()
	{
		return "Embellishing";
	}

	private final static String	localizedName	= CMLib.lang().L("Embellishing");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "EMBELLISH", "EMBELLISHING"});

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ARTISTIC;
	}

	public Embellishing()
	{
		super();
		displayText=L("You are embellishing...");
		verb=L("embellishing");
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	protected static final int	RCP_TICKS		= 2;
	protected static final int	RCP_NAME		= 3;
	protected static final int	RCP_WOOD		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_SPELL		= 6;

	protected static final String DEFAULT_EMBELLISH_WORD= CMLib.lang().L("embellished");

	protected String	embellishWord	= DEFAULT_EMBELLISH_WORD;
	protected String	embellishment	= "@x1 has @x2.";
	protected int		lostValue		= 0;
	protected Item		embellishI		= null;
	protected String	spells			= "";
	protected boolean	messedUp		= false;

	@Override
	public List<List<String>> fetchRecipes()
	{
		return loadRecipes(getRecipeFilename());
	}

	@Override
	public String getRecipeFormat()
	{
		return
		"DISPLAY_MASK\tITEM_LEVEL\tBUILD_TIME_TICKS\tNAME_MASK\t"
		+ "MATERIALS_REQUIRED\tZAPPERMASK\tCODED_SPELL_LIST";
	}

	@Override
	public String getRecipeFilename()
	{
		return "embellishing.txt";
	}

	@Override
	public List<String> matchingRecipeNames(final String recipeName, final boolean beLoose)
	{
		final List<String> matches = new Vector<String>();
		for(final List<String> list : fetchRecipes())
		{
			final String name=list.get(RecipeDriven.RCP_FINALNAME);
			if(name.equalsIgnoreCase(recipeName)
			||(beLoose && (name.toUpperCase().indexOf(recipeName.toUpperCase())>=0)))
				matches.add(name);
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
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(text().length()==0)
		{
			if(canBeUninvoked())
			{
				if((affected!=null)
				&&(affected instanceof MOB)
				&&(tickID==Tickable.TICKID_MOB))
				{
					final MOB mob=(MOB)affected;
					if((embellishI==null)||(mob.location()==null))
					{
						messedUp=true;
						unInvoke();
					}
					if(!mob.isContent(embellishI))
					{
						messedUp=true;
						unInvoke();
					}
				}
			}
			return super.tick(ticking,tickID);
		}
		return ! this.unInvoked;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((embellishI!=null)&&(!aborted))
				{
					final Item I=embellishI;
					if((messedUp)||(I==null))
						commonTelL(mob,"You've failed to @x1!",embellishWord);
					else
					{
						final Room room=CMLib.map().roomLocation(I);
						if((messedUp)
						||(room==null))
							commonTelL(mob,"You've messed up @x1!",verb);
						else
						{
							room.show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to @x2 @x1.",I.name(),embellishWord));
							final String name = I.Name();
							final String newName = embellishment;
							Ability A=I.fetchEffect("ExtraData");
							if(A==null)
							{
								A=CMClass.getAbility("ExtraData");
								I.addNonUninvokableEffect(A);
							}
							if(!A.isStat("PRE_EMBELLISHMENT_NAME"))
								A.setStat("PRE_EMBELLISHMENT_NAME", I.Name());
							if(!A.isStat("PRE_EMBELLISHMENT_DISPLAY"))
								A.setStat("PRE_EMBELLISHMENT_DISPLAY", I.displayText());
							I.setName(newName);
							I.setDisplayText(CMStrings.replaceAll(I.displayText(), name, newName));
							I.basePhyStats().setSensesMask(I.basePhyStats().sensesMask()|PhyStats.SENSE_ITEMUNLEARNABLE);
							final CraftingSkill craft = new CraftingSkill();
							craft.addSpellsOrBehaviors(I, spells, null, null);
							I.recoverPhyStats();
							room.recoverRoomStats();
						}
					}
				}
			}
		}
		super.unInvoke();
	}

	protected boolean wasEmbellished(final Item I)
	{
		final Ability A=I.fetchEffect("ExtraData");
		if(A==null)
			return false;
		if(A.isStat("PRE_EMBELLISHMENT_NAME"))
			return true;
		if(A.isStat("PRE_EMBELLISHMENT_DISPLAY"))
			return true;
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		final String _skillName = CMStrings.capitalizeAndLower(this.triggerStrings()[0]);
		final String _commandWord = this.triggerStrings()[0].toUpperCase().trim();
		final String _skillNaming = name();

		if(commands.size()==0)
		{
			commonTelL(mob,"@x1 what, how?  Try @x2 LIST.",_skillName,_commandWord);
			return false;
		}
		final List<List<String>> recipes = CMLib.utensils().addExtRecipes(mob,ID(),fetchRecipes());
		String command = commands.remove(0).toLowerCase();
		final CraftingSkill craft = new CraftingSkill();
		if(command.equals("list"))
		{
			List<List<String>> listRecipes = recipes;
			String mask=CMParms.combine(commands,0);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			@SuppressWarnings("unchecked")
			final Item I = mob.fetchItem(null, Filterer.ANYTHING, mask);
			if(I != null)
			{
				listRecipes = new ArrayList<List<String>>();
				for(final List<String> recipe : recipes)
				{
					final String imask = recipe.get(RCP_CLASSTYPE);
					if(CMLib.masking().maskCheck(imask, I, true))
						listRecipes.add(recipe);
				}
				allFlag=true;
				mask="";
			}
			final StringBuilder buf=new StringBuilder(L("^N@x1 tasks:\n\r",_skillNaming));
			final int[] cols={
				CMLib.lister().fixColWidth(47,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session())
			};
			buf.append("^H"+CMStrings.padRight(L("Item / Mats"),cols[0])+" "+CMStrings.padRight(L("Lvl"),cols[1]));
			buf.append("^N\n\r");
			listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all"))
					? listRecipes : craft.matchingRecipes(listRecipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=craft.replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String mats=craft.getComponentDescription(mob,V,RCP_WOOD);
					if((level<=xlevel(mob))||allFlag)
						buf.append("^w"+CMStrings.padRight(item,cols[0])
								  +"^N "+CMStrings.padRight(""+level,cols[1])
								  +"\n\r "+mats+"\n\r");
				}
			}
			buf.append("\n\r");
			commonTell(mob,buf.toString());
			return true;
		}
		List<String> foundRecipe = null;
		final List<List<String>> matches=craft.matchingRecipes(recipes,command,false);
		if(matches.size()==0)
			matches.addAll(craft.matchingRecipes(recipes,command,true));
		if(matches.size()>0)
		{
			for(int i=matches.size()-1;i>=0;i--)
			{
				final int level = CMath.s_int(matches.get(i).get(RCP_LEVEL));
				if(level>xlevel(mob))
					matches.remove(i);
			}
			if(matches.size()>0)
			{
				foundRecipe = matches.get(0);
				command = craft.replacePercent(foundRecipe.get(RCP_FINALNAME), "");
			}
		}
		if(foundRecipe == null)
		{
			commonTelL(mob,"@x1 what? '@x2' is unknown. Try @x3 LIST.",_skillName,command,_commandWord);
			return false;
		}
		if(commands.size()==0)
		{
			commonTelL(mob,"@x1 what, how?  Try @x2 LIST.",_skillName,_commandWord);
			return false;
		}
		final Item I=super.getTarget(mob, null, givenTarget, commands, Wearable.FILTER_UNWORNONLY);
		if(I==null)
			return false;
		final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
		final int[] compData = new int[CraftingSkill.CF_TOTAL];
		String realRecipeName=craft.replacePercent(foundRecipe.get(RCP_FINALNAME),"");
		final List<Object> componentsFoundList=craft.getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(realRecipeName),-1,compData,1);
		if(componentsFoundList==null)
			return false;
		final String imask = foundRecipe.get(RCP_CLASSTYPE);
		if(!CMLib.masking().maskCheck(imask, I, true))
		{
			commonTelL(mob,"You can not @x2 @x1 with that recipe.",I.name(mob),_skillName);
			return false;
		}
		if(wasEmbellished(I))
		{
			commonTelL(mob,"You can not @x2 @x1 any more than it is.",I.name(mob),_skillName);
			return false;
		}
		//should they specify the extra material? nah -- other skills don't

		final int[][] data = new int[2][3];
		craft.fixDataForComponents(data,woodRequiredStr,false,componentsFoundList, 1);
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if((componentsFoundList.size()>0)
		&&(componentsFoundList.get(0) instanceof RawMaterial))
		{
			final RawMaterial mattyI = (RawMaterial)componentsFoundList.get(0);
			realRecipeName=craft.replacePercent(foundRecipe.get(RCP_FINALNAME),CMLib.materials().makeResourceWord(mattyI.material(), mattyI.getSubType()));
		}
		final MaterialLibrary.DeadResourceRecord deadComps = CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
		lostValue=deadComps.getLostValue();
		embellishment = "@x1 has @x2."; // can't localize, because ite recipi-ized
		embellishI = I;
		this.verb = L("adding @x1 to @x2",realRecipeName, I.Name());
		this.embellishWord	= L("add @x1",realRecipeName);
		this.embellishment=CMStrings.replaceVariables(foundRecipe.get(RCP_NAME), new String[] {I.Name(), realRecipeName});
		this.spells = foundRecipe.get(RCP_SPELL);

		messedUp=!proficiencyCheck(mob,0,auto);
		final int duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,I.phyStats().level(),2);
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) @x1.",verb));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
