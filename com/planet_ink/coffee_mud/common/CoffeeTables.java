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
	public static final int STAT_TOTAL=10;
	
	private long highestOnline=0;
	private long numberOnlineTotal=0;
	private long numberOnlineCounter=0;
	private long startTime=0;
	private long endTime=0;
	private Hashtable stats=new Hashtable();
	
	public void bump(String s, int type)
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
	public void bump(MOB mob, int type)
	{
		if(type==STAT_HOURSONLINE)
		{
			if(Sessions.size()>highestOnline)
				highestOnline=Sessions.size();
			numberOnlineTotal+=Sessions.size();
			numberOnlineCounter++;
		}
		// classes, races, levels, genders, faiths, clanned, grouped
		bump("B"+mob.charStats().getCurrentClass().baseClass(),type);
		bump("C"+mob.charStats().getCurrentClass().ID(),type);
		bump("R"+mob.charStats().getMyRace().ID(),type);
		bump("L"+mob.envStats().level(),type);
		bump("G"+mob.charStats().getStat(CharStats.GENDER),type);
		bump("F"+mob.getWorshipCharID(),type);
		bump("Q"+mob.getClanID(),type);
		HashSet H=mob.getGroupMembers(new HashSet());
		bump("G"+H.size(),type);
		int pct=0;
		for(Iterator e=H.iterator();e.hasNext();)
			if(!((MOB)e.next()).isMonster()) pct++;
		bump("P"+pct,type);
	}
}
