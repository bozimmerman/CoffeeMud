package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.io.*;
import java.util.*;

public class Chant_GrowItem extends Chant
{
	public String ID() { return "Chant_GrowItem"; }
	public String name(){ return "Grow Item";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 50;}
	public Environmental newInstance(){	return new Chant_GrowItem();}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	private static final int RCP_CAPACITY=7;
	private static final int RCP_ARMORDMG=8;
	private static final int RCP_CONTAINMASK=9;

	protected static Vector loadList(StringBuffer str)
	{
		Vector V=new Vector();
		if(str==null) return V;
		Vector V2=new Vector();
		boolean oneComma=false;
		int start=0;
		int longestList=0;
		for(int i=0;i<str.length();i++)
		{
			if(str.charAt(i)=='\t')
			{
				V2.addElement(str.substring(start,i));
				start=i+1;
				oneComma=true;
			}
			else
			if((str.charAt(i)=='\n')||(str.charAt(i)=='\r'))
			{
				if(oneComma)
				{
					V2.addElement(str.substring(start,i));
					if(V2.size()>longestList) longestList=V2.size();
					V.addElement(V2);
					V2=new Vector();
				}
				start=i+1;
				oneComma=false;
			}
		}
		if(V2.size()>1)
		{
			if(oneComma)
				V2.addElement(str.substring(start,str.length()));
			if(V2.size()>longestList) longestList=V2.size();
			V.addElement(V2);
		}
		for(int v=0;v<V.size();v++)
		{
			V2=(Vector)V.elementAt(v);
			while(V2.size()<longestList)
				V2.addElement("");
		}
		return V;
	}

	protected String replacePercent(String thisStr, String withThis)
	{
		if(withThis.length()==0)
		{
			int x=thisStr.indexOf("% ");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf(" %");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf("%");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		else
		{
			int x=thisStr.indexOf("%");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		return thisStr;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WOODS)
		&&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_JUNGLE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int material=EnvResource.RESOURCE_OAK;
		if((mob.location().myResource()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
			material=mob.location().myResource();
		else
		{
			Vector V=mob.location().resourceChoices();
			Vector V2=new Vector();
			for(int v=0;v<V.size();v++)
			{
				if((((Integer)V.elementAt(v)).intValue()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN)
					V2.addElement(V.elementAt(v));
			}
			material=((Integer)V2.elementAt(Dice.roll(1,V2.size(),-1))).intValue();
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the trees.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Vector V=(Vector)Resources.getResource("CARPENTRY RECIPES");
				if(V==null)
				{
					StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"carpentry.txt");
					V=loadList(str);
					if(V.size()==0)
						Log.errOut("Carpentry","Recipes not found!");
					Resources.submitResource("CARPENTRY RECIPES",V);
				}
				if(V.size()==0) return false;
				Vector foundRecipe=(Vector)V.elementAt(Dice.roll(1,V.size(),-1));
				int tries=0;
				while(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL))>(CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this)+1)&&(++tries<1000))
					foundRecipe=(Vector)V.elementAt(Dice.roll(1,V.size(),-1));
				if(tries>999)
				{
					mob.tell("For some reason, the chant failed...");
					return false;
				}
				
				Item building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
				if(building==null)
				{
					mob.tell("There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
					return false;
				}
				String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(material&EnvResource.RESOURCE_MASK)]).toLowerCase();
				int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
				itemName=Util.startWithAorAn(itemName);
				building.setName(itemName);
				building.setDisplayText(itemName+" is here");
				building.setDescription(itemName+" looks like a hunk of bark and branch!");
				building.baseEnvStats().setWeight(woodRequired);
				building.setBaseValue(0);
				building.setMaterial(material);
				building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
				String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
				int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
				int canContain=Util.s_int((String)foundRecipe.elementAt(RCP_CONTAINMASK));
				int armordmg=Util.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
				Item key=null;
				if((building instanceof Container)
				&&(!(building instanceof Armor)))
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
						((Container)building).setKeyName(new Double(Math.random()).toString());
						key=CMClass.getItem("GenKey");
						((Key)key).setKey(((Container)building).keyName());
						key.setName("a wooden key");
						key.setDisplayText("a small wooden key sits here");
						key.setDescription("looks like a key to "+building.name());
						key.recoverEnvStats();
						key.text();
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
				if(building instanceof Weapon)
				{
					((Weapon)building).setWeaponType(Weapon.TYPE_BASHING);
					((Weapon)building).setWeaponClassification(Weapon.CLASS_BLUNT);
					for(int cl=0;cl<Weapon.classifictionDescription.length;cl++)
					{
						if(misctype.equalsIgnoreCase(Weapon.classifictionDescription[cl]))
							((Weapon)building).setWeaponClassification(cl);
					}
					building.baseEnvStats().setAttackAdjustment((abilityCode()-1));
					building.baseEnvStats().setDamage(armordmg);
					((Weapon)building).setRawProperLocationBitmap(Item.WIELD|Item.HELD);
					((Weapon)building).setRawLogicalAnd((capacity>1));
				}
				if(building instanceof Armor)
				{
					((Armor)building).baseEnvStats().setArmor(armordmg+(abilityCode()-1));
					((Armor)building).setRawProperLocationBitmap(0);
					for(int wo=1;wo<Item.wornLocation.length;wo++)
					{
						String WO=Item.wornLocation[wo].toUpperCase();
						if(misctype.equalsIgnoreCase(WO))
						{
							((Armor)building).setRawProperLocationBitmap(Util.pow(2,wo-1));
							((Armor)building).setRawLogicalAnd(false);
						}
						else
						if((misctype.toUpperCase().indexOf(WO+"||")>=0)
						||(misctype.toUpperCase().endsWith("||"+WO)))
						{
							((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|Util.pow(2,wo-1));
							((Armor)building).setRawLogicalAnd(false);
						}
						else
						if((misctype.toUpperCase().indexOf(WO+"&&")>=0)
						||(misctype.toUpperCase().endsWith("&&"+WO)))
						{
							((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|Util.pow(2,wo-1));
							((Armor)building).setRawLogicalAnd(true);
						}
					}
				}
				if(building instanceof Light)
				{
					((Light)building).setDuration(capacity);
					if(building instanceof Container)
						((Container)building).setCapacity(0);
				}
				building.recoverEnvStats();
				building.text();
				building.recoverEnvStats();
				
				mob.location().addItemRefuse(building,Item.REFUSE_RESOURCE);
				if(key!=null)
					mob.location().addItemRefuse(key,Item.REFUSE_RESOURCE);
				mob.location().showHappens(Affect.MSG_OK_ACTION,building.name()+" grows out of a tree and drops.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the trees, but nothing happens.");

		// return whether it worked
		return success;
	}
}