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
public class JewelMaking extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	public String ID() { return "JewelMaking"; }
	public String name(){ return "Jewel Making";}
	private static final String[] triggerStrings = {"JEWEL","JEWELMAKING"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "GLASS|PRECIOUS|SAND";}
    public String parametersFormat(){ return 
        "ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tAMOUNT_MATERIAL_REQUIRED\tITEM_BASE_VALUE\t"
        +"ITEM_CLASS_ID\tSTATUE||CODED_WEAR_LOCATION\tN_A\tBASE_ARMOR_AMOUNT\tOPTIONAL_RESOURCE_OR_MATERIAL\tCODED_SPELL_LIST";}

	protected static final int RCP_FINALNAME=0;
	protected static final int RCP_LEVEL=1;
	protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_MISCTYPE=6;
	//private static final int RCP_CAPACITY=7;
	protected static final int RCP_ARMORDMG=8;
	protected static final int RCP_EXTRAREQ=9;
	protected static final int RCP_SPELL=10;

	protected Vector beingDone=null;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			MOB mob=(MOB)affected;
			if(fireRequired)
			{
				if((building==null)
				||(getRequiredFire(mob,0)==null))
				{
					messedUp=true;
					unInvoke();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

    public String parametersFile(){ return "jewelmaking.txt";}
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
							messedUpCrafting(mob);
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
							dropAWinner(mob,building);
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
		if(((IE.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
		&&((IE.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GLASS))
		{
			if(!quiet)
				commonTell(mob,"You don't know how to "+what+" something made of "+RawMaterial.CODES.NAME(IE.material()).toLowerCase()+".");
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

	public boolean supportsMending(Environmental E){ return canMend(null,E,true);}
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
		DVector enhancedTypes=enhancedTypes(mob,commands);
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
		int duration=4;
		String misctype="";
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			int toggler=1;
			int toggleTop=2;
			StringBuffer buf=new StringBuffer("");
			for(int r=0;r<toggleTop;r++)
				buf.append(CMStrings.padRight("Item",27)+" Lvl "+CMStrings.padRight("Metal",5)+" ");
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
						buf.append(CMStrings.padRight(item,27)+" "+CMStrings.padRight(""+level,3)+" "+CMStrings.padRight(""+wood,5)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			commonTell(mob,buf.toString());
			enhanceList(mob);
			return true;
		}
		else
		if((str.equalsIgnoreCase("encrust"))||(str.equalsIgnoreCase("mount")))
		{
			String word=str.toLowerCase();
			if(commands.size()<3)
			{
				commonTell(mob,CMStrings.capitalizeAndLower(word)+" what jewel onto what item?");
				return false;
			}
			Item fire=getRequiredFire(mob,autoGenerate);
			building=null;
			mending=false;
			refitting=false;
            aborted=false;
			messedUp=false;
			if(fire==null) return false;
			String jewel=(String)commands.elementAt(1);
			String rest=CMParms.combine(commands,2);
			Environmental jewelE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,jewel,Wearable.FILTER_UNWORNONLY);
			Environmental thangE=mob.location().fetchFromMOBRoomFavorsItems(mob,null,rest,Wearable.FILTER_UNWORNONLY);
			if((jewelE==null)||(!CMLib.flags().canBeSeenBy(jewelE,mob)))
			{ commonTell(mob,"You don't see any '"+jewel+"' here."); return false;}
			if((thangE==null)||(!CMLib.flags().canBeSeenBy(thangE,mob)))
			{ commonTell(mob,"You don't see any '"+rest+"' here."); return false;}
			if((!(jewelE instanceof RawMaterial))||(!(jewelE instanceof Item))
			   ||(((((Item)jewelE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PRECIOUS)
				  &&((((Item)jewelE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_GLASS)))
			{ commonTell(mob,"A "+jewelE.name()+" is not suitable to "+word+" on anything."); return false;}
			Item jewelI=(Item)CMLib.materials().unbundle((Item)jewelE,1);
			if((!(thangE instanceof Item))
			   ||(!thangE.isGeneric())
			   ||(((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_MITHRIL)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PLASTIC)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_ROCK)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_WOODEN)
				  &&((((Item)thangE).material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_LEATHER)))
			{ commonTell(mob,"A "+thangE.name()+" is not suitable to be "+word+"ed on."); return false;}
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			building=(Item)thangE;
			beingDone=new Vector();
			String materialName=RawMaterial.CODES.NAME(jewelI.material()).toLowerCase();
			if(word.equals("encrust"))
			{
				beingDone.addElement(CMStrings.capitalizeAndLower(building.name())+" is encrusted with bits of "+materialName+".");
				startStr="<S-NAME> start(s) encrusting "+building.name()+" with "+materialName+".";
				displayText="You are encrusting "+building.name()+" with "+materialName;
				verb="encrusting "+building.name()+" with bits of "+materialName;
			}
			else
			{
				materialName=CMLib.english().startWithAorAn(materialName).toLowerCase();
				beingDone.addElement(CMStrings.capitalizeAndLower(building.name())+" has "+materialName+" mounted on it.");
				startStr="<S-NAME> start(s) mounting "+materialName+" onto "+building.name()+".";
				displayText="You are mounting "+materialName+" onto "+building.name();
				verb="mounting "+materialName+" onto "+building.name();
			}
			beingDone.addElement(jewelI);
			messedUp=!proficiencyCheck(mob,0,auto);
			duration=10;
			CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
			if(mob.location().okMessage(mob,msg))
			{
				jewelI.destroy();
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,duration);
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
			Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null) return false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
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
			Item fire=getRequiredFire(mob,autoGenerate);
			if(fire==null) return false;
			Vector newCommands=CMParms.parse(CMParms.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Wearable.FILTER_UNWORNONLY);
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
            aborted=false;
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
                    if((autoGenerate>0)||(level<=xlevel(mob)))
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
				Item fire=getRequiredFire(mob,autoGenerate);
				if(fire==null) return false;
			}
			else
				fireRequired=false;
			int woodRequired=CMath.s_int((String)foundRecipe.elementAt(RCP_WOOD));
            woodRequired=adjustWoodRequired(woodRequired,mob);
			if(amount>woodRequired) woodRequired=amount;
			String otherRequired=(String)foundRecipe.elementAt(RCP_EXTRAREQ);
			int[] pm={RawMaterial.MATERIAL_MITHRIL,RawMaterial.MATERIAL_METAL};
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"metal",pm,
												otherRequired.length()>0?1:0,otherRequired,null,
												false,
												autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
			if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
				return false;
			int lostValue=autoGenerate>0?0:
                CMLib.materials().destroyResources(mob.location(),woodRequired,data[0][FOUND_CODE],data[1][FOUND_CODE],null);
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			duration=getDuration(CMath.s_int((String)foundRecipe.elementAt(RCP_TICKS)),mob,CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL)),4);
			String itemName=null;
			if((otherRequired!=null)&&(otherRequired.length()>0)&&(otherRequired.equalsIgnoreCase("PRECIOUS")))
				itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),RawMaterial.CODES.NAME((data[1][FOUND_CODE]))).toLowerCase();
			else
				itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
			if(bundling)
				itemName="a "+woodRequired+"# "+itemName;
			else
				itemName=CMLib.english().startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) making "+building.name()+".";
			displayText="You are making "+building.name();
			verb="making "+building.name();
            playSound="tinktinktink.wav";
			building.setDisplayText(itemName+" lies here");
			if((data[1][FOUND_CODE]>0)
			&&(((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			   ||((data[0][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
			&&(((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)))
				building.setDescription(itemName+" made of "+RawMaterial.CODES.NAME(data[0][FOUND_CODE]).toLowerCase()+".");
			else
				building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(CMath.s_int((String)foundRecipe.elementAt(RCP_VALUE))+(woodRequired*(RawMaterial.CODES.VALUE(data[0][FOUND_CODE]))));
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			if(data[1][FOUND_CODE]==0)
				building.setMaterial(data[0][FOUND_CODE]);
			else
			{
				building.setMaterial(data[1][FOUND_CODE]);
				building.setBaseValue(building.baseGoldValue()+RawMaterial.CODES.VALUE(data[1][FOUND_CODE]));
			}
			building.baseEnvStats().setLevel(CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			//int capacity=CMath.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
			int armordmg=CMath.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
			String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
			addSpells(building,spell);
			if(building instanceof Armor)
			{
                ((Armor)building).baseEnvStats().setArmor(0);
                if(armordmg!=0)
                    ((Armor)building).baseEnvStats().setArmor(armordmg);
                setWearLocation(building,misctype,0);
			}
			if((misctype.equalsIgnoreCase("statue"))&&(!mob.isMonster()))
			{
				String of="";
				try
				{
					of=mob.session().prompt("What is this item a representation of?","");
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

		CMMsg msg=CMClass.getMsg(mob,building,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			building=(Item)msg.target();
			beneficialAffect(mob,mob,asLevel,duration);
			enhanceItem(mob,building,enhancedTypes);
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
