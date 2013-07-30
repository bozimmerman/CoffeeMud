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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Alchemy extends SpellCraftingSkill implements ItemCraftor
{
	public String ID() { return "Alchemy"; }
	public String name(){ return "Alchemy";}
	private static final String[] triggerStrings = {"BREW","ALCHEMY"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "MISC";}
	public String parametersFormat(){ return "SPELL_ID\tRESOURCE_NAME";}
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost() { return CMProps.getSkillTrainCostFormula(ID()); }

	String oldName="";
	protected Ability theSpell=null;
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((buildingI==null)
			||((fireRequired)&&(getRequiredFire(mob,0)==null))
			||(theSpell==null))
			{
				aborted=true;
				unInvoke();
			}
			else
			if(tickUp==0)
			{
				if((theSpell.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				{
					commonEmote(mob,"<S-NAME> start(s) praying for "+buildingI.name()+".");
					displayText="You are praying for "+buildingI.name();
					verb="praying for "+buildingI.name();
				}
				else
				{
					commonEmote(mob,"<S-NAME> start(s) brewing "+buildingI.name()+".");
					displayText="You are brewing "+buildingI.name();
					verb="brewing "+buildingI.name();
					playSound="hotspring.wav";
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	protected boolean doLearnRecipe(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		fireRequired=false;
		return super.doLearnRecipe( mob, commands, givenTarget, auto, asLevel );
	}
	
	public String parametersFile(){ return "alchemy.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public boolean supportsDeconstruction() { return false; }

	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return "Not implemented";
	}

	public boolean mayICraft(final Item I)
	{
		if(I==null) return false;
		if(!super.mayBeCrafted(I))
			return false;
		if(!(I instanceof Potion)) return false;
		Potion P=(Potion)I;
		if((P.liquidType()==RawMaterial.RESOURCE_LIQUOR)
		||(P.liquidType()==RawMaterial.RESOURCE_POISON))
			return false;
		List<Ability> spells=P.getSpells();
		if((spells == null)||(spells.size()==0))
			return false;
		for(Ability A : spells)
			if(((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_SPELL)
			&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PRAYER))
				return false;
		return true;
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
						if(activity==CraftingActivity.LEARNING)
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

	protected int spellLevel(MOB mob, Ability A)
	{
		int lvl=CMLib.ableMapper().qualifyingLevel(mob,A);
		if(lvl<0) lvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
		switch(lvl)
		{
		case 0: return lvl;
		case 1: return lvl;
		case 2: return lvl+1;
		case 3: return lvl+1;
		case 4: return lvl+2;
		case 5: return lvl+2;
		case 6: return lvl+3;
		case 7: return lvl+3;
		case 8: return lvl+4;
		case 9: return lvl+4;
		default: return lvl+5;
		}
	}

	public ItemKeyPair craftItem(String recipe) { return craftItem(recipe,0); }

	protected Item buildItem(Ability theSpell, int level)
	{
		buildingI=CMClass.getItem("GenPotion");
		((Potion)buildingI).setSpellList(theSpell.ID());
		buildingI.setName("a potion of "+theSpell.name().toLowerCase());
		buildingI.setDisplayText("a potion of "+theSpell.name().toLowerCase()+" sits here.");
		buildingI.setDescription("");
		buildingI.basePhyStats().setLevel(level);
		buildingI.phyStats().setLevel(level);
		buildingI.recoverPhyStats();
		buildingI.text();
		return buildingI;
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((auto)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			commands.removeElementAt(0);
			Ability theSpell=super.getCraftableSpellRecipeSpell(commands);
			if(theSpell==null) return false;
			int level=spellLevel(mob,theSpell);
			buildingI=buildItem(theSpell, level);
			commands.addElement(buildingI);
			return true;
		}
		if(super.checkStop(mob, commands))
			return true;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,0);
		if(commands.size()<1)
		{
			commonTell(mob,"Brew what? Enter \"brew list\" for a list, or \"brew stop\" to cancel.");
			return false;
		}
		int[] cols={
				ListingLibrary.ColFixer.fixColWidth(25,mob.session()),
			};
		List<List<String>> recipes=addRecipes(mob,loadRecipes());
		String pos=(String)commands.lastElement();
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			StringBuffer buf=new StringBuffer("Potions you know how to brew:\n\r");
			buf.append(CMStrings.padRight("Spell",cols[0])+" "+CMStrings.padRight("Spell",cols[0])+" "+CMStrings.padRight("Spell",cols[0]));
			int toggler=1;
			int toggleTop=3;
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String spell=V.get(0);
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&((spellLevel(mob,A)>=0)||(allFlag))
					&&((xlevel(mob)>=spellLevel(mob,A))||(allFlag))
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(spell,mask)))
					{
						buf.append(CMStrings.padRight(A.name(),cols[0])+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			if(toggler!=1) buf.append("\n\r");
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if((!auto)&&(commands.size()<2))
		{
			commonEmote(mob,"You must specify what magic you wish to brew, and the container to brew it in.");
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
			if(buildingI.material()!=RawMaterial.RESOURCE_GLASS)
			{
				commonTell(mob,"You can only brew into glass containers.");
				return false;
			}
			activity = CraftingActivity.CRAFTING;
			String recipeName=CMParms.combine(commands,0);
			theSpell=null;
			int theSpellLevel=1;
			String ingredient="";
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String spell=V.get(0);
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(xlevel(mob)>=spellLevel(mob,A))
					&&(A.name().equalsIgnoreCase(recipeName)))
					{
						theSpell=A;
						theSpellLevel=spellLevel(mob, A);
						ingredient=V.get(1);
					}
				}
			}
			if(theSpell==null)
			{
				commonTell(mob,"You don't know how to brew '"+recipeName+"'.  Try \"brew list\" for a list.");
				return false;
			}
			int experienceToLose=10;
			if((theSpell.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			{
				fireRequired=false;
				experienceToLose+=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*10;
				experienceToLose-=CMLib.ableMapper().qualifyingClassLevel(mob,theSpell)*5;
			}
			else
			{
				fireRequired=true;
				Item fire=getRequiredFire(mob,0);
				if(fire==null) return false;
				experienceToLose+=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*10;
				experienceToLose-=CMLib.ableMapper().qualifyingClassLevel(mob,theSpell)*5;
			}
			int resourceType=RawMaterial.CODES.FIND_IgnoreCase(ingredient);

			boolean found=false;
			List<Item> V=((Container)buildingI).getContents();
			if(resourceType>0)
			{
				if(((Drink)buildingI).liquidType()==resourceType)
				{
					found=true;
					if(V.size()>0)
					{
						commonTell(mob,"The extraneous stuff from the "+buildingI.name(mob)+" must be removed before starting.");
						return false;
					}
				}
				else
				for(int i=0;i<V.size();i++)
				{
					Item I=V.get(i);
					if(I.material()==resourceType)
						found=true;
					else
					{
						commonTell(mob,"The "+I.name(mob)+" must be removed from the "+buildingI.name(mob)+" before starting.");
						return false;
					}
				}
				if(!found)
				{
					commonTell(mob,"This potion requires "+ingredient+".  Please place some inside the "+buildingI.name(mob)+" and try again.");
					return false;
				}
			}
			if(experienceToLose<10) experienceToLose=10;

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;

			playSound=null;
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			commonTell(mob,"You lose "+experienceToLose+" experience points for the effort.");
			oldName=buildingI.name();
			buildingI.destroy();
			buildingI=buildItem(theSpell, theSpellLevel);
			buildingI.setSecretIdentity(getBrand(mob));

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
