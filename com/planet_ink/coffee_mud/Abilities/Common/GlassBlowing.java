package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class GlassBlowing extends CraftingSkill
{
	public String ID() { return "GlassBlowing"; }
	public String name(){ return "Glass Blowing";}
	private static final String[] triggerStrings = {"GLASSBLOW","GLASSBLOWING"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_SPELL=8;

	private Item building=null;
	private Item fire=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public GlassBlowing()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new GlassBlowing();}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
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

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("GLASS BLOWING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"glassblowing.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("GlassBlowing","Recipes not found!");
			Resources.submitResource("GLASS BLOWING RECIPES",V);
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
						commonTell(mob,Util.capitalize(building.name())+" explodes!");
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
		int autoGenerate=0;
		if((auto)&&(givenTarget==this)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{	
			autoGenerate=((Integer)commands.firstElement()).intValue(); 
			commands.removeElementAt(0);
			givenTarget=null;
		}
		randomRecipeFix(mob,loadRecipes(),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"glassblow list\" for a list.");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",20)+" Sand required\n\r");
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
		fire=getRequiredFire(mob,autoGenerate);
		if(fire==null) return false;
		building=null;
		messedUp=false;
		int amount=-1;
		if((commands.size()>1)&&(Util.isNumber((String)commands.lastElement())))
		{
			amount=Util.s_int((String)commands.lastElement());
			commands.removeElementAt(commands.size()-1);
		}
		String recipeName=Util.combine(commands,0);
		Vector foundRecipe=null;
		Vector matches=matchingRecipeNames(recipes,recipeName);
		for(int r=0;r<matches.size();r++)
		{
			Vector V=(Vector)matches.elementAt(r);
			if(V.size()>0)
			{
				int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
				if(level<=mob.envStats().level())
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"glassblow list\" for a list.");
			return false;
		}
		int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
		if(amount>woodRequired) woodRequired=amount;
		String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
		int[] pm={EnvResource.RESOURCE_SAND};
		int[][] data=fetchFoundResourceData(mob,
											woodRequired,"sand",pm,
											0,null,null,
											misctype.equalsIgnoreCase("BUNDLE"),
											autoGenerate);
		if(data==null) return false;
		woodRequired=data[0][FOUND_AMT];
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		int lostValue=destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null,autoGenerate);
		building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
		if(building==null)
		{
			commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
			return false;
		}
		completion=Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
		String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK)]).toLowerCase();
		if(misctype.equalsIgnoreCase("BUNDLE")) 
			itemName="a "+woodRequired+"# "+itemName;
		else
			itemName=Util.startWithAorAn(itemName);
		building.setName(itemName);
		startStr="<S-NAME> start(s) blowing "+building.name()+".";
		displayText="You are blowing "+building.name();
		verb="blowing "+building.name();
		building.setDisplayText(itemName+" is here");
		building.setDescription(itemName+". ");
		building.baseEnvStats().setWeight(woodRequired);
		building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE)));
		building.setMaterial(EnvResource.RESOURCE_GLASS);
		building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
		building.setSecretIdentity("This is the work of "+mob.Name()+".");
		int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
		String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
		if(spell.length()>0)
		{
			Ability A=CMClass.getAbility(spell);
			if(A!=null)	building.addNonUninvokableEffect(A);
		}
		if(building instanceof Container)
		{
			if(capacity>0)
				((Container)building).setCapacity(capacity+woodRequired);
			if(misctype.equalsIgnoreCase("LID"))
				((Container)building).setLidsNLocks(true,false,false,false);
			else
			if(misctype.equalsIgnoreCase("LOCK"))
			{
				((Container)building).setLidsNLocks(true,false,true,false);
				((Container)building).setKeyName(new Double(Math.random()).toString());
			}
		}
		if(building instanceof Drink)
		{
			((Drink)building).setLiquidRemaining(0);
			((Drink)building).setLiquidHeld(capacity*50);
			((Drink)building).setThirstQuenched(250);
			if((capacity*50)<250)
				((Drink)building).setThirstQuenched(capacity*50);
		}
		if(misctype.equalsIgnoreCase("bundle")) building.setBaseValue(lostValue);
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();


		messedUp=!profficiencyCheck(mob,0,auto);
		if(completion<4) completion=4;

		if(misctype.equalsIgnoreCase("bundle"))
		{
			messedUp=false;
			completion=1;
			verb="bundling "+EnvResource.RESOURCE_DESCS[building.material()&EnvResource.RESOURCE_MASK].toLowerCase();
			startStr="<S-NAME> start(s) "+verb+".";
			displayText="You are "+verb;
		}

		if(autoGenerate>0)
		{
			commands.addElement(building);
			return true;
		}

		FullMsg msg=new FullMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
