package com.planet_ink.coffee_mud.Commands.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class Lister
{
	private Lister(){}
	
	public static StringBuffer reallyList(Hashtable these, int ofType)
	{
		return reallyList(these,ofType,null);
	}
	public static StringBuffer reallyList(Hashtable these)
	{
		return reallyList(these,-1,null);
	}
	public static StringBuffer reallyList(Hashtable these, Room likeRoom)
	{
		return reallyList(these,-1,likeRoom);
	}
	public static StringBuffer reallyList(Vector these, int ofType)
	{
		return reallyList(these.elements(),ofType,null);
	}
	public static StringBuffer reallyList(Enumeration these, int ofType)
	{
		return reallyList(these,ofType,null);
	}
	public static StringBuffer reallyList(Vector these)
	{
		return reallyList(these.elements(),-1,null);
	}
	public static StringBuffer reallyList(Enumeration these)
	{
		return reallyList(these,-1,null);
	}
	public static StringBuffer reallyList(Vector these, Room likeRoom)
	{
		return reallyList(these.elements(),-1,likeRoom);
	}
	public static StringBuffer reallyList(Hashtable these, int ofType, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(these.size()==0) return lines;
		int column=0;
		for(Enumeration e=these.keys();e.hasMoreElements();)
		{
			String thisOne=(String)e.nextElement();
			Object thisThang=these.get(thisOne);
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
				list=CMClass.className(thisThang);
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if((((Ability)thisThang).classificationCode()&Ability.ALL_CODES)!=ofType)
						list=null;
				}
			}
			if((likeRoom!=null)&&(thisThang instanceof Room))
			{
				if((((Room)thisThang).ID().length()>0)&&(!((Room)thisThang).getArea().name().equals(likeRoom.getArea().name())))
				   list=null;
			}
			if(list!=null)
			{
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(Util.padRight(list,24));
			}
		}
		lines.append("\n\r");
		return lines;
	}

	public static StringBuffer reallyList(Vector these, int ofType, Room likeRoom)
	{ return reallyList(these.elements(),ofType,likeRoom);}
	public static StringBuffer reallyList(Enumeration these, Room likeRoom)
	{ return reallyList(these,-1,likeRoom);}
	public static StringBuffer reallyList(Enumeration these, int ofType, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(!these.hasMoreElements()) return lines;
		int column=0;
		for(Enumeration e=these;e.hasMoreElements();)
		{
			Object thisThang=e.nextElement();
			String list=null;
			if(thisThang instanceof String)
				list=(String)thisThang;
			else
				list=CMClass.className(thisThang);
			if(ofType>=0)
			{
				if((thisThang!=null)&&(thisThang instanceof Ability))
				{
					if((((Ability)thisThang).classificationCode()&Ability.ALL_CODES)!=ofType)
						list=null;
				}
			}
			if((likeRoom!=null)&&(thisThang instanceof Room))
			{
				if((((Room)thisThang).ID().length()>0)&&(!((Room)thisThang).getArea().name().equals(likeRoom.getArea().name())))
				   list=null;
			}
			if(list!=null)
			{
				if(++column>3)
				{
					lines.append("\n\r");
					column=1;
				}
				lines.append(Util.padRight(list,24));
			}
		}
		lines.append("\n\r");
		return lines;
	}
	public static StringBuffer roomDetails(Vector these, Room likeRoom)
	{return roomDetails(these.elements(),likeRoom);}
	public static StringBuffer roomDetails(Enumeration these, Room likeRoom)
	{
		StringBuffer lines=new StringBuffer("");
		if(!these.hasMoreElements()) return lines;
		if(likeRoom==null) return lines;
		for(Enumeration r=these;r.hasMoreElements();)
		{
			Room thisThang=(Room)r.nextElement();
			String thisOne=(String)thisThang.ID();
			if((thisOne.length()>0)&&(thisThang.getArea().name().equals(likeRoom.getArea().name())))
				lines.append(Util.padRight(thisOne,24)+": "+thisThang.displayText()+"\n\r");
		}
		lines.append("\n\r");
		return lines;
	}

	public static StringBuffer listSessions(MOB mob)
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
				lines.append("^!"+Util.padRight(thisSession.mob().name(),17)+"^?| ");
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

	public static void dumpThreadGroup(StringBuffer lines,ThreadGroup tGroup)
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


	public static StringBuffer listThreads(MOB mob)
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

	public static StringBuffer journalList(String journal)
	{
		StringBuffer buf=new StringBuffer("");
		Vector V=ExternalPlay.DBReadJournal(journal);
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
	
	public static void where(MOB mob, Vector commands)
	{
		StringBuffer lines=new StringBuffer("^x");
		lines.append(Util.padRight("Name",17)+"| ");
		lines.append(Util.padRight("Location",17)+"^.^N\n\r");
		String who=Util.combine(commands,1);
		if(who.length()==0)
		{
			for(int s=0;s<Sessions.size();s++)
			{
				Session thisSession=(Session)Sessions.elementAt(s);
				if(thisSession.mob() != null)
				{
					if(mob.isASysOp(thisSession.mob().location()))
					{
						lines.append("^!"+Util.padRight(thisSession.mob().name(),17)+"^?| ");
						if(thisSession.mob().location() != null )
						{
							lines.append(thisSession.mob().location().displayText());
							lines.append(" ("+thisSession.mob().location().ID()+")");
						}
						else
							lines.append("^!(no location)^?");
						lines.append("\n\r");
					}
				}
				else
				if(mob.isASysOp(null))
				{
					lines.append(Util.padRight("NAMELESS",17)+"| ");
					lines.append("NOWHERE");
					lines.append("\n\r");
				}
			}
		}
		else
		{
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R!=null)&&(mob.isASysOp(R)))
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if((CoffeeUtensils.containsString(M.displayName(),who))
					||(CoffeeUtensils.containsString(M.displayText(),who)))
					{
						lines.append("^!"+Util.padRight(M.displayName(),17)+"^?| ");
						lines.append(R.displayText());
						lines.append(" ("+R.ID()+")");
						lines.append("\n\r");
					}
				}
			}
		}
		mob.tell(lines.toString()+"^.");
	}
	
	public static StringBuffer listReports(MOB mob)
	{
		StringBuffer buf=new StringBuffer("");
		buf.append("\n\r^xCoffeeMud System Report:^.^N\n\r");
		long totalTime=System.currentTimeMillis()-ExternalPlay.getStartTime();
		buf.append("The system has been running for ^H"+Util.returnTime(totalTime,0)+"^?.\n\r");
		long free=Runtime.getRuntime().freeMemory()/1000;
		long total=Runtime.getRuntime().totalMemory()/1000;
		buf.append("The system is utilizing ^H"+(total-free)+"^?kb out of ^H"+total+"^?kb.\n\r");
		buf.append(ExternalPlay.systemReport());
		return buf;
	}

	public static void listUsers(MOB mob, Vector commands)
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
		ExternalPlay.listUsers(mob,sortBy);
	}

	public static StringBuffer listRaces(Enumeration these)
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
	public static StringBuffer listQuests()
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
	
	public static void list(MOB mob, Vector commands)
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
			s.rawPrintln(reallyList(CMClass.items()).toString());
		else
		if("ARMOR".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.armor()).toString());
		else
		if("WEAPONS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.weapons()).toString());
		else
		if("MOBS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.mobTypes()).toString());
		else
		if("ROOMS".startsWith(listThis))
			s.rawPrintln(roomDetails(CMMap.rooms(),mob.location()).toString());
		else
		if("AREA".startsWith(listThis))
			s.rawPrintln(reallyList(CMMap.rooms(),mob.location()).toString());
		else
		if("LOCALES".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.locales()).toString());
		else
		if("BEHAVIORS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.behaviors()).toString());
		else
		if("EXITS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.exits()).toString());
		else
		if("RACES".startsWith(listThis))
			s.rawPrintln(listRaces(CMClass.races()).toString());
		else
		if("CLASSES".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.charClasses()).toString());
		else
		if("SPELLS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.abilities(),Ability.SPELL).toString());
		else
		if("SONGS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.abilities(),Ability.SONG).toString());
		else
		if("PRAYERS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.abilities(),Ability.PRAYER).toString());
		else
		if("PROPERTIES".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.abilities(),Ability.PROPERTY).toString());
		else
		if("THIEFSKILLS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.abilities(),Ability.THIEF_SKILL).toString());
		else
		if("SKILLS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.abilities(),Ability.SKILL).toString());
		else
		if("QUESTS".startsWith(listThis))
			mob.tell(listQuests().toString());
		else
		if("DISEASES".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.abilities(),Ability.DISEASE).toString());
		else
		if("POISONS".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.abilities(),Ability.DISEASE).toString());
		else
		if("TICKS".startsWith(listThis))
			mob.tell(ExternalPlay.listTicks(-1).toString());
		else
		if("TICKS".startsWith(listWord))
			mob.tell(ExternalPlay.listTicks(Util.s_int(Util.combine(commands,1))).toString());
		else
		if("MAGIC".startsWith(listThis))
			s.rawPrintln(reallyList(CMClass.miscMagic()).toString());
		else
		if("BUGS".startsWith(listThis))
			mob.tell(journalList("SYSTEM_BUGS").toString());
		else
		if("IDEAS".startsWith(listThis))
			mob.tell(journalList("SYSTEM_IDEAS").toString());
		else
		if("BANNED".startsWith(listThis))
		{
			StringBuffer str=new StringBuffer("\n\rBanned names/ips:\n\r");
			Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini"));
			if((banned!=null)&&(banned.size()>0))
			for(int b=0;b<banned.size();b++)
				str.append((b+1)+") "+((String)banned.elementAt(b))+"\n\r");
			s.rawPrintln(str.toString());
		}
		else
		if("TYPOS".startsWith(listThis))
			mob.tell(journalList("SYSTEM_TYPOS").toString());
		else
		if("LOG".startsWith(listThis))
			s.rawPrintln(Log.getLog().toString());
		else
		if("USERS".startsWith(listWord))
			listUsers(mob,commands);
		else
		if("SESSIONS".startsWith(listThis))
			mob.tell(listSessions(mob).toString());
		else
		if("REPORTS".startsWith(listThis))
			mob.tell(listReports(mob).toString());
		else
		if("THREADS".startsWith(listThis))
			mob.tell(listThreads(mob).toString());
		else
		if("RESOURCES".startsWith(listThis))
			s.rawPrintln(reallyList(Resources.findResourceKeys("")).toString());
		else
			s.rawPrintln("Can't list those, try ITEMS, POISONS, DISEASES, ARMOR, WEAPONS, MOBS, ROOMS, LOCALES, EXITS, RACES, CLASSES, MAGIC, SPELLS, SONGS, PRAYERS, BEHAVIORS, SKILLS, THIEFSKILLS, PROPERTIES, TICKS, LOG, USERS, SESSIONS, THREADS, BUGS, IDEAS, TYPOS, REPORTS, BANNED, RESOURCES, or AREA.");
	}
}
