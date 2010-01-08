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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
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
public class Herbalism extends CraftingSkill implements ItemCraftor
{
	public String ID() { return "Herbalism"; }
	public String name(){ return "Herbalism";}
	private static final String[] triggerStrings = {"HERBALISM","HERBREW","HBREW"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int iniTrainsRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_SKILLTRAINCOST);}
	protected int iniPracticesRequired(){return CMProps.getIntVar(CMProps.SYSTEMI_SKILLPRACCOST);}
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
			if((building==null)
			||(theSpell==null))
			{
				aborted=true;
				unInvoke();
			}
			else
			if(tickUp==0)
			{
				commonEmote(mob,"<S-NAME> start(s) brewing "+building.name()+".");
				displayText="You are brewing "+building.name();
				verb="brewing "+building.name();
			}
		}
		return super.tick(ticking,tickID);
	}

    public String parametersFile(){ return "herbalism.txt";}
    protected Vector loadRecipes(){return super.loadRecipes(parametersFile());}

	public Vector craftItem(String recipe) { return craftItem(recipe,0); }
	
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
	
	protected Item buildItem(Ability theSpell)
	{
		building=CMClass.getItem("GenMultiPotion");
		((Potion)building).setSpellList(theSpell.ID());
		building.setName("a potion of "+theSpell.name().toLowerCase());
		building.setDisplayText("a potion of "+theSpell.name().toLowerCase()+" sits here.");
		((Drink)building).setThirstQuenched(10);
		((Drink)building).setLiquidHeld(100);
		((Drink)building).setLiquidRemaining(100);
		building.setDescription("");
		building.text();
		return building;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,-1);
		if((auto)&&(givenTarget==this)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			commands.removeElementAt(0);
			Ability theSpell=super.getCraftableSpellRecipe(commands);
			if(theSpell==null) return false;
			building=buildItem(theSpell);
			commands.addElement(building);
			return true;
		}
		if(commands.size()<1)
		{
			commonTell(mob,"Brew what? Enter \"hbrew list\" for a list.");
			return false;
		}
		Vector recipes=addRecipes(mob,loadRecipes());
		String pos=(String)commands.lastElement();
		if((commands.firstElement() instanceof String)&&(((String)commands.firstElement()).equalsIgnoreCase("LIST")))
		{
			//String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer("Potions you know how to brew:\n\r");
			buf.append(CMStrings.padRight("Chant",20)+" "+CMStrings.padRight("Level",5)+" Ingredients\n\r");
			boolean fillUsage=(usage.size()==0);
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String spell=(String)V.elementAt(0);
					int level=CMath.s_int((String)V.elementAt(1));
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(level>=0)
					&&(xlevel(mob)>=level))
					{
						buf.append(CMStrings.padRight(A.name(),20)+" "+CMStrings.padRight(""+level,5)+" ");
						for(int i=2;i<V.size();i++)
						{
							String s=((String)V.elementAt(i)).toLowerCase();
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
		if(commands.size()<2)
		{
			commonEmote(mob,"You must specify what chant you wish to brew, and the container to brew it in.");
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
			if(!(building instanceof Container))
			{
				commonTell(mob,"There's nothing in "+building.name()+" to brew!");
				return false;
			}
			if(!(building instanceof Drink))
			{
				commonTell(mob,"You can't drink out of a "+building.name()+".");
				return false;
			}
			if(((Drink)building).liquidRemaining()==0)
			{
				commonTell(mob,"The "+building.name()+" contains no liquid base.  Water is probably fine.");
				return false;
			}
			String recipeName=CMParms.combine(commands,0);
			theSpell=null;
			Vector recipe=null;
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String spell=(String)V.elementAt(0);
					int level=CMath.s_int((String)V.elementAt(1));
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(xlevel(mob)>=level)
					&&(A.name().equalsIgnoreCase(recipeName)))
					{
						theSpell=A;
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

			Vector V=((Container)building).getContents();
			// first check for all the right stuff
			for(int i=2;i<recipe.size();i++)
			{
				String ingredient=((String)recipe.elementAt(i)).trim();
				if(ingredient.length()>0)
				{
					boolean ok=false;
					for(int v=0;v<V.size();v++)
					{
						Item I=(Item)V.elementAt(v);
						if(CMLib.english().containsString(I.Name(),ingredient)
						||(RawMaterial.CODES.NAME(I.material()).equalsIgnoreCase(ingredient)))
						{ ok=true; break;}
					}
					if(!ok)
					{
						commonTell(mob,"This brew requires "+ingredient.toLowerCase()+".  Please place some inside the "+building.name()+" and try again.");
						return false;
					}
				}
			}
			// now check for unnecessary stuff
			for(int v=0;v<V.size();v++)
			{
				Item I=(Item)V.elementAt(v);
				boolean ok=false;
				for(int i=2;i<recipe.size();i++)
				{
					String ingredient=((String)recipe.elementAt(i)).trim();
					if(ingredient.length()>0)
						if(CMLib.english().containsString(I.Name(),ingredient)
						||(RawMaterial.CODES.NAME(I.material()).equalsIgnoreCase(ingredient)))
						{ ok=true; break;}
				}
				if(!ok)
				{
					commonTell(mob,"The "+I.name()+" must be removed from the "+building.name()+" before starting.");
					return false;
				}
			}

			if(experienceToLose<10) experienceToLose=10;

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;

            experienceToLose=getXPCOSTAdjustment(mob,experienceToLose);
			CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
			commonTell(mob,"You lose "+experienceToLose+" experience points for the effort.");
			oldName=building.name();
			building.destroy();
			building=buildItem(theSpell);
            playSound="hotspring.wav";

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
