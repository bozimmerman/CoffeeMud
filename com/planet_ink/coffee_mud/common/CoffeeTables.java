package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class CoffeeTables
{
	public static final int STAT_LOGINS=0;
	public static final int STAT_TICKSONLINE=1;
	public static final int STAT_NEWPLAYERS=2;
	public static final int STAT_LEVELSGAINED=3;
	public static final int STAT_DEATHS=4;
	public static final int STAT_PKDEATHS=5;
	public static final int STAT_MARRIAGES=6;
	public static final int STAT_BIRTHS=7;
	public static final int STAT_DIVORCES=8;
	public static final int STAT_CLASSCHANGE=9;
	public static final int STAT_PURGES=10;
    public static final int STAT_SKILLUSE=11;
	public static final int STAT_TOTAL=12;
	
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
	public long highestOnline(){return highestOnline;}
	public long numberOnlineTotal(){return numberOnlineTotal;}
	public long numberOnlineCounter(){return numberOnlineCounter;}
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
		synchronized(stats)
		{
			if(stats.containsKey(s))
				stat=(long[])stats.get(s);
			else
			{
				stat=new long[STAT_TOTAL];
				stats.put(s,stat);
			}
		}
		stat[type]++;
	}
	
	public void totalUp(String code, long[] tot)
	{
		code=tagFix(code);
		for(Enumeration e=stats.keys();e.hasMoreElements();)
		{
			String s=(String)e.nextElement();
			if(s.startsWith(code)
			||(s.startsWith("C")&&code.startsWith("*")))
			{
				long[] theseStats=(long[])stats.get(s);
				for(int t=0;t<theseStats.length;t++)
					tot[t]+=theseStats[t];
			}
		}
	}

	private String tagFix(String s)
	{
		return s.trim().replaceAll(" ","_").toUpperCase();
	}
	
	public void bumpVal(Environmental E, int type)
	{
		if((E instanceof MOB)&&(((MOB)E).isMonster())) return;
		
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
        if(E instanceof MOB)
        {
            MOB mob=(MOB)E;
    		bumpVal("B"+tagFix(mob.baseCharStats().getCurrentClass().baseClass()),type);
    		bumpVal("C"+tagFix(mob.baseCharStats().getCurrentClass().ID()),type);
    		bumpVal("R"+tagFix(mob.baseCharStats().getMyRace().ID()),type);
    		bumpVal("L"+mob.baseEnvStats().level(),type);
    		bumpVal("G"+((char)mob.baseCharStats().getStat(CharStats.GENDER)),type);
    		bumpVal("F"+tagFix(mob.getWorshipCharID()),type);
    		bumpVal("Q"+tagFix(mob.getClanID()),type);
    		HashSet H=mob.getGroupMembers(new HashSet());
    		bumpVal("J"+H.size(),type);
    		int pct=0;
    		for(Iterator e=H.iterator();e.hasNext();)
    			if(!((MOB)e.next()).isMonster()) pct++;
    		if(pct==0)pct=1;
    		bumpVal("P"+pct,type);
        }
        else
        if(E instanceof Ability)
            bumpVal("A"+tagFix(E.ID()),type);
	}
	
	public void populate(long start, long end, String data)
	{
		synchronized(stats)
		{
			startTime=start;
			endTime=end;
			Vector all=XMLManager.parseAllXML(data);
			if((all==null)||(all.size()==0)) return;
			highestOnline=XMLManager.getIntFromPieces(all,"HIGH");
			numberOnlineTotal=XMLManager.getIntFromPieces(all,"NUMONLINE");
			numberOnlineCounter=XMLManager.getIntFromPieces(all,"NUMCOUNT");
			XMLManager.XMLpiece X=XMLManager.getPieceFromPieces(all,"STATS");
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
				long[] l2=(long[])stats.get(S.tag);
				if(l2!=null)
				{
					for(int i=0;i<l2.length;i++)
						l[i]+=l2[i];
					stats.remove(S.tag);
				}
				stats.put(S.tag,l);
			}
		}
	}
	
	public static void update()
	{
		if(CMSecurity.isDisabled("STATS"))
			return;
		if(todays!=null)
			CMClass.DBEngine().DBUpdateStat(todays.startTime(),todays.data());
	}
	public static void bump(Environmental E, int type)
	{
		if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
			return;
		if(CMSecurity.isDisabled("STATS"))
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
				Calendar C=Calendar.getInstance();
				C.set(Calendar.HOUR_OF_DAY,23);
				C.set(Calendar.MINUTE,59);
				C.set(Calendar.SECOND,59);
				C.set(Calendar.MILLISECOND,999);
				todays=new CoffeeTables();
				todays.startTime=S.getTimeInMillis();
				todays.endTime=C.getTimeInMillis();
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
			Calendar C=Calendar.getInstance();
			C.set(Calendar.HOUR_OF_DAY,23);
			C.set(Calendar.MINUTE,59);
			C.set(Calendar.SECOND,59);
			C.set(Calendar.MILLISECOND,999);
			todays=new CoffeeTables();
			todays.startTime=S.getTimeInMillis();
			todays.endTime=C.getTimeInMillis();
			CMClass.DBEngine().DBCreateStat(todays.startTime(),todays.endTime(),todays.data());
		}
		todays.bumpVal(E,type);
	}
}
