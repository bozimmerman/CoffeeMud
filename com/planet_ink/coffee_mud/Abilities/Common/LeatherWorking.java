package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class LeatherWorking extends CommonSkill
{
	public String ID() { return "LeatherWorking"; }
	public String name(){ return "Leather Working";}
	private static final String[] triggerStrings = {"LEATHERWORK","LEATHERWORKING"};
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


	private Item building=null;
	private boolean mending=false;
	private boolean refitting=false;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public LeatherWorking()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new LeatherWorking();}

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("LEATHERWORK RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"leatherworking.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("LeatherWorking","Recipes not found!");
			Resources.submitResource("LEATHERWORK RECIPES",V);
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
							commonEmote(mob,"<S-NAME> completely mess(es) up mending "+building.name()+".");
						else
						if(refitting)
							commonEmote(mob,"<S-NAME> completely mess(es) up refitting "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> completely mess(es) up making "+building.name()+".");
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"leatherwork list\" for a list, \"leatherwork refit <item>\" to resize, or \"leatherwork mend <item>\".");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		String prefix="";
		int multiplier=1;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer("");
			int toggler=1;
			int toggleTop=4;
			for(int r=0;r<toggleTop;r++)
				buf.append(Util.padRight("Item",14)+" "+Util.padRight("Amt",3)+" ");
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					if(level<=mob.envStats().level())
					{
						buf.append(Util.padRight(item,14)+" "+Util.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
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
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					if((level+5)<=(mob.envStats().level()))
					{
						buf.append(Util.padRight("Hard "+item,14)+" "+Util.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
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
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					if((level+11)<=mob.envStats().level())
					{
						buf.append(Util.padRight("Studded "+item,14)+" "+Util.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			if(toggler!=1) buf.append("\n\r");
			commonTell(mob,buf.toString());
			return true;
		}
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			mending=false;
			messedUp=false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(building==null) return false;
			if((building.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_LEATHER)
			{
				commonTell(mob,"That's not made of any sort of leather.  That can't be mended.");
				return false;
			}
			if(!building.subjectToWearAndTear())
			{
				commonTell(mob,"You can't mend "+building.name()+".");
				return false;
			}
			if(((Item)building).usesRemaining()>=100)
			{
				commonTell(mob,building.name()+" is in good condition already.");
				return false;
			}
			mending=true;
			if(!super.invoke(mob,commands,givenTarget,auto))
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
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(building==null) return false;
			if((building.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_LEATHER)
			{
				commonTell(mob,"That's not made of leather.  That can't be refitted.");
				return false;
			}
			if(!(building instanceof Armor))
		    {
				commonTell(mob,"You don't know how to refit that sort of thing.");
				return false;
			}
			if(((Item)building).envStats().height()==0)
			{
				commonTell(mob,building.name()+" is already the right size.");
				return false;
			}
			refitting=true;
			if(!super.invoke(mob,commands,givenTarget,auto))
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
						multiplier=1;
						foundRecipe=V;
						break;
					}
					else
					if(((level+5)<=(mob.envStats().level()))
					&&(("hard "+replacePercent(item,"")).equalsIgnoreCase(recipeName)))
					{
						multiplier=2;
						prefix="hard ";
						foundRecipe=V;
						break;
					}
					else
					if(((level+11)<=(mob.envStats().level()))
					&&(("studded "+replacePercent(item,"")).equalsIgnoreCase(recipeName)))
					{
						multiplier=3;
						prefix="studded ";
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"leatherwork list\" for a list.");
				return false;
			}
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			Item firstWood=null;
			Item firstMetal=null;
			int foundWood=0;
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().fetchItem(i);
				if((I instanceof EnvResource)
				&&((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LEATHER)
				&&(I.container()==null))
				{
					if(firstWood==null)firstWood=I;
					if(firstWood.material()==I.material())
						foundWood++;
				}
				if((I instanceof EnvResource)
				&&((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
				&&(I.container()==null)
				&&(multiplier==3))
					firstMetal=I;
			}
			if(foundWood==0)
			{
				commonTell(mob,"There is no leather here to make anything from!  It might need to put it down first.");
				return false;
			}
			if((multiplier==3)&&(firstMetal==null))
			{
				commonTell(mob,"You'll need a least a pound of metal on the ground to make studs.");
				return false;
			}
			if(foundWood<woodRequired)
			{
				commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			int woodDestroyed=woodRequired;
			for(int i=mob.location().numItems()-1;i>=0;i--)
			{
				Item I=mob.location().fetchItem(i);
				if((multiplier==3)&&(I==firstMetal))
					I.destroyThis();
				else
				if((I instanceof EnvResource)
				&&((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LEATHER)
				&&(I.container()==null)
				&&(I.material()==firstWood.material())
				&&((--woodDestroyed)>=0))
					I.destroyThis();
			}
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			completion=(multiplier*Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS)))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=(prefix+replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)])).toLowerCase();
			if(itemName.endsWith("s"))
				itemName="some "+itemName;
			else
				if(new String("aeiou").indexOf(Character.toLowerCase(itemName.charAt(0)))>=0)
					itemName="an "+itemName;
				else
					itemName="a "+itemName;
			building.setName(itemName);
			startStr="<S-NAME> start(s) making "+building.name()+".";
			displayText="You are making "+building.name();
			verb="making "+building.name();
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))*multiplier);
			building.setMaterial(firstWood.material());
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL))+((multiplier-1)*3));
			String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
			int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			int armordmg=Util.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG))+(multiplier-1);
			if(building instanceof Weapon)
			{
				((Weapon)building).setWeaponType(Weapon.TYPE_SLASHING);
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
				((Armor)building).baseEnvStats().setArmor(armordmg);
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
			if(building instanceof Drink)
			{
				((Drink)building).setLiquidRemaining(0);
				((Drink)building).setLiquidHeld(capacity*50);
				((Drink)building).setThirstQuenched(250);
				if((capacity*50)<250)
					((Drink)building).setThirstQuenched(capacity*50);
			}
			building.recoverEnvStats();
			building.text();
			building.recoverEnvStats();
		}

		messedUp=!profficiencyCheck(0,auto);
		if(completion<4) completion=4;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
