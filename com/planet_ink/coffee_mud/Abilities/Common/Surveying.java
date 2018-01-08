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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2017-2018 Bo Zimmerman

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
public class Surveying extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Surveying";
	}

	private final static String	localizedName	= CMLib.lang().L("Surveying");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SURVEYWRITE", "SUWRITE" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_CALLIGRAPHY;
	}

	protected Physical	found		= null;
	protected Item		catalogI	= null;
	protected boolean	addLore		= false;
	protected boolean	addAppraise	= false;
	
	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public Surveying()
	{
		super();
		displayText=L("You are surveying...");
		verb=L("surveying");
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(!aborted)
			&&(!helping))
			{
				final MOB mob=(MOB)affected;
				final int expertise=super.getXLEVELLevel(mob);
				if(catalogI==null)
					commonTell(mob,L("You mess up your surveying."));
				else
				{
					final String subject;
					final String message;
					if(found instanceof Area)
					{
						Area A=(Area)found;
						subject=A.Name();
						final StringBuilder msgBuilder=new StringBuilder("");
						msgBuilder.append(L("^HArea Name  : ^N")).append(A.name()).append("\n\r");
						msgBuilder.append(L("^HLinks to   : ^N"));
						List<String> lAreasV=new ArrayList<String>();
						Map<String,int[]> rTypesV=new TreeMap<String,int[]>();
						for(Enumeration<Room> r= A.getFilledProperMap();r.hasMoreElements();)
						{
							final Room R=r.nextElement();
							if((R==null)
							||(mob.playerStats()==null)
							||(!mob.playerStats().hasVisited(R)))
								continue;
							if(!rTypesV.containsKey(R.name()))
								rTypesV.put(R.name(), new int[]{1});
							else
								rTypesV.get(R.name())[0]++;
							for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
							{
								final Room R2=R.getRoomInDir(d);
								final Exit E2=R.getExitInDir(d);
								if((R2!=null)
								&&(E2!=null)
								&&(R2.getArea() != A))
								{
									if(!lAreasV.contains(R2.getArea().Name()))
										lAreasV.add(R2.getArea().Name());
								}
							}
						}
						msgBuilder.append(CMLib.english().toEnglishStringList(lAreasV));
						msgBuilder.append("\n\r");
						if(expertise > 0)
							msgBuilder.append(L("^HPct Visited: ^N")).append(mob.playerStats().percentVisited(mob,A)).append("\n\r");
						if(expertise > 1)
						{
							msgBuilder.append(L("^HMedian Lvl : ^N")).append(A.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()]).append("\n\r");
							if(A.getPlayerLevel()>0)
								msgBuilder.append(L("^HPlayer Lvl : ^N")).append(A.getPlayerLevel()).append("\n\r");
						}
						if(expertise > 2)
						{
							msgBuilder.append(L("^HLevel Range: ^N")).append(A.getAreaIStats()[Area.Stats.MIN_LEVEL.ordinal()])
									  .append("-").append(A.getAreaIStats()[Area.Stats.MAX_LEVEL.ordinal()]).append("\n\r");
						}
						if(expertise > 3)
						{
							Faction F=CMLib.factions().getFaction(CMLib.factions().AlignID());
							if(F!=null)
								msgBuilder.append(L("^HMed Align. : ^N")).append(F.fetchRangeName(A.getAreaIStats()[Area.Stats.MED_ALIGNMENT.ordinal()])).append("\n\r");
						}
						if(expertise > 4)
						{
							Faction F=CMLib.factions().getFaction(CMLib.factions().AlignID());
							if(F!=null)
							{
								msgBuilder.append(L("^HAlign Range: ^N"))
									.append(F.fetchRangeName(A.getAreaIStats()[Area.Stats.MIN_ALIGNMENT.ordinal()]))
									.append("-")
									.append(F.fetchRangeName(A.getAreaIStats()[Area.Stats.MAX_ALIGNMENT.ordinal()])).append("\n\r");
							}
						}
						if(expertise > 5)
						{
							msgBuilder.append(L("^HClimate    : ^N"));
							List<String> climateV=new ArrayList<String>();
							for(int i=1;i<Places.NUM_CLIMATES;i++)
							{
								final String climstr=Places.CLIMATE_DESCS[i];
								final int mask=(int)CMath.pow(2,i-1);
								if(mask != Area.CLIMASK_INHERIT)
								{
									if(CMath.bset(A.getClimateTypeCode(),mask))
										climateV.add(climstr.toLowerCase());
								}
							}
							if(climateV.size()==0)
								msgBuilder.append(L("Normal")).append("\n\r");
							else
								msgBuilder.append(CMLib.english().toEnglishStringList(climateV)).append("\n\r");
						}
						if(expertise > 6)
						{
							for(String roomType : rTypesV.keySet())
							{
								msgBuilder.append("^H"+L(CMStrings.padRight(roomType, 11))).append("^N: ").append(rTypesV.get(roomType)[0]).append("\n\r");
							}
						}
						
						message = msgBuilder.toString();
					}
					else
					if(found instanceof Room)
					{
						final Room room=(Room)found;
						final Area area=room.getArea();
						String roomNumber = room.roomID();
						if(roomNumber.startsWith(area.Name()+"#"))
							roomNumber=roomNumber.substring(area.Name().length());
						subject=room.displayText()+" ("+roomNumber+")";
						final StringBuilder msgBuilder=new StringBuilder("");
						msgBuilder.append(L("^HRoom name  : ^N@x1",room.displayText())).append("\n\r");
						msgBuilder.append(L("^HRoom area  : ^N@x1",area.Name())).append("\n\r");
						msgBuilder.append(L("^HRoom number: ^N@x1",roomNumber)).append("\n\r");
						if(expertise > 1)
							msgBuilder.append(L("^HRoom Descr.: ^N\n\r@x1",room.description(mob))).append("\n\r");
						msgBuilder.append("\n\r");
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							Exit nextE=room.getExitInDir(d);
							Room nextR=room.getRoomInDir(d);
							if((nextE!=null)
							&&(nextR!=null)
							&&(CMLib.flags().canBeSeenBy(nextE, mob)))
							{
								String nextRoomNumber = nextR.roomID();
								if(nextRoomNumber.startsWith(area.Name()+"#"))
									nextRoomNumber=nextRoomNumber.substring(area.Name().length());
								msgBuilder.append(CMStrings.padRight(L("^HExit "+Directions.instance().getDirectionName(d)),10))
										.append(": ^N").append(nextR.displayText());
								if(expertise > 0)
									msgBuilder.append(" (").append(nextRoomNumber).append(")");
								msgBuilder.append(")");
								if(expertise > 6)
								{
									Trap theTrap=CMLib.utensils().fetchMyTrap(room);
									if(theTrap!=null)
										msgBuilder.append(L("^xTrapped!^N^."));
								}
								msgBuilder.append("\n\r");
							}
						}
						if(expertise > 2)
						{
							msgBuilder.append(L("^HRoom Items.: ^N"));
							final List<Item> viewItems=new ArrayList<Item>(room.numItems());
							for(int c=0;c<room.numItems();c++)
							{
								final Item item=room.getItem(c);
								if(item==null)
									continue;

								if(item.container()==null)
									viewItems.add(item);
							}
							final StringBuilder itemStr=CMLib.lister().lister(mob,viewItems,false,null,null,true,false);
							if(itemStr.length()>0)
								msgBuilder.append("\n\r").append(itemStr);
							else
								msgBuilder.append(L("None")).append("\n\r");
							
						}
						if(expertise > 3)
						{
							msgBuilder.append(L("^HRoom Rescs.: ^N"));
							List<String> possResources = new ArrayList<String>();
							if(room.resourceChoices()!=null)
							{
								for(Integer I : room.resourceChoices())
									possResources.add(RawMaterial.CODES.NAME(I.intValue()).toLowerCase());
							}
							if(possResources.size()==0)
								msgBuilder.append(L("None"));
							else
								msgBuilder.append(CMLib.english().toEnglishStringList(possResources));
							msgBuilder.append("\n\r");
						}
						if(expertise > 4)
						{
							msgBuilder.append(L("^HRoom Inhab.: ^N"));
							List<String> roomMobs = new ArrayList<String>(room.numInhabitants()-1);
							for(int i=0;i<room.numInhabitants();i++)
							{
								final MOB mob2=room.fetchInhabitant(i);
								if((mob2!=null)&&(mob2!=mob))
								{
									final String displayText=mob2.displayText(mob);
									if((displayText.length()>0)
									&&(CMLib.flags().canBeSeenBy(mob2,mob)))
										roomMobs.add(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(displayText)));
								}
							}
							if(roomMobs.size()==0)
								msgBuilder.append(L("None"));
							else
							{
								msgBuilder.append("\n\r");
								for(String roomMob : roomMobs)
									msgBuilder.append("      ^M"+roomMob).append("^N\n\r");
							}
						}
						if(expertise > 5)
						{
							msgBuilder.append(L("^HRoom Trap  : ^N"));
							Trap theTrap=CMLib.utensils().fetchMyTrap(room);
							if(theTrap!=null)
								msgBuilder.append(theTrap.name());
							else
								msgBuilder.append(L("None"));
							msgBuilder.append("\n\r");
						}
						
						msgBuilder.append("\n\r");
						message = msgBuilder.toString();
					}
					else
					{
						subject=null;
						message=null;
					}
					if(message != null)
					{
						boolean done=false;
						if((catalogI instanceof Book)
						&&(((((Book)catalogI).getMaxPages()==0)||((Book)catalogI).getMaxPages() > 1)))
						{
							for(int pg=1;pg<=((Book)catalogI).getUsedPages();pg++)
							{
								String content=((Book)catalogI).getRawContent(pg);
								if(content.startsWith("::"+subject+"::")
								||(content.startsWith("::"+L("Chapter @x1: ",""+pg)+subject+"::"))
								||(content.startsWith("::"+L("Chapter @x1 : ",""+pg)+subject+"::")))
								{
									final CMMsg msg=CMClass.getMsg(mob,catalogI,this,
											CMMsg.MSG_REWRITE,L("<S-NAME> update(s) <T-NAMESELF>."),
											CMMsg.MSG_REWRITE,"EDIT "+pg+" ::"+subject+"::"+message,
											CMMsg.MSG_REWRITE,L("<S-NAME> update(s) <T-NAMESELF>."));
									if(mob.location().okMessage(mob,msg))
										mob.location().send(mob,msg);
									done=true;
									break;
								}
							}
						}
						if(!done)
						{
							final CMMsg msg=CMClass.getMsg(mob,catalogI,this,
									CMMsg.MSG_WRITE,L("<S-NAME> write(s) on <T-NAMESELF>."),
									CMMsg.MSG_WRITE,"::"+subject+"::"+message,
									CMMsg.MSG_WRITE,L("<S-NAME> write(s) on <T-NAMESELF>."));
							if(mob.location().okMessage(mob,msg))
								mob.location().send(mob,msg);
						}
					}
				}
			}
		}
		super.unInvoke();
	}
	
	public boolean isPossibleCatalog(final MOB mob, final Item I, final Environmental fullyE, final boolean quiet)
	{
		if((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
		{
			if(!quiet)
				commonTell(mob,L("You can't write in @x1.",I.name(mob)));
			return false;
		}
		if(!CMLib.flags().isReadable(I))
		{
			if(!quiet)
				commonTell(mob,L("@x1 is not even readable!",CMStrings.capitalizeFirstLetter(I.name(mob))));
			return false;
		}
		if(I instanceof Recipe)
		{
			commonTell(mob,L("@x1 isn't a catalog!",CMStrings.capitalizeAndLower(I.name(mob))));
			return false;
		}
		/*
		String brand = getBrand(I);
		if((brand==null)||(brand.length()==0))
		{
			if(!quiet)
				commonTell(mob,L("You aren't permitted to add surveying entries to @x1.",I.name(mob)));
			return false;
		}
		*/
		
		if(fullyE != null)
		{
			if(!Titling.getCatalogType(I).equals(Titling.getCatalogEntryType(fullyE)))
			{
				if(!quiet)
					commonTell(mob,L("@x1 is not a proper catalog for surveying!",CMStrings.capitalizeFirstLetter(I.name(mob)),fullyE.name()));
			}
		}
		return true;
	}

	public Item findCatalogBook(final MOB mob, final Physical itemI)
	{
		Item catalogI=null;
		for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if((I.container()==null)
			&&(!I.amWearingAt(Item.IN_INVENTORY))
			&&(isPossibleCatalog(mob, I, itemI, true)))
				catalogI=I;
		}
		if(catalogI==null)
		{
			for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if((I.container()==null)
				&&(isPossibleCatalog(mob, I, itemI, true)))
					catalogI=I;
			}
		}
		if(catalogI==null)
		{
			for(Enumeration<Item> i=mob.items();i.hasMoreElements();)
			{
				final Item I=i.nextElement();
				if(isPossibleCatalog(mob, I, itemI, true))
					catalogI=I;
			}
		}
		return catalogI;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<1)
		{
			commonTell(mob,L("You must specify what you want to catalog ROOM or AREA, and, optionally, where to add the entry.  If you do "
					+ "not specify the catalog, one prepared with the Titling skill will be automatically selected from your inventory."));
			return false;
		}
		Physical physP=null;
		if(commands.get(0).equalsIgnoreCase("room"))
			physP=mob.location();
		else
		if(commands.get(0).equalsIgnoreCase("area"))
			physP=mob.location().getArea();
		else
		{
			commonTell(mob,L("'@x1' is neither the word ROOM nor AREA.",commands.get(0)));
			return false;
		}
		Item catalogI=null;
		String itemName=CMParms.combine(commands,0);
		if(commands.size()==1) // this always means the item is alone
		{
			catalogI=this.findCatalogBook(mob, physP);
			if(catalogI==null)
			{
				commonTell(mob,L("You need to specify a proper catalog for '@x1'.",physP.name(mob)));
				return false;
			}
		}
		else
		{
			List<String> cmds2=new ArrayList<String>(commands);
			cmds2.remove(0);
			catalogI=this.getTarget(mob, null, givenTarget, cmds2, Wearable.FILTER_UNWORNONLY);
			if(catalogI!=null)
			{
				if(!isPossibleCatalog(mob, catalogI, null, false))
					return false;
				String cat=Titling.getCatalogType(catalogI);
				if((cat.length()>0)&&(!isPossibleCatalog(mob, catalogI, physP, false)))
					return false;
			}
		}
		if(physP==null)
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",itemName));
			return false;
		}
		if(catalogI==null)
		{
			commonTell(mob,L("You don't seem to have a proper catalog."));
			return false;
		}
		
		final Ability writeA=mob.fetchAbility("Skill_Write");
		if(writeA==null)
		{
			commonTell(mob,L("You must know how to write to entitle."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		verb=L("surveying @x1 into @x2",physP.name(),catalogI.name());
		displayText=L("You are @x1",verb);
		this.found=physP;
		this.catalogI=catalogI;
		if((!proficiencyCheck(mob,0,auto))
		||(!writeA.proficiencyCheck(mob,super.getXLEVELLevel(mob)*10,auto)))
			this.catalogI=null;
		final int duration=getDuration(15,mob,1,1);
		final CMMsg msg=CMClass.getMsg(mob,physP,this,getActivityMessageType(),L("<S-NAME> start(s) surveying <T-NAME> into @x1.",catalogI.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
