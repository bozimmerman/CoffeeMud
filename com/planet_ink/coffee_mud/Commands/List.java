package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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

	public StringBuffer listSessions(MOB mob)
	{
		StringBuffer lines=new StringBuffer("^x");
		lines.append(Util.padRight("Status",9)+"| ");
		lines.append(Util.padRight("Valid",5)+"| ");
		lines.append(Util.padRight("Name",17)+"| ");
		lines.append(Util.padRight("IP",17)+"| ");
		lines.append(Util.padRight("Idle",17)+"^.^N\n\r");
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=(Session)Sessions.elementAt(s);
			lines.append((thisSession.killFlag()?"^H":"")+Util.padRight(Session.statusStr[thisSession.getStatus()],9)+(thisSession.killFlag()?"^?":"")+"| ");
			if (thisSession.mob() != null)
			{
				lines.append(Util.padRight(((thisSession.mob().session()==thisSession)?"Yes":"^HNO!^?"),5)+"| ");
				lines.append("^!"+Util.padRight(thisSession.mob().Name(),17)+"^?| ");
			}
			else
			{
				lines.append(Util.padRight("N/A",5)+"| ");
				lines.append(Util.padRight("NAMELESS",17)+"| ");
			}
			lines.append(Util.padRight(thisSession.getAddress(),17)+"| ");
			lines.append(Util.padRight((thisSession.getIdleMillis()+""),17));
			lines.append("\n\r");
		}
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
			buf.append(A.name()+"\t"+A.numberOfIDedRooms()+" rooms\t");
			if(!A.getMap().hasMoreElements())
			{
				buf.append("\n\r");
				continue;
			}
			Vector linkedGroups=new Vector();
			int numMobs=0;
			int totalAlignment=0;
			int totalLevels=0;
			for(Enumeration r=A.getMap();r.hasMoreElements();)
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
			for(Enumeration r=A.getMap();r.hasMoreElements();)
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
		long totalTime=System.currentTimeMillis()-CommonStrings.getStartTime();
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
			if(sortBy!=6)
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
				msg.append("No Area SubOps defined.\n\r");
			else
				msg.append(A.getSubOpList()+"\n\r");
		}
		return msg;
	}

	private void fillCheckDeviations(Room R, String type, Vector check)
	{
		if(type.equalsIgnoreCase("mobs")||type.equalsIgnoreCase("both"))
		{
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if((M!=null)&&(M.isEligibleMonster()))
					check.addElement(M);
			}
		}
		if(type.equalsIgnoreCase("items")||type.equalsIgnoreCase("both"))
		{
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				&&((I instanceof Armor)||(I instanceof Weapon)))
					check.addElement(I);
			}
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB M=R.fetchInhabitant(m);
				if(M!=null)
				{
					for(int i=0;i<M.inventorySize();i++)
					{
						Item I=M.fetchInventory(i);
						if((I!=null)
						&&((I instanceof Armor)||(I instanceof Weapon)))
							check.addElement(I);
					}
					ShopKeeper sk=CoffeeUtensils.getShopKeeper(M);
					if(sk!=null)
					{
						Vector V=sk.getBaseInventory();
						for(int i=0;i<V.size();i++)
							if(V.elementAt(i) instanceof Item)
							{
								Item I=(Item)V.elementAt(i);
								if((I instanceof Armor)||(I instanceof Weapon))
									check.addElement(I);
							}
					}
				}
			}
		}
	}

	private String getDeviation(int val, Hashtable vals, String key)
	{
		if(!vals.containsKey(key))
			return "N/A";
		int val2=Util.s_int((String)vals.get(key));
		return getDeviation(val,val2);
	}
	private String getDeviation(int val, int val2)
	{
		if(val==val2) return "0%";
		int oval=val-val2;
		if(oval>0) return "+"+oval;
		else return ""+oval;
	}

	private int getRoomDirection(Room R, Room toRoom, Vector ignore)
	{
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
			if((R.getRoomInDir(d)==toRoom)
			&&(R!=toRoom)
			&&(!ignore.contains(R)))
				return d;
		return -1;
	}
	public String trailTo(Room R1, Vector commands)
	{
		String where=Util.combine(commands,1);
		if(where.length()==0) return "Trail to where? Try a Room ID, 'everyroom', or 'everyarea'.";
		if(R1==null) return "Where are you?";
		boolean confirm=false;
		if(where.endsWith(" CONFIRM!"))
		{
			where=where.substring(0,where.length()-9).trim();
			confirm=true;
		}
		Vector set=new Vector();
		MUDTracker.getRadiantRooms(R1,set,false,false,true,null,Integer.MAX_VALUE);
		if(where.equalsIgnoreCase("everyarea"))
		{
			StringBuffer str=new StringBuffer("");
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				str.append(Util.padRightPreserve(A.name(),30)+": "+trailTo(R1,set,A.name(),confirm)+"\n\r");
			}
			if(confirm) Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		if(where.equalsIgnoreCase("everyroom"))
		{
			StringBuffer str=new StringBuffer("");
			for(Enumeration a=CMMap.rooms();a.hasMoreElements();)
			{
				Room R=(Room)a.nextElement();
				if((R!=R1)&&(R.roomID().length()>0))
					str.append(Util.padRightPreserve(R.roomID(),30)+": "+trailTo(R1,set,R.roomID(),confirm)+"\n\r");
			}
			if(confirm) Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		{
			String str=Util.padRightPreserve(where,30)+": "+trailTo(R1,set,where,confirm);
			if(confirm) Log.rawSysOut(str);
			return str;
		}
	}
	public String trailTo(Room R1, Vector set, String where, boolean confirm)
	{
		Room R2=CMMap.getRoom(where);
		if(R2==null)
			for(Enumeration a=CMMap.areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(A.name().equalsIgnoreCase(where))
				{
					if(set.size()==0)
					{
						int lowest=Integer.MAX_VALUE;
						for(Enumeration r=A.getMap();r.hasMoreElements();)
						{
							Room R=(Room)r.nextElement();
							int x=R.roomID().indexOf("#");
							if((x>=0)&&(Util.s_int(R.roomID().substring(x+1))<lowest))
								lowest=Util.s_int(R.roomID().substring(x+1));
						}
						if(lowest<Integer.MAX_VALUE)
							R2=CMMap.getRoom(A.name()+"#"+lowest);
					}
					else
					{
						for(int i=0;i<set.size();i++)
						{
							Room R=(Room)set.elementAt(i);
							if(R.getArea()==A)
							{
								R2=R;
								break;
							}
						}
					}
					break;
				}
			}
		if(R2==null) return "Unable to determine '"+where+"'.";
		if(set.size()==0)
			MUDTracker.getRadiantRooms(R1,set,false,false,true,R2,Integer.MAX_VALUE);
		int foundAt=-1;
		for(int i=0;i<set.size();i++)
		{
			Room R=(Room)set.elementAt(i);
			if(R==R2){ foundAt=i; break;}
		}
		if(foundAt<0) return "You can't get to '"+R2.roomID()+"' from here.";
		Room checkR=R2;
		Vector trailV=new Vector();
		trailV.addElement(R2);
		boolean didSomething=false;
		while(checkR!=R1)
		{
			didSomething=false;
			for(int r=0;r<foundAt;r++)
			{
				Room R=(Room)set.elementAt(r);
				if(getRoomDirection(R,checkR,trailV)>=0)
				{
					trailV.addElement(R);
					foundAt=r;
					checkR=R;
					didSomething=true;
					break;
				}
			}
			if(!didSomething)
				return "No trail was found?!";
		}
		Vector theDirTrail=new Vector();
		Vector empty=new Vector();
		for(int s=trailV.size()-1;s>=1;s--)
		{
			Room R=(Room)trailV.elementAt(s);
			Room RA=(Room)trailV.elementAt(s-1);
			theDirTrail.addElement(new Character(Directions.getDirectionName(getRoomDirection(R,RA,empty)).charAt(0)).toString()+" ");
		}
		StringBuffer theTrail=new StringBuffer("");
		if(confirm)	theTrail.append("\n\r"+Util.padRight("Trail",30)+": ");
		char lastDir='\0';
		int lastNum=0;
		while(theDirTrail.size()>0)
		{
			String s=(String)theDirTrail.elementAt(0);
			if(lastNum==0)
			{
				lastDir=s.charAt(0);
				lastNum=1;
			}
			else
			if(s.charAt(0)==lastDir)
				lastNum++;
			else
			{
				if(lastNum==1)
					theTrail.append(new Character(lastDir).toString()+" ");
				else
					theTrail.append(new Integer(lastNum).toString()+new Character(lastDir).toString()+" ");
				lastDir=s.charAt(0);
				lastNum=1;
			}
			theDirTrail.removeElementAt(0);
		}
		if(lastNum==1)
			theTrail.append(new Character(lastDir).toString());
		else
		if(lastNum>0)
			theTrail.append(new Integer(lastNum).toString()+new Character(lastDir).toString());

		if((confirm)&&(trailV.size()>1))
		{
			for(int i=0;i<trailV.size();i++)
			{
				Room R=(Room)trailV.elementAt(i);
				if(R.roomID().length()==0)
				{
					theTrail.append("*");
					break;
				}
			}
			Room R=(Room)trailV.elementAt(1);
			theTrail.append("\n\r"+Util.padRight("From",30)+": "+Directions.getDirectionName(getRoomDirection(R,R2,empty))+" <- "+R.roomID());
			theTrail.append("\n\r"+Util.padRight("Room",30)+": "+R.displayText()+"/"+R.description());
			theTrail.append("\n\r\n\r");
		}
		return theTrail.toString();
	}

	public StringBuffer deviations(MOB mob, String rest)
	{
		Vector V=Util.parse(rest);
		if((V.size()==0)
		||((!((String)V.firstElement()).equalsIgnoreCase("mobs"))
		   &&(!((String)V.firstElement()).equalsIgnoreCase("items"))
		   &&(!((String)V.firstElement()).equalsIgnoreCase("both"))))
			return new StringBuffer("You must specify whether you want deviations on MOBS, ITEMS, or BOTH.");

		String type=((String)V.firstElement()).toLowerCase();
		if(V.size()==1)
			return new StringBuffer("You must also specify a mob or item name, or the word room, or the word area.");

		String where=((String)V.elementAt(1)).toLowerCase();
		Environmental E=mob.location().fetchFromMOBRoomFavorsItems(mob,null,where,Item.WORN_REQ_ANY);
		Vector check=new Vector();
		if(where.equalsIgnoreCase("room"))
			fillCheckDeviations(mob.location(),type,check);
		else
		if(where.equalsIgnoreCase("area"))
		{
			for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				fillCheckDeviations(R,type,check);
			}
		}
		else
		if(E==null)
			return new StringBuffer("'"+where+"' is an unknown item or mob name.");
		else
		if(type.equals("items")
		&&(!(E instanceof Weapon))
		&&(!(E instanceof Armor)))
			return new StringBuffer("'"+where+"' is not a weapon or armor item.");
		else
		if(type.equals("mobs")
		&&(!(E instanceof MOB)))
			return new StringBuffer("'"+where+"' is not a MOB.");
		else
		if((!(E instanceof Weapon))
		&&(!(E instanceof Armor))
		&&(!(E instanceof MOB)))
			return new StringBuffer("'"+where+"' is not a MOB, or Weapon, or Item.");
		else
			check.addElement(E);
		StringBuffer str=new StringBuffer("");
		str.append("Deviations Report:\n\r");
		str.append(Util.padRight("Name",20)+" ");
		str.append(Util.padRight("Lvl",5)+" ");
		str.append(Util.padRight("Att",5)+" ");
		str.append(Util.padRight("Dmg",5)+" ");
		str.append(Util.padRight("Val",5)+" ");
		str.append(Util.padRight("Weigt",5)+" ");
		str.append(Util.padRight("Speed",5)+" ");
		str.append(Util.padRight("MinRg",5)+" ");
		str.append(Util.padRight("MaxRg",5)+" ");
		str.append("\n\r");
		for(int c=0;c<check.size();c++)
		{
			if(check.elementAt(c) instanceof Item)
			{
				Item I=(Item)check.elementAt(c);
				Weapon W=null;
				if(I instanceof Weapon)
					W=(Weapon)I;
				Hashtable vals=CoffeeMaker.timsItemAdjustments(
								I,I.envStats().level(),I.material(),I.baseEnvStats().weight(),
								I.rawLogicalAnd()?2:1,
								(W==null)?0:W.weaponClassification(),
								I.maxRange(),
								I.rawProperLocationBitmap());
				str.append(Util.padRight(I.name(),20)+" ");
				str.append(Util.padRight(""+I.envStats().level(),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.baseEnvStats().attackAdjustment(),
											vals,"ATTACK"),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.baseEnvStats().damage(),
											vals,"DAMAGE"),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.baseGoldValue(),
											vals,"VALUE"),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.baseEnvStats().weight(),
											vals,"WEIGHT"),5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.minRange(),
											vals,"MINRANGE"),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											I.maxRange(),
											vals,"MAXRANGE"),5)+" ");
				str.append("\n\r");
			}
			else
			{
				MOB M=(MOB)check.elementAt(c);
				str.append(Util.padRight(M.name(),20)+" ");
				str.append(Util.padRight(""+M.envStats().level(),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											M.baseEnvStats().attackAdjustment(),
											M.baseCharStats().getCurrentClass().getLevelAttack(M)),5)+" ");
				str.append(Util.padRight(""+getDeviation(
											M.baseEnvStats().damage(),
											M.baseCharStats().getCurrentClass().getLevelDamage(M)),5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append(Util.padRight(""+getDeviation(
											(int)Math.round(M.baseEnvStats().speed()),
											(int)Math.round(M.baseCharStats().getCurrentClass().getLevelSpeed(M))),5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append(Util.padRight("N/A",5)+" ");
				str.append("\n\r");
			}
		}
		return str;
	}

	private String reallyFindWords(MOB mob, Vector commands)
	{
		String words=Util.combine(commands,1);
		if(words.length()==0)
			return "Perhaps you should specify some words to search for.";
		StringBuffer str=new StringBuffer("");
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			StringBuffer s=new StringBuffer("");
			if(EnglishParser.containsString(R.displayText(),words))
				s.append("display ");
			if(EnglishParser.containsString(R.description(),words))
				s.append("description ");
			MOB M=R.fetchInhabitant(words);
			if(M!=null)
				s.append("mob '"+M.name()+"' ");
			Item I=R.fetchItem(null,words);
			if(I!=null)
				s.append("item '"+I.name()+"' ");
			if(s.length()>0)
				str.append(Util.padRight(R.roomID(),30)+": "+s.toString()+"\n\r");
		}
		return str.toString();
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
	public void archonlist(MOB mob, Vector commands)
	{
		if(commands.size()==0)
		{
			mob.tell("List what?");
			return;
		}

		Session s=mob.session();
		if(s==null) return;

		String listThis=Util.combine(commands,0).toUpperCase();
		String listWord=((String)commands.firstElement()).toUpperCase();

		if("ITEMS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.items()).toString());
		else
		if("TRAILTO".startsWith(listWord))
			s.rawPrintln(trailTo(mob.location(),commands));
		else
		if("ARMOR".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.armor()).toString());
		else
		if("ENVRESOURCES".startsWith(listThis))
			s.rawPrintln(listEnvResources());
		else
		if("WEAPONS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.weapons()).toString());
		else
		if("MOBS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.mobTypes()).toString());
		else
		if("ROOMS".startsWith(listThis))
			s.rawPrintln(roomDetails(CMMap.rooms(),mob.location()).toString());
		else
		if("AREA".startsWith(listThis))
			s.rawPrintln(roomTypes(CMMap.rooms(),mob.location()).toString());
		else
		if("LOCALES".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.locales()).toString());
		else
		if("BEHAVIORS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.behaviors()).toString());
		else
		if("EXITS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.exits()).toString());
		else
		if("RACES".startsWith(listThis))
			s.rawPrintln(listRaces(CMClass.races()).toString());
		else
		if("CLASSES".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.charClasses()).toString());
		else
		if("SUBOPS".startsWith(listThis))
			s.rawPrintln(listSubOps(mob).toString());
		else
		if("SPELLS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.SPELL).toString());
		else
		if("SONGS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.SONG).toString());
		else
		if("PRAYERS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.PRAYER).toString());
		else
		if("PROPERTIES".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.PROPERTY).toString());
		else
		if("THIEFSKILLS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.THIEF_SKILL).toString());
		else
		if("COMMON".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.COMMON_SKILL).toString());
		else
		if("DEVIATIONS".startsWith(listWord))
			s.rawPrintln(deviations(mob,Util.combine(commands,1)).toString());
		else
		if("SKILLS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.SKILL).toString());
		else
		if(("QUESTS".startsWith(listThis))&&(mob.isASysOp(null)))
			mob.tell(listQuests().toString());
		else
		if("DISEASES".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.DISEASE).toString());
		else
		if("POISONS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.abilities(),Ability.POISON).toString());
		else
		if(("TICKS".startsWith(listWord))&&(mob.isASysOp(null)))
			mob.tell(listTicks(Util.combine(commands,1)).toString());
		else
		if("MAGIC".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.miscMagic()).toString());
		else
		if("TECH".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.miscTech()).toString());
		else
		if("CLANITEMS".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList(CMClass.clanItems()).toString());
		else
		if(("BUGS".startsWith(listThis))&&(mob.isASysOp(null)))
			mob.tell(journalList("SYSTEM_BUGS").toString());
		else
		if(("IDEAS".startsWith(listThis))&&(mob.isASysOp(null)))
			mob.tell(journalList("SYSTEM_IDEAS").toString());
		else
		if(("NOPURGE".startsWith(listThis))&&(mob.isASysOp(null)))
		{
			StringBuffer str=new StringBuffer("\n\rProtected players:\n\r");
			Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
			if((protectedOnes!=null)&&(protectedOnes.size()>0))
			for(int b=0;b<protectedOnes.size();b++)
				str.append((b+1)+") "+((String)protectedOnes.elementAt(b))+"\n\r");
			s.rawPrintln(str.toString());
		}
		else
		if(("BANNED".startsWith(listThis))&&(mob.isASysOp(null)))
		{
			StringBuffer str=new StringBuffer("\n\rBanned names/ips:\n\r");
			Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
			if((banned!=null)&&(banned.size()>0))
			for(int b=0;b<banned.size();b++)
				str.append((b+1)+") "+((String)banned.elementAt(b))+"\n\r");
			s.rawPrintln(str.toString());
		}
		else
		if(("TYPOS".startsWith(listThis))&&(mob.isASysOp(null)))
			mob.tell(journalList("SYSTEM_TYPOS").toString());
		else
		if(("LOG".startsWith(listThis))&&(mob.isASysOp(null)))
			s.rawPrintln(Log.getLog().toString());
		else
		if(("USERS".startsWith(listWord))&&(mob.isASysOp(null)))
			listUsers(mob,commands);
		else
		if(("SESSIONS".startsWith(listThis))&&(mob.isASysOp(null)))
			mob.tell(listSessions(mob).toString());
        else
        if(("LINKAGES".startsWith(listThis))&&(mob.isASysOp(null)))
            mob.tell(listLinkages(mob).toString());
        else
		if(("REPORTS".startsWith(listThis))&&(mob.isASysOp(null)))
			mob.tell(listReports(mob).toString());
		else
		if(("THREADS".startsWith(listThis))&&(mob.isASysOp(null)))
			mob.tell(listThreads(mob).toString());
		else
		if("RESOURCES".startsWith(listThis))
			s.rawPrintln(CMLister.reallyList2Cols(Resources.findResourceKeys("").elements(),-1,null).toString());
		else
		if("WORDS".startsWith(listWord))
			s.rawPrintln(reallyFindWords(mob,commands));
		else
		if("ONEWAYDOORS".startsWith(listWord))
			s.rawPrintln(reallyFindOneWays(mob,commands));
		else
		if("UNLINKEDEXITS".startsWith(listWord))
			s.rawPrintln(unlinkedExits(mob,commands));
		else
			s.rawPrintln("Can't list those, try ITEMS, POISONS, COMMON, DISEASES, ARMOR, WEAPONS, CLANITEMS, MOBS, ROOMS, LOCALES, EXITS, RACES, CLASSES, MAGIC, TECH, SPELLS, SONGS, PRAYERS, BEHAVIORS, SKILLS, THIEFSKILLS, PROPERTIES, TICKS, LOG, USERS, SESSIONS, THREADS, BUGS, IDEAS, TYPOS, REPORTS, BANNED, NOPURGE, WORDS, RESOURCES, ENVRESOURCES, SUBOPS, TRAILTO, or AREA.");
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		Vector V=new Vector();
		if(commands.size()==0)
			V=CoffeeUtensils.shopkeepers(mob.location(),mob);
		else
		{
			MOB shopkeeper=mob.location().fetchInhabitant(Util.combine(commands,0));
			if((shopkeeper==null)||(CoffeeUtensils.getShopKeeper(shopkeeper)==null)||(!Sense.canBeSeenBy(shopkeeper,mob)))
			{
				if(mob.isASysOp(mob.location()))
				{
					archonlist(mob,commands);
					return false;
				}
			}
			else
				V.addElement(shopkeeper);
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
