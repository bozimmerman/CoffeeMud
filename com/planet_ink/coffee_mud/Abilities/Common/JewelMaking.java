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

public class JewelMaking extends CraftingSkill
{
	public String ID() { return "JewelMaking"; }
	public String name(){ return "Jewel Making";}
	private static final String[] triggerStrings = {"JEWEL","JEWELMAKING"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "GLASS|PRECIOUS|SAND";}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_MISCTYPE=6;
	//private static final int RCP_CAPACITY=7;
	private static final int RCP_ARMORDMG=8;
	private static final int RCP_EXTRAREQ=9;
	private static final int RCP_SPELL=10;

	private Item building=null;
	private Item fire=null;
	private boolean messedUp=false;
	private Vector beingDone=null;
	private boolean mending=false;
	private boolean refitting=false;
	private boolean fireRequired=true;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==MudHost.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if(fireRequired)
			{
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
		}
		return super.tick(ticking,tickID);
	}

	protected Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("JEWELMAKING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"jewelmaking.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Jewelmaking","Recipes not found!");
			Resources.submitResource("JEWELMAKING RECIPES",V);
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
					if((beingDone!=null)&&(beingDone.size()>=2))
					{
						if(messedUp)
							commonEmote(mob,"<S-NAME> mess(es) up "+verb+".");
						else
						{
							Item I=(Item)beingDone.elementAt(1);
							building.setBaseValue(building.baseGoldValue()+(I.baseGoldValue()*2));
							building.setDescription(building.description()+" "+(String)beingDone.elementAt(0));
						}
						beingDone=null;
					}
					else
					if(messedUp)
					{
						if(mending)
							commonEmote(mob,"<S-NAME> mess(es) up mending "+building.name()+".");
						else
						if(refitting)
							commonEmote(mob,"<S-NAME> mess(es) up refitting "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> mess(es) up "+verb+".");
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
				refitting=false;
			}
		}
		super.unInvoke();
	}

	protected boolean canWhat(MOB mob, Environmental E, String what, boolean quiet)
	{
		Item IE=(Item)E;
		if(((IE.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PRECIOUS)
		&&((IE.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_GLASS))
		{
			if(!quiet)
				commonTell(mob,"You don't know how to "+what+" something made of "+EnvResource.RESOURCE_DESCS[IE.material()&EnvResource.RESOURCE_MASK].toLowerCase()+".");
			return false;
		}
		if(!(IE instanceof Armor))
		{
			if(!quiet)
				commonTell(mob,"You don't know how to "+what+" that sort of thing.");
			return false;
		}
		return true;
	}

	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		if(!canWhat(mob,E,"mend",quiet)) return false;
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
			commonTell(mob,"Make what? Enter \"jewel list\" for a list.  You may also enter jewel encrust <gem name> <item name>, or jewel mount <gem name> <item name>, or jewel refit <item name>, or jewel scan, or jewel mend <item name>.");
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
		fireRequired=true;
        bundling=false;
		int completion=4;
		String misctype="";
		if(str.equalsIgnoreCase("list"))
		{
			int toggler=1;
			int toggleTop=2;
			StringBuffer buf=new StringBuffer("");
			for(int r=0;r<toggleTop;r++)
				buf.append(Util.padRight("Item",27)+" Lvl "+Util.padRight("Metal",5)+" ");
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
						buf.append(Util.padRight(item,27)+" "+Util.padRight(""+level,3)+" "+Util.padRight(""+wood,5)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if((str.equalsIgnoreCase("encrust"))||(str.equalsIgnoreCase("mount")))
		{
			String word=str.toLowerCase();
			if(commands.size()<3)
			{
				commonTell(mob,Util.capitalizeAndLower(word)+" what jewel onto what item?");
				return false;
			}
			fire=getRequiredFire(mob,autoGenerate);
			building=null;
			mending=false;
			refitting=false;
			messedUp=false;
			if(fire==null) return false;
			String jewel=(String)commands.elementAt(1);
			String rest=Util.combine(commands,2);
			Environmental jewelE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,jewel,Item.WORN_REQ_UNWORNONLY);
			Environmental thangE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,rest,Item.WORN_REQ_UNWORNONLY);
			if((jewelE==null)||(!Sense.canBeSeenBy(jewelE,mob)))
			{ commonTell(mob,"You don't see any '"+jewel+"' here."); return false;}
			if((thangE==null)||(!Sense.canBeSeenBy(thangE,mob)))
			{ commonTell(mob,"You don't see any '"+rest+"' here."); return false;}
			if((!(jewelE instanceof EnvResource))||(!(jewelE instanceof Item))
			   ||(((((Item)jewelE).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PRECIOUS)
				  &&((((Item)jewelE).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_GLASS)))
			{ commonTell(mob,"A "+jewelE.name()+" is not suitable to "+word+" on anything."); return false;}
			Item jewelI=(Item)jewelE;
			if((!(thangE instanceof Item))
			   ||(!thangE.isGeneric())
			   ||(((((Item)thangE).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_CLOTH)
				  &&((((Item)thangE).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_METAL)
				  &&((((Item)thangE).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_MITHRIL)
				  &&((((Item)thangE).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PLASTIC)
				  &&((((Item)thangE).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_ROCK)
				  &&((((Item)thangE).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
				  &&((((Item)thangE).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_LEATHER)))
			{ commonTell(mob,"A "+thangE.name()+" is not suitable to be "+word+"ed on."); return false;}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			building=(Item)thangE;
			beingDone=new Vector();
			String materialName=EnvResource.RESOURCE_DESCS[jewelI.material()&EnvResource.RESOURCE_MASK].toLowerCase();
			if(word.equals("encrust"))
			{
				beingDone.addElement(Util.capitalizeAndLower(building.name())+" is encrusted with bits of "+materialName+".");
				startStr="<S-NAME> start(s) encrusting "+building.name()+" with "+materialName+".";
				displayText="You are encrusting "+building.name()+" with "+materialName;
				verb="encrusting "+building.name()+" with bits of "+materialName;
			}
			else
			{
				materialName=Util.startWithAorAn(materialName).toLowerCase();
				beingDone.addElement(Util.capitalizeAndLower(building.name())+" has "+materialName+" mounted on it.");
				startStr="<S-NAME> start(s) mounting "+materialName+" onto "+building.name()+".";
				displayText="You are mounting "+materialName+" onto "+building.name();
				verb="mounting "+materialName+" onto "+building.name();
			}
			beingDone.addElement(jewelI);
			messedUp=!profficiencyCheck(mob,0,auto);
			completion=10;
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
			if(mob.location().okMessage(mob,msg))
			{
				jewelI.destroy();
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,completion);
				return true;
			}
			return false;
		}
		if(str.equalsIgnoreCase("scan"))
			return publicScan(mob,commands);
		else
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			mending=false;
			refitting=false;
			messedUp=false;
			fire=getRequiredFire(mob,autoGenerate);
			if(fire==null) return false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(!canMend(mob, building,false)) return false;
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
			fire=getRequiredFire(mob,autoGenerate);
			if(fire==null) return false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(building==null) return false;
			if(!canWhat(mob,building,"refit",false))
				return false;
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
			beingDone=null;
			building=null;
			mending=false;
			refitting=false;
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
                    if((autoGenerate>0)||(level<=mob.envStats().level()))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"jewel list\" for a list.");
				return false;
			}
			misctype=(String)foundRecipe.elementAt(RCP_MISCTYPE);
            bundling=misctype.equalsIgnoreCase("BUNDLE");
			if(!bundling)
			{
				fire=getRequiredFire(mob,autoGenerate);
				if(fire==null) return false;
			}
			else
				fireRequired=false;
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			if(amount>woodRequired) woodRequired=amount;
			String otherRequired=(String)foundRecipe.elementAt(RCP_EXTRAREQ);
			int[] pm={EnvResource.MATERIAL_MITHRIL,EnvResource.MATERIAL_METAL};
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"metal",pm,
												otherRequired.length()>0?1:0,otherRequired,null,
												false,
												autoGenerate);
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
			completion=Util.s_int((String)foundRecipe.elementAt(RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=null;
			if((otherRequired!=null)&&(otherRequired.length()>0)&&(otherRequired.equalsIgnoreCase("PRECIOUS")))
				itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(data[1][FOUND_CODE]&EnvResource.RESOURCE_MASK)]).toLowerCase();
			else
				itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK)]).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=Util.startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) making "+building.name()+".";
			displayText="You are making "+building.name();
			verb="making "+building.name();
            playSound="tinktinktink.wav";
			building.setDisplayText(itemName+" is here");
			if((data[1][FOUND_CODE]>0)
			&&(((data[0][FOUND_CODE]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
			   ||((data[0][FOUND_CODE]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL))
			&&(((data[1][FOUND_CODE]&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PRECIOUS)))
				building.setDescription(itemName+" made of "+EnvResource.RESOURCE_DESCS[data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK].toLowerCase()+".");
			else
				building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))+(woodRequired*(EnvResource.RESOURCE_DATA[data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK][EnvResource.DATA_VALUE])));
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			if(data[1][FOUND_CODE]==0)
				building.setMaterial(data[0][FOUND_CODE]);
			else
			{
				building.setMaterial(data[1][FOUND_CODE]);
				building.setBaseValue(building.baseGoldValue()+EnvResource.RESOURCE_DATA[data[1][FOUND_CODE]&EnvResource.RESOURCE_MASK][EnvResource.DATA_VALUE]);
			}
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			//int capacity=Util.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			int armordmg=Util.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
			addSpells(building,spell);
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
			if((misctype.equalsIgnoreCase("statue"))&&(!mob.isMonster()))
			{
				String of="";
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
				of=of.trim();
				if(of.startsWith("of "))
					of=of.substring(3).trim();
				building.setName(itemName+" of "+of);
				building.setDisplayText(itemName+" of "+of+" is here");
				building.setDescription(itemName+" of "+of+". ");
			}
			if(bundling) building.setBaseValue(lostValue);
			building.recoverEnvStats();
			building.text();
			building.recoverEnvStats();
		}


		messedUp=!profficiencyCheck(mob,0,auto);
		if(completion<8) completion=8;

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

		FullMsg msg=new FullMsg(mob,building,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,completion);
			return true;
		}
		else
		if(bundling)
		{
			messedUp=false;
			aborted=false;
			unInvoke();
		}
		return false;
	}
}
