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

public class ScrimShaw extends CraftingSkill
{
	public String ID() { return "ScrimShaw"; }
	public String name(){ return "Scrimshaw";}
	private static final String[] triggerStrings = {"SCRIM","SCRIMSHAW"};
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
		Vector V=(Vector)Resources.getResource("SCRIMSHAW RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"scrimshaw.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("ScrimShaw","Recipes not found!");
			Resources.submitResource("SCRIMSHAW RECIPES",V);
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
							commonTell(mob,"<S-NAME> mess(es) up scrimshawing "+building.name()+".");
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
		if(IE.material()!=EnvResource.RESOURCE_BONE)
		{
			if(!quiet)
				commonTell(mob,"That's not made of bone.  That can't be mended.");
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
			commonTell(mob,"Scrim what? Enter \"scrim list\" for a list, \"scrim scan\", or \"scrim mend <item>\".");
			return false;
		}
		Vector recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
		String startStr=null;
        bundling=false;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer(Util.padRight("Item",16)+" Lvl Bone required\n\r");
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
			Vector foundRecipe=null;
			Vector matches=matchingRecipeNames(recipes,recipeName,true);
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
				commonTell(mob,"You don't know how to scrim a '"+recipeName+"'.  Try \"scrim list\" for a list.");
				return false;
			}
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			String misctype=(String)foundRecipe.elementAt(RCP_MISCTYPE);
			int[] pm={EnvResource.RESOURCE_BONE};
            bundling=misctype.equalsIgnoreCase("BUNDLE");
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"bone",pm,
												0,null,null,
                                                bundling,
												autoGenerate);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
			if(amount>woodRequired) woodRequired=amount;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int lostValue=destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null,autoGenerate);
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
			startStr="<S-NAME> start(s) scrimshawing "+building.name()+".";
			displayText="You are scrimshawing "+building.name();
			verb="scrimshawing "+building.name();
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))+(woodRequired*(EnvResource.RESOURCE_DATA[data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK][EnvResource.DATA_VALUE])));
			building.setMaterial(data[0][FOUND_CODE]);
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
			if(bundling) building.setBaseValue(lostValue);
			addSpells(building,spell);
			key=null;
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
				if(capacity>0)
					((Container)building).setCapacity(capacity+woodRequired);
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
			if(building instanceof Weapon)
			{
				((Weapon)building).baseEnvStats().setAttackAdjustment(abilityCode()-1);
				((Weapon)building).setWeaponType(Weapon.TYPE_BASHING);
				((Weapon)building).setWeaponClassification(Weapon.CLASS_BLUNT);
				for(int cl=0;cl<Weapon.classifictionDescription.length;cl++)
				{
					if(misctype.equalsIgnoreCase(Weapon.classifictionDescription[cl]))
						((Weapon)building).setWeaponClassification(cl);
				}
				switch(((Weapon)building).weaponClassification())
				{
				case Weapon.CLASS_AXE:
					((Weapon)building).setWeaponType(Weapon.TYPE_SLASHING);
					break;
				case Weapon.CLASS_SWORD:
				case Weapon.CLASS_DAGGER:
				case Weapon.CLASS_EDGED:
				case Weapon.CLASS_NATURAL:
				case Weapon.CLASS_POLEARM:
				case Weapon.CLASS_RANGED:
				case Weapon.CLASS_THROWN:
					((Weapon)building).setWeaponType(Weapon.TYPE_PIERCING);
					break;
				case Weapon.CLASS_BLUNT:
				case Weapon.CLASS_FLAILED:
				case Weapon.CLASS_HAMMER:
				case Weapon.CLASS_STAFF:
					((Weapon)building).setWeaponType(Weapon.TYPE_BASHING);
					break;
				}
				building.baseEnvStats().setDamage(capacity);
				((Weapon)building).setRawProperLocationBitmap(Item.WIELD|Item.HELD);
				((Weapon)building).setRawLogicalAnd(false);
			}
			if(building instanceof Rideable)
			{
				if(misctype.equalsIgnoreCase("CHAIR"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SIT);
				else
				if(misctype.equalsIgnoreCase("TABLE"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_TABLE);
				else
				if(misctype.equalsIgnoreCase("BED"))
					((Rideable)building).setRideBasis(Rideable.RIDEABLE_SLEEP);
			}
			if(building instanceof Light)
			{
				((Light)building).setDuration(capacity);
				if((building instanceof Container)&&(((Container)building).containTypes()!=Container.CONTAIN_SMOKEABLES))
					((Container)building).setCapacity(0);
			}
			building.recoverEnvStats();
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
