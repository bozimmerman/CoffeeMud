package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Herbalism extends CommonSkill
{
	public String ID() { return "Herbalism"; }
	public String name(){ return "Herbalism";}
	private static final String[] triggerStrings = {"HERBALISM","HERBREW","HBREW"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLPRACCOST);}

	private Item building=null;
	String oldName="";
	private Ability theSpell=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public Herbalism()
	{
		super();
	}

	public Environmental newInstance(){ return new Herbalism();	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
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

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("HERBALISM RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"herbalism.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Herbalism","Recipes not found!");
			Resources.submitResource("HERBALISM RECIPES",V);
		}
		return V;
	}

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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			commonTell(mob,"Brew what? Enter \"hbrew list\" for a list.");
			return false;
		}
		Vector recipes=loadRecipes();
		String pos=(String)commands.lastElement();
		if(pos.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer("Potions you know how to brew:\n\r");
			buf.append(Util.padRight("Chant",20)+" "+Util.padRight("Level",5)+" Ingredients\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String spell=(String)V.elementAt(0);
					int level=Util.s_int((String)V.elementAt(1));
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(level>=0)
					&&(mob.envStats().level()>=level))
					{
						buf.append(Util.padRight(A.name(),20)+" "+Util.padRight(""+level,5)+" ");
						for(int i=2;i<V.size();i++)
						{
							String s=((String)V.elementAt(i)).toLowerCase();
							if(s.trim().length()==0) continue;
							if(s.endsWith("$")) s=s.substring(0,s.length()-1);
							buf.append(s+" ");
						}
						buf.append("\n\r");
					}
				}
			}
			commonTell(mob,buf.toString());
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
			building=getTarget(mob,null,givenTarget,Util.parse(pos),Item.WORN_REQ_UNWORNONLY);
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
			String recipeName=Util.combine(commands,0);
			theSpell=null;
			Vector recipe=null;
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String spell=(String)V.elementAt(0);
					int level=Util.s_int((String)V.elementAt(1));
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(mob.envStats().level()>=level)
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
			if((theSpell.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
			{
				experienceToLose+=CMAble.qualifyingLevel(mob,theSpell)*10;
				experienceToLose-=CMAble.qualifyingClassLevel(mob,theSpell)*5;
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
						if(CoffeeUtensils.containsString(I.Name(),ingredient)
						||(EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK].equalsIgnoreCase(ingredient)))
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
						if(CoffeeUtensils.containsString(I.Name(),ingredient)
						||(EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK].equalsIgnoreCase(ingredient)))
						{ ok=true; break;}
				}
				if(!ok)
				{
					commonTell(mob,"The "+I.name()+" must be removed from the "+building.name()+" before starting.");
					return false;
				}
			}

			if(experienceToLose<10) experienceToLose=10;

			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;

			ExternalPlay.postExperience(mob,null,null,-experienceToLose,false);
			commonTell(mob,"You lose "+experienceToLose+" experience points for the effort.");
			oldName=building.name();
			building.destroy();
			building=CMClass.getItem("GenMultiPotion");
			((Potion)building).setSpellList(theSpell.ID());
			building.setName("a potion of "+theSpell.name().toLowerCase());
			building.setDisplayText("a potion of "+theSpell.name().toLowerCase()+" sits here.");
			((Drink)building).setThirstQuenched(10);
			((Drink)building).setLiquidHeld(100);
			((Drink)building).setLiquidRemaining(100);
			building.setDescription("");
			building.recoverEnvStats();
			building.text();

			int completion=CMAble.qualifyingLevel(mob,theSpell)*5;
			if(completion<10) completion=10;
			
			messedUp=!profficiencyCheck(0,auto);
			
			FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,completion);
			}
		}
		return true;
	}
}
