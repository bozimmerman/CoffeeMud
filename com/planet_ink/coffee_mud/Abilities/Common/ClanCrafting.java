package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2012 Bo Zimmerman

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

@SuppressWarnings({"unchecked","rawtypes"})
public class ClanCrafting extends CraftingSkill implements ItemCraftor
{
	public String ID() { return "ClanCrafting"; }
	public String name(){ return "Clan Crafting";}
	private static final String[] triggerStrings = {"CLANCRAFT"};
	public String[] triggerStrings(){return triggerStrings;}
	public String supportedResourceString(){return "WOODEN|METAL|MITHRIL";}
	protected int expRequired = 0;
	protected Clan myClan=null;
	public String parametersFormat(){ return 
		"ITEM_NAME\tRESOURCE_NAME_AMOUNT_MATERIAL_REQUIRED\tRESOURCE_NAME_AMOUNT_MATERIAL_REQUIRED\t"
		+"CLAN_ITEM_CODENUMBER\tITEM_LEVEL\tBUILD_TIME_TICKS\tCLAN_EXPERIENCE_COST_AMOUNT\t"
		+"ITEM_BASE_VALUE\tITEM_CLASS_ID\tCLAN_AREA_FLAG||CODED_WEAR_LOCATION||READABLE_TEXT\t"
		+"CONTAINER_CAPACITY\tBASE_ARMOR_AMOUNT\tCONTAINER_TYPE\tCODED_SPELL_LIST\tREQUIRED_COMMON_SKILL_ID";}

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

	public Hashtable parametersFields(){ return new Hashtable();}
	public String parametersFile(){ return "clancraft.txt";}
	protected List<List<String>> loadRecipes(){return super.loadRecipes(parametersFile());}

