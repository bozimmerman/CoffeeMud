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

public class ClanCrafting extends CraftingSkill implements ItemCraftor
{
	public String ID() { return "ClanCrafting"; }
	public String name(){ return "Clan Crafting";}
	private static final String[] triggerStrings = {"CLANCRAFT"};
	public String[] triggerStrings(){return triggerStrings;}
    public String supportedResourceString(){return "WOODEN|METAL|MITHRIL";}

	protected static final int RCP_FINALNAME=0;
	protected static final int RCP_MATERIAL1=1;
	protected static final int RCP_MATERIAL2=2;
	protected static final int RCP_CITYPE=3;
	protected static final int RCP_LEVEL=4;
	protected static final int RCP_TICKS=5;
	protected static final int RCP_EXP=6;
	protected static final int RCP_VALUE=7;
	protected static final int RCP_CLASSTYPE=8;
	protected static final int RCP_MISCTYPE=9;
	protected static final int RCP_CAPACITY=10;
	protected static final int RCP_ARMORDMG=11;
	protected static final int RCP_CONTAINMASK=12;
	protected static final int RCP_SPELL=13;
	protected static final int RCP_REQUIREDSKILL=14;


	protected Item building=null;
	protected boolean messedUp=false;

    protected Vector loadRecipes(){return super.loadRecipes("clancraft.txt");}

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
						commonEmote(mob,"<S-NAME> mess(es) up crafting "+building.name()+".");
					else
					{
						mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
						CMLib.commands().postGet(mob,null,building,true);
					}
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if((student.getClanID()==null)||(student.getClanID().equalsIgnoreCase("")))
		{
			teacher.tell(student.name()+" is not a member of a clan.");
			student.tell("You need to belong to a clan before you can learn "+name()+".");
			return false;
		}
		Clan C=CMLib.clans().getClan(student.getClanID());
		if(C==null)
		{
			teacher.tell(student.name()+" is not a member of a clan.");
			student.tell("You need to belong to a clan before you can learn "+name()+".");
			return false;
		}
		if(C.allowedToDoThis(student,Clan.FUNC_CLANENCHANT)!=1)
		{
			teacher.tell(student.name()+" is not authorized to draw from the power of "+student.charStats().hisher()+" "+C.typeName()+".");
			student.tell("You must be authorized to draw from the power of your "+C.typeName()+" to learn this skill.");
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
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"clancraft list\" for a list.");
			return false;
		}
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You must be a member of a clan to use this skill.");
			return false;
		}
		Clan C=CMLib.clans().getClan(mob.getClanID());
		if(C==null)
		{
			mob.tell("You must be a member of a clan to use this skill.");
			return false;
		}
		if(C.allowedToDoThis(mob,Clan.FUNC_CLANENCHANT)!=1)
		{
			mob.tell("You are not authorized to draw from the power of your "+C.typeName()+".");
			return false;
		}
		Vector recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer("");
			buf.append(CMStrings.padRight("Item",24)+" "
					   +CMStrings.padRight("Exp",9)+" "
					   +CMStrings.padRight("Material#1",14)+" "
					   +CMStrings.padRight("Amt#1",4)+" "
					   +CMStrings.padRight("Material#2",14)+" "
					   +CMStrings.padRight("Amt#2",4)+"\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
					int exp=CMath.s_int((String)V.elementAt(RCP_EXP));
					if(level<=mob.envStats().level())
					{
						String mat1=(String)V.elementAt(RCP_MATERIAL1);
						String mat2=(String)V.elementAt(RCP_MATERIAL2);
						String amt1="";
						String amt2="";
						int m1=mat1.indexOf("/");
						if(m1>=0)
						{
							amt1=mat1.substring(m1+1);
							mat1=mat1.substring(0,m1).toLowerCase();
						}
						int m2=mat2.indexOf("/");
						if(m2>=0)
						{
							amt2=mat2.substring(m2+1);
							mat2=mat2.substring(0,m2).toLowerCase();
						}
						buf.append(CMStrings.padRight(item,24)+" "
								   +CMStrings.padRight(""+exp,9)+" "
								   +CMStrings.padRight(mat1,14)+" "
								   +CMStrings.padRight(amt1,4)+" "
								   +CMStrings.padRight(mat2,14)+" "
								   +CMStrings.padRight(amt2,4)+"\n\r");
					}
				}
			}
			buf.append("\n\r");
			commonTell(mob,buf.toString());
			return true;
		}
		building=null;
		messedUp=false;
		String recipeName=CMParms.combine(commands,0);
		Vector foundRecipe=null;
		Vector matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			Vector V=(Vector)matches.elementAt(r);
			if(V.size()>0)
			{
				int level=CMath.s_int((String)V.elementAt(RCP_LEVEL));
                if((autoGenerate>0)||(level<=mob.envStats().level()))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"clancraft list\" for a list.");
			return false;
		}

		int amt1=0;
		int amt2=0;
		String mat1=(String)foundRecipe.elementAt(RCP_MATERIAL1);
		String mat2=(String)foundRecipe.elementAt(RCP_MATERIAL2);
		int m1=mat1.indexOf("/");
		if(m1>=0)
		{
			amt1=CMath.s_int(mat1.substring(m1+1));
			mat1=mat1.substring(0,m1).toLowerCase();
		}
		int m2=mat2.indexOf("/");
		if(m2>=0)
		{
			amt2=CMath.s_int(mat2.substring(m2+1));
			mat2=mat2.substring(0,m2).toLowerCase();
		}

		int expRequired=CMath.s_int((String)foundRecipe.elementAt(RCP_EXP));
		if(C.getExp()<expRequired)
		{
			mob.tell("You need "+expRequired+" to do that, but your "+C.typeName()+" has only "+C.getExp()+" experience points.");
			return false;
		}
		int[][] data=fetchFoundResourceData(mob,amt1,mat1,null,amt2,mat2,null,false,autoGenerate);
		if(data==null) return false;
		amt1=data[0][FOUND_AMT];
		amt2=data[1][FOUND_AMT];
		String reqskill=(String)foundRecipe.elementAt(RCP_REQUIREDSKILL);
		if(reqskill.trim().length()>0)
		{
			Ability A=CMClass.findAbility(reqskill.trim());
			if((A!=null)&&(mob.fetchAbility(A.ID())==null))
			{
				commonTell(mob,"You need to know "+A.name()+" to craft this item.");
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if(amt1>0)
			destroyResources(mob.location(),amt1,data[0][FOUND_CODE],0,null,autoGenerate);
		if(amt2>0)
			destroyResources(mob.location(),amt2,data[1][FOUND_CODE],0,null,autoGenerate);
		C.setExp(C.getExp()-expRequired);
		C.update();

		building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
		if(building==null)
		{
			commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
			return false;
		}
		completion=CMath.s_int((String)foundRecipe.elementAt(RCP_TICKS))-((mob.envStats().level()-CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
		String misctype=(String)foundRecipe.elementAt(RCP_MISCTYPE);
		String itemName=null;
		if(!misctype.equalsIgnoreCase("area"))
		{
            if(((String)foundRecipe.elementAt(RCP_FINALNAME)).trim().startsWith("%"))
    			itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),C.typeName()+" "+C.name());
            else
                itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),"of "+C.typeName()+" "+C.name());
			if(misctype.length()>0)
				building.setReadableText(misctype);
		}
		else
		{
			itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),"of "+mob.location().getArea().name()).toLowerCase();
			building.setReadableText(mob.location().getArea().name());
		}
		itemName=CMStrings.startWithAorAn(itemName);
		building.setName(itemName);
		startStr="<S-NAME> start(s) crafting "+building.name()+".";
		displayText="You are crafting "+building.name();
        playSound="sanding.wav";
		verb="crafting "+building.name();
		building.setDisplayText(itemName+" is here");
		building.setDescription(itemName+". ");
		building.baseEnvStats().setWeight(amt1+amt2);
		building.setBaseValue(CMath.s_int((String)foundRecipe.elementAt(RCP_VALUE)));
		building.setMaterial(data[0][FOUND_CODE]);
		int hardness=RawMaterial.RESOURCE_DATA[data[0][FOUND_CODE]&RawMaterial.RESOURCE_MASK][3]-6;
		building.baseEnvStats().setLevel(CMath.s_int((String)foundRecipe.elementAt(RCP_LEVEL))+(hardness*3));
		if(building.baseEnvStats().level()<1) building.baseEnvStats().setLevel(1);
		int capacity=CMath.s_int((String)foundRecipe.elementAt(RCP_CAPACITY));
		int canContain=CMath.s_int((String)foundRecipe.elementAt(RCP_CONTAINMASK));
		int armordmg=CMath.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG));
		building.setSecretIdentity("This is the work of "+mob.Name()+".");
		String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.elementAt(RCP_SPELL)).trim():"";
		if(building instanceof ClanItem)
		{
			building.baseEnvStats().setSensesMask(EnvStats.SENSE_UNLOCATABLE);
			((ClanItem)building).setClanID(mob.getClanID());
			((ClanItem)building).setCIType(CMath.s_int((String)foundRecipe.elementAt(RCP_CITYPE)));
			if(((ClanItem)building).ciType()==ClanItem.CI_PROPAGANDA)
			{
				building.setMaterial(RawMaterial.RESOURCE_PAPER);
				CMLib.flags().setReadable(building,true);
				building.setReadableText("Read the glorious propaganda of "+C.typeName()+" "+C.name().toLowerCase()+"! Join and fight for us today!");
			}
		}

		if((CMLib.flags().isReadable(building))
		&&((building.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN))
			building.setMaterial(RawMaterial.RESOURCE_PAPER);

		addSpells(building,spell);
		if(building instanceof Armor)
		{
			((Armor)building).setRawProperLocationBitmap(0);
			double hardBonus=0.0;
			for(int wo=1;wo<Item.WORN_DESCS.length;wo++)
			{
				String WO=Item.WORN_DESCS[wo].toUpperCase();
				if(misctype.equalsIgnoreCase(WO))
				{
					hardBonus+=Item.WORN_WEIGHTS[wo];
					((Armor)building).setRawProperLocationBitmap(CMath.pow(2,wo-1));
					((Armor)building).setRawLogicalAnd(false);
				}
				else
				if((misctype.toUpperCase().indexOf(WO+"||")>=0)
				||(misctype.toUpperCase().endsWith("||"+WO)))
				{
					if(hardBonus==0.0)
						hardBonus+=Item.WORN_WEIGHTS[wo];
					((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|CMath.pow(2,wo-1));
					((Armor)building).setRawLogicalAnd(false);
				}
				else
				if((misctype.toUpperCase().indexOf(WO+"&&")>=0)
				||(misctype.toUpperCase().endsWith("&&"+WO)))
				{
					hardBonus+=Item.WORN_WEIGHTS[wo];
					((Armor)building).setRawProperLocationBitmap(building.rawProperLocationBitmap()|CMath.pow(2,wo-1));
					((Armor)building).setRawLogicalAnd(true);
				}
			}
			int hardPoints=(int)Math.round(CMath.mul(hardBonus,hardness));
			((Armor)building).baseEnvStats().setArmor(armordmg+hardPoints+(abilityCode()-1));
		}

		if(building instanceof Container)
			if(capacity>0)
			{
				((Container)building).setCapacity(capacity+amt1+amt2);
				((Container)building).setContainTypes(canContain);
			}
		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();


		messedUp=!profficiencyCheck(mob,0,auto);
		if(completion<6) completion=6;

		if(autoGenerate>0)
		{
			commands.addElement(building);
			return true;
		}

		CMMsg msg=CMClass.getMsg(mob,building,this,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,completion);
		}
		return true;
	}
}
