package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Blacksmithing extends CommonSkill
{
	public String ID() { return "Blacksmithing"; }
	public String name(){ return "Blacksmithing";}
	private static final String[] triggerStrings = {"BLACKSMITH","BLACKSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_ARMORDMG=8;
	private static final int RCP_SPELL=9;

	private Item building=null;
	private Item fire=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public Blacksmithing()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Blacksmithing();	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(fire==null)
			||(!Sense.isOnFire(fire))
			||(!mob.location().isContent(fire))
			||(mob.isMine(fire)))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("BLACKSMITHING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"blacksmith.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Blacksmith","Recipes not found!");
			Resources.submitResource("BLACKSMITHING RECIPES",V);
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
						commonTell(mob,"You've ruined "+building.name()+"!");
					else
						mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"blacksmith list\" for a list.");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Metals required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					if(level<=mob.envStats().level())
						buf.append(Util.padRight(item,20)+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		
			fire=getRequiredFire(mob);
			if(fire==null) return false;
		building=null;
		messedUp=false;
		String recipeName=Util.combine(commands,0);
		Vector foundRecipe=null;
		for(int r=0;r<recipes.size();r++)
		{
			Vector V=(Vector)recipes.elementAt(r);
			if(V.size()>0)
			{
				String item=(String)V.elementAt(RCP_FINALNAME);
				int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
				if((level<=mob.envStats().level())
				&&(replacePercent(item,"").equalsIgnoreCase(recipeName)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"blacksmith list\" for a list.");
			return false;
		}
		int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
		Item firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_METAL);
		if(firstWood==null)
			firstWood=findMostOfMaterial(mob.location(),EnvResource.MATERIAL_MITHRIL);
		int foundWood=0;
		if(firstWood!=null)
			foundWood=findNumberOfResource(mob.location(),firstWood.material());
		if(foundWood==0)
		{
			commonTell(mob,"There is no metal here to make anything from!  It might need to put it down first.");
			return false;
		}
		if(firstWood.material()==EnvResource.RESOURCE_MITHRIL)
			woodRequired=woodRequired/2;
		else
		if(firstWood.material()==EnvResource.RESOURCE_ADAMANTITE)
			woodRequired=woodRequired/3;
		if(woodRequired<1) woodRequired=1;
		if(foundWood<woodRequired)
		{
			commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		destroyResources(mob.location(),woodRequired,firstWood.material(),null,null);
		building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
		if(building==null)
		{
			commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
			return false;
		}
		completion=Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
		String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)]).toLowerCase();
		itemName=Util.startWithAorAn(itemName);
		building.setName(itemName);
		startStr="<S-NAME> start(s) smithing "+building.name()+".";
		displayText="You are smithing "+building.name();
		verb="smithing "+building.name();
		building.setDisplayText(itemName+" is here");
		building.setDescription(itemName+". ");
		building.baseEnvStats().setWeight(woodRequired);
		building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))+(woodRequired*(firstWood.baseGoldValue())));
		building.setMaterial(firstWood.material());
		building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
		building.setSecretIdentity("This is the work of "+mob.Name()+".");
		String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
		int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
		String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
		if(spell.length()>0)
		{
			String parm="";
			if(spell.indexOf(";")>0)
			{ 
				parm=spell.substring(spell.indexOf(";")+1);
				spell=spell.substring(0,spell.indexOf(";"));
			}
			Ability A=CMClass.getAbility(spell);
			A.setMiscText(parm);
			if(A!=null)	building.addNonUninvokableAffect(A);
		}
		if((misctype.equalsIgnoreCase("statue"))&&(!mob.isMonster()))
		{
			try
			{
				String of=mob.session().prompt("What is this a statue of?","");
				if(of.trim().length()==0)
					return false;
				building.setName(itemName+" of "+of.trim());
				building.setDisplayText(itemName+" of "+of.trim()+" is here");
				building.setDescription(itemName+" of "+of.trim()+". ");
			}
			catch(java.io.IOException x)
			{
				return false;
			}
		}
		else
		if(building instanceof Container)
		{
			((Container)building).setCapacity(capacity+woodRequired);
			if(misctype.equalsIgnoreCase("LID"))
				((Container)building).setLidsNLocks(true,false,false,false);
			else
			if(misctype.equalsIgnoreCase("LOCK"))
			{
				((Container)building).setLidsNLocks(true,false,true,false);
				((Container)building).setKeyName(new Double(Math.random()).toString());
			}
			else
				((Container)building).setContainTypes(Util.s_long(misctype));
		}
		if(building instanceof Drink)
		{
			((Drink)building).setLiquidHeld(capacity*50);
			((Drink)building).setThirstQuenched(250);
			if((capacity*50)<250)
				((Drink)building).setThirstQuenched(capacity*50);
			((Drink)building).setLiquidRemaining(0);
		}
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();


		messedUp=!profficiencyCheck(0,auto);
		if(completion<4) completion=4;
		
		if(misctype.equalsIgnoreCase("bundle"))
		{
			messedUp=false; 
			completion=1;
			verb="bundling "+EnvResource.RESOURCE_DESCS[building.material()&EnvResource.RESOURCE_MASK].toLowerCase();
			startStr="<S-NAME> start(s) "+verb+".";
			displayText="You are "+verb;
		}
		
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}