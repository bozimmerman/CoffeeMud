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
   Copyright 2000-2006 Bo Zimmerman

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

public class MasterLeatherWorking extends EnhancedCraftingSkill implements ItemCraftor
{
	public String ID() { return "MasterLeatherWorking"; }
	public String name(){ return "Master Leather Working";}
	private static final String[] triggerStrings = {"MASTERLEATHERWORKING","MLEATHERWORK","MLEATHERWORKING"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "LEATHER";}
	protected String[] supportedEnhancements(){ return new String[]{"LITECRAFTI","LITECRAFTII","LITECRAFTIII",
																	"DURACRAFTI","DURACRAFTII","DURACRAFTIII",
																	"QUALCRAFTI","QUALCRAFTII","QUALCRAFTIII"};}

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

    protected Vector loadRecipes(){return super.loadRecipes("masterleatherworking.txt");}

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
						if(refitting)
							commonEmote(mob,"<S-NAME> mess(es) up refitting "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up making "+building.name()+".");
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
							mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
					}
				}
				building=null;
				mending=false;
			}
		}
		super.unInvoke();
	}

	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		Item IE=(Item)E;
		if((IE.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)
		{
			if(!quiet)
				commonTell(mob,"That's not made of any sort of leather.  That can't be mended.");
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
			commonTell(mob,"Make what? Enter \"mleatherwork list\" for a list, \"mleatherwork refit <item>\" to resize, \"mleatherwork scan\", or \"mleatherwork mend <item>\".");
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
        playSound="scissor.wav";
		String startStr=null;
		String prefix="";
        bundling=false;
		int multiplier=4;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer("");
			int toggler=1;
			int toggleTop=2;
			for(int r=0;r<toggleTop;r++)
				buf.append(CMStrings.padRight("Item",30)+" "+CMStrings.padRight("Lvl",3)+" "+CMStrings.padRight("Amt",3)+" ");
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=CMath.s_int((String)V.elementAt(RCP_WOOD));
					if(level+20<=mob.envStats().level())
					{
						buf.append(CMStrings.padRight("Designer "+item,30)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=CMath.s_int((String)V.elementAt(RCP_WOOD));
					if((level+25)<=(mob.envStats().level()))
					{
						buf.append(CMStrings.padRight("Cuirbouli "+item,30)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=CMath.s_int((String)V.elementAt(RCP_WOOD));
					if((level+30)<=mob.envStats().level())
					{
						buf.append(CMStrings.padRight("Reinforced "+item,30)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=CMath.s_int((String)V.elementAt(RCP_WOOD));
					if((level+35)<=mob.envStats().level())
					{
						buf.append(CMStrings.padRight("Masterwork "+item,30)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}			
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=CMath.s_int((String)V.elementAt(RCP_WOOD));
					if((level+40)<=mob.envStats().level())
					{
						buf.append(CMStrings.padRight("Laminar "+item,30)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=CMath.s_int((String)V.elementAt(RCP_WOOD));
					if((level+45)<=mob.envStats().level())
					{
						buf.append(CMStrings.padRight("Battlemoulded "+item,30)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
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
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORNREQ_UNWORNONLY);
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
			refitting=false;
			messedUp=false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORNREQ_UNWORNONLY);
			if(building==null) return false;
			if((building.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)
			{
				commonTell(mob,"That's not made of leather.  That can't be refitted.");
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
					if(((level+45)<=(mob.envStats().level()))
					&&(recipeName.toUpperCase().indexOf("BATTLEMOULDED")>=0))
					{
						multiplier=9;
						prefix="Battlemoulded ";
						foundRecipe=V;
						break;
					}
					else
					if(((level+40)<=(mob.envStats().level()))
					&&(recipeName.toUpperCase().indexOf("LAMINAR")>=0))
					{
						multiplier=8;
						prefix="Laminar ";
						foundRecipe=V;
						break;
					}
					else
					if((level+35)<=(mob.envStats().level())
					&&(recipeName.toUpperCase().indexOf("MASTERWORK")>=0))
					{
						multiplier=7;
						prefix="Masterwork ";
						foundRecipe=V;
						break;
					}
					else
					if(((level+30)<=(mob.envStats().level()))
					&&(recipeName.toUpperCase().indexOf("REINFORCED")>=0))
					{
						multiplier=6;
						prefix="Reinforced ";
						foundRecipe=V;
						break;
					}
					else
					if(((level+25)<=(mob.envStats().level()))
					&&(recipeName.toUpperCase().indexOf("CUIRBOULI")>=0))
					{
						multiplier=5;
						prefix="Cuirbouli ";
						foundRecipe=V;
						break;
					}
					else
					if((level+20)<=(mob.envStats().level()))
					{
						multiplier=4;
						prefix="Designer ";
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"mleatherwork list\" for a list.");
				return false;
			}
			int woodRequired=CMath.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			if(amount>woodRequired) woodRequired=amount;
			int[] pm={RawMaterial.MATERIAL_LEATHER};
			int[] pm1={RawMaterial.MATERIAL_METAL,RawMaterial.MATERIAL_MITHRIL};
			String misctype=(String)foundRecipe.elementAt(RCP_MISCTYPE);
            bundling=misctype.equalsIgnoreCase("BUNDLE");
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"leather",pm,
												(multiplier==6)?1:0,
												(multiplier==6)?"metal":null,
												(multiplier==6)?pm1:null,
                                                bundling,
												autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int lostValue=destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],data[1][FOUND_CODE],null,autoGenerate);
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			completion=(multiplier*CMath.s_int((String)foundRecipe.elementAt(RCP_TICKS)))-((mob.envStats().level()-CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=(prefix+replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),RawMaterial.RESOURCE_DESCS[(data[0][FOUND_CODE]&RawMaterial.RESOURCE_MASK)])).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
			if(itemName.endsWith("s"))
				itemName="some "+itemName;
			else
				itemName=CMStrings.startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) making "+building.name()+".";
			displayText="You are making "+building.name();
			verb="making "+building.name();
			building.setDisplayText(itemName+" lies here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(CMath.s_int((String)foundRecipe.elementAt(RCP_VALUE))*multiplier);
			building.setMaterial(data[0][FOUND_CODE]);
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			int hardness=RawMaterial.RESOURCE_DATA[data[0][FOUND_CODE]&RawMaterial.RESOURCE_MASK][3]-2;
			building.baseEnvStats().setLevel(CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL))+6*hardness+((multiplier-1)*5));
			int capacity=CMath.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			int canContain=CMath.s_int((String)foundRecipe.elementAt(RCP_CONTAINMASK));
			int armordmg=CMath.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
			if(armordmg!=0)armordmg=armordmg+(multiplier-1);
			if(bundling) building.setBaseValue(lostValue);
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
			addSpells(building,spell);
			if(building instanceof Weapon)
			{
				((Weapon)building).baseEnvStats().setAttackAdjustment(abilityCode()+(hardness*5)+(abilityCode()-1)-1);
				((Weapon)building).setWeaponType(Weapon.TYPE_SLASHING);
				((Weapon)building).setWeaponClassification(Weapon.CLASS_FLAILED);
				for(int cl=0;cl<Weapon.classifictionDescription.length;cl++)
				{
					if(misctype.equalsIgnoreCase(Weapon.classifictionDescription[cl]))
						((Weapon)building).setWeaponClassification(cl);
				}
				switch(((Weapon)building).weaponClassification())
				{
				case Weapon.CLASS_AXE:
				case Weapon.CLASS_FLAILED:
					((Weapon)building).setWeaponType(Weapon.TYPE_SLASHING);
					break;
				case Weapon.CLASS_SWORD:
				case Weapon.CLASS_DAGGER:
				case Weapon.CLASS_EDGED:
				case Weapon.CLASS_POLEARM:
				case Weapon.CLASS_RANGED:
				case Weapon.CLASS_THROWN:
					((Weapon)building).setWeaponType(Weapon.TYPE_PIERCING);
					break;
				case Weapon.CLASS_NATURAL:
				case Weapon.CLASS_BLUNT:
				case Weapon.CLASS_HAMMER:
				case Weapon.CLASS_STAFF:
					((Weapon)building).setWeaponType(Weapon.TYPE_BASHING);
					break;
				}
				building.baseEnvStats().setDamage(armordmg+hardness);
				((Weapon)building).setRawProperLocationBitmap(Item.WORN_WIELD|Item.WORN_HELD);
				((Weapon)building).setRawLogicalAnd((capacity>1));
			}
			if(building instanceof Armor)
			{
				misctype=applyLayers((Armor)building,misctype);
				if(armordmg!=0)
					((Armor)building).baseEnvStats().setArmor(armordmg+hardness+(abilityCode()-1));
				((Armor)building).setRawProperLocationBitmap(0);
				if(capacity>0)
				{
					((Armor)building).setCapacity(capacity+woodRequired);
					((Armor)building).setContainTypes(canContain);
				}
				for(int wo=1;wo<Item.WORN_DESCS.length;wo++)
				{
					String WO=Item.WORN_DESCS[wo].toUpperCase();
					if(misctype.equalsIgnoreCase(WO))
					{
						((Armor)building).setRawProperLocationBitmap(CMath.pow(2,wo-1));
						((Armor)building).setRawLogicalAnd(false);
					}
					else
					if((misctype.toUpperCase().indexOf(WO+"||")>=0)
					||(misctype.toUpperCase().endsWith("||"+WO)))
					{
						((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|CMath.pow(2,wo-1));
						((Armor)building).setRawLogicalAnd(false);
					}
					else
					if((misctype.toUpperCase().indexOf(WO+"&&")>=0)
					||(misctype.toUpperCase().endsWith("&&"+WO)))
					{
						((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|CMath.pow(2,wo-1));
						((Armor)building).setRawLogicalAnd(true);
					}
				}
			}
			if(building instanceof Drink)
			{
				if(CMLib.flags().isGettable(building))
				{
					((Drink)building).setLiquidRemaining(0);
					((Drink)building).setLiquidHeld(capacity*50);
					((Drink)building).setThirstQuenched(250);
					if((capacity*50)<250)
						((Drink)building).setThirstQuenched(capacity*50);
				}
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
			verb="bundling "+RawMaterial.RESOURCE_DESCS[building.material()&RawMaterial.RESOURCE_MASK].toLowerCase();
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
			beneficialAffect(mob,mob,asLevel,completion);
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
