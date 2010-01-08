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
public class Weaving extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	public String ID() { return "Weaving"; }
	public String name(){ return "Weaving";}
	private static final String[] triggerStrings = {"WEAVING","WEAVE"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "WHEAT|VINE|SEAWEED|HEMP|SILK|COTTON";}
    public String parametersFormat(){ return 
        "ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tAMOUNT_MATERIAL_REQUIRED\tITEM_BASE_VALUE\t"
        +"ITEM_CLASS_ID\tWEAPON_CLASS||CODED_WEAR_LOCATION||RIDE_BASIS||LID_LOCK\t"
        +"CONTAINER_CAPACITY||WEAPON_HANDS_REQUIRED\tBASE_ARMOR_AMOUNT||BASE_DAMAGE\t"
        +"CONTAINER_TYPE\tCODED_SPELL_LIST";}

	protected static final int RCP_FINALNAME=0;
	protected static final int RCP_LEVEL=1;
	protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	protected static final int RCP_CAPACITY=7;
	protected static final int RCP_ARMORDMG=8;
	protected static final int RCP_CONTAINMASK=9;
	protected static final int RCP_SPELL=10;

	protected Item key=null;

	public Weaving()
	{
	    super();

	    building = null;
	    mending = false;
	    refitting = false;
	    messedUp = false;
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			if(building==null)
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

    public String parametersFile(){ return "weaving.txt";}
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
						if(refitting)
							commonEmote(mob,"<S-NAME> mess(es) up refitting "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up weaving "+building.name()+".");
					}
					else
					{
						if(mending)
							building.setUsesRemaining(100);
						else
						if(refitting)
						{
							building.baseEnvStats().setHeight(0);
							building.recoverEnvStats();
						}
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
		if((IE.material()!=RawMaterial.RESOURCE_COTTON)
		&&(IE.material()!=RawMaterial.RESOURCE_SILK)
		&&(IE.material()!=RawMaterial.RESOURCE_HEMP)
		&&(IE.material()!=RawMaterial.RESOURCE_VINE)
		&&(IE.material()!=RawMaterial.RESOURCE_WHEAT)
		&&(IE.material()!=RawMaterial.RESOURCE_SEAWEED))
		{
			if(!quiet)
				commonTell(mob,"That's not made of any sort of weavable material.  It can't be mended.");
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
			commonTell(mob,"Weave what? Enter \"weave list\" for a list, \"weave refit <item>\" to resize, \"weave scan\", or \"weave mend <item>\".");
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
        bundling=false;
		String startStr=null;
		int duration=4;
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer("");
			int toggler=1;
			int toggleTop=3;
			for(int r=0;r<toggleTop;r++)
				buf.append(CMStrings.padRight("Item",10)+" "+CMStrings.padRight("Lvl",3)+" "+CMStrings.padRight("Material",10)+" ");
			buf.append("\n\r");
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
					{
						buf.append(CMStrings.padRight(item,10)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(""+wood,10)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			if(toggler!=1) buf.append("\n\r");
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
			messedUp=false;
			key=null;
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
		if(str.equalsIgnoreCase("refit"))
		{
			building=null;
			mending=false;
			key=null;
			refitting=false;
			messedUp=false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
			if(building==null) return false;
			if((building.material()!=RawMaterial.RESOURCE_COTTON)
			&&(building.material()!=RawMaterial.RESOURCE_SILK)
			&&(building.material()!=RawMaterial.RESOURCE_HEMP)
			&&(building.material()!=RawMaterial.RESOURCE_VINE)
			&&(building.material()!=RawMaterial.RESOURCE_WHEAT)
			&&(building.material()!=RawMaterial.RESOURCE_SEAWEED))
			{
				commonTell(mob,"That's not made of any sort of weavable material.  It can't be refitted.");
				return false;
			}
			if(!(building instanceof Armor))
		    {
				commonTell(mob,"You don't know how to refit that sort of thing.");
				return false;
			}
			if(building.envStats().height()==0)
			{
				commonTell(mob,building.name()+" is already the right size.");
				return false;
			}
			refitting=true;
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			startStr="<S-NAME> start(s) refitting "+building.name()+".";
			displayText="You are refitting "+building.name();
			verb="refitting "+building.name();
		}
		else
		{
			building=null;
			mending=false;
			messedUp=false;
            refitting=false;
            aborted=false;
			key=null;
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
					if(level<=xlevel(mob))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to weave a '"+recipeName+"'.  Try \"weave list\" for a list.");
				return false;
			}
			int woodRequired=CMath.s_int((String)foundRecipe.elementAt(RCP_WOOD));
            woodRequired=adjustWoodRequired(woodRequired,mob);
			if(amount>woodRequired) woodRequired=amount;
			int[] pm={RawMaterial.RESOURCE_COTTON,
					  RawMaterial.RESOURCE_SILK,
					  RawMaterial.RESOURCE_HEMP,
					  RawMaterial.RESOURCE_VINE,
					  RawMaterial.RESOURCE_WHEAT,
					  RawMaterial.RESOURCE_SEAWEED};
			String misctype=(String)foundRecipe.elementAt(RCP_MISCTYPE);
            String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
            bundling=spell.equalsIgnoreCase("BUNDLE")||misctype.equalsIgnoreCase("BUNDLE");
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"weavable material",pm,
												0,null,null,
												false,
												autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
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
			if(itemName.endsWith("s"))
				itemName="some "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) weaving "+building.name()+".";
			displayText="You are weaving "+building.name();
			verb="weaving "+building.name();
			building.setDisplayText(itemName+" lies here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired/2);
			building.setBaseValue(CMath.s_int((String)foundRecipe.elementAt(RCP_VALUE)));
			building.setMaterial(data[0][FOUND_CODE]);
			building.baseEnvStats().setLevel(CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			int capacity=CMath.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			int canContain=CMath.s_int((String)foundRecipe.elementAt(RCP_CONTAINMASK));
			int armordmg=CMath.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
			if(bundling)
			{
			    building.setBaseValue(lostValue);
				building.baseEnvStats().setWeight(woodRequired);
			}
			addSpells(building,spell);
			if(building instanceof Weapon)
			{
				((Weapon)building).setWeaponClassification(Weapon.CLASS_FLAILED);
				for(int cl=0;cl<Weapon.CLASS_DESCS.length;cl++)
				{
					if(misctype.equalsIgnoreCase(Weapon.CLASS_DESCS[cl]))
						((Weapon)building).setWeaponClassification(cl);
				}
				building.baseEnvStats().setDamage(armordmg);
				((Weapon)building).setRawProperLocationBitmap(Wearable.WORN_WIELD|Wearable.WORN_HELD);
				((Weapon)building).setRawLogicalAnd((capacity>1));
			}
			key=null;
			if(building instanceof Armor)
			{
				if(capacity>0)
				{
					((Armor)building).setCapacity(capacity+woodRequired);
					((Armor)building).setContainTypes(canContain);
				}
                ((Armor)building).baseEnvStats().setArmor(0);
                if(armordmg!=0)
                    ((Armor)building).baseEnvStats().setArmor(armordmg+(abilityCode()-1));
                setWearLocation(building,misctype,0);
			}
			else
			if(building instanceof Container)
			{
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
			if(building instanceof Rideable)
			{
                setRideBasis((Rideable)building,misctype);
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
			if(key!=null) commands.addElement(key);
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
