package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class LeatherWorking extends CraftingSkill
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
	private static final int RCP_CONTAINMASK=9;
	private static final int RCP_SPELL=10;


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

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("LEATHERWORK RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"leatherworking.txt");
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
		if((IE.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_LEATHER)
		{
			if(!quiet)
				commonTell(mob,"That's not made of any sort of leather.  That can't be mended.");
			return false;
		}
		return true;
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
			commonTell(mob,"Make what? Enter \"leatherwork list\" for a list, \"leatherwork refit <item>\" to resize, \"leatherwork scan\", or \"leatherwork mend <item>\".");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		String prefix="";
		boolean bundle=false;
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
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			mending=false;
			messedUp=false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(!canMend(mob,building,false)) return false;
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
					if(((level+11)<=(mob.envStats().level()))
					&&(recipeName.toUpperCase().indexOf("STUDDED ")>=0))
					{
						multiplier=3;
						prefix="studded ";
						foundRecipe=V;
						break;
					}
					else
					if(((level+5)<=(mob.envStats().level()))
					&&(recipeName.toUpperCase().indexOf("HARD")>=0))
					{
						multiplier=2;
						prefix="hard ";
						foundRecipe=V;
						break;
					}
					else
					if(level<=(mob.envStats().level()))
					{
						multiplier=1;
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
			if(amount>woodRequired) woodRequired=amount;
			int[] pm={EnvResource.MATERIAL_LEATHER};
			int[] pm1={EnvResource.MATERIAL_METAL,EnvResource.MATERIAL_MITHRIL};
			String misctype=(String)foundRecipe.elementAt(this.RCP_MISCTYPE);
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"leather",pm,
												(multiplier==3)?1:0,
												(multiplier==3)?"metal":null,
												(multiplier==3)?pm1:null,
												misctype.equalsIgnoreCase("BUNDLE"),
												autoGenerate);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			int lostValue=destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],data[1][FOUND_CODE],null,autoGenerate);
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			completion=(multiplier*Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS)))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=(prefix+replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK)])).toLowerCase();
			if(misctype.equalsIgnoreCase("BUNDLE")) 
				itemName="a "+woodRequired+"# "+itemName;
			else
			if(itemName.endsWith("s"))
				itemName="some "+itemName;
			else
				itemName=Util.startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) making "+building.name()+".";
			displayText="You are making "+building.name();
			verb="making "+building.name();
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))*multiplier);
			building.setMaterial(data[0][FOUND_CODE]);
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			int hardness=EnvResource.RESOURCE_DATA[data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK][3]-2;
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL))+((multiplier-1)*3));
			int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			int canContain=Util.s_int((String)foundRecipe.elementAt(RCP_CONTAINMASK));
			int armordmg=Util.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG))+(multiplier-1);
			bundle=misctype.equalsIgnoreCase("BUNDLE");
			if(misctype.equalsIgnoreCase("bundle")) building.setBaseValue(lostValue);
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
				if(A!=null)	building.addNonUninvokableEffect(A);
			}
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
				((Weapon)building).setRawProperLocationBitmap(Item.WIELD|Item.HELD);
				((Weapon)building).setRawLogicalAnd((capacity>1));
			}
			if(building instanceof Armor)
			{
				((Armor)building).baseEnvStats().setArmor(armordmg+hardness+(abilityCode()-1));
				((Armor)building).setRawProperLocationBitmap(0);
				if(capacity>0)
				{
					((Armor)building).setCapacity(capacity+woodRequired);
					((Armor)building).setContainTypes(canContain);
				}
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

		messedUp=!profficiencyCheck(mob,0,auto);
		if(completion<4) completion=4;

		if(bundle)
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
		else
		if(bundle)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return true;
	}
}
