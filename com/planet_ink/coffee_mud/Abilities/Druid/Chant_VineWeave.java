package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.io.*;
import java.util.*;

public class Chant_VineWeave extends Chant
{
	public String ID() { return "Chant_VineWeave"; }
	public String name(){ return "Vine Weave";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 50;}
	public Environmental newInstance(){	return new Chant_VineWeave();}

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
		if(((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
		&&((mob.location().myResource()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION)
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_COTTON)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SILK)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_HEMP)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_VINE)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_WHEAT)))
		&&(!mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SEAWEED))))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int material=EnvResource.RESOURCE_VINE;
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_VINE)))
			material=EnvResource.RESOURCE_VINE;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SILK)))
			material=EnvResource.RESOURCE_SILK;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_HEMP)))
			material=EnvResource.RESOURCE_HEMP;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_WHEAT)))
			material=EnvResource.RESOURCE_WHEAT;
		else
		if(mob.location().resourceChoices().contains(new Integer(EnvResource.RESOURCE_SEAWEED)))
			material=EnvResource.RESOURCE_SEAWEED;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the plants.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				Vector V=(Vector)Resources.getResource("WEAVING RECIPES");
				if(V==null)
				{
					StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"weaving.txt");
					V=loadList(str);
					if(V.size()==0)
						Log.errOut("Chant_VineMass","Recipes not found!");
					Resources.submitResource("WEAVING RECIPES",V);
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
				building.setDescription(itemName+" looks like it was twisted from vines!");
				building.baseEnvStats().setWeight(woodRequired);
				building.setBaseValue(0);
				building.setMaterial(material);
				building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
				String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
				int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
				int canContain=Util.s_int((String)foundRecipe.elementAt(RCP_CONTAINMASK));
				int armordmg=Util.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
				if(building instanceof Weapon)
				{
					((Weapon)building).setWeaponClassification(Weapon.CLASS_FLAILED);
					for(int cl=0;cl<Weapon.classifictionDescription.length;cl++)
					{
						if(misctype.equalsIgnoreCase(Weapon.classifictionDescription[cl]))
							((Weapon)building).setWeaponClassification(cl);
					}
					building.baseEnvStats().setDamage(armordmg);
					((Weapon)building).setRawProperLocationBitmap(Item.WIELD|Item.HELD);
					((Weapon)building).setRawLogicalAnd((capacity>1));
				}
				if(building instanceof Armor)
				{

					if(capacity>0)
					{
						((Armor)building).setCapacity(capacity+woodRequired);
						((Armor)building).setContainTypes(canContain);
					}
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
					if(misctype.equalsIgnoreCase("ENTER"))
						((Rideable)building).setRideBasis(Rideable.RIDEABLE_ENTERIN);
					else
					if(misctype.equalsIgnoreCase("BED"))
						((Rideable)building).setRideBasis(Rideable.RIDEABLE_SLEEP);
				}
				//Behavior B=CMClass.getBehavior("Decay");
				//B.setParms("min=490 max=490 chance=100");
				//building.addBehavior(B);

				building.recoverEnvStats();
				building.text();
				building.recoverEnvStats();

				mob.location().addItemRefuse(building,Item.REFUSE_RESOURCE);
				mob.location().showHappens(Affect.MSG_OK_ACTION,building.name()+" twists out of some vines and grows still.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the plants, but nothing happens.");

		// return whether it worked
		return success;
	}
}