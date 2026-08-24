package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2026 Bo Zimmerman

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
public class DateMasks extends StdTest
{
	@Override
	public String ID()
	{
		return "DateMasks";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"all"};
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		final Area A = CMClass.getAreaType("StdTimeZone");
		final Room R = CMClass.getLocale("StdRoom");
		R.setArea(A);
		final Random r = new Random(System.nanoTime());
		final MOB M = CMClass.getFactoryMOB();
		R.addInhabitant(M);
		M.setStartRoom(R);
		M.setLocation(R);
		final TimeClock C = CMLib.time().homeClock(M);
		// GYPSY/FORTUNETELLING bug reproduction (SYSTEM_BUGS17873124462900)
		// Bug: dateMaskToNextTimeClock returns day 21 on a 20-day/month, 5-day/week calendar.
		// Root cause: DefaultTimeClock.setDayOfMonth computes weekOfMonth=floor(day/daysInWeek)
		//   using 1-indexed day, so day 20 -> weekOfMonth=4 (max valid is 3).
		//   Then MUDZapper:9152 sets DAY=weekOfMonth*daysInWeek+1 = 4*5+1 = 21.
		// Repro: set day=20 (last valid day), use a WEEK mask from awards.txt,
		//   call dateMaskToNextTimeClock, check result day is in 1..daysInMonth.
		{
			final TimeClock prodC = (TimeClock)C.copyOf();
			prodC.setDaysInMonth(20);
			prodC.setMonthsInYear(new String[]{"Mendahlary (1)","Naturuary (2)","Kaizuzen (3)","Bruhtiusen (4)","Thalosinan (5)","Zathryan (6)","Suedoonry (7)","Shessairry (8)"});
			prodC.setWeekNames(new String[]{"Dallmayrday","Mikoday","Bewleyday","Nestleday","Hillsbroday"});
			prodC.setHoursInDay(6);
			prodC.setDawnToDusk(0,1,4,5);
			prodC.setYear(1251);
			prodC.setMonth(4);
			prodC.setHourOfDay(0);
			A.setTimeObj(prodC);
			// Test 1: exact award mask from awards.txt:1051 that triggered the bug report
			prodC.setDayOfMonth(20);
			{
				final String prodMask = "-WEEK \"+1 of 4\" -MONTH \"+4 of 9\"";
				final CompiledZMask cm = CMLib.masking().maskCompile(prodMask);
				final TimeClock rc = CMLib.masking().dateMaskToNextTimeClock(M, cm);
				if(rc != null && rc.getDayOfMonth() > rc.getDaysInMonth())
					return "Fail(gypsy-award): mask "+prodMask+" day=20 -> day "+rc.getDayOfMonth()+" (max "+rc.getDaysInMonth()+") : "+rc.getShortTimeDescription();
			}
			// Test 2: isolate with a WEEK-only mask
			prodC.setDayOfMonth(20);
			{
				final String weekMask = "-WEEK \"+0 of 4\"";
				final CompiledZMask cm = CMLib.masking().maskCompile(weekMask);
				final TimeClock rc = CMLib.masking().dateMaskToNextTimeClock(M, cm);
				if(rc != null && rc.getDayOfMonth() > rc.getDaysInMonth())
					return "Fail(gypsy-week): mask "+weekMask+" day=20 -> day "+rc.getDayOfMonth()+" (max "+rc.getDaysInMonth()+") : "+rc.getShortTimeDescription();
			}
			// Test 3: sweep all valid starting days with a WEEK mask
			{
				final String weekMask = "-WEEK \"+0 of 4\"";
				final CompiledZMask cm = CMLib.masking().maskCompile(weekMask);
				for(int day=1; day<=prodC.getDaysInMonth(); day++)
				{
					prodC.setDayOfMonth(day);
					final TimeClock rc = CMLib.masking().dateMaskToNextTimeClock(M, cm);
					if(rc != null && rc.getDayOfMonth() > rc.getDaysInMonth())
						return "Fail(gypsy-sweep): start day="+day+" mask "+weekMask+" -> day "+rc.getDayOfMonth()+" (max "+rc.getDaysInMonth()+") : "+rc.getShortTimeDescription();
				}
			}
			// Test 4: sweep all valid starting days with the full award mask
			{
				final String prodMask = "-WEEK \"+1 of 4\" -MONTH \"+4 of 9\"";
				final CompiledZMask cm = CMLib.masking().maskCompile(prodMask);
				for(int day=1; day<=prodC.getDaysInMonth(); day++)
				{
					prodC.setDayOfMonth(day);
					final TimeClock rc = CMLib.masking().dateMaskToNextTimeClock(M, cm);
					if(rc != null && rc.getDayOfMonth() > rc.getDaysInMonth())
						return "Fail(gypsy-sweep-award): start day="+day+" mask "+prodMask+" -> day "+rc.getDayOfMonth()+" (max "+rc.getDaysInMonth()+") : "+rc.getShortTimeDescription();
					if(rc != null && rc.getMonth() >= rc.getMonthsInYear())
						return "Fail(gypsy-sweep-award): start day="+day+" mask "+prodMask+" -> month "+rc.getMonth()+" (max "+(rc.getMonthsInYear()-1)+") : "+rc.getShortTimeDescription();
				}
			}
			A.setTimeObj(C);
		}
		for(int i=0;i<10000;i++)
		{
			A.setTimeObj(C);
			final StringBuilder mask=new StringBuilder("");
			final List<TimePeriod> tps = new ArrayList<TimePeriod>();
			while(tps.size()==0)
			{
				for(final TimePeriod p : TimePeriod.values())
					if((r.nextDouble()<0.2)
					&&(p!=TimePeriod.WEEK)
					&&(p!=TimePeriod.SEASON)
					&&(p!=TimePeriod.ALLTIME))
						tps.add(p);
			}
			while(tps.size()>0)
			{
				final TimePeriod P = tps.remove(CMLib.dice().roll(1, tps.size(), -1));
				final int min;
				final int max;
				if(P == TimePeriod.YEAR)
				{
					max = C.getYear()+CMLib.dice().roll(1, 100, 0);
					min = C.getYear();
				}
				else
				{
					max = C.getMax(P);
					min = C.getMin(P);
				}
				char c;
				if(r.nextDouble()<0.20)
				{
					mask.append("+");
					c='-';
				}
				else
				{
					mask.append("-");
					c='+';
				}
				mask.append(P.name()).append(" ");
				double chance = 1.1;
				final Set<Integer> dup = new HashSet<Integer>();
				while(r.nextDouble()<chance)
				{
					int newVal = min + CMLib.dice().roll(1, max, -1);
					while((dup.contains(Integer.valueOf(newVal)))&&(dup.size()<(max-min)))
						newVal = min + CMLib.dice().roll(1, max, -1);
					dup.add(Integer.valueOf(newVal));
					mask.append(c).append(newVal);
					chance = chance/2.0;
					mask.append(" ");
				}
			}
			CompiledZMask cm = CMLib.masking().maskCompile(mask.toString());
			TimeClock C1 = CMLib.masking().dateMaskToNextTimeClock(M, cm);
			A.setTimeObj(C1);
			if(!CMLib.masking().maskCheck(cm, M, true))
			{
				cm = CMLib.masking().maskCompile(mask.toString());
				C1 = CMLib.masking().dateMaskToNextTimeClock(M, cm);
				CMLib.masking().maskCheck(cm, M, true);
				return "Fail(test#"+i+"): "+mask.toString()+" != "+C1.toTimeString();
			}
		}
		R.destroy();
		A.destroy();
		return null;
	}
}
