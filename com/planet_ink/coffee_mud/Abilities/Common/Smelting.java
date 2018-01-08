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
   Copyright 2002-2018 Bo Zimmerman

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

public class Smelting extends CraftingSkill implements CraftorAbility
{
	@Override
	public String ID()
	{
		return "Smelting";
	}

	private final static String	localizedName	= CMLib.lang().L("Smelting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SMELT", "SMELTING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "METAL|MITHRIL";
	}

	@Override
	public String parametersFormat()
	{
		return "ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tFUTURE_USE\tFUTURE_USE\tITEM_CLASS_ID\tRESOURCE_NAME\tRESOURCE_NAME";
	}

	//protected static final int RCP_FINALNAME=0;
	//protected static final int RCP_LEVEL=1;
	//protected static final int RCP_TICKS=2;
	//private static final int RCP_WOOD_ALWAYSONEONE=3;
	//private static final int RCP_VALUE_DONTMATTER=4;
	//private static final int RCP_CLASSTYPE=5;
	protected static final int RCP_METALONE=6;
	protected static final int RCP_METALTWO=7;

	protected int amountMaking=0;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((buildingI==null)
			||(amountMaking<1)
			||(getRequiredFire(mob,0)==null))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public String parametersFile()
	{
		return "smelting.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	@Override
	public String getDecodedComponentsDescription(MOB mob, List<String> recipe)
	{
		return "1";
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
					amountMaking=amountMaking*(baseYield()+abilityCode());
					if(messedUp)
					{
						final Item copy=(Item)buildingI.copyOf();
						if(copy instanceof RawMaterial)
						{
							copy.basePhyStats().setWeight(amountMaking);
							copy.phyStats().setWeight(amountMaking);
							CMLib.materials().adjustResourceName(copy);
						}
						commonEmote(mob,L("<S-NAME> ruin(s) @x1!",copy.name()));
						copy.destroy();
					}
					else
					for(int i=0;i<amountMaking;i++)
					{
						final Item copy=(Item)buildingI.copyOf();
						copy.setMiscText(buildingI.text());
						copy.recoverPhyStats();
						if(!dropAWinner(mob,copy))
							break;
					}
				}
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,0);
		if(commands.size()==0)
		{
			commonTell(mob,L("Make what? Enter \"smelt list\" for a list, or \"smelt stop\" to cancel."));
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
				CMLib.lister().fixColWidth(20,mob.session()),
				CMLib.lister().fixColWidth(3,mob.session()),
				CMLib.lister().fixColWidth(16,mob.session())
			};
			final StringBuffer buf=new StringBuffer(L("@x1 @x2 @x3 Metal #2\n\r",CMStrings.padRight(L("Item"),cols[0]),CMStrings.padRight(L("Lvl"),cols[1]),CMStrings.padRight(L("Metal #1"),cols[2])));
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final String metal1=V.get(RCP_METALONE).toLowerCase();
					final String metal2=V.get(RCP_METALTWO).toLowerCase();
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" "+CMStrings.padRight(metal1,cols[2])+" "+metal2+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		final Item fire=getRequiredFire(mob,0);
		if(fire==null)
			return false;
		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		messedUp=false;
		String recipeName=CMParms.combine(commands,0);
		int maxAmount=1;
		if((commands.size()>1)&&(CMath.isNumber(commands.get(commands.size()-1))))
		{
			maxAmount=CMath.s_int(commands.get(commands.size()-1));
			commands.remove(commands.size()-1);
			recipeName=CMParms.combine(commands,0);
		}
		List<String> foundRecipe=null;
		final List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			final List<String> V=matches.get(r);
			if(V.size()>0)
			{
				final int level=CMath.s_int(V.get(RCP_LEVEL));
				if(level<=xlevel(mob))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,L("You don't know how to make '@x1'.  Try \"smelt list\" for a list.",recipeName));
			return false;
		}
		final String doneResourceDesc=foundRecipe.get(RCP_FINALNAME);
		final String resourceDesc1=foundRecipe.get(RCP_METALONE);
		final String resourceDesc2=foundRecipe.get(RCP_METALTWO);
		final int resourceCode1=RawMaterial.CODES.FIND_IgnoreCase(resourceDesc1);
		final int resourceCode2=RawMaterial.CODES.FIND_IgnoreCase(resourceDesc2);
		final int doneResourceCode=RawMaterial.CODES.FIND_IgnoreCase(doneResourceDesc);
		if((resourceCode1<0)||(resourceCode2<0)||(doneResourceCode<0))
		{
			commonTell(mob,L("CoffeeMud error in this alloy.  Please let your local Archon know."));
			return false;
		}
		final int amountResource1=CMLib.materials().findNumberOfResource(mob.location(),RawMaterial.CODES.GET(resourceCode1));
		final int amountResource2=CMLib.materials().findNumberOfResource(mob.location(),RawMaterial.CODES.GET(resourceCode2));
		if(amountResource1==0)
		{
			commonTell(mob,L("There is no @x1 here to make @x2 from.  It might need to be put down first.",resourceDesc1,doneResourceDesc));
			return false;
		}
		if(amountResource2==0)
		{
			commonTell(mob,L("There is no @x1 here to make @x2 from.  It might need to be put down first.",resourceDesc2,doneResourceDesc));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		amountMaking=amountResource1;
		if(amountResource2<amountResource1)
			amountMaking=amountResource2;
		if((maxAmount>0)&&(amountMaking>maxAmount))
			amountMaking=maxAmount;
		CMLib.materials().destroyResourcesValue(mob.location(),amountMaking,RawMaterial.CODES.GET(resourceCode1),0,null);
		CMLib.materials().destroyResourcesValue(mob.location(),amountMaking,RawMaterial.CODES.GET(resourceCode2),0,null);
		duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),6);
		amountMaking+=amountMaking;
		buildingI=(Item)CMLib.materials().makeResource(RawMaterial.CODES.GET(doneResourceCode),null,false,null);
		startStr=L("<S-NAME> start(s) smelting @x1.",doneResourceDesc.toLowerCase());
		displayText=L("You are smelting @x1",doneResourceDesc.toLowerCase());
		playSound="sizzling.wav";
		verb=L("smelting @x1",doneResourceDesc.toLowerCase());

		messedUp=!proficiencyCheck(mob,0,auto);

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
