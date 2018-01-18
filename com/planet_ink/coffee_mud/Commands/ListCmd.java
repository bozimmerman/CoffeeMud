package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ShopPrice;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AbilityAward;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AmountAward;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Award;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.CurrencyAward;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.ExpertiseAward;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.StatAward;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.TitleAward;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournalFlags;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.*;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
public class ListCmd extends StdCommand
{
	public ListCmd()
	{
	}

	private final String[]	access	= I(new String[] { "LIST" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private enum WikiFlag
	{
		NO,
		WIKILIST,
		WIKIHELP
	}
	
	private WikiFlag getWikiFlagRemoved(List<String> commands)
	{
		if(commands == null)
			return WikiFlag.NO;
		for(String s : commands)
		{
			if(s.equalsIgnoreCase("wiki"))
			{
				commands.remove(s);
				return WikiFlag.WIKILIST;
			}
			if(s.equalsIgnoreCase("wikihelp"))
			{
				commands.remove(s);
				return WikiFlag.WIKIHELP;
			}
		}
		return WikiFlag.NO;
	}
	
	private static class WorldFilter implements Filterer<Area>
	{
		private final TimeClock to;
		public WorldFilter(Room R)
		{
			if((R!=null)&&(R.getArea()!=null))
				to=R.getArea().getTimeObj();
			else
				to=CMLib.time().globalClock();
		}

		@Override
		public boolean passesFilter(Area obj)
		{
			return (obj.getTimeObj()==to);
		}
	}

	private static Filterer<Area> planetsAreaFilter=new Filterer<Area>()
	{
		@Override
		public boolean passesFilter(Area obj)
		{
			return (obj instanceof SpaceObject) && (!(obj instanceof SpaceShip));
		}
	};

	private static Filterer<Area> allAreaFilter=new Filterer<Area>()
	{
		@Override
		public boolean passesFilter(Area obj)
		{
			return (!(obj instanceof SpaceShip));
		}
	};

	private static Filterer<Area> mundaneAreaFilter=new Filterer<Area>()
	{
		@Override
		public boolean passesFilter(Area obj)
		{
			return !(obj instanceof SpaceObject);
		}
	};
	
	private static Filterer<Area> spaceShipsAreaFilter=new Filterer<Area>()
	{
		@Override
		public boolean passesFilter(Area obj)
		{
			return (obj instanceof SpaceObject) && (obj instanceof SpaceShip);
		}
	};
	
	public StringBuilder listAllQualifies(Session viewerS, List<String> cmds)
	{
		final StringBuilder str=new StringBuilder("");
		final Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		str.append("<<EACH CLASS>>\n\r");
		Map<String,AbilityMapper.AbilityMapping> subMap=map.get("EACH");
		str.append(CMStrings.padRight(L("Skill ID"), CMLib.lister().fixColWidth(20.0,viewerS)));
		str.append(CMStrings.padRight(L("Lvl"), CMLib.lister().fixColWidth(4.0,viewerS)));
		str.append(CMStrings.padRight(L("Gain"), CMLib.lister().fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight(L("Prof"), CMLib.lister().fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight(L("Mask"), CMLib.lister().fixColWidth(40.0,viewerS)));
		str.append("\n\r");
		for(final AbilityMapper.AbilityMapping mapped : subMap.values())
		{
			str.append(CMStrings.padRight(mapped.abilityID(), CMLib.lister().fixColWidth(20.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.qualLevel(), CMLib.lister().fixColWidth(4.0,viewerS)));
			str.append(CMStrings.padRight(mapped.autoGain()?L("yes"):L("no"), CMLib.lister().fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.defaultProficiency(), CMLib.lister().fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(mapped.extraMask(), CMLib.lister().fixColWidth(40.0,viewerS)));
			str.append("\n\r");
		}
		str.append("\n\r");
		str.append("<<ALL CLASSES>>\n\r");
		subMap=map.get("ALL");
		str.append(CMStrings.padRight(L("Skill ID"), CMLib.lister().fixColWidth(20.0,viewerS)));
		str.append(CMStrings.padRight(L("Lvl"), CMLib.lister().fixColWidth(4.0,viewerS)));
		str.append(CMStrings.padRight(L("Gain"), CMLib.lister().fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight(L("Prof"), CMLib.lister().fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight(L("Mask"), CMLib.lister().fixColWidth(40.0,viewerS)));
		str.append("\n\r");
		for(final AbilityMapper.AbilityMapping mapped : subMap.values())
		{
			str.append(CMStrings.padRight(mapped.abilityID(), CMLib.lister().fixColWidth(20.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.qualLevel(), CMLib.lister().fixColWidth(4.0,viewerS)));
			str.append(CMStrings.padRight(mapped.autoGain()?L("yes"):L("no"), CMLib.lister().fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.defaultProficiency(), CMLib.lister().fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(mapped.extraMask(), CMLib.lister().fixColWidth(40.0,viewerS)));
			str.append("\n\r");
		}
		return str;
	}

	public StringBuilder roomDetails(Session viewerS, Vector<Room> these, Room likeRoom)
	{
		return roomDetails(viewerS, these.elements(), likeRoom);
	}

	public StringBuilder roomDetails(Session viewerS, Enumeration<Room> these, Room likeRoom)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements())
			return lines;
		if(likeRoom==null)
			return lines;
		Room thisThang=null;
		String thisOne=null;
		final int COL_LEN1=CMLib.lister().fixColWidth(31.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(43.0,viewerS);
		for(final Enumeration<Room> r=these;r.hasMoreElements();)
		{
			thisThang=r.nextElement();
			thisOne=thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
				lines.append(CMStrings.padRightPreserve("^N^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",COL_LEN1)+": "+CMStrings.limit(thisThang.displayText(),COL_LEN2)+"^N^.\n\r");
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder roomExpires(Session viewerS, Enumeration<Room> these, Room likeRoom)
	{
		final StringBuilder lines=new StringBuilder("The time is: "+CMLib.time().date2String(System.currentTimeMillis())+"\n\r\n\r");
		if(!these.hasMoreElements())
			return lines;
		if(likeRoom==null)
			return lines;
		Room thisThang=null;
		String thisOne=null;
		for(final Enumeration<Room> r=these;r.hasMoreElements();)
		{
			thisThang=r.nextElement();
			thisOne=thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
			{
				String expires=null;
				if(thisThang.expirationDate()==0)
					expires="*";
				else
					expires=CMLib.time().date2String(thisThang.expirationDate());
				lines.append(CMStrings.padRightPreserve("^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",30)+": "+expires+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder roomPropertyDetails(Session viewerS, Area A, String rest)
	{
		if(rest.trim().length()==0)
			return roomPropertyDetails(viewerS, A.getMetroMap(), null);
		else
		if(rest.trim().equalsIgnoreCase("area"))
			return roomPropertyDetails(viewerS, A.getMetroMap(), null);
		else
		if(rest.trim().equalsIgnoreCase("world"))
			return roomPropertyDetails(viewerS, CMLib.map().rooms(), null);
		else
		if(rest.trim().toLowerCase().startsWith("area "))
			return roomPropertyDetails(viewerS, A.getMetroMap(), rest.trim().substring(5).trim());
		else
		if(rest.trim().toLowerCase().startsWith("world "))
			return roomPropertyDetails(viewerS, CMLib.map().rooms(), rest.trim().substring(6).trim());
		else
			return new StringBuilder("Illegal parameters... try LIST REALESTATE AREA/WORLD (USERNAME/CLANNAME)");
	}

	public StringBuilder roomPropertyDetails(Session viewerS, Enumeration<Room> these, String owner)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements())
			return lines;
		LandTitle t=null;
		Room thisThang=null;
		String thisOne=null;
		for(final Enumeration<Room> r=these;r.hasMoreElements();)
		{
			thisThang=r.nextElement();
			t=CMLib.law().getLandTitle(thisThang);
			if(t!=null)
			{
				thisOne=thisThang.roomID();
				if((thisOne.length()>0)&&((owner==null)||(t.getOwnerName().equalsIgnoreCase(owner))))
					lines.append(CMStrings.padRightPreserve("^N^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",30)+": "+CMStrings.limit(thisThang.displayText(),23)+CMStrings.limit(" ^.^N("+t.getOwnerName()+", $"+t.getPrice()+")",20)+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public String cataMark(Environmental E)
	{
		if(E==null)
			return "";
		if(CMLib.catalog().isCatalogObj(E))
			return "^g";
		return "";
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

	private String getShopPrice(Room R, MOB buyer, ShopKeeper SK, Environmental E)
	{
		MOB seller = null;
		boolean destroySeller = false;
		try
		{
			if(SK instanceof MOB)
				seller=(MOB)SK;
			else
			if((SK instanceof Item)&&(((Item)SK).owner() instanceof MOB))
				seller=(MOB)((Item)SK).owner();
			else
			if((SK instanceof Ability)&&(((Ability)SK).affecting() instanceof MOB))
				seller=(MOB)((Ability)SK).affecting();
			else
			{
				seller=CMClass.getFactoryMOB("the shop", 1, R);
				destroySeller=true;
			}
			ShopPrice p = CMLib.coffeeShops().sellingPrice(seller, buyer, E, SK, SK.getShop(), false);
			if(p!=null)
			{
				if(p.experiencePrice>0)
					return p.experiencePrice+"xp";
				else
				if(p.questPointPrice>0)
					return p.questPointPrice+"qp";
				else
				if(p.absoluteGoldPrice>0)
					return CMLib.beanCounter().abbreviatedPrice(seller, p.absoluteGoldPrice); 
			}
		}
		finally
		{
			if(destroySeller && (seller != null))
				seller.destroy();
		}
		return "?";
	}
	
	public StringBuffer getStuff(MOB mob, List<String> commands, int start, Enumeration<Room> r)
	{
		boolean mobOnly = false;
		boolean itemOnly = false;
		boolean shopOnly = false;
		boolean roomOnly = false;
		boolean exitOnly = false;
		boolean zapperMask = false;
		boolean zapperMask2 = false;
		MaskingLibrary.CompiledZMask compiledZapperMask=null;
		String who="";
		if(commands.size()>start)
			who=commands.get(start).toString().toUpperCase();

		final StringBuffer lines=new StringBuffer("");
		String rest=CMParms.combine(commands,start);
		if((who.equals("ROOM"))
		||(who.equals("ROOMS")))
		{
			roomOnly=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
		}
		else
		if(who.equalsIgnoreCase("RESOURCE")
		||who.equalsIgnoreCase("RESOURCES")
		||who.equalsIgnoreCase("ENVRESOURCES")
		||(who.equalsIgnoreCase("TYPE")
		||who.equalsIgnoreCase("TYPES")))
		{
			return new StringBuffer(roomResources(mob.session(), r, mob.location()).toString());
		}
		else
		if((who.equals("EXIT "))
		||(who.equals("EXITS")))
		{
			exitOnly=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
		}
		else
		if((who.equals("ITEM"))
		||(who.equals("ITEMS")))
		{
			itemOnly=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
		}
		else
		if((who.equals("MOB"))
		||(who.equals("MOBS")))
		{
			mobOnly=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
		}
		else
		if((who.equals("SHOP"))
		||(who.equals("SHOPS")))
		{
			shopOnly=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
		}
		else
		if(who.equals("MOBMASK")
		||who.equals("MOBMASK="))
		{
			mobOnly=true;
			zapperMask=true;
			commands.remove(start);
			lines.append("^xMask used:^?^.^N "+CMLib.masking().maskDesc(who)+"\n\r");
			compiledZapperMask=CMLib.masking().maskCompile(CMParms.combine(commands,start));
			rest="";
		}
		else
		if(who.equals("ITEMMASK")
		||who.equals("ITEMMASK="))
		{
			itemOnly=true;
			zapperMask=true;
			commands.remove(start);
			lines.append("^xMask used:^?^.^N "+CMLib.masking().maskDesc(who)+"\n\r");
			compiledZapperMask=CMLib.masking().maskCompile(CMParms.combine(commands,start));
			rest="";
		}
		else
		if(who.equals("MOBMASK2")
		||who.equals("MOBMASK2="))
		{
			mobOnly=true;
			zapperMask2=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
			lines.append("^xMask used:^?^.^N "+CMLib.masking().maskDesc(rest)+"\n\r");
		}
		else
		if(who.equals("ITEMMASK2")
		||who.equals("ITEMMASK2="))
		{
			itemOnly=true;
			zapperMask2=true;
			commands.remove(start);
			rest=CMParms.combine(commands,start);
			lines.append("^xMask used:^?^.^N "+CMLib.masking().maskDesc(rest)+"\n\r");
		}
		Room R = null;

		try
		{
			for(;r.hasMoreElements();)
			{
				R=r.nextElement();
				if((R!=null)
				&&(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.WHERE))
				&&(CMLib.flags().canAccess(mob,R.getArea())))
				{
					if((!mobOnly)&&(!itemOnly)&&(!exitOnly)&&(!shopOnly))
					{
						if((rest.length()==0)
						||CMLib.english().containsString(R.displayText(),rest)
						||CMLib.english().containsString(R.description(),rest))
						{
							lines.append("^!"+CMStrings.padRight("*",17)+"^?| ");
							lines.append(R.displayText(mob));
							lines.append("^.^N (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
							lines.append("\n\r");
						}
					}
					if(exitOnly)
					{
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							final Exit E=R.getRawExit(d);
							if((E!=null)
							&&((rest.length()==0)
								||((E.Name().length()>0)&&(CMLib.english().containsString(E.Name(),rest)))
								||((E.doorName().length()>0)&& CMLib.english().containsString(E.doorName(),rest))
								||(CMLib.english().containsString(E.viewableText(mob,R).toString(),rest))))
							{
								lines.append("^!"+CMStrings.padRight(CMLib.directions().getDirectionName(d),17)+"^N| ");
								lines.append("^N^. (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
								lines.append("\n\r");
							}
						}
					}
					if(shopOnly)
					{
						final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(R);
						if(SK!=null)
						{
							for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
							{
								final Environmental E=i.next();
								if((rest.length()==0)
								||(CMLib.english().containsString(E.name(),rest))
								||(CMLib.english().containsString(E.displayText(),rest))
								||(CMLib.english().containsString(E.description(),rest)))
								{
									lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
									lines.append("SHOP: "+cataMark(R)+R.name(mob)+"^N");
									lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
									if(shopOnly)
										lines.append("| "+getShopPrice(R,mob,SK,E));
									lines.append("\n\r");
								}
							}
						}
					}
					if((!mobOnly)&&(!roomOnly)&&(!exitOnly)&&(!shopOnly))
					{
						for(int i=0;i<R.numItems();i++)
						{
							final Item I=R.getItem(i);
							if((zapperMask)&&(itemOnly))
							{
								if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
								{
									lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
									lines.append(R.displayText(mob));
									lines.append("^.^N (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
									lines.append("\n\r");
								}
							}
							else
							if((zapperMask2)&&(itemOnly))
							{
								if(CMLib.masking().maskCheck(rest,I,true))
								{
									lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
									lines.append(R.displayText(mob));
									lines.append("^.^N (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
									lines.append("\n\r");
								}
							}
							else
							if((rest.length()==0)
							||(CMLib.english().containsString(I.name(),rest))
							||(CMLib.english().containsString(I.displayText(),rest))
							||(CMLib.english().containsString(I.description(),rest)))
							{
								lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
								lines.append(R.displayText(mob));
								lines.append("^.^N (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
								lines.append("\n\r");
							}
						}
					}
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if((M!=null)&&((M.isMonster())||(canShowTo(mob,M))))
						{
							if((!itemOnly)&&(!roomOnly)&&(!exitOnly)&&(!shopOnly))
							{
								if((zapperMask)&&(mobOnly))
								{
									if(CMLib.masking().maskCheck(compiledZapperMask,M,true))
									{
										lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),17)+"^N| ");
										lines.append(R.displayText(mob));
										lines.append("^.^N (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
								else
								if((zapperMask2)&&(mobOnly))
								{
									if(CMLib.masking().maskCheck(rest,M,true))
									{
										lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),17)+"^N| ");
										lines.append(R.displayText(mob));
										lines.append("^.^N (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
										lines.append("\n\r");
									}
								}
								else
								if((rest.length()==0)
								||(CMLib.english().containsString(M.name(),rest))
								||(CMLib.english().containsString(M.displayText(),rest))
								||(CMLib.english().containsString(M.description(),rest)))
								{
									lines.append("^!"+CMStrings.padRight(cataMark(M)+M.name(mob),17)+"^N| ");
									lines.append(R.displayText(mob));
									lines.append("^.^N (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
									lines.append("\n\r");
								}
							}
							if((!mobOnly)&&(!roomOnly)&&(!exitOnly))
							{
								if(!shopOnly)
								{
									for(int i=0;i<M.numItems();i++)
									{
										final Item I=M.getItem(i);
										if((zapperMask)&&(itemOnly))
										{
											if(CMLib.masking().maskCheck(compiledZapperMask,I,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
												lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										else
										if((zapperMask2)&&(itemOnly))
										{
											if(CMLib.masking().maskCheck(rest,I,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
												lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												lines.append("\n\r");
											}
										}
										else
										if((rest.length()==0)
										||(CMLib.english().containsString(I.name(),rest))
										||(CMLib.english().containsString(I.displayText(),rest))
										||(CMLib.english().containsString(I.description(),rest)))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(I)+I.name(mob),17)+"^N| ");
											lines.append("INV: "+cataMark(M)+M.name(mob)+"^N");
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
											lines.append("\n\r");
										}
									}
								}
								final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(M);
								if(SK!=null)
								{
									for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
									{
										final Environmental E=i.next();
										if((zapperMask)&&(E instanceof Item)&&(itemOnly))
										{
											if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
												lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												if(shopOnly)
													lines.append("| "+getShopPrice(R,mob,SK,E));
												lines.append("\n\r");
											}
										}
										else
										if((zapperMask)&&(E instanceof MOB)&&(mobOnly))
										{
											if(CMLib.masking().maskCheck(compiledZapperMask,E,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
												lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												if(shopOnly)
													lines.append("| "+getShopPrice(R,mob,SK,E));
												lines.append("\n\r");
											}
										}
										else
										if((zapperMask2)&&(E instanceof Item)&&(itemOnly))
										{
											if(CMLib.masking().maskCheck(rest,E,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
												lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												if(shopOnly)
													lines.append("| "+getShopPrice(R,mob,SK,E));
												lines.append("\n\r");
											}
										}
										else
										if((zapperMask2)&&(E instanceof MOB)&&(mobOnly))
										{
											if(CMLib.masking().maskCheck(rest,E,true))
											{
												lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
												lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
												lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
												if(shopOnly)
													lines.append("| "+getShopPrice(R,mob,SK,E));
												lines.append("\n\r");
											}
										}
										else
										if((rest.length()==0)
										||(CMLib.english().containsString(E.name(),rest))
										||(CMLib.english().containsString(E.displayText(),rest))
										||(CMLib.english().containsString(E.description(),rest)))
										{
											lines.append("^!"+CMStrings.padRight(cataMark(E)+E.name(),17)+"^N| ");
											lines.append("SHOP: "+cataMark(M)+M.name(mob)+"^N");
											lines.append(" (^<LSTROOMID^>"+CMLib.map().getDescriptiveExtendedRoomID(R)+"^</LSTROOMID^>)");
											if(shopOnly)
												lines.append("| "+getShopPrice(R,mob,SK,E));
											lines.append("\n\r");
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch(final NoSuchElementException nse)
		{
		}
		return lines;
	}

	public StringBuilder roomTypes(MOB mob, Enumeration<Room> these, Room likeRoom, List<String> commands)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements())
			return lines;
		if(likeRoom==null)
			return lines;
		if(commands.size()==1)
		{
			Room thisThang=null;
			String thisOne=null;
			for(final Enumeration<Room> r=these;r.hasMoreElements();)
			{
				thisThang=r.nextElement();
				thisOne=thisThang.roomID();
				if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
					lines.append(CMStrings.padRightPreserve(thisOne,30)+": "+thisThang.ID()+"\n\r");
			}
			lines.append("\n\r");
		}
		else
		{
			lines.append(getStuff(mob, commands, 1, these));
		}
		return lines;
	}

	public StringBuilder roomResources(Session viewerS, Vector<Room> these, Room likeRoom)
	{
		return roomResources(viewerS, these.elements(),likeRoom);
	}
	
	public StringBuilder roomResources(Session viewerS, Enumeration<Room> these, Room likeRoom)
	{
		final int COL_LEN1=CMLib.lister().fixColWidth(30.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(15.0,viewerS);
		final StringBuilder lines=new StringBuilder(CMStrings.padRight(L("Room ID#"),COL_LEN1)+"| "
										   +CMStrings.padRight(L("Room Type"),COL_LEN2)+"| "
										   +"Resource\n\r");
		if(!these.hasMoreElements())
			return lines;
		if(likeRoom==null)
			return lines;
		Room thisThang=null;
		String thisOne=null;
		for(final Enumeration<Room> r=these;r.hasMoreElements();)
		{
			thisThang=r.nextElement();
			thisOne=thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
			{
				lines.append(CMStrings.padRight(thisOne,COL_LEN1)+": ");
				lines.append(CMStrings.padRight(thisThang.ID(),COL_LEN2)+": ");
				String thisRsc="-";
				if(thisThang.myResource()>=0)
					thisRsc=RawMaterial.CODES.NAME(thisThang.myResource());
				lines.append(thisRsc+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder areaConquests(Session viewerS, Enumeration<Area> these)
	{
		final int COL_LEN1=CMLib.lister().fixColWidth(26.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(40.0,viewerS);
		final StringBuilder lines=new StringBuilder(CMStrings.padRight(L("Area"),COL_LEN1)+"| "
										   +CMStrings.padRight(L("Clan"),COL_LEN2)+"| "
										   +"Controlled\n\r");
		if(!these.hasMoreElements())
			return lines;
		Area thisThang=null;
		String thisOne=null;
		for(final Enumeration<Area> r=these;r.hasMoreElements();)
		{
			thisThang=r.nextElement();
			thisOne=thisThang.name();
			if(thisOne.length()>0)
			{
				lines.append(CMStrings.padRight(thisOne,COL_LEN1)+": ");
				String controller="The Archons";
				String fully="";
				final LegalBehavior law=CMLib.law().getLegalBehavior(thisThang);
				if(law!=null)
				{
					controller=law.rulingOrganization();
					fully=""+((controller.length()>0)&&law.isFullyControlled());
				}
				lines.append(CMStrings.padRight(controller,COL_LEN2)+": ");
				lines.append(fully+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}

	protected void dumpThreadGroup(Session viewerS, StringBuilder lines,ThreadGroup tGroup, boolean ignoreZeroTickThreads, boolean extend)
	{
		final int ac = tGroup.activeCount();
		final int agc = tGroup.activeGroupCount();
		final Thread tArray[] = new Thread [ac+1];
		final ThreadGroup tgArray[] = new ThreadGroup [agc+1];

		tGroup.enumerate(tArray,false);
		tGroup.enumerate(tgArray,false);

		lines.append(" ^HTGRP^?  ^H" + tGroup.getName() + "^?\n\r");
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null)
			{
				if(ignoreZeroTickThreads)
				{
					final java.lang.StackTraceElement[] s=tArray[i].getStackTrace();
					boolean isAlive=tArray[i].isAlive();
					if(isAlive)
					{
						for (final StackTraceElement element : s)
						{
							if(element.getMethodName().equalsIgnoreCase("sleep")
							&&(element.getClassName().equalsIgnoreCase("java.lang.Thread")))
								isAlive=false;
							else
							if(element.getMethodName().equalsIgnoreCase("park")
							&&(element.getClassName().equalsIgnoreCase("sun.misc.Unsafe")))
								isAlive=false;
							else
							if(element.getMethodName().equalsIgnoreCase("wait")
							&&(element.getClassName().equalsIgnoreCase("java.lang.Object")))
								isAlive=false;
							break;
						}
					}
					if(!isAlive)
						continue;
				}
				lines.append(tArray[i].isAlive()? "  ok   " : " BAD!  ");
				lines.append(CMStrings.padRight(tArray[i].getName(),20)+": ");
				final String summary;
				if(tArray[i] instanceof MudHost)
					summary=CMClass.classID(tArray[i])+": "+((MudHost)tArray[i]).getStatus();
				else
				{
					final Runnable R=CMLib.threads().findRunnableByThread(tArray[i]);
					if(R instanceof TickableGroup)
						summary=((TickableGroup)R).getName()+": "+((TickableGroup)R).getStatus();
					else
					if(R instanceof Session)
					{
						final Session S=(Session)R;
						final MOB mob=S.mob();
						final String mobName=(mob==null)?"null":mob.Name();
						summary="session "+mobName+": "+S.getStatus().toString()+": "+CMParms.combineQuoted(S.getPreviousCMD(),0);
					}
					else
					if(R instanceof CMRunnable)
						summary=CMClass.classID(R)+": active for "+((CMRunnable)R).activeTimeMillis()+"ms";
					else
					if(CMClass.classID(R).length()>0)
						summary=CMClass.classID(R);
					else
					if(extend)
						summary=tArray[i].toString();
					else
						summary="";
				}
				lines.append(summary+"\n\r");
			}
		}

		if (agc > 0)
		{
			lines.append("{\n\r");
			for (int i = 0; i<agc; ++i)
			{
				if (tgArray[i] != null)
					dumpThreadGroup(viewerS,lines,tgArray[i],ignoreZeroTickThreads,extend);
			}
			lines.append("}\n\r");
		}
	}

	protected Thread findThread(String name,ThreadGroup tGroup, boolean ignoreZeroTickThreads)
	{
		final int ac = tGroup.activeCount();
		final int agc = tGroup.activeGroupCount();
		final Thread tArray[] = new Thread [ac+1];
		final ThreadGroup tgArray[] = new ThreadGroup [agc+1];

		tGroup.enumerate(tArray,false);
		tGroup.enumerate(tgArray,false);

		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null)
			{
				if((ignoreZeroTickThreads)&&(!tArray[i].isAlive()))
					continue;
				if(tArray[i].getName().equalsIgnoreCase(name))
					return tArray[i];
			}
		}

		if (agc > 0)
		{
			for (int i = 0; i<agc; ++i)
			{
				if (tgArray[i] != null)
				{
					Thread T=findThread(name,tgArray[i],ignoreZeroTickThreads);
					if(T!=null)
						return T;
				}
			}
		}
		return null;
	}

	public StringBuilder listThreads(Session viewerS, MOB mob, boolean ignoreZeroTickThreads, boolean extend)
	{
		final StringBuilder lines=new StringBuilder("^xStatus|Name                 ^.^?\n\r");
		try
		{
			ThreadGroup topTG = Thread.currentThread().getThreadGroup();
			while (topTG != null && topTG.getParent() != null)
				topTG = topTG.getParent();
			if (topTG != null)
				dumpThreadGroup(viewerS,lines,topTG,ignoreZeroTickThreads, extend);
		}
		catch (final Exception e)
		{
			lines.append ("\n\rBastards! Exception while listing threads: " + e.getMessage() + "\n\r");
		}
		return lines;

	}

	public StringBuilder listThread(Session viewerS, MOB mob, String threadname)
	{
		final StringBuilder lines=new StringBuilder("^xStatus|Name                 ^.^?\n\r");
		try
		{
			ThreadGroup topTG = Thread.currentThread().getThreadGroup();
			while (topTG != null && topTG.getParent() != null)
				topTG = topTG.getParent();
			if (topTG != null)
			{
				Thread T=findThread(threadname,topTG,false);
				if(T==null)
					lines.append(L("No thread named @x1 found. Try LIST THREADS.",threadname));
				else
				{
					lines.append("\n\r^HThread: ^N"+T.getName()+"\n\r");
					final java.lang.StackTraceElement[] s=T.getStackTrace();
					for (final StackTraceElement element : s)
						lines.append("\n   "+element.getClassName()+": "+element.getMethodName()+"("+element.getFileName()+": "+element.getLineNumber()+")");
				}
			}
		}
		catch (final Exception e)
		{
			lines.append ("\n\rBastards! Exception while listing threads: " + e.getMessage() + "\n\r");
		}
		return lines;

	}

	public void addScripts(DVector DV, Room R, ShopKeeper SK, MOB M, Item I, PhysicalAgent E)
	{
		if(E==null)
			return;
		for(final Enumeration<Behavior> e=E.behaviors();e.hasMoreElements();)
		{
			final Behavior B=e.nextElement();
			if(B instanceof ScriptingEngine)
			{
				final java.util.List<String> files=B.externalFiles();
				if(files != null)
				{
					for(int f=0;f<files.size();f++)
						DV.add(files.get(f),E,R,M,I,B);
				}
				final String nonFiles=((ScriptingEngine)B).getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
				if(nonFiles.trim().length()>0)
					DV.add("*Custom*"+nonFiles.trim(),E,R,M,I,B);
			}
		}
		for(final Enumeration<ScriptingEngine> e=E.scripts();e.hasMoreElements();)
		{
			final ScriptingEngine SE=e.nextElement();
			final java.util.List<String> files=SE.externalFiles();
			if(files != null)
			{
				for(int f=0;f<files.size();f++)
					DV.add(files.get(f),E,R,M,I,SE);
			}
			final String nonFiles=SE.getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
			if(nonFiles.trim().length()>0)
				DV.add("*Custom*"+nonFiles.trim(),E,R,M,I,SE);
		}
	}

	public void addShopScripts(DVector DV, Room R, MOB M, Item I, Environmental E)
	{
		if(E==null)
			return;
		final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(E);
		if(SK!=null)
		{
			for(final Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
			{
				final Environmental E2=i.next();
				if(E2 instanceof PhysicalAgent)
					addScripts(DV,R,SK,M,I,(PhysicalAgent)E2);
			}
		}
	}

	public StringBuilder listScripts(Session viewerS, MOB mob, List<String> cmds)
	{
		if(cmds.size()==0)
			return new StringBuilder("");
		cmds.remove(0);
		if(cmds.size()==0)
			return new StringBuilder("List what script details? Try LIST SCRIPTS (COUNT/DETAILS/CUSTOM)");
		final String rest=CMParms.combine(cmds,0);
		final DVector scriptTree=new DVector(6);
		Area A=null;
		Room R=null;
		WorldMap.LocatedPair LP=null;
		PhysicalAgent AE=null;
		for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			A=e.nextElement(); 
			if(A==null) 
				continue;
			for(final Enumeration<WorldMap.LocatedPair> ae=CMLib.map().scriptHosts(A);ae.hasMoreElements();)
			{
				LP=ae.nextElement(); 
				if(LP==null) 
					continue;
				AE=LP.obj(); 
				if(AE==null) 
					continue;
				R=LP.room(); 
				if(R==null) 
					R=CMLib.map().getStartRoom(AE);

				if((AE instanceof Area)||(AE instanceof Exit))
				{
					if(R==null)
						R=A.getRandomProperRoom();
					addScripts(scriptTree,R,null,null,null,AE);
					addShopScripts(scriptTree,R,null,null,AE);
				}
				else
				if(AE instanceof Room)
				{
					addScripts(scriptTree,R,null,null,null,AE);
					addShopScripts(scriptTree,R,null,null,AE);
				}
				else
				if(AE instanceof MOB)
				{
					addScripts(scriptTree,R,null,(MOB)AE,null,AE);
					addShopScripts(scriptTree,R,(MOB)AE,null,AE);
				}
				else
				if(AE instanceof Item)
				{
					final ItemPossessor IP=((Item)AE).owner();
					if(IP instanceof MOB)
					{
						addScripts(scriptTree,R,null,(MOB)IP,(Item)AE,AE);
						addShopScripts(scriptTree,R,(MOB)IP,(Item)AE,AE);
					}
					else
					{
						addScripts(scriptTree,R,null,null,(Item)AE,AE);
						addShopScripts(scriptTree,R,null,(Item)AE,AE);
					}
				}
			}
		}

		StringBuilder lines=new StringBuilder("");
		if(rest.equalsIgnoreCase("COUNT"))
		{
			final int COL_LEN1=CMLib.lister().fixColWidth(50.0,viewerS);
			final int COL_LEN2=CMLib.lister().fixColWidth(5.0,viewerS);
			lines=new StringBuilder("^x")
			.append(CMStrings.padRight(L("Script File"),COL_LEN1))
			.append(CMStrings.padRight(L("Usage"),COL_LEN2))
			.append("^.^N\n\r");
			scriptTree.sortBy(1);
			if(scriptTree.size()>0)
			{
				String lastOne=(String)scriptTree.get(0,1);
				if(lastOne.startsWith("*Custom*"))
					lastOne="*Custom*";
				int counter=1;
				for(int d=1;d<scriptTree.size();d++)
				{
					String scriptFilename=(String)scriptTree.get(d,1);
					if(scriptFilename.startsWith("*Custom*"))
						scriptFilename="*Custom*";
					if(lastOne.equalsIgnoreCase(scriptFilename))
						counter++;
					else
					{
						lines.append(CMStrings.padRight(lastOne,COL_LEN1));
						lines.append(CMStrings.padRight(""+counter,COL_LEN2));
						lines.append("^.^N\n\r");
						counter=1;
						lastOne=scriptFilename;
					}
				}
				lines.append(CMStrings.padRight(lastOne,COL_LEN1));
				lines.append(CMStrings.padRight(""+counter,COL_LEN2));
				lines.append("\n\r");
			}
		}
		else
		if(rest.equalsIgnoreCase("DETAILS"))
		{
			final int COL_LEN1=CMLib.lister().fixColWidth(30.0,viewerS);
			final int COL_LEN2=CMLib.lister().fixColWidth(20.0,viewerS);
			final int COL_LEN3=CMLib.lister().fixColWidth(25.0,viewerS);
			lines=new StringBuilder("^x")
			.append(CMStrings.padRight(L("Script File"),COL_LEN1))
			.append(CMStrings.padRight(L("Host"),COL_LEN2))
			.append(CMStrings.padRight(L("Location"),COL_LEN3))
			.append("^.^N\n\r");
			scriptTree.sortBy(1);
			if(scriptTree.size()>0)
			{
				for(int d=0;d<scriptTree.size();d++)
				{
					final String scriptFilename=(String)scriptTree.get(d,1);
					final Environmental host=(Environmental)scriptTree.get(d,2);
					final Room room=(Room)scriptTree.get(d,3);
					lines.append(CMStrings.padRight(scriptFilename,COL_LEN1));
					lines.append(CMStrings.padRight(host.Name(),COL_LEN2));
					lines.append(CMStrings.padRight(CMLib.map().getExtendedRoomID(room),COL_LEN3));
					lines.append("^.^N\n\r");
				}
			}
		}
		else
		if(rest.equalsIgnoreCase("CUSTOM"))
		{
			lines=new StringBuilder("^xCustom Scripts")
									.append("^.^N\n\r");
			scriptTree.sortBy(1);
			if(scriptTree.size()>0)
			{
				for(int d=0;d<scriptTree.size();d++)
				{
					final String scriptFilename=(String)scriptTree.get(d,1);
					if(scriptFilename.startsWith("*Custom*"))
					{
						final Environmental host=(Environmental)scriptTree.get(d,2);
						final Room room=(Room)scriptTree.get(d,3);
						lines.append("^xHost: ^.^N").append(host.Name())
							 .append(", ^xLocation: ^.^N").append(CMLib.map().getExtendedRoomID(room));
						lines.append("^.^N\n\r");
						lines.append(scriptFilename.substring(8));
						lines.append("^.^N\n\r");
					}
				}
			}
		}
		else
			return new StringBuilder("Invalid parameter for LIST SCRIPTS.  Enter LIST SCRIPTS alone for help.");
		return lines;
	}

	public StringBuilder listLinkages(Session viewerS, MOB mob, String rest)
	{
		final StringBuilder buf=new StringBuilder("Links: \n\r");
		final List<List<Area>> areaLinkGroups=new Vector<List<Area>>();
		Vector<String> parms=CMParms.parse(rest.toUpperCase());
		boolean showSubStats = parms.contains("SUBSTATS");
		Enumeration<Area> a;
		if(parms.contains("WORLD"))
			a=CMLib.map().areas();
		else
			a=new XVector<Area>(mob.location().getArea()).elements();
		for(;a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			buf.append("^H"+A.name()+"^N, "+A.numberOfProperIDedRooms()+" rooms, ");
			if(!A.getProperMap().hasMoreElements())
			{
				buf.append("\n\r");
				continue;
			}
			final List<List<Room>> linkedGroups=new Vector<List<Room>>();
			for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(R.roomID().length()>0)
				{
					List<Room> myVec=null;
					List<Room> clearVec=null;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						final Room R2=R.rawDoors()[d];
						if(R2!=null)
						{
							for(int g=0;g<linkedGroups.size();g++)
							{
								final List<Room> G=linkedGroups.get(g);
								if(G.size()==0)
									clearVec=G;
								else
								if(G.contains(R2))
								{
									if(myVec==null)
									{
										myVec=G;
										myVec.add(R);
									}
									else
									if(myVec!=G)
									{
										for(int g2=0;g2<myVec.size();g2++)
											G.add(myVec.get(g2));
										myVec.clear();
										clearVec=myVec;
										myVec=G;
									}
								}
							}
						}
					}
					if(myVec==null)
					{
						if(clearVec!=null)
							clearVec.add(R);
						else
						{
							clearVec=new Vector<Room>();
							clearVec.add(R);
							linkedGroups.add(clearVec);
						}
					}
				}
				for(int g=linkedGroups.size()-1;g>=0;g--)
				{
					if((linkedGroups.get(g)).size()==0)
						linkedGroups.remove(g);
				}
			}
			final StringBuilder ext=new StringBuilder("links ");
			List<Area> myVec=null;
			List<Area> clearVec=null;
			for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(R.roomID().length()>0)
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R2=R.rawDoors()[d];
					if((R2!=null)&&(R2.getArea()!=R.getArea()))
					{
						ext.append("\n\r   "+CMLib.directions().getDirectionName(d)+": "+R2.getArea().name());
						if(showSubStats)
						{
							int[] areaStats = R2.getArea().getAreaIStats();
							ext.append(" (");
							ext.append(L("@x1 mobs, @x2 avg lvl, @x3 med lvl, @x4 avg align",
									""+areaStats[Area.Stats.POPULATION.ordinal()],
									""+areaStats[Area.Stats.AVG_LEVEL.ordinal()],
									""+areaStats[Area.Stats.MED_LEVEL.ordinal()],
									""+areaStats[Area.Stats.AVG_ALIGNMENT.ordinal()]));
							ext.append(") ");
						}
						else
							ext.append(" ("+R.roomID()+"/"+R2.roomID()+") ");
						for(int g=0;g<areaLinkGroups.size();g++)
						{
							final List<Area> G=areaLinkGroups.get(g);
							if(G.size()==0)
								clearVec=G;
							else
							if(G.contains(R2.getArea()))
							{
								if(myVec==null)
								{
									myVec=G;
									myVec.add(R.getArea());
								}
								else
								if(myVec!=G)
								{
									for(int g2=0;g2<myVec.size();g2++)
										G.add(myVec.get(g2));
									myVec.clear();
									clearVec=myVec;
									myVec=G;
								}
							}
						}
					}
				}
			}
			if(myVec==null)
			{
				if(clearVec!=null)
					clearVec.add(A);
				else
				{
					clearVec=new Vector<Area>();
					clearVec.add(A);
					areaLinkGroups.add(clearVec);
				}
			}
			int[] areaStats = A.getAreaIStats();
			if(areaStats[Area.Stats.POPULATION.ordinal()]>0)
			{
				buf.append(L("@x1 mobs, @x2 avg lvl, @x3 med lvl, @x4 avg align",
						""+areaStats[Area.Stats.POPULATION.ordinal()],
						""+areaStats[Area.Stats.AVG_LEVEL.ordinal()],
						""+areaStats[Area.Stats.MED_LEVEL.ordinal()],
						""+areaStats[Area.Stats.AVG_ALIGNMENT.ordinal()]));
			}
			if(linkedGroups.size()>0)
			{
				buf.append(", groups: "+linkedGroups.size()+" sizes: ");
				for(final List<Room> grp : linkedGroups)
					buf.append(grp.size()+" ");
			}
			buf.append("\t"+ext.toString()+"\n\r");
		}
		buf.append(L("There were @x1 area groups:",""+areaLinkGroups.size()));
		for(int g=areaLinkGroups.size()-1;g>=0;g--)
		{
			if(areaLinkGroups.get(g).size()==0)
				areaLinkGroups.remove(g);
		}
		final StringBuilder unlinkedGroups=new StringBuilder("");
		for(final List<Area> V : areaLinkGroups)
		{
			buf.append(V.size()+" ");
			if(V.size()<4)
			{
				for(int v=0;v<V.size();v++)
					unlinkedGroups.append(V.get(0).name()+"\t");
				unlinkedGroups.append("|\t");
			}

		}
		buf.append("\n\r");
		buf.append(L("Small Group Areas:\t@x1",unlinkedGroups.toString()));
		//Log.sysOut("Lister",buf.toString());
		return buf;
	}

	public StringBuilder journalList(final MOB mob, Session viewerS, String partialjournal, String rest)
	{
		final StringBuilder buf=new StringBuilder("");
		String journal=null;
		List<String> flagsV=new ArrayList<String>();
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMJ.NAME()+"S").startsWith(partialjournal.toUpperCase().trim()))
			{
				journal=CMJ.NAME().trim();
				flagsV=CMParms.parseAny(CMJ.getFlag(CommandJournalFlags.ASSIGN), ':', true);
			}
		}
		if(journal==null)
			return buf;
		final String fullJournalName="SYSTEM_"+journal+"S";
		final List<String> toV=new ArrayList<String>();
		if(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.LISTADMIN)
		||(CMSecurity.isAllowed(mob, mob.location(),CMSecurity.SecFlag.JOURNALS))
		||(CMSecurity.isJournalAccessAllowed(mob,journal)))
		{
			if(rest.trim().length()==0)
			{
			}
			else
			{
				List<String> chosen=CMParms.parseCommas(rest.toUpperCase().trim(), true);
				for(String chosen1 : chosen)
				{
					if(CMLib.players().playerExists(CMStrings.capitalizeAndLower(chosen1)))
						toV.add(CMStrings.capitalizeAndLower(chosen1));
					else
					if(flagsV.contains(chosen1))
						toV.add(chosen1);
				}
			}
		}
		else
			toV.add(mob.Name());
		
		final List<JournalEntry> V=CMLib.database().DBReadJournalMsgsByUpdateDate(fullJournalName, true, 100000, toV.toArray(new String[0]));
		final int COL_LEN1=CMLib.lister().fixColWidth(3.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(10.0,viewerS);
		final int COL_LEN3=CMLib.lister().fixColWidth(10.0,viewerS);
		if(V!=null)
		{
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1+2)
					+CMStrings.padRight(L("From"),COL_LEN2)
					+CMStrings.padRight(L("To"),COL_LEN3)
					+" Entry^.^N\n\r");
			buf.append("---------------------------------------------\n\r");
			for(int j=0;j<V.size();j++)
			{
				final JournalEntry entry=V.get(j);
				final String from=entry.from();
				final String message=entry.msg();
				final String to=entry.to();
				buf.append("^x"+CMStrings.padRight((j+1)+"",COL_LEN1)+") "
				+CMStrings.padRight(from,COL_LEN2)
				+CMStrings.padRight(to,COL_LEN2)
				+"^?^. "+message+"\n\r");
			}
		}
		return buf;
	}

	public StringBuilder listReports(Session viewerS, MOB mob)
	{
		mob.tell(L("\n\r^xCoffeeMud System Report:^.^N"));
		try
		{
			System.gc();
			Thread.sleep(1500);
		}
		catch(final Exception e)
		{
		}
		final StringBuilder buf=new StringBuilder("");
		final long totalTime=System.currentTimeMillis()-CMSecurity.getStartTime();
		buf.append(L("The system has been running for ^H@x1^?.\n\r",""+CMLib.english().returnTime(totalTime,0)));
		final long free=Runtime.getRuntime().freeMemory()/1024;
		final long total=Runtime.getRuntime().totalMemory()/1024;
		buf.append(L("The system is utilizing ^H@x1^?kb out of ^H@x2^?kb.\n\r",""+(total-free),""+total));
		buf.append(L("\n\r^xTickables report:^.^N\n\r"));
		final String totalTickers=CMLib.threads().systemReport("totalTickers");
		final String tickGroupSize=CMLib.threads().systemReport("TICKGROUPSIZE");
		final long totalMillis=CMath.s_long(CMLib.threads().systemReport("totalMillis"));
		final long totalTicks=CMath.s_long(CMLib.threads().systemReport("totalTicks"));
		buf.append(L("There are ^H@x1^? ticking objects in ^H@x2^? groups.\n\r",totalTickers,tickGroupSize));
		buf.append(L("The ticking objects have consumed: ^H@x1^?.\n\r",CMLib.english().returnTime(totalMillis,totalTicks)));
		/*
		String topGroupNumber=CMLib.threads().systemReport("topGroupNumber");
		long topGroupMillis=CMath.s_long(CMLib.threads().systemReport("topGroupMillis"));
		long topGroupTicks=CMath.s_long(CMLib.threads().systemReport("topGroupTicks"));
		long topObjectMillis=CMath.s_long(CMLib.threads().systemReport("topObjectMillis"));
		long topObjectTicks=CMath.s_long(CMLib.threads().systemReport("topObjectTicks"));
		buf.append(L("The most active group, #^H@x1^?, has consumed: ^H@x2^?.\n\r",topGroupNumber,CMLib.english().returnTime(topGroupMillis,topGroupTicks)));
		String topObjectClient=CMLib.threads().systemReport("topObjectClient");
		String topObjectGroup=CMLib.threads().systemReport("topObjectGroup");
		if(topObjectClient.length()>0)
		{
			buf.append(L("The most active object has been '^H@x1^?', from group #^H@x2^?.\n\r",topObjectClient,topObjectGroup));
			buf.append(L("That object has consumed: ^H@x1^?.\n\r",CMLib.english().returnTime(topObjectMillis,topObjectTicks)));
		}
		*/
		buf.append("\n\r");
		buf.append(L("^xServices report:^.^N\n\r"));
		buf.append(L("There are ^H@x1^? active out of ^H@x2^? live worker threads.\n\r",CMLib.threads().systemReport("numactivethreads"),CMLib.threads().systemReport("numthreads")));
		int threadNum=0;
		String threadName=CMLib.threads().systemReport("Thread"+threadNum+"name");
		while(threadName.trim().length()>0)
		{
			final long saveThreadMilliTotal=CMath.s_long(CMLib.threads().systemReport("Thread"+threadNum+"MilliTotal"));
			final long saveThreadTickTotal=CMath.s_long(CMLib.threads().systemReport("Thread"+threadNum+"TickTotal"));
			buf.append("Service '"+threadName+"' has consumed: ^H"+CMLib.english().returnTime(saveThreadMilliTotal,saveThreadTickTotal)+" ("+CMLib.threads().systemReport("Thread"+threadNum+"Status")+")^?.");
			buf.append("\n\r");
			threadNum++;
			threadName=CMLib.threads().systemReport("Thread"+threadNum+"name");
		}
		buf.append("\n\r");
		buf.append(L("^xSession report:^.^N\n\r"));
		final long totalMOBMillis=CMath.s_long(CMLib.threads().systemReport("totalMOBMillis"));
		final long totalMOBTicks=CMath.s_long(CMLib.threads().systemReport("totalMOBTicks"));
		buf.append(L("There are ^H@x1^? ticking players logged on.\n\r",""+CMLib.sessions().getCountLocalOnline()));
		buf.append(L("The ticking players have consumed: ^H@x1^?.\n\r",""+CMLib.english().returnTime(totalMOBMillis,totalMOBTicks)));
		/*
		long topMOBMillis=CMath.s_long(CMLib.threads().systemReport("topMOBMillis"));
		long topMOBTicks=CMath.s_long(CMLib.threads().systemReport("topMOBTicks"));
		String topMOBClient=CMLib.threads().systemReport("topMOBClient");
		if(topMOBClient.length()>0)
		{
			buf.append(L("The most active mob has been '^H@x1^?'\n\r",topMOBClient));
			buf.append(L("That mob has consumed: ^H@x1^?.\n\r",CMLib.english().returnTime(topMOBMillis,topMOBTicks)));
		}
		*/
		return buf;
	}

	public void listUsers(Session viewerS, MOB mob, List<String> commands)
	{
		if(commands.size()==0)
			return;
		commands.remove(0);
		int sortBy=-1;
		if(commands.size()>0)
		{
			final String rest=CMParms.combine(commands,0).toUpperCase();
			sortBy = CMLib.players().getCharThinSortCode(rest,true);
			if(sortBy<0)
			{
				mob.tell(L("Unrecognized sort criteria: @x1",rest));
				return;
			}
		}
		final int COL_LEN1=CMLib.lister().fixColWidth(8.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(10.0,viewerS);
		final int COL_LEN3=CMLib.lister().fixColWidth(4.0,viewerS);
		final int COL_LEN4=CMLib.lister().fixColWidth(5.0,viewerS);
		final int COL_LEN5=CMLib.lister().fixColWidth(23.0,viewerS);
		final int COL_LEN6=CMLib.lister().fixColWidth(18.0,viewerS);
		final int COL_LEN7=CMLib.lister().fixColWidth(15.0,viewerS);
		final StringBuilder head=new StringBuilder("");
		head.append("[");
		head.append(CMStrings.padRight(L("Race"),COL_LEN1)+" ");
		head.append(CMStrings.padRight(L("Class"),COL_LEN2)+" ");
		head.append(CMStrings.padRight(L("Lvl"),COL_LEN3)+" ");
		head.append(CMStrings.padRight(L("Hours"),COL_LEN4)+" ");
		switch(sortBy)
		{
		case 6: head.append(CMStrings.padRight(L("E-Mail"),COL_LEN5)+" "); break;
		case 7: head.append(CMStrings.padRight(L("IP Address"),COL_LEN5)+" "); break;
		default: head.append(CMStrings.padRight(L("Last"),COL_LEN6)+" "); break;
		}

		head.append("] Character name\n\r");
		java.util.List<PlayerLibrary.ThinPlayer> allUsers=CMLib.database().getExtendedUserList();
		final java.util.List<PlayerLibrary.ThinPlayer> oldSet=allUsers;
		final int showBy=sortBy;
		final PlayerLibrary lib=CMLib.players();
		while((oldSet.size()>0)&&(sortBy>=0)&&(sortBy<=7))
		{
			if(oldSet==allUsers)
				allUsers=new Vector<PlayerLibrary.ThinPlayer>();
			if((sortBy<3)||(sortBy>4))
			{
				PlayerLibrary.ThinPlayer selected=oldSet.get(0);
				for(int u=1;u<oldSet.size();u++)
				{
					final PlayerLibrary.ThinPlayer U=oldSet.get(u);
					if(lib.getThinSortValue(selected,sortBy).compareTo(lib.getThinSortValue(U,sortBy))>0)
						selected=U;
				}
				if(selected!=null)
				{
					oldSet.remove(selected);
					allUsers.add(selected);
				}
			}
			else
			{
				PlayerLibrary.ThinPlayer selected=oldSet.get(0);
				for(int u=1;u<oldSet.size();u++)
				{
					final PlayerLibrary.ThinPlayer U=oldSet.get(u);
					if(CMath.s_long(lib.getThinSortValue(selected,sortBy))>CMath.s_long(lib.getThinSortValue(U,sortBy)))
						selected=U;
				}
				if(selected!=null)
				{
					oldSet.remove(selected);
					allUsers.add(selected);
				}
			}
		}

		for(int u=0;u<allUsers.size();u++)
		{
			final PlayerLibrary.ThinPlayer U=allUsers.get(u);

			head.append("[");
			head.append(CMStrings.padRight(U.race(),COL_LEN1)+" ");
			head.append(CMStrings.padRight(U.charClass(),COL_LEN2)+" ");
			head.append(CMStrings.padRight(""+U.level(),COL_LEN3)+" ");
			final long age=Math.round(CMath.div(CMath.s_long(""+U.age()),60.0));
			head.append(CMStrings.padRight(""+age,COL_LEN4)+" ");
			switch(showBy)
			{
			case 6:
				head.append(CMStrings.padRight(U.email(), COL_LEN5) + " ");
				break;
			case 7:
				head.append(CMStrings.padRight(U.ip(), COL_LEN5) + " ");
				break;
			default:
				head.append(CMStrings.padRight(CMLib.time().date2String(U.last()), COL_LEN6) + " ");
				break;
			}
			head.append("] "+CMStrings.padRight("^<LSTUSER^>"+U.name()+"^</LSTUSER^>",COL_LEN7));
			head.append("\n\r");
		}
		mob.tell(head.toString());
	}

	public void listAccounts(Session viewerS, MOB mob, List<String> commands)
	{
		if(commands.size()==0)
			return;
		commands.remove(0);
		int sortBy=-1;
		if(commands.size()>0)
		{
			final String rest=CMParms.combine(commands,0).toUpperCase();
			sortBy = CMLib.players().getCharThinSortCode(rest,true);
			if(sortBy<0)
			{
				mob.tell(L("Unrecognized sort criteria: @x1",rest));
				return;
			}
		}
		final int COL_LEN1=CMLib.lister().fixColWidth(10.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(18.0,viewerS);
		final int COL_LEN3=CMLib.lister().fixColWidth(23.0,viewerS);
		final StringBuilder head=new StringBuilder("");
		head.append("^X");
		head.append("[");
		head.append(CMStrings.padRight(L("Account"),COL_LEN1)+" ");
		head.append(CMStrings.padRight(L("Last"),COL_LEN2)+" ");
		switch(sortBy)
		{
		default:
			head.append(CMStrings.padRight(L("E-Mail"), COL_LEN3) + " ");
			break;
		case 7:
			head.append(CMStrings.padRight(L("IP Address"), COL_LEN3) + " ");
			break;
		}

		head.append("] Characters^.^N\n\r");
		List<PlayerAccount> allAccounts=CMLib.database().DBListAccounts(null);
		final List<PlayerAccount> oldSet=allAccounts;
		final Hashtable<String, PlayerLibrary.ThinPlayer> thinAcctHash=new Hashtable<String, PlayerLibrary.ThinPlayer>();
		for(final PlayerAccount acct : allAccounts)
		{
			PlayerLibrary.ThinPlayer selectedU=new PlayerLibrary.ThinPlayer()
			{
				@Override
				public String name()
				{
					return acct.getAccountName();
				}

				@Override
				public String charClass()
				{
					return "";
				}

				@Override
				public String race()
				{
					return "";
				}

				@Override
				public int level()
				{
					return 0;
				}

				@Override
				public int age()
				{
					return 0;
				}

				@Override
				public long last()
				{
					return acct.getLastDateTime();
				}

				@Override
				public String email()
				{
					return acct.getEmail();
				}

				@Override
				public String ip()
				{
					return acct.getLastIP();
				}

				@Override
				public int exp()
				{
					return 0;
				}

				@Override
				public int expLvl()
				{
					return 0;
				}
				
			};
			thinAcctHash.put(acct.getAccountName(), selectedU);
		}
		final int showBy=sortBy;
		final PlayerLibrary lib=CMLib.players();
		while((oldSet.size()>0)&&(sortBy>=0)&&(sortBy<=7))
		{
			if(oldSet==allAccounts)
				allAccounts=new Vector<PlayerAccount>();
			if((sortBy<3)||(sortBy>4))
			{
				PlayerAccount selected = oldSet.get(0);
				if(selected != null)
				{
					PlayerLibrary.ThinPlayer selectedU=thinAcctHash.get(selected.getAccountName());
					for(int u=1;u<oldSet.size();u++)
					{
						final PlayerAccount acct = oldSet.get(u);
						final PlayerLibrary.ThinPlayer U=thinAcctHash.get(acct.getAccountName());
						if(lib.getThinSortValue(selectedU,sortBy).compareTo(lib.getThinSortValue(U,sortBy))>0)
						{
							selected=acct;
							selectedU=U;
						}
					}
					oldSet.remove(selected);
					allAccounts.add(selected);
				}
			}
			else
			{
				PlayerAccount selected = oldSet.get(0);
				if(selected!=null)
				{
					PlayerLibrary.ThinPlayer selectedU=thinAcctHash.get(selected.getAccountName());
					for(int u=1;u<oldSet.size();u++)
					{
						final PlayerAccount acct = oldSet.get(u);
						final PlayerLibrary.ThinPlayer U=thinAcctHash.get(acct.getAccountName());
						if(CMath.s_long(lib.getThinSortValue(selectedU,sortBy))>CMath.s_long(lib.getThinSortValue(U,sortBy)))
						{
							selected=acct;
							selectedU=U;
						}
					}
					oldSet.remove(selected);
					allAccounts.add(selected);
				}
			}
		}

		for(int u=0;u<allAccounts.size();u++)
		{
			final PlayerAccount U=allAccounts.get(u);
			final StringBuilder line=new StringBuilder("");
			line.append("[");
			line.append(CMStrings.padRight(U.getAccountName(),COL_LEN1)+" ");
			line.append(CMStrings.padRight(CMLib.time().date2String(U.getLastDateTime()),COL_LEN2)+" ");
			String players = CMParms.toListString(U.getPlayers());
			final Vector<String> pListsV = new Vector<String>();
			while(players.length()>0)
			{
				int x=players.length();
				if(players.length()>20)
				{
					x=players.lastIndexOf(',',20);
					if(x<0)
						x=24;
				}
				pListsV.add(players.substring(0,x));
				players=players.substring(x).trim();
				if(players.startsWith(","))
					players=players.substring(1).trim();
			}
			switch(showBy)
			{
			default:
				line.append(CMStrings.padRight(U.getEmail(), COL_LEN3) + " ");
				break;
			case 7:
				line.append(CMStrings.padRight(U.getLastIP(), COL_LEN3) + " ");
				break;
			}
			line.append("] ");
			final int len = line.length();
			head.append(line.toString());
			boolean notYet = true;
			for(final String s : pListsV)
			{
				if(notYet)
					notYet=false;
				else
					head.append(CMStrings.repeat(' ', len));
				head.append(s);
				head.append("\n\r");
			}
			if(pListsV.size()==0)
				head.append("\n\r");
		}
		mob.tell(head.toString());
	}

	public StringBuilder listRaces(Session viewerS, Enumeration<Race> these, String rest)
	{
		final List<String> parms=CMParms.parse(rest.toUpperCase());
		final boolean shortList=parms.contains("SHORT");
		if(shortList)
			parms.remove("SHORT");
		WikiFlag wiki = getWikiFlagRemoved(parms);
		final String restRest=CMParms.combine(parms).trim();
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements())
			return lines;
		int column=0;
		final int COL_LEN=CMLib.lister().fixColWidth(25.0,viewerS);
		if(shortList)
		{
			final Vector<String> raceNames=new Vector<String>();
			for(final Enumeration<Race> e=these;e.hasMoreElements();)
			{
				final Race R=e.nextElement();
				if((restRest.length()==0)
				||(CMLib.english().containsString(R.ID(), restRest))
				||(CMLib.english().containsString(R.name(), restRest))
				||(CMLib.english().containsString(R.racialCategory(), restRest)))
					raceNames.add(R.ID());
			}
			lines.append(CMParms.toListString(raceNames));
		}
		else
		for(final Enumeration<Race> e=these;e.hasMoreElements();)
		{
			final Race R=e.nextElement();
			if((restRest.length()==0)
			||(CMLib.english().containsString(R.ID(), restRest))
			||(CMLib.english().containsString(R.name(), restRest))
			||(CMLib.english().containsString(R.racialCategory(), restRest)))
			{
				if(wiki == WikiFlag.WIKILIST)
					lines.append("*[["+R.name()+"|"+R.name()+"]]\n\r");
				else
				if(wiki == WikiFlag.WIKIHELP)
				{
					String statAdj=R.getStatAdjDesc();
					if(R.getTrainAdjDesc().length()>0)
						statAdj+=((statAdj.length()>0)?", ":"")+R.getTrainAdjDesc();
					if(R.getPracAdjDesc().length()>0)
						statAdj+=((statAdj.length()>0)?", ":"")+R.getPracAdjDesc();
					String ableStr=R.getSensesChgDesc();
					String langStr="";
					String rableStr="";
					String cultStr="";
					String effStr="";
					if(R.getDispositionChgDesc().length()>0)
						ableStr+=((ableStr.length()>0)?", ":"")+R.getDispositionChgDesc();
					if(R.getAbilitiesDesc().length()>0)
					{
						for(Ability A : R.racialAbilities(null))
						{
							if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
								langStr+=("[["+A.ID()+"|"+A.name()+"]] ");
							else
								rableStr+=("[["+A.ID()+"|"+A.name()+"]] ");
						}
						for(Ability A : R.racialEffects(null))
						{
							if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
								langStr+=("[["+A.ID()+"|"+A.name()+"]] ");
							else
								effStr+=("[["+A.ID()+"|"+A.name()+"]] ");
						}
						final QuadVector<String,Integer,Integer,Boolean> cables=R.culturalAbilities();
						if(cables != null)
						{
							for(int c=0;c<cables.size();c++)
							{
								final Ability A=CMClass.getAbility(cables.getFirst(c));
								if(A!=null)
								{
									if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
										langStr+=("[["+A.ID()+"|"+A.name()+"]] ");
									else
										cultStr+=("[["+A.ID()+"|"+A.name()+"]] ");
								}
							}
						}
					}
					String immunoStr="";
					for(String ableID : R.abilityImmunities())
					{
						final Ability A=CMClass.getAbilityPrototype(ableID);
						if(A!=null)
							immunoStr+=((immunoStr.length()>0)?", ":"")+A.name();
					}
					StringBuilder help=CMLib.help().getHelpText(R.ID(),null,false,true);
					if(help==null)
						help=CMLib.help().getHelpText(R.name(),null,false,true);
					if((help!=null)&&(help.toString().startsWith("<RACE>")))
						help=new StringBuilder(help.toString().substring(6));
					else
						help=new StringBuilder("");
					String eqStr="";
					if(R.outfit(null)!=null)
					{
						for(final Item I : R.outfit(null))
						{
							if(I!=null)
								eqStr+=((eqStr.length()>0)?", ":"")+I.Name();
						}
					}
					String qualClassesStr="";
					if(CMLib.login().isAvailableRace(R))
					{
						for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
						{
							final CharClass C=c.nextElement();
							if(((CMProps.isTheme(C.availabilityCode())))
							&&(C.isAllowedRace(R)))
							{
								qualClassesStr+="[["+C.name()+"("+C.baseClass()+")|"+C.name()+"]] ";
							}
						}
					}
					int maxAge=R.getAgingChart()[Race.AGE_ANCIENT];
					String lifeExpStr;
					if(maxAge>Integer.MAX_VALUE/2)
						lifeExpStr=L("Forever");
					else
						lifeExpStr=L("@x1 years",""+maxAge);
					String wearLocs="";
					for(long wearLoc : Wearable.CODES.ALL())
					{
						if((wearLoc!=0)&&((R.forbiddenWornBits()&wearLoc)==0))
							wearLocs += " " + Wearable.CODES.NAME(wearLoc);
					}
					final String helpEOL=CMStrings.getEOL(help.toString(),"\n\r");
					lines.append("{{RaceTemplate"
							+ "|Name="+R.name()
							+ "|Description="+CMStrings.replaceAll(help.toString(),helpEOL,helpEOL+helpEOL)
							+ "|Statadj="+statAdj
							+ "|RacialAbilities="+rableStr
							+ "|CulturalAbilities="+cultStr
							+ "|RacialEffects="+effStr
							+ "|BonusAbilities="+ableStr
							+ "|BonusLanguages="+langStr
							+ "|Immunities="+immunoStr
							+ "|LifeExpectancy="+lifeExpStr
							+ "|Startingequipment="+eqStr
							+ "|WearLocs="+wearLocs
							+ "|ExpMod="+R.getXPAdjustment()+"%"
							+ "|Qualifyingclasses="+qualClassesStr
							+ "}}\n\r");
				}
				else
				{
					if(++column>3)
					{
						lines.append("\n\r");
						column=1;
					}
					lines.append(CMStrings.padRight(R.ID()
								+(R.isGeneric()?"*":"")
								+" ("+R.racialCategory()+")",COL_LEN));
				}
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder listCharClasses(Session viewerS, Enumeration<CharClass> these, List<String> commands)
	{
		boolean shortList=false;
		WikiFlag wiki=this.getWikiFlagRemoved(commands);
		for(String c : commands)
		{
			if(c.equalsIgnoreCase("SHORT"))
				shortList=true;
		}
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements())
			return lines;
		int column=0;
		final int COL_LEN=CMLib.lister().fixColWidth(25.0,viewerS);
		if(shortList)
		{
			final Vector<String> classNames=new Vector<String>();
			for(final Enumeration<CharClass> e=these;e.hasMoreElements();)
				classNames.add(e.nextElement().ID());
			lines.append(CMParms.toListString(classNames));
		}
		else
		for(final Enumeration<CharClass> e=these;e.hasMoreElements();)
		{
			final CharClass C=e.nextElement();
			if(wiki==WikiFlag.WIKILIST)
			{
				lines.append("[["+C.name()+"("+C.baseClass()+")|"+C.name()+"]]\n\r");
			}
			else
			if(wiki==WikiFlag.WIKIHELP)
			{
				final Set<Integer> types=new STreeSet<Integer>(new Integer[]{
					Integer.valueOf(Ability.ACODE_CHANT),
					Integer.valueOf(Ability.ACODE_COMMON_SKILL),
					Integer.valueOf(Ability.ACODE_PRAYER),
					Integer.valueOf(Ability.ACODE_SKILL),
					Integer.valueOf(Ability.ACODE_SONG),
					Integer.valueOf(Ability.ACODE_SPELL),
					Integer.valueOf(Ability.ACODE_THIEF_SKILL),
					Integer.valueOf(Ability.ACODE_TECH),
					Integer.valueOf(Ability.ACODE_SUPERPOWER)
				});
				StringBuilder help=CMLib.help().getHelpText(C.ID(),null,false,true);
				if(help==null)
					help=CMLib.help().getHelpText(C.name(),null,false,true);
				if((help!=null)&&(help.toString().startsWith("<CHARCLASS>")))
					help=new StringBuilder(help.toString().substring(11));
				else
					help=new StringBuilder("");
				final List<Item> items=C.outfit(null);
				StringBuilder outfit=new StringBuilder("");
				if(items !=null)
				{
					for(final Item I : items)
					{
						if(I!=null)
							outfit.append(((outfit.length()>0)?", ":"")+I.name());
					}
				}
				StringBuilder raceStr=new StringBuilder("");
				for(Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
				{
					final Race R=r.nextElement();
					if(CMLib.login().isAvailableRace(R)
					&&(C.isAllowedRace(R)))
						raceStr.append("[["+R.name()+"|"+R.name()+"]] ");
				}
				StringBuilder langStr=new StringBuilder("");
				for(Enumeration<Ability> a=CMClass.abilities();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
					&&(CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID())>=0)
					&&(CMLib.ableMapper().getSecretSkill(C.ID(),false,A.ID())))
						langStr.append("[["+A.ID()+"|"+A.name()+"]] ");
				}
				final String helpEOL=CMStrings.getEOL(help.toString(),"\n\r");
				lines.append("{{ClassTemplate"
						+ "|Name="+C.name()
						+ "|Description="+CMStrings.replaceAll(help.toString(),helpEOL,helpEOL+helpEOL)
						+ "|PrimeStat="+C.getPrimeStatDesc()
						+ "|Qualifications="+C.getStatQualDesc()
						+ "|Practices="+C.getPracticeDesc()
						+ "|Trains="+C.getTrainDesc()
						+ "|Hitpoints="+C.getHitPointDesc()
						+ "|Mana="+C.getManaDesc()
						+ "|Movement="+C.getMovementDesc()
						+ "|Attack="+C.getAttackDesc()
						+ "|Damage="+C.getDamageDesc()
						+ "|MaxStat="+C.getMaxStatDesc()
						+ "|Bonuses="+C.getOtherBonusDesc()
						+ "|Weapons="+C.getWeaponLimitDesc()
						+ "|Armor="+C.getArmorLimitDesc()
						+ "|Limitations="+C.getOtherLimitsDesc()
						+ "|StartingEq="+outfit.toString()
						+ "|Races="+raceStr.toString()
						+ "|Languages="+langStr.toString());
				for(int l=1;l<=CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);l++)
				{
					final StringBuilder levelList=new StringBuilder("");
					final List<Ability> autoGains=new ArrayList<Ability>(3);
					final List<Ability> qualifies=new ArrayList<Ability>(3);
					for (final Enumeration<AbilityMapper.AbilityMapping> a = CMLib.ableMapper().getClassAbles(C.ID(),true); a.hasMoreElements(); )
					{
						final AbilityMapper.AbilityMapping cimable=a.nextElement();
						if((cimable.qualLevel() ==l)&&(!cimable.isSecret()))
						{
							final Ability A=CMClass.getAbility(cimable.abilityID());
							if((A!=null)
							&&(types.contains(Integer.valueOf(A.classificationCode()&Ability.ALL_ACODES))))
							{
								if(cimable.autoGain())
									autoGains.add(A);
								else
									qualifies.add(A);
							}
						}
					}
					for(Ability A : autoGains)
						levelList.append("[["+A.ID()+"|"+A.name()+"]]&nbsp;&nbsp;&nbsp;&nbsp;");
					for(Ability A : qualifies)
						levelList.append("([["+A.ID()+"|"+A.name()+"]])&nbsp;&nbsp;&nbsp;&nbsp;");
					
					lines.append("|level"+l+"="+levelList.toString());
				}
				lines.append("}}");
			}
			else
			{
				if(++column>2)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(CMStrings.padRight(C.ID()
							+(C.isGeneric()?"*":"")
							+" ("+C.baseClass()+")",COL_LEN));
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder listRaceCats(Session viewerS, Enumeration<Race> these, List<String> commands)
	{
		final StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements())
			return lines;
		boolean shortList=false;
		for(String c : commands)
		{
			if(c.equalsIgnoreCase("SHORT"))
				shortList=true;
		}
		WikiFlag wiki=this.getWikiFlagRemoved(commands);
		int column=0;
		final Vector<String> raceCats=new Vector<String>();
		Race R=null;
		final int COL_LEN=CMLib.lister().fixColWidth(25.0,viewerS);
		for(final Enumeration<Race> e=these;e.hasMoreElements();)
		{
			R=e.nextElement();
			if(!raceCats.contains(R.racialCategory()))
				raceCats.add(R.racialCategory());
		}
		final Object[] sortedB=(new TreeSet<String>(raceCats)).toArray();
		if(shortList)
		{
			final String[] sortedC=new String[sortedB.length];
			for(int i=0;i<sortedB.length;i++)
				sortedC[i]=(String)sortedB[i];
			lines.append(CMParms.toListString(sortedC));
		} 
		else
		if((wiki==WikiFlag.WIKILIST)||(wiki==WikiFlag.WIKIHELP))
		{
			for (final Object element : sortedB)
			{
				final String raceCat=(String)element;
				lines.append("*[["+raceCat+"(RacialCategory)|"+raceCat+"]]\n\r");
			}
		}
		else
		{
			for (final Object element : sortedB)
			{
				final String raceCat=(String)element;
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(CMStrings.padRight(raceCat,COL_LEN));
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder listQuests(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("");
		if(CMLib.quests().numQuests()==0)
			buf.append(L("No quests loaded."));
		else
		{
			buf.append("\n\r^xQuest Report:^.^N\n\r");
			final int COL_LEN1=CMLib.lister().fixColWidth(5.0,viewerS);
			final int COL_LEN2=CMLib.lister().fixColWidth(30.0,viewerS);
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1)+CMStrings.padRight(L("Name"),COL_LEN2)+" Status^.^N\n\r");
			for(int i=0;i<CMLib.quests().numQuests();i++)
			{
				final Quest Q=CMLib.quests().fetchQuest(i);
				if(Q!=null)
				{
					buf.append(CMStrings.padRight(""+(i+1),COL_LEN1)+CMStrings.padRight("^<LSTQUEST^>"+Q.name()+"^</LSTQUEST^>",COL_LEN2)+" ");
					if(Q.running())
					{
						String minsLeft="("+Q.minsRemaining()+" mins left)";
						if(Q.duration()==0)
							minsLeft="(Eternal)";
						if(Q.isCopy())
							buf.append(L("copy running @x1",minsLeft));
						else
							buf.append("running "+minsLeft);
					}
					else
					if(Q.suspended())
						buf.append("disabled");
					else
					if(Q.waiting())
					{
						long min=Q.waitRemaining();
						if(min>0)
						{
							min=min*CMProps.getTickMillis();
							if(min>60000)
								buf.append(L("waiting (@x1 minutes left)",""+(min/60000)));
							else
								buf.append(L("waiting (@x1 seconds left)",""+(min/1000)));
						}
						else
							buf.append(L("waiting (@x1 minutes left)",""+min));
					}
					else
						buf.append("loaded");
					buf.append("^N\n\r");
				}
			}
		}
		return buf;
	}

	public StringBuilder listQuestNames(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("");
		if(CMLib.quests().numQuests()==0)
			buf.append(L("No quests loaded."));
		else
		{
			buf.append("\n\r^xQuest Names Report:^.^N\n\r");
			final int COL_LEN1=CMLib.lister().fixColWidth(5.0,viewerS);
			final int COL_LEN2=CMLib.lister().fixColWidth(30.0,viewerS);
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1)+CMStrings.padRight(L("Name"),COL_LEN2)+" Display Name^.^N\n\r");
			for(int i=0;i<CMLib.quests().numQuests();i++)
			{
				final Quest Q=CMLib.quests().fetchQuest(i);
				if(Q!=null)
				{
					buf.append(CMStrings.padRight(""+(i+1),COL_LEN1)+CMStrings.padRight("^<LSTQUEST^>"+Q.name()+"^</LSTQUEST^>",COL_LEN2)+" ");
					buf.append(Q.displayName());
					buf.append("^N\n\r");
				}
			}
		}
		return buf;
	}

	public StringBuilder listQuestWinners(Session viewerS, String rest)
	{
		final StringBuilder buf=new StringBuilder("");
		if(CMLib.quests().numQuests()==0)
			buf.append(L("No quests loaded."));
		else
		{
			buf.append("\n\r^xQuest Winners Report:^.^N\n\r");
			final int COL_LEN1=CMLib.lister().fixColWidth(5.0,viewerS);
			final int COL_LEN2=CMLib.lister().fixColWidth(30.0,viewerS);
			final int COL_LEN3=CMLib.lister().fixColWidth(40.0,viewerS);
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1)+CMStrings.padRight(L("Name"),COL_LEN2)+" Winners"+CMStrings.padRight(" ",COL_LEN3-7)+"^.^N\n\r");
			for(int i=0;i<CMLib.quests().numQuests();i++)
			{
				final Quest Q=CMLib.quests().fetchQuest(i);
				if((Q!=null)
				&&((rest==null)
					||(rest.trim().length()==0)
					||(CMLib.english().containsString(Q.name(), rest))
					||(CMLib.english().containsString(Q.displayName(), rest))))
				{
					buf.append(CMStrings.padRight(""+(i+1),COL_LEN1)+CMStrings.padRight("^<LSTQUEST^>"+Q.name()+"^</LSTQUEST^>",COL_LEN2)+" ");
					Map<String,Long> winners = Q.getWinners();
					Iterator<String> ni=winners.keySet().iterator();
					if(ni.hasNext())
					{
						String name = ni.next();
						String time = CMLib.time().date2String(Q.whenLastWon(name).longValue());
						buf.append(CMStrings.padRight(name+" @ "+time, COL_LEN3));
						for(;ni.hasNext();)
						{
							name = ni.next();
							time = CMLib.time().date2String(Q.whenLastWon(name).longValue());
							buf.append("^N\n\r");
							buf.append(CMStrings.padRight(" ",COL_LEN1)+CMStrings.padRight(" ",COL_LEN2)+" ");
							buf.append(CMStrings.padRight(name+" @ "+time, COL_LEN3));
						}
					}
					buf.append("^N\n\r");
				}
			}
		}
		return buf;
	}

	public StringBuilder listJournals(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("");
		final List<String> journals=CMLib.database().DBReadJournals();

		if(journals.size()==0)
			buf.append(L("No journals exits."));
		else
		{
			final int COL_LEN1=CMLib.lister().fixColWidth(5.0,viewerS);
			final int COL_LEN2=CMLib.lister().fixColWidth(30.0,viewerS);
			buf.append("\n\r^xJournals List:^.^N\n\r");
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1)+CMStrings.padRight(L("Name"),COL_LEN2)+" Messages^.^N\n\r");
			for(int i=0;i<journals.size();i++)
			{
				final String journal=journals.get(i);
				final int messages=CMLib.database().DBCountJournal(journal,null,null);
				buf.append(CMStrings.padRight(""+(i+1),COL_LEN1)+CMStrings.padRight(journal,COL_LEN2)+" "+messages);
				buf.append("^N\n\r");
			}
		}
		return buf;
	}

	public StringBuilder listCommandJournals(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("");
		Enumeration<JournalsLibrary.CommandJournal> enumJ=CMLib.journals().commandJournals();
		final List<JournalsLibrary.CommandJournal> journals=new XVector<JournalsLibrary.CommandJournal>(enumJ);

		if(journals.size()==0)
			buf.append(L("No command journals exits."));
		else
		{
			final int COL_LEN1=CMLib.lister().fixColWidth(5.0,viewerS);
			final int COL_LEN2=CMLib.lister().fixColWidth(30.0,viewerS);
			buf.append("\n\r^xCommand Journals List:^.^N\n\r");
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1)+CMStrings.padRight(L("Name"),COL_LEN2)+" Messages^.^N\n\r");
			for(int i=0;i<journals.size();i++)
			{
				final JournalsLibrary.CommandJournal journal=journals.get(i);
				final int messages=CMLib.database().DBCountJournal(journal.JOURNAL_NAME(),null,null);
				buf.append(CMStrings.padRight(""+(i+1),COL_LEN1)+CMStrings.padRight(journal.NAME(),COL_LEN2)+" "+messages);
				buf.append("^N\n\r");
			}
		}
		return buf;
	}

	public StringBuilder listTicks(Session viewerS, String whichGroupStr)
	{
		final StringBuilder msg=new StringBuilder("\n\r");
		boolean activeOnly=false;
		String mask=null;
		Set<Pair<Integer,Integer>> whichTicks=null;
		Set<Integer> whichGroups=null;
		final int x=whichGroupStr.lastIndexOf(' ');
		String finalCol="tickercodeword";
		String finalColName="Status";
		if(x>0)
		{
			String lastWord=whichGroupStr.substring(x+1).trim().toLowerCase();
			final String[] validCols={"tickername","tickerid","tickerstatus","tickerstatusstr","tickercodeword","tickertickdown","tickerretickdown","tickermillitotal","tickermilliavg","tickerlaststartmillis","tickerlaststopmillis","tickerlaststartdate","tickerlaststopdate","tickerlastduration","tickersuspended"};
			final int y=CMParms.indexOf(validCols,lastWord);
			if(y>=0)
				finalCol=lastWord;
			else
			{
				for(final String w : validCols)
				{
					if(w.endsWith(lastWord))
					{
						lastWord=w;
						finalCol=lastWord;
					}
				}
			}
			if(!finalCol.equals(lastWord))
				return new StringBuilder("Invalid column: '"+lastWord+"'.  Valid cols are: "+CMParms.toListString(validCols));
			else
			{
				whichGroupStr=whichGroupStr.substring(0,x).trim();
				finalColName=finalCol;
				if(finalColName.startsWith("ticker"))
					finalColName=finalColName.substring(6);
				if(finalColName.startsWith("milli"))
					finalColName="ms"+finalColName.substring(5);
				finalColName=CMStrings.limit(CMStrings.capitalizeAndLower(finalColName),5);
			}
		}

		if("ACTIVE".startsWith(whichGroupStr.toUpperCase())&&(whichGroupStr.length()>0))
			activeOnly=true;
		else
		if("PROBLEMS".startsWith(whichGroupStr.toUpperCase())&&(whichGroupStr.length()>0))
		{
			whichTicks=new HashSet<Pair<Integer,Integer>>();
			final String problemSets=CMLib.threads().systemReport("tickerProblems");
			final List<String> sets=CMParms.parseSemicolons(problemSets, true);
			for(final String set : sets)
			{
				final List<String> pair=CMParms.parseCommas(set, true);
				if(pair.size()==2)
					whichTicks.add(new Pair<Integer,Integer>(Integer.valueOf(CMath.s_int(pair.get(0))), Integer.valueOf(CMath.s_int(pair.get(1)))));
			}
		}
		else
		if(CMath.isInteger(whichGroupStr)&&(whichGroupStr.length()>0))
		{
			whichGroups=new HashSet<Integer>();
			whichGroups.add(Integer.valueOf(CMath.s_int(whichGroupStr)));
		}
		else
		if(whichGroupStr.length()>0)
		{
			mask=whichGroupStr.toUpperCase().trim();
		}
		final int COL_LEN1=CMLib.lister().fixColWidth(4.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(20.0,viewerS);
		final int COL_LEN3=CMLib.lister().fixColWidth(3.0,viewerS);
		final int COL_LEN4=CMLib.lister().fixColWidth(8.0,viewerS);
		if(!activeOnly)
			msg.append(CMStrings.padRight(L("Grp"),COL_LEN1)+CMStrings.padRight(L("Client"),COL_LEN2)+" "+CMStrings.padRight(L("ID"),COL_LEN3)+CMStrings.padRight(finalColName,COL_LEN4));
		msg.append(CMStrings.padRight(L("Grp"),COL_LEN1)+CMStrings.padRight(L("Client"),COL_LEN2)+" "+CMStrings.padRight(L("ID"),COL_LEN3)+CMStrings.padRight(finalColName,COL_LEN4)+"\n\r");
		int col=0;
		final int numGroups=CMath.s_int(CMLib.threads().tickInfo("tickGroupSize"));
		if((mask!=null)&&(mask.length()==0))
			mask=null;
		String chunk=null;
		for(int group=0;group<numGroups;group++)
		{
			if((whichGroups==null)||(whichGroups.contains(Integer.valueOf(group))))
			{
				final int tickersSize=CMath.s_int(CMLib.threads().tickInfo("tickersSize"+group));
				for(int tick=0;tick<tickersSize;tick++)
				{
					if((whichTicks==null)||(whichTicks.contains(new Pair<Integer,Integer>(Integer.valueOf(group), Integer.valueOf(tick)))))
					{
						final long tickerlaststartdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststartmillis"+group+"-"+tick));
						final long tickerlaststopdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststopmillis"+group+"-"+tick));
						final boolean isActive=(tickerlaststopdate<tickerlaststartdate);
						if((!activeOnly)||(isActive))
						{
							final String name=CMLib.threads().tickInfo("tickerName"+group+"-"+tick);
							if((mask==null)||(name.toUpperCase().indexOf(mask)>=0))
							{
								final String id=CMLib.threads().tickInfo("tickerID"+group+"-"+tick);
								final String status=CMLib.threads().tickInfo(finalCol+group+"-"+tick);
								final boolean suspended=CMath.s_bool(CMLib.threads().tickInfo("tickerSuspended"+group+"-"+tick));
								if(((col++)>=2)||(activeOnly))
								{
									msg.append("\n\r");
									col=1;
								}
								chunk=CMStrings.padRight(""+group,COL_LEN1)
								   +CMStrings.padRight(name,COL_LEN2)
								   +" "+CMStrings.padRight(id+"",COL_LEN3)
								   +CMStrings.padRight((activeOnly?(status+(suspended?"*":"")):status+(suspended?"*":"")),COL_LEN4);
								msg.append(chunk);
							}
						}
					}
				}
			}
		}
		return msg;
	}

	public StringBuilder listSubOps(Session viewerS)
	{
		final StringBuilder msg=new StringBuilder("");
		final int COL_LEN=CMLib.lister().fixColWidth(25.0,viewerS);
		for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			msg.append(CMStrings.padRight(A.Name(),COL_LEN)+": ");
			if(A.getSubOpList().length()==0)
				msg.append("\n\r");
			else
				msg.append(A.getSubOpList()+"\n\r");
		}
		return msg;
	}

	protected String reallyFindOneWays(Session viewerS, List<String> commands)
	{
		final StringBuilder str=new StringBuilder("");
		try
		{
			for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(R.roomID().length()>0)
				{
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						final Room R2=R.rawDoors()[d];
						if((R2!=null)&&(R2.rawDoors()[Directions.getOpDirectionCode(d)]!=R))
							str.append(L("@x1: @x2 to @x3\n\r",CMStrings.padRight(R.roomID(),30),CMLib.directions().getDirectionName(d),R2.roomID()));
					}
				}
			}
		}
		catch (final NoSuchElementException e)
		{
		}
		if(str.length()==0)
			str.append(L("None!"));
		if(CMParms.combine(commands,1).equalsIgnoreCase("log"))
			Log.rawSysOut(str.toString());
		return str.toString();
	}

	protected String unlinkedExits(Session viewerS, List<String> commands)
	{
		final StringBuilder str=new StringBuilder("");
		try
		{
			for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R2=R.rawDoors()[d];
					final Exit E2=R.getRawExit(d);
					if((R2==null)&&(E2!=null))
						str.append(L("@x1: @x2 to @x3 (@x4)\n\r",CMStrings.padRight(R.roomID(),30),CMLib.directions().getDirectionName(d),E2.temporaryDoorLink(),E2.displayText()));
				}
			}
		}
		catch (final NoSuchElementException e)
		{
		}
		if(str.length()==0)
			str.append(L("None!"));
		if(CMParms.combine(commands,1).equalsIgnoreCase("log"))
			Log.rawSysOut(str.toString());
		return str.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String listResources(MOB mob, String parm)
	{
		final Iterator<String> keyIter=Resources.findResourceKeys(parm);
		if(!keyIter.hasNext())
			return "";
		final String key = keyIter.next();
		if(!keyIter.hasNext())
		{
			final StringBuilder str=new StringBuilder("^x"+key+"^?\n\r");
			final Object o=Resources.getResource(key);
			if(o instanceof List)
				str.append(CMParms.toListString((List)o));
			else
			if(o instanceof Map)
				str.append(CMParms.toKeyValueSlashListString((Map)o));
			else
			if(o instanceof Set)
				str.append(CMParms.toListString((Set)o));
			else
			if(o instanceof String[])
				str.append(CMParms.toListString((String[])o));
			else
			if(o instanceof boolean[])
				str.append(CMParms.toListString((boolean[])o));
			if(o instanceof byte[])
				str.append(CMParms.toListString((byte[])o));
			else
			if(o instanceof char[])
				str.append(CMParms.toListString((char[])o));
			else
			if(o instanceof double[])
				str.append(CMParms.toListString((double[])o));
			else
			if(o instanceof int[])
				str.append(CMParms.toListString((int[])o));
			else
			if(o instanceof long[])
				str.append(CMParms.toListString((long[])o));
			else
			if(o!=null)
				str.append(o.toString());
			return str.toString();
		}
		final Enumeration<String> keys=new IteratorEnumeration<String>(Resources.findResourceKeys(parm));
		return CMLib.lister().reallyList2Cols(mob,keys).toString();
	}

	public String listHelpFileRequests(MOB mob, String rest)
	{
		final String fileName=Log.instance().getLogFilename(Log.Type.help);
		if(fileName==null)
			return "This feature requires that help request log entries be directed to a file.";
		final CMFile f=new CMFile(fileName,mob,CMFile.FLAG_LOGERRORS);
		if((!f.exists())||(!f.canRead()))
			return "File '"+f.getName()+"' does not exist.";
		final List<String> V=Resources.getFileLineVector(f.text());
		final Hashtable<String,int[]> entries = new Hashtable<String,int[]>();
		for(int v=0;v<V.size();v++)
		{
			final String s=V.get(v);
			if(s.indexOf(" help  Help")>0)
			{
				final int x=s.indexOf("wanted help on",19);
				final String helpEntry=s.substring(x+14).trim().toLowerCase();
				int[] sightings=entries.get(helpEntry);
				if(sightings==null)
				{
					sightings=new int[1];
					if(CMLib.help().getHelpText(helpEntry,mob,false)!=null)
						sightings[0]=-1;
					entries.put(helpEntry,sightings);
				}
				if(sightings[0]>=0)
					sightings[0]++;
				else
					sightings[0]--;
			}
		}
		final Hashtable<String,Integer> readyEntries = new Hashtable<String,Integer>(entries.size());
		for(final Enumeration<String> e=entries.keys();e.hasMoreElements();)
		{
			final String key=e.nextElement();
			final int[] val=entries.get(key);
			readyEntries.put(key,Integer.valueOf(val[0]));
		}
		final DVector sightingsDV=DVector.toNVector(readyEntries);
		sightingsDV.sortBy(2);
		final StringBuilder str=new StringBuilder("^HHelp entries, sorted by popularity: ^N\n\r");
		for(int d=0;d<sightingsDV.size();d++)
		{
			str.append("^w"+CMStrings.padRight(sightingsDV.get(d,2).toString(),4))
			   .append(" ")
			   .append(sightingsDV.get(d,1).toString())
			   .append("\n\r");
		}
		return str.toString()+"^N";
	}

	public String listRecipes(MOB mob, String rest)
	{
		final StringBuilder str = new StringBuilder("");
		if(rest.trim().length()==0)
		{
			str.append(L("Common Skills with editable recipes: "));
			for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				final Ability A=e.nextElement();
				if(A instanceof CraftorAbility)
				{
					final CraftorAbility iA = (CraftorAbility)A;
					if((iA.parametersFormat()==null)
					||(iA.parametersFormat().length()==0)
					||(iA.parametersFile()==null)
					||(iA.parametersFile().length()==0))
						continue;
					str.append(A.ID()).append(", ");
				}
			}
			if(str.toString().endsWith(", "))
				str.delete(str.length()-2,str.length());
		}
		else
		{
			final Ability A=CMClass.findAbility(rest,Ability.ACODE_COMMON_SKILL,-1,false);
			if(A==null)
				str.append(L("Ability '@x1' does not exist -- try list recipes",rest));
			else
			if(!(A instanceof CraftorAbility))
				str.append(L("Ability '@x1' is not a proper ability -- try list recipes",A.ID()));
			else
			{
				final CraftorAbility iA = (CraftorAbility)A;
				if((iA.parametersFormat()==null)
				||(iA.parametersFormat().length()==0)
				||(iA.parametersFile()==null)
				||(iA.parametersFile().length()==0))
					str.append(L("Ability '@x1' is not editable -- try list recipes",A.ID()));
				else
					str.append(CMLib.ableParms().getRecipeList(iA));
			}
		}
		return str.toString();
	}

	public String listMaterials()
	{
		return CMParms.toListString(RawMaterial.Material.values());
	}
	
	private enum SpaceFilterCode {SPACE, BODIES, MOONS, STARS, SHIPS}
	
	public String getSpaceObjectType(final SpaceObject obj)
	{
		final String type;
		if((obj instanceof Physical) && (!(obj instanceof SpaceShip)) && CMLib.flags().isLightSource(((Physical)obj)))
			type="Star";
		else
		if(obj instanceof SpaceShip)
			type="Ship";
		else
		if((obj instanceof Area) && (!(obj instanceof SpaceShip)) && (obj.radius() > (SpaceObject.Distance.MoonRadius.dm+10000L)))
			type="Planet";
		else
		if((obj instanceof Area) && (!(obj instanceof SpaceShip)) && (obj.radius() <= (SpaceObject.Distance.MoonRadius.dm+10000L)))
			type="Moon";
		else
			type="Obj.";
		return type;
	}
	
	private String shortenNumber(long number, int len)
	{
		String s=""+number;
		char c='d';
		while(s.length()>len)
		{
			number=Math.round(number/1000);
			switch(c)
			{
			case 'd':
				c = 'k';
				break;
			case 'k':
				c = 'm';
				break;
			case 'm':
				c = 'b';
				break;
			case 'b':
				c = 't';
				break;
			case 't':
				c = 'q';
				break;
			}
			s=""+number+Character.toString(c);
		}
		return s;
	}
	
	public String listSpace(MOB mob, List<String> commands)
	{
		final Session viewerS=mob.session();
		if(viewerS==null)
			return "";
		StringBuilder str=new StringBuilder("");
		String listWhat=commands.get(0).toString().toUpperCase().trim();
		Filterer<SpaceObject> filter=null;
		for(final SpaceFilterCode code : SpaceFilterCode.values())
		{
			if(code.toString().toUpperCase().startsWith(listWhat))
			{
				filter=new Filterer<SpaceObject>()
				{
					@Override
					public boolean passesFilter(SpaceObject obj)
					{
						switch(code)
						{
						case SPACE:
							return true;
						case BODIES:
							return (obj instanceof Area) 
								&& (!(obj instanceof SpaceShip)) 
								&& (obj.radius() > (SpaceObject.Distance.MoonRadius.dm + 10000L));
						case SHIPS:
							return (obj instanceof SpaceShip);
						case STARS:
							return (obj instanceof Physical) 
								&& (!(obj instanceof SpaceShip)) 
								&& CMLib.flags().isOnFire((Physical) obj);
						case MOONS:
							return (obj instanceof Area) 
								&& (!(obj instanceof SpaceShip)) 
								&& (obj.radius() <= (SpaceObject.Distance.MoonRadius.dm + 10000L));
						}
						return false;
					}
				};
				break;
			}
		}
		
		if((commands.size()<=1)
		||(filter==null)
		||commands.get(1).toString().equals("?")
		||commands.get(1).toString().equals(L("help")))
		{
			str.append(L("List what in space? Try one of the following:\n\r"));
			str.append(L("LIST SPACE ALL - List everything in space everywhere!!\n\r"));
			str.append(L("LIST SPACE WITHIN [DISTANCE] - List within distance of current planet.\n\r"));
			str.append(L("LIST SPACE AROUND [X],[Y],[Z] - List all within 1 solar system of coords.\n\r"));
			str.append(L("LIST SPACE AROUND [NAME] - List all within 1 solar system of named object.\n\r"));
			str.append(L("LIST SPACE AROUND [NAME] WITHIN [DISTANCE] - List all within [distance] of named object.\n\r"));
			str.append(L("\n\r[DISTANCE] can be in DM (decameters), KM (kilometers), AU (astro units), or SU (solar system units.\n\r"));
			str.append(L("Instead of LIST SPACE you can also specify BODIES, MOONS, STARS, or SPACESHIPS.\n\r"));
			return str.toString();
		}
		final List<SpaceObject> objs=new ArrayList<SpaceObject>();
		for(Enumeration<SpaceObject> objEnum=CMLib.map().getSpaceObjects();objEnum.hasMoreElements();)
		{
			final SpaceObject obj=objEnum.nextElement();
			if(!filter.passesFilter(obj))
				continue;
			objs.add(obj);
		}
		final String[] keywords=CMLib.lang().sessionTranslation(new String[]{"ALL","WITHIN","DISTANCE"});
		final String[] sortcols=CMLib.lang().sessionTranslation(new String[]{"TYPE","RADIUS","COORDINATES","SPEED","MASS","NAME","COORDSX","COORDSY","COORDSZ"});
		Long withinDistance=null;
		long[] centerPoint=null;
		final SpaceObject SO=CMLib.map().getSpaceObject(mob, false);
		for(int i=1;i<commands.size();i++)
		{
			String s=commands.get(i).toUpperCase();
			if(L("ALL").startsWith(s))
				continue;
			else
			if(L("AROUND").startsWith(s))
			{
				if(i<commands.size()-1)
				{
					i++;
					int end=i;
					while((end<commands.size()-1)&&(!CMStrings.contains(keywords, commands.get(end).toString().toUpperCase())))
						end++;
					if(end==i)
					{
						return L("\n\rBad AROUND parm: '@x1' -- no coordinates or object specified.\n\r","");
					}
					else
					{
						String around=CMParms.combine(commands,i,end);
						List<String> listStr=CMParms.parseCommas(around,true);
						long[] coords=null;
						if(listStr.size()==3)
						{
							long[] valL=new long[3];
							for(int x=0;x<3;x++)
							{
								Long newValue=CMLib.english().parseSpaceDistance(listStr.get(x));
								if(newValue==null)
									break;
								else
								{
									valL[i]=newValue.longValue();
									if(i==2)
										coords=valL;
								}
							}
						}
						if(coords==null)
						{
							SpaceObject SO2=CMLib.map().findSpaceObject(around, true);
							if(SO2==null)
								SO2=CMLib.map().findSpaceObject(around, true);
							if(SO2!=null)
								coords=SO2.coordinates();
						}
						if(coords==null)
						{
							return L("\n\rBad AROUND parm: '@x1' -- bad coordinates or object specified.\n\r",around);
						}
						centerPoint=coords;
						i=end-1;
					}
				}
				else
				{
					return L("\n\rBad AROUND parm: '@x1' -- no coordinates or object specified.\n\r","");
				}
			}
			else
			if(L("WITHIN").startsWith(s))
			{
				if(i<commands.size()-1)
				{
					i++;
					int end=i;
					while((end<commands.size()-1)&&(!CMStrings.contains(keywords, commands.get(end).toString().toUpperCase())))
						end++;
					if(end==i)
					{
						return L("\n\rBad WITHIN parm: '@x1' -- no valid distance specified.\n\r","");
					}
					else
					{
						String within=CMParms.combine(commands,i,end);
						Long distance=CMLib.english().parseSpaceDistance(within);
						if(distance==null)
						{
							return L("\n\rBad WITHIN parm: '@x1' -- no valid distance specified.\n\r",within);
						}
						withinDistance=distance;
						i=end-1;
					}
				}
				else
				{
					return L("\n\rBad WITHIN parm: '@x1' -- no distance specified.\n\r","");
				}
			}
			else
			if(L("ORDERBY").startsWith(s))
			{
				if(i<commands.size()-1)
				{
					i++;
					s=commands.get(i);
					int end=i;
					while((end<commands.size()-1)&&(CMStrings.contains(sortcols, commands.get(end).toString().toUpperCase())))
						end++;
					if(end==i)
					{
						return L("\n\rBad ORDERBY parm: '@x1' ORDERBY -- no column specified.  Try @x2.\n\r",commands.get(i).toString(),CMParms.toListString(sortcols));
					}
					for(int x=end-1;x>=i;x--)
					{
						final int[][] b=new int[][]{{0,1,2}};
						int dex=CMParms.indexOf(sortcols, commands.get(x).toString().toUpperCase());
						switch(dex)
						{
						case 0:
							Collections.sort(objs, new Comparator<SpaceObject>()
							{
								@Override
								public int compare(SpaceObject o1, SpaceObject o2)
								{
									return getSpaceObjectType(o1).compareTo(getSpaceObjectType(o2));
								}
							});
							break;
						case 1:
							Collections.sort(objs, new Comparator<SpaceObject>()
							{
								@Override
								public int compare(SpaceObject o1, SpaceObject o2)
								{
									return Long.valueOf(o1 == null ? 0 : o1.radius()).compareTo(Long.valueOf(o2 == null ? 0 : o2.radius()));
								}
							});
							break;
						case 3:
							Collections.sort(objs, new Comparator<SpaceObject>()
							{
								@Override
								public int compare(SpaceObject o1, SpaceObject o2)
								{
									return Double.valueOf(o1 == null ? 0 : o1.speed()).compareTo(Double.valueOf(o2 == null ? 0 : o2.speed()));
								}
							});
							break;
						case 4:
							Collections.sort(objs, new Comparator<SpaceObject>()
							{
								@Override
								public int compare(SpaceObject o1, SpaceObject o2)
								{
									return Long.valueOf(o1 == null ? 0 : o1.getMass()).compareTo(Long.valueOf(o2 == null ? 0 : o2.getMass()));
								}
							});
							break;
						case 5:
							Collections.sort(objs, new Comparator<SpaceObject>()
							{
								@Override
								public int compare(SpaceObject o1, SpaceObject o2)
								{
									return (o1==null?"":o1.name()).compareToIgnoreCase(o2==null?"":o2.name());
								}
							});
							break;
						case 8:
							b[0] = new int[] { 2, 0, 1 };
							//$FALL-THROUGH$
						case 7:
							if (x == 7)
								b[0] = new int[] { 1, 2, 0 };
							//$FALL-THROUGH$
						case 6: 
							//$FALL-THROUGH$
						case 2:
							Collections.sort(objs, new Comparator<SpaceObject>()
							{
								@Override
								public int compare(SpaceObject o1, SpaceObject o2)
								{
									int i = Long.valueOf(o1 == null ? Long.MIN_VALUE : o1.coordinates()[b[0][0]])
											.compareTo(Long.valueOf(o2 == null ? Long.MIN_VALUE : o2.coordinates()[b[0][0]]));
									if (i != 0)
									{
										i = Long.valueOf(o1 == null ? Long.MIN_VALUE : o1.coordinates()[b[0][1]])
											.compareTo(Long.valueOf(o2 == null ? Long.MIN_VALUE : o2.coordinates()[b[0][1]]));
									}
									if (i != 0)
									{
										i = Long.valueOf(o1 == null ? Long.MIN_VALUE : o1.coordinates()[b[0][2]])
											.compareTo(Long.valueOf(o2 == null ? Long.MIN_VALUE : o2.coordinates()[b[0][2]]));
									}
									return i;
								}
							});
							break;
						}
					}
					i=end-1;
				}
				else
				{
					return L("\n\rBad ORDERBY parm: '@x1' ORDERBY -- no column specified.  Try @x2.\n\r","",CMParms.toListString(sortcols));
				}
			}
		}
		
		if((centerPoint!=null)||(withinDistance!=null))
		{
			if(centerPoint==null)
			{
				if(SO!=null)
					centerPoint=SO.coordinates();
				else
					centerPoint=new long[]{0,0,0};
			}
			if(withinDistance==null)
				withinDistance=Long.valueOf(SpaceObject.Distance.SolarSystemRadius.dm+1000000);
			final List<SpaceObject> objs2=CMLib.map().getSpaceObjectsByCenterpointWithin(centerPoint, 0, withinDistance.longValue());
			for(final Iterator<SpaceObject> i=objs.iterator();i.hasNext();)
			{
				final SpaceObject obj=i.next();
				if(!objs2.contains(obj))
					i.remove();
			}
		}
		
		final int COL_LEN1, COL_LEN2, COL_LEN3, COL_LEN4, COL_LEN5;
		str.append(CMStrings.padRight(L("Typ"),COL_LEN1=CMLib.lister().fixColWidth(3.0,viewerS))+" ");
		str.append(CMStrings.padRight(L("Radius"),COL_LEN2=CMLib.lister().fixColWidth(7.0,viewerS))+" ");
		str.append(CMStrings.padRight(L("Coordinates"),COL_LEN3=CMLib.lister().fixColWidth(25.0,viewerS))+" ");
		str.append(CMStrings.padRight(L("Speed"),COL_LEN4=CMLib.lister().fixColWidth(10.0,viewerS))+" ");
		str.append(CMStrings.padRight(L("Mass"),COL_LEN5=CMLib.lister().fixColWidth(7.0,viewerS))+" ");
		str.append(L("Name\n\r"));
		for(SpaceObject obj : objs)
		{
			str.append(CMStrings.padRight(getSpaceObjectType(obj),COL_LEN1)+" ");
			str.append(CMStrings.padRight(CMLib.english().sizeDescShort(obj.radius()),COL_LEN2)+" ");
			str.append(CMStrings.padRight(CMLib.english().coordDescShort(obj.coordinates()),COL_LEN3)+" ");
			str.append(CMStrings.padRight(CMLib.english().speedDescShort(obj.speed()),COL_LEN4)+" ");
			str.append(CMStrings.padRight(shortenNumber(obj.getMass(),COL_LEN5),COL_LEN5)+" ");
			str.append(obj.name()+"\n\r");
		}
		return str.toString();
	}

	public String listExpired(MOB mob)
	{
		final StringBuilder buf=new StringBuilder("");
		if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
		{
			final String theWord=(CMProps.isUsingAccountSystem())?"account":"character";
			final List<String> l=CMLib.login().getExpiredAcctOrCharsList();
			if(l.size()>0)
			{
				buf.append(L("\n\rThere are currently @x1 expired @x2s.\n\r",""+l.size(),theWord));
				buf.append(CMLib.lister().reallyList2Cols(mob,new IteratorEnumeration<String>(l.iterator())).toString());
				buf.append(L("\n\r\n\rUse EXPIRE command to alter them.^?^.\n\r"));
			}
			else
				buf.append(L("\n\rThere are no expired @x1s at this time.\n\r",theWord));
		}
		else
			buf.append(L("\n\rAccount expiration system is not enabled on this mud.\n\r"));
		return buf.toString();
	}

	public String listEnvResources(Session viewerS, String rest)
	{
		final List<String> parms=CMParms.parse(rest.toUpperCase());
		final boolean shortList=parms.contains("SHORT");
		if(shortList)
			return CMParms.toListString(RawMaterial.CODES.NAMES());
		final StringBuilder str=new StringBuilder("");
		//for(String S : RawMaterial.CODES.NAMES())
		//	str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(S.toLowerCase()),16));
		final int COL_LEN1=CMLib.lister().fixColWidth(15.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(10.0,viewerS);
		final int COL_LEN3=CMLib.lister().fixColWidth(3.0,viewerS);
		final int COL_LEN4=CMLib.lister().fixColWidth(4.0,viewerS);
		final int COL_LEN5=CMLib.lister().fixColWidth(3.0,viewerS);
		final int COL_LEN6=CMLib.lister().fixColWidth(36.0,viewerS);
		final int COL_LEN7=COL_LEN1+1+COL_LEN2+1+COL_LEN3+1+COL_LEN4+1+COL_LEN5+1;
		str.append(CMStrings.padRight(L("Resource"),COL_LEN1)+" ");
		str.append(CMStrings.padRight(L("Material"),COL_LEN2)+" ");
		str.append(CMStrings.padRight(L("Val"),COL_LEN3)+" ");
		str.append(CMStrings.padRight(L("Freq"),COL_LEN4)+" ");
		str.append(CMStrings.padRight(L("Str"),COL_LEN5)+" ");
		str.append(L("Locales\n\r"));
		for(final int i : RawMaterial.CODES.ALL())
		{
			final String resourceName=CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(i).toLowerCase());
			final String materialName=RawMaterial.Material.findByMask(i&RawMaterial.MATERIAL_MASK).noun().toLowerCase();
			if((rest.length()==0)
			||(resourceName.indexOf(rest)>=0)
			||(materialName.indexOf(rest)>=0))
			{
				str.append(CMStrings.padRight(resourceName,COL_LEN1+1));
				str.append(CMStrings.padRight(materialName,COL_LEN2+1));
				str.append(CMStrings.padRight(""+RawMaterial.CODES.VALUE(i),COL_LEN3+1));
				str.append(CMStrings.padRight(""+RawMaterial.CODES.FREQUENCY(i),COL_LEN4+1));
				str.append(CMStrings.padRight(""+RawMaterial.CODES.HARDNESS(i),COL_LEN5+1));
				StringBuilder locales=new StringBuilder("");
				for(final Enumeration<Room> e=CMClass.locales();e.hasMoreElements();)
				{
					final Room R=e.nextElement();
					if(!(R instanceof GridLocale))
						if((R.resourceChoices()!=null)&&(R.resourceChoices().contains(Integer.valueOf(i))))
							locales.append(R.ID()+" ");
				}
				while(locales.length()>COL_LEN6)
				{
					str.append(locales.toString().substring(0,COL_LEN6)+"\n\r"+CMStrings.padRight(" ",COL_LEN7));
					locales=new StringBuilder(locales.toString().substring(COL_LEN6));
				}
				str.append(locales.toString());
				str.append("\n\r");
			}
		}
		return str.toString();
	}

	public List<String> getMyCmdWords(MOB mob)
	{
		final ArrayList<String> V=new ArrayList<String>();
		for (final ListCmdEntry cmd : ListCmdEntry.values())
		{
			if((CMSecurity.isAllowedAnywhereContainsAny(mob, cmd.flags))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				V.add(cmd.cmd[0]);
		}
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				V.add(CMJ.NAME()+"S");
		}
		Collections.sort(V);
		return V;
	}

	public ListCmdEntry getMyCmd(MOB mob, String s)
	{
		s=s.toUpperCase().trim();
		for(final ListCmdEntry cmd : ListCmdEntry.values())
		{
			for(int i2=0;i2<cmd.cmd.length;i2++)
			{
				if(cmd.cmd[i2].startsWith(s))
				{
					if((CMSecurity.isAllowedAnywhereContainsAny(mob, cmd.flags))
					||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
					{
						return cmd;
					}
				}
			}
		}
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(((CMJ.NAME()+"S").startsWith(s)||CMJ.NAME().equals(s)||CMJ.NAME().replace('_', ' ').equals(s))
			&&((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
				||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)))
					return ListCmdEntry.COMMANDJOURNAL;
		}
		return null;
	}

	public ListCmdEntry getAnyCmd(MOB mob)
	{
		for(final ListCmdEntry cmd : ListCmdEntry.values())
		{
			if((CMSecurity.isAllowedAnywhereContainsAny(mob, cmd.flags))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
			{
				return cmd;
			}
		}
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				return ListCmdEntry.COMMANDJOURNAL;
		}
		return null;
	}

	public String listComponents(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("^xAll Defined Spells and required components: ^N\n\r");
		for(final String ID : CMLib.ableComponents().getAbilityComponentMap().keySet())
		{
			final List<AbilityComponent> DV=CMLib.ableComponents().getAbilityComponentMap().get(ID);
			if(DV!=null)
				buf.append(CMStrings.padRight(ID,20)+": "+CMLib.ableComponents().getAbilityComponentDesc(null,ID)+"\n\r");
		}
		if(buf.length()==0)
			return "None defined.";
		return buf.toString();
	}

	public String listExpertises(Session viewerS, List<String> commands)
	{
		WikiFlag wiki = getWikiFlagRemoved(commands);
		final StringBuilder buf=new StringBuilder("^xAll Defined Expertise Codes: ^N\n\r");
		final int COL_LEN=CMLib.lister().fixColWidth(20.0,viewerS);
		if(wiki==WikiFlag.WIKILIST)
		{
			final Set<String> doneBases=new TreeSet<String>();
			for(final Enumeration<ExpertiseLibrary.ExpertiseDefinition> e=CMLib.expertises().definitions();e.hasMoreElements();)
			{
				ExpertiseLibrary.ExpertiseDefinition def=e.nextElement();
				if(!doneBases.contains(def.getBaseName()))
				{
					doneBases.add(def.getBaseName());
					String name=def.name();
					int x=name.lastIndexOf(' ');
					if(CMath.isRomanNumeral(name.substring(x+1).trim()))
						name=name.substring(0, x).trim();
					buf.append("*[["+name+"(Expertise)|"+name+"]]\n\r");
				}
			}
		}
		else
		if(wiki==WikiFlag.WIKIHELP)
		{
			final Set<String> doneBases=new TreeSet<String>();
			for(final Enumeration<ExpertiseLibrary.ExpertiseDefinition> e=CMLib.expertises().definitions();e.hasMoreElements();)
			{
				ExpertiseLibrary.ExpertiseDefinition def=e.nextElement();
				if(!doneBases.contains(def.getBaseName()))
				{
					doneBases.add(def.getBaseName());
					String name=def.name();
					int x=name.lastIndexOf(' ');
					if(CMath.isRomanNumeral(name.substring(x+1).trim()))
					{
						name=name.substring(0, x).trim();
						ExpertiseLibrary.ExpertiseDefinition defGuess=CMLib.expertises().findDefinition(def.getBaseName()+"1",true);
						if(defGuess!=null)
							def=defGuess;
					}
					String desc=CMLib.expertises().getExpertiseHelp(def.name().toUpperCase().replaceAll(" ","_"), true);
					if((desc!=null)&&(desc.startsWith("<EXPERTISE>")))
						desc=desc.substring(11);
					else
						desc="";
					final String helpEOL=CMStrings.getEOL(desc.toString(),"\n\r");
					buf.append("{{ExpertiseTemplate"
							+ "|Name="+name
							+ "|Requires="+CMLib.masking().maskDesc(def.allRequirements(),true)
							+ "|Description="+CMStrings.replaceAll(desc,helpEOL,helpEOL+helpEOL)
							+ "|Cost="+def.costDescription()
							+ "}}\n\r");
				}
			}
		}
		else
		for(final Enumeration<ExpertiseLibrary.ExpertiseDefinition> e=CMLib.expertises().definitions();e.hasMoreElements();)
		{
			final ExpertiseLibrary.ExpertiseDefinition def=e.nextElement();
			buf.append(CMStrings.padRight("^Z"+def.ID(),COL_LEN)+"^?: "
					  +CMStrings.padRight(def.name(),COL_LEN)+": "
					  +CMLib.masking().maskDesc(def.allRequirements())+"\n\r");
		}
		if(buf.length()==0)
			return "None defined.";
		return buf.toString();
	}

	public String listSocials(Session viewerS, List<String> commands)
	{
		WikiFlag wiki = getWikiFlagRemoved(commands);
		final StringBuilder buf=new StringBuilder("^xAll Defined Socials: ^N\n\r");
		final int COL_LEN=CMLib.lister().fixColWidth(18.0,viewerS);
		int col=0;
		Set<String> baseDone = new HashSet<String>();
		for(String social : CMLib.socials().getSocialsList())
		{
			final Social soc=CMLib.socials().fetchSocial(social,true);
			if(baseDone.contains(soc.baseName()))
				continue;
			baseDone.add(soc.baseName());
			if(wiki == WikiFlag.WIKILIST)
				buf.append("*[["+social+"|"+social+"]]\n\r");
			else
			if(wiki == WikiFlag.WIKIHELP)
			{
				final String TargetNoneYouSee;
				final String TargetNoneOthersSee;
				final String TargetSomeoneNoTarget;
				final String TargetSomeoneYouSee;
				final String TargetSomeoneTargetSees;
				final String TargetSomeoneOthersSee;
				final String TargetSelfYouSee;
				final String TargetSelfOthersSee;
				final Social TargetNoneSoc = CMLib.socials().fetchSocial(soc.baseName(), true);
				if(TargetNoneSoc!=null)
				{
					TargetNoneYouSee = TargetNoneSoc.You_see();
					TargetNoneOthersSee = TargetNoneSoc.Third_party_sees();
				}
				else
				{
					TargetNoneYouSee = "";
					TargetNoneOthersSee = "";
				}
				final Social TargetSomeoneSoc = CMLib.socials().fetchSocial(soc.baseName()+" <T-NAME>", true);
				if(TargetSomeoneSoc!=null)
				{
					TargetSomeoneNoTarget = TargetSomeoneSoc.See_when_no_target();
					TargetSomeoneYouSee = TargetSomeoneSoc.You_see();
					TargetSomeoneOthersSee = TargetSomeoneSoc.Third_party_sees();
					TargetSomeoneTargetSees=TargetSomeoneSoc.Target_sees();
				}
				else
				{
					TargetSomeoneNoTarget = "";
					TargetSomeoneYouSee = "";
					TargetSomeoneOthersSee = "";
					TargetSomeoneTargetSees= "";
				}
				final Social TargetSelfSoc = CMLib.socials().fetchSocial(soc.baseName()+" SELF", true);
				if(TargetSelfSoc!=null)
				{
					TargetSelfYouSee = TargetSelfSoc.You_see();
					TargetSelfOthersSee = TargetSelfSoc.Third_party_sees();
				}
				else
				{
					TargetSelfYouSee = "";
					TargetSelfOthersSee = "";
				}
				buf.append("{{SocialTemplate"
						+ "|Name="+CMStrings.capitalizeAndLower(soc.baseName())
						+ "|TargetNoneUSee="+TargetNoneYouSee
						+ "|TargetNoneTheySee="+TargetNoneOthersSee
						+ "|TargetSomeoneNoTarget="+TargetSomeoneNoTarget
						+ "|TargetSomeoneUSee="+TargetSomeoneYouSee
						+ "|TargetSomeoneTargetSees="+TargetSomeoneTargetSees
						+ "|TargetSomeoneOthersSee="+TargetSomeoneOthersSee
						+ "|TargetSelfUSee="+TargetSelfYouSee
						+ "|TargetSelfOthersSee="+TargetSelfOthersSee
						+ "}}\n\r");
			}
			else
			{
				buf.append(CMStrings.padRight(social,COL_LEN)+" ");
				col++;
				if(col==4)
				{
					col=0;
					buf.append("\n\r");
				}
			}
		}
		if(buf.length()==0)
			return "None defined.";
		return buf.toString();
	}

	public String listTitles(Session viewerS)
	{
		final StringBuilder buf=new StringBuilder("^xAll Defined Auto-Titles: ^N\n\r");
		for(final Enumeration<String> e=CMLib.titles().autoTitles();e.hasMoreElements();)
		{
			final String title=e.nextElement();
			final String maskDesc=CMLib.masking().maskDesc(CMLib.titles().getAutoTitleMask(title));
			buf.append(CMStrings.padRight(title,30)+": "+maskDesc+"\n\r");
		}
		if(buf.length()==0)
			return "None defined.";
		return buf.toString();
	}

	public String listAchievements(Session viewerS)
	{
		final StringBuilder str=new StringBuilder();
		final int COL_LEN1=CMLib.lister().fixColWidth(17.0,viewerS);
		final int COL_LEN2=CMLib.lister().fixColWidth(7.0,viewerS);
		final int COL_LEN3=CMLib.lister().fixColWidth(6.0,viewerS);
		final int COL_LEN4=CMLib.lister().fixColWidth(5.0,viewerS);
		final int COL_LEN5=CMLib.lister().fixColWidth(11.0,viewerS);
		final int COL_LEN6=CMLib.lister().fixColWidth(26.0,viewerS);
		final int COL_LEN7=COL_LEN1+1+COL_LEN2+1+COL_LEN3+1+COL_LEN4+1+COL_LEN5+1;
		final boolean accountSys = CMProps.isUsingAccountSystem(); 
		for(AccountStats.Agent agent : AccountStats.Agent.values())
		{
			if((!accountSys) && (agent != AccountStats.Agent.PLAYER))
				continue;
			switch(agent)
			{
			case ACCOUNT:
				str.append(L("^xAccount Achievements: ^N\n\r"));
				break;
			case PLAYER:
				str.append(L("^xPlayer Achievements: ^N\n\r"));
				break;
			}
			str.append(CMStrings.padRight(L("Tattoo"),COL_LEN1)+" ");
			str.append(CMStrings.padRight(L("Type"),COL_LEN2)+" ");
			str.append(CMStrings.padRight(L("Awards"),COL_LEN3)+" ");
			str.append(CMStrings.padRight(L("Count"),COL_LEN4)+" ");
			str.append(CMStrings.padRight(L("Parm1"),COL_LEN5)+" ");
			str.append(L("Display\n\r"));
			str.append(CMStrings.repeat('-',COL_LEN7+COL_LEN6)).append("\n\r");
			StringBuilder listStr = new StringBuilder("");
			for(final Enumeration<Achievement> e=CMLib.achievements().achievements(agent);e.hasMoreElements();)
			{
				final Achievement A=e.nextElement();
				listStr.append(CMStrings.padRight(A.getTattoo(),COL_LEN1)+" ");
				listStr.append(CMStrings.padRight(A.getEvent().name(),COL_LEN2)+" ");
				StringBuilder rewardDisplay = new StringBuilder("");
				TitleAward titleAward = null;
				for(Award award : A.getRewards())
				{
					switch(award.getType())
					{
					case ABILITY:
						rewardDisplay.append(((AbilityAward)award).getAbilityMapping().abilityID()+" ");
						break;
					case CURRENCY:
						rewardDisplay.append(((CurrencyAward)award).getAmount()+" "+((CurrencyAward)award).getCurrency()+" ");
						break;
					case STAT:
						if(((StatAward)award).getStat().indexOf(' ')>0)
							rewardDisplay.append(((StatAward)award).getAmount()+" \\\""+((StatAward)award).getStat()+"\\\" ");
						else
							rewardDisplay.append(((StatAward)award).getAmount()+" "+((StatAward)award).getStat()+" ");
						break;
					case EXPERTISE:
						rewardDisplay.append(((ExpertiseAward)award).getExpertise().ID()+" ");
						break;
					case QP:
						rewardDisplay.append(((AmountAward)award).getAmount()+"QP ");
						break;
					case TITLE:
						titleAward=(TitleAward)award;
						break;
					case XP:
						rewardDisplay.append(((AmountAward)award).getAmount()+"XP ");
						break;
					default:
						break;
					}
				}
				if((titleAward!=null) && (rewardDisplay.length() < COL_LEN3))
					rewardDisplay.append(titleAward.getTitle()+" ");
				listStr.append(CMStrings.padRight(rewardDisplay.toString(),COL_LEN3)+" ");
				listStr.append(CMStrings.padRight(A.getTargetCount()+"",COL_LEN4)+" ");
				String miscVal = "";
				for(String parmName : A.getEvent().getParameters())
				{
					if(!CMStrings.contains(AchievementLibrary.BASE_ACHIEVEMENT_PARAMETERS,parmName))
					{
						String val = A.getRawParmVal(parmName);
						if((val.length()>0)&&(!CMath.isMathExpression(val)))
							miscVal=val;
					}
				}
				listStr.append(CMStrings.padRight(miscVal,COL_LEN5)+" ");
				listStr.append(CMStrings.limit(A.getDisplayStr(),COL_LEN6));
				listStr.append("\n\r");
			}
			if(listStr.length()==0)
				listStr.append(L("None defined.\n\r"));
			str.append(listStr).append("\n\r");
		}
		return str.toString();
	}

	public String listClanGovernments(Session viewerS, List<String> commands)
	{
		final StringBuilder buf=new StringBuilder("^xAll Clan Governments: ^N\n\r");
		int glen=0;
		for(final ClanGovernment G : CMLib.clans().getStockGovernments())
		{
			if(G.getName().length()>glen)
				glen=G.getName().length();
		}
		final int SCREEN_LEN=CMLib.lister().fixColWidth(78.0,viewerS);
		for(final ClanGovernment G : CMLib.clans().getStockGovernments())
			buf.append(CMStrings.padRight(G.getName(),glen)+": "+CMStrings.limit(G.getShortDesc(),SCREEN_LEN-glen-2)+"\n\r");
		return buf.toString();
	}

	public String listClans(Session viewerS, List<String> commands)
	{
		final StringBuilder buf=new StringBuilder("^xAll Clans: ^N\n\r");
		int clen=0;
		for(final Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
		{
			final Clan C=c.nextElement();
			if(C.clanID().length()>clen)
				clen=C.clanID().length();
		}
		final int SCREEN_LEN=CMLib.lister().fixColWidth(78.0,viewerS);
		for(final Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
		{
			final Clan C=c.nextElement();
			buf.append(CMStrings.padRight(C.clanID(),clen)+": "+CMStrings.limit(C.getMemberList().size()+" members",SCREEN_LEN-clen-2)+"\n\r");
		}
		return buf.toString();
	}

	public StringBuilder listContent(MOB mob, List<String> commands)
	{
		commands.remove(0);
		Enumeration<Room> roomsToDo=null;
		final String rest=CMParms.combine(commands,0);
		if(rest.equalsIgnoreCase("area"))
			roomsToDo=mob.location().getArea().getMetroMap();
		else
		if(rest.trim().length()==0)
			roomsToDo=new XVector<Room>(mob.location()).elements();
		else
		{
			final Area A=CMLib.map().findArea(rest);
			if(A!=null)
				roomsToDo=A.getMetroMap();
			else
			{
				final Room R=CMLib.map().getRoom(rest);
				if(R!=null)
					roomsToDo=new XVector<Room>(mob.location()).elements();
				else
					return new StringBuilder("There's no such place as '"+rest+"'");
			}
		}
		final StringBuilder buf=new StringBuilder("");
		Room R=null;
		Room TR=null;
		final int SCREEN_LEN1=CMLib.lister().fixColWidth(15.0,mob);
		final int SCREEN_LEN2=CMLib.lister().fixColWidth(35.0,mob);
		final int SCREEN_LEN3=CMLib.lister().fixColWidth(3.0,mob);
		final int SCREEN_LIMIT=CMLib.lister().fixColWidth(22.0 - (2*3),mob);
		buf.append("^N"+CMStrings.padRight("Class",SCREEN_LEN1)+": "+CMStrings.padRight("Display/Name",SCREEN_LEN2)+": "
				+CMStrings.padRight("Lvl",SCREEN_LEN3)+": "
				+CMStrings.limit("Align/Container",SCREEN_LIMIT)
				+"^N\n\r");
		buf.append("^N"+CMStrings.repeat('-',SCREEN_LEN1)+": "+CMStrings.repeat('-',SCREEN_LEN2)+": "
				+CMStrings.repeat('-',SCREEN_LEN3)+": "
				+CMStrings.repeat('-',SCREEN_LIMIT)
				+"^N\n\r");
		boolean first=true;
		for(;roomsToDo.hasMoreElements();)
		{
			R=roomsToDo.nextElement();
			if(R.roomID().length()==0)
				continue;
			TR=CMLib.database().DBReadRoom(CMLib.map().getExtendedRoomID(R),false);
			if(TR==null)
				buf.append(L("'@x1' could not be read from the database!\n\r",CMLib.map().getExtendedRoomID(R)));
			else
			{
				CMLib.database().DBReadContent(TR.roomID(),TR,false);
				if(roomsToDo.hasMoreElements() || !first)
					buf.append("\n\r^NRoomID: "+CMLib.map().getDescriptiveExtendedRoomID(TR)+"\n\r");
				for(int m=0;m<TR.numInhabitants();m++)
				{
					final MOB M=TR.fetchInhabitant(m);
					if(M==null)
						continue;
					buf.append("^M"+CMStrings.padRight(M.ID(),SCREEN_LEN1)+": "+CMStrings.padRight(M.displayText(),SCREEN_LEN2)+": "
								+CMStrings.padRight(M.phyStats().level()+"",SCREEN_LEN3)+": "
								+CMStrings.limit(CMLib.flags().getAlignmentName(M),SCREEN_LIMIT)
								+"^N\n\r");
					for(int i=0;i<M.numItems();i++)
					{
						final Item I=M.getItem(i);
						if(I!=null)
						{
							buf.append("    ^I"+CMStrings.padRight(I.ID(),SCREEN_LEN1-4)
									+": "+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),SCREEN_LEN2)+": "
									+CMStrings.padRight(I.phyStats().level()+"",SCREEN_LEN3)+": "
									+"^N"+CMStrings.limit(((I.container()!=null)?I.container().Name():""),SCREEN_LIMIT)+"\n\r");
						}
					}
				}
				for(int i=0;i<TR.numItems();i++)
				{
					final Item I=TR.getItem(i);
					if(I!=null)
					{
						buf.append("^I"+CMStrings.padRight(I.ID(),SCREEN_LEN1)+": "
								+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),SCREEN_LEN2)+": "
								+CMStrings.padRight(I.phyStats().level()+"",SCREEN_LEN3)+": "
								+"^N"+CMStrings.limit(((I.container()!=null)?I.container().Name():""),SCREEN_LIMIT)+"\n\r");
					}
				}
				TR.destroy();
			}
			first=false;
		}
		return buf;
	}

	public void listPolls(MOB mob, List<String> commands)
	{
		final Iterator<Poll> i=CMLib.polls().getPollList();
		if(!i.hasNext())
			mob.tell(L("\n\rNo polls available.  Fix that by entering CREATE POLL!"));
		else
		{
			final StringBuilder str=new StringBuilder("");
			int v=1;
			for(;i.hasNext();v++)
			{
				final Poll P=i.next();
				str.append(CMStrings.padRight(""+v,2)+": "+P.getName());
				if(!CMath.bset(P.getFlags(),Poll.FLAG_ACTIVE))
					str.append(L(" (inactive)"));
				else
				if(P.getExpiration()>0)
					str.append(L(" (expires: @x1)",CMLib.time().date2String(P.getExpiration())));
				str.append("\n\r");
			}
			mob.tell(str.toString());
		}
	}
	
	public void listFileUse(MOB mob, Session S, String fileName)
	{
		CMFile F = new CMFile(fileName,mob,CMFile.FLAG_LOGERRORS);
		if((!F.exists())||(!F.canRead()))
		{
			F = new CMFile(Resources.makeFileResourceName(fileName),mob,CMFile.FLAG_LOGERRORS);
			if((!F.exists())||(!F.canRead()))
			{
				if(S!=null)
					S.safeRawPrintln(L("File not found: @x1",fileName));
				return;
			}
		}
		if(S!=null)
			S.safeRawPrintln(L("Searching..."));
		
		fileName = fileName.toLowerCase();
		Map<String,Set<Environmental>> found=new TreeMap<String,Set<Environmental>>();
		for(Enumeration<Room> a=CMLib.map().rooms();a.hasMoreElements();)
		{
			final Room R=a.nextElement();
			if(R!=null)
			{
				CMLib.coffeeMaker().fillFileMap(R, found);
				for(String foundPath : found.keySet())
				{
					final String lfoundPath = foundPath.toLowerCase();
					if(lfoundPath.endsWith(fileName) || fileName.endsWith(lfoundPath))
					{
						if(S!=null)
							S.println(L("Found '@x1' on @x2 in room @x3.",foundPath,found.get(foundPath).iterator().next().Name(),CMLib.map().getExtendedRoomID(R)));
					}
				}
				found.clear();
			}
		}
		if(S!=null)
			S.safeRawPrintln(L("Done."));
	}

	public void listLog(MOB mob, List<String> commands)
	{
		final int pageBreak=((mob.playerStats()!=null)?mob.playerStats().getPageBreak():0);
		int lineNum=0;
		if(commands.size()<2)
		{
			final Log.LogReader log=Log.instance().getLogReader();
			String line=log.nextLine();
			while((line!=null)&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				mob.session().safeRawPrintln(line);
				if((pageBreak>0)&&(lineNum>=pageBreak))
				{
					if(!pause(mob.session()))
						break;
					else
						lineNum=0;
				}
				lineNum++;
				line=log.nextLine();
			}
			log.close();
			return;
		}

		int start = 0;
		final int logSize = Log.instance().numLines();
		int end = logSize;
		final Log.LogReader log = Log.instance().getLogReader();

		for(int i=1;i<commands.size();i++)
		{
			String s=commands.get(i);
			if((s.equalsIgnoreCase("front")||(s.equalsIgnoreCase("first"))||(s.equalsIgnoreCase("head")))
			&&(i<(commands.size()-1)))
			{
				s=commands.get(i+1);
				if(CMath.isInteger(s))
				{
					i++;
					end=CMath.s_int(s);
				}
				else
				{
					mob.tell(L("Bad @x1 parameter format after.",s));
					return;
				}
			}
			else
			if((s.equalsIgnoreCase("back")||(s.equalsIgnoreCase("last"))||(s.equalsIgnoreCase("tail")))
			&&(i<(commands.size()-1)))
			{
				s=commands.get(i+1);
				if(CMath.isInteger(s))
				{
					i++;
					start=(end-CMath.s_int(s))-1;
				}
				else
				{
					mob.tell(L("Bad @x1 parameter format after.",s));
					return;
				}
			}
			else
			if(s.equalsIgnoreCase("skip")
			&&(i<(commands.size()-1)))
			{
				s=commands.get(i+1);
				if(CMath.isInteger(s))
				{
					i++;
					start=start+CMath.s_int(s);
				}
				else
				{
					mob.tell(L("Bad @x1 parameter format after.",s));
					return;
				}
			}
		}
		if(end>=logSize)
			end=logSize;
		if(start<0)
			start=0;
		String line=log.nextLine();
		lineNum=0;
		int shownLineNum=0;
		while((line!=null)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			if((lineNum>start)&&(lineNum<=end))
			{
				mob.session().safeRawPrintln(line);
				if((pageBreak>0)&&(shownLineNum>=pageBreak))
				{
					if(!pause(mob.session()))
						break;
					else
						shownLineNum=0;
				}
				shownLineNum++;
			}
			lineNum++;
			line=log.nextLine();
		}
		log.close();
	}

	public enum ListCmdEntry
	{
		UNLINKEDEXITS("UNLINKEDEXITS",new SecFlag[]{SecFlag.CMDEXITS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		ITEMS("ITEMS",new SecFlag[]{SecFlag.CMDITEMS}),
		ARMOR("ARMOR",new SecFlag[]{SecFlag.CMDITEMS}),
		ABILITYDOMAINS("ABILITYDOMAINS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDABILITIES}),
		ABILITYCODES("ABILITYCODES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDABILITIES}),
		ABILITYFLAGS("ABILITYFLAGS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDABILITIES}),
		ENVRESOURCES("ENVRESOURCES",new SecFlag[]{SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		WEAPONS("WEAPONS",new SecFlag[]{SecFlag.CMDITEMS}),
		MOBS("MOBS",new SecFlag[]{SecFlag.CMDMOBS}),
		ROOMS("ROOMS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		AREA("AREA",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		LOCALES("LOCALES",new SecFlag[]{SecFlag.CMDROOMS}),
		BEHAVIORS("BEHAVIORS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		EXITS("EXITS",new SecFlag[]{SecFlag.CMDEXITS}),
		RACES("RACES",new SecFlag[]{SecFlag.CMDRACES,SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS}),
		CLASSES("CLASSES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDCLASSES}),
		STAFF("STAFF",new SecFlag[]{SecFlag.CMDAREAS}),
		ABILITIES("ABILITIES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		SPELLS("SPELLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		SONGS("SONGS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		PRAYERS("PRAYERS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		PROPERTIES("PROPERTIES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		THIEFSKILLS("THIEFSKILLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		COMMON("COMMON",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		JOURNALS("JOURNALS",new SecFlag[]{SecFlag.JOURNALS}),
		COMMANDJOURNALS("COMMANDJOURNALS",new SecFlag[]{SecFlag.JOURNALS}),
		SKILLS("SKILLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		QUESTS("QUESTS",new SecFlag[]{SecFlag.CMDQUESTS}),
		QUESTWINNERS("QUESTWINNERS",new SecFlag[]{SecFlag.CMDQUESTS}),
		QUESTNAMES("QUESTNAMES",new SecFlag[]{SecFlag.CMDQUESTS}),
		DISEASES("DISEASES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		POISONS("POISONS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		LANGUAGES("LANGUAGES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		TICKS("TICKS",new SecFlag[]{SecFlag.LISTADMIN}),
		MAGIC("MAGIC",new SecFlag[]{SecFlag.CMDITEMS}),
		TECH("TECH",new SecFlag[]{SecFlag.CMDITEMS}),
		CLANITEMS("CLANITEMS",new SecFlag[]{SecFlag.CMDITEMS,SecFlag.CMDCLANS}),
		COMMANDJOURNAL("COMMANDJOURNAL",new SecFlag[]{}), // blank, but used!
		REALESTATE("REALESTATE",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		NOPURGE("NOPURGE",new SecFlag[]{SecFlag.NOPURGE}),
		BANNED("BANNED",new SecFlag[]{SecFlag.BAN}),
		RACECATS("RACECATS",new SecFlag[]{SecFlag.CMDRACES,SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS}),
		LOG("LOG",new SecFlag[]{SecFlag.LISTADMIN}),
		USERS("USERS",new SecFlag[]{SecFlag.CMDPLAYERS,SecFlag.STAT}),
		LINKAGES("LINKAGES",new SecFlag[]{SecFlag.CMDAREAS}),
		REPORTS("REPORTS",new SecFlag[]{SecFlag.LISTADMIN}),
		THREAD("THREAD",new SecFlag[]{SecFlag.LISTADMIN}),
		THREADS("THREADS",new SecFlag[]{SecFlag.LISTADMIN}),
		RESOURCES("RESOURCES",new SecFlag[]{SecFlag.LOADUNLOAD}),
		ONEWAYDOORS("ONEWAYDOORS",new SecFlag[]{SecFlag.CMDEXITS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		CHANTS("CHANTS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		SUPERPOWERS(new String[]{"SUPERPOWERS","POWERS"},new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		COMPONENTS("COMPONENTS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.COMPONENTS}),
		EXPERTISES("EXPERTISES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.EXPERTISES}),
		FACTIONS("FACTIONS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDFACTIONS}),
		MATERIALS("MATERIALS",new SecFlag[]{SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		OBJCOUNTERS("OBJCOUNTERS",new SecFlag[]{SecFlag.LISTADMIN}),
		POLLS("POLLS",new SecFlag[]{SecFlag.POLLS,SecFlag.LISTADMIN}),
		CONTENTS("CONTENTS",new SecFlag[]{SecFlag.CMDROOMS,SecFlag.CMDITEMS,SecFlag.CMDMOBS,SecFlag.CMDAREAS}),
		EXPIRES("EXPIRES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		TITLES("TITLES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.TITLES}),
		ACHIEVEMENTS("ACHIEVEMENTS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.ACHIEVEMENTS}),
		AREARESOURCES("AREARESOURCES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		CONQUERED("CONQUERED",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		HOLIDAYS("HOLIDAYS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		RECIPES("RECIPES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDRECIPES}),
		HELPFILEREQUESTS("HELPFILEREQUESTS",new SecFlag[]{SecFlag.LISTADMIN}),
		SCRIPTS("SCRIPTS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		ACCOUNTS("ACCOUNTS",new SecFlag[]{SecFlag.CMDPLAYERS,SecFlag.STAT}),
		GOVERNMENTS("GOVERNMENTS",new SecFlag[]{SecFlag.CMDCLANS}),
		CLANS("CLANS",new SecFlag[]{SecFlag.CMDCLANS}),
		DEBUGFLAG("DEBUGFLAG",new SecFlag[]{SecFlag.LISTADMIN}),
		DISABLEFLAG("DISABLEFLAG",new SecFlag[]{SecFlag.LISTADMIN}),
		ENABLEFLAG("ENABLEFLAG",new SecFlag[]{SecFlag.LISTADMIN}),
		ALLQUALIFYS("ALLQUALIFYS",new SecFlag[]{SecFlag.CMDABILITIES,SecFlag.LISTADMIN}),
		NEWS("NEWS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.JOURNALS,SecFlag.NEWS}),
		AREAS("AREAS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		TIMEZONES("TIMEZONES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		SESSIONS("SESSIONS",new SecFlag[]{SecFlag.SESSIONS}),
		WORLD("WORLD",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		PLANETS("PLANETS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		SPACE(new String[]{"SPACE","BODIES","MOONS","STARS","SPACESHIPS"},new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		SPACESHIPAREAS("SPACESHIPAREAS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		CURRENTS("CURRENTS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS,SecFlag.CMDMOBS}),
		MANUFACTURERS("MANUFACTURERS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDITEMS}),
		TECHSKILLS("TECHSKILLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		SOFTWARE("SOFTWARE",new SecFlag[]{SecFlag.CMDITEMS}),
		EXPIRED("EXPIRED",new SecFlag[]{SecFlag.CMDPLAYERS}),
		SQL("SQL",new SecFlag[]{SecFlag.CMDDATABASE}),
		SHIPS("SHIPS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDPLAYERS}),
		COMMANDS("COMMANDS",new SecFlag[]{SecFlag.LISTADMIN}),
		FILEUSE("FILEUSE",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS}),
		SOCIALS("SOCIALS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDSOCIALS,SecFlag.AREA_CMDSOCIALS}),
		AREATYPES("AREATYPES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS}),
		GENSTATS("STATS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDITEMS,SecFlag.CMDMOBS}),
		;
		public String[]			   cmd;
		public CMSecurity.SecGroup flags;
		private ListCmdEntry(String cmd, SecFlag[] flags)
		{
			this.cmd=new String[]{cmd};
			this.flags=new CMSecurity.SecGroup(flags);
		}

		private ListCmdEntry(String[] cmd, SecFlag[] flags)
		{
			this.cmd=cmd;
			this.flags=new CMSecurity.SecGroup(flags);
		}
	}

	public boolean pause(Session sess)
	{
		if((sess==null)||(sess.isStopped()))
			return false;
		sess.rawCharsOut("<pause - enter>".toCharArray());
		try
		{
			String s=sess.blockingIn(10 * 60 * 1000, true);
			if(s!=null)
			{
				s=s.toLowerCase();
				if(s.startsWith("qu")||s.startsWith("ex")||s.equals("x"))
					return false;
			}
		}
		catch (final Exception e)
		{
			return false;
		}
		return !sess.isStopped();
	}

	public void listNews(MOB mob, List<String> commands)
	{
		final String theRest=CMParms.combine(commands,1);
		final Item I=CMClass.getItem("StdJournal");
		I.setName(L("SYSTEM_NEWS"));
		I.setDescription(L("Enter `LIST NEWS [NUMBER]` to read an entry.%0D%0AEnter CREATE NEWS to add new entries. "));
		final CMMsg newMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_READ|CMMsg.MASK_ALWAYS,null,CMMsg.MSG_READ|CMMsg.MASK_ALWAYS,theRest,CMMsg.MSG_READ|CMMsg.MASK_ALWAYS,null);
		if(mob.location().okMessage(mob,newMsg)&&(I.okMessage(mob, newMsg)))
		{
			mob.location().send(mob,newMsg);
			I.executeMsg(mob,newMsg);
		}
	}

	public void listSql(MOB mob, String rest)
	{
		mob.tell(L("SQL Query: @x1",rest));
		try
		{
			final List<String[]> rows=CMLib.database().DBRawQuery(rest.replace('`','\''));
			final StringBuilder report=new StringBuilder("");
			for(final String[] row : rows)
				report.append(CMParms.toListString(row)).append("\n\r");
			if(mob.session()==null)
				return;
			mob.session().rawPrint(report.toString());
		}
		catch(final Exception e)
		{
			mob.tell(L("SQL Query Error: @x1",e.getMessage()));
		}
	}

	private enum ListAreaStats
	{
		NAME("Name", 30),
		AUTHOR("Auth", 15),
		DESCRIPTION("Desc", 50),
		ROOMS("Rooms", 6),
		STATE("State", 10),
		HIDDEN("Hiddn", 6)
		;

		public String	shortName;
		public Integer	len;

		private ListAreaStats(String shortName, int len)
		{
			this.shortName=shortName;
			this.len=Integer.valueOf(len);
		}

		@SuppressWarnings("rawtypes")
		public Comparable getFromArea(Area A)
		{
			switch(this)
			{
			case NAME:
				return A.Name();
			case HIDDEN:
				return "" + CMLib.flags().isHidden(A);
			case ROOMS:
				return Integer.valueOf(A.getProperRoomnumbers().roomCountAllAreas());
			case STATE:
				return A.getAreaState().name();
			case AUTHOR:
				return A.getAuthorID();
			case DESCRIPTION:
				return A.description().replace('\n', ' ').replace('\r', ' ');
			default:
				return "";
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public Comparable getAreaStatFromSomewhere(Area A, String stat)
	{
		if(A==null)
			return null;
		stat=stat.toUpperCase().trim();
		final ListAreaStats ls=(ListAreaStats)CMath.s_valueOf(ListAreaStats.class, stat);
		final Area.Stats as=(Area.Stats)CMath.s_valueOf(Area.Stats.class, stat);
		if(ls != null)
			return ls.getFromArea(A);
		else
		if(as!=null)
			return Integer.valueOf(A.getAreaIStats()[as.ordinal()]);
		else
			return null;
	}
	
	public void listCommands(MOB mob, List<String> commands)
	{
		
		String rest="";
		MOB whoM=mob;
		WikiFlag wiki = getWikiFlagRemoved(commands);
		if(commands.size()>1)
		{
			rest=commands.get(1);
			whoM=CMLib.players().getLoadPlayer(rest);
			if(whoM==null)
			{
				mob.tell("No '"+rest+"'");
				return;
			}
		}
		
		final StringBuffer commandList=new StringBuffer("");
		final Vector<String> commandSet=new Vector<String>();
		int col=0;
		final HashSet<String> done=new HashSet<String>();
		for(final Enumeration<Command> e=CMClass.commands();e.hasMoreElements();)
		{
			final Command C=e.nextElement();
			final String[] access=C.getAccessWords();
			if((access!=null)
			&&(access.length>0)
			&&(access[0].length()>0)
			&&(!done.contains(access[0]))
			&&(C.securityCheck(whoM)))
			{
				done.add(access[0]);
				commandSet.add(access[0]);
			}
		}
		for(final Enumeration<Ability> a=whoM.allAbilities();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)&&(A.triggerStrings()!=null)&&(A.triggerStrings().length>0)&&(!done.contains(A.triggerStrings()[0])))
			{
				done.add(A.triggerStrings()[0]);
				commandSet.add(A.triggerStrings()[0]);
			}
		}
		Collections.sort(commandSet);
		final int COL_LEN=CMLib.lister().fixColWidth(19.0,mob);
		for(final Iterator<String> i=commandSet.iterator();i.hasNext();)
		{
			final String s=i.next();
			if(wiki == WikiFlag.WIKILIST)
			{
				commandList.append("*[["+s+"|"+s+"]]\n\r");
			}
			else
			if(wiki == WikiFlag.WIKIHELP)
			{
				StringBuilder help=CMLib.help().getHelpText(s,null,false,true);
				if(help==null)
				{
					System.out.println(s);
					continue;
				}
				String helpStr=help.toString();
				if(helpStr.startsWith("<ABILITY>"))
					continue;
				String usage="";
				String example="";
				String shorts="";
				while(helpStr.startsWith("Command")||helpStr.startsWith("  "))
				{
					int end=helpStr.indexOf("\n");
					if(end<0)
						break;
					helpStr=helpStr.substring(end+1).trim();
				}
				while(helpStr.startsWith("Usage")||helpStr.startsWith("  "))
				{
					int end=helpStr.indexOf("\n");
					int start=helpStr.indexOf(":");
					if((end<0)||(start<0)||(start>end))
						break;
					usage += ((usage.length()>0) ? "\n\r" : "" ) + helpStr.substring(start+1,end).trim();
					helpStr=helpStr.substring(end+1).trim();
				}
				while(helpStr.startsWith("Example")||helpStr.startsWith("  "))
				{
					int end=helpStr.indexOf("\n");
					int start=helpStr.indexOf(":");
					if((end<0)||(start<0)||(start>end))
						break;
					example += ((example.length()>0) ? "\n\r" : "" ) + helpStr.substring(start+1,end).trim();
					helpStr=helpStr.substring(end+1).trim();
				}
				while(helpStr.startsWith("Short")||helpStr.startsWith("  "))
				{
					int end=helpStr.indexOf("\n");
					int start=helpStr.indexOf(":");
					if((end<0)||(start<0)||(start>end))
						break;
					shorts += ((example.length()>0) ? "\n\r" : "" ) + helpStr.substring(start+1,end).trim();
					helpStr=helpStr.substring(end+1).trim();
				}

				final String helpEOL=CMStrings.getEOL(helpStr,"\n\r");
				helpStr = CMStrings.replaceAll(helpStr,helpEOL,helpEOL+helpEOL);
				commandList.append("{{CommandTemplate"
								+ "|Name="+s
								+ "|Usage="+CMStrings.replaceAllofAny(usage,"[]{}<>|".toCharArray(),"\"\"()()!".toCharArray())
								+ "|Examples="+example
								+ "|Shorts="+shorts
								+ "|Description="+CMStrings.replaceAllofAny(helpStr,"[]{}<>|".toCharArray(),"()()()!".toCharArray())
								+ "}}\n\r");
			}
			else
			{
				if (++col > 3)
				{
					commandList.append("\n\r");
					col = 0;
				}
				commandList.append(CMStrings.padRight(s,COL_LEN));
			}
		}
		if(mob.session()!=null)
			mob.session().rawPrint(commandList.toString());
	}

	public void listManufacturers(MOB mob, List<String> commands)
	{
		final StringBuffer str=new StringBuffer("");
		str.append(CMStrings.padRight(L("Name"), 20)).append(" ");
		str.append(CMStrings.padRight(L("Tech"), 5)).append(" ");
		str.append(CMStrings.padRight(L("Eff."), 4)).append(" ");
		str.append(CMStrings.padRight(L("Rel."), 4)).append(" ");
		str.append("!");
		str.append(CMStrings.padRight(L("Name"), 20)).append(" ");
		str.append(CMStrings.padRight(L("Tech"), 5)).append(" ");
		str.append(CMStrings.padRight(L("Eff."), 4)).append(" ");
		str.append(CMStrings.padRight(L("Rel."), 4));
		str.append("\n\r");
		str.append(CMStrings.repeat('-', 75)).append("\n\r");
		final List<Manufacturer> l=new XVector<Manufacturer>(CMLib.tech().manufacterers());
		Collections.sort(l,new Comparator<Manufacturer>()
		{
			@Override public int compare(Manufacturer o1, Manufacturer o2)
			{
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});
		for(final Iterator<Manufacturer> i =l.iterator();i.hasNext();)
		{
			Manufacturer M=i.next();
			str.append(CMStrings.padRight(M.name(), 20)).append(" ");
			str.append(CMStrings.padRight(M.getMinTechLevelDiff()+"-"+M.getMaxTechLevelDiff(), 5)).append(" ");
			str.append(CMStrings.padRight(Math.round(M.getEfficiencyPct()*100.0)+"%", 4)).append(" ");
			str.append(CMStrings.padRight(Math.round(M.getReliabilityPct()*100.0)+"%", 4)).append(" ");
			if(i.hasNext())
			{
				M=i.next();
				str.append("!");
				str.append(CMStrings.padRight(M.name(), 20)).append(" ");
				str.append(CMStrings.padRight(M.getMinTechLevelDiff()+"-"+M.getMaxTechLevelDiff(), 5)).append(" ");
				str.append(CMStrings.padRight(Math.round(M.getEfficiencyPct()*100.0)+"%", 4)).append(" ");
				str.append(CMStrings.padRight(Math.round(M.getReliabilityPct()*100.0)+"%", 4));
			}
			str.append("\n\r");
		}
		str.append("\n\r");
		if(mob.session()!=null)
			mob.session().rawPrint(str.toString());
	}

	public void listStats(MOB mob, List<String> commands)
	{
		String wd=CMParms.combine(commands,1);
		Object o = CMClass.getObjectOrPrototype(wd);
		if(o instanceof Modifiable)
		{
			if(o instanceof CMObject)
				mob.tell("Stats for '"+((CMObject)o).ID()+"': "+CMParms.toListString(((Modifiable)o).getStatCodes()));
			else
				mob.tell("Stats for '"+o.toString()+"': "+CMParms.toListString(((Modifiable)o).getStatCodes()));
		}
		else
		{
			@SuppressWarnings("unchecked")
			PhysicalAgent P=mob.location().fetchFromMOBRoomFavorsItems(mob, null, wd, Filterer.ANYTHING);
			if(P!=null)
				mob.tell("Stats for '"+P.ID()+"': "+CMParms.toListString(P.getStatCodes()));
			else
				mob.tell("Don't know about any stats for '"+wd+"'.");
		}
	}
	
	public void listCurrents(MOB mob, List<String> commands)
	{
		final StringBuffer str=new StringBuffer("");
		for(final String key : CMLib.tech().getMakeRegisteredKeys())
		{
			str.append(L("Registered key: @x1 : @x2\n\r",key,(CMLib.tech().isCurrentActive(key)?"Activated":"Suspended")));
			str.append(CMStrings.padRight(L("Name"), 30)).append(" ");
			str.append(CMStrings.padRight(L("Room"), 30)).append(" ");
			str.append("\n\r");
			str.append(CMStrings.repeat('-', 75)).append("\n\r");
			for(final Electronics e : CMLib.tech().getMakeRegisteredElectronics(key))
			{
				str.append(CMStrings.padRight(e.Name(), 30)).append(" ");
				str.append(CMStrings.padRight(CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(e)), 30)).append(" ");
				str.append("\n\r");
			}
			str.append("\n\r");
		}
		if(str.length()==0)
			str.append(L("No electronics found.\n\r"));
		if(mob.session()!=null)
			mob.session().rawPrint(str.toString());
	}

	public void listShips(MOB mob, List<String> commands)
	{
		final Session viewerS = mob.session();
		final StringBuffer str=new StringBuffer("");
		str.append(CMStrings.padRight(L("Name"), CMLib.lister().fixColWidth(30.0,viewerS))).append(" ");
		str.append(CMStrings.padRight(L("Location"), CMLib.lister().fixColWidth(30.0,viewerS))).append(" ");
		str.append(CMStrings.padRight(L("Reg."), CMLib.lister().fixColWidth(15.0,viewerS))).append(" ");
		str.append("\n\r");
		str.append(CMStrings.repeat('-', CMLib.lister().fixColWidth(75.0,viewerS))).append("\n\r");
		for(final Enumeration<BoardableShip> s=CMLib.map().ships(); s.hasMoreElements();)
		{
			final BoardableShip S=s.nextElement();
			str.append(CMStrings.padRight(S.Name(), CMLib.lister().fixColWidth(30.0,viewerS))).append(" ");
			if((S instanceof SpaceObject)&&(((SpaceShip)S).getIsDocked()==null))
				str.append(CMStrings.padRight(CMParms.toListString(((SpaceObject)S).coordinates()), CMLib.lister().fixColWidth(30.0,viewerS))).append(" ");
			else
				str.append(CMStrings.padRight(CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(S)), CMLib.lister().fixColWidth(30.0,viewerS))).append(" ");
			if(S instanceof SpaceObject)
				str.append(CMLib.tech().getElectronicsKey(S));
			else
				str.append("N/A");
			
			str.append("\n\r");
		}
		if(str.length()==0)
			str.append(L("No ships found.\n\r"));
		if(mob.session()!=null)
			mob.session().rawPrint(str.toString());
	}

	public void listAbilities(MOB mob, Session s, List<String> commands, String title, int ofType)
	{
		int domain=0;
		final WikiFlag wiki = this.getWikiFlagRemoved(commands);
		for(int i=1;i<commands.size();i++)
		{
			final String str=commands.get(i);
			if(domain<=0)
			{
				int x=CMParms.indexOf(Ability.DOMAIN_DESCS,str.toUpperCase().trim());
				if(x>=0)
				{
					domain = x << 5;
					String domainName=CMStrings.capitalizeAllFirstLettersAndLower(Ability.DOMAIN_DESCS[x].toLowerCase().replaceAll("_"," "));
					title=(domainName+" "+title).trim();
				}
				else
				{
					s.println("Unknown '"+str+"'");
					return;
				}
			}
		}
		if(wiki == WikiFlag.WIKILIST)
		{
			if(title.length()==0)
				title="Abilities";
			if(domain == 0)
				s.println("==="+title+"s===");
			else
				s.println("==="+title+"===");
			final XVector<Ability> sortedAs = new XVector<Ability>(CMClass.abilities());
			CMClass.sortEnvironmentalsByName(sortedAs);
			s.wraplessPrintln(CMLib.lister().reallyWikiList(mob, sortedAs.elements(), ofType|domain).toString());
		}
		else
		if(wiki == WikiFlag.WIKIHELP)
		{
			StringBuilder str=new StringBuilder("");
			final XVector<Ability> sortedAs = new XVector<Ability>(CMClass.abilities());
			CMClass.sortEnvironmentalsByName(sortedAs);
			for(final Enumeration<Ability> e=sortedAs.elements();e.hasMoreElements();)
			{
				final Ability A=e.nextElement();
				if((ofType>=0)&&(ofType!=Ability.ALL_ACODES))
				{
					if((A.classificationCode()&Ability.ALL_ACODES)!=ofType)
						continue;
				}
				if(domain>0)
				{
					if((A.classificationCode()&Ability.ALL_DOMAINS)!=domain)
						continue;
				}
				String domainID = Ability.DOMAIN_DESCS[(A.classificationCode()&Ability.ALL_DOMAINS)>>5].toLowerCase();
				String domainName = CMStrings.capitalizeAllFirstLettersAndLower(domainID.replaceAll("_"," "));
				StringBuilder availStr=new StringBuilder("");
				final PairList<String,Integer> avail=CMLib.ableMapper().getAvailabilityList(A, Integer.MAX_VALUE);
				for(int c=0;c<avail.size();c++)
				{
					CharClass C=CMClass.getCharClass(avail.getFirst(c));
					Integer I=avail.getSecond(c);
					availStr.append("[["+C.name()+"("+C.baseClass()+")|"+C.name(I.intValue())+"("+I.intValue()+")]] ");
				}
				StringBuilder allowsStr=new StringBuilder("");
				for(Iterator<String> i=CMLib.expertises().filterUniqueExpertiseIDList(CMLib.ableMapper().getAbilityAllowsList(A.ID()));i.hasNext();)
				{
					final String ID=i.next();
					ExpertiseDefinition def=CMLib.expertises().getDefinition(ID);
					if(def != null)
					{
						String name=def.name();
						int x=name.lastIndexOf(' ');
						if(CMath.isRomanNumeral(name.substring(x+1).trim()))
							name=name.substring(0, x).trim();
						allowsStr.append("[["+name+"(Expertise)|"+name+"]]").append(" ");
					}
					else
					{
						final Ability A2=CMClass.getAbilityPrototype(ID);
						if(A2!=null)
							allowsStr.append("[["+A2.ID()+"|"+A2.name()+"]]").append(" ");
						else
							allowsStr.append(ID).append(" ");
					}
				}
				final String cost=CMLib.help().getAbilityCostDesc(A, null);
				final String quality=CMLib.help().getAbilityQualityDesc(A);
				String targets=CMLib.help().getAbilityTargetDesc(A);
				final String range=CMLib.help().getAbilityRangeDesc(A);
				final boolean archon = ofType == Ability.ACODE_PROPERTY;
				StringBuilder help=CMLib.help().getHelpText(A.ID(),null,archon,true);
				String usage="";
				String example="";
				String helpStr="";
				if(help==null)
					help=CMLib.help().getHelpText(A.name(),null,archon,true);
				if((help!=null)&&(help.toString().startsWith("<ABILITY>")
				||help.toString().startsWith("Property")
				||help.toString().startsWith("Disease")))
				{
					helpStr=help.toString().substring(9);
					while(helpStr.trim().startsWith(": "))
					{
						int end=helpStr.indexOf("\n");
						int start=helpStr.indexOf(":");
						if((end<0)||(start<0)||(start>end))
							break;
						helpStr=helpStr.substring(end+1).trim();
					}
					if(helpStr.startsWith("Targets"))
						targets="";
					while(helpStr.startsWith("Targets")||helpStr.startsWith("  "))
					{
						int end=helpStr.indexOf("\n");
						int start=helpStr.indexOf(":");
						if((end<0)||(start<0)||(start>end))
							break;
						targets += ((usage.length()>0) ? "\n\r" : "" ) + helpStr.substring(start+1,end).trim();
						helpStr=helpStr.substring(end+1).trim();
					}
					while(helpStr.startsWith("Usage")||helpStr.startsWith("Parameters")||helpStr.startsWith("  "))
					{
						int end=helpStr.indexOf("\n");
						int start=helpStr.indexOf(":");
						if((end<0)||(start<0)||(start>end))
							break;
						usage += ((usage.length()>0) ? "\n\r" : "" ) + helpStr.substring(start+1,end).trim();
						helpStr=helpStr.substring(end+1).trim();
					}
					while(helpStr.startsWith("Example")||helpStr.startsWith("  "))
					{
						int end=helpStr.indexOf("\n");
						int start=helpStr.indexOf(":");
						if((end<0)||(start<0)||(start>end))
							break;
						example += ((example.length()>0) ? "\n\r" : "" ) + helpStr.substring(start+1,end).trim();
						helpStr=helpStr.substring(end+1).trim();
					}
					if(helpStr.startsWith("Description")||helpStr.startsWith("  "))
					{
						int end=helpStr.indexOf("\n");
						int start=helpStr.indexOf(":");
						if((end<0)||(start<0)||(start>end))
							break;
						helpStr=helpStr.substring(end+1).trim();
					}
				}
				while(!example.endsWith("\n\r\n\r"))
					example += "\n\r";
				final String helpEOL=CMStrings.getEOL(helpStr,"\n\r");
				helpStr = CMStrings.replaceAll(helpStr,helpEOL,helpEOL+helpEOL);
				String templateName = "SkillTemplate";
				if((A.classificationCode()&Ability.ALL_ACODES)==(Ability.ACODE_PROPERTY))
					templateName="PropertyTemplate";
				if((A.classificationCode()&Ability.ALL_ACODES)==(Ability.ACODE_DISEASE))
					templateName="DiseaseTemplate";
				str.append("{{"+templateName
						+ "|ID="+A.ID()
						+ "|Name="+A.name()
						+ "|Domain=[["+domainID+"(Domain)|"+domainName+"]]"
						+ "|Available="+availStr.toString()
						+ "|Allows="+allowsStr.toString()
						+ "|UseCost="+cost
						+ "|Quality="+quality
						+ "|Targets="+targets
						+ "|Range="+range
						+ "|Commands="+CMParms.toListString(A.triggerStrings())
						+ "|Usage="+CMStrings.replaceAllofAny(usage,"[]{}<>|".toCharArray(),"\"\"()()!".toCharArray())
						+ "|Examples="+example
						+ "|Description="+CMStrings.replaceAllofAny(helpStr,"[]{}<>|".toCharArray(),"()()()!".toCharArray())
						+ "}}\n\r");
			}
			s.wraplessPrintln(str.toString());
		}
		else
		{
			s.println("^H"+title+" Ability IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.abilities(), ofType|domain).toString());
		}
	}
	
	public void listBehaviors(MOB mob, Session s, List<String> commands, String title)
	{
		int domain=0;
		final WikiFlag wiki = this.getWikiFlagRemoved(commands);
		if(wiki == WikiFlag.WIKILIST)
		{
			if(title.length()==0)
				title="Behaviors";
			if(domain == 0)
				s.println("==="+title+"s===");
			else
				s.println("==="+title+"===");
			s.wraplessPrintln(CMLib.lister().reallyWikiList(mob, CMClass.behaviors(), 0).toString());
		}
		else
		if(wiki == WikiFlag.WIKIHELP)
		{
			StringBuilder str=new StringBuilder("");
			for(final Enumeration<Behavior> e=CMClass.behaviors();e.hasMoreElements();)
			{
				final Behavior B=e.nextElement();
				StringBuilder help=CMLib.help().getHelpText(B.ID(),null,true,true);
				String usage="";
				String example="";
				String targets="";
				String helpStr="";
				if(help==null)
					help=CMLib.help().getHelpText(B.name(),null,false,true);
				if((help!=null)&&(help.toString().length()>0))
				{
					helpStr=help.toString();
					if(helpStr.startsWith("Behavior"))
					{
						int end=helpStr.indexOf("\n");
						helpStr=helpStr.substring(end+1).trim();
					}
					if(helpStr.startsWith("Targets"))
					{
						int end=helpStr.indexOf("\n");
						int start=helpStr.indexOf(":");
						if((end<0)||(start<0)||(start>end))
							break;
						targets += ((targets.length()>0) ? "\n\r" : "" ) + helpStr.substring(start+1,end).trim();
						helpStr=helpStr.substring(end+1).trim();
					}
					while(helpStr.startsWith("Parameters")||helpStr.startsWith("  "))
					{
						int end=helpStr.indexOf("\n");
						int start=helpStr.indexOf(":");
						if((end<0)||(start<0)||(start>end))
							break;
						usage += ((usage.length()>0) ? "\n\r" : "" ) + helpStr.substring(start+1,end).trim();
						helpStr=helpStr.substring(end+1).trim();
					}
					while(helpStr.startsWith("Example")||helpStr.startsWith("  "))
					{
						int end=helpStr.indexOf("\n");
						int start=helpStr.indexOf(":");
						if((end<0)||(start<0)||(start>end))
							break;
						example += ((example.length()>0) ? "\n\r" : "" ) + helpStr.substring(start+1,end).trim();
						helpStr=helpStr.substring(end+1).trim();
					}
					if(helpStr.trim().startsWith("Description: "))
						helpStr=helpStr.substring(13).trim();
				}
				final String helpEOL=CMStrings.getEOL(helpStr,"\n\r");
				helpStr = CMStrings.replaceAll(helpStr,helpEOL,helpEOL+helpEOL);
				str.append("{{BehaviorTemplate"
						+ "|Name="+B.name()
						+ "|Targets="+CMStrings.replaceAllofAny(targets,"[]{}<>|".toCharArray(),"\"\"()()!".toCharArray())
						+ "|Usage="+CMStrings.replaceAllofAny(usage,"[]{}<>|".toCharArray(),"\"\"()()!".toCharArray())
						+ "|Examples="+example
						+ "|Description="+CMStrings.replaceAllofAny(helpStr,"[]{}<>|".toCharArray(),"()()()!".toCharArray())
						+ "}}\n\r");
			}
			s.wraplessPrintln(str.toString());
		}
		else
		{
			s.println("^H"+title+" Behavior IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.behaviors(), 0).toString());
		}
	}
	
	public void listTimeZones(MOB mob, List<String> commands, Filterer<Area> filter)
	{
		if(mob==null)
			return;
		Map<TimeClock,List<Area>> timeMap = new Hashtable<TimeClock,List<Area>>();
		Map<TimeClock,Pair<String,int[]>> topClockerMap = new Hashtable<TimeClock,Pair<String,int[]>>();
		int numCols = 3;
		final int colWidth=CMLib.lister().fixColWidth(Math.round(Math.floor(78.0-numCols))/numCols,mob);
		
		Map<Area,int[]> parentageCounts = new Hashtable<Area,int[]>();
		for(final Enumeration<Area> as=CMLib.map().areas();as.hasMoreElements();)
			parentageCounts.put(as.nextElement(),new int[1]);
		Stack<Area> parentStack = new Stack<Area>();
		for(final Enumeration<Area> as=CMLib.map().areas();as.hasMoreElements();)
		{
			final Area A=as.nextElement();
			if((filter!=null)&&(!filter.passesFilter(A)))
				continue;
			for(Enumeration<Area> pa=A.getParents();pa.hasMoreElements();)
				parentStack.add(pa.nextElement());
			while(parentStack.size()>0)
			{
				Area pA=parentStack.pop();
				for(Enumeration<Area> pa=pA.getParents();pa.hasMoreElements();)
					parentStack.add(pa.nextElement());
				parentageCounts.get(pA)[0]++;
			}
		}
		
		for(final Enumeration<Area> as=CMLib.map().areas();as.hasMoreElements();)
		{
			final Area A=as.nextElement();
			if((filter!=null)&&(!filter.passesFilter(A)))
				continue;
			TimeClock C=A.getTimeObj();
			if(!timeMap.containsKey(C))
			{
				timeMap.put(C, new LinkedList<Area>());
				topClockerMap.put(C, new Pair<String,int[]>(A.Name(),new int[1]));
			}
			timeMap.get(C).add(A);
			int[] parentageCount = parentageCounts.get(A);
			Pair<String,int[]> topCount = topClockerMap.get(C);
			if(parentageCount[0] > topCount.second[0])
			{
				topCount.first = A.Name();
				topCount.second = parentageCount;
			}
		}

		StringBuilder str=new StringBuilder("");
		for(TimeClock C : timeMap.keySet())
		{
			String topName = topClockerMap.get(C).first;
			str.append(L("\n\r^HTime Zone: ^N@x1\n\r",topName));
			int col =0;
			for(Area A : timeMap.get(C))
			{
				col++;
				if(col >= numCols)
				{
					str.append("\n\r");
					col=0;
				}
				str.append(CMStrings.padRight(A.name(), colWidth));
			}
			str.append("\n\r");
		}
		final Session S=mob.session();
		if(S!=null)
			S.colorOnlyPrint(str.toString(), true);
	}
	
	public void listAreas(MOB mob, List<String> commands, Filterer<Area> filter)
	{
		if(mob==null)
			return;
		commands.remove(0);
		List<String> sortBys=null;
		List<String> colNames=null;
		WikiFlag wiki=getWikiFlagRemoved(commands);
		if(commands.size()>0)
		{
			List<String> addTos=null;
			while(commands.size()>0)
			{
				if(commands.get(0).toString().equalsIgnoreCase("sortby"))
				{
					commands.remove(0);
					sortBys=new Vector<String>();
					addTos=sortBys;
				}
				else
				if(commands.get(0).toString().equalsIgnoreCase("cols")||commands.get(0).toString().equalsIgnoreCase("columns"))
				{
					commands.remove(0);
					colNames=new Vector<String>();
					addTos=colNames;
				}
				else
				if(addTos!=null)
				{
					final String stat=commands.get(0).toString().toUpperCase().trim();
					final ListAreaStats ls=(ListAreaStats)CMath.s_valueOf(ListAreaStats.class, stat);
					final Area.Stats as=(Area.Stats)CMath.s_valueOf(Area.Stats.class, stat);
					if((ls==null)&&(as==null))
					{
						mob.tell(L("'@x1' is not recognized.  Try one of these: @x2, @x3",stat,CMParms.toListString(ListAreaStats.values()),CMParms.toListString(Area.Stats.values())));
						return;
					}
					addTos.add(stat);
					commands.remove(0);
				}
				else
				{
					mob.tell(L("'@x1' is not recognized.  Try 'columns' or 'sortby' followed by one or more of these: @x2, @x3",commands.get(0).toString(),CMParms.toListString(ListAreaStats.values()),CMParms.toListString(Area.Stats.values())));
					return;
				}
			}
		}
		final Vector<Triad<String,String,Integer>> columns=new Vector<Triad<String,String,Integer>>();
		if((colNames!=null)&&(colNames.size()>0))
		{
			for(final String newCol : colNames)
			{
				final ListAreaStats ls=(ListAreaStats)CMath.s_valueOf(ListAreaStats.class, newCol);
				final Area.Stats as=(Area.Stats)CMath.s_valueOf(Area.Stats.class, newCol);
				if(ls!=null)
					columns.add(new Triad<String,String,Integer>(ls.shortName,ls.name(),ls.len));
				else
				if(as!=null)
					columns.add(new Triad<String,String,Integer>(CMStrings.scrunchWord(CMStrings.capitalizeAndLower(newCol), 6),as.name(),Integer.valueOf(6)));
			}
		}
		else
		{
			//AREASTAT_DESCS
			columns.add(new Triad<String,String,Integer>(ListAreaStats.NAME.shortName,ListAreaStats.NAME.name(),ListAreaStats.NAME.len));
			columns.add(new Triad<String,String,Integer>(ListAreaStats.HIDDEN.shortName,ListAreaStats.HIDDEN.name(),ListAreaStats.HIDDEN.len));
			columns.add(new Triad<String,String,Integer>(ListAreaStats.ROOMS.shortName,ListAreaStats.ROOMS.name(),ListAreaStats.ROOMS.len));
			columns.add(new Triad<String,String,Integer>(ListAreaStats.STATE.shortName,ListAreaStats.STATE.name(),ListAreaStats.STATE.len));
			columns.add(new Triad<String,String,Integer>("Pop",Area.Stats.POPULATION.name(),Integer.valueOf(6)));
			columns.add(new Triad<String,String,Integer>("MedLv",Area.Stats.MED_LEVEL.name(),Integer.valueOf(6)));
		}

		final Session s=mob.session();
		final double wrap=(s==null)?78:s.getWrap();
		double totalCols=0;
		for(int i=0;i<columns.size();i++)
			totalCols+=columns.get(i).third.intValue();
		for(int i=0;i<columns.size();i++)
		{
			final double colVal=columns.get(i).third.intValue();
			final double pct=CMath.div(colVal,totalCols );
			final int newSize=(int)Math.round(Math.floor(CMath.mul(pct, wrap)));
			columns.get(i).third=Integer.valueOf(newSize);
		}

		final StringBuilder str=new StringBuilder("");
		for(final Triad<String,String,Integer> head : columns)
			str.append(CMStrings.padRight(head.first, head.third.intValue()));
		str.append("\n\r");
		final Triad<String,String,Integer> lastColomn=columns.get(columns.size()-1);
		Enumeration<Area> a;
		if(sortBys!=null)
		{
			final List<String> sortFields=sortBys;
			List<Area> sorted=new ArrayList<Area>();
			for(final Enumeration<Area> as=CMLib.map().areas();as.hasMoreElements();)
			{
				final Area A=as.nextElement();
				if((filter!=null)&&(!filter.passesFilter(A)))
					continue;
				sorted.add(A);
			}
			Collections.sort(sorted,new Comparator<Area>()
			{
				@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
				public int compare(Area arg0, Area arg1) 
				{
					for(String sortField : sortFields)
					{
						Comparable val0=getAreaStatFromSomewhere(arg0,sortField);
						Comparable val1=getAreaStatFromSomewhere(arg1,sortField);
						int comp=1;
						if((val0==null)&&(val1==null))
							comp=0;
						else
						if(val0==null)
							comp=-1;
						else
						if(val1==null)
							comp=1;
						else
							comp=val0.compareTo(val1);
						if(comp!=0)
							return comp;
					}
					return 0;
				}
			});
			if(sorted.size()>0)
				a=new IteratorEnumeration<Area>(sorted.iterator());
			else
				a=CMLib.map().areas();
		}
		else
			a=CMLib.map().areas();
		Faction theFaction=CMLib.factions().getFaction(CMLib.factions().AlignID());
		if(theFaction == null)
		{
			for(final Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
			{
				final Faction F=e.nextElement();
				if(F.showInSpecialReported())
					theFaction=F;
			}
		}
		for(;a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			if((filter!=null)&&(!filter.passesFilter(A)))
				continue;
			if(wiki==WikiFlag.WIKILIST)
			{
				str.append("*[["+A.name()+"|"+A.name()+"]]");
			}
			else
			if(wiki==WikiFlag.WIKIHELP)
			{
				String currency = CMLib.beanCounter().getCurrency(A);
				if((currency==null)||(currency.trim().length()==0))
					currency="Gold coins (default)";
				else
					currency=CMStrings.capitalizeAndLower(currency);
				final String helpEOL=CMStrings.getEOL(A.description(),"\n\r");
				final String desc = CMStrings.replaceAll(A.description(),helpEOL,helpEOL+helpEOL);
				str.append("{{AreaTemplate"
						+ "|Name="+A.name()
						+ "|Description="+desc
						+ "|Author="+A.getAuthorID()
						+ "|Rooms="+A.getAreaIStats()[Area.Stats.VISITABLE_ROOMS.ordinal()]
						+ "|Population="+A.getAreaIStats()[Area.Stats.POPULATION.ordinal()]
						+ "|Currency="+currency
						+ "|LevelRange="+A.getAreaIStats()[Area.Stats.MIN_LEVEL.ordinal()]+"-"+A.getAreaIStats()[Area.Stats.MAX_LEVEL.ordinal()]
						+ "|MedianLevel="+A.getAreaIStats()[Area.Stats.MED_LEVEL.ordinal()]
						+ "|AvgAlign="+((theFaction!=null)?theFaction.fetchRangeName(A.getAreaIStats()[Area.Stats.AVG_ALIGNMENT.ordinal()]):"")
						+ "|MedAlignment="+((theFaction!=null)?theFaction.fetchRangeName(A.getAreaIStats()[Area.Stats.MED_ALIGNMENT.ordinal()]):""));
				for(Enumeration<String> f=A.areaBlurbFlags();f.hasMoreElements();)
				{
					String flag=f.nextElement();
					str.append("|"+flag+"="+A.getBlurbFlag(flag));
				}
				str.append("}} ");
			}
			else
			{
				for(final Triad<String,String,Integer> head : columns)
				{
					Object val =getAreaStatFromSomewhere(A,head.second);
					if(val==null)
						val="?";
					if(head==lastColomn)
						str.append(CMStrings.scrunchWord(val.toString(), head.third.intValue()-1));
					else
						str.append(CMStrings.padRight(CMStrings.scrunchWord(val.toString(), head.third.intValue()-1), head.third.intValue()));
				}
			}
			str.append("\n\r");
		}
		if(s!=null)
			s.colorOnlyPrint(str.toString(), true);
	}

	public void listSessions(MOB mob, List<String> commands)
	{
		String sort="";
		if((commands!=null)&&(commands.size()>1))
			sort=CMParms.combine(commands,1).trim().toUpperCase();
		
		final StringBuffer lines=new StringBuffer("\n\r^x");
		lines.append(CMStrings.padRight("#",3)+"| ");
		lines.append(CMStrings.padRight(L("Status"),9)+"| ");
		lines.append(CMStrings.padRight(L("Valid"),5)+"| ");
		lines.append(CMStrings.padRight(L("Name"),17)+"| ");
		lines.append(CMStrings.padRight(L("IP"),17)+"| ");
		lines.append(CMStrings.padRight(L("Idle"),17)+"^.^N\n\r");
		final Vector<String[]> broken=new Vector<String[]>();
		final boolean skipUnnamed = (sort.length()>0)&&("NAME".startsWith(sort)||"PLAYER".startsWith(sort));
		for(final Session S : CMLib.sessions().allIterable())
		{
			final String[] set=new String[6];
			set[0]=CMStrings.padRight(""+broken.size(),3)+"| ";
			set[1]=(S.isStopped()?"^H":"")+CMStrings.padRight(S.getStatus().toString(),9)+(S.isStopped()?"^?":"")+"| ";
			if (S.mob() != null)
			{
				set[2]=CMStrings.padRight(((S.mob().session()==S)?"Yes":"^HNO!^?"),5)+"| ";
				set[3]="^!"+CMStrings.padRight("^<LSTUSER^>"+S.mob().Name()+"^</LSTUSER^>",17)+"^?| ";
			}
			else
			if(skipUnnamed)
				continue;
			else
			{
				set[2]=CMStrings.padRight(L("N/A"),5)+"| ";
				set[3]=CMStrings.padRight(L("NAMELESS"),17)+"| ";
			}
			set[4]=CMStrings.padRight(S.getAddress(),17)+"| ";
			set[5]=CMStrings.padRight(CMLib.english().returnTime(S.getIdleMillis(),0)+"",17);
			broken.add(set);
		}
		Vector<String[]> sorted=null;
		int sortNum=-1;
		if(sort.length()>0)
		{
			if("STATUS".startsWith(sort))
				sortNum=1;
			else
			if("VALID".startsWith(sort))
				sortNum=2;
			else
			if(("NAME".startsWith(sort))||("PLAYER".startsWith(sort)))
				sortNum=3;
			else
			if(("IP".startsWith(sort))||("ADDRESS".startsWith(sort)))
				sortNum=4;
			else
			if(("IDLE".startsWith(sort))||("MILLISECONDS".startsWith(sort)))
				sortNum=5;
		}
		if(sortNum<0)
			sorted=broken;
		else
		{
			sorted=new Vector<String[]>();
			while(broken.size()>0)
			{
				int selected=0;
				for(int s=1;s<broken.size();s++)
				{
					final String[] S=broken.get(s);
					if(S[sortNum].compareToIgnoreCase(broken.get(selected)[sortNum])<0)
						selected=s;
				}
				sorted.add(broken.get(selected));
				broken.remove(selected);
			}
		}
		for(int s=0;s<sorted.size();s++)
		{
			final String[] S=sorted.get(s);
			for (final String element : S)
				lines.append(element);
			lines.append("\n\r");
		}
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(lines.toString());
	}

	public void archonlist(MOB mob, List<String> commands)
	{
		if(commands.size()==0)
		{
			mob.tell(L("List what?"));
			return;
		}

		final Session s=mob.session();
		if(s==null)
			return;

		final String listWord=commands.get(0).toUpperCase();
		String rest=(commands.size()>1)?rest=CMParms.combine(commands,1):"";
		ListCmdEntry code=getMyCmd(mob, listWord);
		if((code==null)||(listWord.length()==0))
		{
			final List<String> V=getMyCmdWords(mob);
			if(V.size()==0)
				mob.tell(L("You are not allowed to use this command!"));
			else
			{
				final StringBuilder str=new StringBuilder("");
				for(int v=0;v<V.size();v++)
				{
					if(V.get(v).length()>0)
					{
						str.append(V.get(v));
						if(v==(V.size()-2))
							str.append(L(", or "));
						else
						if(v<(V.size()-1))
							str.append(", ");
					}
				}
				mob.tell(L("You cannot list '@x1'.  Try @x2.",listWord,str.toString()));
			}
			return;
		}
		switch(code)
		{
		case COMMANDS:
			listCommands(mob,commands);
			break;
		case UNLINKEDEXITS:
			s.wraplessPrintln(unlinkedExits(mob.session(), commands));
			break;
		case ITEMS:
			s.println("^HBasic Item IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.basicItems()).toString());
			break;
		case ARMOR:
			s.println("^HArmor Item IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.armor()).toString());
			break;
		case ENVRESOURCES:
			s.wraplessPrintln(listEnvResources(mob.session(), rest));
			break;
		case WEAPONS:
			s.println("^HWeapon Item IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.weapons()).toString());
			break;
		case MOBS:
			s.println("^HMOB IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.mobTypes()).toString());
			break;
		case ROOMS:
			s.println("^HRoom Locale IDs:^N");
			s.wraplessPrintln(roomDetails(mob.session(), mob.location().getArea().getMetroMap(), mob.location()).toString());
			break;
		case AREA:
			s.println("^HArea IDs:^N");
			s.wraplessPrintln(roomTypes(mob, mob.location().getArea().getMetroMap(), mob.location(), commands).toString());
			break;
		case LOCALES:
			s.println("^HRoom Class Locale IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.locales()).toString());
			s.println("^HDomain Locales include the following:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, new IteratorEnumeration<String>(Arrays.asList(CMParms.combine(Room.DOMAIN_INDOORS_DESCS, Room.DOMAIN_OUTDOOR_DESCS)).iterator())).toString());
			break;
		case BEHAVIORS:
			//s.println("^HBehavior IDs:^N");
			listBehaviors(mob,s,commands,"Behaviors");
			break;
		case EXITS:
			s.println("^HExit IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.exits()).toString());
			break;
		case RACES:
			s.println("^HRace IDs (Racial Category):^N");
			s.wraplessPrintln(listRaces(s, CMClass.races(), rest).toString());
			break;
		case CLASSES:
			s.println("^HCharacter Class IDs:^N");
			s.wraplessPrintln(listCharClasses(s, CMClass.charClasses(), commands).toString());
			break;
		case STAFF:
			s.wraplessPrintln(listSubOps(mob.session()).toString());
			break;
		case ABILITIES:
			listAbilities(mob,s,commands,"",Ability.ALL_ACODES);
			break;
		case SPELLS:
			listAbilities(mob,s,commands,"Spell",Ability.ACODE_SPELL);
			break;
		case SONGS:
			listAbilities(mob,s,commands,"Song",Ability.ACODE_SONG);
			break;
		case PRAYERS:
			listAbilities(mob,s,commands,"Prayer",Ability.ACODE_PRAYER);
			break;
		case PROPERTIES:
			//s.println("^HProperty Ability IDs:^N");
			listAbilities(mob,s,commands,"Properties",Ability.ACODE_PROPERTY);
			break;
		case THIEFSKILLS:
			listAbilities(mob,s,commands,"Thief Skill",Ability.ACODE_THIEF_SKILL);
			break;
		case COMMON:
			listAbilities(mob,s,commands,"Common Skill",Ability.ACODE_COMMON_SKILL);
			break;
		case JOURNALS:
			s.println(listJournals(mob.session()).toString());
			break;
		case COMMANDJOURNALS:
			s.println(listCommandJournals(mob.session()).toString());
			break;
		case SKILLS:
			listAbilities(mob,s,commands,"Skill",Ability.ACODE_SKILL);
			break;
		case QUESTS:
			s.println(listQuests(mob.session()).toString());
			break;
		case QUESTNAMES:
			s.println(listQuestNames(mob.session()).toString());
			break;
		case QUESTWINNERS:
			s.println(listQuestWinners(mob.session(),rest).toString());
			break;
		case DISEASES:
			listAbilities(mob,s,commands,"Disease",Ability.ACODE_DISEASE);
			break;
		case POISONS:
			listAbilities(mob,s,commands,"Poison",Ability.ACODE_POISON);
			break;
		case LANGUAGES:
			listAbilities(mob,s,commands,"Language",Ability.ACODE_LANGUAGE);
			break;
		case TICKS:
			s.println(listTicks(mob.session(), CMParms.combine(commands, 1)).toString());
			break;
		case MAGIC:
			s.println("^HMagic Item IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.miscMagic()).toString());
			break;
		case TECH:
			s.println("^HTech Item IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.tech()).toString());
			break;
		case CLANITEMS:
			s.println("^HClan Item IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.clanItems()).toString());
			break;
		case AREATYPES:
			s.println("^HArea Type IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.areaTypes()).toString());
			break;
		case COMMANDJOURNAL:
			s.println(journalList(mob,mob.session(), listWord, rest).toString());
			break;
		case REALESTATE:
			s.wraplessPrintln(roomPropertyDetails(mob.session(), mob.location().getArea(), rest).toString());
			break;
		case FILEUSE:
			listFileUse(mob, s, CMParms.combine(commands, 1));
			break;
		case NOPURGE:
		{
			final StringBuilder str=new StringBuilder("\n\rProtected players:\n\r");
			final List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
			if((protectedOnes!=null)&&(protectedOnes.size()>0))
			for(int b=0;b<protectedOnes.size();b++)
				str.append((b+1)+") "+(protectedOnes.get(b))+"\n\r");
			s.wraplessPrintln(str.toString());
			break;
		}
		case BANNED:
		{
			final StringBuilder str=new StringBuilder("\n\rBanned names/ips:\n\r");
			final List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
			if((banned!=null)&&(banned.size()>0))
			for(int b=0;b<banned.size();b++)
				str.append((b+1)+") "+(banned.get(b))+"\n\r");
			s.wraplessPrintln(str.toString());
			break;
		}
		case RACECATS:
			s.println("^HRacial Categories:^N");
			s.wraplessPrintln(listRaceCats(s, CMClass.races(), commands).toString());
			break;
		case LOG:
			listLog(mob, commands);
			break;
		case USERS:
			listUsers(mob.session(), mob, commands);
			break;
		case LINKAGES:
			s.println(listLinkages(mob.session(), mob, rest).toString());
			break;
		case REPORTS:
			s.println(listReports(mob.session(), mob).toString());
			break;
		case THREAD:
			s.println(listThread(mob.session(), mob, rest).toString());
			break;
		case THREADS:
			s.println(listThreads(mob.session(), mob, 
					CMParms.containsIgnoreCase(commands, "SHORT")||CMParms.containsIgnoreCase(commands, "ACTIVE"), 
					CMParms.containsIgnoreCase(commands, "EXTEND")).toString());
			break;
		case RESOURCES:
			s.println(listResources(mob, CMParms.combine(commands, 1)));
			break;
		case ONEWAYDOORS:
			s.wraplessPrintln(reallyFindOneWays(mob.session(), commands));
			break;
		case CHANTS:
			listAbilities(mob,s,commands,"Chant",Ability.ACODE_CHANT);
			break;
		case SUPERPOWERS:
			listAbilities(mob,s,commands,"Super Power",Ability.ACODE_SUPERPOWER);
			break;
		case COMPONENTS:
			s.wraplessPrintln(listComponents(mob.session()));
			break;
		case EXPERTISES:
			s.wraplessPrintln(listExpertises(mob.session(),commands));
			break;
		case SOCIALS:
			s.wraplessPrintln(listSocials(mob.session(),commands));
			break;
		case FACTIONS:
			s.wraplessPrintln(CMLib.factions().listFactions());
			break;
		case MATERIALS:
			s.wraplessPrintln(listMaterials());
			break;
		case OBJCOUNTERS:
			s.println("\n\r^xCounter Report: NO LONGER AVAILABLE^.^N\n\r");
			break;// +CMClass.getCounterReport()); break;
		case POLLS:
			listPolls(mob, commands);
			break;
		case CONTENTS:
			s.wraplessPrintln(listContent(mob, commands).toString());
			break;
		case EXPIRES:
			s.wraplessPrintln(roomExpires(mob.session(), mob.location().getArea().getProperMap(), mob.location()).toString());
			break;
		case TITLES:
			s.wraplessPrintln(listTitles(mob.session()));
			break;
		case ACHIEVEMENTS:
			s.wraplessPrintln(listAchievements(mob.session()));
			break;
		case AREARESOURCES:
			s.wraplessPrintln(roomResources(mob.session(), mob.location().getArea().getMetroMap(), mob.location()).toString());
			break;
		case CONQUERED:
			s.wraplessPrintln(areaConquests(mob.session(), CMLib.map().areas()).toString());
			break;
		case HOLIDAYS:
			s.wraplessPrintln(CMLib.quests().listHolidays(mob.location().getArea(), CMParms.combine(commands, 1)));
			break;
		case RECIPES:
			s.wraplessPrintln(listRecipes(mob, CMParms.combine(commands, 1)));
			break;
		case HELPFILEREQUESTS:
			s.wraplessPrint(listHelpFileRequests(mob, CMParms.combine(commands, 1)));
			break;
		case SCRIPTS:
			s.wraplessPrintln(listScripts(mob.session(), mob, commands).toString());
			break;
		case ACCOUNTS:
			listAccounts(mob.session(), mob, commands);
			break;
		case GOVERNMENTS:
			s.wraplessPrintln(listClanGovernments(mob.session(), commands));
			break;
		case CLANS:
			s.wraplessPrintln(listClans(mob.session(), commands));
			break;
		case DEBUGFLAG:
			s.println("\n\r^xDebug Settings: ^?^.^N\n\r" + CMParms.toListString(new XVector<CMSecurity.DbgFlag>(CMSecurity.getDebugEnum())) + "\n\r");
			break;
		case DISABLEFLAG:
			s.println("\n\r^xDisable Settings: ^?^.^N\n\r" + CMParms.toListString(new XVector<Object>(CMSecurity.getDisablesEnum())) + "\n\r");
			break;
		case ENABLEFLAG:
			s.println("\n\r^xEnable Settings: ^?^.^N\n\r" + CMParms.toListString(new XVector<Object>(CMSecurity.getEnablesEnum())) + "\n\r");
			break;
		case ALLQUALIFYS:
			s.wraplessPrintln(listAllQualifies(mob.session(), commands).toString());
			break;
		case NEWS:
			listNews(mob, commands);
			break;
		case AREAS:
			listAreas(mob, commands, mundaneAreaFilter);
			break;
		case TIMEZONES:
			listTimeZones(mob, commands, allAreaFilter);
			break;
		case SESSIONS:
			listSessions(mob, commands);
			break;
		case WORLD:
			listAreas(mob, commands, new WorldFilter(mob.location()));
			break;
		case PLANETS:
			listAreas(mob, commands, planetsAreaFilter);
			break;
		case SPACESHIPAREAS:
			listAreas(mob, commands, spaceShipsAreaFilter);
			break;
		case GENSTATS:
			listStats(mob, commands);
			break;
		case CURRENTS:
			listCurrents(mob, commands);
			break;
		case MANUFACTURERS:
			listManufacturers(mob, commands);
			break;
		case TECHSKILLS:
			listAbilities(mob,s,commands,"Tech Skill",Ability.ACODE_TECH);
			break;
		case SOFTWARE:
			s.println("^HSoftware Item IDs:^N");
			s.wraplessPrintln(CMLib.lister().reallyList(mob, CMClass.tech(new Filterer<Technical>()
			{
				@Override
				public boolean passesFilter(Technical obj)
				{
					return obj instanceof Software;
				}
			})).toString());
			break;
		case EXPIRED:
			s.wraplessPrintln(listExpired(mob));
			break;
		case SPACE:
			s.wraplessPrintln(listSpace(mob, commands).toString());
			break;
		case SQL:
			listSql(mob, rest);
			break;
		case SHIPS:
			listShips(mob, commands);
			break;
		case ABILITYDOMAINS:
		{
			WikiFlag wiki=getWikiFlagRemoved(commands);
			if(wiki == WikiFlag.NO)
				s.wraplessPrintln(CMParms.toListString(Ability.DOMAIN_DESCS));
			else
			{
				for(String domain : Ability.DOMAIN_DESCS)
				{
					domain = domain.toLowerCase();
					String domainName = CMStrings.capitalizeAllFirstLettersAndLower(domain.replaceAll("_"," "));
					s.println("[["+domain+"(Domain)|"+domainName+"]]");
				}
			}
			break;
		}
		case ABILITYCODES:
			s.wraplessPrintln(CMParms.toListString(Ability.ACODE_DESCS_));
			break;
		case ABILITYFLAGS:
			s.wraplessPrintln(CMParms.toListString(Ability.FLAG_DESCS));
			break;
		}
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		List<Environmental> V=new Vector<Environmental>();
		commands.remove(0);
		String forWhat=null;
		if(commands.size()==0)
		{
			if(getAnyCmd(mob)!=null)
			{
				archonlist(mob,commands);
				return false;
			}
			V=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
		}
		else
		{
			final Vector<String> origCommands=new XVector<String>(commands);
			for(int c=commands.size()-2;c>=0;c--)
			{
				if(commands.get(c).equalsIgnoreCase("for"))
				{
					forWhat=CMParms.combine(commands,c+1);
					for(int c1=commands.size()-1;c1>=c;c1--)
						commands.remove(c1);
					break;
				}
			}
			final String what=CMParms.combine(commands,0);
			final List<Environmental> V2=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
			Environmental shopkeeper=CMLib.english().fetchEnvironmental(V2,what,false);
			if((shopkeeper==null)&&(what.equals("shop")||what.equals("the shop")))
			{
				for(int v=0;v<V2.size();v++)
				{
					if(V2.get(v) instanceof Area)
					{
						shopkeeper = V2.get(v);
						break;
					}
				}
			}
			if((shopkeeper!=null)
			&&(CMLib.coffeeShops().getShopKeeper(shopkeeper)!=null)
			&&(CMLib.flags().canBeSeenBy(shopkeeper,mob)))
				V.add(shopkeeper);
			else
			if(getAnyCmd(mob)!=null)
			{
				archonlist(mob,origCommands);
				return false;
			}
		}
		if(V.size()==0)
		{
			mob.tell(L("You don't see anyone here buying or selling."));
			return false;
		}
		for(int i=0;i<V.size();i++)
		{
			final Environmental shopkeeper=V.get(i);
			final ShopKeeper SHOP=CMLib.coffeeShops().getShopKeeper(shopkeeper);
			String str=L("<S-NAME> review(s) <T-YOUPOSS> inventory");
			if(SHOP instanceof Banker)
				str=L("<S-NAME> review(s) <S-HIS-HER> account with <T-NAMESELF>");
			else
			if(SHOP instanceof PostOffice)
				str=L("<S-NAME> check(s) <S-HIS-HER> postal box with <T-NAMESELF>");
			else
			if(SHOP instanceof Librarian)
				str=L("<S-NAME> review(s) <T-YOUPOSS> catalog");
			if(forWhat!=null)
				str+=L(" for '@x1'",forWhat);
			str+=".";
			final CMMsg newMsg=CMClass.getMsg(mob,shopkeeper,null,CMMsg.MSG_LIST,str);
			if(!mob.location().okMessage(mob,newMsg))
				return false;
			mob.location().send(mob,newMsg);
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
