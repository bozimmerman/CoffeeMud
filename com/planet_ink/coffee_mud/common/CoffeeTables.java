package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CoffeeTables
{
	public static final int STAT_LOGINS=0;
	public static final int STAT_HOURSONLINE=1;
	public static final int STAT_NEWPLAYERS=2;
	public static final int STAT_LEVELSGAINED=3;
	public static final int STAT_DEATHS=4;
	public static final int STAT_PKDEATHS=5;
	public static final int STAT_MARRIAGES=6;
	public static final int STAT_BIRTHS=7;
	public static final int STAT_DIVORCES=8;
	public static final int STAT_CLASSCHANGE=9;
	public static final int STAT_PURGES=10;
	public static final int STAT_TOTAL=11;
	
	public static final int STAT_SPECIAL_NUMONLINE=1000;
	
	private static CoffeeTables todays=null;
	
	private long highestOnline=0;
	private long numberOnlineTotal=0;
	private long numberOnlineCounter=0;
	private long startTime=0;
	private long endTime=0;
	public long startTime(){return startTime;}
	public long endTime(){return endTime;}
	private Hashtable stats=new Hashtable();
	public String data()
	{
		StringBuffer data=new StringBuffer("");
		data.append(XMLManager.convertXMLtoTag("HIGH",highestOnline));
		data.append(XMLManager.convertXMLtoTag("NUMONLINE",numberOnlineTotal));
		data.append(XMLManager.convertXMLtoTag("NUMCOUNT",numberOnlineCounter));
		data.append("<STATS>");
		for(Enumeration e=stats.keys();e.hasMoreElements();)
		{
			String s=(String)e.nextElement();
			long[] l=(long[])stats.get(s);
			data.append(XMLManager.convertXMLtoTag(s,Util.toStringList(l)));
		}
		data.append("</STATS>");
		return data.toString();
	}
	
	public void bumpVal(String s, int type)
	{
		long[] stat=null;
		if(stats.containsKey(s))
			stat=(long[])stats.get(s);
		else
		{
			stat=new long[STAT_TOTAL];
			stats.put(s,stat);
		}
		stat[type]++;
	}
	public void bumpVal(MOB mob, int type)
	{
		if(type==STAT_SPECIAL_NUMONLINE)
		{
			int ct=0;
			for(int s=0;s<Sessions.size();s++)
			{
				Session S=Sessions.elementAt(s);
				if((S!=null)&&(S.mob()!=null)
				&&(S.mob().location()!=null)
				&&(S.mob().location().isInhabitant(S.mob())))
				   ct++;
			}
			numberOnlineCounter++;
			numberOnlineTotal+=ct;
			if(ct>highestOnline)
				highestOnline=ct;
			return;
		}
										   
		// classes, races, levels, genders, faiths, clanned, grouped
		bumpVal("B"+mob.baseCharStats().getCurrentClass().baseClass(),type);
		bumpVal("C"+mob.baseCharStats().getCurrentClass().ID(),type);
		bumpVal("R"+mob.baseCharStats().getMyRace().ID(),type);
		bumpVal("L"+mob.baseEnvStats().level(),type);
		bumpVal("G"+mob.baseCharStats().getStat(CharStats.GENDER),type);
		bumpVal("F"+mob.getWorshipCharID(),type);
		bumpVal("Q"+mob.getClanID(),type);
		HashSet H=mob.getGroupMembers(new HashSet());
		bumpVal("G"+H.size(),type);
		int pct=0;
		for(Iterator e=H.iterator();e.hasNext();)
			if(!((MOB)e.next()).isMonster()) pct++;
		bumpVal("P"+pct,type);
	}
	
	public void populate(long start, long end, String data)
	{
		startTime=start;
		endTime=end;
		Vector all=XMLManager.parseAllXML(data);
		if((all==null)||(all.size()==0)) return;
		highestOnline=XMLManager.getIntFromPieces(all,"HIGH");
		numberOnlineTotal=XMLManager.getIntFromPieces(all,"NUMONLINE");
		numberOnlineCounter=XMLManager.getIntFromPieces(all,"NUMCOUNT");
		XMLManager.XMLpiece X=(XMLManager.XMLpiece)XMLManager.getPieceFromPieces(all,"STATS");
		if((X==null)||(X.contents==null)||(X.contents.size()==0)||(!X.tag.equals("STATS")))
			return;
		stats.clear();
		for(int s=0;s<X.contents.size();s++)
		{
			XMLManager.XMLpiece S=(XMLManager.XMLpiece)X.contents.elementAt(s);
			long[] l=Util.toLongArray(Util.parseCommas(S.value,true));
			if(l.length<STAT_TOTAL)
			{
				long[] l2=new long[STAT_TOTAL];
				for(int i=0;i<l.length;i++)
					l2[i]=l[i];
				l=l2;
			}
			stats.put(S.tag,l);
		}
	}
	
	public static void update()
	{
		if(todays!=null)
			CMClass.DBEngine().DBUpdateStat(todays.startTime(),todays.data());
	}
	public static void bump(MOB mob, int type)
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return;
		if(todays==null)
		{
			Calendar S=Calendar.getInstance();
			S.set(Calendar.HOUR_OF_DAY,0);
			S.set(Calendar.MINUTE,0);
			S.set(Calendar.SECOND,0);
			S.set(Calendar.MILLISECOND,0);
			todays=(CoffeeTables)CMClass.DBEngine().DBReadStat(S.getTimeInMillis());
			if(todays==null)
			{
				Calendar E=Calendar.getInstance();
				E.set(Calendar.HOUR_OF_DAY,23);
				E.set(Calendar.MINUTE,59);
				E.set(Calendar.SECOND,59);
				E.set(Calendar.MILLISECOND,999);
				todays=new CoffeeTables();
				todays.startTime=S.getTimeInMillis();
				todays.endTime=E.getTimeInMillis();
				CMClass.DBEngine().DBCreateStat(todays.startTime(),todays.endTime(),todays.data());
			}
		}
		if(System.currentTimeMillis()>todays.endTime)
		{
			CMClass.DBEngine().DBUpdateStat(todays.startTime(),todays.data());
			Calendar S=Calendar.getInstance();
			S.set(Calendar.HOUR_OF_DAY,0);
			S.set(Calendar.MINUTE,0);
			S.set(Calendar.SECOND,0);
			S.set(Calendar.MILLISECOND,0);
			Calendar E=Calendar.getInstance();
			E.set(Calendar.HOUR_OF_DAY,23);
			E.set(Calendar.MINUTE,59);
			E.set(Calendar.SECOND,59);
			E.set(Calendar.MILLISECOND,999);
			todays=new CoffeeTables();
			todays.startTime=S.getTimeInMillis();
			todays.endTime=E.getTimeInMillis();
			CMClass.DBEngine().DBCreateStat(todays.startTime,todays.endTime,todays.data());
		}
		todays.bump(mob,type);
	}
}