	public boolean supportsDeconstruction() { return false; }

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
						commonEmote(mob,"<S-NAME> mess(es) up crafting "+building.name()+".");
						if(myClan!=null)
						{
							myClan.setExp(myClan.getExp()+expRequired);
							myClan.update();
						}
					}
					else
					{
						dropAWinner(mob,building);
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
		if(!student.clans().iterator().hasNext())
		{
			teacher.tell(student.name()+" is not a member of a clan.");
			student.tell("You need to belong to a clan before you can learn "+name()+".");
			return false;
		}
		Pair<Clan,Integer> p=CMLib.clans().findPrivilegedClan(student, Clan.Function.ENCHANT);
		if(p==null)
		{
			teacher.tell(student.name()+" is not authorized to draw from the power of "+student.charStats().hisher()+" clan.");
			student.tell("You must be authorized to draw from the power of your clan to learn this skill.");
			return false;
		}
		return true;
	}

	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return "Not implemented";
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		int autoGenerate=0;
		if((auto)&&(commands.size()>0)&&(commands.firstElement() instanceof Integer))
		{
			autoGenerate=((Integer)commands.firstElement()).intValue();
			commands.removeElementAt(0);
			givenTarget=null;
		}
		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"clancraft list\" for a list, or \"clancraft stop\" to cancel.");
			return false;
		}
		String clanTypeName="Clan";
		String clanName="None";
		Clan clanC=null;
		if(autoGenerate<=0)
		{
			if(!mob.clans().iterator().hasNext())
			{
				mob.tell("You must be a member of a clan to use this skill.");
				return false;
			}
			Pair<Clan,Integer> p=CMLib.clans().findPrivilegedClan(mob, Clan.Function.ENCHANT);
			if((p==null)
			&&(!CMSecurity.isASysOp(mob)))
			{
				mob.tell("You are not authorized to draw from the power of your clan.");
				return false;
			}
			if(p!=null)
			{
				clanName=p.first.getName();
				clanTypeName=p.first.getGovernmentName();
				clanC=p.first;
			}
		}
		List<List<String>> recipes=addRecipes(mob,loadRecipes());
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int duration=4;
		int[] cols={
				ListingLibrary.ColFixer.fixColWidth(24,mob.session()),
				ListingLibrary.ColFixer.fixColWidth(9,mob.session()),
				ListingLibrary.ColFixer.fixColWidth(14,mob.session()),
				ListingLibrary.ColFixer.fixColWidth(4,mob.session()),
				ListingLibrary.ColFixer.fixColWidth(14,mob.session()),
				ListingLibrary.ColFixer.fixColWidth(4,mob.session())
			};
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			StringBuffer buf=new StringBuffer("");
			buf.append(CMStrings.padRight("Item",cols[0])+" "
					   +CMStrings.padRight("Exp",cols[1])+" "
					   +CMStrings.padRight("Material#1",cols[2])+" "
					   +CMStrings.padRight("Amt#1",cols[3])+" "
					   +CMStrings.padRight("Material#2",cols[4])+" "
					   +CMStrings.padRight("Amt#2",cols[5])+"\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.get(RCP_FINALNAME),"");
					int level=CMath.s_int((String)V.get(RCP_LEVEL));
					int exp=CMath.s_int((String)V.get(RCP_EXP));
					if((level<=xlevel(mob))
					&&((mask==null)||(mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						String mat1=(String)V.get(RCP_MATERIAL1);
						String mat2=(String)V.get(RCP_MATERIAL2);
						String amt1="";
						String amt2="";
						int m1=mat1.indexOf('/');
						if(m1>=0)
						{
							amt1=mat1.substring(m1+1);
							mat1=mat1.substring(0,m1).toLowerCase();
							//amt1=""+adjustWoodRequired(CMath.s_int(amt1),mob);
						}
						int m2=mat2.indexOf('/');
						if(m2>=0)
						{
							amt2=mat2.substring(m2+1);
							mat2=mat2.substring(0,m2).toLowerCase();
							//amt2=""+adjustWoodRequired(CMath.s_int(amt2),mob);
						}
						buf.append(CMStrings.padRight(item,cols[0])+" "
								   +CMStrings.padRight(""+exp,cols[1])+" "
								   +CMStrings.padRight(mat1,cols[2])+" "
								   +CMStrings.padRight(amt1,cols[3])+" "
								   +CMStrings.padRight(mat2,cols[4])+" "
								   +CMStrings.padRight(amt2,cols[5])+"\n\r");
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
		List<String> foundRecipe=null;
		List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			List<String> V=matches.get(r);
			if(V.size()>0)
			{
				int level=CMath.s_int((String)V.get(RCP_LEVEL));
				if((autoGenerate>0)||(level<=xlevel(mob)))
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
		String mat1=(String)foundRecipe.get(RCP_MATERIAL1);
		String mat2=(String)foundRecipe.get(RCP_MATERIAL2);
		int m1=mat1.indexOf('/');
		if(m1>=0)
		{
			amt1=CMath.s_int(mat1.substring(m1+1));
			mat1=mat1.substring(0,m1).toLowerCase();
			//amt1=adjustWoodRequired(amt1, mob);
		}
		int m2=mat2.indexOf('/');
		if(m2>=0)
		{
			amt2=CMath.s_int(mat2.substring(m2+1));
			mat2=mat2.substring(0,m2).toLowerCase();
			//amt2=adjustWoodRequired(amt2, mob);
		}

		expRequired=CMath.s_int((String)foundRecipe.get(RCP_EXP));
		expRequired=getXPCOSTAdjustment(mob,expRequired);
		if((clanC!=null)&&(clanC.getExp()<expRequired))
		{
			mob.tell("You need "+expRequired+" to do that, but your "+clanTypeName+" has only "+clanC.getExp()+" experience points.");
			return false;
		}
		int[][] data=fetchFoundResourceData(mob,amt1,mat1,null,amt2,mat2,null,false,autoGenerate,null);
		if(data==null) return false;
		amt1=data[0][FOUND_AMT];
		amt2=data[1][FOUND_AMT];
		String reqskill=(String)foundRecipe.get(RCP_REQUIREDSKILL);
		if((autoGenerate<=0)&&(reqskill.trim().length()>0))
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
		if((amt1>0)&&(autoGenerate<=0))
			CMLib.materials().destroyResources(mob.location(),amt1,data[0][FOUND_CODE],0,null);
		if((amt2>0)&&(autoGenerate<=0))
			CMLib.materials().destroyResources(mob.location(),amt2,data[1][FOUND_CODE],0,null);

		building=CMClass.getItem((String)foundRecipe.get(RCP_CLASSTYPE));
		if(building==null)
		{
			commonTell(mob,"There's no such thing as a "+foundRecipe.get(RCP_CLASSTYPE)+"!!!");
			return false;
		}
		
		duration=getDuration(CMath.s_int((String)foundRecipe.get(RCP_TICKS)),mob,CMath.s_int((String)foundRecipe.get(RCP_LEVEL)),4);
		String misctype=(String)foundRecipe.get(RCP_MISCTYPE);
		String itemName=null;
		if(!misctype.equalsIgnoreCase("area"))
		{
			if(((String)foundRecipe.get(RCP_FINALNAME)).trim().startsWith("%"))
				itemName=replacePercent((String)foundRecipe.get(RCP_FINALNAME),clanTypeName+" "+clanName);
			else
				itemName=replacePercent((String)foundRecipe.get(RCP_FINALNAME),"of "+clanTypeName+" "+clanName);
			if(misctype.length()>0)
				building.setReadableText(misctype);
		}
		else
		{
			LegalBehavior B=CMLib.law().getLegalBehavior(mob.location().getArea());
			Area A2=CMLib.law().getLegalObject(mob.location().getArea());
			if((B==null)||(A2==null))
			{
				commonTell(mob,"This area is controlled by the Archons -- you can't build that here.");
				return false;
			}
			if((B.rulingOrganization().length()==0)||(mob.getClanRole(B.rulingOrganization())==null))
			{
				commonTell(mob,"This area is not controlled by your clan -- you can't build that here.");
				return false;
			}
			
			itemName=replacePercent((String)foundRecipe.get(RCP_FINALNAME),"of "+A2.name()).toLowerCase();
			building.setReadableText(A2.name());
		}
		itemName=CMLib.english().startWithAorAn(itemName);
		building.setName(itemName);
		startStr="<S-NAME> start(s) crafting "+building.name()+".";
		displayText="You are crafting "+building.name();
		playSound="sanding.wav";
		verb="crafting "+building.name();
		building.setDisplayText(itemName+" lies here");
		building.setDescription(itemName+". ");
		building.basePhyStats().setWeight(amt1+amt2);
		building.setBaseValue(CMath.s_int((String)foundRecipe.get(RCP_VALUE)));
		building.setMaterial(data[0][FOUND_CODE]);
		int hardness=RawMaterial.CODES.HARDNESS(data[0][FOUND_CODE])-6;
		building.basePhyStats().setLevel(CMath.s_int((String)foundRecipe.get(RCP_LEVEL))+(hardness*3));
		if(building.basePhyStats().level()<1) building.basePhyStats().setLevel(1);
		int capacity=CMath.s_int((String)foundRecipe.get(RCP_CAPACITY));
		long canContain=getContainerType((String)foundRecipe.get(RCP_CONTAINMASK));
		int armordmg=CMath.s_int((String)foundRecipe.get(RCP_ARMORDMG));
		building.setSecretIdentity(getBrand(mob));
		String spell=(foundRecipe.size()>RCP_SPELL)?((String)foundRecipe.get(RCP_SPELL)).trim():"";
		if(building instanceof ClanItem)
		{
			building.basePhyStats().setSensesMask(PhyStats.SENSE_UNLOCATABLE);
			if(clanC!=null)
				((ClanItem)building).setClanID(clanC.clanID());
			else
			if(CMLib.clans().numClans()>0)
				((ClanItem)building).setClanID(CMLib.clans().clans().nextElement().clanID());
			((ClanItem)building).setCIType(CMath.s_int((String)foundRecipe.get(RCP_CITYPE)));
			if(((ClanItem)building).ciType()==ClanItem.CI_PROPAGANDA)
			{
				building.setMaterial(RawMaterial.RESOURCE_PAPER);
				CMLib.flags().setReadable(building,true);
				building.setReadableText("Read the glorious propaganda of "+clanTypeName+" "+clanName.toLowerCase()+"! Join and fight for us today!");
			}
		}

		if((building.isReadable())
		&&((building.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN))
			building.setMaterial(RawMaterial.RESOURCE_PAPER);

		addSpells(building,spell);
		if((building instanceof Armor)&&(!(building instanceof FalseLimb)))
		{
			((Armor)building).basePhyStats().setArmor(0);
			if(armordmg!=0)
				((Armor)building).basePhyStats().setArmor(armordmg+(abilityCode()-1));
			setWearLocation(building,misctype,hardness);
		}

		if(building instanceof Container)
			if(capacity>0)
			{
				((Container)building).setCapacity(capacity+amt1+amt2);
				((Container)building).setContainTypes(canContain);
			}
		building.recoverPhyStats();
		building.text();
		building.recoverPhyStats();


		messedUp=!proficiencyCheck(mob,0,auto);

		if(autoGenerate>0)
		{
			commands.addElement(building);
			return true;
		}

		CMMsg msg=CMClass.getMsg(mob,building,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
			ClanCrafting CC=(ClanCrafting)mob.fetchEffect(ID());
			if((CC!=null)&&(clanC!=null))
			{
				clanC.setExp(clanC.getExp()-expRequired);
				clanC.update();
				
				CC.expRequired=expRequired;
				CC.myClan=clanC;
			}
		}
		return true;
	}
}
