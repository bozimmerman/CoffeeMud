package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2023-2023 Bo Zimmerman

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
public class CalendarCmd extends StdCommand
{
	public CalendarCmd()
	{
	}

	private final String[] access=I(new String[]{"CALENDAR"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected static enum CalCmd
	{
		NEXT,
		SOON,
		PAST,
		RECENT,
		NOW,
		ADD,
		REMOVE("DELETE"),
		;
		public String[] all;
		private CalCmd(final String... others)
		{
			final List<String> all=new ArrayList<String>();
			all.add(name());
			all.addAll(Arrays.asList(others));
			this.all = all.toArray(new String[all.size()]);
		}
	}

	protected final static String[] rlPrefixes = new String[] {"REAL LIFE","REAL","RL","R"};

	protected List<JournalEntry> getGlobalCalendarByStartRange(final long fromTm, final long toTm)
	{
		final List<JournalEntry> calendar = new Vector<JournalEntry>();
		for(final JournalEntry holiday : CMLib.quests().getHolidayEntries())
		{
			if((holiday.update()>=fromTm)&&(holiday.update()<=toTm))
				calendar.add(holiday);
		}
		calendar.addAll(CMLib.database().DBReadJournalMsgsByUpdateRange("SYSTEM_CALENDAR", "SYSTEM", fromTm, toTm));
		return calendar;
	}

	protected List<JournalEntry> getGlobalCalendarByExpirationRange(final long fromTm, final long toTm)
	{
		final List<JournalEntry> calendar = new Vector<JournalEntry>();
		for(final JournalEntry holiday : CMLib.quests().getHolidayEntries())
		{
			if((holiday.expiration()>=fromTm)&&(holiday.expiration()<=toTm))
				calendar.add(holiday);
		}
		calendar.addAll(CMLib.database().DBReadJournalMsgsByExpiRange("SYSTEM_CALENDAR", "SYSTEM", fromTm, toTm));
		return calendar;
	}

	protected List<JournalEntry> getGlobalCalendarByTimeStamps(final long fromTm, final long toTm)
	{
		final List<JournalEntry> calendar = new Vector<JournalEntry>();
		for(final JournalEntry holiday : CMLib.quests().getHolidayEntries())
		{
			if((fromTm>=holiday.update())&&(holiday.expiration()<=toTm))
				calendar.add(holiday);
		}
		calendar.addAll(CMLib.database().DBReadJournalMsgsByTimeStamps("SYSTEM_CALENDAR", "SYSTEM", fromTm, toTm));
		return calendar;
	}

	protected List<JournalEntry> getCalendarByStartRange(final MOB whoM, final long fromTm, final long toTm)
	{
		final List<JournalEntry> entries = getGlobalCalendarByStartRange(fromTm, toTm);
		if(whoM != null)
		{
			entries.addAll(CMLib.database().DBReadJournalMsgsByUpdateRange("SYSTEM_CALENDAR", whoM.Name(), fromTm, toTm));
			for(final Pair<Clan,Integer> C : whoM.clans())
			{
				if(C.first.getAuthority(C.second.intValue(),Function.LIST_MEMBERS)!=Authority.CAN_NOT_DO)
					entries.addAll(CMLib.database().DBReadJournalMsgsByUpdateRange("SYSTEM_CALENDAR", C.first.clanID(), fromTm, toTm));
			}
		}
		Collections.sort(entries,new Comparator<JournalEntry>()
		{
			@Override
			public int compare(final JournalEntry o1, final JournalEntry o2)
			{
				if(o1.update()==o2.update())
					return 0;
				if(o1.update()>o2.update())
					return 1;
				return -1;
			}

		});
		return entries;
	}

	protected List<JournalEntry> getCalendarByExpirationRange(final MOB whoM, final long fromTm, final long toTm)
	{
		final List<JournalEntry> entries = getGlobalCalendarByExpirationRange(fromTm, toTm);
		if(whoM != null)
		{
			entries.addAll(CMLib.database().DBReadJournalMsgsByUpdateRange("SYSTEM_CALENDAR", whoM.Name(), fromTm, toTm));
			for(final Pair<Clan,Integer> C : whoM.clans())
			{
				if(C.first.getAuthority(C.second.intValue(),Function.LIST_MEMBERS)!=Authority.CAN_NOT_DO)
					entries.addAll(CMLib.database().DBReadJournalMsgsByExpiRange("SYSTEM_CALENDAR", C.first.clanID(), fromTm, toTm));
			}
		}
		Collections.sort(entries,new Comparator<JournalEntry>()
		{
			@Override
			public int compare(final JournalEntry o1, final JournalEntry o2)
			{
				if(o1.update()==o2.update())
					return 0;
				if(o1.update()>o2.update())
					return 1;
				return -1;
			}

		});
		return entries;
	}

	protected List<JournalEntry> getCalendarByTimeStamps(final MOB whoM, final long fromTm, final long toTm)
	{
		final List<JournalEntry> entries = getGlobalCalendarByStartRange(fromTm, toTm);
		if(whoM != null)
		{
			entries.addAll(CMLib.database().DBReadJournalMsgsByUpdateRange("SYSTEM_CALENDAR", whoM.Name(), fromTm, toTm));
			for(final Pair<Clan,Integer> C : whoM.clans())
			{
				if(C.first.getAuthority(C.second.intValue(),Function.LIST_MEMBERS)!=Authority.CAN_NOT_DO)
					entries.addAll(CMLib.database().DBReadJournalMsgsByTimeStamps("SYSTEM_CALENDAR", C.first.clanID(), fromTm, toTm));
			}
		}
		Collections.sort(entries,new Comparator<JournalEntry>()
		{
			@Override
			public int compare(final JournalEntry o1, final JournalEntry o2)
			{
				if(o1.update()==o2.update())
					return 0;
				if(o1.update()>o2.update())
					return 1;
				return -1;
			}

		});
		return entries;
	}

	protected TimeClock.TimePeriod getTimePeriod(String period, final boolean[] rl)
	{
		TimeClock.TimePeriod per = null;
		for(final String pf : rlPrefixes)
		{
			if(period.startsWith(pf))
			{
				period = period.substring(pf.length()).trim();
				rl[0]=true;
				break;
			}
		}
		per = (TimeClock.TimePeriod)CMath.s_valueOf(TimeClock.TimePeriod.class, period);
		if(per == null)
		{
			for(final TimeClock.TimePeriod P : TimeClock.TimePeriod.values())
			{
				if(P.name().startsWith(period))
				{
					per = P;
					break;
				}
			}
		}
		return per;
	}

	public long getMillis(final MOB mob, final TimeClock.TimePeriod P, final boolean rl)
	{
		if(rl)
			return P.getIncrement();
		TimeClock C = CMLib.time().homeClock(mob);
		if(C == null)
			C=CMLib.time().globalClock();
		switch(P)
		{
		case ALLTIME:
			return 0;
		case DAY:
			return CMProps.getMillisPerMudHour() * C.getHoursInDay();
		case HOUR:
			return CMProps.getMillisPerMudHour();
		case MONTH:
			return CMProps.getMillisPerMudHour() * C.getHoursInDay() * C.getDaysInMonth();
		case SEASON:
			return CMProps.getMillisPerMudHour() * C.getHoursInDay() * C.getDaysInMonth() * C.getMonthsInSeason();
		case WEEK:
			return CMProps.getMillisPerMudHour() * C.getHoursInDay() * C.getDaysInWeek();
		case YEAR:
			return CMProps.getMillisPerMudHour() * C.getHoursInDay() * C.getDaysInYear();
		}
		return 0;
	}

	public String getEvents(final MOB M, final List<JournalEntry> entries)
	{
		final int COLW=CMLib.lister().fixColWidth(36.0,M.session());
		final int COLE=CMLib.lister().fixColWidth(36.0,M.session());
		final StringBuilder str = new StringBuilder("");
		str.append("\n\r^H"+CMStrings.padRight(L("When (y/m/d)"), COLW)
					+CMStrings.padRight(L("Event"), COLE)
					).append("^N\n\r");
		TimeClock C = CMLib.time().homeClock(M);
		if(C == null)
			C=CMLib.time().globalClock();
		boolean color=true;
		for(final JournalEntry entry : entries)
		{
			final TimeClock sC = C.deriveClock(entry.update());
			String startDateStr = sC.getShortestTimeDescription();
			startDateStr += " (" + CMLib.time().date2String24(entry.update()) + ")";
			str.append(color?"^w":"^W");
			str.append(CMStrings.padRight(startDateStr, COLW)
					+ CMStrings.padRight(entry.subj(), COLE)).append("\n\r");
			final TimeClock eC = C.deriveClock(entry.expiration());
			String endDateStr = "-"+eC.getShortestTimeDescription();
			endDateStr += " (" + CMLib.time().date2String24(entry.expiration())+")";
			final String rest = entry.from()+": "+entry.to();
			str.append(color?"^w":"^W");
			str.append(CMStrings.padRight(endDateStr, COLW)
					+ CMStrings.padRight(rest, COLE)).append("^N\n\r\n\r");
			color=!color;
		}
		return str.toString()+"^N";
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		//final List<String> ogCommands=new XVector<String>(commands);
		if(commands.size()<2)
		{
			if(commands.size()==0)
				commands.add("NOMATTA");
			commands.add("SOON");
		}
		commands.remove(0);
		final String cmdStr = commands.remove(0).toUpperCase().trim();
		CalCmd cmd = (CalCmd)CMath.s_valueOf(CalCmd.class, cmdStr);
		if(cmd == null)
		{
			for(final CalCmd C : CalCmd.values())
			{
				if(C.name().startsWith(cmdStr))
				{
					cmd = C;
					break;
				}
			}
		}
		if(cmd == null)
		{
			mob.tell(L("@x1 is an unknown argument.  Try: "+CMLib.english().toEnglishStringList(CalCmd.class, false)));
			return false;
		}
		if(cmd == CalCmd.NOW)
		{
			final long fromTm = System.currentTimeMillis() - CMProps.getTickMillis();
			final long toTm = System.currentTimeMillis() + CMProps.getTickMillis();
			final List<JournalEntry> all = getCalendarByTimeStamps(mob, fromTm, toTm);
			if(all.size()==0)
			{
				if(CMLib.flags().isInTheGame(mob, true))
					mob.tell(L("There are no Calendar events occurring right now."));
				return false;
			}
			else
			{
				mob.tell(L("^HCalendar events occurring right now.^?"));
				mob.tell(getEvents(mob, all));
			}
		}
		if(cmd == CalCmd.SOON)
		{
			final long fromTm = System.currentTimeMillis() - CMProps.getTickMillis();
			final long toTm = System.currentTimeMillis() + (2*TimeManager.MILI_DAY);
			final List<JournalEntry> all = getCalendarByStartRange(mob, fromTm, toTm);
			if(all.size()==0)
			{
				if(CMLib.flags().isInTheGame(mob, true))
					mob.tell(L("There are no Calendar events starting soon."));
				return false;
			}
			else
			{
				mob.tell(L("^HCalendar events starting soon.^?"));
				mob.tell(getEvents(mob, all));
			}
		}
		if(cmd == CalCmd.RECENT)
		{
			final long fromTm = System.currentTimeMillis() - (2*TimeManager.MILI_DAY);
			final long toTm = System.currentTimeMillis() + CMProps.getTickMillis();
			final List<JournalEntry> all = getCalendarByTimeStamps(mob, fromTm, toTm);
			if(all.size()==0)
			{
				if(CMLib.flags().isInTheGame(mob, true))
					mob.tell(L("There are no Calendar events recently."));
				return false;
			}
			else
			{
				mob.tell(L("^HRecent calendar events.^?"));
				mob.tell(getEvents(mob, all));
			}
		}
		if((cmd == CalCmd.NEXT)||(cmd == CalCmd.PAST))
		{
			int num = 1;
			TimeClock.TimePeriod per;
			final boolean[] rl = new boolean[] { false };
			if(commands.size() == 0)
				per = TimeClock.TimePeriod.WEEK;
			else
			if(commands.size()==1)
			{
				if(CMath.isInteger(commands.get(0)))
				{
					num = CMath.s_int(commands.get(0));
					per = TimeClock.TimePeriod.WEEK;
				}
				else
				{
					final String period = CMParms.combine(commands,0).toUpperCase().trim();
					per = this.getTimePeriod(period, rl);
					if(per == null)
					{
						mob.tell(L("'@x1' is neither a number, nor a proper time period.",period));
						return false;
					}
				}
			}
			else
			if(!CMath.isInteger(commands.get(0)))
			{
				mob.tell(L("'@x1' is neither a number, nor a proper time period."));
				return false;
			}
			else
			{
				num = CMath.s_int(commands.get(0));
				final String period = CMParms.combine(commands,1).toUpperCase().trim();
				per = this.getTimePeriod(period, rl);
				if(per == null)
				{
					mob.tell(L("'@x1' is neither a number, nor a proper time period.",period));
					return false;
				}
			}
			final long millis = this.getMillis(mob, per, rl[0]);
			final long fromTm;
			final long toTm;
			if(cmd == CalCmd.NEXT)
			{
				fromTm = System.currentTimeMillis() - CMProps.getTickMillis();
				toTm = System.currentTimeMillis() + (num * millis);
			}
			else
			{
				fromTm = System.currentTimeMillis() - (num * millis);
				toTm = System.currentTimeMillis() + CMProps.getTickMillis();
			}
			final List<JournalEntry> all = getCalendarByExpirationRange(mob, fromTm, toTm);
			if(all.size()==0)
			{
				if(CMLib.flags().isInTheGame(mob, true))
					mob.tell(L("There are no Calendar events within the "+cmd.name().toLowerCase()+" @x1 @x2.",""+num,""+per.name().toLowerCase()));
				return false;
			}
			else
			{
				mob.tell(L("^HCalendar events within the "+cmd.name().toLowerCase()+" @x1 @x2.^?",""+num,""+per.name().toLowerCase()));
				mob.tell(getEvents(mob, all));
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
