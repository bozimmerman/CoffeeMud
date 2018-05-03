package com.planet_ink.coffee_mud.Common;
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
import com.planet_ink.coffee_mud.Libraries.StdLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
public class DefaultCoffeeTableRow implements CoffeeTableRow
{
	@Override
	public String ID()
	{
		return "DefaultCoffeeTableRow";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	public Map<String, long[]> stats= new SHashtable<String, long[]>();
	
	public long	highestOnline		= 0;
	public long	numberOnlineTotal	= 0;
	public long	numberOnlineCounter	= 0;
	public long	startTime			= 0;
	public long	endTime				= 0;

	@Override
	public void setStartTime(long time)
	{
		startTime = time;
	}

	@Override
	public void setEndTime(long time)
	{
		endTime = time;
	}

	@Override
	public long startTime()
	{
		return startTime;
	}

	@Override
	public long endTime()
	{
		return endTime;
	}

	@Override
	public long highestOnline()
	{
		return highestOnline;
	}

	@Override
	public long numberOnlineTotal()
	{
		return numberOnlineTotal;
	}

	@Override
	public long numberOnlineCounter()
	{
		return numberOnlineCounter;
	}

	@Override
	public String data()
	{
		final StringBuffer data=new StringBuffer("");
		data.append(CMLib.xml().convertXMLtoTag("HIGH",highestOnline));
		data.append(CMLib.xml().convertXMLtoTag("NUMONLINE",numberOnlineTotal));
		data.append(CMLib.xml().convertXMLtoTag("NUMCOUNT",numberOnlineCounter));
		data.append("<STATS>");
		for(final Iterator<String> e=stats.keySet().iterator();e.hasNext();)
		{
			final String s=e.next();
			final long[] l=stats.get(s);
			data.append(CMLib.xml().convertXMLtoTag(s,CMParms.toListString(l)));
		}
		data.append("</STATS>");
		return data.toString();
	}

	@Override
	public void bumpVal(String s, int type)
	{
		long[] stat=null;
		synchronized(stats)
		{
			if(stats.containsKey(s))
				stat=stats.get(s);
			else
			{
				stat=new long[STAT_TOTAL];
				stats.put(s,stat);
			}
		}
		stat[type]++;
	}

	@Override
	public void totalUp(String code, long[] tot)
	{
		code=tagFix(code);
		for(final Iterator<String> e=stats.keySet().iterator();e.hasNext();)
		{
			final String s=e.next();
			if(s.startsWith(code)
			||(s.startsWith("C")&&code.startsWith("*")))
			{
				final long[] theseStats=stats.get(s);
				for(int t=0;t<theseStats.length;t++)
					tot[t]+=theseStats[t];
			}
		}
	}

	@Override
	public String tagFix(String s)
	{
		return s.trim().replaceAll(" ","_").toUpperCase();
	}

	@Override
	public void bumpVal(CMObject E, int type)
	{
		if((E instanceof MOB)&&(((MOB)E).isMonster()))
			return;

		if(type==STAT_SPECIAL_NUMONLINE)
		{
			int ct=0;
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				if(S!=null)
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
			final MOB mob=(MOB)E;
			final Room R=mob.location();
			Area A=(R==null) ? null : R.getArea();
			if((A!=null) && (CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD)))
				A=CMLib.map().getModelArea(A);
			if(A!=null)
				bumpVal("X"+tagFix(A.Name()),type);
			bumpVal("B"+tagFix(mob.baseCharStats().getCurrentClass().baseClass()),type);
			bumpVal("C"+tagFix(mob.baseCharStats().getCurrentClass().ID()),type);
			bumpVal("R"+tagFix(mob.baseCharStats().getMyRace().ID()),type);
			bumpVal("L"+mob.basePhyStats().level(),type);
			bumpVal("G"+((char)mob.baseCharStats().getStat(CharStats.STAT_GENDER)),type);
			bumpVal("F"+tagFix(mob.getWorshipCharID()),type);
			for(final Pair<Clan,Integer> p : mob.clans())
				bumpVal("Q"+tagFix(p.first.clanID()),type);
			final Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
			bumpVal("J"+H.size(),type);
			int pct=0;
			for (final MOB mob2 : H)
			{
				if(!mob2.isMonster())
					pct++;
			}
			if(pct==0)
				pct=1;
			bumpVal("P"+pct,type);
		}
		else
		if(E instanceof Ability)
			bumpVal("A"+tagFix(E.ID()),type);
		else
		if(E instanceof Quest)
			bumpVal("U"+tagFix(((Quest)E).name()),type);
	}

	@Override
	public void populate(long start, long end, String data)
	{
		synchronized(stats)
		{
			startTime=start;
			endTime=end;
			final List<XMLLibrary.XMLTag> all=CMLib.xml().parseAllXML(data);
			if((all==null)||(all.size()==0))
				return;
			highestOnline=CMLib.xml().getIntFromPieces(all,"HIGH");
			numberOnlineTotal=CMLib.xml().getIntFromPieces(all,"NUMONLINE");
			numberOnlineCounter=CMLib.xml().getIntFromPieces(all,"NUMCOUNT");
			final XMLTag X=CMLib.xml().getPieceFromPieces(all,"STATS");
			if((X==null)||(X.contents()==null)||(X.contents().size()==0)||(!X.tag().equals("STATS")))
				return;
			stats.clear();
			for(int s=0;s<X.contents().size();s++)
			{
				final XMLTag S=X.contents().get(s);
				long[] l=CMParms.toLongArray(CMParms.parseCommas(S.value(),true));
				if(l.length<STAT_TOTAL)
				{
					final long[] l2=new long[STAT_TOTAL];
					for(int i=0;i<l.length;i++)
						l2[i]=l[i];
					l=l2;
				}
				final long[] l2=stats.get(S.tag());
				if(l2!=null)
				{
					for(int i=0;i<l2.length;i++)
						l[i]+=l2[i];
					stats.remove(S.tag());
				}
				stats.put(S.tag(),l);
			}
		}
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultCoffeeTableRow();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final DefaultCoffeeTableRow CR=(DefaultCoffeeTableRow)this.clone();
			CR.stats=new SHashtable<String,long[]>();
			CR.stats.putAll(stats);
			return CR;
		}
		catch(final Exception e){return newInstance();}
	}
}
