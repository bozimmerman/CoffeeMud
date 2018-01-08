package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.ItemKeyPair;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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

public class Herbalism extends SpellCraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "Herbalism";
	}

	private final static String localizedName = CMLib.lang().L("Herbalism");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"HERBALISM","HERBREW","HBREW"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}

	@Override
	public String parametersFormat(){ return
		"SPELL_ID\tITEM_LEVEL\t"
		+"RESOURCE_NAME_OR_HERB_NAME\t"
		+"RESOURCE_NAME_OR_HERB_NAME\t"
		+"RESOURCE_NAME_OR_HERB_NAME\t"
		+"RESOURCE_NAME_OR_HERB_NAME\t"
		+"RESOURCE_NAME_OR_HERB_NAME\t"
		+"RESOURCE_NAME_OR_HERB_NAME";}

	String oldName="";
	private Ability theSpell=null;
	private static final Hashtable<String,Integer> usage=new Hashtable<String,Integer>();

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
			if((buildingI==null)
			||(theSpell==null))
			{
				aborted=true;
				unInvoke();
			}
			else
			if(tickUp==0)
			{
				commonEmote(mob,L("<S-NAME> start(s) brewing @x1.",buildingI.name()));
				displayText=L("You are brewing @x1",buildingI.name());
				verb=L("brewing @x1",buildingI.name());
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public String parametersFile()
	{
		return "herbalism.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	@Override
	public ItemKeyPair craftItem(String recipe)
	{
		return craftItem(recipe,0,false, false);
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return true;
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return "Not implemented";
	}

	@Override
	public boolean mayICraft(final Item I)
	{
		if(I==null)
			return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(I instanceof Drink)
		{
			final Drink D=(Drink)I;
			if((D.liquidType()==RawMaterial.RESOURCE_LIQUOR)
			||(D.liquidType()==RawMaterial.RESOURCE_POISON))
				return false;
		}
		if(I instanceof Potion)
		{
			final Potion P=(Potion)I;
			final List<Ability> spells=P.getSpells();
			if((spells == null)||(spells.size()==0))
				return false;
			boolean chantCheck=false;
			for(final Ability A : spells)
				switch(A.classificationCode()&Ability.ALL_ACODES)
				{
				case Ability.ACODE_CHANT: chantCheck=true; break;
				default: return false;
				}
			return chantCheck;
		}
		else
		if(I instanceof Drink)
		{
			for(int i=0;i<I.numEffects();i++)
			{
				final Ability A=I.fetchEffect(i);
				if(A instanceof AbilityContainer)
				{
					boolean chantCheck=false;
					for(final Enumeration<Ability> a=((AbilityContainer)A).allAbilities();a.hasMoreElements();)
					{
						switch(a.nextElement().classificationCode()&Ability.ALL_ACODES)
						{
						case Ability.ACODE_CHANT:
							chantCheck = true;
							break;
						default:
							return false;
						}
					}
					return chantCheck;
				}
				else
				if(A!=null)
					return false;
			}
		}
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
						if(oldName.length()>0)
							commonTell(mob,L("Something went wrong! @x1 explodes!",(Character.toUpperCase(oldName.charAt(0))+oldName.substring(1))));
						buildingI.destroy();
					}
					else
					if(activity==CraftingActivity.LEARNING)
					{
						deconstructRecipeInto( buildingI, recipeHolder );
						buildingI.destroy();
					}
					else
						mob.addItem(buildingI);
				}
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	protected Item buildItem(Ability theSpell, int level)
	{
		buildingI=CMClass.getItem("GenMultiPotion");
		((Potion)buildingI).setSpellList(theSpell.ID());
		buildingI.setName(L("a potion of @x1",theSpell.name().toLowerCase()));
		buildingI.setDisplayText(L("a potion of @x1 sits here.",theSpell.name().toLowerCase()));
		buildingI.basePhyStats().setLevel(level);
		buildingI.phyStats().setLevel(level);
		((Drink)buildingI).setThirstQuenched(10);
		((Drink)buildingI).setLiquidHeld(100);
		((Drink)buildingI).setLiquidRemaining(100);
		buildingI.setDescription("");
		buildingI.text();
		return buildingI;
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
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,-1);

		if(autoGenerate>0)
		{
			final Ability theSpell=super.getCraftableSpellRecipeSpell(commands);
			if(theSpell==null)
				return false;
			int level=super.getCraftableSpellLevel(commands);
			if(level<0)
				level=1;
			buildingI=buildItem(theSpell, level);
			crafted.add(buildingI);
			return true;
		}
		if(commands.size()<1)
		{
			commonTell(mob,L("Brew what? Enter \"hbrew list\" for a list, \"hbrew learn <item>\" to learn recipes, or \"hbrew stop\" to cancel."));
			return false;
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String pos=commands.get(commands.size()-1);
		if(((commands.get(0)).equalsIgnoreCase("LIST")))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer(L("Potions you know how to brew:\n\r"));
			final int[] cols={
					CMLib.lister().fixColWidth(20,mob.session()),
					CMLib.lister().fixColWidth(5,mob.session())
				};
			buf.append(L("@x1 @x2 Ingredients\n\r",CMStrings.padRight(L("Chant"),cols[0]),CMStrings.padRight(L("Level"),cols[1])));
			final boolean fillUsage=(usage.size()==0);
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String spell=V.get(RCP_FINALNAME);
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(level>=0)
					&&((level<=xlevel(mob))||allFlag))
					{
						buf.append(CMStrings.padRight(A.name(),cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" ");
						for(int i=2;i<V.size();i++)
						{
							String s=V.get(i).toLowerCase();
							if(s.trim().length()==0)
								continue;
							if(s.endsWith("$"))
								s=s.substring(0,s.length()-1);
							if(fillUsage)
							{
								Integer I=usage.get(s.toUpperCase().trim());
								if(I==null)
									I=Integer.valueOf(0);
								else
									usage.remove(s.toUpperCase().trim());
								usage.put(s.toUpperCase().trim(),Integer.valueOf(I.intValue()+1));
							}
							buf.append(s+" ");
						}
						buf.append("\n\r");
					}
				}
			}
			commonTell(mob,buf.toString());
			/*
			for(Enumeration e=usage.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				Integer I=(Integer)usage.get(key);
				mob.tell(key+"="+I.intValue());
			}*/
			return true;
		}
		else
		if(((commands.get(0))).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		else
		if(commands.size()<2)
		{
			commonEmote(mob,L("You must specify what chant you wish to brew, and the container to brew it in."));
			return false;
		}
		else
		{
			buildingI=getTarget(mob,null,givenTarget,CMParms.parse(pos),Wearable.FILTER_UNWORNONLY);
			commands.remove(pos);
			if(buildingI==null)
				return false;
			if(!mob.isMine(buildingI))
			{
				commonTell(mob,L("You'll need to pick that up first."));
				return false;
			}
			if(!(buildingI instanceof Container))
			{
				commonTell(mob,L("There's nothing in @x1 to brew!",buildingI.name(mob)));
				return false;
			}
			if(!(buildingI instanceof Drink))
			{
				commonTell(mob,L("You can't drink out of a @x1.",buildingI.name(mob)));
				return false;
			}
			if(((Drink)buildingI).liquidRemaining()==0)
			{
				commonTell(mob,L("The @x1 contains no liquid base.  Water is probably fine.",buildingI.name(mob)));
				return false;
			}
			final String recipeName=CMParms.combine(commands,0);
			theSpell=null;
			int theLevel=-1;
			List<String> recipe=null;
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String spell=V.get(RCP_FINALNAME);
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(xlevel(mob)>=level)
					&&(A.name().equalsIgnoreCase(recipeName)))
					{
						theSpell=A;
						theLevel=level;
						recipe=V;
					}
				}
			}
			if((theSpell==null)||(recipe==null))
			{
				commonTell(mob,L("You don't know how to brew '@x1'.  Try \"hbrew list\" for a list.",recipeName));
				return false;
			}
			int experienceToLose=10;
			if((theSpell.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			{
				experienceToLose+=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*10;
				experienceToLose-=CMLib.ableMapper().qualifyingClassLevel(mob,theSpell)*5;
			}

			final List<Item> V=((Container)buildingI).getContents();
			// first check for all the right stuff
			for(int i=2;i<recipe.size();i++)
			{
				final String ingredient=recipe.get(i).trim();
				if(ingredient.length()>0)
				{
					boolean ok=false;
					for(int v=0;v<V.size();v++)
					{
						final Item I=V.get(v);
						if(CMLib.english().containsString(I.Name(),ingredient)
						||(RawMaterial.CODES.NAME(I.material()).equalsIgnoreCase(ingredient)))
						{
							ok=true;
							break;
						}
					}
					if(!ok)
					{
						commonTell(mob,L("This brew requires @x1.  Please place some inside the @x2 and try again.",ingredient.toLowerCase(),buildingI.name(mob)));
						return false;
					}
				}
			}
			// now check for unnecessary stuff
			for(int v=0;v<V.size();v++)
			{
				final Item I=V.get(v);
				boolean ok=false;
				for(int i=2;i<recipe.size();i++)
				{
					final String ingredient=recipe.get(i).trim();
					if(ingredient.length()>0)
						if(CMLib.english().containsString(I.Name(),ingredient)
						||(RawMaterial.CODES.NAME(I.material()).equalsIgnoreCase(ingredient)))
						{
							ok=true;
							break;
						}
				}
				if(!ok)
				{
					commonTell(mob,L("The @x1 must be removed from the @x2 before starting.",I.name(mob),buildingI.name(mob)));
					return false;
				}
			}

			if(experienceToLose<10)
				experienceToLose=10;

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;

			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			commonTell(mob,L("You lose @x1 experience points for the effort.",""+experienceToLose));
			oldName=buildingI.name();
			buildingI.destroy();
			buildingI=buildItem(theSpell, theLevel);
			playSound="hotspring.wav";

			int duration=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*5;
			if(duration<10)
				duration=10;

			messedUp=!proficiencyCheck(mob,0,auto);
			final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				buildingI=(Item)msg.target();
				beneficialAffect(mob,mob,asLevel,duration);
			}
		}
		return true;
	}
}
