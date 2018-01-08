package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.MusicalInstrument.InstrumentType;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
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

public class InstrumentMaking extends CraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "InstrumentMaking";
	}

	private final static String	localizedName	= CMLib.lang().L("Instrument Making");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "INSTRUMENTMAKING", "INSTRUMENTMAKE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "WOODEN";
	}

	@Override
	public String parametersFormat()
	{
		return
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tRIDE_CAPACITY||CODED_WEAR_LOCATION\tMETAL_OR_WOOD\tOPTIONAL_RACE_ID\tINSTRUMENT_TYPE";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_MISCTYPE	= 6;
	protected static final int	RCP_MATERIAL	= 7;
	protected static final int	RCP_RACES		= 8;
	protected static final int	RCP_TYPE		= 9;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(buildingI==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public String parametersFile()
	{
		return "instruments.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return true;
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(CMLib.flags().isDeadlyOrMaliciousEffect(I))
			return false;
		if(isANativeItem(I.Name()))
			return true;
		if(I instanceof MusicalInstrument)
			return true;
		return false;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(activity == CraftingActivity.LEARNING)
							commonEmote(mob,L("<S-NAME> fail(s) to learn how to make @x1.",buildingI.name()));
						else
							commonEmote(mob,L("<S-NAME> mess(es) up making @x1.",buildingI.name()));
						buildingI.destroy();
					}
					else
					if(activity==CraftingActivity.LEARNING)
					{
						deconstructRecipeInto( buildingI, recipeHolder );
						buildingI.destroy();
					}
					else
					{
						dropAWinner(mob,buildingI);
						CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this);
					}
				}
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_WOOD );
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new Vector<Item>(0));
	}
	
	@Override
	protected boolean autoGenInvoke(final MOB mob, List<String> commands, Physical givenTarget, final boolean auto, 
								 final int asLevel, int autoGenerate, boolean forceLevels, List<Item> crafted)
	{
		if(super.checkStop(mob, commands))
			return true;

		if(super.checkInfo(mob, commands))
			return true;
		
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,L("Make what Instrument? Enter \"instrumentmake list\" for a list, \"instrumentmake info <item>\","
							+ " \"instrumentmake learn <item>\" to gain recipes, or \"instrumentmake stop\" to cancel."));
			return false;
		}
		if((!auto)
		&&(commands.size()>0)
		&&((commands.get(0)).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String str=commands.get(0);
		bundling=false;
		String startStr=null;
		int duration=4;
		final boolean archon=CMSecurity.isASysOp(mob)||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ALLSKILLS);
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final int[] cols={
				CMLib.lister().fixColWidth(23,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session()),
				CMLib.lister().fixColWidth(10,mob.session())
			};
			final StringBuffer buf=new StringBuffer(L("@x1 @x2 @x3 Material required\n\r",CMStrings.padRight(L("Item"),cols[0]),CMStrings.padRight(L("Lvl"),cols[1]),CMStrings.padRight(L("Type"),cols[2])));
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					String type=V.get(RCP_MATERIAL);
					final String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5)
						type="";
					final String race=V.get(RCP_RACES).trim();
					final String itype=CMStrings.capitalizeAndLower(V.get(RCP_TYPE).toLowerCase()).trim();
					if(((level<=xlevel(mob))||allFlag)
					&&((race.length()==0)||archon||((" "+race+" ").toUpperCase().indexOf(" "+mob.charStats().getMyRace().ID().toUpperCase()+" ")>=0))
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRight(itype,cols[2])+" "+wood+" "+type+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if(((commands.get(0))).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		int amount=-1;
		if((commands.size()>1)&&(CMath.isNumber(commands.get(commands.size()-1))))
		{
			amount=CMath.s_int(commands.get(commands.size()-1));
			commands.remove(commands.size()-1);
		}
		final String recipeName=CMParms.combine(commands,0);
		List<String> foundRecipe=null;
		final List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			final List<String> V=matches.get(r);
			if(V.size()>0)
			{
				final String race=V.get(RCP_RACES).trim();
				final int level=CMath.s_int(V.get(RCP_LEVEL));
				if(((autoGenerate>0)||(level<=xlevel(mob)))
				&&((autoGenerate>0)||(race.length()==0)||archon||((" "+race+" ").toUpperCase().indexOf(" "+mob.charStats().getMyRace().ID().toUpperCase()+" ")>=0)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,L("You don't know how to make a '@x1'.  Try \"instrumentmake list\" for a list.",recipeName));
			return false;
		}

		final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
		final int[] compData = new int[CF_TOTAL];
		final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName),autoGenerate,compData);
		if(componentsFoundList==null)
			return false;
		int woodRequired=CMath.s_int(woodRequiredStr);
		woodRequired=adjustWoodRequired(woodRequired,mob);

		if(amount>woodRequired)
			woodRequired=amount;
		final String materialRequired=foundRecipe.get(RCP_MATERIAL);
		final String misctype=foundRecipe.get(RCP_MISCTYPE);
		final int[] pm={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
		if(!materialRequired.toUpperCase().startsWith("METAL"))
		{
			pm[0]=RawMaterial.MATERIAL_WOODEN;
			pm[1]=RawMaterial.MATERIAL_WOODEN;
		}
		bundling=misctype.equalsIgnoreCase("BUNDLE");
		final int[][] data=fetchFoundResourceData(mob,
												woodRequired,"material",pm,
												0,null,null,
												bundling,
												autoGenerate,
												null);
		if(data==null)
			return false;
		woodRequired=data[0][FOUND_AMT];
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final int lostValue=autoGenerate>0?0:
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],0,null)
			+CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
		buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
		if(buildingI==null)
		{
			commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
			return false;
		}
		duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
		buildingI.setMaterial(super.getBuildingMaterial(woodRequired, data, compData));
		String itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(buildingI.material())).toLowerCase();
		if(bundling)
			itemName="a "+woodRequired+"# "+itemName;
		else
			itemName=CMLib.english().startWithAorAn(itemName);
		buildingI.setName(itemName);
		startStr=L("<S-NAME> start(s) making @x1.",buildingI.name());
		displayText=L("You are making @x1",buildingI.name());
		verb=L("making @x1",buildingI.name());
		playSound="sanding.wav";
		buildingI.setDisplayText(L("@x1 lies here",itemName));
		buildingI.setDescription(itemName+". ");
		buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired+compData[CF_AMOUNT],bundling));
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
		if(buildingI.basePhyStats().level()<1)
			buildingI.basePhyStats().setLevel(1);
		final String type=foundRecipe.get(RCP_TYPE);
		for(InstrumentType iType : InstrumentType.values())
		{
			if(type.equalsIgnoreCase(iType.name()))
				((MusicalInstrument)buildingI).setInstrumentType(iType);
		}
		setBrand(mob, buildingI);
		if(buildingI instanceof Rideable)
		{
			((Rideable)buildingI).setRideBasis(Rideable.RIDEABLE_SIT);
			((Rideable)buildingI).setRiderCapacity(CMath.s_int(misctype));
			if(((Rideable)buildingI).riderCapacity()<=0)
				((Rideable)buildingI).setRiderCapacity(1);
		}
		else
		if(!(buildingI instanceof FalseLimb))
		{
			setWearLocation(buildingI,misctype,0);
		}
		if(bundling)
			buildingI.setBaseValue(lostValue);
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();

		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb=L("bundling @x1",RawMaterial.CODES.NAME(buildingI.material()).toLowerCase());
			startStr=L("<S-NAME> start(s) @x1.",verb);
			displayText=L("You are @x1",verb);
		}

		if(autoGenerate>0)
		{
			crafted.add(buildingI);
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
