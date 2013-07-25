package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
public class ScrollScribing extends SpellCraftingSkill implements ItemCraftor
{
	public String ID() { return "ScrollScribing"; }
	public String name(){ return "Scroll Scribing";}
	private static final String[] triggerStrings = {"ENSCRIBE","SCROLLSCRIBE","SCROLLSCRIBING"};
	public String[] triggerStrings(){return triggerStrings;}
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost() { return CMProps.getSkillTrainCostFormula(ID()); }
	public String supportedResourceString(){return "MISC";}
	public String parametersFormat(){ return "SPELL_ID\tRESOURCE_NAME";}

	String oldName="";
	protected Ability theSpell=null;
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
				commonEmote(mob,"<S-NAME> start(s) scribing "+buildingI.name()+".");
				displayText="You are scribing "+buildingI.name();
				verb="scribing "+buildingI.name();
			}
		}
		return super.tick(ticking,tickID);
	}

	public String parametersFile(){ return "scribing.txt";}
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
					{
						if(oldName.length()>0)
							commonTell(mob,"Something went wrong! "+(Character.toUpperCase(oldName.charAt(0))+oldName.substring(1))+" explodes!");
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

	public boolean supportsDeconstruction() { return false; }

	public ItemKeyPair craftItem(String recipe) { return craftItem(recipe,0); }

	protected Item buildItem(Ability theSpell, int level)
	{
		buildingI=CMClass.getItem("GenScroll");
		((Scroll)buildingI).setSpellList(theSpell.ID());
		buildingI.setName("a scroll of "+theSpell.name().toLowerCase());
		buildingI.setDisplayText("a scroll of "+theSpell.name().toLowerCase()+" sits here.");
		buildingI.setDescription("");
		buildingI.basePhyStats().setLevel(level);
		buildingI.phyStats().setLevel(level);
		buildingI.recoverPhyStats();
		buildingI.setUsesRemaining(1);
		buildingI.text();
		return buildingI;
	}
	
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return "Not implemented";
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
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
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,0);
		if(commands.size()<1)
		{
			commonTell(mob,"Enscribe what? Enter \"enscribe list\" for a list, or \"enscribe stop\" to cancel.");
			return false;
		}
		List<List<String>> recipes=addRecipes(mob,loadRecipes());
		String pos=(String)commands.lastElement();
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer("Scrolls you know how to enscribe:\n\r");
			int colWidth=ListingLibrary.ColFixer.fixColWidth(25,mob.session());
			buf.append(CMStrings.padRight("Spell",colWidth)+" "+CMStrings.padRight("Spell",colWidth)+" "+CMStrings.padRight("Spell",colWidth));
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
					&&(spellLevel(mob,A)>=0)
					&&(xlevel(mob)>=spellLevel(mob,A))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(spell,mask)))
					{
						buf.append(CMStrings.padRight(A.name(),colWidth)+((toggler!=toggleTop)?" ":"\n\r"));
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
			commonEmote(mob,"You must specify what magic you wish to enscribe, and the paper to enscribe it in.");
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
			if(!CMath.bset(buildingI.material(),RawMaterial.MATERIAL_PAPER))
			{
				commonTell(mob,buildingI.name(mob)+" isn't even made of paper!");
				return false;
			}
			if(!(buildingI instanceof Scroll))
			{
				commonTell(mob,"There's can't enscribe magic on "+buildingI.name(mob)+"!");
				return false;
			}
			if(((Scroll)buildingI).getSpells().size()>0)
			{
				commonTell(mob,"You can only scribe on blank scrolls.");
				return false;
			}
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
						theSpellLevel=spellLevel(mob,A);
						ingredient=V.get(1);
					}
				}
			}
			if(theSpell==null)
			{
				commonTell(mob,"You don't know how to enscribe '"+recipeName+"'.  Try \"enscribe list\" for a list.");
				return false;
			}
			int experienceToLose=10;
			experienceToLose+=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*10;
			experienceToLose-=CMLib.ableMapper().qualifyingClassLevel(mob,theSpell)*5;
			int resourceType=RawMaterial.CODES.FIND_IgnoreCase(ingredient);

			int[][] data = null;
			if(resourceType>0)
			{
				int[] pm={resourceType};
				data=fetchFoundResourceData(mob,
											1,ingredient,pm,
											0,null,null,
											bundling,
											-1,
											null);
				if(data==null) return false;
			}
			if(experienceToLose<10) experienceToLose=10;

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;

			if((resourceType>0)&&(data != null))
				CMLib.materials().destroyResources(mob.location(),data[0][FOUND_AMT],data[0][FOUND_CODE],0,null);
			
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
