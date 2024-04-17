package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2023-2024 Bo Zimmerman

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

	protected final static Comparator<JournalEntry> calendarSort = new Comparator<JournalEntry>()
	{
		@Override
		public int compare(final JournalEntry o1, final JournalEntry o2)
		{
			if(o1.date()==o2.date())
				return 0;
			if(o1.date()>o2.date())
				return 1;
			return -1;
		}
	};

	protected List<JournalEntry> getGlobalCalendarByStartRange(final long fromTm, final long toTm)
	{
		final List<JournalEntry> calendar = new Vector<JournalEntry>();
		for(final JournalEntry holiday : CMLib.quests().getHolidayEntries(true))
		{
			if((holiday.date()>=fromTm)&&(holiday.date()<=toTm))
				calendar.add(holiday);
		}
		calendar.addAll(CMLib.database().DBReadJournalMsgsByUpdateRange("SYSTEM_CALENDAR", "SYSTEM", fromTm, toTm));
		return calendar;
	}

	protected List<JournalEntry> getGlobalCalendarByExpirationRange(final long fromTm, final long toTm)
	{
		final List<JournalEntry> calendar = new Vector<JournalEntry>();
		for(final JournalEntry holiday : CMLib.quests().getHolidayEntries(true))
		{
			if((holiday.expiration()>=fromTm)&&(holiday.expiration()<=toTm))
				calendar.add(holiday);
		}
		calendar.addAll(CMLib.database().DBReadJournalMsgsByExpiRange("SYSTEM_CALENDAR", "SYSTEM", fromTm, toTm, null));
		return calendar;
	}

	protected List<JournalEntry> getGlobalCalendarByTimeStamps(final long fromTm, final long toTm)
	{
		final List<JournalEntry> calendar = new Vector<JournalEntry>();
		for(final JournalEntry holiday : CMLib.quests().getHolidayEntries(true))
		{
			if((fromTm>=holiday.date())&&(holiday.expiration()<=toTm))
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
		Collections.sort(entries,calendarSort);
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
					entries.addAll(CMLib.database().DBReadJournalMsgsByExpiRange("SYSTEM_CALENDAR", C.first.clanID(), fromTm, toTm, null));
			}
		}
		Collections.sort(entries,calendarSort);
		return entries;
	}

	protected List<JournalEntry> getCalendarByTimeStamps(final MOB whoM, final long fromTm, final long toTm)
	{
		final List<JournalEntry> entries = getGlobalCalendarByTimeStamps(fromTm, toTm);
		if(whoM != null)
		{
			//System.out.println(CMLib.time().date2String24(fromTm)+"-"+CMLib.time().date2String24(toTm));
			entries.addAll(CMLib.database().DBReadJournalMsgsByTimeStamps("SYSTEM_CALENDAR", whoM.Name(), fromTm, toTm));
			for(final Pair<Clan,Integer> C : whoM.clans())
			{
				if(C.first.getAuthority(C.second.intValue(),Function.LIST_MEMBERS)!=Authority.CAN_NOT_DO)
					entries.addAll(CMLib.database().DBReadJournalMsgsByTimeStamps("SYSTEM_CALENDAR", C.first.clanID(), fromTm, toTm));
			}
		}
		Collections.sort(entries,calendarSort);
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
		str.append("\n\r^H"
					+ CMStrings.padRight(L("When (y/m/d)"), COLW)
					+ CMStrings.padRight(L("Event"), COLE)
					).append("^N\n\r");
		TimeClock C = CMLib.time().homeClock(M);
		if(C == null)
			C = CMLib.time().globalClock();
		boolean color=true;
		for(final JournalEntry entry : entries)
		{
			TimeClock sC;
			if((entry.getKnownClock() != null)
			&&(entry.dateStr().indexOf('/')>0))
				sC = C.fromTimePeriodCodeString(entry.dateStr());
			else
				sC = C.deriveClock(entry.date()+CMProps.getTickMillis());
			String startDateStr = sC.getShortestTimeDescription();
			startDateStr += " (" + CMLib.time().date2String24(entry.date()) + ")";
			str.append(color?"^w":"^W");
			final String rest = entry.from()+": "+entry.to();
			str.append(CMStrings.padRight(startDateStr, COLW)
					+ CMStrings.padRight(rest, COLE)).append("\n\r");
			final TimeClock eC = C.deriveClock(entry.expiration()+CMProps.getTickMillis());
			String endDateStr = "-"+eC.getShortestTimeDescription();
			endDateStr += " (" + CMLib.time().date2String24(entry.expiration())+")";
			str.append(color?"^w":"^W");
			str.append(CMStrings.padRight(endDateStr, COLW)
					+ CMStrings.padRight(entry.subj(), COLE)).append("^N\n\r\n\r");
			color=!color;
		}
		return str.toString()+"^N";
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final List<String> ogCommands=new XVector<String>(commands);
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
			mob.tell(L("'@x1' is an unknown argument.  Try: @x2", cmdStr, CMLib.english().toEnglishStringList(CalCmd.class, false)));
			return false;
		}
		switch(cmd)
		{
		case NOW:
		{
			final TimeClock C = CMLib.time().localClock(mob);
			final String dateStr = L("^HThe time is now (y/m/d): ^w@x1^H (System: ^w@x2^H)\n\r^N",
					C.getShortestTimeDescription(),CMLib.time().date2String24(System.currentTimeMillis()));
			final long fromTm = System.currentTimeMillis() - CMProps.getTickMillis();
			final long toTm = System.currentTimeMillis() + CMProps.getTickMillis();
			final List<JournalEntry> all = getCalendarByTimeStamps(mob, fromTm, toTm);
			if(all.size()==0)
			{
				mob.tell(dateStr + L("There are no Calendar events occurring right now."));
				return false;
			}
			else
			{
				mob.tell(dateStr + L("^HCalendar events occurring right now.^?"));
				mob.tell(getEvents(mob, all));
			}
			break;
		}
		case SOON:
		{
			long fromTm = System.currentTimeMillis() - CMProps.getTickMillis();
			long toTm = System.currentTimeMillis() + CMProps.getTickMillis();
			final List<JournalEntry> all = getCalendarByTimeStamps(mob, fromTm, toTm);
			fromTm = System.currentTimeMillis() - CMProps.getTickMillis();
			toTm = System.currentTimeMillis() + (2*TimeManager.MILI_DAY);
			final List<JournalEntry> soon = getCalendarByStartRange(mob, fromTm, toTm);
			for(final JournalEntry E : soon)
			{
				boolean found=false;
				for(final JournalEntry N : all)
				{
					if(N.subj().equals(E.subj())&&(N.date()==E.date()))
					{
						found=true;
						break;
					}
				}
				if(!found)
					all.add(E);
			}
			if(all.size()==0)
			{
				if(CMLib.flags().isInTheGame(mob, true))
					mob.tell(L("There are no Calendar events running now, or starting soon."));
				return false;
			}
			else
			{
				Collections.sort(all,calendarSort);
				mob.tell(L("^HCalendar events running now, or starting soon.^?"));
				mob.tell(getEvents(mob, all));
			}
			break;
		}
		case RECENT:
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
			break;
		}
		case NEXT:
		case PAST:
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
				mob.tell(L("'@x1' is neither a number, nor a proper time period.",commands.get(0)));
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
			final String perName = (rl[0]?L("real life "):"")+per.name().toLowerCase();

			if(all.size()==0)
			{
				if(CMLib.flags().isInTheGame(mob, true))
					mob.tell(L("There are no Calendar events within the "+cmd.name().toLowerCase()+" @x1 @x2.",""+num,""+perName));
				return false;
			}
			else
			{
				mob.tell(L("^HCalendar events within the "+cmd.name().toLowerCase()+" @x1 @x2.^?",""+num,""+perName));
				mob.tell(getEvents(mob, all));
			}
			break;
		}
		case REMOVE:
		{
			final long fromTm = System.currentTimeMillis() - TimeManager.MILI_YEAR;
			final long toTm = System.currentTimeMillis() + TimeManager.MILI_YEAR;
			final List<JournalEntry> all = getCalendarByTimeStamps(mob, fromTm, toTm);
			final String match = CMParms.combine(commands,0);
			JournalEntry entry = CMLib.english().fetchReflective(all, match, "subj", true);
			if(entry == null)
				entry = CMLib.english().fetchReflective(all, match, "subj", false);
			if(entry == null)
			{
				mob.tell(L("No calendar entry '@x1' found.",match));
				return false;
			}
			if(((entry.from().equalsIgnoreCase("Holiday")))
			&&(!entry.from().equalsIgnoreCase(mob.Name())))
			{
				if(CMSecurity.isAllowedAnywhere(mob, SecFlag.CMDQUESTS))
					mob.tell(L("Holidays can not be deleted through this interface."));
				else
					mob.tell(L("You are not allowed to delete Holidays."));
				return false;
			}
			if(entry.from().equalsIgnoreCase(mob.Name()))
			{
				CMLib.database().DBDeleteJournal("SYSTEM_CALENDAR", entry.key());
				mob.tell(L("Calendar event deleted: '@x1'",entry.subj()));
			}
			else
			{
				final Clan C = CMLib.clans().fetchClanAnyHost(entry.from());
				if(C == null)
				{
					mob.tell(L("You are not allowed to delete '@x1'",entry.subj()));
					return false;
				}
				else
				{
					final boolean skipChecks=
						  (mob.getClanRole(mob.Name()) != null)
						&&(mob.getClanRole(mob.Name()).first == C);
					if(skipChecks)
					{
						CMLib.database().DBDeleteJournal("SYSTEM_CALENDAR", entry.key());
						mob.tell(L("Calendar event deleted: '@x1'",entry.subj()));
					}
					else
					if(!CMLib.clans().goForward(mob,C,ogCommands,Clan.Function.CREATE_MOTD,true))
					{
						mob.tell(L("You aren't in the right position to change the calendar for your @x1.",C.getGovernmentName()));
						return false;
					}
					else
					{
						CMLib.database().DBDeleteJournal("SYSTEM_CALENDAR", entry.key());
						mob.tell(L("Calendar event deleted: '@x1'",entry.subj()));
						CMLib.journals().resetCalendarEvents();
					}
				}
			}
			break;
		}
		case ADD:
		{
			String from = mob.Name();
			final Session session = mob.session();
			final String clanName=CMParms.combine(commands,0);
			boolean skipChecks=false;
			Clan chkC=null;
			skipChecks=mob.getClanRole(mob.Name())!=null;
			if(skipChecks)
				chkC=mob.getClanRole(mob.Name()).first;
			if((chkC==null)
			&&(clanName.equalsIgnoreCase("SYSTEM"))
			&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.JOURNALS)))
				from="SYSTEM";
			else
			if((chkC==null)
			&&(clanName.length()>0))
			{
				if(clanName.equals("CLAN") && (mob.clans().iterator().hasNext()))
					chkC=mob.clans().iterator().next().first;
				if(chkC==null)
				{
					for(final Pair<Clan,Integer> c : mob.clans())
					{
						if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
						&&(c.first.getAuthority(c.second.intValue(), Clan.Function.LIST_MEMBERS)!=Authority.CAN_NOT_DO))
						{
							chkC = c.first;
							break;
						}
					}
				}
				if(chkC==null)
				{
					mob.tell(L("You aren't allowed to add to the calendar of @x1.",clanName));
					return false;
				}
				else
				if(!CMLib.clans().goForward(mob,chkC,ogCommands,Clan.Function.CREATE_CALENDAR,false))
				{
					mob.tell(L("You aren't allowed to add to the calendar of @x1.",chkC.clanID()));
					return false;
				}
			}
			if(chkC != null)
				from = chkC.clanID();
			final List<String> finalV;
			if((skipChecks)||(session == null))
				finalV = commands;
			else
				finalV = new Vector<String>();
			final boolean[] useRealTime = new boolean[] { false };
			final boolean doSkipAllSecurityChecks = skipChecks;
			final Clan C = chkC;
			final String runFrom = from;
			final Runnable createEvent = new Runnable()
			{
				final boolean autoCreate = doSkipAllSecurityChecks;
				final Clan clanC=C;
				final Session S = session;
				final MOB M = mob;

				@Override
				public void run()
				{
					final List<String> voteCommands = new XVector<String>(finalV);
					if(finalV.size()>0)
					{
						voteCommands.add(0, "CALENDAR");
						voteCommands.add(1, "ADD");
					}
					if((autoCreate)
					||(clanC==null)
					||(CMLib.clans().goForward(mob,clanC,voteCommands,Clan.Function.CREATE_CALENDAR,true)))
					{
						final JournalEntry entry = (JournalEntry)CMClass.getCommon("DefaultJournalEntry");
						entry.from(runFrom);
						entry.to("ALL");
						if(finalV.size()==4)
						{
							entry.subj(finalV.get(0));
							if((finalV.get(3).startsWith("REPEAT:"))
							&&(finalV.get(2).startsWith("DURATION/HOURS:"))
							&&(finalV.get(1).startsWith("START:")
								||(finalV.get(1).startsWith("REAL-START:"))))
							{
								final boolean useRealTimes = finalV.get(1).startsWith("R");
								final String[] start = finalV.get(1).substring(useRealTimes?11:6).split("/");
								if(start.length != 4)
									return;
								final long duration=CMath.s_long(finalV.get(2).substring(15));
								final String[] repeat = finalV.get(3).substring(7).split(" ");
								final StringBuilder data = new StringBuilder("");
								if(useRealTimes)
								{
									final Calendar thenC = Calendar.getInstance();
									thenC.set(Calendar.YEAR, CMath.s_int(start[0]));
									thenC.set(Calendar.MONTH, CMath.s_int(start[1])-1);
									thenC.set(Calendar.DAY_OF_MONTH, CMath.s_int(start[2]));
									thenC.set(Calendar.HOUR_OF_DAY, CMath.s_int(start[3]));
									thenC.set(Calendar.MINUTE, 0);
									entry.dateStr(""+thenC.getTimeInMillis());
									entry.update(thenC.getTimeInMillis());
								}
								else
								{
									final TimeClock C = CMLib.time().localClock(M);
									final TimeClock newC = C.deriveClock(System.currentTimeMillis());
									newC.setYear(CMath.s_int(start[0]));
									newC.setMonth(CMath.s_int(start[1]));
									newC.setDayOfMonth(CMath.s_int(start[2]));
									newC.setHourOfDay(CMath.s_int(start[3]));
									entry.dateStr(newC.toTimePeriodCodeString());
									entry.update(entry.date());
								}
								if(useRealTimes)
									entry.expiration(entry.date() + (TimeManager.MILI_HOUR * duration));
								else
									entry.expiration(entry.date() + (CMProps.getMillisPerMudHour() * duration));
								data.append("<HOURS>"+duration+"</HOURS>");
								if(repeat.length==2)
								{
									final TimePeriod P = (TimePeriod)CMath.s_valueOf(TimePeriod.class, repeat[1]);
									final int n = CMath.s_int(repeat[0]);
									if((n>0)&&(P!=null))
										data.append("<PERIOD>"+n+" "+P.name()+"</PERIOD>");
								}
								entry.data(data.toString());
								S.println("Event created.");
								CMLib.database().DBWriteJournal("SYSTEM_CALENDAR", entry);
								CMLib.journals().resetCalendarEvents();
								return;
							}
						}
						S.println(L("Unable to create event."));
					}
				}
			};
			if(doSkipAllSecurityChecks
			&&(C!=null)
			&&(commands.size()>1))
			{
				createEvent.run();
				return true;
			}
			if(session != null)
			{
				final InputCallback[] repeatCallback =new InputCallback[1];
				repeatCallback[0] = new InputCallback(InputCallback.Type.PROMPT, "", 0)
				{
					final Session S = session;

					@Override
					public void showPrompt()
					{
						if(useRealTime[0])
							S.promptPrint(L("(Optional) Repeating real-life period (e.g. 4 hours, 1 week, etc).\n\r: "));
						else
							S.promptPrint(L("(Optional) Repeating game-time period (e.g. 4 years, 1 week, etc).\n\r: "));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						final String s=this.input;
						if(s.length()==0)
						{
							finalV.add("REPEAT:");
							createEvent.run();
							return;
						}
						final String[] split=s.split(" ");
						if((!CMath.isInteger(split[0]))
						||(split.length!=2)
						||(CMath.s_int(split[0])<1)
						||(CMath.s_int(split[0])>999))
						{
							S.println(L("@x1 is not a valid repeat period.",s));
							if(repeatCallback[0] != null)
								S.prompt(repeatCallback[0]);
							return;
						}
						final int n = CMath.s_int(split[0]);
						TimePeriod timeP = null;
						for(final TimePeriod P : TimePeriod.values())
						{
							if(split[1].toUpperCase().startsWith(P.name()))
							{
								timeP=P;
								break;
							}
						}
						if((timeP==null)||(timeP==TimePeriod.ALLTIME))
						{
							S.println(L("@x1 is not a valid repeat period. Try @x2",split[1],
									CMLib.english().toEnglishStringList(TimePeriod.class, false)));
							if(repeatCallback[0] != null)
								S.prompt(repeatCallback[0]);
							return;
						}
						finalV.add("REPEAT:"+n+" "+timeP.name());
						createEvent.run();
					}
				};
				final InputCallback[] durationCallback =new InputCallback[1];
				durationCallback[0] = new InputCallback(InputCallback.Type.PROMPT, "", 0)
				{
					final Session S = session;
					@Override
					public void showPrompt()
					{
						if(useRealTime[0])
							S.promptPrint(L("Duration of event in real-life hours: "));
						else
							S.promptPrint(L("Duration of event in game hours: "));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						final String s=this.input;
						if(s.length()==0)
						{
							return;
						}
						if((!CMath.isInteger(s))
						||(CMath.s_int(s)<1)
						||(CMath.s_int(s)>(9999)))
						{
							S.println(L("@x1 is not a valid number of hours.",s));
							if(durationCallback[0] != null)
								S.prompt(durationCallback[0]);
							return;
						}
						final int n = CMath.s_int(s);
						finalV.add("DURATION/HOURS:"+n);
						if(repeatCallback[0] != null)
							S.prompt(repeatCallback[0]);
					}
				};
				final InputCallback[] startDateCallback = new InputCallback[1];
				startDateCallback[0]=new InputCallback(InputCallback.Type.PROMPT, "", 0)
				{
					final Session S = session;
					final MOB M = mob;

					@Override
					public void showPrompt()
					{
						final TimeClock C = CMLib.time().localClock(mob);
						final String dateStr = L("^HThe time is now (y/m/d): ^w@x1^H (System: ^w@x2^H)\n\r^N",
								C.getShortestTimeDescription(),CMLib.time().date2String24(System.currentTimeMillis()));
						final String tz = Calendar.getInstance().getTimeZone().getDisplayName();
						S.promptPrint(dateStr+"\n\r"
								+ L("Enter the event start date  in 'y/m/d/h' (number) format.  "
								+ "Include the word REAL or RL to use real-world dates in @x1.\n\r: ", tz));
					}

					@Override
					public void timedOut()
					{
					}

					protected void showError(final Session S, final InputCallback C, final String msg, final String... args)
					{
						S.println(L(msg,args));
						if(C != null)
							S.prompt(C);
						return;
					}

					@Override
					public void callBack()
					{
						final String premise=this.input;
						if(premise.length()==0)
						{
							return;
						}
						int start;
						for(start=0;start<premise.length();start++)
						{
							if(Character.isDigit(premise.charAt(start)))
								break;
						}
						if(start >= premise.length())
						{
							if(startDateCallback[0] != null)
								S.prompt(startDateCallback[0]);
							return;
						}
						int end;
						for(end = premise.length()-1; end >start; end--)
						{
							if(Character.isDigit(premise.charAt(end)))
								break;
						}
						final String dateStr = premise.substring(start,end+1).trim();
						final String arg = (premise.substring(0,start) + premise.substring(end+1)).trim();
						if(arg.toUpperCase().trim().startsWith("R"))
							useRealTime[0] = true;
						else
						if(arg.length()>0)
						{
							showError(S,startDateCallback[0],"Unknown chars '@x1'.",arg);
							return;
						}
						final String[] dSplit = dateStr.split("/");
						if((dSplit.length != 4)
						||(!CMath.isInteger(dSplit[0]))
						||(!CMath.isInteger(dSplit[1]))
						||(!CMath.isInteger(dSplit[2]))
						||(!CMath.isInteger(dSplit[3])))
						{
							showError(S,startDateCallback[0],"^XBad format '@x1'!^N",dateStr);
							return;
						}
						int y = CMath.s_int(dSplit[0]);
						final int m = CMath.s_int(dSplit[1]);
						final int d = CMath.s_int(dSplit[2]);
						final int h = CMath.s_int(dSplit[3]);
						if(useRealTime[0])
						{
							final Calendar nowC = Calendar.getInstance();
							final int yremain = (nowC.get(Calendar.YEAR) % 100);
							final int hunYear = nowC.get(Calendar.YEAR) - yremain;
							if(y<100)
							{
								if(y <= yremain)
									y += hunYear;
								else
									y += (hunYear-1);
							}
							final Calendar thenC = Calendar.getInstance();
							if(y<nowC.get(Calendar.YEAR))
							{
								showError(S,startDateCallback[0],"^XBad year (@x1)!^N",""+y);
								return;
							}
							thenC.set(Calendar.YEAR, y);
							if((m<1)||(m>12)
							||((y==nowC.get(Calendar.YEAR))
								&&(m<=nowC.get(Calendar.MONTH))))
							{
								showError(S,startDateCallback[0],"^XBad month (@x1)!^N",""+m);
								return;
							}
							int maxDay = 31;
							thenC.set(Calendar.MONTH, m-1);
							maxDay = thenC.getActualMaximum(Calendar.DAY_OF_MONTH);
							if((d<1)||(d>maxDay)
							||((m==nowC.get(Calendar.MONTH)-1)
								&&(y==nowC.get(Calendar.YEAR))
								&&(d<nowC.get(Calendar.DAY_OF_MONTH))))
							{
								showError(S,startDateCallback[0],"^XBad day of the month (@x1/@x2)!^N",""+d,""+maxDay);
								return;
							}
							if((h<0)||(h>23)
							||((m==nowC.get(Calendar.MONTH)-1)
								&&(d==nowC.get(Calendar.DAY_OF_MONTH))
								&&(y==nowC.get(Calendar.YEAR))
								&&(h<=nowC.get(Calendar.HOUR_OF_DAY))))
							{
								showError(S,startDateCallback[0],"^XBad hour (@x1)! (0-23, and future only)^N",""+h);
								return;
							}
							finalV.add("REAL-START:"+y+"/"+m+"/"+d+"/"+h);
						}
						else
						{
							final TimeClock C = CMLib.time().localClock(M);
							if(y<C.getYear())
							{
								showError(S,startDateCallback[0],"^XBad year (@x1)!^N",""+y);
								return;
							}
							if((m<1)||(m>12)
							||((y==C.getYear())&&(m<C.getMonth())))
							{
								showError(S,startDateCallback[0],"^XBad month (@x1)!^N",""+m);
								return;
							}
							if((d<1)||(d>C.getDaysInMonth())
							||((m==C.getMonth())&&(y==C.getYear())&&(d<C.getDayOfMonth())))
							{
								showError(S,startDateCallback[0],"^XBad day of the month (@x1)!^N",""+d);
								return;
							}
							if((h<0)||(h>C.getHoursInDay())
							||((m==C.getMonth())&&(y==C.getYear())&&(d==C.getDayOfMonth())&&(h<=C.getHourOfDay())))
							{
								showError(S,startDateCallback[0],"^XBad hour (@x1)! (0-@x2 only)^N",""+h,""+C.getHoursInDay());
								return;
							}
							finalV.add("START:"+y+"/"+m+"/"+d+"/"+h);
						}
						if(durationCallback[0] != null)
							S.prompt(durationCallback[0]);
					}
				};
				final InputCallback eventNameCallback =new InputCallback(InputCallback.Type.PROMPT, "", 0)
				{
					final Session S = session;

					@Override
					public void showPrompt()
					{
						S.promptPrint(L("Describe your event (40 chars) : "));
					}

					@Override
					public void timedOut()
					{
					}

					@Override
					public void callBack()
					{
						final String premise=this.input;
						if(premise.length()==0)
						{
							return;
						}
						//entry.subj(premise);
						finalV.add(premise);
						if(startDateCallback[0] != null)
							S.prompt(startDateCallback[0]);
					}
				};
				session.prompt(eventNameCallback);
			}
			break;
		}
		default:
			mob.tell(L("Nothing Done."));
			break;
		}
		return true;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
