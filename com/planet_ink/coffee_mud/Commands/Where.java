package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.regex.Pattern;

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

public class Where extends StdCommand
{
	public Where()
	{
	}
	
	private final String[]	access	= I(new String[] { "WHERE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected void whereAdd(DVector V, Area area, int i)
	{
		if(V.contains(area))
			return;

		for(int v=0;v<V.size();v++)
		{
			if(((Integer)V.get(v,2)).intValue()>i)
			{
				V.insertElementAt(v,area,Integer.valueOf(i));
				return;
			}
		}
		V.add(area,Integer.valueOf(i));
	}

	public boolean canShowTo(MOB showTo, MOB show)
	{
		if((show!=null)
		&&(show.session()!=null)
		&&(showTo!=null)
		&&(((show.phyStats().disposition()&PhyStats.IS_CLOAKED)==0)
			||((CMSecurity.isAllowedAnywhere(showTo,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(showTo,CMSecurity.SecFlag.WIZINV))
				&&(showTo.phyStats().level()>=show.phyStats().level()))))
			return true;
		return false;
	}

	public String cataMark(Environmental E)
	{
		if(E==null)
			return "";
		if(CMLib.catalog().isCatalogObj(E))
			return "^g";
		return "";
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		boolean overrideSet = false;
		if((commands.size()>1)&&(commands.get(1).equals("!")))
			overrideSet=commands.remove(commands.get(1));
		int firstColWidth = CMLib.lister().fixColWidth(25,mob);
		if((CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.WHERE))
		&&(!overrideSet))
		{
			final StringBuffer lines=new StringBuffer("^x");
			lines.append(CMStrings.padRight(L("Name"),firstColWidth)+"| ");
			lines.append(CMStrings.padRight(L("Location"),17)+"^.^N\n\r");
			String who=CMParms.combineQuoted(commands,1);
			if(who.length()==0)
			{
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					final MOB mob2=S.mob();
					if(canShowTo(mob,mob2))
					{
						final Room R=mob2.location();
						if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.WHERE))
						{
							lines.append("^!"+CMStrings.padRight(mob2.Name(),firstColWidth)+"^N| ");
							if(R != null )
							{
								lines.append(R.displayText(mob));
								lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
							}
							else
								lines.append("^!(no location)^?");
							lines.append("\n\r");
						}
					}
					else
					{
						lines.append(CMStrings.padRight(L("NAMELESS"),firstColWidth)+"| ");
						lines.append("NOWHERE");
						lines.append("\n\r");
					}
				}
			}
			else
			{
				boolean mobOnly=false;
				boolean itemOnly=false;
				boolean roomOnly=false;
				boolean exitOnly=false;
				boolean zapperMask=false;
				boolean zapperMask2=false;
				boolean areaFlag=false;
				MaskingLibrary.CompiledZMask compiledZapperMask=null;
				if(who.toUpperCase().startsWith("AREA "))
				{
					areaFlag=true;
					who=who.substring(5).trim();
				}
				// no else here, EVAR!!!
				if((who.toUpperCase().startsWith("ROOM "))
				||(who.toUpperCase().startsWith("ROOMS")))
				{
					roomOnly=true;
					who=who.substring(5).trim();
				}

				if((who.toUpperCase().startsWith("EXIT "))
				||(who.toUpperCase().startsWith("EXITS")))
				{
					exitOnly=true;
					who=who.substring(5).trim();
				}
				else
				if((who.toUpperCase().startsWith("ITEM "))
				||(who.toUpperCase().startsWith("ITEMS")))
				{
					itemOnly=true;
					who=who.substring(5).trim();
				}
				else
				if((who.toUpperCase().startsWith("MOB "))
				||(who.toUpperCase().startsWith("MOBS")))
				{
					mobOnly=true;
					who=who.substring(4).trim();
				}
				else
				if(who.toUpperCase().startsWith("MOBMASK ")||who.toUpperCase().startsWith("MOBMASK="))
				{
					mobOnly=true;
					zapperMask=true;
					who=who.substring(8).trim();
					mob.tell(L("^xMask used:^?^.^N @x1\n\r",CMLib.masking().maskDesc(who)));
					compiledZapperMask=CMLib.masking().maskCompile(who);
				}
				else
				if(who.toUpperCase().startsWith("ITEMMASK ")||who.toUpperCase().startsWith("ITEMMASK="))
				{
					itemOnly=true;
					zapperMask=true;
					who=who.substring(9).trim();
					mob.tell(L("^xMask used:^?^.^N @x1\n\r",CMLib.masking().maskDesc(who)));
					compiledZapperMask=CMLib.masking().maskCompile(who);
				}
				else
				if(who.toUpperCase().startsWith("MOBMASK2 ")||who.toUpperCase().startsWith("MOBMASK2="))
				{
					mobOnly=true;
					zapperMask2=true;
					mob.tell(L("^xMask used:^?^.^N @x1\n\r",CMLib.masking().maskDesc(who)));
					who=who.substring(9).trim();
				}
				else
				if(who.toUpperCase().startsWith("ITEMMASK2 ")||who.toUpperCase().startsWith("ITEMMASK2="))
				{
					itemOnly=true;
					zapperMask2=true;
					mob.tell(L("^xMask used:^?^.^N @x1\n\r",CMLib.masking().maskDesc(who)));
					who=who.substring(10).trim();
				}

				MultiEnumeration<Room> r=new MultiEnumeration<Room>((roomOnly||exitOnly)?CMLib.map().rooms():CMLib.map().roomsFilled());
				if(who.toUpperCase().startsWith("AREA ")||areaFlag)
				{
					r=new MultiEnumeration<Room>((roomOnly||exitOnly)?mob.location().getArea().getProperMap():mob.location().getArea().getFilledProperMap());
					areaFlag=true;
				}
				if(who.toUpperCase().startsWith("AREA "))
					who=who.substring(5).trim();
				Room R = null;

				try
				{
					for(;r.hasMoreElements();)
					{
						R=r.nextElement();
						if((R!=null)&&(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.WHERE))&&(CMLib.flags().canAccess(mob,R.getArea())))
						{
							if((!mobOnly)&&(!itemOnly)&&(!exitOnly))
							{
								if((who.length()==0)
								||CMLib.english().containsString(R.displayText(),who)
								||CMLib.english().containsString(R.description(),who))
								{
									lines.append("^!"+CMStrings.padRight("*",firstColWidth)+"^?| ");
									lines.append(R.displayText(mob));
									lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
									lines.append("\n\r");
								}
							}
							if(exitOnly)
							{
								for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
								{
									final Exit E=R.getRawExit(d);
									if((E!=null)
									&&((who.length()==0)
										||((E.Name().length()>0)&&(CMLib.english().containsString(E.Name(),who)))
										||(E.hasADoor() && (E.doorName().length()>0)&& CMLib.english().containsString(E.doorName(),who))
										||(CMLib.english().containsString(E.viewableText(mob,R).toString(),who))))
									{
										lines.append("^!"+CMStrings.padRight(CMLib.directions().getDirectionName(d),firstColWidth)+"^N| ");
										lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
							}
							if((!mobOnly)&&(!roomOnly)&&(!exitOnly))
							{
								for(int i=0;i<R.numItems();i++)
								{
									final Item I=R.getItem(i);
									if((areaFlag)&&(I instanceof BoardableShip))
										r.addEnumeration(((BoardableShip)I).getShipArea().getProperMap());
									if((zapperMask)&&(itemOnly))
									{
										if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
											lines.append(R.displayText(mob));
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((zapperMask2)&&(itemOnly))
									{
										if(CMLib.masking().maskCheck(who,I,true))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
											lines.append(R.displayText(mob));
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									else
									if((who.length()==0)
									||(CMLib.english().containsString(I.name(),who))
									||(CMLib.english().containsString(I.displayText(),who))
									||(CMLib.english().containsString(I.description(),who)))
									{
										lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
										lines.append(R.displayText(mob));
										lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
							}
							for(int m=0;m<R.numInhabitants();m++)
							{
								final MOB M=R.fetchInhabitant(m);
								if((M!=null)&&((M.isMonster())||(canShowTo(mob,M))))
								{
									if((!itemOnly)&&(!roomOnly)&&(!exitOnly))
									{
										if((zapperMask)&&(mobOnly))
										{
											if(CMLib.masking().maskCheck(compiledZapperMask,M,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),firstColWidth)+"^N| ");
												lines.append(R.displayText(mob));
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										else
										if((zapperMask2)&&(mobOnly))
										{
											if(CMLib.masking().maskCheck(who,M,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),firstColWidth)+"^N| ");
												lines.append(R.displayText(mob));
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										else
										if((who.length()==0)
										||(CMLib.english().containsString(M.name(),who))
										||(CMLib.english().containsString(M.displayText(),who))
										||(CMLib.english().containsString(M.description(),who)))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),firstColWidth)+"^N| ");
											lines.append(R.displayText(mob));
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
									if((!mobOnly)&&(!roomOnly)&&(!exitOnly))
									{
										for(int i=0;i<M.numItems();i++)
										{
											final Item I=M.getItem(i);
											if((areaFlag)&&(I instanceof BoardableShip))
												r.addEnumeration(((BoardableShip)I).getShipArea().getProperMap());
											if((zapperMask)&&(itemOnly))
											{
												if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
													lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((zapperMask2)&&(itemOnly))
											{
												if(CMLib.masking().maskCheck(who,I,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
													lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((who.length()==0)
											||(CMLib.english().containsString(I.name(),who))
											||(CMLib.english().containsString(I.displayText(),who))
											||(CMLib.english().containsString(I.description(),who)))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),firstColWidth)+"^N| ");
												lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
										if(SK!=null)
										for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
										{
											final Environmental E=i.next();
											if((areaFlag)&&(E instanceof BoardableShip))
												r.addEnumeration(((BoardableShip)E).getShipArea().getProperMap());
											if((zapperMask)&&(E instanceof Item)&&(itemOnly))
											{
												if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
													lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((zapperMask)&&(E instanceof MOB)&&(mobOnly))
											{
												if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
													lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((zapperMask2)&&(E instanceof Item)&&(itemOnly))
											{
												if(CMLib.masking().maskCheck(who,E,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
													lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((zapperMask2)&&(E instanceof MOB)&&(mobOnly))
											{
												if(CMLib.masking().maskCheck(who,E,true))
												{
													lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
													lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
													lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
													lines.append("\n\r");
												}
											}
											else
											if((who.length()==0)
											||(CMLib.english().containsString(E.name(),who))
											||(CMLib.english().containsString(E.displayText(),who))
											||(CMLib.english().containsString(E.description(),who)))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),firstColWidth)+"^N| ");
												lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
									}
								}
							}
						}
					}
				}
				catch (final NoSuchElementException nse)
				{
				}
			}
			mob.tell(lines.toString()+"^.");
		}
		else
		{
			int alignment=mob.fetchFaction(CMLib.factions().AlignID());
			for(int i=commands.size()-1;i>=0;i--)
			{
				final String s=commands.get(i);
				if(s.equalsIgnoreCase("good"))
				{
					alignment=CMLib.factions().getAlignMedianFacValue(Faction.Align.GOOD);
					commands.remove(i);
				}
				else
				if(s.equalsIgnoreCase("neutral"))
				{
					alignment=CMLib.factions().getAlignMedianFacValue(Faction.Align.NEUTRAL);
					commands.remove(i);
				}
				else
				if(s.equalsIgnoreCase("evil"))
				{
					alignment=CMLib.factions().getAlignMedianFacValue(Faction.Align.EVIL);
					commands.remove(i);
				}
			}

			final int adjust=CMath.s_int(CMParms.combine(commands,1));
			final DVector levelsVec=new DVector(2);
			final DVector mobsVec=new DVector(2);
			final DVector alignVec=new DVector(2);
			final int moblevel=mob.phyStats().level()+adjust;
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if((CMLib.flags().canAccess(mob,A))
				&&(CMLib.flags().canBeLocated(A))
				&&(A.getAreaIStats()!=null))
				{
					int median=A.getPlayerLevel();
					if(median==0)
						median=A.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
					int medianDiff=0;
					final int diffLimit=6;
					if((median<(moblevel+diffLimit))
					&&((median>=(moblevel-diffLimit))))
					{
						if(mob.phyStats().level()>=median)
							medianDiff=(int)Math.round(9.0*CMath.div(median,moblevel));
						else
							medianDiff=(int)Math.round(10.0*CMath.div(moblevel,median));
					}
					whereAdd(levelsVec,A,medianDiff);

					whereAdd(mobsVec,A,A.getAreaIStats()[Area.Stats.POPULATION.ordinal()]);

					final int align=A.getAreaIStats()[Area.Stats.MED_ALIGNMENT.ordinal()];
					final int alignDiff=((int)Math.abs((double)(alignment-align)));
					whereAdd(alignVec,A,alignDiff);
				}
			}
			final StringBuffer msg=new StringBuffer(L("You are currently in: ^H@x1^?\n\r",mob.location().getArea().name()));
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.ROOMVISITS))
			&&(mob.playerStats()!=null))
			{
				msg.append(L("You have explored @x1% of this area and @x2% of the world.\n\r",
						""+mob.playerStats().percentVisited(mob,mob.location().getArea()),
						""+mob.playerStats().percentVisited(mob,null)));
			}
			final DVector scores=new DVector(2);
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if(CMLib.flags().canAccess(mob,A))
				{
					int index=levelsVec.indexOf(A);
					if(index>=0)
					{
						final Integer I=(Integer)levelsVec.get(index,2);
						if((I!=null)&&(I.intValue()!=0))
						{
							int score=(index+1);
							index=mobsVec.indexOf(A);
							if(index>=0)
								score+=(index+1);

							index=alignVec.indexOf(A);
							if(index>=0)
								score+=(index+1);
							whereAdd(scores,A,score);
						}
					}
				}
			}
			msg.append(L("\n\r^HThe best areas for you to try appear to be: ^?\n\r\n\r"));
			msg.append("^x"+CMStrings.padRight(L("Area Name"),35)+CMStrings.padRight(L("Level"),6)+CMStrings.padRight(L("Alignment"),20)+CMStrings.padRight(L("Pop"),10)+"^.^?\n\r");
			final List<Area> finalScoreList = new ArrayList<Area>();
			for(int i=scores.size()-1;((i>=0)&&(i>=(scores.size()-15)));i--)
				finalScoreList.add((Area)scores.get(i,1));
			final int mobLevel=mob.phyStats().level();
			Collections.sort(finalScoreList,new Comparator<Area>()
			{
				@Override
				public int compare(Area o1, Area o2)
				{
					int median1=o1.getPlayerLevel();
					if(median1==0)
						median1=o1.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
					int median2=o2.getPlayerLevel();
					if(median2==0)
						median2=o2.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
					final int lvlDiff1=Math.abs(mobLevel - median1);
					final int lvlDiff2=Math.abs(mobLevel - median2);
					return lvlDiff1==lvlDiff2?0:(lvlDiff1>lvlDiff2)?1:-1;
				}
				
			});
			for(Area A : finalScoreList)
			{
				int lvl=A.getPlayerLevel();
				if(lvl==0)
					lvl=A.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()];
				final int align=A.getAreaIStats()[Area.Stats.MED_ALIGNMENT.ordinal()];

				msg.append(CMStrings.padRight(A.name(),35))
				   .append(CMStrings.padRight(Integer.toString(lvl),6))
				   .append(CMStrings.padRight(CMLib.factions().getRange(CMLib.factions().AlignID(), align).name(),20))
				   .append(CMStrings.padRight(Integer.toString(A.getAreaIStats()[Area.Stats.POPULATION.ordinal()]),10))
				   .append("\n\r");
			}
			msg.append(L("\n\r\n\r^HEnter 'HELP (AREA NAME) for more information.^?"));
			if(!mob.isMonster())
				mob.session().colorOnlyPrintln(msg.toString()+"\n\r");
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
