package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
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
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class ScrollScribing extends CraftingSkill implements ItemCraftor
{
	public String ID() { return "ScrollScribing"; }
	public String name(){ return "Scroll Scribing";}
	private static final String[] triggerStrings = {"ENSCRIBE","SCROLLSCRIBE","SCROLLSCRIBING"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int iniTrainsRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_SKILLTRAINCOST);}
	protected int iniPracticesRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_SKILLPRACCOST);}
    public String supportedResourceString(){return "MISC";}
    public String parametersFormat(){ return "SPELL_ID\tRESOURCE_NAME";}

	String oldName="";
    protected Ability theSpell=null;
	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(theSpell==null))
			{
				aborted=true;
				unInvoke();
			}
			else
			if(tickUp==0)
			{
				commonEmote(mob,"<S-NAME> start(s) scribing "+building.name()+".");
				displayText="You are scribing "+building.name();
				verb="scribing "+building.name();
			}
		}
		return super.tick(ticking,tickID);
	}

    public String parametersFile(){ return "scribing.txt";}
    protected Vector loadRecipes(){return super.loadRecipes(parametersFile());}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(oldName.length()>0)
							commonTell(mob,"Something went wrong! "+(Character.toUpperCase(oldName.charAt(0))+oldName.substring(1))+" explodes!");
					}
					else
						mob.addInventory(building);
				}
				building=null;
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

	public Vector craftItem(String recipe) { return craftItem(recipe,0); }

	protected Item buildItem(Ability theSpell)
	{
		building=CMClass.getItem("GenScroll");
		((Scroll)building).setSpellList(theSpell.ID());
		building.setName("a scroll of "+theSpell.name().toLowerCase());
		building.setDisplayText("a scroll of "+theSpell.name().toLowerCase()+" sits here.");
		building.setDescription("");
		building.recoverEnvStats();
		building.setUsesRemaining(1);
		building.text();
		return building;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((auto)&&(givenTarget==this)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			commands.removeElementAt(0);
			Ability theSpell=super.getCraftableSpellRecipe(commands);
			if(theSpell==null) return false;
			building=buildItem(theSpell);
			commands.addElement(building);
			return true;
		}
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,0);
		if(commands.size()<1)
		{
			commonTell(mob,"Enscribe what? Enter \"enscribe list\" for a list.");
			return false;
		}
		Vector recipes=addRecipes(mob,loadRecipes());
		String pos=(String)commands.lastElement();
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement())).equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer("Scrolls you know how to enscribe:\n\r");
			buf.append(CMStrings.padRight("Spell",25)+" "+CMStrings.padRight("Spell",25)+" "+CMStrings.padRight("Spell",25));
			int toggler=1;
			int toggleTop=3;
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String spell=(String)V.elementAt(0);
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(spellLevel(mob,A)>=0)
					&&(xlevel(mob)>=spellLevel(mob,A))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(spell,mask)))
					{
						buf.append(CMStrings.padRight(A.name(),25)+((toggler!=toggleTop)?" ":"\n\r"));
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
			building=getTarget(mob,null,givenTarget,CMParms.parse(pos),Wearable.FILTER_UNWORNONLY);
			commands.remove(pos);
			if(building==null) return false;
			if(!mob.isMine(building))
			{
				commonTell(mob,"You'll need to pick that up first.");
				return false;
			}
			if(!CMath.bset(building.material(),RawMaterial.MATERIAL_PAPER))
			{
				commonTell(mob,building.name()+" isn't even made of paper!");
				return false;
			}
			if(!(building instanceof Scroll))
			{
				commonTell(mob,"There's can't enscribe magic on "+building.name()+"!");
				return false;
			}
			if(((Scroll)building).getSpells().size()>0)
			{
				commonTell(mob,"You can only scribe on blank scrolls.");
				return false;
			}
			String recipeName=CMParms.combine(commands,0);
			theSpell=null;
			String ingredient="";
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String spell=(String)V.elementAt(0);
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(xlevel(mob)>=spellLevel(mob,A))
					&&(A.name().equalsIgnoreCase(recipeName)))
					{
						theSpell=A;
						ingredient=(String)V.elementAt(1);
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
			oldName=building.name();
			building.destroy();
			building=buildItem(theSpell);
			building.setSecretIdentity("This is the work of "+mob.Name()+".");

			int duration=CMLib.ableMapper().qualifyingLevel(mob,theSpell)*5;
			if(duration<10) duration=10;
			messedUp=!proficiencyCheck(mob,0,auto);

			CMMsg msg=CMClass.getMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				building=(Item)msg.target();
				beneficialAffect(mob,mob,asLevel,duration);
			}
		}
		return true;
	}
}
