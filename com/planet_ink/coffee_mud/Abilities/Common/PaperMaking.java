package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;


/* 
   Copyright 2000-2013 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class PaperMaking extends CraftingSkill implements ItemCraftor
{
	public String ID() { return "PaperMaking"; }
	public String name(){ return "Paper Making";}
	private static final String[] triggerStrings = {"PAPERMAKE","PAPERMAKING"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "WOODEN|HEMP|SILK|CLOTH";}
	public String parametersFormat(){ return 
		"ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tMATERIALS_REQUIRED\tITEM_BASE_VALUE\t"
		+"ITEM_CLASS_ID\tRESOURCE_OR_MATERIAL\tN_A\tN_A\tCODED_SPELL_LIST";}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_WOODTYPE=6;
	//private static final int RCP_CAPACITY=7;
	protected static final int RCP_SPELL=9;

	public boolean supportsDeconstruction() { return false; }

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(buildingI==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public String parametersFile(){ return "papermaking.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
						commonTell(mob,"<S-NAME> mess(es) up making "+buildingI.name(mob)+".");
					else
						dropAWinner(mob,buildingI);
				}
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return super.getComponentDescription( mob, recipe, RCP_WOOD );
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		int autoGenerate=0;
		if((auto)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
			givenTarget=null;
		}
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Papermake what? Enter \"papermake list\" for a list, or \"papermake stop\" to cancel.");
			return false;
		}
		if((!auto)
		&&(commands.size()>0)
		&&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
		{
			bundling=true;
			if(super.invoke(mob,commands,givenTarget,auto,asLevel))
				return super.bundle(mob,commands);
			return false;
		}
		List<List<String>> recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
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
			int[] cols={
					ListingLibrary.ColFixer.fixColWidth(22,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(3,mob.session())
				};
			StringBuffer buf=new StringBuffer(CMStrings.padRight("Item",cols[0])+" "+CMStrings.padRight("Lvl",cols[1])+" Material required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=replacePercent(V.get(RCP_FINALNAME),"");
					int level=CMath.s_int(V.get(RCP_LEVEL));
					String material=V.get(RCP_WOODTYPE);
					String wood=getComponentDescription(mob,V,RCP_WOOD);
					if(wood.length()>5) material="";
					if(((level<=xlevel(mob))||allFlag)
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+wood+" "+material.toLowerCase()+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		messedUp=false;
		String materialDesc="";
		String recipeName=CMParms.combine(commands,0);
		List<String> foundRecipe=null;
		List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			List<String> V=matches.get(r);
			if(V.size()>0)
			{
				int level=CMath.s_int(V.get(RCP_LEVEL));
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
			commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"make list\" for a list.");
			return false;
		}

		final String woodRequiredStr = foundRecipe.get(RCP_WOOD);
		final List<Object> componentsFoundList=getAbilityComponents(mob, woodRequiredStr, "make "+CMLib.english().startWithAorAn(recipeName), autoGenerate);
		if(componentsFoundList==null) return false;
		int woodRequired=CMath.s_int(woodRequiredStr);
		woodRequired=adjustWoodRequired(woodRequired,mob);
		
		int[][] data=fetchFoundResourceData(mob,
											woodRequired,materialDesc,null,
											0,null,null,
											false,
											autoGenerate,
											null);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(autoGenerate<=0)
		{
			CMLib.materials().destroyResourcesValue(mob.location(),woodRequired,data[0][FOUND_CODE],0,null);
			CMLib.ableMapper().destroyAbilityComponents(componentsFoundList);
		}
		buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
		if(buildingI==null)
		{
			commonTell(mob,"There's no such thing as a "+foundRecipe.get(RCP_CLASSTYPE)+"!!!");
			return false;
		}
		duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
		String itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
		itemName=CMLib.english().startWithAorAn(itemName);
		buildingI.setName(itemName);
		startStr="<S-NAME> start(s) making "+buildingI.name()+".";
		displayText="You are making "+buildingI.name();
		verb="making "+buildingI.name();
		playSound="crumple.wav";
		buildingI.setDisplayText(itemName+" lies here");
		buildingI.setDescription(itemName+". ");
		buildingI.basePhyStats().setWeight(getStandardWeight(woodRequired,bundling));
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
		buildingI.setMaterial(data[0][FOUND_CODE]);
		String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
		addSpells(buildingI,spell);
		buildingI.setSecretIdentity(getBrand(mob));
		if(((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN)
		||(data[0][FOUND_CODE]==RawMaterial.RESOURCE_RICE))
			buildingI.setMaterial(RawMaterial.RESOURCE_PAPER);
		if(buildingI instanceof Recipe)
			((Recipe)buildingI).setTotalRecipePages(CMath.s_int(woodRequiredStr));
		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL)));
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();

		messedUp=!proficiencyCheck(mob,0,auto);

		if(autoGenerate>0)
		{
			commands.addElement(buildingI);
			return true;
		}

		CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			buildingI=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
