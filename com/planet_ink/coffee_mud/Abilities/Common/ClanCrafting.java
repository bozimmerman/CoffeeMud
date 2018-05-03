package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "ClanCrafting";
	}

	private final static String localizedName = CMLib.lang().L("Clan Crafting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"CLANCRAFT"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String supportedResourceString()
	{
		return "WOODEN|METAL|MITHRIL";
	}

	protected int expRequired = 0;
	protected Clan myClan=null;

	@Override
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

	public Hashtable<String,String> parametersFields(){ return new Hashtable<String,String>();}
	@Override
	public String parametersFile()
	{
		return "clancraft.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return super.loadRecipes(parametersFile());
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return false;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((buildingI!=null)&&(!aborted))
				{
					if(messedUp)
					{
						commonEmote(mob,L("<S-NAME> mess(es) up crafting @x1.",buildingI.name()));
						if(myClan!=null)
						{
							myClan.setExp(myClan.getExp()+expRequired);
							myClan.update();
						}
					}
					else
					{
						dropAWinner(mob,buildingI);
						CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this);
						CMLib.commands().postGet(mob,null,buildingI,true);
					}
				}
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null)
			return true;
		if(!student.clans().iterator().hasNext())
		{
			if(teacher != null)
				teacher.tell(L("@x1 is not a member of a clan.",student.name()));
			student.tell(L("You need to belong to a clan before you can learn @x1.",name()));
			return false;
		}
		final Pair<Clan,Integer> p=CMLib.clans().findPrivilegedClan(student, Clan.Function.ENCHANT);
		if(p==null)
		{
			if(teacher != null)
				teacher.tell(L("@x1 is not authorized to draw from the power of @x2 clan.",student.name(),student.charStats().hisher()));
			student.tell(L("You must be authorized to draw from the power of your clan to learn this skill."));
			return false;
		}
		return true;
	}

	@Override
	public String getDecodedComponentsDescription(final MOB mob, final List<String> recipe)
	{
		return "Not implemented";
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new Vector<Item>(0));
	}
	
	@Override
	protected boolean autoGenInvoke(final MOB mob, List<String> commands, Physical givenTarget, final boolean auto, 
								 final int asLevel, int autoGenerate, boolean forceLevels, List<Item> crafted)
	{
		if(super.checkStop(mob, commands))
			return true;

		if(super.checkInfo(mob, commands))
			return true;

		randomRecipeFix(mob,addRecipes(mob,loadRecipes()),commands,autoGenerate);
		if(commands.size()==0)
		{
			commonTell(mob,L("Make what? Enter \"clancraft list\" for a list, or \"clancraft stop\" to cancel."));
			return false;
		}
		String clanTypeName="Clan";
		String clanName="None";
		Clan clanC=null;
		if(autoGenerate<=0)
		{
			if(!mob.clans().iterator().hasNext())
			{
				mob.tell(L("You must be a member of a clan to use this skill."));
				return false;
			}
			final Pair<Clan,Integer> p=CMLib.clans().findPrivilegedClan(mob, Clan.Function.ENCHANT);
			if((p==null)
			&&(!CMSecurity.isASysOp(mob)))
			{
				mob.tell(L("You are not authorized to draw from the power of your clan."));
				return false;
			}
			if(p!=null)
			{
				clanName=p.first.getName();
				clanTypeName=p.first.getGovernmentName();
				clanC=p.first;
			}
		}
		final List<List<String>> recipes=addRecipes(mob,loadRecipes());
		final String str=commands.get(0);
		String startStr=null;
		int duration=4;
		final int[] cols={
				CMLib.lister().fixColWidth(24,mob.session()),
				CMLib.lister().fixColWidth(9,mob.session()),
				CMLib.lister().fixColWidth(14,mob.session()),
				CMLib.lister().fixColWidth(4,mob.session()),
				CMLib.lister().fixColWidth(14,mob.session()),
				CMLib.lister().fixColWidth(4,mob.session())
			};
		if(str.equalsIgnoreCase("list"))
		{
			String mask=CMParms.combine(commands,1);
			boolean allFlag=false;
			if(mask.equalsIgnoreCase("all"))
			{
				allFlag=true;
				mask="";
			}
			final StringBuffer buf=new StringBuffer("");
			buf.append(CMStrings.padRight(L("Item"),cols[0])+" "
					   +CMStrings.padRight(L("Exp"),cols[1])+" "
					   +CMStrings.padRight(L("Material#1"),cols[2])+" "
					   +CMStrings.padRight(L("Amt#1"),cols[3])+" "
					   +CMStrings.padRight(L("Material#2"),cols[4])+" "
					   +CMStrings.padRight(L("Amt#2"),cols[5])+"\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				final List<String> V=recipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final int exp=CMath.s_int(V.get(RCP_EXP));
					if(((level<=xlevel(mob))||allFlag)
					&&((mask.length()==0)||mask.equalsIgnoreCase("all")||CMLib.english().containsString(item,mask)))
					{
						String mat1=V.get(RCP_MATERIAL1);
						String mat2=V.get(RCP_MATERIAL2);
						String amt1="";
						String amt2="";
						final int m1=mat1.indexOf('/');
						if(m1>=0)
						{
							amt1=mat1.substring(m1+1);
							mat1=mat1.substring(0,m1).toLowerCase();
							//amt1=""+adjustWoodRequired(CMath.s_int(amt1),mob);
						}
						final int m2=mat2.indexOf('/');
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
		activity = CraftingActivity.CRAFTING;
		buildingI=null;
		messedUp=false;
		final String recipeName=CMParms.combine(commands,0);
		List<String> foundRecipe=null;
		final List<List<String>> matches=matchingRecipeNames(recipes,recipeName,true);
		for(int r=0;r<matches.size();r++)
		{
			final List<String> V=matches.get(r);
			if(V.size()>0)
			{
				final int level=CMath.s_int(V.get(RCP_LEVEL));
				if((autoGenerate>0)||(level<=xlevel(mob)))
				{
					foundRecipe=V;
					break;
				}
			}
		}
		if(foundRecipe==null)
		{
			commonTell(mob,L("You don't know how to make a '@x1'.  Try \"clancraft list\" for a list.",recipeName));
			return false;
		}

		int amt1=0;
		int amt2=0;
		String mat1=foundRecipe.get(RCP_MATERIAL1);
		String mat2=foundRecipe.get(RCP_MATERIAL2);
		final int m1=mat1.indexOf('/');
		if(m1>=0)
		{
			amt1=CMath.s_int(mat1.substring(m1+1));
			mat1=mat1.substring(0,m1).toLowerCase();
			//amt1=adjustWoodRequired(amt1, mob);
		}
		final int m2=mat2.indexOf('/');
		if(m2>=0)
		{
			amt2=CMath.s_int(mat2.substring(m2+1));
			mat2=mat2.substring(0,m2).toLowerCase();
			//amt2=adjustWoodRequired(amt2, mob);
		}

		expRequired=CMath.s_int(foundRecipe.get(RCP_EXP));
		expRequired=getXPCOSTAdjustment(mob,expRequired);
		if((clanC!=null)&&(clanC.getExp()<expRequired))
		{
			mob.tell(L("You need @x1 to do that, but your @x2 has only @x3 experience points.",""+expRequired,clanTypeName,""+clanC.getExp()));
			return false;
		}
		final int[][] data=fetchFoundResourceData(mob,amt1,mat1,null,amt2,mat2,null,false,autoGenerate,null);
		if(data==null)
			return false;
		amt1=data[0][FOUND_AMT];
		amt2=data[1][FOUND_AMT];
		final String reqskill=foundRecipe.get(RCP_REQUIREDSKILL);
		if((autoGenerate<=0)&&(reqskill.trim().length()>0))
		{
			final Ability A=CMClass.findAbility(reqskill.trim());
			if((A!=null)&&(mob.fetchAbility(A.ID())==null))
			{
				commonTell(mob,L("You need to know @x1 to craft this item.",A.name()));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		if((amt1>0)&&(autoGenerate<=0))
			CMLib.materials().destroyResourcesValue(mob.location(),amt1,data[0][FOUND_CODE],0,null);
		if((amt2>0)&&(autoGenerate<=0))
			CMLib.materials().destroyResourcesValue(mob.location(),amt2,data[1][FOUND_CODE],0,null);

		buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
		if(buildingI==null)
		{
			commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
			return false;
		}

		duration=getDuration(CMath.s_int(foundRecipe.get(RCP_TICKS)),mob,CMath.s_int(foundRecipe.get(RCP_LEVEL)),4);
		final String misctype=foundRecipe.get(RCP_MISCTYPE);
		String itemName=null;
		if(!misctype.equalsIgnoreCase("area"))
		{
			if(foundRecipe.get(RCP_FINALNAME).trim().startsWith("%"))
				itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),clanTypeName+" "+clanName);
			else
				itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),"of "+clanTypeName+" "+clanName);
			if(misctype.length()>0)
				buildingI.setReadableText(misctype);
		}
		else
		{
			final LegalBehavior B=CMLib.law().getLegalBehavior(mob.location().getArea());
			final Area A2=CMLib.law().getLegalObject(mob.location().getArea());
			if((B==null)||(A2==null))
			{
				commonTell(mob,L("This area is controlled by the Archons -- you can't build that here."));
				return false;
			}
			if((B.rulingOrganization().length()==0)||(mob.getClanRole(B.rulingOrganization())==null))
			{
				commonTell(mob,L("This area is not controlled by your clan -- you can't build that here."));
				return false;
			}

			itemName=replacePercent(foundRecipe.get(RCP_FINALNAME),"of "+A2.name()).toLowerCase();
			buildingI.setReadableText(A2.name());
		}
		itemName=CMLib.english().startWithAorAn(itemName);
		buildingI.setName(itemName);
		startStr=L("<S-NAME> start(s) crafting @x1.",buildingI.name());
		displayText=L("You are crafting @x1",buildingI.name());
		playSound="sanding.wav";
		verb=L("crafting @x1",buildingI.name());
		buildingI.setDisplayText(L("@x1 lies here",itemName));
		buildingI.setDescription(itemName+". ");
		buildingI.basePhyStats().setWeight(amt1+amt2);
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
		buildingI.setMaterial(getBuildingMaterial(amt1+amt2,data,new int[CF_TOTAL]));
		final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-6;
		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL))+(hardness*3));
		if(buildingI.basePhyStats().level()<1)
			buildingI.basePhyStats().setLevel(1);
		final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
		final long canContain=getContainerType(foundRecipe.get(RCP_CONTAINMASK));
		final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
		setBrand(mob, buildingI);
		final String spell=(foundRecipe.size()>RCP_SPELL)?foundRecipe.get(RCP_SPELL).trim():"";
		if(buildingI instanceof ClanItem)
		{
			buildingI.basePhyStats().setSensesMask(PhyStats.SENSE_UNLOCATABLE);
			if(clanC!=null)
				((ClanItem)buildingI).setClanID(clanC.clanID());
			else
			if(CMLib.clans().numClans()>0)
				((ClanItem)buildingI).setClanID(CMLib.clans().clans().nextElement().clanID());
			final String type = foundRecipe.get(RCP_CITYPE);
			if(CMath.isInteger(type))
				((ClanItem)buildingI).setClanItemType(ClanItem.ClanItemType.values()[CMath.s_int(type)]);
			else
			{
				ClanItem.ClanItemType cType = (ClanItem.ClanItemType)CMath.s_valueOf(ClanItem.ClanItemType.class, type.toUpperCase().trim());
				if(cType != null)
					((ClanItem)buildingI).setClanItemType(cType);
			}
			if(((ClanItem)buildingI).getClanItemType()==ClanItem.ClanItemType.PROPAGANDA)
			{
				buildingI.setMaterial(RawMaterial.RESOURCE_PAPER);
				CMLib.flags().setReadable(buildingI,true);
				buildingI.setReadableText("Read the glorious propaganda of "+clanTypeName+" "+clanName.toLowerCase()+"! Join and fight for us today!");
			}
		}

		if((buildingI.isReadable())
		&&((buildingI.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN))
			buildingI.setMaterial(RawMaterial.RESOURCE_PAPER);

		addSpells(buildingI,spell);
		if((buildingI instanceof Armor)&&(!(buildingI instanceof FalseLimb)))
		{
			((Armor)buildingI).basePhyStats().setArmor(0);
			if(armordmg!=0)
				((Armor)buildingI).basePhyStats().setArmor(armordmg+(baseYield()+abilityCode()-1));
			setWearLocation(buildingI,misctype,hardness);
		}

		if(buildingI instanceof Container)
		{
			if(capacity>0)
			{
				((Container)buildingI).setCapacity(capacity+amt1+amt2);
				((Container)buildingI).setContainTypes(canContain);
			}
		}
		buildingI.recoverPhyStats();
		buildingI.text();
		buildingI.recoverPhyStats();

		messedUp=!proficiencyCheck(mob,0,auto);

		if(autoGenerate>0)
		{
			crafted.add(buildingI);
			return true;
		}

		final CMMsg msg=CMClass.getMsg(mob,buildingI,this,getActivityMessageType(),startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
			final ClanCrafting CC=(ClanCrafting)mob.fetchEffect(ID());
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
