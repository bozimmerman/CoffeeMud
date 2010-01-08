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
public class Fletching extends EnhancedCraftingSkill implements ItemCraftor, MendingSkill
{
	public String ID() { return "Fletching"; }
	public String name(){ return "Fletching";}
	private static final String[] triggerStrings = {"FLETCH","FLETCHING"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "WOODEN";}
    public String parametersFormat(){ return 
        "ITEM_NAME\tITEM_LEVEL\tBUILD_TIME_TICKS\tAMOUNT_MATERIAL_REQUIRED\tITEM_BASE_VALUE\t"
        +"ITEM_CLASS_ID\tAMMO_TYPE\tAMMO_CAPACITY\tBASE_DAMAGE\tMAXIMUM_RANGE\t"
        +"OPTIONAL_RESOURCE_OR_MATERIAL\tCODED_SPELL_LIST";}

	protected static final int RCP_FINALNAME=0;
	protected static final int RCP_LEVEL=1;
	protected static final int RCP_TICKS=2;
	protected static final int RCP_WOOD=3;
	protected static final int RCP_VALUE=4;
	protected static final int RCP_CLASSTYPE=5;
	protected static final int RCP_AMMOTYPE=6;
	protected static final int RCP_AMOCAPACITY=7;
	protected static final int RCP_ARMORDMG=8;
	protected static final int RCP_MAXRANGE=9;
	protected static final int RCP_EXTRAREQ=10;
	protected static final int RCP_SPELL=11;

    public String parametersFile(){ return "fletching.txt";}
    protected Vector loadRecipes(){return super.loadRecipes(parametersFile());}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted)&&(!helping))
				{
					if(messedUp)
					{
						if(mending)
							messedUpCrafting(mob);
						else
							commonEmote(mob,"<S-NAME> mess(es) up making "+building.name()+".");
					}
					else
					{
						if(mending)
							building.setUsesRemaining(100);
						else
							dropAWinner(mob,building);
					}
				}
				building=null;
				mending=false;
			}
		}
		super.unInvoke();
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

	public boolean supportsMending(Environmental E){ return canMend(null,E,true);}
	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(!super.canMend(mob,E,quiet)) return false;
		Item IE=(Item)E;
		if((!(IE instanceof Weapon))
		||(((Weapon)IE).weaponClassification()!=Weapon.CLASS_RANGED)
		   &&(((Weapon)IE).weaponClassification()!=Weapon.CLASS_THROWN))
		{
			if(!quiet)
				commonTell(mob,"You don't know how to mend that sort of thing.");
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
			commonTell(mob,"Make what? Enter \"fletch list\" for a list, \"fletch scan\", or \"fletch mend <item>\".");
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
			int toggler=1;
			int toggleTop=2;
			StringBuffer buf=new StringBuffer("");
			for(int r=0;r<toggleTop;r++)
				buf.append(CMStrings.padRight("Item",27)+" Lvl "+CMStrings.padRight("Wood",5)+" ");
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
			buf.append("\n\rSome items may require additional material.");
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
				commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"fletch list\" for a list.");
				return false;
			}
			int woodRequired=CMath.s_int((String)foundRecipe.elementAt(RCP_WOOD));
            woodRequired=adjustWoodRequired(woodRequired,mob);
			if((amount>woodRequired)&&(woodRequired>0)) woodRequired=amount;
			String otherRequired=(String)foundRecipe.elementAt(RCP_EXTRAREQ);
			int[] pm={RawMaterial.MATERIAL_WOODEN};
			int[][] data=fetchFoundResourceData(mob,
												woodRequired,"wood",pm,
												(otherRequired.length()>0)?1:0,otherRequired,null,
												false,
												autoGenerate,
												enhancedTypes);
			if(data==null) return false;
			woodRequired=data[0][FOUND_AMT];
			if(((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
			||((data[1][FOUND_CODE]&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL))
			{
				Item fire=null;
				for(int i=0;i<mob.location().numItems();i++)
				{
					Item I2=mob.location().fetchItem(i);
					if((I2!=null)&&(I2.container()==null)&&(CMLib.flags().isOnFire(I2)))
					{
						fire=I2;
						break;
					}
				}
				if((fire==null)||(!mob.location().isContent(fire)))
				{
					commonTell(mob,"You'll need to build a fire first.");
					return false;
				}
			}
            String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
            bundling=spell.equalsIgnoreCase("BUNDLE");
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
			String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),RawMaterial.CODES.NAME(data[0][FOUND_CODE])).toLowerCase();
			itemName=CMLib.english().startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) making "+building.name()+".";
			displayText="You are making "+building.name();
			verb="making "+building.name();
            playSound="sanding.wav";
			building.setDisplayText(itemName+" lies here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue(CMath.s_int((String)foundRecipe.elementAt(RCP_VALUE)));
			building.setMaterial(data[0][FOUND_CODE]);
			building.baseEnvStats().setLevel(CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL)));
			building.setSecretIdentity("This is the work of "+mob.Name()+".");
			String ammotype=(String)foundRecipe.elementAt(RCP_AMMOTYPE);
			int capacity=CMath.s_int((String)foundRecipe.elementAt(RCP_AMOCAPACITY));
			int maxrange=CMath.s_int((String)foundRecipe.elementAt(RCP_MAXRANGE));
			int armordmg=CMath.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
			if(bundling) building.setBaseValue(lostValue);
			addSpells(building,spell);
			if(building instanceof Weapon)
			{
				if(ammotype.length()>0)
				{
					((Weapon)building).setAmmoCapacity(capacity);
					((Weapon)building).setAmmoRemaining(0);
					((Weapon)building).setAmmunitionType(ammotype);
				}
				building.baseEnvStats().setAttackAdjustment((abilityCode()-1));
				building.baseEnvStats().setDamage(armordmg);
				((Weapon)building).setRanges(((Weapon)building).minRange(),maxrange);
			}
			else
			if((ammotype.length()>0)&&(building instanceof Ammunition))
			{
				((Ammunition)building).setAmmunitionType(ammotype);
				building.setUsesRemaining(capacity);
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
