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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

public class Alchemy extends SpellCraftingSkill implements ItemCraftor
{
	@Override
	public String ID()
	{
		return "Alchemy";
	}

	private final static String localizedName = CMLib.lang().L("Alchemy");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"BREW","ALCHEMY"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "MISC";
	}

	@Override
	public String parametersFormat()
	{
		return "SPELL_ID\tRESOURCE_NAME";
	}

	@Override
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}

	String oldName="";
	protected Ability theSpell=null;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)affected;
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
					commonEmote(mob,L("<S-NAME> start(s) praying for @x1.",buildingI.name()));
					displayText=L("You are praying for @x1",buildingI.name());
					verb=L("praying for @x1",buildingI.name());
				}
				else
				{
					commonEmote(mob,L("<S-NAME> start(s) brewing @x1.",buildingI.name()));
					displayText=L("You are brewing @x1",buildingI.name());
					verb=L("brewing @x1",buildingI.name());
					playSound="hotspring.wav";
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	protected boolean doLearnRecipe(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		fireRequired=false;
		return super.doLearnRecipe( mob, commands, givenTarget, auto, asLevel );
	}

	@Override
	public String parametersFile()
	{
		return "alchemy.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return false;
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
		if(!(I instanceof Potion))
			return false;
		final Potion P=(Potion)I;
		if((P.liquidType()==RawMaterial.RESOURCE_LIQUOR)
		||(P.liquidType()==RawMaterial.RESOURCE_POISON))
			return false;
		final List<Ability> spells=P.getSpells();
		if((spells == null)||(spells.size()==0))
			return false;
		for(final Ability A : spells)
			if(((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_SPELL)
			&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PRAYER))
				return false;
		return true;
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
						if(activity==CraftingActivity.LEARNING)
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

	protected int spellLevel(MOB mob, Ability A)
	{
		int lvl=CMLib.ableMapper().qualifyingLevel(mob,A);
		if(lvl<0)
			lvl=CMLib.ableMapper().lowestQualifyingLevel(A.ID());
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

	@Override
	public ItemKeyPair craftItem(String recipe)
	{
		return craftItem(recipe,0,false, false);
	}

	protected Item buildItem(Ability theSpell, int level)
	{
		buildingI=CMClass.getItem("GenPotion");
		((Potion)buildingI).setSpellList(theSpell.ID());
		buildingI.setName(L("a potion of @x1",theSpell.name().toLowerCase()));
		buildingI.setDisplayText(L("a potion of @x1 sits here.",theSpell.name().toLowerCase()));
		buildingI.setDescription("");
		buildingI.basePhyStats().setLevel(level);
		buildingI.phyStats().setLevel(level);
		buildingI.recoverPhyStats();
		buildingI.text();
		return buildingI;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new Vector<Item>(1));
	}
	
	@Override
	protected boolean autoGenInvoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel, int autoGenerate, boolean forceLevels, List<Item> crafted)
	{
		if(autoGenerate>0)
		{
			final Ability theSpell=super.getCraftableSpellRecipeSpell(commands);
			if(theSpell==null)
				return false;
			final int level=spellLevel(mob,theSpell);
			buildingI=buildItem(theSpell, level);
			crafted.add(buildingI);
			if(forceLevels)
			{
				final int minLevel=CMLib.ableMapper().lowestQualifyingLevel(theSpell.ID());
				buildingI.basePhyStats().setLevel(minLevel);
				buildingI.phyStats().setLevel(buildingI.basePhyStats().level());
			}
			return true;
		}
		if(super.checkStop(mob, commands))
			return true;
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,0);
		if(commands.size()<1)
		{
			commonTell(mob,L("Brew what? Enter \"brew list\" for a list, or \"brew stop\" to cancel."));
			return false;
		}
		final int[] cols={
				CMLib.lister().fixColWidth(25,mob.session()),
			};
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String pos=commands.get(commands.size()-1);
		if(((commands.get(0))).equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer(L("Potions you know how to brew:\n\r"));
			buf.append(CMStrings.padRight(L("Spell"),cols[0])+" "+CMStrings.padRight(L("Spell"),cols[0])+" "+CMStrings.padRight(L("Spell"),cols[0]));
			int toggler=1;
			final int toggleTop=3;
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String spell=V.get(0);
					final Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&((spellLevel(mob,A)>=0)||(allFlag))
					&&((xlevel(mob)>=spellLevel(mob,A))||(allFlag))
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(spell,mask)))
					{
						buf.append(CMStrings.padRight(A.name(),cols[0])+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop)
							toggler=1;
					}
				}
			}
			if(toggler!=1)
				buf.append("\n\r");
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if((!auto)&&(commands.size()<2))
		{
			commonEmote(mob,L("You must specify what magic you wish to brew, and the container to brew it in."));
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
			if(buildingI.material()!=RawMaterial.RESOURCE_GLASS)
			{
				commonTell(mob,L("You can only brew into glass containers."));
				return false;
			}
			activity = CraftingActivity.CRAFTING;
			final String recipeName=CMParms.combine(commands,0);
			theSpell=null;
			int theSpellLevel=1;
			String ingredient="";
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String spell=V.get(0);
					final Ability A=mob.fetchAbility(spell);
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
				commonTell(mob,L("You don't know how to brew '@x1'.  Try \"brew list\" for a list.",recipeName));
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
				final Item fire=getRequiredFire(mob,0);
				if(fire==null)
					return false;
				experienceToLose+=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*10;
				experienceToLose-=CMLib.ableMapper().qualifyingClassLevel(mob,theSpell)*5;
			}
			final int resourceType=RawMaterial.CODES.FIND_IgnoreCase(ingredient);

			boolean found=false;
			final List<Item> V=((Container)buildingI).getContents();
			if(resourceType>0)
			{
				if(((Drink)buildingI).liquidType()==resourceType)
				{
					found=true;
					if(V.size()>0)
					{
						commonTell(mob,L("The extraneous stuff from the @x1 must be removed before starting.",buildingI.name(mob)));
						return false;
					}
				}
				else
				for(int i=0;i<V.size();i++)
				{
					final Item I=V.get(i);
					if(I.material()==resourceType)
						found=true;
					else
					{
						commonTell(mob,L("The @x1 must be removed from the @x2 before starting.",I.name(mob),buildingI.name(mob)));
						return false;
					}
				}
				if(!found)
				{
					commonTell(mob,L("This potion requires @x1.  Please place some inside the @x2 and try again.",ingredient,buildingI.name(mob)));
					return false;
				}
			}
			if(experienceToLose<10)
				experienceToLose=10;

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;

			playSound=null;
			experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			commonTell(mob,L("You lose @x1 experience points for the effort.",""+experienceToLose));
			oldName=buildingI.name();
			buildingI.destroy();
			buildingI=buildItem(theSpell, theSpellLevel);
			setBrand(mob, buildingI);

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
