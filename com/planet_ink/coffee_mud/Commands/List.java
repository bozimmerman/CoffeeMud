package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
		for(Enumeration r=these;r.hasMoreElements();)
		{
			Room thisThang=(Room)r.nextElement();
			String thisOne=(String)thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
				lines.append(Util.padRightPreserve(thisOne,30)+": "+thisThang.displayText()+"\n\r");
		}
		lines.append("\n\r");
		return lines;
	}

	public StringBuffer roomTypes(Vector these, Room likeRoom)
	{return roomDetails(these.elements(),likeRoom);}
	public StringBuffer roomTypes(Enumeration these, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		for(Enumeration r=these;r.hasMoreElements();)
		{
			Room thisThang=(Room)r.nextElement();
			String thisOne=(String)thisThang.roomID();
			if((thisOne.length()>0)&&(thisThang.getArea().Name().equals(likeRoom.getArea().Name())))
				lines.append(Util.padRightPreserve(thisOne,30)+": "+thisThang.ID()+"\n\r");
		}
		lines.append("\n\r");
		return lines;
	}

	public void dumpThreadGroup(StringBuffer lines,ThreadGroup tGroup)
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
				lines.append(tArray[i].isAlive()? "  ok   " : " BAD!  ");
				lines.append(tArray[i].getName() + "\n\r");
			}
		}

		if (agc > 0)
		{
			lines.append("{\n\r");
			for (int i = 0; i<agc; ++i)
			{
				if (tgArray[i] != null)
					dumpThreadGroup(lines,tgArray[i]);
			}
			lines.append("}\n\r");
		}
	}


	public StringBuffer listThreads(MOB mob)
	{
		StringBuffer lines=new StringBuffer("^xStatus|Name                 ^.^?\n\r");
		try
		{
			ThreadGroup topTG = Thread.currentThread().getThreadGroup();
			while (topTG != null && topTG.getParent() != null)
				topTG = topTG.getParent();
			if (topTG != null)
				dumpThreadGroup(lines,topTG);

		}
		catch (Exception e)
		{
			lines.append ("\n\rBastards! Exception while listing threads: " + e.getMessage() + "\n\r");
		}
		return lines;

	}

	public StringBuffer listLinkages(MOB mob)
	{
		StringBuffer buf=new StringBuffer("Links: \n\r");
		Vector areaLinkGroups=new Vector();
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
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
			for(Enumeration r=A.getProperMap();r.hasMoreElements();)
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
						totalAlignment+=M.getAlignment();
						totalLevels+=M.envStats().level();
					}
				}

			}
			StringBuffer ext=new StringBuffer("links ");
			Vector myVec=null;
			Vector clearVec=null;
			for(Enumeration r=A.getProperMap();r.hasMoreElements();)
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
				buf.append(numMobs+" mobs\t"+(totalLevels/numMobs)+" avg levels\t"+(totalAlignment/numMobs)+" avg alignment");
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


	public StringBuffer journalList(String journal)
	{
		StringBuffer buf=new StringBuffer("");
		Vector V=CMClass.DBEngine().DBReadJournal(journal);
		if(V!=null)
		{
			buf.append("\n\r^x"+Util.padRight("#",5)+Util.padRight("From",10)+" Entry^.^N\n\r");
			buf.append("---------------------------------------------\n\r");
			for(int j=0;j<V.size();j++)
			{
				Vector entry=(Vector)V.elementAt(j);
				String from=(String)entry.elementAt(1);
				//String date=(String)entry.elementAt(2);
				//String to=(String)entry.elementAt(3);
				//String subject=(String)entry.elementAt(4);
				String message=(String)entry.elementAt(5);
				buf.append(Util.padRight((j+1)+"",3)+") "+Util.padRight(from,10)+" "+message+"\n\r");
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
		buf.append("The system has been running for ^H"+Util.returnTime(totalTime,0)+"^?.\n\r");
		long free=Runtime.getRuntime().freeMemory()/1000;
		long total=Runtime.getRuntime().totalMemory()/1000;
		buf.append("The system is utilizing ^H"+(total-free)+"^?kb out of ^H"+total+"^?kb.\n\r");
		buf.append("\n\r^xService Engine report:^.^N\n\r");
		String totalTickers=CMClass.ThreadEngine().systemReport("totalTickers");
		String tickGroupSize=CMClass.ThreadEngine().systemReport("TICKGROUPSIZE");
		long totalMillis=Util.s_long(CMClass.ThreadEngine().systemReport("totalMillis"));
		long totalTicks=Util.s_long(CMClass.ThreadEngine().systemReport("totalTicks"));
		String topGroupNumber=CMClass.ThreadEngine().systemReport("topGroupNumber");
		long topGroupMillis=Util.s_long(CMClass.ThreadEngine().systemReport("topGroupMillis"));
		long topGroupTicks=Util.s_long(CMClass.ThreadEngine().systemReport("topGroupTicks"));
		long topObjectMillis=Util.s_long(CMClass.ThreadEngine().systemReport("topObjectMillis"));
		long topObjectTicks=Util.s_long(CMClass.ThreadEngine().systemReport("topObjectTicks"));
		buf.append("There are ^H"+totalTickers+"^? ticking objects in ^H"+tickGroupSize+"^? threads.\n\r");
		buf.append("The ticking objects have consumed: ^H"+Util.returnTime(totalMillis,totalTicks)+"^?.\n\r");
		buf.append("The most active group, #^H"+topGroupNumber+"^?, has consumed: ^H"+Util.returnTime(topGroupMillis,topGroupTicks)+"^?.\n\r");
		String topObjectClient=CMClass.ThreadEngine().systemReport("topObjectClient");
		String topObjectGroup=CMClass.ThreadEngine().systemReport("topObjectGroup");
		if(topObjectClient.length()>0)
		{
			buf.append("The most active object has been '^H"+topObjectClient+"^?', from group #^H"+topObjectGroup+"^?.\n\r");
			buf.append("That object has consumed: ^H"+Util.returnTime(topObjectMillis,topObjectTicks)+"^?.\n\r");
		}
		buf.append("\n\r");
		buf.append("^xSave Thread report:^.^N\n\r");
		long saveThreadMilliTotal=Util.s_long(CMClass.ThreadEngine().systemReport("saveThreadMilliTotal"));
		long saveThreadTickTotal=Util.s_long(CMClass.ThreadEngine().systemReport("saveThreadTickTotal"));
		buf.append("The Save Thread has consumed: ^H"+Util.returnTime(saveThreadMilliTotal,saveThreadTickTotal)+"^?.\n\r");
		buf.append("\n\r");
		buf.append("^xUtility Thread report:^.^N\n\r");
		long utilThreadMilliTotal=Util.s_long(CMClass.ThreadEngine().systemReport("utilThreadMilliTotal"));
		long utilThreadTickTotal=Util.s_long(CMClass.ThreadEngine().systemReport("utilThreadTickTotal"));
		buf.append("The Utility Thread has consumed: ^H"+Util.returnTime(utilThreadMilliTotal,utilThreadTickTotal)+"^?.\n\r");
		buf.append("\n\r");
		buf.append("^xSession report:^.^N\n\r");
		long totalMOBMillis=Util.s_long(CMClass.ThreadEngine().systemReport("totalMOBMillis"));
		long totalMOBTicks=Util.s_long(CMClass.ThreadEngine().systemReport("totalMOBTicks"));
		buf.append("There are ^H"+Sessions.size()+"^? ticking players logged on.\n\r");
		buf.append("The ticking players have consumed: ^H"+Util.returnTime(totalMOBMillis,totalMOBTicks)+"^?.\n\r");
		long topMOBMillis=Util.s_long(CMClass.ThreadEngine().systemReport("topMOBMillis"));
		long topMOBTicks=Util.s_long(CMClass.ThreadEngine().systemReport("topMOBTicks"));
		String topMOBClient=CMClass.ThreadEngine().systemReport("topMOBClient");
		if(topMOBClient.length()>0)
		{
			buf.append("The most active mob has been '^H"+topMOBClient+"^?'\n\r");
			buf.append("That mob has consumed: ^H"+Util.returnTime(topMOBMillis,topMOBTicks)+"^?.\n\r");
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
			String rest=Util.combine(commands,0).toUpperCase();
			if("RACE".startsWith(rest))
				sortBy=2;
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
		head.append(Util.padRight("Race",8)+" ");
		head.append(Util.padRight("Class",10)+" ");
		head.append(Util.padRight("Lvl",4)+" ");
		head.append(Util.padRight("Hours",5)+" ");
		if(sortBy!=6)
			head.append(Util.padRight("Last",18)+" ");
		else
			head.append(Util.padRight("E-Mail",23)+" ");
		head.append("] Character name\n\r");
		Vector allUsers=CMClass.DBEngine().getUserList();
		Vector oldSet=allUsers;
		int showBy=sortBy;
		while((oldSet.size()>0)&&(sortBy>=0)&&(sortBy<=6))
		{
			if(oldSet==allUsers) allUsers=new Vector();

			if((sortBy<3)||(sortBy>4))
			{
				if(sortBy==6) sortBy=0;
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
					if(Util.s_long((String)selected.elementAt(sortBy))>Util.s_long(((String)V.elementAt(sortBy))))
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
			head.append(Util.padRight((String)U.elementAt(2),8)+" ");
			head.append(Util.padRight((String)U.elementAt(1),10)+" ");
			head.append(Util.padRight((String)U.elementAt(3),4)+" ");
			long age=Math.round(Util.div(Util.s_long((String)U.elementAt(4)),60.0));
			head.append(Util.padRight(""+age,5)+" ");
			if(showBy!=6)
				head.append(Util.padRight(IQCalendar.d2String(Util.s_long((String)U.elementAt(5))),18)+" ");
			else
				head.append(Util.padRight((String)U.elementAt(6),23)+" ");
			head.append("] "+Util.padRight((String)U.elementAt(0),15));
			head.append("\n\r");
		}
		mob.tell(head.toString());
	}

	public StringBuffer listRaces(Enumeration these)
	{
		StringBuffer lines=new StringBuffer("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		for(Enumeration e=these;e.hasMoreElements();)
		{
			Race thisThang=(Race)e.nextElement();
			if(++column>3)
			{
				lines.append("\n\r");
				column=1;
			}
			lines.append(Util.padRight(thisThang.ID()+" ("+thisThang.racialCategory()+")",25));
		}
		lines.append("\n\r");
		return lines;
	}
	public StringBuffer listQuests()
	{
		StringBuffer buf=new StringBuffer("");
		if(Quests.numQuests()==0)
			buf.append("No quests loaded.");
		else
		{
			buf.append("\n\r^xQuest Report:^.^N\n\r");
			buf.append("\n\r^x"+Util.padRight("#",5)+Util.padRight("Name",20)+" Status^.^N\n\r");
			for(int i=0;i<Quests.numQuests();i++)
			{
				Quest Q=Quests.fetchQuest(i);
				if(Q!=null)
				{
					buf.append(Util.padRight(""+(i+1),5)+Util.padRight(Q.name(),20)+" ");
					if(Q.running())
						buf.append("running ("+Q.minsRemaining()+" mins left)");
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

	public StringBuffer listTicks(String whichTickTock)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append(Util.padRight("Grp",4)+Util.padRight("Client",18)+" "+Util.padRight("ID",5)+Util.padRight("Time",10));
		msg.append(Util.padRight("Grp",4)+Util.padRight("Client",18)+" "+Util.padRight("ID",5)+Util.padRight("Time",10)+"\n\r");
		int col=0;
		int numGroups=Util.s_int(CMClass.ThreadEngine().tickInfo("tickGroupSize"));
		int whichTick=-1;
		if(whichTickTock.length()>0) whichTick=Util.s_int(whichTickTock);
		for(int v=0;v<numGroups;v++)
		{
			int tickersSize=Util.s_int(CMClass.ThreadEngine().tickInfo("tickersSize"+v));
			if((whichTick<0)||(whichTick==v))
			for(int t=0;t<tickersSize;t++)
			{
				String name=CMClass.ThreadEngine().tickInfo("tickerName"+v+"-"+t);
				String id=CMClass.ThreadEngine().tickInfo("tickerID"+v+"-"+t);
				String pr=CMClass.ThreadEngine().tickInfo("tickerTickDown"+v+"-"+t);
				String oo=CMClass.ThreadEngine().tickInfo("tickerReTickDown"+v+"-"+t);
				boolean suspended=Util.s_bool(CMClass.ThreadEngine().tickInfo("tickerSuspended"+v+"-"+t));
				if((col++)==2)
				{
					msg.append("\n\r");
					col=1;
				}
				msg.append(Util.padRight(""+v,4)
						   +Util.padRight(name,18)
						   +" "+Util.padRight(id+"",5)
						   +Util.padRight(pr+"/"+(suspended?"??":""+oo),10));
			}
		}
		return msg;
	}

	public StringBuffer listSubOps(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			msg.append(Util.padRight(A.Name(),25)+": ");
			if(A.getSubOpList().length()==0)
				msg.append("No Area staff defined.\n\r");
			else
				msg.append(A.getSubOpList()+"\n\r");
		}
		return msg;
	}

	private String reallyFindOneWays(MOB mob, Vector commands)
	{
		StringBuffer str=new StringBuffer("");
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room R2=R.rawDoors()[d];
				if((R2!=null)&&(R2.rawDoors()[Directions.getOpDirectionCode(d)]!=R))
					str.append(Util.padRight(R.roomID(),30)+": "+Directions.getDirectionName(d)+" to "+R2.roomID()+"\n\r");
			}
		}
		if(str.length()==0) str.append("None!");
		if(Util.combine(commands,1).equalsIgnoreCase("log"))
			Log.rawSysOut(str.toString());
		return str.toString();
	}


	private String unlinkedExits(MOB mob, Vector commands)
	{
		StringBuffer str=new StringBuffer("");
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			{
				Room R2=R.rawDoors()[d];
				Exit E2=R.rawExits()[d];
				if((R2==null)&&(E2!=null))
					str.append(Util.padRight(R.roomID(),30)+": "+Directions.getDirectionName(d)+" to "+E2.temporaryDoorLink()+" ("+E2.displayText()+")\n\r");
			}
		}
		if(str.length()==0) str.append("None!");
		if(Util.combine(commands,1).equalsIgnoreCase("log"))
			Log.rawSysOut(str.toString());
		return str.toString();
	}

	public String listEnvResources()
	{
		StringBuffer str=new StringBuffer("");
		str.append(Util.padRight("Resource",11)+" ");
		str.append(Util.padRight("Material",10)+" ");
		str.append(Util.padRight("Val",3)+" ");
		str.append(Util.padRight("Freq",4)+" ");
		str.append(Util.padRight("Str",3)+" ");
		str.append("Locales\n\r");
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
		{
			str.append(Util.padRight(Util.capitalize(EnvResource.RESOURCE_DESCS[i].toLowerCase()),12));
			str.append(Util.padRight(Util.capitalize(EnvResource.MATERIAL_DESCS[(EnvResource.RESOURCE_DATA[i][0]&EnvResource.MATERIAL_MASK)>>8].toLowerCase()),11));
			str.append(Util.padRight(""+EnvResource.RESOURCE_DATA[i][1],4));
			str.append(Util.padRight(""+EnvResource.RESOURCE_DATA[i][2],5));
			str.append(Util.padRight(""+EnvResource.RESOURCE_DATA[i][3],4));
			StringBuffer locales=new StringBuffer("");
			for(Enumeration e=CMClass.locales();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				if(!(R instanceof GridLocale))
					if((R.resourceChoices()!=null)&&(R.resourceChoices().contains(new Integer(EnvResource.RESOURCE_DATA[i][0]))))
						locales.append(R.ID()+" ");
			}
			while(locales.length()>43)
			{
				str.append(locales.toString().substring(0,42)+"\n\r"+Util.padRight(" ",36));
				locales=new StringBuffer(locales.toString().substring(42));
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
			String[] cmd=(String[])SECURITY_LISTMAP[i];
			for(int c=1;c<cmd.length;c++)
				if(CMSecurity.isAllowed(mob,mob.location(),cmd[c])
				||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
				{ V.addElement(cmd[0]); break;}
		}
		return V;
	}
	
	public int getMyCmdCode(MOB mob, String s)
	{
		s=s.toUpperCase().trim();
		for(int i=0;i<SECURITY_LISTMAP.length;i++)
		{
			String[] cmd=(String[])SECURITY_LISTMAP[i];
			if(cmd[0].startsWith(s))
			for(int c=1;c<cmd.length;c++)
				if(CMSecurity.isAllowed(mob,mob.location(),cmd[c])
				||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
				{ return i;}
		}
		return -1;
	}
	
	public int getAnyCode(MOB mob)
	{
		for(int i=0;i<SECURITY_LISTMAP.length;i++)
		{
			String[] cmd=(String[])SECURITY_LISTMAP[i];
			for(int c=1;c<cmd.length;c++)
				if(CMSecurity.isAllowed(mob,mob.location(),cmd[c])
				||CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
				{ return i;}
		}
		return -1;
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
		/*14*/{"SPELLS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*15*/{"SONGS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*16*/{"PRAYERS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*17*/{"PROPERTIES","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*18*/{"THIEFSKILLS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*19*/{"COMMON","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*20*/{"",""},
		/*21*/{"SKILLS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*22*/{"QUESTS","CMDQUESTS"},
		/*23*/{"DISEASES","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*24*/{"POISONS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
		/*25*/{"TICKS","LISTADMIN"},
		/*26*/{"MAGIC","CMDITEMS"},
		/*27*/{"TECH","CMDITEMS"},
		/*28*/{"CLANITEMS","CMDITEMS"},
		/*29*/{"BUGS","KILLBUGS"},
		/*30*/{"IDEAS","KILLIDEAS"},
		/*31*/{"NOPURGE","NOPURGE"},
		/*32*/{"BANNED","BAN"},
		/*33*/{"TYPOS","KILLTYPOS"},
		/*34*/{"LOG","LISTADMIN"},
		/*35*/{"USERS","CMDPLAYERS","STAT"},
		/*36*/{"LINKAGES","CMDAREAS"},
		/*37*/{"REPORTS","LISTADMIN"},
		/*38*/{"THREADS","LISTADMIN"},
		/*39*/{"RESOURCES","LOADUNLOAD"},
		/*40*/{"ONEWAYDOORS","CMDEXITS","CMDROOMS","CMDAREAS"},
		/*41*/{"CHANTS","CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES","CMDCLASSES"},
	};
	
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
		case 0:	s.rawPrintln(unlinkedExits(mob,commands)); break;
		case 1: s.rawPrintln(CMLister.reallyList(CMClass.items()).toString()); break;
		case 2: s.rawPrintln(CMLister.reallyList(CMClass.armor()).toString()); break;
		case 3: s.rawPrintln(listEnvResources()); break;
		case 4: s.rawPrintln(CMLister.reallyList(CMClass.weapons()).toString()); break;
		case 5: s.rawPrintln(CMLister.reallyList(CMClass.mobTypes()).toString()); break;
		case 6: s.rawPrintln(roomDetails(CMMap.rooms(),mob.location()).toString()); break;
		case 7: s.rawPrintln(roomTypes(CMMap.rooms(),mob.location()).toString()); break;
		case 8: s.rawPrintln(CMLister.reallyList(CMClass.locales()).toString()); break;
		case 9: s.rawPrintln(CMLister.reallyList(CMClass.behaviors()).toString()); break;
		case 10: s.rawPrintln(CMLister.reallyList(CMClass.exits()).toString()); break;
		case 11: s.rawPrintln(listRaces(CMClass.races()).toString()); break;
		case 12: s.rawPrintln(CMLister.reallyList(CMClass.charClasses()).toString()); break;
		case 13: s.rawPrintln(listSubOps(mob).toString()); break;
		case 14: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.SPELL).toString()); break;
		case 15: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.SONG).toString()); break;
		case 16: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.PRAYER).toString()); break;
		case 17: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.PROPERTY).toString()); break;
		case 18: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.THIEF_SKILL).toString()); break;
		case 19: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.COMMON_SKILL).toString()); break;
		case 20: break;
		case 21: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.SKILL).toString()); break;
		case 22: s.println(listQuests().toString()); break;
		case 23: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.DISEASE).toString()); break;
		case 24: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.POISON).toString()); break;
		case 25: s.println(listTicks(Util.combine(commands,1)).toString()); break;
		case 26: s.rawPrintln(CMLister.reallyList(CMClass.miscMagic()).toString()); break;
		case 27: s.rawPrintln(CMLister.reallyList(CMClass.miscTech()).toString()); break;
		case 28: s.rawPrintln(CMLister.reallyList(CMClass.clanItems()).toString()); break;
		case 29: s.println(journalList("SYSTEM_BUGS").toString()); break;
		case 30: s.println(journalList("SYSTEM_IDEAS").toString()); break;
		case 31:
		{
			StringBuffer str=new StringBuffer("\n\rProtected players:\n\r");
			Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
			if((protectedOnes!=null)&&(protectedOnes.size()>0))
			for(int b=0;b<protectedOnes.size();b++)
				str.append((b+1)+") "+((String)protectedOnes.elementAt(b))+"\n\r");
			s.rawPrintln(str.toString());
			break;
		}
		case 32:
		{
			StringBuffer str=new StringBuffer("\n\rBanned names/ips:\n\r");
			Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
			if((banned!=null)&&(banned.size()>0))
			for(int b=0;b<banned.size();b++)
				str.append((b+1)+") "+((String)banned.elementAt(b))+"\n\r");
			s.rawPrintln(str.toString());
			break;
		}
		case 33: s.println(journalList("SYSTEM_TYPOS").toString()); break;
		case 34: s.rawPrintln(Log.getLog().toString()); break;
		case 35: listUsers(mob,commands); break;
		case 36: s.println(listLinkages(mob).toString()); break;
		case 37: s.println(listReports(mob).toString()); break;
		case 38: s.println(listThreads(mob).toString()); break;
		case 39: s.rawPrintln(CMLister.reallyList2Cols(Resources.findResourceKeys("").elements(),-1,null).toString()); break;
		case 40: s.rawPrintln(reallyFindOneWays(mob,commands)); break;
		case 41: s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.CHANT).toString()); break;
		default:
			s.println("List?!");
			break;
		}
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		Vector V=new Vector();
		if(commands.size()==0)
		{
			if(getAnyCode(mob)>=0)
			{
				archonlist(mob,commands);
				return false;
			}
			else
				V=CoffeeUtensils.shopkeepers(mob.location(),mob);
		}
		else
		{
			MOB shopkeeper=mob.location().fetchInhabitant(Util.combine(commands,0));
			if((shopkeeper!=null)&&(CoffeeUtensils.getShopKeeper(shopkeeper)!=null)&&(Sense.canBeSeenBy(shopkeeper,mob)))
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
			MOB shopkeeper=(MOB)V.elementAt(i);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,null,CMMsg.MSG_LIST,(shopkeeper instanceof Banker)?"<S-NAME> review(s) <S-HIS-HER> account with <T-NAMESELF>.":"<S-NAME> review(s) <T-YOUPOSS> inventory.");
			if(!mob.location().okMessage(mob,newMsg))
				return false;
			mob.location().send(mob,newMsg);
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
