package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftParms;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor.CraftorType;
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
   Copyright 2004-2022 Bo Zimmerman

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
	public CraftorType getCraftorType()
	{
		return CraftorType.ClanItems;
	}

	@Override
	public String supportedResourceString()
	{
		return "WOODEN|METAL|MITHRIL";
	}

	protected int expRequired = 0;
	protected Clan myClan=null;

	@Override
	public String parametersFormat()
	{
		return
		"ITEM_NAME\tRESOURCE_NAME_AMOUNT_MATERIAL_REQUIRED\tRESOURCE_NAME_AMOUNT_MATERIAL_REQUIRED\t"
		+"CLAN_ITEM_CODENUMBER\tITEM_LEVEL\tBUILD_TIME_TICKS\tCLAN_EXPERIENCE_COST_AMOUNT\t"
		+"ITEM_BASE_VALUE\tITEM_CLASS_ID\tCLAN_AREA_FLAG||CODED_WEAR_LOCATION||READABLE_TEXT\t"
		+"CONTAINER_CAPACITY\tBASE_ARMOR_AMOUNT||BOARDABLE_POP\tCONTAINER_TYPE\tCODED_SPELL_LIST\t"
		+"REQUIRED_COMMON_SKILL_ID";
	}

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

	@Override
	public List<List<String>> fetchMyRecipes(final MOB mob)
	{
		return this.addRecipes(mob, loadRecipes());
	}

	public Hashtable<String, String> parametersFields()
	{
		return new Hashtable<String, String>();
	}

	@Override
	public String parametersFile()
	{
		return "clancraft.txt";
	}

	@Override
	protected List<List<String>> loadRecipes()
	{
		return loadRecipes(parametersFile());
	}

	@Override
	public boolean supportsDeconstruction()
	{
		return false;
	}

	protected List<Item> getCastles()
	{
		final String allItemID = "CLANCRAFTING_PARSED";
		@SuppressWarnings("unchecked")
		List<Item> castlePrototypes = (List<Item>)Resources.getResource(allItemID);
		if(castlePrototypes == null)
		{
			castlePrototypes=new Vector<Item>();
			final CMFile F=new CMFile(Resources.makeFileResourceName("skills/clancastles.cmare"),null);
			if(F.exists())
			{
				CMLib.coffeeMaker().addItemsFromXML(F.textUnformatted().toString(), castlePrototypes, null);
				for(final Item I : castlePrototypes)
					CMLib.threads().deleteAllTicks(I);
				if(castlePrototypes.size()>0)
					Resources.submitResource(allItemID, castlePrototypes);
			}
		}
		return castlePrototypes;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<List<String>> loadRecipes(final String filename)
	{
		List<List<String>> V=(List<List<String>>)Resources.getResource("PARSED_RECIPE: "+filename);
		if(V==null)
		{
			final StringBuffer str=new CMFile(Resources.buildResourcePath("skills")+filename,null,CMFile.FLAG_LOGERRORS).text();
			V=loadList(str);
			for(final Item I : getCastles())
			{
				final List<String> recipe = new ArrayList<String>();
				int weight;
				if(I instanceof Boardable)
					weight=((Boardable)I).getArea().numberOfProperIDedRooms();
				else
					weight=I.basePhyStats().weight();
				recipe.add(I.Name());
				recipe.add(RawMaterial.CODES.NAME(I.material())+"/"+(weight*2500)); // material
				recipe.add(""); // material#2
				recipe.add(""+((I instanceof ClanItem)?((ClanItem)I).getClanItemType().ordinal():0)); // type
				recipe.add(""+I.basePhyStats().level()); // level
				recipe.add(""+(weight * 450)); // build time
				recipe.add(""+(weight*50)); // xp cost
				recipe.add(""+I.baseGoldValue()); // value
				recipe.add(I.ID()); // class
				recipe.add(""); // area flag, wear location, readable text
				recipe.add("0"); // container capacity
				if(I instanceof Boardable)
					recipe.add(""+getDefaultPopRequirement((Boardable)I)); // base armor/room pop
				else
					recipe.add("0"); // base armor/room pop
				recipe.add("0"); // container type
				recipe.add(""); // additional spells
				recipe.add("Masonry"); // required common skill id ?!
				V.add(recipe);
			}
			Collections.sort(V,new Comparator<List<String>>()
			{
				@Override
				public int compare(final List<String> o1, final List<String> o2)
				{
					if(o1.size()<=RCP_LEVEL)
						return -1;
					if(o2.size()<=RCP_LEVEL)
						return 1;
					final int level1=CMath.s_int(o1.get(RCP_LEVEL));
					final int level2=CMath.s_int(o2.get(RCP_LEVEL));
					return (level1>level2)?1:(level1<level2)?-1:0;
				}
			});
			if((V.size()==0)
			&&(!ID().equals("GenCraftSkill"))
			&&(!ID().endsWith("Costuming")))
				Log.errOut(ID(),"Recipes not found!");
			V=new ReadOnlyList<List<String>>(V);
			Resources.submitResource("PARSED_RECIPE: "+filename,V);
		}
		return V;
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
							myClan.adjExp(mob, expRequired);
							myClan.update();
						}
					}
					else
					{
						dropAWinner(mob,buildingI);
						CMLib.achievements().possiblyBumpAchievement(mob, AchievementLibrary.Event.CRAFTING, 1, this, buildingI);
						if(buildingI instanceof Boardable)
						{
							if(buildingI instanceof Boardable)
								((Boardable)buildingI).dockHere(mob.location());
						}
						else
							CMLib.commands().postGet(mob,null,buildingI,true);
					}
				}
				buildingI=null;
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean canBeLearnedBy(final MOB teacher, final MOB student)
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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		return autoGenInvoke(mob,commands,givenTarget,auto,asLevel,0,false,new Vector<Item>(0));
	}

	protected int getDefaultPopRequirement(final Boardable B)
	{
		if(B==null)
			return 0;
		if(B.getArea()==null)
			return 0;
		return 20 + (B.getArea().numberOfProperIDedRooms() * 10);
	}

	@Override
	protected boolean autoGenInvoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto,
								 final int asLevel, final int autoGenerate, final boolean forceLevels, final List<Item> crafted)
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
		@SuppressWarnings("unused")
		int recipeLevel=1;
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
		if(str.equalsIgnoreCase("list") && (autoGenerate<=0))
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
			final List<List<String>> listRecipes=((mask.length()==0) || mask.equalsIgnoreCase("all")) ? recipes : super.matchingRecipeNames(recipes, mask, true);
			for(int r=0;r<listRecipes.size();r++)
			{
				final List<String> V=listRecipes.get(r);
				if(V.size()>0)
				{
					final String item=replacePercent(V.get(RCP_FINALNAME),"");
					final int level=CMath.s_int(V.get(RCP_LEVEL));
					final int exp=CMath.s_int(V.get(RCP_EXP));
					if((level<=xlevel(mob))||allFlag)
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
					recipeLevel=level;
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

		buildingI=CMClass.getItem(foundRecipe.get(RCP_CLASSTYPE));
		if(buildingI instanceof Boardable)
		{
			for(final Item I : getCastles())
				if(I.Name().equals(foundRecipe.get(RCP_FINALNAME)))
					buildingI=(Item)I.copyOf();
		}
		final Item buildingI=this.buildingI;
		if(buildingI==null)
		{
			commonTell(mob,L("There's no such thing as a @x1!!!",foundRecipe.get(RCP_CLASSTYPE)));
			return false;
		}
		final int armordmg=CMath.s_int(foundRecipe.get(RCP_ARMORDMG));
		if((buildingI instanceof SiegableItem)
		&&(buildingI instanceof Boardable)
		&&(clanC!=null)
		&&(autoGenerate<=0))
		{
			final Room R=CMLib.map().roomLocation(mob);
			if(R==null)
				return false;
			final LegalBehavior conqB = CMLib.law().getLegalBehavior(R.getArea());
			if((conqB == null)
			||(conqB.rulingOrganization().length()==0)
			||(!conqB.rulingOrganization().equalsIgnoreCase(clanC.clanID()))
			||((!conqB.isFullyControlled())&&(!CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDROOMS))))
			{
				commonTell(mob, L("That can only be built in an area conquered by @x1.",clanName));
				return false;
			}
			final Area A=CMLib.law().getLegalObject(R.getArea());
			int areaPop = (A==null)?0:A.getAreaIStats()[Area.Stats.INTELLIGENT_MOBS.ordinal()];
			boolean another=false;
			if(A != null)
			{
				for(final Enumeration<Room> r=A.getMetroMap();r.hasMoreElements();)
				{
					final Room R1=r.nextElement();
					if(R1!=null)
					{
						for(final Enumeration<Item> i=R1.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I instanceof ClanItem)
							&&(I instanceof SiegableItem)
							&&(I instanceof Boardable))
							{
								another=true;
								areaPop -= getDefaultPopRequirement((Boardable)I);
							}
						}
					}
				}
			}
			if(areaPop < armordmg)
			{
				final String areaName = (A==null)?"This Area":A.Name();
				if(another)
					commonTell(mob, L("@x1 does not have the population to support another such structure.",areaName));
				else
					commonTell(mob, L("@x1 does not have the population to support such a structure.",areaName));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
		{
			buildingI.destroy();
			return false;
		}
		if((amt1>0)&&(autoGenerate<=0))
			CMLib.materials().destroyResourcesValue(mob.location(),amt1,data[0][FOUND_CODE],data[0][FOUND_SUB],0,0);
		if((amt2>0)&&(autoGenerate<=0))
			CMLib.materials().destroyResourcesValue(mob.location(),amt2,data[1][FOUND_CODE],data[1][FOUND_SUB],0,0);

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
		final String oldName=buildingI.Name();
		buildingI.setName(itemName);
		startStr=L("<S-NAME> start(s) crafting @x1.",buildingI.name());
		displayText=L("You are crafting @x1",buildingI.name());
		playSound="sanding.wav";
		verb=L("crafting @x1",buildingI.name());
		if(!(buildingI instanceof Boardable))
		{
			buildingI.setDisplayText(L("@x1 lies here",itemName));
			buildingI.setDescription(itemName+". ");
		}
		else
			buildingI.setDisplayText(CMStrings.replaceAllIgnoreCase(buildingI.displayText(), oldName, itemName));
		buildingI.basePhyStats().setWeight(amt1+amt2);
		buildingI.setBaseValue(CMath.s_int(foundRecipe.get(RCP_VALUE)));
		buildingI.setMaterial(getBuildingMaterial(amt1+amt2,data,new int[CF_TOTAL]));
		final int hardness=RawMaterial.CODES.HARDNESS(buildingI.material())-6;
		buildingI.basePhyStats().setLevel(CMath.s_int(foundRecipe.get(RCP_LEVEL))+(hardness*3));
		if(buildingI.basePhyStats().level()<1)
			buildingI.basePhyStats().setLevel(1);
		final int capacity=CMath.s_int(foundRecipe.get(RCP_CAPACITY));
		final long canContain=getContainerType(foundRecipe.get(RCP_CONTAINMASK));
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
				final ClanItem.ClanItemType cType = (ClanItem.ClanItemType)CMath.s_valueOf(ClanItem.ClanItemType.class, type.toUpperCase().trim());
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

		addSpellsOrBehaviors(buildingI,spell,null,null);
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
				clanC.adjExp(mob,-expRequired);
				clanC.update();

				CC.expRequired=expRequired;
				CC.myClan=clanC;
			}
		}
		return true;
	}
}
