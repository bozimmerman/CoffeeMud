package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class List extends StdCommand
{
	public List(){}

	private String[] access={"LIST"};
	public String[] getAccessWords(){return access;}


	public StringBuffer roomDetails(Vector these, Room likeRoom)
	{return roomDetails(these.elements(),likeRoom);}

	public StringBuffer roomDetails(Enumeration these, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
        Room thisThang=null;
        String thisOne=null;
        for(Enumeration r=these;r.hasMoreElements();)
        {
            thisThang=(Room)r.nextElement();
            thisOne=thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
				lines.append(CMStrings.padRightPreserve("^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",30)+": "+CMStrings.limit(thisThang.displayText(),43)+"\n\r");
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuffer roomExpires(Enumeration these, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("The time is: "+CMLib.time().date2String(System.currentTimeMillis())+"\n\r\n\r");
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
    public StringBuffer roomPropertyDetails(Enumeration these, Room likeRoom)
    {
        StringBuffer lines=new StringBuffer("");
        if(!these.hasMoreElements()) return lines;
        if(likeRoom==null) return lines;
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
                if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
                    lines.append(CMStrings.padRightPreserve("^<LSTROOMID^>"+thisOne+"^</LSTROOMID^>",30)+": "+CMStrings.limit(thisThang.displayText(),23)+CMStrings.limit(" ("+t.landOwner()+", $"+t.landPrice()+")",20)+"\n\r");
            }
        }
        lines.append("\n\r");
        return lines;
    }
	public StringBuffer roomTypes(Vector these, Room likeRoom)
	{return roomTypes(these.elements(),likeRoom);}
	public StringBuffer roomTypes(Enumeration these, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
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

	public StringBuffer roomResources(Vector these, Room likeRoom)
	{return roomResources(these.elements(),likeRoom);}
	public StringBuffer roomResources(Enumeration these, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer(CMStrings.padRight("Room ID#",30)+"| "
										   +CMStrings.padRight("Room Type",15)+"| "
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
				lines.append(CMStrings.padRight(thisOne,30)+": ");
				lines.append(CMStrings.padRight(thisThang.ID(),15)+": ");
				String thisRsc="-";
				if(thisThang.myResource()>=0) 
					thisRsc=RawMaterial.RESOURCE_DESCS[thisThang.myResource()&RawMaterial.RESOURCE_MASK];
				lines.append(thisRsc+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}
	
	public StringBuffer areaConquests(Enumeration these)
	{
		StringBuffer lines=new StringBuffer(CMStrings.padRight("Area",26)+"| "
										   +CMStrings.padRight("Clan",40)+"| "
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
				lines.append(CMStrings.padRight(thisOne,26)+": ");
				String controller="The Archons";
				String fully="";
				LegalBehavior law=CMLib.law().getLegalBehavior(thisThang);
				if(law!=null)
				{
					controller=law.rulingClan();
					fully=""+((controller.length()>0)&&law.isFullyControlledByClan());
				}
				lines.append(CMStrings.padRight(controller,40)+": ");
				lines.append(fully+"\n\r");
			}
		}
		lines.append("\n\r");
		return lines;
	}
	
	public void dumpThreadGroup(StringBuffer lines,ThreadGroup tGroup, boolean ignoreZeroTickThreads)
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
                if((tArray[i] instanceof TickableGroup)
                &&(((TickableGroup)tArray[i]).lastTicked()!=null)
                &&(((TickableGroup)tArray[i]).lastTicked().getTickStatus()==0))
                    continue;
                if((tArray[i] instanceof Tickable)
                &&(((Tickable)tArray[i]).getTickStatus()==0))
                    continue;
                
                lines.append(tArray[i].isAlive()? "  ok   " : " BAD!  ");
                lines.append(CMStrings.padRight(tArray[i].getName(),20)+": ");
                if(tArray[i] instanceof Session)
                {
                    Session S=(Session)tArray[i];
                    lines.append("Session status "+S.getStatus()+"-"+CMParms.combine(S.previousCMD(),0) + "\n\r");
                }
                else
                if(tArray[i] instanceof Tickable)
                {
                    Tickable T=(Tickable)tArray[i];
                    lines.append("Tickable "+T.ID()+"-"+T.name()+"-"+T.getTickStatus() + "\n\r");
                }
                else
                if((tArray[i] instanceof TickableGroup)
                &&(((TickableGroup)tArray[i]).lastTicked()!=null))
                    lines.append("Tick "+tArray[i].getName()+" "
                            +((TickableGroup)tArray[i]).lastTicked().ID()
                            +"-"+((TickableGroup)tArray[i]).lastTicked().name()
                            +"-"+((TickableGroup)tArray[i]).lastTicked().getTickStatus() 
                            +" ("+CMLib.threads().getTickStatusSummary(((TickableGroup)tArray[i]).lastTicked())+")\n\r");
                else
                {
                    String status=CMLib.threads().getServiceThreadSummary(tArray[i]);
                    lines.append("Thread "+tArray[i].getName() + status+"\n\r");
                }
			}
		}

		if (agc > 0)
		{
			lines.append("{\n\r");
			for (int i = 0; i<agc; ++i)
			{
				if (tgArray[i] != null)
					dumpThreadGroup(lines,tgArray[i],ignoreZeroTickThreads);
			}
			lines.append("}\n\r");
		}
	}


	public StringBuffer listThreads(MOB mob, boolean ignoreZeroTickThreads)
	{
		StringBuffer lines=new StringBuffer("^xStatus|Name                 ^.^?\n\r");
		try
		{
			ThreadGroup topTG = Thread.currentThread().getThreadGroup();
			while (topTG != null && topTG.getParent() != null)
				topTG = topTG.getParent();
			if (topTG != null)
				dumpThreadGroup(lines,topTG,ignoreZeroTickThreads);

		}
		catch (Exception e)
		{
			lines.append ("\n\rBastards! Exception while listing threads: " + e.getMessage() + "\n\r");
		}
		return lines;

	}
    
    
    

	public StringBuffer listLinkages(MOB mob)
	{
	    Faction useFaction=null;
	    for(Enumeration e=CMLib.factions().factionSet().elements();e.hasMoreElements();)
	    {
	        Faction F=(Faction)e.nextElement();
	        if(F.showinspecialreported()) useFaction=F;
	    }
		StringBuffer buf=new StringBuffer("Links: \n\r");
		Vector areaLinkGroups=new Vector();
		for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			buf.append(A.name()+"\t"+A.numberOfProperIDedRooms()+" rooms\t");
			if(!A.getProperMap().hasMoreElements())
			{
				buf.append("\n\r");
				continue;
			}
			Vector linkedGroups=new Vector();
			int numMobs=0;
			int totalAlignment=0;
			int totalLevels=0;
			for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
				{
					Vector myVec=null;
					Vector clearVec=null;
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						Room R2=R.rawDoors()[d];
						if(R2!=null)
						{
							for(int g=0;g<linkedGroups.size();g++)
							{
								Vector G=(Vector)linkedGroups.elementAt(g);
								if(G.size()==0)
									clearVec=G;
								else
								if(G.contains(R2))
								{
									if(myVec==null)
									{
										myVec=G;
										myVec.addElement(R);
									}
									else
									if(myVec!=G)
									{
										for(int g2=0;g2<myVec.size();g2++)
											G.addElement(myVec.elementAt(g2));
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
							clearVec.addElement(R);
						else
						{
							clearVec=new Vector();
							clearVec.addElement(R);
							linkedGroups.addElement(clearVec);
						}
					}
				}
				for(int g=linkedGroups.size()-1;g>=0;g--)
				{
					if(((Vector)linkedGroups.elementAt(g)).size()==0)
						linkedGroups.removeElementAt(g);
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
						totalLevels+=M.envStats().level();
					}
				}

			}
			StringBuffer ext=new StringBuffer("links ");
			Vector myVec=null;
			Vector clearVec=null;
			for(Enumeration r=A.getCompleteMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R2=R.rawDoors()[d];
					if((R2!=null)&&(R2.getArea()!=R.getArea()))
					{
						ext.append(Directions.getDirectionName(d)+" to "+R2.getArea().name()+" ("+R.roomID()+"/"+R2.roomID()+") ");
						for(int g=0;g<areaLinkGroups.size();g++)
						{
							Vector G=(Vector)areaLinkGroups.elementAt(g);
							if(G.size()==0)
								clearVec=G;
							else
							if(G.contains(R2.getArea()))
							{
								if(myVec==null)
								{
									myVec=G;
									myVec.addElement(R.getArea());
								}
								else
								if(myVec!=G)
								{
									for(int g2=0;g2<myVec.size();g2++)
										G.addElement(myVec.elementAt(g2));
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
					clearVec.addElement(A);
				else
				{
					clearVec=new Vector();
					clearVec.addElement(A);
					areaLinkGroups.addElement(clearVec);
				}
			}
			if(numMobs>0)
				buf.append(numMobs+" mobs\t"+(totalLevels/numMobs)+" avg levels\t");
            if((numMobs>0)&&(useFaction!=null)&&(CMLib.factions().getFaction(useFaction.factionID())!=null))
                buf.append((totalAlignment/numMobs)+" avg "+useFaction.name());
			if(linkedGroups.size()>0)
			{
				buf.append("\tgroups: "+linkedGroups.size()+" sizes: ");
				for(Enumeration r=linkedGroups.elements();r.hasMoreElements();)
					buf.append(((Vector)r.nextElement()).size()+" ");
			}
			buf.append("\t"+ext.toString()+"\n\r");
		}
		buf.append("There were "+areaLinkGroups.size()+" area groups:");
		for(int g=areaLinkGroups.size()-1;g>=0;g--)
		{
			if(((Vector)areaLinkGroups.elementAt(g)).size()==0)
				areaLinkGroups.removeElementAt(g);
		}
		StringBuffer unlinkedGroups=new StringBuffer("");
		for(Enumeration r=areaLinkGroups.elements();r.hasMoreElements();)
		{
			Vector V=(Vector)r.nextElement();
			buf.append(V.size()+" ");
			if(V.size()<4)
			{
				for(int v=0;v<V.size();v++)
					unlinkedGroups.append(((Area)V.firstElement()).name()+"\t");
				unlinkedGroups.append("|\t");
			}

		}
		buf.append("\n\r");
		buf.append("Small Group Areas:\t"+unlinkedGroups.toString());
		Log.sysOut("Lister",buf.toString());
		return buf;
	}


	public StringBuffer journalList(String partialjournal)
	{
		StringBuffer buf=new StringBuffer("");
        String journal=null;
        for(int i=0;i<CMLib.journals().getNumCommandJournals();i++)
            if((CMLib.journals().getCommandJournalName(i).toUpperCase().trim()+"S").startsWith(partialjournal.toUpperCase().trim()))
                journal=CMLib.journals().getCommandJournalName(i).toUpperCase().trim();
        if(journal==null) return buf;
		Vector V=CMLib.database().DBReadJournal("SYSTEM_"+journal+"S");
		if(V!=null)
		{
			buf.append("\n\r^x"+CMStrings.padRight("#",5)+CMStrings.padRight("From",10)+" Entry^.^N\n\r");
			buf.append("---------------------------------------------\n\r");
			for(int j=0;j<V.size();j++)
			{
				Vector entry=(Vector)V.elementAt(j);
				String from=(String)entry.elementAt(1);
				//String date=(String)entry.elementAt(2);
				//String to=(String)entry.elementAt(3);
				//String subject=(String)entry.elementAt(4);
				String message=(String)entry.elementAt(5);
				buf.append(CMStrings.padRight((j+1)+"",3)+") "+CMStrings.padRight(from,10)+" "+message+"\n\r");
			}
		}
		return buf;
	}

	public StringBuffer listReports(MOB mob)
	{
		mob.tell("\n\r^xCoffeeMud System Report:^.^N");
		try
		{
			System.gc();
			Thread.sleep(1500);
		}catch(Exception e){}
		StringBuffer buf=new StringBuffer("");
		long totalTime=System.currentTimeMillis()-CMSecurity.getStartTime();
		buf.append("The system has been running for ^H"+CMLib.english().returnTime(totalTime,0)+"^?.\n\r");
		long free=Runtime.getRuntime().freeMemory()/1000;
		long total=Runtime.getRuntime().totalMemory()/1000;
		buf.append("The system is utilizing ^H"+(total-free)+"^?kb out of ^H"+total+"^?kb.\n\r");
		buf.append("\n\r^xService Engine report:^.^N\n\r");
		String totalTickers=CMLib.threads().systemReport("totalTickers");
		String tickGroupSize=CMLib.threads().systemReport("TICKGROUPSIZE");
		long totalMillis=CMath.s_long(CMLib.threads().systemReport("totalMillis"));
		long totalTicks=CMath.s_long(CMLib.threads().systemReport("totalTicks"));
		String topGroupNumber=CMLib.threads().systemReport("topGroupNumber");
		long topGroupMillis=CMath.s_long(CMLib.threads().systemReport("topGroupMillis"));
		long topGroupTicks=CMath.s_long(CMLib.threads().systemReport("topGroupTicks"));
		long topObjectMillis=CMath.s_long(CMLib.threads().systemReport("topObjectMillis"));
		long topObjectTicks=CMath.s_long(CMLib.threads().systemReport("topObjectTicks"));
		buf.append("There are ^H"+totalTickers+"^? ticking objects in ^H"+tickGroupSize+"^? threads.\n\r");
		buf.append("The ticking objects have consumed: ^H"+CMLib.english().returnTime(totalMillis,totalTicks)+"^?.\n\r");
		buf.append("The most active group, #^H"+topGroupNumber+"^?, has consumed: ^H"+CMLib.english().returnTime(topGroupMillis,topGroupTicks)+"^?.\n\r");
		String topObjectClient=CMLib.threads().systemReport("topObjectClient");
		String topObjectGroup=CMLib.threads().systemReport("topObjectGroup");
		if(topObjectClient.length()>0)
		{
			buf.append("The most active object has been '^H"+topObjectClient+"^?', from group #^H"+topObjectGroup+"^?.\n\r");
			buf.append("That object has consumed: ^H"+CMLib.english().returnTime(topObjectMillis,topObjectTicks)+"^?.\n\r");
		}
		buf.append("\n\r");
		buf.append("^xSave Thread report:^.^N\n\r");
		long saveThreadMilliTotal=CMath.s_long(CMLib.threads().systemReport("saveThreadMilliTotal"));
		long saveThreadTickTotal=CMath.s_long(CMLib.threads().systemReport("saveThreadTickTotal"));
		buf.append("The Save Thread has consumed: ^H"+CMLib.english().returnTime(saveThreadMilliTotal,saveThreadTickTotal)+" ("+CMLib.threads().systemReport("saveThreadStatus")+")^?.\n\r");
		buf.append("\n\r");
		buf.append("^xUtility Thread report:^.^N\n\r");
		long utilThreadMilliTotal=CMath.s_long(CMLib.threads().systemReport("utilThreadMilliTotal"));
		long utilThreadTickTotal=CMath.s_long(CMLib.threads().systemReport("utilThreadTickTotal"));
		buf.append("The Utility Thread has consumed: ^H"+CMLib.english().returnTime(utilThreadMilliTotal,utilThreadTickTotal)+" ("+CMLib.threads().systemReport("utilThreadStatus")+")^?.\n\r");
		buf.append("\n\r");
		buf.append("^xSession report:^.^N\n\r");
		long totalMOBMillis=CMath.s_long(CMLib.threads().systemReport("totalMOBMillis"));
		long totalMOBTicks=CMath.s_long(CMLib.threads().systemReport("totalMOBTicks"));
		buf.append("There are ^H"+CMLib.sessions().size()+"^? ticking players logged on.\n\r");
		buf.append("The ticking players have consumed: ^H"+CMLib.english().returnTime(totalMOBMillis,totalMOBTicks)+"^?.\n\r");
		long topMOBMillis=CMath.s_long(CMLib.threads().systemReport("topMOBMillis"));
		long topMOBTicks=CMath.s_long(CMLib.threads().systemReport("topMOBTicks"));
		String topMOBClient=CMLib.threads().systemReport("topMOBClient");
		if(topMOBClient.length()>0)
		{
			buf.append("The most active mob has been '^H"+topMOBClient+"^?'\n\r");
			buf.append("That mob has consumed: ^H"+CMLib.english().returnTime(topMOBMillis,topMOBTicks)+"^?.\n\r");
		}
		return buf;
	}

	public void listUsers(MOB mob, Vector commands)
	{
		if(commands.size()==0) return;
		commands.removeElementAt(0);
		int sortBy=-1;
		if(commands.size()>0)
		{
			String rest=CMParms.combine(commands,0).toUpperCase();
			if("RACE".startsWith(rest))
				sortBy=2;
			else
			if("IP".startsWith(rest))
				sortBy=7;
			else
			if("CLASS".startsWith(rest))
				sortBy=1;
			else
			if("CHARACTER".startsWith(rest)||"NAME".startsWith(rest))
				sortBy=0;
			else
			if("LEVEL".startsWith(rest)||"LVL".startsWith(rest))
				sortBy=3;
			else
			if("AGE".startsWith(rest)||"HOURS".startsWith(rest))
				sortBy=4;
			else
			if("DATE".startsWith(rest)||"LAST".startsWith(rest))
				sortBy=5;
			else
			if("EMAIL".startsWith(rest))
				sortBy=6;
			else
			{
				mob.tell("Unrecognized sort criteria: "+rest);
				return;
			}
		}
		StringBuffer head=new StringBuffer("");
		head.append("[");
		head.append(CMStrings.padRight("Race",8)+" ");
		head.append(CMStrings.padRight("Class",10)+" ");
		head.append(CMStrings.padRight("Lvl",4)+" ");
		head.append(CMStrings.padRight("Hours",5)+" ");
		switch(sortBy){
		case 6: head.append(CMStrings.padRight("E-Mail",23)+" "); break; 
		case 7: head.append(CMStrings.padRight("IP Address",23)+" "); break; 
		default: head.append(CMStrings.padRight("Last",18)+" "); break;
		}
			
		head.append("] Character name\n\r");
		Vector allUsers=CMLib.database().getExtendedUserList();
		Vector oldSet=allUsers;
		int showBy=sortBy;
		while((oldSet.size()>0)&&(sortBy>=0)&&(sortBy<=7))
		{
			if(oldSet==allUsers) allUsers=new Vector();
			if((sortBy<3)||(sortBy>4))
			{
				Vector selected=(Vector)oldSet.firstElement();
				for(int u=1;u<oldSet.size();u++)
				{
					Vector V=(Vector)oldSet.elementAt(u);
					if(((String)selected.elementAt(sortBy)).compareTo(((String)V.elementAt(sortBy)))>0)
					   selected=V;
				}
				if(selected!=null)
				{
					oldSet.removeElement(selected);
					allUsers.addElement(selected);
				}
			}
			else
			{
				Vector selected=(Vector)oldSet.firstElement();
				for(int u=1;u<oldSet.size();u++)
				{
					Vector V=(Vector)oldSet.elementAt(u);
					if(CMath.s_long((String)selected.elementAt(sortBy))>CMath.s_long(((String)V.elementAt(sortBy))))
					   selected=V;
				}
				if(selected!=null)
				{
					oldSet.removeElement(selected);
					allUsers.addElement(selected);
				}
			}
		}

		for(int u=0;u<allUsers.size();u++)
		{
			Vector U=(Vector)allUsers.elementAt(u);

			head.append("[");
			head.append(CMStrings.padRight((String)U.elementAt(2),8)+" ");
			head.append(CMStrings.padRight((String)U.elementAt(1),10)+" ");
			head.append(CMStrings.padRight((String)U.elementAt(3),4)+" ");
			long age=Math.round(CMath.div(CMath.s_long((String)U.elementAt(4)),60.0));
			head.append(CMStrings.padRight(""+age,5)+" ");
			switch(showBy){
			case 6: head.append(CMStrings.padRight((String)U.elementAt(6),23)+" "); break; 
			case 7: head.append(CMStrings.padRight((String)U.elementAt(7),23)+" "); break; 
			default: head.append(CMStrings.padRight(CMLib.time().date2String(CMath.s_long((String)U.elementAt(5))),18)+" "); break;
			}
			head.append("] "+CMStrings.padRight("^<LSTUSER^>"+((String)U.elementAt(0))+"^</LSTUSER^>",15));
			head.append("\n\r");
		}
		mob.tell(head.toString());
	}

	public StringBuffer listRaces(Enumeration these, boolean shortList)
	{
		StringBuffer lines=new StringBuffer("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
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
                                        +" ("+thisThang.racialCategory()+")",25));
		}
		lines.append("\n\r");
		return lines;
	}
    public StringBuffer listRaceCats(Enumeration these, boolean shortList)
    {
        StringBuffer lines=new StringBuffer("");
        if(!these.hasMoreElements()) return lines;
        int column=0;
        Vector raceCats=new Vector();
        Race R=null;
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
            lines.append(CMStrings.padRight(raceCat,25));
        }
        lines.append("\n\r");
        return lines;
    }
    
	public StringBuffer listQuests()
	{
		StringBuffer buf=new StringBuffer("");
		if(CMLib.quests().numQuests()==0)
			buf.append("No quests loaded.");
		else
		{
			buf.append("\n\r^xQuest Report:^.^N\n\r");
			buf.append("\n\r^x"+CMStrings.padRight("#",5)+CMStrings.padRight("Name",20)+" Status^.^N\n\r");
			for(int i=0;i<CMLib.quests().numQuests();i++)
			{
				Quest Q=CMLib.quests().fetchQuest(i);
				if(Q!=null)
				{
					buf.append(CMStrings.padRight(""+(i+1),5)+CMStrings.padRight("^<LSTQUEST^>"+Q.name()+"^</LSTQUEST^>",20)+" ");
					if(Q.running())
					{
						if(Q.isCopy())
							buf.append("copy running ("+Q.minsRemaining()+" mins left)");
						else
							buf.append("running ("+Q.minsRemaining()+" mins left)");
					}
					else
					if(Q.waiting())
						buf.append("waiting ("+Q.waitRemaining()+" ticks left)");
					else
						buf.append("loaded");
					buf.append("^N\n\r");
				}
			}
		}
		return buf;
	}

    public StringBuffer listJournals()
    {
        StringBuffer buf=new StringBuffer("");
        Vector journals=CMLib.database().DBReadJournal(null);
        
        if(journals.size()==0)
            buf.append("No journals exits.");
        else
        {
            buf.append("\n\r^xJournals List:^.^N\n\r");
            buf.append("\n\r^x"+CMStrings.padRight("#",5)+CMStrings.padRight("Name",30)+" Messages^.^N\n\r");
            for(int i=0;i<journals.size();i++)
            {
                String journal=(String)journals.elementAt(i);
                int messages=CMLib.database().DBCountJournal(journal,null,null);
                buf.append(CMStrings.padRight(""+(i+1),5)+CMStrings.padRight(journal,30)+" "+messages);
                buf.append("^N\n\r");
            }
        }
        return buf;
    }

	public StringBuffer listTicks(String whichTickTock)
	{
		StringBuffer msg=new StringBuffer("\n\r");
		boolean activeOnly=false;
		String mask=null;
		if("ACTIVE".startsWith(whichTickTock.toUpperCase())&&(whichTickTock.length()>0))
		{
		    activeOnly=true;
		    whichTickTock="";
		}
		if(!activeOnly)
			msg.append(CMStrings.padRight("Grp",4)+CMStrings.padRight("Client",20)+" "+CMStrings.padRight("ID",3)+CMStrings.padRight("Status",8));
		msg.append(CMStrings.padRight("Grp",4)+CMStrings.padRight("Client",20)+" "+CMStrings.padRight("ID",3)+CMStrings.padRight("Status",8)+"\n\r");
		int col=0;
		int numGroups=CMath.s_int(CMLib.threads().tickInfo("tickGroupSize"));
		int whichTick=-1;
		if(CMath.isInteger(whichTickTock)&&(whichTickTock.length()>0)) 
		    whichTick=CMath.s_int(whichTickTock);
		else
		if(whichTickTock.length()>0)
			mask=whichTickTock.toUpperCase().trim();
		if((mask!=null)&&(mask.length()==0)) mask=null;
        String chunk=null;
		for(int v=0;v<numGroups;v++)
		{
			int tickersSize=CMath.s_int(CMLib.threads().tickInfo("tickersSize"+v));
			if((whichTick<0)||(whichTick==v))
			for(int t=0;t<tickersSize;t++)
			{
				long tickerlaststartdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststartmillis"+v+"-"+t));
				long tickerlaststopdate=CMath.s_long(CMLib.threads().tickInfo("tickerlaststopmillis"+v+"-"+t));
				boolean isActive=(tickerlaststopdate<tickerlaststartdate);			
				if((!activeOnly)||(isActive))
				{
					String name=CMLib.threads().tickInfo("tickerName"+v+"-"+t);
					if((mask==null)||(name.toUpperCase().indexOf(mask)>=0))
					{
						String id=CMLib.threads().tickInfo("tickerID"+v+"-"+t);
						String status=CMLib.threads().tickInfo("tickercodeword"+v+"-"+t);
						boolean suspended=CMath.s_bool(CMLib.threads().tickInfo("tickerSuspended"+v+"-"+t));
						if(((col++)>=2)||(activeOnly))
						{
							msg.append("\n\r");
							col=1;
						}
                        chunk=CMStrings.padRight(""+v,4)
                           +CMStrings.padRight(name,22)
                           +" "+CMStrings.padRight(id+"",3)
                           +CMStrings.padRight((activeOnly?(status+(suspended?"*":"")):status+(suspended?"*":"")),8);
						msg.append(chunk);
					}
				}
			}
		}
		return msg;
	}

	public StringBuffer listSubOps(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		for(Enumeration a=CMLib.map().sortedAreas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			msg.append(CMStrings.padRight(A.Name(),25)+": ");
			if(A.getSubOpList().length()==0)
				msg.append("No Area staff defined.\n\r");
			else
				msg.append(A.getSubOpList()+"\n\r");
		}
		return msg;
	}

	protected String reallyFindOneWays(MOB mob, Vector commands)
	{
		StringBuffer str=new StringBuffer("");
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
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


	protected String unlinkedExits(MOB mob, Vector commands)
	{
		StringBuffer str=new StringBuffer("");
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R2=R.rawDoors()[d];
					Exit E2=R.rawExits()[d];
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
		Vector keySet=Resources.findResourceKeys(parm);
		if(keySet.size()==1)
		{
			String key=(String)keySet.firstElement();
			StringBuffer str=new StringBuffer("^x"+key.toString()+"^?\n\r");
			Object o=Resources.getResource(key);
			if(o instanceof Vector) str.append(CMParms.toStringList((Vector)o));
			else
			if(o instanceof Hashtable) str.append(CMParms.toStringList((Hashtable)o));
			else
			if(o instanceof HashSet) str.append(CMParms.toStringList((HashSet)o));
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
		Enumeration keys=keySet.elements();
		return CMLib.lister().reallyList2Cols(keys,-1,null).toString();
	}
	
    public String listMaterials()
    {
        return CMParms.toStringList(RawMaterial.MATERIAL_DESCS);
    }
	public String listEnvResources(boolean shortList)
	{
        if(shortList)
            return CMParms.toStringList(RawMaterial.RESOURCE_DESCS);
		StringBuffer str=new StringBuffer("");
        for(int i=0;i<RawMaterial.RESOURCE_DESCS.length;i++)
            str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(RawMaterial.RESOURCE_DESCS[i].toLowerCase()),16));
		str.append(CMStrings.padRight("Resource",15)+" ");
		str.append(CMStrings.padRight("Material",10)+" ");
		str.append(CMStrings.padRight("Val",3)+" ");
		str.append(CMStrings.padRight("Freq",4)+" ");
		str.append(CMStrings.padRight("Str",3)+" ");
		str.append("Locales\n\r");
		for(int i=0;i<RawMaterial.RESOURCE_DESCS.length;i++)
		{
			str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(RawMaterial.RESOURCE_DESCS[i].toLowerCase()),16));
			str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(RawMaterial.MATERIAL_DESCS[(RawMaterial.RESOURCE_DATA[i][0]&RawMaterial.MATERIAL_MASK)>>8].toLowerCase()),11));
			str.append(CMStrings.padRight(""+RawMaterial.RESOURCE_DATA[i][1],4));
			str.append(CMStrings.padRight(""+RawMaterial.RESOURCE_DATA[i][2],5));
			str.append(CMStrings.padRight(""+RawMaterial.RESOURCE_DATA[i][3],4));
			StringBuffer locales=new StringBuffer("");
			for(Enumeration e=CMClass.locales();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				if(!(R instanceof GridLocale))
					if((R.resourceChoices()!=null)&&(R.resourceChoices().contains(new Integer(RawMaterial.RESOURCE_DATA[i][0]))))
						locales.append(R.ID()+" ");
			}
			while(locales.length()>36)
			{
				str.append(locales.toString().substring(0,36)+"\n\r"+CMStrings.padRight(" ",40));
				locales=new StringBuffer(locales.toString().substring(36));
			}
			str.append(locales.toString());
			str.append("\n\r");
		}
		return str.toString();
	}

	public Vector getMyCmdWords(MOB mob)
	{
		Vector V=new Vector();
		for(int i=0;i<SECURITY_LISTMAP.length;i++)
		{
			String[] cmd=SECURITY_LISTMAP[i];
            if(cmd.length==0) continue;
			for(int c=1;c<cmd.length;c++)
				if(CMSecurity.isAllowed(mob,mob.location(),cmd[c])
				||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
				{ V.addElement(cmd[0]); break;}
		}
        for(int i=0;i<CMLib.journals().getNumCommandJournals();i++)
        if((CMSecurity.isAllowed(mob,mob.location(),CMLib.journals().getCommandJournalName(i)))
                ||CMSecurity.isAllowed(mob,mob.location(),"KILL"+CMLib.journals().getCommandJournalName(i)+"S")
                ||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
            V.addElement(CMLib.journals().getCommandJournalName(i)+"S");
		return V;
	}

	public int getMyCmdCode(MOB mob, String s)
	{
		s=s.toUpperCase().trim();
		for(int i=0;i<SECURITY_LISTMAP.length;i++)
		{
			String[] cmd=SECURITY_LISTMAP[i];
            if(cmd.length==0) continue;
			if(cmd[0].startsWith(s))
			for(int c=1;c<cmd.length;c++)
				if(CMSecurity.isAllowed(mob,mob.location(),cmd[c])
				||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
				{ return i;}
		}
        for(int i=0;i<CMLib.journals().getNumCommandJournals();i++)
            if((CMLib.journals().getCommandJournalName(i)+"S").startsWith(s)
                    &&((CMSecurity.isAllowed(mob,mob.location(),CMLib.journals().getCommandJournalName(i)))
                            ||CMSecurity.isAllowed(mob,mob.location(),"KILL"+CMLib.journals().getCommandJournalName(i)+"S")
                            ||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN")))
                return 29;
		return -1;
	}

	public int getAnyCode(MOB mob)
	{
		for(int i=0;i<SECURITY_LISTMAP.length;i++)
		{
			String[] cmd=SECURITY_LISTMAP[i];
            if(cmd.length==0) continue;
			for(int c=1;c<cmd.length;c++)
				if(CMSecurity.isAllowed(mob,mob.location(),cmd[c])
				||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
				{ return i;}
		}
        for(int i=0;i<CMLib.journals().getNumCommandJournals();i++)
            if((CMSecurity.isAllowed(mob,mob.location(),CMLib.journals().getCommandJournalName(i)))
                    ||CMSecurity.isAllowed(mob,mob.location(),"KILL"+CMLib.journals().getCommandJournalName(i)+"S")
                    ||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
                return 29;
		return -1;
	}
	
    public String listComponents(){
        StringBuffer buf=new StringBuffer("^xAll Defined Spells and required components: ^N\n\r");
        for(Enumeration e=CMLib.ableMapper().getAbilityComponentMap().keys();e.hasMoreElements();)
        {
            String ID=(String)e.nextElement();
            DVector DV=(DVector)CMLib.ableMapper().getAbilityComponentMap().get(ID);
            if(DV!=null)
                buf.append(CMStrings.padRight(ID,20)+": "+CMLib.ableMapper().getAbilityComponentDesc(null,ID)+"\n\r");
        }
        if(buf.length()==0) return "None defined.";
        return buf.toString();
    }
    
    public String listExpertises()
    {
        StringBuffer buf=new StringBuffer("^xAll Defined Expertise Codes: ^N\n\r");
        for(Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
        {
            ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)e.nextElement();
            buf.append(CMStrings.padRight("^Z"+def.ID,20)+"^?: "+CMStrings.padRight(def.name,20)+": "+CMLib.masking().maskDesc(def.allRequirements())+"\n\r");
        }
        if(buf.length()==0) return "None defined.";
        return buf.toString();
    }
    
    public String listTitles()
    {
        StringBuffer buf=new StringBuffer("^xAll Defined Auto-Titles: ^N\n\r");
        for(Enumeration e=CMLib.login().autoTitles();e.hasMoreElements();)
        {
            String title=(String)e.nextElement();
            String maskDesc=CMLib.masking().maskDesc(CMLib.login().getAutoTitleMask(title));
            buf.append(CMStrings.padRight(title,30)+": "+maskDesc+"\n\r");
        }
        if(buf.length()==0) return "None defined.";
        return buf.toString();
    }
    
	public final static String[][] SECURITY_LISTMAP={
		/*00*/{"UNLINKEDEXITS","CMDEXITS","CMDROOMS","CMDAREAS"},
		/*01*/{"ITEMS","CMDITEMS"},
		/*02*/{"ARMOR","CMDITEMS"},
		/*03*/{"ENVRESOURCES","CMDITEMS","CMDROOMS","CMDAREAS"},
		/*04*/{"WEAPONS","CMDITEMS"},
		/*05*/{"MOBS","CMDMOBS"},
		/*06*/{"ROOMS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*07*/{"AREA","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*08*/{"LOCALES","CMDROOMS"},
		/*09*/{"BEHAVIORS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*10*/{"EXITS","CMDEXITS"},
		/*11*/{"RACES","CMDRACES","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS"},
		/*12*/{"CLASSES","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDCLASSES"},
		/*13*/{"STAFF","CMDAREAS"},
		/*14*/{"SPELLS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*15*/{"SONGS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*16*/{"PRAYERS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*17*/{"PROPERTIES","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*18*/{"THIEFSKILLS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*19*/{"COMMON","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*20*/{"JOURNALS","JOURNALS"},
		/*21*/{"SKILLS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*22*/{"QUESTS","CMDQUESTS"},
		/*23*/{"DISEASES","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*24*/{"POISONS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*25*/{"TICKS","LISTADMIN"},
		/*26*/{"MAGIC","CMDITEMS"},
		/*27*/{"TECH","CMDITEMS"},
		/*28*/{"CLANITEMS","CMDITEMS"},
		/*29*/{"COMMANDJOURNAL",""}, // blank, but used!
        /*30*/{"REALESTATE","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*31*/{"NOPURGE","NOPURGE"},
		/*32*/{"BANNED","BAN"},
        /*33*/{"RACECATS","CMDRACES","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS"},
		/*34*/{"LOG","LISTADMIN"},
		/*35*/{"USERS","CMDPLAYERS","STAT"},
		/*36*/{"LINKAGES","CMDAREAS"},
		/*37*/{"REPORTS","LISTADMIN"},
		/*38*/{"THREADS","LISTADMIN"},
		/*39*/{"RESOURCES","LOADUNLOAD"},
		/*40*/{"ONEWAYDOORS","CMDEXITS","CMDROOMS","CMDAREAS"},
		/*41*/{"CHANTS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*42*/{"POWERS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*43*/{"SUPERPOWERS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES","CMDABILITIES"},
		/*44*/{"COMPONENTS","LISTADMIN","COMPONENTS"},
        /*45*/{"EXPERTISES","LISTADMIN","EXPERTISES"},
        /*46*/{"FACTIONS","LISTADMIN","CMDFACTIONS"},
        /*47*/{"MATERIALS","CMDITEMS","CMDROOMS","CMDAREAS"},
        /*48*/{"OBJCOUNTERS","LISTADMIN"},
        /*49*/{"POLLS","POLLS","LISTADMIN"},
        /*50*/{"CONTENTS","CMDROOMS","CMDITEMS","CMDMOBS","CMDAREAS"},
		/*51*/{"EXPIRES","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
        /*52*/{"TITLES","LISTADMIN","TITLES"},
		/*53*/{"AREARESOURCES","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*54*/{"CONQUERED","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
        /*55*/{"HOLIDAYS","LISTADMIN","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
	};

    public StringBuffer listContent(MOB mob, Vector commands)
    {
        commands.removeElementAt(0);
        Enumeration roomsToDo=null;
        String rest=CMParms.combine(commands,0);
        if(rest.equalsIgnoreCase("area"))
            roomsToDo=mob.location().getArea().getMetroMap();
        else
        if(rest.trim().length()==0)
            roomsToDo=CMParms.makeVector(mob.location()).elements();
        else
        {
            Area A=CMLib.map().findArea(rest);
            if(A!=null)
                roomsToDo=A.getMetroMap();
            else
            {
                Room R=CMLib.map().getRoom(rest);
                if(R!=null)
                    roomsToDo=CMParms.makeVector(mob.location()).elements();
                else
                    return new StringBuffer("There's no such place as '"+rest+"'");
            }
        }
        StringBuffer buf=new StringBuffer("");
        Room R=null;
        Room TR=null;
        Vector set=null;
        for(;roomsToDo.hasMoreElements();)
        {
            R=(Room)roomsToDo.nextElement();
            if(R.roomID().length()==0) continue;
            set=CMLib.database().DBReadRoomData(CMLib.map().getExtendedRoomID(R),false);
            if((set==null)||(set.size()==0))
                buf.append("'"+CMLib.map().getExtendedRoomID(R)+"' could not be read from the database!\n\r");
            else
            {
                TR=(Room)set.elements().nextElement();
                CMLib.database().DBReadContent(TR,set);
                buf.append("\n\r^NRoomID: "+CMLib.map().getExtendedRoomID(TR)+"\n\r");
                for(int m=0;m<TR.numInhabitants();m++)
                {
                    MOB M=TR.fetchInhabitant(m);
                    if(M==null) continue;
                    buf.append("^M"+CMStrings.padRight(M.ID(),15)+": "+CMStrings.padRight(M.displayText(),35)+": "
                    			+CMStrings.padRight(M.envStats().level()+"",3)+": "
                    			+CMLib.flags().getAlignmentName(M)
		                		+"^N\n\r");
                    for(int i=0;i<M.inventorySize();i++)
                    {
                        Item I=M.fetchInventory(i);
                        if(I!=null)
                            buf.append("    ^I"+CMStrings.padRight(I.ID(),15)
                            		+": "+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),35)+": "
                        			+CMStrings.padRight(I.envStats().level()+"",3)+": "
                            		+"^N"+((I.container()!=null)?I.Name():"")+"\n\r");
                    }
                }
                for(int i=0;i<TR.numItems();i++)
                {
                    Item I=TR.fetchItem(i);
                    if(I!=null)
                        buf.append("^I"+CMStrings.padRight(I.ID(),15)+": "
                        		+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),35)+": "
                    			+CMStrings.padRight(I.envStats().level()+"",3)+": "
                        		+"^N"+((I.container()!=null)?I.Name():"")+"\n\r");
                }
                TR.destroy();
            }
        }
        return buf;
    }
    
    public void listPolls(MOB mob, Vector commands)
    {
        Vector V=CMLib.polls().getPollList();
        if(V.size()==0)
            mob.tell("\n\rNo polls available.  Fix that by entering CREATE POLL!");
        else
        {
            StringBuffer str=new StringBuffer("");
            for(int v=0;v<V.size();v++)
            {
                Poll P=(Poll)V.elementAt(v);
                str.append(CMStrings.padRight(""+(v+1),2)+": "+P.getName());
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
		int code=getMyCmdCode(mob, listWord);
		if((code<0)||(listWord.length()==0))
		{
			Vector V=getMyCmdWords(mob);
			if(V.size()==0)
				mob.tell("You are not allowed to use this command!");
			else
			{
				StringBuffer str=new StringBuffer("");
				for(int v=0;v<V.size();v++)
					if(((String)V.elementAt(v)).length()>0)
					{
						str.append((String)V.elementAt(v));
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
		case 0:	s.wraplessPrintln(unlinkedExits(mob,commands)); break;
		case 1: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.basicItems()).toString()); break;
		case 2: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.armor()).toString()); break;
		case 3: s.wraplessPrintln(listEnvResources(rest.equalsIgnoreCase("SHORT"))); break;
		case 4: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.weapons()).toString()); break;
		case 5: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.mobTypes()).toString()); break;
		case 6: s.wraplessPrintln(roomDetails(mob.location().getArea().getMetroMap(),mob.location()).toString()); break;
		case 7: s.wraplessPrintln(roomTypes(mob.location().getArea().getMetroMap(),mob.location()).toString()); break;
		case 8: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.locales()).toString()); break;
		case 9: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.behaviors()).toString()); break;
		case 10: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.exits()).toString()); break;
		case 11: s.wraplessPrintln(listRaces(CMClass.races(),rest.equalsIgnoreCase("SHORT")).toString()); break;
		case 12: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.charClasses()).toString()); break;
		case 13: s.wraplessPrintln(listSubOps(mob).toString()); break;
		case 14: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_SPELL).toString()); break;
		case 15: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_SONG).toString()); break;
		case 16: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_PRAYER).toString()); break;
		case 17: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_PROPERTY).toString()); break;
		case 18: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_THIEF_SKILL).toString()); break;
		case 19: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_COMMON_SKILL).toString()); break;
		case 20: s.println(listJournals().toString()); break;
		case 21: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_SKILL).toString()); break;
		case 22: s.println(listQuests().toString()); break;
		case 23: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_DISEASE).toString()); break;
		case 24: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_POISON).toString()); break;
		case 25: s.println(listTicks(CMParms.combine(commands,1)).toString()); break;
		case 26: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.miscMagic()).toString()); break;
		case 27: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.miscTech()).toString()); break;
		case 28: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.clanItems()).toString()); break;
		case 29: s.println(journalList(listWord).toString()); break;
        case 30: s.wraplessPrintln(roomPropertyDetails(mob.location().getArea().getMetroMap(),mob.location()).toString()); break;
		case 31:
		{
			StringBuffer str=new StringBuffer("\n\rProtected players:\n\r");
			Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
			if((protectedOnes!=null)&&(protectedOnes.size()>0))
			for(int b=0;b<protectedOnes.size();b++)
				str.append((b+1)+") "+((String)protectedOnes.elementAt(b))+"\n\r");
			s.wraplessPrintln(str.toString());
			break;
		}
		case 32:
		{
			StringBuffer str=new StringBuffer("\n\rBanned names/ips:\n\r");
			Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
			if((banned!=null)&&(banned.size()>0))
			for(int b=0;b<banned.size();b++)
				str.append((b+1)+") "+((String)banned.elementAt(b))+"\n\r");
			s.wraplessPrintln(str.toString());
			break;
		}
        case 33: s.wraplessPrintln(listRaceCats(CMClass.races(),rest.equalsIgnoreCase("SHORT")).toString()); break;
		case 34: s.wraplessPrintln(Log.getLog().toString()); break;
		case 35: listUsers(mob,commands); break;
		case 36: s.println(listLinkages(mob).toString()); break;
		case 37: s.println(listReports(mob).toString()); break;
		case 38: s.println(listThreads(mob,CMParms.combine(commands,1).equalsIgnoreCase("SHORT")).toString()); break;
		case 39: s.println(listResources(mob,CMParms.combine(commands,1))); break;
		case 40: s.wraplessPrintln(reallyFindOneWays(mob,commands)); break;
		case 41: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_CHANT).toString()); break;
		case 42:
		case 43: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_SUPERPOWER).toString()); break;
		case 44: s.wraplessPrintln(listComponents()); break;
		case 45: s.wraplessPrintln(listExpertises()); break;
        case 46: s.wraplessPrintln(CMLib.factions().listFactions()); break;
        case 47: s.wraplessPrintln(listMaterials()); break;
        case 48: s.println("\n\r^xCounter Report:^.^N\n\r"+CMClass.getCounterReport()); break;
        case 49: listPolls(mob,commands); break;
        case 50: s.wraplessPrintln(listContent(mob,commands).toString()); break;
		case 51: s.wraplessPrintln(roomExpires(mob.location().getArea().getProperMap(),mob.location()).toString()); break;
        case 52: s.wraplessPrintln(listTitles()); break;
		case 53: s.wraplessPrintln(roomResources(mob.location().getArea().getMetroMap(),mob.location()).toString()); break;
		case 54: s.wraplessPrintln(areaConquests(CMLib.map().sortedAreas()).toString()); break;
        case 55: s.wraplessPrintln(CMLib.quests().listHolidays(mob.location().getArea(),CMParms.combine(commands,1)).toString()); break;
        default:
			s.println("List?!");
			break;
		}
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Vector V=new Vector();
        commands.removeElementAt(0);
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
            String what=CMParms.combine(commands,0);
            Vector V2=CMLib.coffeeShops().getAllShopkeepers(mob.location(),mob);
            Environmental shopkeeper=CMLib.english().fetchEnvironmental(V2,what,false);
            if((shopkeeper==null)&&(what.equals("shop")||what.equals("the shop")))
                for(int v=0;v<V2.size();v++)
                    if(V2.elementAt(v) instanceof Area)
                    { shopkeeper=(Environmental)V2.elementAt(v); break;}
			if((shopkeeper!=null)
            &&(CMLib.coffeeShops().getShopKeeper(shopkeeper)!=null)
            &&(CMLib.flags().canBeSeenBy(shopkeeper,mob)))
				V.addElement(shopkeeper);
			else
			if(getAnyCode(mob)>=0)
			{
				archonlist(mob,commands);
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
			Environmental shopkeeper=(Environmental)V.elementAt(i);
            ShopKeeper SHOP=CMLib.coffeeShops().getShopKeeper(shopkeeper);
            String str="<S-NAME> review(s) <T-YOUPOSS> inventory.";
            if(SHOP instanceof Banker)
                str="<S-NAME> review(s) <S-HIS-HER> account with <T-NAMESELF>.";
            else
            if(SHOP instanceof PostOffice)
                str="<S-NAME> check(s) <S-HIS-HER> postal box with <T-NAMESELF>.";
			CMMsg newMsg=CMClass.getMsg(mob,shopkeeper,null,CMMsg.MSG_LIST,str);
			if(!mob.location().okMessage(mob,newMsg))
				return false;
			mob.location().send(mob,newMsg);
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
