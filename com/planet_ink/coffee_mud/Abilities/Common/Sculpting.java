package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.File;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class Sculpting extends CraftingSkill
{
	public String ID() { return "Sculpting"; }
	public String name(){ return "Sculpting";}
	private static final String[] triggerStrings = {"SCULPT","SCULPTING"};
	public String[] triggerStrings(){return triggerStrings;}
    protected String supportedResourceString(){return "ROCK|STONE";}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_CONTAINMASK=8;
	private static final int RCP_SPELL=9;

	private Item building=null;
	private Item key=null;
	private boolean mending=false;
	private boolean messedUp=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("SCULPTING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"sculpting.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Sculpting","Recipes not found!");
			Resources.submitResource("SCULPTING RECIPES",V);
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
						if(mending)
							commonEmote(mob,"<S-NAME> mess(es) up mending "+building.name()+".");
						else
							commonTell(mob,"<S-NAME> mess(es) up sculpting "+building.name()+".");
					}
					else
					{
						if(mending)
							building.setUsesRemaining(100);
						else
						{
							mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
							if(key!=null)
							{
								mob.location().addItemRefuse(key,Item.REFUSE_PLAYER_DROP);
								key.setContainer(building);
							}
						}
					}
				}
				building=null;
				key=null;
				mending=false;
			}
		}
		super.unInvoke();
	}

	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		Item IE=(Item)E;
		if((IE.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_ROCK)
		{
			if(!quiet)
				commonTell(mob,"That's not made of stone.  That can't be mended.");
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		int autoGenerate=0;
		if((auto)&&(givenTarget==this)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
			givenTarget=null;
		}
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Sculpt what? Enter \"sculpt list\" for a list, \"sculpt scan\", or \"sculpt mend <item>\".");
			return false;
		}
        if((!auto)
        &&(commands.size()>0)
        &&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
        {
            bundling=true;
            if(super.invoke(mob,commands,givenTarget,auto,asLevel))
                return super.bundle(mob,commands);
            else
                return false;
        }
		Vector recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
        bundling=false;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",16)+" Lvl Stone required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					if(level<=mob.envStats().level())
						buf.append(Util.padRight(item,16)+" "+Util.padRight(""+level,3)+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			mending=false;
			key=null;
			messedUp=false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(!canMend(mob,building,false)) return false;
			mending=true;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) mending "+building.name()+".";
			displayText="You are mending "+building.name();
			verb="mending "+building.name();

			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
		}
		else
		{
			building=null;
			mending=false;
			key=null;
			messedUp=false;
			int amount=-1;
			if((commands.size()>1)&&(Util.isNumber((String)commands.lastElement())))
			{
				amount=Util.s_int((String)commands.lastElement());
				commands.removeElementAt(commands.size()-1);
			}
			String recipeName=Util.combine(commands,0);
			String rest="";
			Vector foundRecipe=null;
			Vector matches=matchingRecipeNames(recipes,recipeName,true);
			if(matches.size()==0)
			{
				matches=matchingRecipeNames(recipes,(String)commands.firstElement(),true);
				if(matches.size()>0)
				{
					recipeName=(String)commands.firstElement();
					rest=Util.combine(commands,1);
				}
			}
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
				commonTell(mob,"You don't know how to sculpt a '"+recipeName+"'.  Try \"sculpt list\" for a list.");
				return false;
			}
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			if(amount>woodRequired) woodRequired=amount;
			String misctype=(String)foundRecipe.elementAt(RCP_MISCTYPE);
            bundling=misctype.equalsIgnoreCase("BUNDLE");
			int[] pm={EnvResource.MATERIAL_ROCK};
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"stone",pm,
												0,null,null,
												bundling,
												autoGenerate);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			completion=Util.s_int((String)foundRecipe.elementAt(RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK)]).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=Util.startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) sculpting "+building.name()+".";
			displayText="You are sculpting "+building.name();
			verb="sculpting "+building.name();
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))+(woodRequired*(EnvResource.RESOURCE_DATA[data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK][EnvResource.DATA_VALUE])));
			building.setMaterial(data[0][FOUND_CODE]);
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
			addSpells(building,spell);
			int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			int canContain=Util.s_int((String)foundRecipe.elementAt(RCP_CONTAINMASK));
			key=null;
			if((misctype.equalsIgnoreCase("statue"))&&((!mob.isMonster())||(rest.length()>0)))
			{
				String of="";
				if(rest.length()>0)
					of=rest;
				else
				{
					try
					{
						of=mob.session().prompt("What is this a statue of?","");
						if(of.trim().length()==0)
							return false;
					}
					catch(java.io.IOException x)
					{
						return false;
					}
				}
				of=of.trim();
				if(of.startsWith("of "))
					of=of.substring(3).trim();
				building.setName(itemName+" of "+of);
				building.setDisplayText(itemName+" of "+of+" is here");
				building.setDescription(itemName+" of "+of+". ");
			}
			else
			if(building instanceof Container)
			{
				if(building instanceof Drink)
				{
					if(Sense.isGettable(building))
					{
						((Drink)building).setLiquidHeld(capacity*50);
						((Drink)building).setThirstQuenched(250);
						if((capacity*50)<250)
							((Drink)building).setThirstQuenched(capacity*50);
						((Drink)building).setLiquidRemaining(0);
					}
				}
				if(capacity>0)
				{
					((Container)building).setCapacity(capacity+woodRequired);
					((Container)building).setContainTypes(canContain);
				}
				if(misctype.equalsIgnoreCase("LID"))
					((Container)building).setLidsNLocks(true,false,false,false);
				else
				if(misctype.equalsIgnoreCase("LOCK"))
				{
					((Container)building).setLidsNLocks(true,false,true,false);
					((Container)building).setKeyName(new Double(Math.random()).toString());
					key=CMClass.getItem("GenKey");
					((Key)key).setKey(((Container)building).keyName());
					key.setName("a key");
					key.setDisplayText("a small key sits here");
					key.setDescription("looks like a key to "+building.name());
					key.recoverEnvStats();
					key.text();
				}
			}
			if(building instanceof Rideable)
			{
				if(misctype.equalsIgnoreCase("CHAIR"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SIT);
				else
				if(misctype.equalsIgnoreCase("TABLE"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_TABLE);
				else
				if(misctype.equalsIgnoreCase("LADDER"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_LADDER);
				else
				if(misctype.equalsIgnoreCase("BED"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SLEEP);
			}
			if(building instanceof Light)
			{
				((Light)building).setDuration(capacity);
				if(building instanceof Container)
					((Container)building).setCapacity(0);
			}
			building.recoverEnvStats();
			if((!Sense.isGettable(building))
			&&(!CoffeeUtensils.doesOwnThisProperty(mob,mob.location())))
			{
				commonTell(mob,"You are not allowed to build that here.");
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int lostValue=destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,building,autoGenerate);
			if(bundling) building.setBaseValue(lostValue);
			building.text();
			building.recoverEnvStats();

		}

		messedUp=!profficiencyCheck(mob,0,auto);
		if(completion<4) completion=4;

		if(bundling)
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
			beneficialAffect(mob,mob,asLevel,completion);
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
