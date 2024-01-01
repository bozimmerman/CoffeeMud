package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.LeatherWorking.Stage;
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
   Copyright 2023-2024 Bo Zimmerman

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
public class Mycology extends CommonSkill implements RecipeDriven
{
	@Override
	public String ID()
	{
		return "Mycology";
	}

	private final static String localizedName = CMLib.lang().L("Mycology");

	public static final int	RCP_FINALNAME	= 0;
	public static final int	RCP_FREQ		= 1;
	public static final int	RCP_VALUE		= 2;
	public static final int RCP_SPELL		= 3;
	
	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"MYCOLOGY"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL|Ability.DOMAIN_NATURELORE;
	}

	public String parametersFormat()
	{
		return "MUSHROOM_NAME\tFREQUENCY\tITEM_BASE_VALUE";
	}

	protected Item		found		= null;
	protected String	foundName	= null;
	protected boolean	lastAuto	= false;
	protected boolean	messedUp	= false;

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public Mycology()
	{
		super();
		displayText=L("You are evaluating...");
		verb=L("evaluating");
	}

	@Override
	public List<List<String>> fetchRecipes()
	{
		return loadRecipes(getRecipeFilename());
	}

	@Override
	public String getRecipeFormat()
	{
		return "MUSHROOM_NAME\tFREQUENCY\tITEM_BASE_VALUE\tCODED_SPELL_LIST";
	}

	@Override
	public String getRecipeFilename()
	{
		return "mycology.txt";
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
				Integer.valueOf(1));
	}


	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(!aborted)
			&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(messedUp)
					commonTelL(mob,"You lose your concentration on @x1.",found.name());
				else
				{
					final List<List<String>> mushroomList=this.fetchRecipes();
					@SuppressWarnings("unchecked")
					IntegerRangeMap<List<String>> map=(IntegerRangeMap<List<String>>)
						Resources.getResource("PARSED_RECIPE_MAP: "+getRecipeFilename());
					if(map==null)
					{
						map = new IntegerRangeMap<List<String>>();
						int cur = 0;
						for(List<String> recipe : mushroomList)
						{
							if((recipe.size()>RCP_FREQ)
							&&(recipe.get(0).trim().length()>0))
							{
								int v = CMath.s_int(recipe.get(RCP_FREQ));
								int min = cur;
								int max = cur + v;
								cur = cur + v + 1;
								map.put(new int[] {min,max}, recipe);
							}
						}
						Resources.submitResource("PARSED_RECIPE_MAP: "+getRecipeFilename(),map);
					}
					Item origFound=found;
					while(found != null)
					{
						if(found.phyStats().weight()>1)
							found=CMLib.materials().unbundle(found, 1, null);
						List<String> mushroomFound=null;
						while((mushroomList.size()>2)
						&&((mushroomFound==null)||(mushroomFound.get(0).trim().length()==0)))
						{
							int rand = CMLib.dice().roll(1, map.getMax(), 0);
							mushroomFound=map.get(new int[] {rand,rand});
						}
						if(mushroomFound==null)
							mushroomFound=new XVector<String>(L("unknown"),"1","1");
						String mushroom=mushroomFound.get(RCP_FINALNAME).toLowerCase().trim();
						int valueMultiplier=mushroomFound.size()>2?CMath.s_int(mushroomFound.get(RCP_VALUE)):1;
						if(found.rawSecretIdentity().length()>0)
						{
							mushroom=found.rawSecretIdentity();
							valueMultiplier=1;
							found.setSecretIdentity("");
						}
						commonTelL(mob,"@x1 appears to be @x2.",found.name(),mushroom);
						String name=found.Name();
						name=name.substring(0,name.length()-10).trim(); //?!
						String nameAddendum=mushroom.endsWith("shroom")?"":L(" mushrooms");
						if(name.length()>0)
							found.setName(name+" "+mushroom+nameAddendum);
						else
							found.setName(L("some @x1"+nameAddendum,mushroom));
						found.setDisplayText(L("@x1 is here",found.Name()));
						found.setBaseValue(found.baseGoldValue()*valueMultiplier);
						found.setDescription("");
						if(found instanceof RawMaterial)
							((RawMaterial)found).setSubType(mushroom.toUpperCase().trim());
						final String spell=(mushroomFound.size()>RCP_SPELL)?mushroomFound.get(RCP_SPELL).trim():"";
						new CraftingSkill().addSpellsOrBehaviors(found,spell,new ArrayList<CMObject>(),new ArrayList<CMObject>());
						found.text();
						if((!isLimitedToOne()) && (foundName!=null))
						{
							final Item tempFound=found;
							if((origFound!=null)
							&&(!origFound.amDestroyed())
							&&(origFound!=found))
								found=origFound;
							else
								found=mob.fetchItem(null, Wearable.FILTER_UNWORNONLY, "$"+foundName+"$");
							if((found != null)
							&&((found.material()==RawMaterial.RESOURCE_MUSHROOMS))
							&&((found.Name().toUpperCase().endsWith(" MUSHROOMS"))
								||(found.Name().equalsIgnoreCase("mushroomss"))
								||(found.Name().toUpperCase().endsWith("BUNDLE")))
							&&(proficiencyCheck(mob,0,lastAuto)))
							{
								if(origFound==tempFound)
									origFound=found;
								continue;
							}
							found=null;
						}
						else
							found=null;
					}
				}
			}
		}
		super.unInvoke();
	}

	protected boolean isLimitedToOne()
	{
		return true;
	}

	protected int duration(final MOB mob)
	{
		return getDuration(15,mob,1,2);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<1)
		{
			commonTelL(mob,"You must specify what mushroom or fungi you want to identify.");
			return false;
		}
		final String finalName=CMParms.combine(commands,0);
		Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,finalName);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTelL(mob,"You don't seem to have a '@x1'.",(commands.get(0)));
			return false;
		}
		commands.remove(commands.get(0));

		if((target.material()!=RawMaterial.RESOURCE_MUSHROOMS)
		||((!target.Name().toUpperCase().endsWith(" MUSHROOMS"))
		   &&(!target.Name().equalsIgnoreCase("mushrooms"))
		   &&(!target.Name().toUpperCase().endsWith("BUNDLE")))
		||(!(target instanceof RawMaterial))
		||(((RawMaterial)target).getSubType().length()>0)
		||(!target.isGeneric()))
		{
			commonTelL(mob,"You can only identify unknown mushrooms or fungi.");
			return false;
		}
		if(isLimitedToOne() && target.basePhyStats().weight()>1)
			target=CMLib.materials().unbundle(target, 1, null);
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb=L("studying @x1",target.name());
		displayText=L("You are @x1",verb);
		found=target;
		messedUp=false;
		if(!proficiencyCheck(mob,0,auto))
			messedUp=true;
		final int duration=duration(mob);
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> stud(ys) @x1.",target.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			this.foundName=target.Name();
			this.lastAuto=auto;
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
