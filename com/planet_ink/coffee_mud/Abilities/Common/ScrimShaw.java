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
public class ScrimShaw extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	public String ID() { return "ScrimShaw"; }
	public String name(){ return "Scrimshawing";}
	private static final String[] triggerStrings = {"SCRIM","SCRIMSHAWING"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "BONE";}
    public String parametersFormat(){ return 
        "ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tAMOUNT_MATERIAL_REQUIRED\tITEM_BASE_VALUE\t"
        +"ITEM_CLASS_ID\tSTATUE||LID_LOCK||WEAPON_CLASS||RIDE_BASIS\t"
        +"CONTAINER_CAPACITY||BASE_DAMAGE||LIGHT_DURATION";}

	protected static final int RCP_FINALNAME=0;
	protected static final int RCP_LEVEL=1;
	protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_SPELL=8;

	protected Item key=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

    public String parametersFile(){ return "scrimshaw.txt";}
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
						if(mending)
							messedUpCrafting(mob);
						else
							commonTell(mob,"<S-NAME> mess(es) up scrimshawing "+building.name()+".");
					}
					else
					{
						if(mending)
							building.setUsesRemaining(100);
						else
						{
							dropAWinner(mob,building);
							if(key!=null)
							{
								dropAWinner(mob,key);
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

	public boolean supportsMending(Environmental E){ return canMend(null,E,true);}
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		Item IE=(Item)E;
		if(IE.material()!=RawMaterial.RESOURCE_BONE)
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
		DVector enhancedTypes=enhancedTypes(mob,commands);
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Scrim what? Enter \"scrim list\" for a list, \"scrim scan\", or \"scrim mend <item>\".");
			return false;
		}
        if((!auto)
        &&(commands.size()>0)
        &&(((String)commands.firstElement()).equalsIgnoreCase("bundle")))
        {
            bundling=true;
            if(super.invoke(mob,commands,givenTarget,auto,asLevel))
                return super.bundle(mob,commands);
            return false;
        }
		Vector recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
		String startStr=null;
        bundling=false;
		int duration=4;
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer(CMStrings.padRight("Item",16)+" Lvl Bone required\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=CMath.s_int((String)V.elementAt(RCP_WOOD));
                    wood=adjustWoodRequired(wood,mob);
					if((level<=xlevel(mob))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
						buf.append(CMStrings.padRight(item,16)+" "+CMStrings.padRight(""+level,3)+" "+wood+"\n\r");
				}
			}
			commonTell(mob,buf.toString());
			enhanceList(mob);
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
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
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
            refitting=false;
            aborted=false;
			int amount=-1;
			if((commands.size()>1)&&(CMath.isNumber((String)commands.lastElement())))
			{
				amount=CMath.s_int((String)commands.lastElement());
				commands.removeElementAt(commands.size()-1);
			}
			String recipeName=CMParms.combine(commands,0);
			Vector foundRecipe=null;
			Vector matches=matchingRecipeNames(recipes,recipeName,true);
			for(int r=0;r<matches.size();r++)
			{
				Vector V=(Vector)matches.elementAt(r);
				if(V.size()>0)
				{
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
                    if((autoGenerate>0)||(level<=xlevel(mob)))
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
			int woodRequired=CMath.s_int((String)foundRecipe.elementAt(RCP_WOOD));
            woodRequired=adjustWoodRequired(woodRequired,mob);
			String misctype=(String)foundRecipe.elementAt(RCP_MISCTYPE);
			int[] pm={RawMaterial.RESOURCE_BONE};
            bundling=misctype.equalsIgnoreCase("BUNDLE");
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"bone",pm,
												0,null,null,
                                                bundling,
												autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
			if(amount>woodRequired) woodRequired=amount;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int lostValue=autoGenerate>0?0:
                CMLib.materials().destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],0,null);
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			duration=getDuration(CMath.s_int((String)foundRecipe.elementAt(RCP_TICKS)),mob,CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL)),4);
			String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) scrimshawing "+building.name()+".";
			displayText="You are scrimshawing "+building.name();
			verb="scrimshawing "+building.name();
			building.setDisplayText(itemName+" lies here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(CMath.s_int((String)foundRecipe.elementAt(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
			building.setMaterial(data[0][FOUND_CODE]);
			building.baseEnvStats().setLevel(CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			int capacity=CMath.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
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
					((Container)building).setKeyName(Double.toString(Math.random()));
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
                ((Weapon)building).setWeaponClassification(Weapon.CLASS_FLAILED);
                setWeaponTypeClass((Weapon)building,misctype,Weapon.TYPE_BASHING,Weapon.TYPE_PIERCING);
				building.baseEnvStats().setDamage(capacity);
				((Weapon)building).setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
				((Weapon)building).setRawLogicalAnd(false);
			}
			if(building instanceof Rideable)
			{
                setRideBasis((Rideable)building,misctype);
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


		messedUp=!proficiencyCheck(mob,0,auto);

		if(bundling)
		{
			messedUp=false;
			duration=1;
			verb="bundling "+RawMaterial.CODES.NAME(building.material()).toLowerCase();
			startStr="<S-NAME> start(s) "+verb+".";
			displayText="You are "+verb;
		}

		if(autoGenerate>0)
		{
			commands.addElement(building);
			return true;
		}

		CMMsg msg=CMClass.getMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,building,enhancedTypes);
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
