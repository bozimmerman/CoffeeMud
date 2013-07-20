package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.*;
import com.planet_ink.miniweb.interfaces.HTTPRequest;

import java.util.*;
import java.util.concurrent.TimeUnit;

/*
   Copyright 2000-2013 Bo Zimmerman

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
public class ListCmd extends StdCommand
{
	public ListCmd(){}

	private final String[] access={"LIST"};
	public String[] getAccessWords(){return access;}

	protected static class WorldFilter implements Filterer<Area>
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
		public boolean passesFilter(Area obj) {
			return (obj.getTimeObj()==to);
		}
	}
	
	protected Filterer<Area> planetsFilter=new Filterer<Area>() {
		@Override
		public boolean passesFilter(Area obj) {
			return (obj instanceof SpaceObject) && (!(obj instanceof SpaceShip));
		}
	};
	
	protected Filterer<Area> areaFilter=new Filterer<Area>() {
		@Override
		public boolean passesFilter(Area obj) {
			return !(obj instanceof SpaceObject);
		}
	};
	
	protected Filterer<Area> shipFilter=new Filterer<Area>() {
		@Override
		public boolean passesFilter(Area obj) {
			return (obj instanceof SpaceShip);
		}
	};
	
	public StringBuilder listAllQualifies(Session viewerS, Vector cmds)
	{
		StringBuilder str=new StringBuilder("");
		Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		str.append("<<EACH CLASS>>\n\r");
		Map<String,AbilityMapper.AbilityMapping> subMap=map.get("EACH");
		str.append(CMStrings.padRight("Skill ID", ListingLibrary.ColFixer.fixColWidth(20.0,viewerS)));
		str.append(CMStrings.padRight("Lvl", ListingLibrary.ColFixer.fixColWidth(4.0,viewerS)));
		str.append(CMStrings.padRight("Gain", ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight("Prof", ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight("Mask", ListingLibrary.ColFixer.fixColWidth(40.0,viewerS)));
		str.append("\n\r");
		for(AbilityMapper.AbilityMapping mapped : subMap.values())
		{
			str.append(CMStrings.padRight(mapped.abilityID, ListingLibrary.ColFixer.fixColWidth(20.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.qualLevel, ListingLibrary.ColFixer.fixColWidth(4.0,viewerS)));
			str.append(CMStrings.padRight(mapped.autoGain?"yes":"no", ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.defaultProficiency, ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(mapped.extraMask, ListingLibrary.ColFixer.fixColWidth(40.0,viewerS)));
			str.append("\n\r");
		}
		str.append("\n\r");
		str.append("<<ALL CLASSES>>\n\r");
		subMap=map.get("ALL");
		str.append(CMStrings.padRight("Skill ID", ListingLibrary.ColFixer.fixColWidth(20.0,viewerS)));
		str.append(CMStrings.padRight("Lvl", ListingLibrary.ColFixer.fixColWidth(4.0,viewerS)));
		str.append(CMStrings.padRight("Gain", ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight("Prof", ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
		str.append(CMStrings.padRight("Mask", ListingLibrary.ColFixer.fixColWidth(40.0,viewerS)));
		str.append("\n\r");
		for(AbilityMapper.AbilityMapping mapped : subMap.values())
		{
			str.append(CMStrings.padRight(mapped.abilityID, ListingLibrary.ColFixer.fixColWidth(20.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.qualLevel, ListingLibrary.ColFixer.fixColWidth(4.0,viewerS)));
			str.append(CMStrings.padRight(mapped.autoGain?"yes":"no", ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(""+mapped.defaultProficiency, ListingLibrary.ColFixer.fixColWidth(5.0,viewerS)));
			str.append(CMStrings.padRight(mapped.extraMask, ListingLibrary.ColFixer.fixColWidth(40.0,viewerS)));
			str.append("\n\r");
		}
		return str;
	}

	public StringBuilder roomDetails(Session viewerS, Vector these, Room likeRoom)
	{return roomDetails(viewerS,these.elements(),likeRoom);}

	public StringBuilder roomDetails(Session viewerS, Enumeration these, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		Room thisThang=null;
		String thisOne=null;
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(31.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(43.0,viewerS);
		for(Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Room)r.nextElement();
			thisOne=thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
				lines.append(CMStrings.padRightPreserve("^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",COL_LEN1)+": "+CMStrings.limit(thisThang.displayText(),COL_LEN2)+"\n\r");
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder roomExpires(Session viewerS, Enumeration these, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("The time is: "+CMLib.time().date2String(System.currentTimeMillis())+"\n\r\n\r");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		Room thisThang=null;
		String thisOne=null;
		for(Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Room)r.nextElement();
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
	
	public StringBuilder roomPropertyDetails(Session viewerS, Enumeration these, String owner)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		LandTitle t=null;
		Room thisThang=null;
		String thisOne=null;
		for(Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Room)r.nextElement();
			t=CMLib.law().getLandTitle(thisThang);
			if(t!=null)
			{
				thisOne=thisThang.roomID();
				if((thisOne.length()>0)&&((owner==null)||(t.getOwnerName().equalsIgnoreCase(owner))))
					lines.append(CMStrings.padRightPreserve("^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",30)+": "+CMStrings.limit(thisThang.displayText(),23)+CMStrings.limit(" ("+t.getOwnerName()+", $"+t.getPrice()+")",20)+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}
	public StringBuilder roomTypes(Session viewerS, Vector these, Room likeRoom)
	{return roomTypes(viewerS, these.elements(),likeRoom);}
	public StringBuilder roomTypes(Session viewerS, Enumeration these, Room likeRoom)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		Room thisThang=null;
		String thisOne=null;
		for(Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Room)r.nextElement();
			thisOne=thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
				lines.append(CMStrings.padRightPreserve(thisOne,30)+": "+thisThang.ID()+"\n\r");
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder roomResources(Session viewerS, Vector these, Room likeRoom)
	{return roomResources(viewerS, these.elements(),likeRoom);}
	public StringBuilder roomResources(Session viewerS, Enumeration these, Room likeRoom)
	{
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(30.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(15.0,viewerS);
		StringBuilder lines=new StringBuilder(CMStrings.padRight("Room ID#",COL_LEN1)+"| "
										   +CMStrings.padRight("Room Type",COL_LEN2)+"| "
										   +"Resource\n\r");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		Room thisThang=null;
		String thisOne=null;
		for(Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Room)r.nextElement();
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

	public StringBuilder areaConquests(Session viewerS, Enumeration these)
	{
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(26.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(40.0,viewerS);
		StringBuilder lines=new StringBuilder(CMStrings.padRight("Area",COL_LEN1)+"| "
										   +CMStrings.padRight("Clan",COL_LEN2)+"| "
										   +"Controlled\n\r");
		if(!these.hasMoreElements()) return lines;
		Area thisThang=null;
		String thisOne=null;
		for(Enumeration r=these;r.hasMoreElements();)
		{
			thisThang=(Area)r.nextElement();
			thisOne=thisThang.name();
			if(thisOne.length()>0)
			{
				lines.append(CMStrings.padRight(thisOne,COL_LEN1)+": ");
				String controller="The Archons";
				String fully="";
				LegalBehavior law=CMLib.law().getLegalBehavior(thisThang);
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

	public void dumpThreadGroup(Session viewerS, StringBuilder lines,ThreadGroup tGroup, boolean ignoreZeroTickThreads)
	{
		int ac = tGroup.activeCount();
		int agc = tGroup.activeGroupCount();
		Thread tArray[] = new Thread [ac+1];
		ThreadGroup tgArray[] = new ThreadGroup [agc+1];

		tGroup.enumerate(tArray,false);
		tGroup.enumerate(tgArray,false);

		lines.append(" ^HTGRP^?  ^H" + tGroup.getName() + "^?\n\r");
		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null)
			{
				if((ignoreZeroTickThreads)&&(!tArray[i].isAlive()))
					continue;
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
						summary="session "+mobName+": "+S.getStatus().toString()+": "+CMParms.combineWithQuotes(S.getPreviousCMD(),0);
					}
					else
					if(R instanceof CMRunnable)
						summary=CMClass.classID(R)+": active for "+((CMRunnable)R).activeTimeMillis()+"ms";
					else
					if(CMClass.classID(R).length()>0)
						summary=CMClass.classID(R);
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
					dumpThreadGroup(viewerS,lines,tgArray[i],ignoreZeroTickThreads);
			}
			lines.append("}\n\r");
		}
	}


	public StringBuilder listThreads(Session viewerS, MOB mob, boolean ignoreZeroTickThreads)
	{
		StringBuilder lines=new StringBuilder("^xStatus|Name                 ^.^?\n\r");
		try
		{
			ThreadGroup topTG = Thread.currentThread().getThreadGroup();
			while (topTG != null && topTG.getParent() != null)
				topTG = topTG.getParent();
			if (topTG != null)
				dumpThreadGroup(viewerS,lines,topTG,ignoreZeroTickThreads);

		}
		catch (Exception e)
		{
			lines.append ("\n\rBastards! Exception while listing threads: " + e.getMessage() + "\n\r");
		}
		return lines;

	}
	
	public void addScripts(DVector DV, Room R, ShopKeeper SK, MOB M, Item I, PhysicalAgent E)
	{
		if(E==null) return;
		for(Enumeration<Behavior> e=E.behaviors();e.hasMoreElements();)
		{
			Behavior B=e.nextElement();
			if(B instanceof ScriptingEngine)
			{
				java.util.List<String> files=B.externalFiles();
				if(files != null)
					for(int f=0;f<files.size();f++)
						DV.addElement(files.get(f),E,R,M,I,B);
				String nonFiles=((ScriptingEngine)B).getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
				if(nonFiles.trim().length()>0)
					DV.addElement("*Custom*"+nonFiles.trim(),E,R,M,I,B);
			}
		}
		for(Enumeration<ScriptingEngine> e=E.scripts();e.hasMoreElements();)
		{
			ScriptingEngine SE=e.nextElement();
			java.util.List<String> files=SE.externalFiles();
			if(files != null)
				for(int f=0;f<files.size();f++)
					DV.addElement(files.get(f),E,R,M,I,SE);
			String nonFiles=SE.getVar("*","COFFEEMUD_SYSTEM_INTERNAL_NONFILENAME_SCRIPT");
			if(nonFiles.trim().length()>0)
				DV.addElement("*Custom*"+nonFiles.trim(),E,R,M,I,SE);
		}
	}
	
	public void addShopScripts(DVector DV, Room R, MOB M, Item I, Environmental E)
	{
		if(E==null) return;
		ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(E);
		if(SK!=null)
		{
			for(Iterator<Environmental> i=SK.getShop().getStoreInventory();i.hasNext();)
			{
				Environmental E2=i.next();
				if(E2 instanceof PhysicalAgent)
					addScripts(DV,R,SK,M,I,(PhysicalAgent)E2);
			}
		}
	}
	
	public StringBuilder listScripts(Session viewerS, MOB mob, Vector cmds)
	{
		if(cmds.size()==0) return new StringBuilder("");
		cmds.removeElementAt(0);
		if(cmds.size()==0)
			return new StringBuilder("List what script details? Try LIST SCRIPTS (COUNT/DETAILS/CUSTOM)");
		String rest=CMParms.combine(cmds,0);
		DVector scriptTree=new DVector(6);
		Area A=null;
		Room R=null;
		WorldMap.LocatedPair LP=null;
		PhysicalAgent AE=null;
		for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
		{
			A=(Area)e.nextElement(); if(A==null) continue;
			for(Enumeration<WorldMap.LocatedPair> ae=CMLib.map().scriptHosts(A);ae.hasMoreElements();)
			{
				LP=ae.nextElement(); if(LP==null) continue;
				AE=LP.obj(); if(AE==null) continue;
				R=LP.room(); if(R==null) R=CMLib.map().getStartRoom(AE);
				
				if((AE instanceof Area)||(AE instanceof Exit))
				{
					if(R==null) R=A.getRandomProperRoom();
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
					ItemPossessor IP=((Item)AE).owner();
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
			final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(50.0,viewerS);
			final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(5.0,viewerS);
			lines=new StringBuilder("^x")
			.append(CMStrings.padRight("Script File",COL_LEN1))
			.append(CMStrings.padRight("Usage",COL_LEN2))
			.append("^.^N\n\r");
			scriptTree.sortBy(1);
			if(scriptTree.size()>0)
			{
				String lastOne=(String)scriptTree.elementAt(0,1);
				if(lastOne.startsWith("*Custom*")) lastOne="*Custom*";
				int counter=1;
				for(int d=1;d<scriptTree.size();d++)
				{
					String scriptFilename=(String)scriptTree.elementAt(d,1);
					if(scriptFilename.startsWith("*Custom*")) scriptFilename="*Custom*";
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
			final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(30.0,viewerS);
			final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(20.0,viewerS);
			final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
			lines=new StringBuilder("^x")
			.append(CMStrings.padRight("Script File",COL_LEN1))
			.append(CMStrings.padRight("Host",COL_LEN2))
			.append(CMStrings.padRight("Location",COL_LEN3))
			.append("^.^N\n\r");
			scriptTree.sortBy(1);
			if(scriptTree.size()>0)
			{
				for(int d=0;d<scriptTree.size();d++)
				{
					String scriptFilename=(String)scriptTree.elementAt(d,1);
					Environmental host=(Environmental)scriptTree.elementAt(d,2);
					Room room=(Room)scriptTree.elementAt(d,3);
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
					String scriptFilename=(String)scriptTree.elementAt(d,1);
					if(scriptFilename.startsWith("*Custom*"))
					{
						Environmental host=(Environmental)scriptTree.elementAt(d,2);
						Room room=(Room)scriptTree.elementAt(d,3);
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
		Faction useFaction=null;
		for(Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
		{
			Faction F=e.nextElement();
			if(F.showInSpecialReported()) useFaction=F;
		}
		StringBuilder buf=new StringBuilder("Links: \n\r");
		List<List<Area>> areaLinkGroups=new Vector<List<Area>>();
		Enumeration<Area> a;
		if(rest.equalsIgnoreCase("world"))
			a=CMLib.map().sortedAreas();
		else
			a=new XVector<Area>(mob.location().getArea()).elements();
		for(;a.hasMoreElements();)
		{
			Area A=a.nextElement();
			buf.append(A.name()+"\t"+A.numberOfProperIDedRooms()+" rooms\t");
			if(!A.getProperMap().hasMoreElements())
			{
				buf.append("\n\r");
				continue;
			}
			List<List<Room>> linkedGroups=new Vector();
			int numMobs=0;
			int totalAlignment=0;
			int totalLevels=0;
			for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
				{
					List<Room> myVec=null;
					List<Room> clearVec=null;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Room R2=R.rawDoors()[d];
						if(R2!=null)
						{
							for(int g=0;g<linkedGroups.size();g++)
							{
								List<Room> G=linkedGroups.get(g);
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
							clearVec=new Vector();
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

				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((M!=null)
					&&(M.isMonster())
					&&(M.getStartRoom()!=null)
					&&(M.getStartRoom().getArea()==R.getArea()))
					{
						numMobs++;
						if((useFaction!=null)
						&&(CMLib.factions().getFaction(useFaction.factionID())!=null)
						&&(M.fetchFaction(useFaction.factionID())!=Integer.MAX_VALUE))
							totalAlignment+=M.fetchFaction(useFaction.factionID());
						totalLevels+=M.phyStats().level();
					}
				}

			}
			StringBuilder ext=new StringBuilder("links ");
			List<Area> myVec=null;
			List<Area> clearVec=null;
			for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R2=R.rawDoors()[d];
					if((R2!=null)&&(R2.getArea()!=R.getArea()))
					{
						ext.append(Directions.getDirectionName(d)+" to "+R2.getArea().name()+" ("+R.roomID()+"/"+R2.roomID()+") ");
						for(int g=0;g<areaLinkGroups.size();g++)
						{
							List<Area> G=areaLinkGroups.get(g);
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
					clearVec=new Vector();
					clearVec.add(A);
					areaLinkGroups.add(clearVec);
				}
			}
			if(numMobs>0)
				buf.append(numMobs+" mobs\t"+(totalLevels/numMobs)+" avg levels\t");
			if((numMobs>0)&&(useFaction!=null)&&(CMLib.factions().getFaction(useFaction.factionID())!=null))
				buf.append((totalAlignment/numMobs)+" avg "+useFaction.name());
			if(linkedGroups.size()>0)
			{
				buf.append("\tgroups: "+linkedGroups.size()+" sizes: ");
				for(List<Room> grp : linkedGroups)
					buf.append(grp.size()+" ");
			}
			buf.append("\t"+ext.toString()+"\n\r");
		}
		buf.append("There were "+areaLinkGroups.size()+" area groups:");
		for(int g=areaLinkGroups.size()-1;g>=0;g--)
		{
			if(areaLinkGroups.get(g).size()==0)
				areaLinkGroups.remove(g);
		}
		StringBuilder unlinkedGroups=new StringBuilder("");
		for(List<Area> V : areaLinkGroups)
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
		buf.append("Small Group Areas:\t"+unlinkedGroups.toString());
		Log.sysOut("Lister",buf.toString());
		return buf;
	}


	public StringBuilder journalList(Session viewerS, String partialjournal)
	{
		StringBuilder buf=new StringBuilder("");
		String journal=null;
		for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMJ.NAME()+"S").startsWith(partialjournal.toUpperCase().trim()))
				journal=CMJ.NAME().trim();
		}
		if(journal==null) return buf;
		List<JournalsLibrary.JournalEntry> V=CMLib.database().DBReadJournalMsgs("SYSTEM_"+journal+"S");
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(3.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(10.0,viewerS);
		if(V!=null)
		{
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1+2)+CMStrings.padRight("From",COL_LEN2)+" Entry^.^N\n\r");
			buf.append("---------------------------------------------\n\r");
			for(int j=0;j<V.size();j++)
			{
				JournalsLibrary.JournalEntry entry=V.get(j);
				String from=entry.from;
				String message=entry.msg;
				buf.append(CMStrings.padRight((j+1)+"",COL_LEN1)+") "+CMStrings.padRight(from,COL_LEN2)+" "+message+"\n\r");
			}
		}
		return buf;
	}

	public StringBuilder listReports(Session viewerS, MOB mob)
	{
		mob.tell("\n\r^xCoffeeMud System Report:^.^N");
		try
		{
			System.gc();
			Thread.sleep(1500);
		}catch(Exception e){}
		StringBuilder buf=new StringBuilder("");
		long totalTime=System.currentTimeMillis()-CMSecurity.getStartTime();
		buf.append("The system has been running for ^H"+CMLib.english().returnTime(totalTime,0)+"^?.\n\r");
		long free=Runtime.getRuntime().freeMemory()/1000;
		long total=Runtime.getRuntime().totalMemory()/1000;
		buf.append("The system is utilizing ^H"+(total-free)+"^?kb out of ^H"+total+"^?kb.\n\r");
		buf.append("\n\r^xTickables report:^.^N\n\r");
		String totalTickers=CMLib.threads().systemReport("totalTickers");
		String tickGroupSize=CMLib.threads().systemReport("TICKGROUPSIZE");
		long totalMillis=CMath.s_long(CMLib.threads().systemReport("totalMillis"));
		long totalTicks=CMath.s_long(CMLib.threads().systemReport("totalTicks"));
		buf.append("There are ^H"+totalTickers+"^? ticking objects in ^H"+tickGroupSize+"^? groups.\n\r");
		buf.append("The ticking objects have consumed: ^H"+CMLib.english().returnTime(totalMillis,totalTicks)+"^?.\n\r");
		/*
		String topGroupNumber=CMLib.threads().systemReport("topGroupNumber");
		long topGroupMillis=CMath.s_long(CMLib.threads().systemReport("topGroupMillis"));
		long topGroupTicks=CMath.s_long(CMLib.threads().systemReport("topGroupTicks"));
		long topObjectMillis=CMath.s_long(CMLib.threads().systemReport("topObjectMillis"));
		long topObjectTicks=CMath.s_long(CMLib.threads().systemReport("topObjectTicks"));
		buf.append("The most active group, #^H"+topGroupNumber+"^?, has consumed: ^H"+CMLib.english().returnTime(topGroupMillis,topGroupTicks)+"^?.\n\r");
		String topObjectClient=CMLib.threads().systemReport("topObjectClient");
		String topObjectGroup=CMLib.threads().systemReport("topObjectGroup");
		if(topObjectClient.length()>0)
		{
			buf.append("The most active object has been '^H"+topObjectClient+"^?', from group #^H"+topObjectGroup+"^?.\n\r");
			buf.append("That object has consumed: ^H"+CMLib.english().returnTime(topObjectMillis,topObjectTicks)+"^?.\n\r");
		}
		*/
		buf.append("\n\r");
		buf.append("^xServices report:^.^N\n\r");
		buf.append("There are ^H"+CMLib.threads().systemReport("numactivethreads")+"^? active out of ^H"+CMLib.threads().systemReport("numthreads")+"^? live worker threads.\n\r");
		int threadNum=0;
		String threadName=CMLib.threads().systemReport("Thread"+threadNum+"name");
		while(threadName.trim().length()>0)
		{
			long saveThreadMilliTotal=CMath.s_long(CMLib.threads().systemReport("Thread"+threadNum+"MilliTotal"));
			long saveThreadTickTotal=CMath.s_long(CMLib.threads().systemReport("Thread"+threadNum+"TickTotal"));
			buf.append("Service '"+threadName+"' has consumed: ^H"+CMLib.english().returnTime(saveThreadMilliTotal,saveThreadTickTotal)+" ("+CMLib.threads().systemReport("Thread"+threadNum+"Status")+")^?.");
			buf.append("\n\r");
			threadNum++;
			threadName=CMLib.threads().systemReport("Thread"+threadNum+"name");
		}
		buf.append("\n\r");
		buf.append("^xSession report:^.^N\n\r");
		long totalMOBMillis=CMath.s_long(CMLib.threads().systemReport("totalMOBMillis"));
		long totalMOBTicks=CMath.s_long(CMLib.threads().systemReport("totalMOBTicks"));
		buf.append("There are ^H"+CMLib.sessions().getCountLocalOnline()+"^? ticking players logged on.\n\r");
		buf.append("The ticking players have consumed: ^H"+CMLib.english().returnTime(totalMOBMillis,totalMOBTicks)+"^?.\n\r");
		/*
		long topMOBMillis=CMath.s_long(CMLib.threads().systemReport("topMOBMillis"));
		long topMOBTicks=CMath.s_long(CMLib.threads().systemReport("topMOBTicks"));
		String topMOBClient=CMLib.threads().systemReport("topMOBClient");
		if(topMOBClient.length()>0)
		{
			buf.append("The most active mob has been '^H"+topMOBClient+"^?'\n\r");
			buf.append("That mob has consumed: ^H"+CMLib.english().returnTime(topMOBMillis,topMOBTicks)+"^?.\n\r");
		}
		*/
		return buf;
	}
	
	public void listUsers(Session viewerS, MOB mob, Vector commands)
	{
		if(commands.size()==0) return;
		commands.removeElementAt(0);
		int sortBy=-1;
		if(commands.size()>0)
		{
			String rest=CMParms.combine(commands,0).toUpperCase();
			sortBy = CMLib.players().getCharThinSortCode(rest,true);
			if(sortBy<0)
			{
				mob.tell("Unrecognized sort criteria: "+rest);
				return;
			}
		}
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(8.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(10.0,viewerS);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(4.0,viewerS);
		final int COL_LEN4=ListingLibrary.ColFixer.fixColWidth(5.0,viewerS);
		final int COL_LEN5=ListingLibrary.ColFixer.fixColWidth(23.0,viewerS);
		final int COL_LEN6=ListingLibrary.ColFixer.fixColWidth(18.0,viewerS);
		final int COL_LEN7=ListingLibrary.ColFixer.fixColWidth(15.0,viewerS);
		StringBuilder head=new StringBuilder("");
		head.append("[");
		head.append(CMStrings.padRight("Race",COL_LEN1)+" ");
		head.append(CMStrings.padRight("Class",COL_LEN2)+" ");
		head.append(CMStrings.padRight("Lvl",COL_LEN3)+" ");
		head.append(CMStrings.padRight("Hours",COL_LEN4)+" ");
		switch(sortBy){
		case 6: head.append(CMStrings.padRight("E-Mail",COL_LEN5)+" "); break;
		case 7: head.append(CMStrings.padRight("IP Address",COL_LEN5)+" "); break;
		default: head.append(CMStrings.padRight("Last",COL_LEN6)+" "); break;
		}

		head.append("] Character name\n\r");
		java.util.List<PlayerLibrary.ThinPlayer> allUsers=CMLib.database().getExtendedUserList();
		java.util.List<PlayerLibrary.ThinPlayer> oldSet=allUsers;
		int showBy=sortBy;
		PlayerLibrary lib=CMLib.players();
		while((oldSet.size()>0)&&(sortBy>=0)&&(sortBy<=7))
		{
			if(oldSet==allUsers) allUsers=new Vector();
			if((sortBy<3)||(sortBy>4))
			{
				PlayerLibrary.ThinPlayer selected=oldSet.get(0);
				for(int u=1;u<oldSet.size();u++)
				{
					PlayerLibrary.ThinPlayer U=oldSet.get(u);
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
					PlayerLibrary.ThinPlayer U=oldSet.get(u);
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
			PlayerLibrary.ThinPlayer U=allUsers.get(u);

			head.append("[");
			head.append(CMStrings.padRight(U.race,COL_LEN1)+" ");
			head.append(CMStrings.padRight(U.charClass,COL_LEN2)+" ");
			head.append(CMStrings.padRight(""+U.level,COL_LEN3)+" ");
			long age=Math.round(CMath.div(CMath.s_long(""+U.age),60.0));
			head.append(CMStrings.padRight(""+age,COL_LEN4)+" ");
			switch(showBy){
			case 6: head.append(CMStrings.padRight(U.email,COL_LEN5)+" "); break;
			case 7: head.append(CMStrings.padRight(U.ip,COL_LEN5)+" "); break;
			default: head.append(CMStrings.padRight(CMLib.time().date2String(U.last),COL_LEN6)+" "); break;
			}
			head.append("] "+CMStrings.padRight("^<LSTUSER^>"+U.name+"^</LSTUSER^>",COL_LEN7));
			head.append("\n\r");
		}
		mob.tell(head.toString());
	}

	public void listAccounts(Session viewerS, MOB mob, Vector commands)
	{
		if(commands.size()==0) return;
		commands.removeElementAt(0);
		int sortBy=-1;
		if(commands.size()>0)
		{
			String rest=CMParms.combine(commands,0).toUpperCase();
			sortBy = CMLib.players().getCharThinSortCode(rest,true);
			if(sortBy<0)
			{
				mob.tell("Unrecognized sort criteria: "+rest);
				return;
			}
		}
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(10.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(18.0,viewerS);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(23.0,viewerS);
		StringBuilder head=new StringBuilder("");
		head.append("^X");
		head.append("[");
		head.append(CMStrings.padRight("Account",COL_LEN1)+" ");
		head.append(CMStrings.padRight("Last",COL_LEN2)+" ");
		switch(sortBy){
			default : head.append(CMStrings.padRight("E-Mail",COL_LEN3)+" "); break;
			case 7: head.append(CMStrings.padRight("IP Address",COL_LEN3)+" "); break;
		}

		head.append("] Characters^.^N\n\r");
		List<PlayerAccount> allAccounts=CMLib.database().DBListAccounts(null);
		List<PlayerAccount> oldSet=allAccounts;
		Hashtable<String, PlayerLibrary.ThinPlayer> thinAcctHash=new Hashtable<String, PlayerLibrary.ThinPlayer>();
		for(PlayerAccount acct : allAccounts)
		{
			PlayerLibrary.ThinPlayer selectedU=new PlayerLibrary.ThinPlayer();
			selectedU.email=acct.getEmail();
			selectedU.ip=acct.lastIP();
			selectedU.last=acct.lastDateTime();
			selectedU.name=acct.accountName();
			thinAcctHash.put(acct.accountName(), selectedU);
		}
		int showBy=sortBy;
		PlayerLibrary lib=CMLib.players();
		while((oldSet.size()>0)&&(sortBy>=0)&&(sortBy<=7))
		{
			if(oldSet==allAccounts)
				allAccounts=new Vector<PlayerAccount>();
			if((sortBy<3)||(sortBy>4))
			{
				PlayerAccount selected = oldSet.get(0);
				if(selected != null)
				{
					PlayerLibrary.ThinPlayer selectedU=thinAcctHash.get(selected.accountName());
					for(int u=1;u<oldSet.size();u++)
					{
						PlayerAccount acct = oldSet.get(u);
						PlayerLibrary.ThinPlayer U=thinAcctHash.get(acct.accountName());
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
					PlayerLibrary.ThinPlayer selectedU=thinAcctHash.get(selected.accountName());
					for(int u=1;u<oldSet.size();u++)
					{
						PlayerAccount acct = oldSet.get(u);
						PlayerLibrary.ThinPlayer U=thinAcctHash.get(acct.accountName());
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
			PlayerAccount U=allAccounts.get(u);
			StringBuilder line=new StringBuilder("");
			line.append("[");
			line.append(CMStrings.padRight(U.accountName(),COL_LEN1)+" ");
			line.append(CMStrings.padRight(CMLib.time().date2String(U.lastDateTime()),COL_LEN2)+" ");
			String players = CMParms.toStringList(U.getPlayers());
			Vector<String> pListsV = new Vector<String>();
			while(players.length()>0)
			{
				int x=players.length();
				if(players.length()>20)
				{
					x=players.lastIndexOf(',',20);
					if(x<0) x=24;
				}
				pListsV.addElement(players.substring(0,x));
				players=players.substring(x).trim();
				if(players.startsWith(",")) players=players.substring(1).trim();
			}
			switch(showBy){
			default: line.append(CMStrings.padRight(U.getEmail(),COL_LEN3)+" "); break;
			case 7: line.append(CMStrings.padRight(U.lastIP(),COL_LEN3)+" "); break;
			}
			line.append("] ");
			int len = line.length();
			head.append(line.toString());
			boolean notYet = true;
			for(String s : pListsV)
			{
				if(notYet)
					notYet=false;
				else
					head.append(CMStrings.repeat(" ", len));
				head.append(s);
				head.append("\n\r");
			}
			if(pListsV.size()==0)
				head.append("\n\r");
		}
		mob.tell(head.toString());
	}

	public StringBuilder listRaces(Session viewerS, Enumeration these, boolean shortList)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
		if(shortList)
		{
			Vector raceNames=new Vector();
			for(Enumeration e=these;e.hasMoreElements();)
				raceNames.addElement(((Race)e.nextElement()).ID());
			lines.append(CMParms.toStringList(raceNames));
		}
		else
		for(Enumeration e=these;e.hasMoreElements();)
		{
			Race thisThang=(Race)e.nextElement();
			if(++column>3)
			{
				lines.append("\n\r");
				column=1;
			}
			lines.append(CMStrings.padRight(thisThang.ID()
										+(thisThang.isGeneric()?"*":"")
										+" ("+thisThang.racialCategory()+")",COL_LEN));
		}
		lines.append("\n\r");
		return lines;
	}
	public StringBuilder listCharClasses(Session viewerS, Enumeration these, boolean shortList)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
		if(shortList)
		{
			Vector classNames=new Vector();
			for(Enumeration e=these;e.hasMoreElements();)
				classNames.addElement(((CharClass)e.nextElement()).ID());
			lines.append(CMParms.toStringList(classNames));
		}
		else
		for(Enumeration e=these;e.hasMoreElements();)
		{
			CharClass thisThang=(CharClass)e.nextElement();
			if(++column>2)
			{
				lines.append("\n\r");
				column=1;
			}
			lines.append(CMStrings.padRight(thisThang.ID()
										+(thisThang.isGeneric()?"*":"")
										+" ("+thisThang.baseClass()+")",COL_LEN));
		}
		lines.append("\n\r");
		return lines;
	}
	public StringBuilder listRaceCats(Session viewerS, Enumeration these, boolean shortList)
	{
		StringBuilder lines=new StringBuilder("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		Vector raceCats=new Vector();
		Race R=null;
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
		for(Enumeration e=these;e.hasMoreElements();)
		{
			R=(Race)e.nextElement();
			if(!raceCats.contains(R.racialCategory()))
				raceCats.addElement(R.racialCategory());
		}
		Object[] sortedB=(new TreeSet(raceCats)).toArray();
		if(shortList)
		{
			String[] sortedC=new String[sortedB.length];
			for(int i=0;i<sortedB.length;i++)
				sortedC[i]=(String)sortedB[i];
			lines.append(CMParms.toStringList(sortedC));
		}
		else
		for(int i=0;i<sortedB.length;i++)
		{
			String raceCat=(String)sortedB[i];
			if(++column>3)
			{
				lines.append("\n\r");
				column=1;
			}
			lines.append(CMStrings.padRight(raceCat,COL_LEN));
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuilder listQuests(Session viewerS)
	{
		StringBuilder buf=new StringBuilder("");
		if(CMLib.quests().numQuests()==0)
			buf.append("No quests loaded.");
		else
		{
			buf.append("\n\r^xQuest Report:^.^N\n\r");
			final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(5.0,viewerS);
			final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(30.0,viewerS);
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1)+CMStrings.padRight("Name",COL_LEN2)+" Status^.^N\n\r");
			for(int i=0;i<CMLib.quests().numQuests();i++)
			{
				Quest Q=CMLib.quests().fetchQuest(i);
				if(Q!=null)
				{
					buf.append(CMStrings.padRight(""+(i+1),COL_LEN1)+CMStrings.padRight("^<LSTQUEST^>"+Q.name()+"^</LSTQUEST^>",COL_LEN2)+" ");
					if(Q.running())
					{
						String minsLeft="("+Q.minsRemaining()+" mins left)";
						if(Q.duration()==0)
							minsLeft="(Eternal)";

						if(Q.isCopy())
							buf.append("copy running "+minsLeft);
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
						if(min>0) {
							min=min*CMProps.getTickMillis();
							if(min>60000)
								buf.append("waiting ("+(min/60000)+" minutes left)");
							else
								buf.append("waiting ("+(min/1000)+" seconds left)");
						}
						else
							buf.append("waiting ("+min+" minutes left)");
					}
					else
						buf.append("loaded");
					buf.append("^N\n\r");
				}
			}
		}
		return buf;
	}

	public StringBuilder listJournals(Session viewerS)
	{
		StringBuilder buf=new StringBuilder("");
		List<String> journals=CMLib.database().DBReadJournals();

		if(journals.size()==0)
			buf.append("No journals exits.");
		else
		{
			final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(5.0,viewerS);
			final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(30.0,viewerS);
			buf.append("\n\r^xJournals List:^.^N\n\r");
			buf.append("\n\r^x"+CMStrings.padRight("#",COL_LEN1)+CMStrings.padRight("Name",COL_LEN2)+" Messages^.^N\n\r");
			for(int i=0;i<journals.size();i++)
			{
				String journal=journals.get(i);
				int messages=CMLib.database().DBCountJournal(journal,null,null);
				buf.append(CMStrings.padRight(""+(i+1),COL_LEN1)+CMStrings.padRight(journal,COL_LEN2)+" "+messages);
				buf.append("^N\n\r");
			}
		}
		return buf;
	}

	public StringBuilder listTicks(Session viewerS, String whichGroupStr)
	{
		StringBuilder msg=new StringBuilder("\n\r");
		boolean activeOnly=false;
		String mask=null;
		Set<Pair<Integer,Integer>> whichTicks=null;
		Set<Integer> whichGroups=null;
		int x=whichGroupStr.lastIndexOf(' ');
		String finalCol="tickercodeword";
		String finalColName="Status";
		if(x>0)
		{
			String lastWord=whichGroupStr.substring(x+1).trim().toLowerCase();
			final String[] validCols={"tickername","tickerid","tickerstatus","tickerstatusstr","tickercodeword","tickertickdown","tickerretickdown","tickermillitotal","tickermilliavg","tickerlaststartmillis","tickerlaststopmillis","tickerlaststartdate","tickerlaststopdate","tickerlastduration","tickersuspended"};
			int y=CMParms.indexOf(validCols,lastWord);
			if(y>=0)
				finalCol=lastWord;
			else
			for(String w : validCols)
				if(w.endsWith(lastWord))
				{
					lastWord=w;
					finalCol=lastWord;
				}
			if(!finalCol.equals(lastWord))
				return new StringBuilder("Invalid column: '"+lastWord+"'.  Valid cols are: "+CMParms.toStringList(validCols));
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
			String problemSets=CMLib.threads().systemReport("tickerProblems");
			List<String> sets=CMParms.parseSemicolons(problemSets, true);
			for(String set : sets)
			{
				List<String> pair=CMParms.parseCommas(set, true);
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
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(4.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(20.0,viewerS);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(3.0,viewerS);
		final int COL_LEN4=ListingLibrary.ColFixer.fixColWidth(8.0,viewerS);
		if(!activeOnly)
			msg.append(CMStrings.padRight("Grp",COL_LEN1)+CMStrings.padRight("Client",COL_LEN2)+" "+CMStrings.padRight("ID",COL_LEN3)+CMStrings.padRight(finalColName,COL_LEN4));
		msg.append(CMStrings.padRight("Grp",COL_LEN1)+CMStrings.padRight("Client",COL_LEN2)+" "+CMStrings.padRight("ID",COL_LEN3)+CMStrings.padRight(finalColName,COL_LEN4)+"\n\r");
		int col=0;
		int numGroups=CMath.s_int(CMLib.threads().tickInfo("tickGroupSize"));
		if((mask!=null)&&(mask.length()==0)) mask=null;
		String chunk=null;
		for(int group=0;group<numGroups;group++)
		{
			if((whichGroups==null)||(whichGroups.contains(Integer.valueOf(group))))
			{
				int tickersSize=CMath.s_int(CMLib.threads().tickInfo("tickersSize"+group));
				for(int tick=0;tick<tickersSize;tick++)
				{
					if((whichTicks==null)||(whichTicks.contains(new Pair<Integer,Integer>(Integer.valueOf(group), Integer.valueOf(tick)))))
					{
						long tickerlaststartdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststartmillis"+group+"-"+tick));
						long tickerlaststopdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststopmillis"+group+"-"+tick));
						boolean isActive=(tickerlaststopdate<tickerlaststartdate);
						if((!activeOnly)||(isActive))
						{
							String name=CMLib.threads().tickInfo("tickerName"+group+"-"+tick);
							if((mask==null)||(name.toUpperCase().indexOf(mask)>=0))
							{
								String id=CMLib.threads().tickInfo("tickerID"+group+"-"+tick);
								String status=CMLib.threads().tickInfo(finalCol+group+"-"+tick);
								boolean suspended=CMath.s_bool(CMLib.threads().tickInfo("tickerSuspended"+group+"-"+tick));
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
		StringBuilder msg=new StringBuilder("");
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(25.0,viewerS);
		for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			msg.append(CMStrings.padRight(A.Name(),COL_LEN)+": ");
			if(A.getSubOpList().length()==0)
				msg.append("No Area staff defined.\n\r");
			else
				msg.append(A.getSubOpList()+"\n\r");
		}
		return msg;
	}

	protected String reallyFindOneWays(Session viewerS, Vector commands)
	{
		StringBuilder str=new StringBuilder("");
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Room R2=R.rawDoors()[d];
						if((R2!=null)&&(R2.rawDoors()[Directions.getOpDirectionCode(d)]!=R))
							str.append(CMStrings.padRight(R.roomID(),30)+": "+Directions.getDirectionName(d)+" to "+R2.roomID()+"\n\r");
					}
			}
		}catch(NoSuchElementException e){}
		if(str.length()==0) str.append("None!");
		if(CMParms.combine(commands,1).equalsIgnoreCase("log"))
			Log.rawSysOut(str.toString());
		return str.toString();
	}


	protected String unlinkedExits(Session viewerS, Vector commands)
	{
		StringBuilder str=new StringBuilder("");
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Room R2=R.rawDoors()[d];
					Exit E2=R.getRawExit(d);
					if((R2==null)&&(E2!=null))
						str.append(CMStrings.padRight(R.roomID(),30)+": "+Directions.getDirectionName(d)+" to "+E2.temporaryDoorLink()+" ("+E2.displayText()+")\n\r");
				}
			}
		}catch(NoSuchElementException e){}
		if(str.length()==0) str.append("None!");
		if(CMParms.combine(commands,1).equalsIgnoreCase("log"))
			Log.rawSysOut(str.toString());
		return str.toString();
	}

	public String listResources(MOB mob, String parm)
	{
		Iterator<String> keyIter=Resources.findResourceKeys(parm);
		if(!keyIter.hasNext())
			return "";
		String key = keyIter.next();
		if(!keyIter.hasNext())
		{
			StringBuilder str=new StringBuilder("^x"+key+"^?\n\r");
			Object o=Resources.getResource(key);
			if(o instanceof List) str.append(CMParms.toStringList((List)o));
			else
			if(o instanceof Map) str.append(CMParms.toStringList((Map)o));
			else
			if(o instanceof Set) str.append(CMParms.toStringList((Set)o));
			else
			if(o instanceof String[]) str.append(CMParms.toStringList((String[])o));
			else
			if(o instanceof boolean[]) str.append(CMParms.toStringList((boolean[])o));
			if(o instanceof byte[]) str.append(CMParms.toStringList((byte[])o));
			else
			if(o instanceof char[]) str.append(CMParms.toStringList((char[])o));
			else
			if(o instanceof double[]) str.append(CMParms.toStringList((double[])o));
			else
			if(o instanceof int[]) str.append(CMParms.toStringList((int[])o));
			else
			if(o instanceof long[]) str.append(CMParms.toStringList((long[])o));
			else
			if(o!=null)
				str.append(o.toString());
			return str.toString();
		}
		Enumeration<String> keys=new IteratorEnumeration<String>(Resources.findResourceKeys(parm));
		return CMLib.lister().reallyList2Cols(mob,keys).toString();
	}

	public String listHelpFileRequests(MOB mob, String rest)
	{
		String fileName=Log.instance().getLogFilename(Log.Type.help);
		if(fileName==null)
			return "This feature requires that help request log entries be directed to a file.";
		CMFile f=new CMFile(fileName,mob,true);
		if((!f.exists())||(!f.canRead()))
			return "File '"+f.getName()+"' does not exist.";
		List<String> V=Resources.getFileLineVector(f.text());
		Hashtable entries = new Hashtable();
		for(int v=0;v<V.size();v++)
		{
			String s=V.get(v);
			if(s.indexOf(" help  Help")>0)
			{
				int x=s.indexOf("wanted help on",19);
				String helpEntry=s.substring(x+14).trim().toLowerCase();
				int[] sightings=(int[])entries.get(helpEntry);
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
		Hashtable readyEntries = new Hashtable(entries.size());
		for(Enumeration e=entries.keys();e.hasMoreElements();)
		{
			Object key=e.nextElement();
			int[] val=(int[])entries.get(key);
			readyEntries.put(key,Integer.valueOf(val[0]));
		}
		DVector sightingsDV=DVector.toDVector(readyEntries);
		sightingsDV.sortBy(2);
		StringBuilder str=new StringBuilder("^HHelp entries, sorted by popularity: ^N\n\r");
		for(int d=0;d<sightingsDV.size();d++)
			str.append("^w"+CMStrings.padRight(sightingsDV.elementAt(d,2).toString(),4))
			   .append(" ")
			   .append(sightingsDV.elementAt(d,1).toString())
			   .append("\n\r");
		return str.toString()+"^N";
	}
	
	public String listRecipes(MOB mob, String rest)
	{
		StringBuilder str = new StringBuilder("");
		if(rest.trim().length()==0)
		{
			str.append("Common Skills with editable recipes: ");
			for(Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
			{
				Ability A=e.nextElement();
				if(A instanceof ItemCraftor)
				{
					ItemCraftor iA = (ItemCraftor)A;
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
			Ability A=CMClass.findAbility(rest,Ability.ACODE_COMMON_SKILL,-1,false);
			if(A==null)
				str.append("Ability '"+rest+"' does not exist -- try list recipes");
			else
			if(!(A instanceof ItemCraftor))
				str.append("Ability '"+A.ID()+"' is not a proper ability -- try list recipes");
			else
			{
				ItemCraftor iA = (ItemCraftor)A;
				if((iA.parametersFormat()==null)
				||(iA.parametersFormat().length()==0)
				||(iA.parametersFile()==null)
				||(iA.parametersFile().length()==0))
					str.append("Ability '"+A.ID()+"' is not editable -- try list recipes");
				else
					str.append(CMLib.ableParms().getRecipeList(iA));
			}
		}
		return str.toString();
	}
	public String listMaterials()
	{
		return CMParms.toStringList(RawMaterial.Material.values());
	}
	public String listEnvResources(Session viewerS, boolean shortList)
	{
		if(shortList)
			return CMParms.toStringList(RawMaterial.CODES.NAMES());
		StringBuilder str=new StringBuilder("");
		//for(String S : RawMaterial.CODES.NAMES())
		//	str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(S.toLowerCase()),16));
		final int COL_LEN1=ListingLibrary.ColFixer.fixColWidth(15.0,viewerS);
		final int COL_LEN2=ListingLibrary.ColFixer.fixColWidth(10.0,viewerS);
		final int COL_LEN3=ListingLibrary.ColFixer.fixColWidth(3.0,viewerS);
		final int COL_LEN4=ListingLibrary.ColFixer.fixColWidth(4.0,viewerS);
		final int COL_LEN5=ListingLibrary.ColFixer.fixColWidth(3.0,viewerS);
		final int COL_LEN6=ListingLibrary.ColFixer.fixColWidth(36.0,viewerS);
		final int COL_LEN7=COL_LEN1+1+COL_LEN2+1+COL_LEN3+1+COL_LEN4+1+COL_LEN5+1;
		str.append(CMStrings.padRight("Resource",COL_LEN1)+" ");
		str.append(CMStrings.padRight("Material",COL_LEN2)+" ");
		str.append(CMStrings.padRight("Val",COL_LEN3)+" ");
		str.append(CMStrings.padRight("Freq",COL_LEN4)+" ");
		str.append(CMStrings.padRight("Str",COL_LEN5)+" ");
		str.append("Locales\n\r");
		for(int i : RawMaterial.CODES.ALL())
		{
			str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(i).toLowerCase()),COL_LEN1+1));
			str.append(CMStrings.padRight(RawMaterial.Material.findByMask(i&RawMaterial.MATERIAL_MASK).noun(),COL_LEN2+1));
			str.append(CMStrings.padRight(""+RawMaterial.CODES.VALUE(i),COL_LEN3+1));
			str.append(CMStrings.padRight(""+RawMaterial.CODES.FREQUENCY(i),COL_LEN4+1));
			str.append(CMStrings.padRight(""+RawMaterial.CODES.HARDNESS(i),COL_LEN5+1));
			StringBuilder locales=new StringBuilder("");
			for(Enumeration e=CMClass.locales();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
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
		return str.toString();
	}

	public List<String> getMyCmdWords(MOB mob)
	{
		Vector<String> V=new Vector<String>();
		for(int i=0;i<SECURITY_LISTMAP.length;i++)
		{
			ListCmdEntry cmd=SECURITY_LISTMAP[i];
			if((CMSecurity.isAllowedContainsAny(mob, cmd.flags))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				V.addElement(cmd.cmd);
		}
		for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				V.addElement(CMJ.NAME()+"S");
		}
		return V;
	}

	public int getMyCmdCode(MOB mob, String s)
	{
		s=s.toUpperCase().trim();
		for(int i=0;i<SECURITY_LISTMAP.length;i++)
		{
			ListCmdEntry cmd=SECURITY_LISTMAP[i];
			if(cmd.cmd.startsWith(s))
				if((CMSecurity.isAllowedContainsAny(mob, cmd.flags))
				||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				{ 
					return i;
				}
		}
		for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if(((CMJ.NAME()+"S").startsWith(s)||CMJ.NAME().equals(s)||CMJ.NAME().replace('_', ' ').equals(s))
			&&((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
				||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)))
					return 29;
		}
		return -1;
	}

	public int getAnyCode(MOB mob)
	{
		for(int i=0;i<SECURITY_LISTMAP.length;i++)
		{
			ListCmdEntry cmd=SECURITY_LISTMAP[i];
			if((CMSecurity.isAllowedContainsAny(mob, cmd.flags))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
			{ 
				return i;
			}
		}
		for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME()))
			||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				return 29;
		}
		return -1;
	}

	public String listComponents(Session viewerS)
	{
		StringBuilder buf=new StringBuilder("^xAll Defined Spells and required components: ^N\n\r");
		for(String ID : CMLib.ableMapper().getAbilityComponentMap().keySet())
		{
			List<AbilityComponent> DV=CMLib.ableMapper().getAbilityComponentMap().get(ID);
			if(DV!=null)
				buf.append(CMStrings.padRight(ID,20)+": "+CMLib.ableMapper().getAbilityComponentDesc(null,ID)+"\n\r");
		}
		if(buf.length()==0) return "None defined.";
		return buf.toString();
	}

	public String listExpertises(Session viewerS)
	{
		StringBuilder buf=new StringBuilder("^xAll Defined Expertise Codes: ^N\n\r");
		final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(20.0,viewerS);
		for(Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
		{
			ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)e.nextElement();
			buf.append(CMStrings.padRight("^Z"+def.ID,COL_LEN)+"^?: "+CMStrings.padRight(def.name,COL_LEN)+": "+CMLib.masking().maskDesc(def.allRequirements())+"\n\r");
		}
		if(buf.length()==0) return "None defined.";
		return buf.toString();
	}

	public String listTitles(Session viewerS)
	{
		StringBuilder buf=new StringBuilder("^xAll Defined Auto-Titles: ^N\n\r");
		for(Enumeration e=CMLib.titles().autoTitles();e.hasMoreElements();)
		{
			String title=(String)e.nextElement();
			String maskDesc=CMLib.masking().maskDesc(CMLib.titles().getAutoTitleMask(title));
			buf.append(CMStrings.padRight(title,30)+": "+maskDesc+"\n\r");
		}
		if(buf.length()==0) return "None defined.";
		return buf.toString();
	}

	public String listClanGovernments(Session viewerS, List commands)
	{
		StringBuilder buf=new StringBuilder("^xAll Clan Governments: ^N\n\r");
		int glen=0;
		for(ClanGovernment G : CMLib.clans().getStockGovernments())
			if(G.getName().length()>glen)
				glen=G.getName().length();
		final int SCREEN_LEN=ListingLibrary.ColFixer.fixColWidth(78.0,viewerS);
		for(ClanGovernment G : CMLib.clans().getStockGovernments())
			buf.append(CMStrings.padRight(G.getName(),glen)+": "+CMStrings.limit(G.getShortDesc(),SCREEN_LEN-glen-2)+"\n\r");
		return buf.toString();
	}
	
	public String listClans(Session viewerS, List commands)
	{
		StringBuilder buf=new StringBuilder("^xAll Clans: ^N\n\r");
		int clen=0;
		for(Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
		{
			Clan C=c.nextElement();
			if(C.clanID().length()>clen)
				clen=C.clanID().length();
		}
		final int SCREEN_LEN=ListingLibrary.ColFixer.fixColWidth(78.0,viewerS);
		for(Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
		{
			Clan C=c.nextElement();
			buf.append(CMStrings.padRight(C.clanID(),clen)+": "+CMStrings.limit(C.getMemberList().size()+" members",SCREEN_LEN-clen-2)+"\n\r");
		}
		return buf.toString();
	}
	
	public StringBuilder listContent(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		Enumeration roomsToDo=null;
		String rest=CMParms.combine(commands,0);
		if(rest.equalsIgnoreCase("area"))
			roomsToDo=mob.location().getArea().getMetroMap();
		else
		if(rest.trim().length()==0)
			roomsToDo=new XVector(mob.location()).elements();
		else
		{
			Area A=CMLib.map().findArea(rest);
			if(A!=null)
				roomsToDo=A.getMetroMap();
			else
			{
				Room R=CMLib.map().getRoom(rest);
				if(R!=null)
					roomsToDo=new XVector(mob.location()).elements();
				else
					return new StringBuilder("There's no such place as '"+rest+"'");
			}
		}
		StringBuilder buf=new StringBuilder("");
		Room R=null;
		Room TR=null;
		Map<String,Room> set=null;
		final int SCREEN_LEN1=ListingLibrary.ColFixer.fixColWidth(15.0,mob);
		final int SCREEN_LEN2=ListingLibrary.ColFixer.fixColWidth(35.0,mob);
		final int SCREEN_LEN3=ListingLibrary.ColFixer.fixColWidth(3.0,mob);
		for(;roomsToDo.hasMoreElements();)
		{
			R=(Room)roomsToDo.nextElement();
			if(R.roomID().length()==0) continue;
			set=CMLib.database().DBReadRoomData(CMLib.map().getExtendedRoomID(R),false);
			if((set==null)||(set.size()==0))
				buf.append("'"+CMLib.map().getExtendedRoomID(R)+"' could not be read from the database!\n\r");
			else
			{
				TR=set.entrySet().iterator().next().getValue();
				CMLib.database().DBReadContent(TR.roomID(),TR,false);
				buf.append("\n\r^NRoomID: "+CMLib.map().getExtendedRoomID(TR)+"\n\r");
				for(int m=0;m<TR.numInhabitants();m++)
				{
					MOB M=TR.fetchInhabitant(m);
					if(M==null) continue;
					buf.append("^M"+CMStrings.padRight(M.ID(),SCREEN_LEN1)+": "+CMStrings.padRight(M.displayText(),SCREEN_LEN2)+": "
								+CMStrings.padRight(M.phyStats().level()+"",SCREEN_LEN3)+": "
								+CMLib.flags().getAlignmentName(M)
								+"^N\n\r");
					for(int i=0;i<M.numItems();i++)
					{
						Item I=M.getItem(i);
						if(I!=null)
							buf.append("    ^I"+CMStrings.padRight(I.ID(),SCREEN_LEN1)
									+": "+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),SCREEN_LEN2)+": "
									+CMStrings.padRight(I.phyStats().level()+"",SCREEN_LEN3)+": "
									+"^N"+((I.container()!=null)?I.Name():"")+"\n\r");
					}
				}
				for(int i=0;i<TR.numItems();i++)
				{
					Item I=TR.getItem(i);
					if(I!=null)
						buf.append("^I"+CMStrings.padRight(I.ID(),SCREEN_LEN1)+": "
								+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),SCREEN_LEN2)+": "
								+CMStrings.padRight(I.phyStats().level()+"",SCREEN_LEN3)+": "
								+"^N"+((I.container()!=null)?I.Name():"")+"\n\r");
				}
				TR.destroy();
			}
		}
		return buf;
	}

	public void listPolls(MOB mob, Vector commands)
	{
		Iterator<Poll> i=CMLib.polls().getPollList();
		if(!i.hasNext())
			mob.tell("\n\rNo polls available.  Fix that by entering CREATE POLL!");
		else
		{
			StringBuilder str=new StringBuilder("");
			int v=1;
			for(;i.hasNext();v++)
			{
				Poll P=i.next();
				str.append(CMStrings.padRight(""+v,2)+": "+P.getName());
				if(!CMath.bset(P.getFlags(),Poll.FLAG_ACTIVE))
					str.append(" (inactive)");
				else
				if(P.getExpiration()>0)
					str.append(" (expires: "+CMLib.time().date2String(P.getExpiration())+")");
				str.append("\n\r");
			}
			mob.tell(str.toString());
		}
	}

	public void listLog(MOB mob, Vector commands)
	{
		int pageBreak=((mob.playerStats()!=null)?mob.playerStats().getPageBreak():0);
		int lineNum=0;
		if(commands.size()<2)
		{
			Log.LogReader log=Log.instance().getLogReader();
			String line=log.nextLine();
			while((line!=null)&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				mob.session().rawPrintln(line);
				if((pageBreak>0)&&(lineNum>=pageBreak))
					if(!pause(mob.session()))
						break;
					else
						lineNum=0;
				lineNum++;
				line=log.nextLine();
			}
			log.close();
			return;
		}

		int start=0;
		int logSize=Log.instance().numLines();
		int end=logSize;
		Log.LogReader log=Log.instance().getLogReader();
		
		for(int i=1;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if((s.equalsIgnoreCase("front")||(s.equalsIgnoreCase("first"))||(s.equalsIgnoreCase("head")))
			&&(i<(commands.size()-1)))
			{
				s=(String)commands.elementAt(i+1);
				if(CMath.isInteger(s))
				{
					i++;
					end=CMath.s_int(s);
				}
				else
				{
					mob.tell("Bad "+s+" parameter format after.");
					return;
				}
			}
			else
			if((s.equalsIgnoreCase("back")||(s.equalsIgnoreCase("last"))||(s.equalsIgnoreCase("tail")))
			&&(i<(commands.size()-1)))
			{
				s=(String)commands.elementAt(i+1);
				if(CMath.isInteger(s))
				{
					i++;
					start=(end-CMath.s_int(s))-1;
				}
				else
				{
					mob.tell("Bad "+s+" parameter format after.");
					return;
				}
			}
			else
			if(s.equalsIgnoreCase("skip")
			&&(i<(commands.size()-1)))
			{
				s=(String)commands.elementAt(i+1);
				if(CMath.isInteger(s))
				{
					i++;
					start=start+CMath.s_int(s);
				}
				else
				{
					mob.tell("Bad "+s+" parameter format after.");
					return;
				}
			}
		}
		if(end>=logSize) end=logSize;
		if(start<0) start=0;
		String line=log.nextLine();
		lineNum=0;
		int shownLineNum=0;
		while((line!=null)&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			if((lineNum>start)&&(lineNum<=end))
			{
				mob.session().rawPrintln(line);
				if((pageBreak>0)&&(shownLineNum>=pageBreak))
					if(!pause(mob.session()))
						break;
					else
						shownLineNum=0;
				shownLineNum++;
			}
			lineNum++;
			line=log.nextLine();
		}
		log.close();
	}
	
	private static class ListCmdEntry
	{
		public String 			   cmd;
		public CMSecurity.SecGroup flags;
		public ListCmdEntry(String cmd, SecFlag[] flags)
		{
			this.cmd=cmd;
			this.flags=new CMSecurity.SecGroup(flags);
		}
		
	}
	
	public final static ListCmdEntry[] SECURITY_LISTMAP={
		/*00*/new ListCmdEntry("UNLINKEDEXITS",new SecFlag[]{SecFlag.CMDEXITS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		/*01*/new ListCmdEntry("ITEMS",new SecFlag[]{SecFlag.CMDITEMS}),
		/*02*/new ListCmdEntry("ARMOR",new SecFlag[]{SecFlag.CMDITEMS}),
		/*03*/new ListCmdEntry("ENVRESOURCES",new SecFlag[]{SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		/*04*/new ListCmdEntry("WEAPONS",new SecFlag[]{SecFlag.CMDITEMS}),
		/*05*/new ListCmdEntry("MOBS",new SecFlag[]{SecFlag.CMDMOBS}),
		/*06*/new ListCmdEntry("ROOMS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		/*07*/new ListCmdEntry("AREA",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		/*08*/new ListCmdEntry("LOCALES",new SecFlag[]{SecFlag.CMDROOMS}),
		/*09*/new ListCmdEntry("BEHAVIORS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		/*10*/new ListCmdEntry("EXITS",new SecFlag[]{SecFlag.CMDEXITS}),
		/*11*/new ListCmdEntry("RACES",new SecFlag[]{SecFlag.CMDRACES,SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS}),
		/*12*/new ListCmdEntry("CLASSES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDCLASSES}),
		/*13*/new ListCmdEntry("STAFF",new SecFlag[]{SecFlag.CMDAREAS}),
		/*14*/new ListCmdEntry("SPELLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*15*/new ListCmdEntry("SONGS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*16*/new ListCmdEntry("PRAYERS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*17*/new ListCmdEntry("PROPERTIES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*18*/new ListCmdEntry("THIEFSKILLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*19*/new ListCmdEntry("COMMON",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*20*/new ListCmdEntry("JOURNALS",new SecFlag[]{SecFlag.JOURNALS}),
		/*21*/new ListCmdEntry("SKILLS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*22*/new ListCmdEntry("QUESTS",new SecFlag[]{SecFlag.CMDQUESTS}),
		/*23*/new ListCmdEntry("DISEASES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*24*/new ListCmdEntry("POISONS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*25*/new ListCmdEntry("TICKS",new SecFlag[]{SecFlag.LISTADMIN}),
		/*26*/new ListCmdEntry("MAGIC",new SecFlag[]{SecFlag.CMDITEMS}),
		/*27*/new ListCmdEntry("TECH",new SecFlag[]{SecFlag.CMDITEMS}),
		/*28*/new ListCmdEntry("CLANITEMS",new SecFlag[]{SecFlag.CMDITEMS,SecFlag.CMDCLANS}),
		/*29*/new ListCmdEntry("COMMANDJOURNAL",new SecFlag[]{}), // blank, but used!
		/*30*/new ListCmdEntry("REALESTATE",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		/*31*/new ListCmdEntry("NOPURGE",new SecFlag[]{SecFlag.NOPURGE}),
		/*32*/new ListCmdEntry("BANNED",new SecFlag[]{SecFlag.BAN}),
		/*33*/new ListCmdEntry("RACECATS",new SecFlag[]{SecFlag.CMDRACES,SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS}),
		/*34*/new ListCmdEntry("LOG",new SecFlag[]{SecFlag.LISTADMIN}),
		/*35*/new ListCmdEntry("USERS",new SecFlag[]{SecFlag.CMDPLAYERS,SecFlag.STAT}),
		/*36*/new ListCmdEntry("LINKAGES",new SecFlag[]{SecFlag.CMDAREAS}),
		/*37*/new ListCmdEntry("REPORTS",new SecFlag[]{SecFlag.LISTADMIN}),
		/*38*/new ListCmdEntry("THREADS",new SecFlag[]{SecFlag.LISTADMIN}),
		/*39*/new ListCmdEntry("RESOURCES",new SecFlag[]{SecFlag.LOADUNLOAD}),
		/*40*/new ListCmdEntry("ONEWAYDOORS",new SecFlag[]{SecFlag.CMDEXITS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		/*41*/new ListCmdEntry("CHANTS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*42*/new ListCmdEntry("POWERS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*43*/new ListCmdEntry("SUPERPOWERS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES,SecFlag.CMDABILITIES}),
		/*44*/new ListCmdEntry("COMPONENTS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.COMPONENTS}),
		/*45*/new ListCmdEntry("EXPERTISES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.EXPERTISES}),
		/*46*/new ListCmdEntry("FACTIONS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDFACTIONS}),
		/*47*/new ListCmdEntry("MATERIALS",new SecFlag[]{SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS}),
		/*48*/new ListCmdEntry("OBJCOUNTERS",new SecFlag[]{SecFlag.LISTADMIN}),
		/*49*/new ListCmdEntry("POLLS",new SecFlag[]{SecFlag.POLLS,SecFlag.LISTADMIN}),
		/*50*/new ListCmdEntry("CONTENTS",new SecFlag[]{SecFlag.CMDROOMS,SecFlag.CMDITEMS,SecFlag.CMDMOBS,SecFlag.CMDAREAS}),
		/*51*/new ListCmdEntry("EXPIRES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		/*52*/new ListCmdEntry("TITLES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.TITLES}),
		/*53*/new ListCmdEntry("AREARESOURCES",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		/*54*/new ListCmdEntry("CONQUERED",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		/*55*/new ListCmdEntry("HOLIDAYS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		/*56*/new ListCmdEntry("RECIPES",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDRECIPES}),
		/*57*/new ListCmdEntry("HELPFILEREQUESTS",new SecFlag[]{SecFlag.LISTADMIN}),
		/*58*/new ListCmdEntry("SCRIPTS",new SecFlag[]{SecFlag.CMDMOBS,SecFlag.CMDITEMS,SecFlag.CMDROOMS,SecFlag.CMDAREAS,SecFlag.CMDEXITS,SecFlag.CMDRACES,SecFlag.CMDCLASSES}),
		/*59*/new ListCmdEntry("ACCOUNTS",new SecFlag[]{SecFlag.CMDPLAYERS,SecFlag.STAT}),
		/*60*/new ListCmdEntry("GOVERNMENTS",new SecFlag[]{SecFlag.CMDCLANS}),
		/*61*/new ListCmdEntry("CLANS",new SecFlag[]{SecFlag.CMDCLANS}),
		/*62*/new ListCmdEntry("DEBUGFLAG",new SecFlag[]{SecFlag.LISTADMIN}),
		/*63*/new ListCmdEntry("DISABLEFLAG",new SecFlag[]{SecFlag.LISTADMIN}),
		/*64*/new ListCmdEntry("ALLQUALIFYS",new SecFlag[]{SecFlag.CMDABILITIES,SecFlag.LISTADMIN}),
		/*65*/new ListCmdEntry("NEWS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.JOURNALS,SecFlag.NEWS}),
		/*66*/new ListCmdEntry("AREAS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		/*67*/new ListCmdEntry("SESSIONS",new SecFlag[]{SecFlag.SESSIONS}),
		/*68*/new ListCmdEntry("SPACESHIPS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		/*69*/new ListCmdEntry("WORLD",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		/*70*/new ListCmdEntry("PLANETS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS}),
		/*71*/new ListCmdEntry("CURRENTS",new SecFlag[]{SecFlag.LISTADMIN,SecFlag.CMDAREAS,SecFlag.CMDROOMS,SecFlag.CMDMOBS}),
	};

	public boolean pause(Session sess) 
	{
		if((sess==null)||(sess.isStopped())) return false;
		sess.rawCharsOut("<pause - enter>".toCharArray());
		try{ 
			String s=sess.blockingIn(10 * 60 * 1000); 
			if(s!=null)
			{
				s=s.toLowerCase();
				if(s.startsWith("qu")||s.startsWith("ex")||s.equals("x"))
					return false;
			}
		}catch(Exception e){return false;}
		return !sess.isStopped();
	}

	public void listNews(MOB mob, Vector commands)
	{
		final String theRest=CMParms.combine(commands,1);
		Item I=CMClass.getItem("StdJournal");
		I.setName("SYSTEM_NEWS");
		I.setDescription("Enter `LIST NEWS [NUMBER]` to read an entry.%0D%0AEnter CREATE NEWS to add new entries. ");
		CMMsg newMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_READ|CMMsg.MASK_ALWAYS,null,CMMsg.MSG_READ|CMMsg.MASK_ALWAYS,theRest,CMMsg.MSG_READ|CMMsg.MASK_ALWAYS,null);
		if(mob.location().okMessage(mob,newMsg)&&(I.okMessage(mob, newMsg)))
		{
			mob.location().send(mob,newMsg);
			I.executeMsg(mob,newMsg);
		}
	}

	public void listSql(MOB mob, String rest)
	{
		mob.tell("SQL Query: "+rest);
		try
		{
			List<String[]> rows=CMLib.database().DBRawQuery(rest.replace('`','\''));
			StringBuilder report=new StringBuilder("");
			for(final String[] row : rows)
				report.append(CMParms.toStringList(row)).append("\n\r");
			if(mob.session()==null) return;
			mob.session().rawPrint(report.toString());
		}
		catch(Exception e)
		{
			mob.tell("SQL Query Error: "+e.getMessage());
		}
	}

	private enum ListAreaStats 
	{ 
		NAME("Name",30), AUTHOR("Auth",15), DESCRIPTION("Desc",50), ROOMS("Rooms",6), STATE("State",10), HIDDEN("Hiddn",6);
		public String shortName;
		public Integer len;
		private ListAreaStats(String shortName, int len)
		{
			this.shortName=shortName;
			this.len=Integer.valueOf(len);
		}
		public Object getFromArea(Area A)
		{
			switch(this)
			{
			case NAME: return A.Name();
			case HIDDEN: return ""+CMLib.flags().isHidden(A);
			case ROOMS: return Integer.valueOf(A.getProperRoomnumbers().roomCountAllAreas());
			case STATE: return A.getAreaState().name();
			case AUTHOR: return A.getAuthorID();
			case DESCRIPTION: return A.description().replace('\n', ' ').replace('\r', ' ');
			default: return "";
			}
		}
	} 

	public Object getAreaStatFromSomewhere(Area A, String stat)
	{
		stat=stat.toUpperCase().trim();
		ListAreaStats ls=(ListAreaStats)CMath.s_valueOf(ListAreaStats.class, stat);
		Area.Stats as=(Area.Stats)CMath.s_valueOf(Area.Stats.class, stat);
		if(ls != null)
			return ls.getFromArea(A);
		else
		if(as!=null)
			return Integer.valueOf(A.getAreaIStats()[as.ordinal()]);
		else
			return null;
	}

	public void listCurrents(MOB mob, Vector commands)
	{
		StringBuffer str=new StringBuffer("");
		for(String key : CMLib.tech().getMakeRegisteredKeys())
		{
			str.append("Registered key: "+key+"\n\r");
			str.append(CMStrings.padRight("Name", 30)).append(" ");
			str.append(CMStrings.padRight("Room", 30)).append(" ");
			str.append("\n\r");
			str.append(CMStrings.repeat("-", 75)).append("\n\r");
			for(Electronics e : CMLib.tech().getMakeRegisteredElectronics(key))
			{
				str.append(CMStrings.padRight(e.Name(), 30)).append(" ");
				str.append(CMStrings.padRight(CMLib.map().getExtendedRoomID(CMLib.map().roomLocation(e)), 30)).append(" ");
				str.append("\n\r");
			}
			str.append("\n\r");
		}
		if(str.length()==0)
			str.append("No electronics found.\n\r");
		if(mob.session()!=null)
			mob.session().rawPrint(str.toString());
	}

	public void listAreas(MOB mob, Vector commands, Filterer<Area> filter)
	{
		if(mob==null) return;
		commands.remove(0);
		List<String> sortBys=null;
		List<String> colNames=null;
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
					String stat=commands.get(0).toString().toUpperCase().trim();
					ListAreaStats ls=(ListAreaStats)CMath.s_valueOf(ListAreaStats.class, stat);
					Area.Stats as=(Area.Stats)CMath.s_valueOf(Area.Stats.class, stat);
					if((ls==null)&&(as==null))
					{
						mob.tell("'"+stat+"' is not recognized.  Try one of these: "+CMParms.toStringList(ListAreaStats.values())+", "+CMParms.toStringList(Area.Stats.values()));
						return;
					}
					addTos.add(stat);
					commands.remove(0);
				}
				else
				{
					mob.tell("'"+commands.get(0).toString()+"' is not recognized.  Try 'columns' or 'sortby' followed by one or more of these: "+CMParms.toStringList(ListAreaStats.values())+", "+CMParms.toStringList(Area.Stats.values()));
					return;
				}
			}
		}
		Vector<Triad<String,String,Integer>> columns=new Vector<Triad<String,String,Integer>>();
		if((colNames!=null)&&(colNames.size()>0))
		{
			for(String newCol : colNames)
			{
				ListAreaStats ls=(ListAreaStats)CMath.s_valueOf(ListAreaStats.class, newCol);
				Area.Stats as=(Area.Stats)CMath.s_valueOf(Area.Stats.class, newCol);
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
		
		Session s=mob.session();
		double wrap=(s==null)?78:s.getWrap();
		double totalCols=0;
		for(int i=0;i<columns.size();i++)
			totalCols+=columns.get(i).third.intValue();
		for(int i=0;i<columns.size();i++)
		{
			double colVal=columns.get(i).third.intValue();
			double pct=CMath.div(colVal,totalCols );
			int newSize=(int)Math.round(Math.floor(CMath.mul(pct, wrap)));
			columns.get(i).third=Integer.valueOf(newSize);
		}
		
		StringBuilder str=new StringBuilder("");
		for(Triad<String,String,Integer> head : columns)
			str.append(CMStrings.padRight(head.first, head.third.intValue()));
		str.append("\n\r");
		Triad<String,String,Integer> lastColomn=columns.get(columns.size()-1);
		Enumeration<Area> a;
		if(sortBys!=null)
		{
			TreeMap<Comparable,Area> sorted=new TreeMap<Comparable,Area>();
			for(Enumeration<Area> as=CMLib.map().areas();as.hasMoreElements();)
			{
				Area A=as.nextElement();
				if((filter!=null)&&(!filter.passesFilter(A)))
					continue;
				sorted.put(A.name(), A);
			}
			for(int si=sortBys.size()-1; si>=0;si--)
			{
				TreeMap<Comparable,Area> newsorted=new TreeMap<Comparable,Area>();
				for(Iterator<Area> a2=sorted.values().iterator();a2.hasNext();)
				{
					Area A=a2.next();
					Object val=getAreaStatFromSomewhere(A,sortBys.get(si));
					if(val==null)
					{
						newsorted=sorted;
						break;
					}
					newsorted.put((Comparable)val, A);
				}
				sorted=newsorted;
			}
			if(sorted.size()>0)
				a=new IteratorEnumeration<Area>(sorted.values().iterator());
			else
				a=CMLib.map().sortedAreas();
		}
		else
			a=CMLib.map().sortedAreas();
		for(;a.hasMoreElements();)
		{
			Area A=a.nextElement();
			if((filter!=null)&&(!filter.passesFilter(A)))
				continue;
			for(Triad<String,String,Integer> head : columns)
			{
				Object val =getAreaStatFromSomewhere(A,head.second);
				if(val==null) val="?";
				if(head==lastColomn)
					str.append(CMStrings.scrunchWord(val.toString(), head.third.intValue()-1));
				else
					str.append(CMStrings.padRight(CMStrings.scrunchWord(val.toString(), head.third.intValue()-1), head.third.intValue()));
			}
			str.append("\n\r");
		}
		if(s!=null)
			s.colorOnlyPrint(str.toString(), true);
	}
	
	public void listSessions(MOB mob, Vector commands)
	{
		String sort="";
		if((commands!=null)&&(commands.size()>1))
			sort=CMParms.combine(commands,1).trim().toUpperCase();
		StringBuffer lines=new StringBuffer("\n\r^x");
		lines.append(CMStrings.padRight("#",3)+"| ");
		lines.append(CMStrings.padRight("Status",9)+"| ");
		lines.append(CMStrings.padRight("Valid",5)+"| ");
		lines.append(CMStrings.padRight("Name",17)+"| ");
		lines.append(CMStrings.padRight("IP",17)+"| ");
		lines.append(CMStrings.padRight("Idle",17)+"^.^N\n\r");
		Vector broken=new Vector();
		for(Session S : CMLib.sessions().allIterable())
		{
			String[] set=new String[6];
			set[0]=CMStrings.padRight(""+broken.size(),3)+"| ";
			set[1]=(S.isStopped()?"^H":"")+CMStrings.padRight(S.getStatus().toString(),9)+(S.isStopped()?"^?":"")+"| ";
			if (S.mob() != null)
			{
				set[2]=CMStrings.padRight(((S.mob().session()==S)?"Yes":"^HNO!^?"),5)+"| ";
				set[3]="^!"+CMStrings.padRight("^<LSTUSER^>"+S.mob().Name()+"^</LSTUSER^>",17)+"^?| ";
			}
			else
			{
				set[2]=CMStrings.padRight("N/A",5)+"| ";
				set[3]=CMStrings.padRight("NAMELESS",17)+"| ";
			}
			set[4]=CMStrings.padRight(S.getAddress(),17)+"| ";
			set[5]=CMStrings.padRight(CMLib.english().returnTime(S.getIdleMillis(),0)+"",17);
			broken.addElement(set);
		}
		Vector sorted=null;
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
			sorted=new Vector();
			while(broken.size()>0)
			{
				int selected=0;
				for(int s=1;s<broken.size();s++)
				{
					String[] S=(String[])broken.elementAt(s);
					if(S[sortNum].compareToIgnoreCase(((String[])broken.elementAt(selected))[sortNum])<0)
					   selected=s;
				}
				sorted.addElement(broken.elementAt(selected));
				broken.removeElementAt(selected);
			}
		}
		for(int s=0;s<sorted.size();s++)
		{
			String[] S=(String[])sorted.elementAt(s);
			for(int i=0;i<S.length;i++)
				lines.append(S[i]);
			lines.append("\n\r");
		}
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(lines.toString());
	}
	
	public void archonlist(MOB mob, Vector commands)
	{
		if(commands.size()==0)
		{
			mob.tell("List what?");
			return;
		}

		Session s=mob.session();
		if(s==null) return;

		String listWord=((String)commands.firstElement()).toUpperCase();
		String rest=(commands.size()>1)?rest=CMParms.combine(commands,1):"";
		int code;
		if(listWord.equalsIgnoreCase("sql")&&CMSecurity.isASysOp(mob))
			code=999;
		else
			code=getMyCmdCode(mob, listWord);
		if((code<0)||(listWord.length()==0))
		{
			List<String> V=getMyCmdWords(mob);
			if(V.size()==0)
				mob.tell("You are not allowed to use this command!");
			else
			{
				StringBuilder str=new StringBuilder("");
				for(int v=0;v<V.size();v++)
					if(V.get(v).length()>0)
					{
						str.append(V.get(v));
						if(v==(V.size()-2))
							str.append(", or ");
						else
						if(v<(V.size()-1))
							str.append(", ");
					}
				mob.tell("You cannot list '"+listWord+"'.  Try "+str.toString()+".");
			}
			return;
		}
		switch(code)
		{
		case 0:	s.wraplessPrintln(unlinkedExits(mob.session(),commands)); break;
		case 1: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.basicItems()).toString()); break;
		case 2: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.armor()).toString()); break;
		case 3: s.wraplessPrintln(listEnvResources(mob.session(),rest.equalsIgnoreCase("SHORT"))); break;
		case 4: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.weapons()).toString()); break;
		case 5: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.mobTypes()).toString()); break;
		case 6: s.wraplessPrintln(roomDetails(mob.session(),mob.location().getArea().getMetroMap(),mob.location()).toString()); break;
		case 7: s.wraplessPrintln(roomTypes(mob.session(),mob.location().getArea().getMetroMap(),mob.location()).toString()); break;
		case 8: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.locales()).toString()); break;
		case 9: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.behaviors()).toString()); break;
		case 10: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.exits()).toString()); break;
		case 11: s.wraplessPrintln(listRaces(s,CMClass.races(),rest.equalsIgnoreCase("SHORT")).toString()); break;
		case 12: s.wraplessPrintln(listCharClasses(s,CMClass.charClasses(),rest.equalsIgnoreCase("SHORT")).toString()); break;
		case 13: s.wraplessPrintln(listSubOps(mob.session()).toString()); break;
		case 14: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_SPELL).toString()); break;
		case 15: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_SONG).toString()); break;
		case 16: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_PRAYER).toString()); break;
		case 17: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_PROPERTY).toString()); break;
		case 18: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_THIEF_SKILL).toString()); break;
		case 19: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_COMMON_SKILL).toString()); break;
		case 20: s.println(listJournals(mob.session()).toString()); break;
		case 21: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_SKILL).toString()); break;
		case 22: s.println(listQuests(mob.session()).toString()); break;
		case 23: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_DISEASE).toString()); break;
		case 24: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_POISON).toString()); break;
		case 25: s.println(listTicks(mob.session(),CMParms.combine(commands,1)).toString()); break;
		case 26: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.miscMagic()).toString()); break;
		case 27: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.miscTech()).toString()); break;
		case 28: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.clanItems()).toString()); break;
		case 29: s.println(journalList(mob.session(),listWord).toString()); break;
		case 30: s.wraplessPrintln(roomPropertyDetails(mob.session(),mob.location().getArea(),rest).toString()); break;
		case 31:
		{
			StringBuilder str=new StringBuilder("\n\rProtected players:\n\r");
			List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
			if((protectedOnes!=null)&&(protectedOnes.size()>0))
			for(int b=0;b<protectedOnes.size();b++)
				str.append((b+1)+") "+(protectedOnes.get(b))+"\n\r");
			s.wraplessPrintln(str.toString());
			break;
		}
		case 32:
		{
			StringBuilder str=new StringBuilder("\n\rBanned names/ips:\n\r");
			List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
			if((banned!=null)&&(banned.size()>0))
			for(int b=0;b<banned.size();b++)
				str.append((b+1)+") "+(banned.get(b))+"\n\r");
			s.wraplessPrintln(str.toString());
			break;
		}
		case 33: s.wraplessPrintln(listRaceCats(s,CMClass.races(),rest.equalsIgnoreCase("SHORT")).toString()); break;
		case 34: listLog(mob,commands); break;
		case 35: listUsers(mob.session(),mob,commands); break;
		case 36: s.println(listLinkages(mob.session(),mob,rest).toString()); break;
		case 37: s.println(listReports(mob.session(),mob).toString()); break;
		case 38: s.println(listThreads(mob.session(),mob,CMParms.combine(commands,1).equalsIgnoreCase("SHORT")).toString()); break;
		case 39: s.println(listResources(mob,CMParms.combine(commands,1))); break;
		case 40: s.wraplessPrintln(reallyFindOneWays(mob.session(),commands)); break;
		case 41: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_CHANT).toString()); break;
		case 42:
		case 43: s.wraplessPrintln(CMLib.lister().reallyList(mob,CMClass.abilities(),Ability.ACODE_SUPERPOWER).toString()); break;
		case 44: s.wraplessPrintln(listComponents(mob.session())); break;
		case 45: s.wraplessPrintln(listExpertises(mob.session())); break;
		case 46: s.wraplessPrintln(CMLib.factions().listFactions()); break;
		case 47: s.wraplessPrintln(listMaterials()); break;
		case 48: s.println("\n\r^xCounter Report: NO LONGER AVAILABLE^.^N\n\r"); break;//+CMClass.getCounterReport()); break;
		case 49: listPolls(mob,commands); break;
		case 50: s.wraplessPrintln(listContent(mob,commands).toString()); break;
		case 51: s.wraplessPrintln(roomExpires(mob.session(),mob.location().getArea().getProperMap(),mob.location()).toString()); break;
		case 52: s.wraplessPrintln(listTitles(mob.session())); break;
		case 53: s.wraplessPrintln(roomResources(mob.session(),mob.location().getArea().getMetroMap(),mob.location()).toString()); break;
		case 54: s.wraplessPrintln(areaConquests(mob.session(),CMLib.map().sortedAreas()).toString()); break;
		case 55: s.wraplessPrintln(CMLib.quests().listHolidays(mob.location().getArea(),CMParms.combine(commands,1))); break;
		case 56: s.wraplessPrintln(listRecipes(mob,CMParms.combine(commands,1))); break;
		case 57: s.wraplessPrint(listHelpFileRequests(mob,CMParms.combine(commands,1))); break;
		case 58: s.wraplessPrintln(listScripts(mob.session(),mob,commands).toString()); break;
		case 59: listAccounts(mob.session(),mob,commands); break;
		case 60: s.wraplessPrintln(listClanGovernments(mob.session(),commands)); break;
		case 61: s.wraplessPrintln(listClans(mob.session(),commands)); break;
		case 62: s.println("\n\r^xDebug Settings: ^?^.^N\n\r"+CMParms.toStringList(new XVector<CMSecurity.DbgFlag>(CMSecurity.getDebugEnum()))+"\n\r"); break;
		case 63: s.println("\n\r^xDisable Settings: ^?^.^N\n\r"+CMParms.toStringList(new XVector<CMSecurity.DisFlag>(CMSecurity.getDisablesEnum()))+"\n\r"); break;
		case 64: s.wraplessPrintln(listAllQualifies(mob.session(),commands).toString()); break;
		case 65: listNews(mob,commands); break;
		case 66: listAreas(mob, commands, areaFilter); break;
		case 67: { listSessions(mob,commands); break; }
		case 68: listAreas(mob, commands, shipFilter); break;
		case 69: listAreas(mob, commands, new WorldFilter(mob.location())); break;
		case 70: listAreas(mob, commands, planetsFilter); break;
		case 71: listCurrents(mob, commands); break;
		case 999: listSql(mob,rest); break;
		default:
			s.println("List broke?!");
			break;
		}
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		List<Environmental> V=new Vector();
		commands.removeElementAt(0);
		String forWhat=null;
		if(commands.size()==0)
		{
			if(getAnyCode(mob)>=0)
			{
				archonlist(mob,commands);
				return false;
			}
			V=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
		}
		else
		{
			Vector origCommands=new XVector(commands);
			for(int c=commands.size()-2;c>=0;c--)
			{
				if(((String)commands.elementAt(c)).equalsIgnoreCase("for"))
				{
					forWhat=CMParms.combine(commands,c+1);
					for(int c1=commands.size()-1;c1>=c;c1--)
						commands.removeElementAt(c1);
					break;
				}
			}
			String what=CMParms.combine(commands,0);
			List<Environmental> V2=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
			Environmental shopkeeper=CMLib.english().fetchEnvironmental(V2,what,false);
			if((shopkeeper==null)&&(what.equals("shop")||what.equals("the shop")))
				for(int v=0;v<V2.size();v++)
					if(V2.get(v) instanceof Area)
					{ shopkeeper=V2.get(v); break;}
			if((shopkeeper!=null)
			&&(CMLib.coffeeShops().getShopKeeper(shopkeeper)!=null)
			&&(CMLib.flags().canBeSeenBy(shopkeeper,mob)))
				V.add(shopkeeper);
			else
			if(getAnyCode(mob)>=0)
			{
				archonlist(mob,origCommands);
				return false;
			}
		}
		if(V.size()==0)
		{
			mob.tell("You don't see anyone here buying or selling.");
			return false;
		}
		for(int i=0;i<V.size();i++)
		{
			Environmental shopkeeper=V.get(i);
			ShopKeeper SHOP=CMLib.coffeeShops().getShopKeeper(shopkeeper);
			String str="<S-NAME> review(s) <T-YOUPOSS> inventory";
			if(SHOP instanceof Banker)
				str="<S-NAME> review(s) <S-HIS-HER> account with <T-NAMESELF>";
			else
			if(SHOP instanceof PostOffice)
				str="<S-NAME> check(s) <S-HIS-HER> postal box with <T-NAMESELF>";
			if(forWhat!=null)str+=" for '"+forWhat+"'";
			str+=".";
			CMMsg newMsg=CMClass.getMsg(mob,shopkeeper,null,CMMsg.MSG_LIST,str);
			if(!mob.location().okMessage(mob,newMsg))
				return false;
			mob.location().send(mob,newMsg);
		}
		return false;
	}

	public boolean canBeOrdered(){return true;}


}
