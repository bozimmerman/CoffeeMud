package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
public class Herbalism extends SpellCraftingSkill implements ItemCraftor
{
	public String ID() { return "Herbalism"; }
	public String name(){ return "Herbalism";}
	private static final String[] triggerStrings = {"HERBALISM","HERBREW","HBREW"};
	public String[] triggerStrings(){return triggerStrings;}
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost() { return CMProps.getSkillTrainCostFormula(ID()); }
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
	private static final Hashtable usage=new Hashtable();

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((buildingI==null)
			||(theSpell==null))
			{
				aborted=true;
				unInvoke();
			}
			else
			if(tickUp==0)
			{
				commonEmote(mob,"<S-NAME> start(s) brewing "+buildingI.name()+".");
				displayText="You are brewing "+buildingI.name();
				verb="brewing "+buildingI.name();
			}
		}
		return super.tick(ticking,tickID);
	}

	public String parametersFile(){ return "herbalism.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public ItemKeyPair craftItem(String recipe) { return craftItem(recipe,0); }

	public boolean supportsDeconstruction() { return true; }

	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return "Not implemented";
	}

	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(I instanceof Drink)
		{
			Drink D=(Drink)I;
			if((D.liquidType()==RawMaterial.RESOURCE_LIQUOR)
			||(D.liquidType()==RawMaterial.RESOURCE_POISON))
				return false;
		}
		if(I instanceof Potion)
		{
			Potion P=(Potion)I;
			List<Ability> spells=P.getSpells();
			if((spells == null)||(spells.size()==0))
				return false;
			boolean chantCheck=false;
			for(Ability A : spells)
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
				Ability A=I.fetchEffect(i);
				if(A instanceof AbilityContainer)
				{
					boolean chantCheck=false;
					for(Enumeration<Ability> a=((AbilityContainer)A).abilities();a.hasMoreElements();)
						switch(a.nextElement().classificationCode()&Ability.ALL_ACODES)
						{
						case Ability.ACODE_CHANT: chantCheck=true; break;
						default: return false;
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
					{
						if(activity == CraftingActivity.LEARNING)
							commonEmote(mob,"<S-NAME> fail(s) to learn how to make "+buildingI.name()+".");
						else
						if(oldName.length()>0)
							commonTell(mob,"Something went wrong! "+(Character.toUpperCase(oldName.charAt(0))+oldName.substring(1))+" explodes!");
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
		buildingI.setName("a potion of "+theSpell.name().toLowerCase());
		buildingI.setDisplayText("a potion of "+theSpell.name().toLowerCase()+" sits here.");
		buildingI.basePhyStats().setLevel(level);
		buildingI.phyStats().setLevel(level);
		((Drink)buildingI).setThirstQuenched(10);
		((Drink)buildingI).setLiquidHeld(100);
		((Drink)buildingI).setLiquidRemaining(100);
		buildingI.setDescription("");
		buildingI.text();
		return buildingI;
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,-1);
		if((auto)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			commands.removeElementAt(0);
			Ability theSpell=super.getCraftableSpellRecipeSpell(commands);
			if(theSpell==null) return false;
			int level=super.getCraftableSpellLevel(commands);
			if(level<0) level=1;
			buildingI=buildItem(theSpell, level);
			commands.addElement(buildingI);
			return true;
		}
		if(commands.size()<1)
		{
			commonTell(mob,"Brew what? Enter \"hbrew list\" for a list, \"hbrew learn <item>\" to learn recipes, or \"hbrew stop\" to cancel.");
			return false;
		}
		List<List<String>> recipes=addRecipes(mob,loadRecipes());
		String pos=(String)commands.lastElement();
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement()).equalsIgnoreCase("LIST")))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			StringBuffer buf=new StringBuffer("Potions you know how to brew:\n\r");
			int[] cols={
					ListingLibrary.ColFixer.fixColWidth(20,mob.session()),
					ListingLibrary.ColFixer.fixColWidth(5,mob.session())
				};
			buf.append(CMStrings.padRight("Chant",cols[0])+" "+CMStrings.padRight("Level",cols[1])+" Ingredients\n\r");
			boolean fillUsage=(usage.size()==0);
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String spell=V.get(RCP_FINALNAME);
					int level=CMath.s_int(V.get(RCP_LEVEL));
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(level>=0)
					&&((level<=xlevel(mob))||allFlag))
					{
						buf.append(CMStrings.padRight(A.name(),cols[0])+" "+CMStrings.padRight(""+level,cols[1])+" ");
						for(int i=2;i<V.size();i++)
						{
							String s=V.get(i).toLowerCase();
							if(s.trim().length()==0) continue;
							if(s.endsWith("$")) s=s.substring(0,s.length()-1);
							if(fillUsage)
							{
								Integer I=(Integer)usage.get(s.toUpperCase().trim());
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
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("learn"))
		{
			return doLearnRecipe(mob, commands, givenTarget, auto, asLevel);
		}
		else
		if(commands.size()<2)
		{
			commonEmote(mob,"You must specify what chant you wish to brew, and the container to brew it in.");
			return false;
		}
		else
		{
			buildingI=getTarget(mob,null,givenTarget,CMParms.parse(pos),Wearable.FILTER_UNWORNONLY);
			commands.remove(pos);
			if(buildingI==null) return false;
			if(!mob.isMine(buildingI))
			{
				commonTell(mob,"You'll need to pick that up first.");
				return false;
			}
			if(!(buildingI instanceof Container))
			{
				commonTell(mob,"There's nothing in "+buildingI.name(mob)+" to brew!");
				return false;
			}
			if(!(buildingI instanceof Drink))
			{
				commonTell(mob,"You can't drink out of a "+buildingI.name(mob)+".");
				return false;
			}
			if(((Drink)buildingI).liquidRemaining()==0)
			{
				commonTell(mob,"The "+buildingI.name(mob)+" contains no liquid base.  Water is probably fine.");
				return false;
			}
			String recipeName=CMParms.combine(commands,0);
			theSpell=null;
			int theLevel=-1;
			List<String> recipe=null;
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String spell=V.get(RCP_FINALNAME);
					int level=CMath.s_int(V.get(RCP_LEVEL));
					Ability A=mob.fetchAbility(spell);
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
				commonTell(mob,"You don't know how to brew '"+recipeName+"'.  Try \"hbrew list\" for a list.");
				return false;
			}
			int experienceToLose=10;
			if((theSpell.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
			{
				experienceToLose+=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*10;
				experienceToLose-=CMLib.ableMapper().qualifyingClassLevel(mob,theSpell)*5;
			}

			List<Item> V=((Container)buildingI).getContents();
			// first check for all the right stuff
			for(int i=2;i<recipe.size();i++)
			{
				String ingredient=recipe.get(i).trim();
				if(ingredient.length()>0)
				{
					boolean ok=false;
					for(int v=0;v<V.size();v++)
					{
						Item I=V.get(v);
						if(CMLib.english().containsString(I.Name(),ingredient)
						||(RawMaterial.CODES.NAME(I.material()).equalsIgnoreCase(ingredient)))
						{ ok=true; break;}
					}
					if(!ok)
					{
						commonTell(mob,"This brew requires "+ingredient.toLowerCase()+".  Please place some inside the "+buildingI.name(mob)+" and try again.");
						return false;
					}
				}
			}
			// now check for unnecessary stuff
			for(int v=0;v<V.size();v++)
			{
				Item I=V.get(v);
				boolean ok=false;
				for(int i=2;i<recipe.size();i++)
				{
					String ingredient=recipe.get(i).trim();
					if(ingredient.length()>0)
						if(CMLib.english().containsString(I.Name(),ingredient)
						||(RawMaterial.CODES.NAME(I.material()).equalsIgnoreCase(ingredient)))
						{ ok=true; break;}
				}
				if(!ok)
				{
					commonTell(mob,"The "+I.name(mob)+" must be removed from the "+buildingI.name(mob)+" before starting.");
					return false;
				}
			}

			if(experienceToLose<10) experienceToLose=10;

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;

			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			commonTell(mob,"You lose "+experienceToLose+" experience points for the effort.");
			oldName=buildingI.name();
			buildingI.destroy();
			buildingI=buildItem(theSpell, theLevel);
			playSound="hotspring.wav";

			int duration=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*5;
			if(duration<10) duration=10;

			messedUp=!proficiencyCheck(mob,0,auto);
			CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),null);
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
