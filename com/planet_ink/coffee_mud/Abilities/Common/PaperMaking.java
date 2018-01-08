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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
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

public class PaperMaking extends CraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "PaperMaking";
	}

	private final static String	localizedName	= CMLib.lang().L("Paper Making");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "PAPERMAKE", "PAPERMAKING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "WOODEN|HEMP|SILK|CLOTH";
	}

	@Override
	public String parametersFormat()
	{
		return "ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t" 
			+ "ITEM_CLASS_ID\tRESOURCE_OR_MATERIAL\tLID_LOCK||STATUE||\tCONTAINER_CAPACITY||PAGES_CHARS\tCODED_SPELL_LIST";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int	RCP_WOOD		= 3;
	protected static final int	RCP_VALUE		= 4;
	protected static final int	RCP_CLASSTYPE	= 5;
	protected static final int	RCP_WOODTYPE	= 6;
	protected static final int	RCP_MISCTYPE	= 7;
	protected static final int	RCP_CAPACITY	= 8;
	protected static final int	RCP_SPELL		= 9;

	@Override
	public boolean supportsDeconstruction()
	{
		return false;
	}

	protected DoorKey key=null;

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
		return "papermaking.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
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
						commonTell(mob,L("<S-NAME> mess(es) up making @x1.",buildingI.name(mob)));
					else
					{
						dropAWinner(mob,buildingI);
						CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this);
						if(key!=null)
						{
							dropAWinner(mob,key);
							if(buildingI instanceof Container)
								key.setContainer((Container)buildingI);
						}
					}
				}
				buildingI=null;
				key=null;
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
		final List<String> originalCommands = new XVector<String>(commands);
		if(super.checkStop(mob, commands))
			return true;
		final Session session=mob.session();

		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,L("Papermake what? Enter \"papermake list\" for a list, or \"papermake stop\" to cancel."));
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
		String startStr=null;
		int duration=4;
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
				CMLib.lister().fixColWidth(22,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session())
			};
			final StringBuffer buf=new StringBuffer(L("@x1 @x2 Material required\n\r",CMStrings.padRight(L("Item"),cols[0]),CMStrings.padRight(L("Lvl"),cols[1])));
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					String material=V.get(RCP_WOODTYPE);
					String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(!CMath.isInteger(wood))
					{
						material="";
						wood=wood.toLowerCase();
					}
					else
					if(CMath.s_int(wood)>1)
						material="pounds of "+material;
					else
						material="pound of "+material;
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+wood+" "+material.toLowerCase()+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		key=null;
		messedUp=false;
		String statue=null;
		if((commands.size()>1)&&(commands.get(commands.size()-1)).startsWith("STATUE="))
		{
			statue=((commands.get(commands.size()-1)).substring(7)).trim();
			if(statue.length()==0)
				statue=null;
			else
				commands.remove(commands.size()-1);
		}
		String materialDesc="";
		final String recipeName=CMParms.combine(commands,0);
		List<String> foundRecipe=null;
		final List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			final List<String> V=matches.get(r);
			if(V.size()>0)
			{
				final int level=CMath.s_int(V.get(RCP_LEVEL));
				if((autoGenerate>0)||(level<=xlevel(mob)))
				{
					foundRecipe=V;
					materialDesc=foundRecipe.get(RCP_WOODTYPE);
					if(materialDesc.equalsIgnoreCase("WOOD"))
						materialDesc="WOODEN";
					break;
				}
			}
		}

		if(materialDesc.length()==0)
			materialDesc="WOODEN";

		if(foundRecipe==null)
		{
			commonTell(mob,L("You don't know how to make a '@x1'.  Try \"make list\" for a list.",recipeName));
			return false;
		}

		final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
		final int[] compData = new int[CF_TOTAL];
		final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName),autoGenerate,compData);
		if(componentsFoundList==null)
			return false;
		int woodRequired=CMath.s_int(woodRequiredStr);
		woodRequired=adjustWoodRequired(woodRequired,mob);

		final int[][] data=fetchFoundResourceData(mob,
												woodRequired,materialDesc,null,
												0,null,null,
												false,
												autoGenerate,
												null);
		if(data==null)
			return false;
		woodRequired=data[0][FOUND_AMT];

		final String misctype=(foundRecipe.size()>RCP_MISCTYPE)?foundRecipe.get(RCP_MISCTYPE).trim():"";
		if((misctype.equalsIgnoreCase("statue"))
		&&(session!=null)
		&&((statue==null)||(statue.trim().length()==0)))
		{
			final Ability me=this;
			final Physical target=givenTarget;
			session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
			{
				@Override
				public void showPrompt()
				{
					session.promptPrint(L("What is this of?\n\r: "));
				}

				@Override
				public void timedOut()
				{
				}

				@Override
				public void callBack()
				{
					final String of=this.input;
					if((of.trim().length()==0)||(of.indexOf('<')>=0))
						return;
					final Vector<String> newCommands=new XVector<String>(originalCommands);
					newCommands.add("STATUE="+of);
					me.invoke(mob, newCommands, target, auto, asLevel);
				}
			});
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(autoGenerate<=0)
		{
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],0,null);
			CMLib.ableComponents().destroyAbilityComponents(componentsFoundList);
		}
		buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
		if(buildingI==null)
		{
			commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
			return false;
		}
		duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
		String itemName=foundRecipe.get(RCP_FINALNAME).toLowerCase();
		itemName=CMLib.english().startWithAorAn(itemName);
		buildingI.setName(itemName);
		startStr=L("<S-NAME> start(s) making @x1.",buildingI.name());
		displayText=L("You are making @x1",buildingI.name());
		verb=L("making @x1",buildingI.name());
		playSound="crumple.wav";
		buildingI.setDisplayText(L("@x1 lies here",itemName));
		buildingI.setDescription(itemName+". ");
		int weight = getStandardWeight(woodRequired+compData[CF_AMOUNT],bundling) / 10;
		if(weight < 1)
			weight = 1;
		buildingI.basePhyStats().setWeight(weight);
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
		buildingI.setMaterial(super.getBuildingMaterial(woodRequired, data, compData));
		final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
		addSpells(buildingI,spell);
		setBrand(mob, buildingI);
		if(((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
		||(data[0][FOUND_CODE]==RawMaterial.RESOURCE_RICE))
			buildingI.setMaterial(RawMaterial.RESOURCE_PAPER);
		if(buildingI instanceof Recipe)
			((Recipe)buildingI).setTotalRecipePages(CMath.s_int(woodRequiredStr));
		final int capacity;
		if(buildingI instanceof Book)
		{
			capacity=0;
			final String pgs=foundRecipe.get(RCP_CAPACITY);
			int x=pgs.indexOf('/');
			if(x<0)
				((Book)buildingI).setMaxPages(CMath.s_int(pgs));
			else
			{
				((Book)buildingI).setMaxPages(CMath.s_int(pgs.substring(0, x)));
				((Book)buildingI).setMaxCharsPerPage(CMath.s_int(pgs.substring(x+1)));
			}
		}
		else
			capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();

		if((misctype.equalsIgnoreCase("statue"))
		&&(statue!=null)
		&&(statue.trim().length()>0))
		{
			if(buildingI.Name().indexOf('%')>0)
			{
				buildingI.setName(CMStrings.replaceAll(buildingI.Name(), "%", statue.trim()));
				buildingI.setDisplayText(CMStrings.replaceAll(buildingI.displayText(), "%", statue.trim()));
				buildingI.setDescription(CMStrings.replaceAll(buildingI.description(), "%", statue.trim()));
			}
			else
			{
				buildingI.setName(L("@x1 of @x2",itemName,statue.trim()));
				buildingI.setDisplayText(L("@x1 of @x2 is here",itemName,statue.trim()));
				buildingI.setDescription(L("@x1 of @x2. ",itemName,statue.trim()));
			}
			verb=L("making @x1",buildingI.name());
		}
		else
		if((buildingI instanceof Container)
		&&(!(buildingI instanceof Armor)))
		{
			if(capacity>0)
			{
				((Container)buildingI).setCapacity(capacity+woodRequired);
				((Container)buildingI).setContainTypes(Container.CONTAIN_ANYTHING);
			}
			if(misctype.equalsIgnoreCase("LID"))
				((Container)buildingI).setDoorsNLocks(true,false,true,false,false,false);
			else
			if(misctype.equalsIgnoreCase("LOCK"))
			{
				((Container)buildingI).setDoorsNLocks(true,false,true,true,false,true);
				((Container)buildingI).setKeyName(Double.toString(Math.random()));
				key=(DoorKey)CMClass.getItem("GenKey");
				key.setKey(((Container)buildingI).keyName());
				key.setName(L("a key"));
				key.setDisplayText(L("a small key sits here"));
				key.setDescription(L("looks like a key to @x1",buildingI.name()));
				key.recoverPhyStats();
				key.text();
			}
		}

		messedUp=!proficiencyCheck(mob,0,auto);

		if(autoGenerate>0)
		{
			if(key!=null)
				crafted.add(key);
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
		return true;
	}
}
