package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerStats.PlayerCombatStat;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Stat  extends Skills
{
	public Stat()
	{
	}

	private final String[] access=I(new String[]{"STAT"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public static final int	ABLETYPE_EQUIPMENT		= -2;
	public static final int	ABLETYPE_INVENTORY		= -3;
	public static final int	ABLETYPE_QUESTWINS		= -4;
	public static final int	ABLETYPE_TATTOOS		= -5;
	public static final int	ABLETYPE_COMBAT			= -6;
	public static final int	ABLETYPE_SCRIPTS		= -7;
	public static final int	ABLETYPE_TITLES			= -8;
	public static final int	ABLETYPE_ROOMSEXPLORED	= -9;
	public static final int	ABLETYPE_AREASEXPLORED	= -10;
	public static final int	ABLETYPE_WORLDEXPLORED	= -11;
	public static final int	ABLETYPE_FACTIONS		= -12;
	public static final int	ABLETYPE_CHARSTATS		= -13;
	public static final int	ABLETYPE_LEVELTIMES		= -14;
	public static final int	ABLETYPE_AFFECTS		= -15;
	public static final int	ABLETYPE_OBJECTS		= -16;

	public static final String[][] ABLETYPE_DESCS={
		{"EQUIPMENT","EQ","EQUIP"},
		{"INVENTORY","INVEN","INV"},
		{"QUESTWINS","QUESTS","QUEST","QUESTWIN"},
		{"TATTOOS","TATTOO","TATT"},
		{"COMBAT"},
		{"SCRIPTS"},
		{"TITLES","TITLE"},
		{"ROOMSEXPLORED"},
		{"AREASEXPLORED"},
		{"WORLDEXPLORED"},
		{"FACTIONS","FACTION"},
		{"CHARSTATISTICS","CSTAT","CHARSTATS"},
		{"LEVELTIMES","LVLS"},
		{"AFFECTS","EFFECTS","EFF"},
		{"POBJECTS"},
	};

	public MOB getTarget(final MOB mob, final String targetName, final boolean quiet)
	{
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				final Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,L("You can't do that to <T-NAMESELF>."));
					return null;
				}
			}
		}
		return target;
	}

	public String doSenses(final int senseMask)
	{
		final StringBuilder str=new StringBuilder("");
		for(int i=0;i<PhyStats.CAN_SEE_DESCS.length;i++)
		{
			if(CMath.isSet(senseMask, i))
				str.append(PhyStats.CAN_SEE_DESCS[i].replace(' ','_')).append(" ");
		}
		if(str.length()==0)
			str.append("NONE ");
		return str.toString().trim();
	}

	public String doDisposition(final int dispositionMask)
	{
		final StringBuilder str=new StringBuilder("");
		for(int i=0;i<PhyStats.IS_DESCS.length;i++)
		{
			if(CMath.isSet(dispositionMask, i))
				str.append(PhyStats.IS_DESCS[i].replace(' ','_')).append(" ");
		}
		if(str.length()==0)
			str.append("NONE ");
		return str.toString().trim();
	}

	public boolean showTableStats(final MOB mob, final int days, final int scale, String rest)
	{
		final Calendar ENDQ=Calendar.getInstance();
		ENDQ.add(Calendar.DATE,-days);
		ENDQ.set(Calendar.HOUR_OF_DAY,23);
		ENDQ.set(Calendar.MINUTE,59);
		ENDQ.set(Calendar.SECOND,59);
		ENDQ.set(Calendar.MILLISECOND,999);
		CMLib.coffeeTables().update();
		final List<CoffeeTableRow> V=CMLib.coffeeTables().readRawStats(ENDQ.getTimeInMillis()-1,0);
		if (V.size() == 0)
		{
			mob.tell(L("No Stats?!"));
			return false;
		}
		final StringBuffer table=new StringBuffer("");
		boolean skillUse=false;
		boolean socUse=false;
		boolean cmdUse=false;
		boolean questStats=false;
		boolean crimeStats=false;
		boolean areaStats=false;
		boolean players=false;
		if(rest.toUpperCase().trim().startsWith("SKILLUSE"))
		{
			skillUse=true;
			final int x=rest.indexOf(' ');
			if(x>0)
				rest=rest.substring(x+1).trim();
			else
				rest="";
		}
		if(rest.toUpperCase().trim().startsWith("SOCUSE"))
		{
			socUse=true;
			final int x=rest.indexOf(' ');
			if(x>0)
				rest=rest.substring(x+1).trim();
			else
				rest="";
		}
		if(rest.toUpperCase().trim().startsWith("CMDUSE"))
		{
			cmdUse=true;
			final int x=rest.indexOf(' ');
			if(x>0)
				rest=rest.substring(x+1).trim();
			else
				rest="";
		}
		if(rest.toUpperCase().trim().startsWith("QUEST"))
		{
			questStats=true;
			final int x=rest.indexOf(' ');
			if(x>0)
				rest=rest.substring(x+1).trim();
			else
				rest="";
		}
		if(rest.toUpperCase().trim().startsWith("CRIME"))
		{
			crimeStats=true;
			final int x=rest.indexOf(' ');
			if(x>0)
				rest=rest.substring(x+1).trim();
			else
				rest="";
		}
		if(rest.toUpperCase().trim().startsWith("AREA"))
		{
			areaStats=true;
			final int x=rest.indexOf(' ');
			if(x>0)
				rest=rest.substring(x+1).trim();
			else
				rest="";
		}
		if(rest.toUpperCase().trim().startsWith("PLAY"))
		{
			players=true;
			final int x=rest.indexOf(' ');
			if(x>0)
				rest=rest.substring(x+1).trim();
			else
				rest="";
		}
		table.append("^xStatistics since "+CMLib.time().date2String(ENDQ.getTimeInMillis())+":^.^N\n\r\n\r");
		if(skillUse)
			table.append(CMStrings.padRight(L("Skill"),25)+CMStrings.padRight(L("Uses"),10)+CMStrings.padRight(L("Skill"),25)+CMStrings.padRight(L("Uses"),10)+"\n\r");
		else
		if(socUse)
			table.append(CMStrings.padRight(L("Social"),25)+CMStrings.padRight(L("Uses"),10)+CMStrings.padRight(L("Social"),25)+CMStrings.padRight(L("Uses"),10)+"\n\r");
		else
		if(cmdUse)
			table.append(CMStrings.padRight(L("Command"),25)+CMStrings.padRight(L("Uses"),10)+CMStrings.padRight(L("Command"),25)+CMStrings.padRight(L("Uses"),10)+"\n\r");
		else
		if(questStats)
		{
			table.append(CMStrings.padRight(L("Quest"),30)
	   					+CMStrings.padRight(L("STRT"),5)
						+CMStrings.padRight(L("TSRT"),5)
						+CMStrings.padRight(L("FLST"),5)
						+CMStrings.padRight(L("ACPT"),5)
						+CMStrings.padRight(L("WINS"),5)
						+CMStrings.padRight(L("FAIL"),5)
						+CMStrings.padRight(L("DROP"),5)
						+CMStrings.padRight(L("TSTP"),5)
						+CMStrings.padRight(L("STOP"),5)
						+"\n\r");
		}
		else
		if(crimeStats)
		{
			table.append(CMStrings.padRight(L("Date"),25)
						+CMStrings.padRight(L("Warrants"),10)
	   					+CMStrings.padRight(L("Arrests"),10)
						+CMStrings.padRight(L("Paroles"),10)
						+CMStrings.padRight(L("Jailings"),10)
						+CMStrings.padRight(L("Executions"),10)
						+"\n\r");
		}
		else
		if(areaStats)
		{
			table.append(CMStrings.padRight(L("Area"),25)
						 +CMStrings.padRight(L("CONs"),5)
						 +CMStrings.padRight(L("HIGH"),5)
						 +CMStrings.padRight(L("ONLN"),5)
						 +CMStrings.padRight(L("AVGM"),5)
						 +CMStrings.padRight(L("NEWB"),5)
						 +CMStrings.padRight(L("DTHs"),5)
						 +CMStrings.padRight(L("PKDs"),5)
						 +CMStrings.padRight(L("CLAS"),5)
						 +CMStrings.padRight(L("PURG"),5)
						 +CMStrings.padRight(L("MARR"),5)+"\n\r");
		}
		else
			table.append(CMStrings.padRight(L("Date"),25)
						 +CMStrings.padRight(L("CONs"),5)
						 +CMStrings.padRight(L("HIGH"),5)
						 +CMStrings.padRight(L("ONLN"),5)
						 +CMStrings.padRight(L("AVGM"),5)
						 +CMStrings.padRight(L("NEWB"),5)
						 +CMStrings.padRight(L("DTHs"),5)
						 +CMStrings.padRight(L("PKDs"),5)
						 +CMStrings.padRight(L("CLAS"),5)
						 +CMStrings.padRight(L("PURG"),5)
						 +CMStrings.padRight(L("MARR"),5)+"\n\r");
		table.append(CMStrings.repeat('-',75)+"\n\r");
		final Calendar C=Calendar.getInstance();
		C.set(Calendar.HOUR_OF_DAY,23);
		C.set(Calendar.MINUTE,59);
		C.set(Calendar.SECOND,59);
		C.set(Calendar.MILLISECOND,999);
		long curTime=C.getTimeInMillis();
		String code="*";
		if(rest.length()>0)
			code=""+rest.toUpperCase().charAt(0);
		long lastCur=System.currentTimeMillis();
		if(skillUse)
		{
			final CharClass CharC=CMClass.getCharClass(rest);
			final ArrayList<Ability> allSkills=new ArrayList<Ability>();
			for(final Enumeration<Ability> e=CMClass.abilities();e.hasMoreElements();)
				allSkills.add(e.nextElement());
			final long[][] totals=new long[allSkills.size()][CoffeeTableRow.STAT_TOTAL];
			while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
			{
				lastCur=curTime;
				final Calendar C2=Calendar.getInstance();
				C2.setTimeInMillis(curTime);
				C2.add(Calendar.DATE,-(scale));
				curTime=C2.getTimeInMillis();
				C2.set(Calendar.HOUR_OF_DAY,23);
				C2.set(Calendar.MINUTE,59);
				C2.set(Calendar.SECOND,59);
				C2.set(Calendar.MILLISECOND,999);
				curTime=C2.getTimeInMillis();
				final ArrayList<CoffeeTableRow> set=new ArrayList<CoffeeTableRow>();
				for(int v=V.size()-1;v>=0;v--)
				{
					final CoffeeTableRow T=V.get(v);
					if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
					{
						set.add(T);
						V.remove(v);
					}
				}
				for(int s=0;s<set.size();s++)
				{
					final CoffeeTableRow T=set.get(s);
					for(int x=0;x<allSkills.size();x++)
						T.totalUp("A"+allSkills.get(x).ID().toUpperCase(),totals[x]);
				}
				if(scale==0)
					break;
			}
			boolean cr=false;
			for(int x=0;x<allSkills.size();x++)
			{
				Ability A=allSkills.get(x);
				if((CharC!=null)&&(CMLib.ableMapper().getQualifyingLevel(CharC.ID(),true,A.ID())<0))
					continue;
				if(totals[x][CoffeeTableRow.STAT_SKILLUSE]>0)
				{
					table.append(CMStrings.padRight(""+A.ID(),25)
							+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_SKILLUSE],10));
					if(cr)
						table.append("\n\r");
					cr=!cr;
				}
				x++;
				if(x<allSkills.size())
				{
					A=allSkills.get(x);
					if(totals[x][CoffeeTableRow.STAT_SKILLUSE]>0)
					{

						table.append(CMStrings.padRight(""+A.ID(),25)
								+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_SKILLUSE],10));
						if(cr)
							table.append("\n\r");
						cr=!cr;
					}
				}
			}
			if(cr)
				table.append("\n\r");
		}
		else
		if(socUse)
		{
			final ArrayList<Social> allSocials=new ArrayList<Social>();
			for(final Enumeration<Social> e=CMLib.socials().getAllSocials();e.hasMoreElements();)
				allSocials.add(e.nextElement());
			final long[][] totals=new long[allSocials.size()][CoffeeTableRow.STAT_TOTAL];
			while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
			{
				lastCur=curTime;
				final Calendar C2=Calendar.getInstance();
				C2.setTimeInMillis(curTime);
				C2.add(Calendar.DATE,-(scale));
				curTime=C2.getTimeInMillis();
				C2.set(Calendar.HOUR_OF_DAY,23);
				C2.set(Calendar.MINUTE,59);
				C2.set(Calendar.SECOND,59);
				C2.set(Calendar.MILLISECOND,999);
				curTime=C2.getTimeInMillis();
				final ArrayList<CoffeeTableRow> set=new ArrayList<CoffeeTableRow>();
				for(int v=V.size()-1;v>=0;v--)
				{
					final CoffeeTableRow T=V.get(v);
					if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
					{
						set.add(T);
						V.remove(v);
					}
				}
				for(int s=0;s<set.size();s++)
				{
					final CoffeeTableRow T=set.get(s);
					for(int x=0;x<allSocials.size();x++)
						T.totalUp("S"+allSocials.get(x).baseName().toUpperCase(),totals[x]);
				}
				if(scale==0)
					break;
			}
			boolean cr=false;
			for(int x=0;x<allSocials.size();x++)
			{
				Social S=allSocials.get(x);
				if(totals[x][CoffeeTableRow.STAT_SKILLUSE]>0)
				{
					table.append(CMStrings.padRight(""+S.baseName(),25)
							+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_SOCUSE],10));
					if(cr)
						table.append("\n\r");
					cr=!cr;
				}
				x++;
				if(x<allSocials.size())
				{
					S=allSocials.get(x);
					if(totals[x][CoffeeTableRow.STAT_SOCUSE]>0)
					{

						table.append(CMStrings.padRight(""+S.baseName(),25)
								+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_SOCUSE],10));
						if(cr)
							table.append("\n\r");
						cr=!cr;
					}
				}
			}
			if(cr)
				table.append("\n\r");
		}
		else
		if(cmdUse)
		{
			final ArrayList<Command> allCommands=new ArrayList<Command>();
			for(final Enumeration<Command> e=CMClass.commands();e.hasMoreElements();)
				allCommands.add(e.nextElement());
			final long[][] totals=new long[allCommands.size()][CoffeeTableRow.STAT_TOTAL];
			while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
			{
				lastCur=curTime;
				final Calendar C2=Calendar.getInstance();
				C2.setTimeInMillis(curTime);
				C2.add(Calendar.DATE,-(scale));
				curTime=C2.getTimeInMillis();
				C2.set(Calendar.HOUR_OF_DAY,23);
				C2.set(Calendar.MINUTE,59);
				C2.set(Calendar.SECOND,59);
				C2.set(Calendar.MILLISECOND,999);
				curTime=C2.getTimeInMillis();
				final ArrayList<CoffeeTableRow> set=new ArrayList<CoffeeTableRow>();
				for(int v=V.size()-1;v>=0;v--)
				{
					final CoffeeTableRow T=V.get(v);
					if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
					{
						set.add(T);
						V.remove(v);
					}
				}
				for(int s=0;s<set.size();s++)
				{
					final CoffeeTableRow T=set.get(s);
					for(int x=0;x<allCommands.size();x++)
						T.totalUp("M"+allCommands.get(x).ID().toUpperCase(),totals[x]);
				}
				if(scale==0)
					break;
			}
			boolean cr=false;
			for(int x=0;x<allCommands.size();x++)
			{
				Command M=allCommands.get(x);
				if(totals[x][CoffeeTableRow.STAT_CMDUSE]>0)
				{
					table.append(CMStrings.padRight(""+M.ID(),25)
							+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_CMDUSE],10));
					if(cr)
						table.append("\n\r");
					cr=!cr;
				}
				x++;
				if(x<allCommands.size())
				{
					M=allCommands.get(x);
					if(totals[x][CoffeeTableRow.STAT_CMDUSE]>0)
					{

						table.append(CMStrings.padRight(""+M.ID(),25)
								+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_CMDUSE],10));
						if(cr)
							table.append("\n\r");
						cr=!cr;
					}
				}
			}
			if(cr)
				table.append("\n\r");
		}
		else
		if(questStats)
		{
			final List<Quest> sortedQuests=new XVector<Quest>(CMLib.quests().enumQuests());
			Collections.sort(sortedQuests,new Comparator<Quest>()
			{
				@Override
				public int compare(final Quest o1, final Quest o2)
				{
					return o1.name().toLowerCase().compareTo(o2.name().toLowerCase());
				}
			});
			final long[][] totals=new long[sortedQuests.size()][CoffeeTableRow.STAT_TOTAL];
			while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
			{
				lastCur=curTime;
				final Calendar C2=Calendar.getInstance();
				C2.setTimeInMillis(curTime);
				C2.add(Calendar.DATE,-(scale));
				curTime=C2.getTimeInMillis();
				C2.set(Calendar.HOUR_OF_DAY,23);
				C2.set(Calendar.MINUTE,59);
				C2.set(Calendar.SECOND,59);
				C2.set(Calendar.MILLISECOND,999);
				curTime=C2.getTimeInMillis();
				final ArrayList<CoffeeTableRow> set=new ArrayList<CoffeeTableRow>();
				for(int v=V.size()-1;v>=0;v--)
				{
					final CoffeeTableRow T=V.get(v);
					if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
					{
						set.add(T);
						V.remove(v);
					}
				}
				if(set.size()==0)
				{
					set.addAll(V);
					V.clear();
				}
				for(int s=0;s<set.size();s++)
				{
					final CoffeeTableRow T=set.get(s);
					for(int x=0;x<sortedQuests.size();x++)
						T.totalUp("U"+T.tagFix(sortedQuests.get(x).name()),totals[x]);
				}
				if(scale==0)
					break;
			}
			for(int x=0;x<sortedQuests.size();x++)
			{
				final Quest Q=sortedQuests.get(x);
				table.append(
						 CMStrings.padRight(Q.name(),30)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTSTARTATTEMPT],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTTIMESTART],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTFAILEDSTART],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTACCEPTED],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTSUCCESS],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTFAILED],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTDROPPED],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTTIMESTOP],5)
						+CMStrings.centerPreserve(""+totals[x][CoffeeTableRow.STAT_QUESTSTOP],5));
				table.append("\n\r");
			}
			table.append("\n\r");
		}
		else
		if(crimeStats)
		{
			while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
			{
				lastCur=curTime;
				final Calendar C2=Calendar.getInstance();
				C2.setTimeInMillis(curTime);
				C2.add(Calendar.DATE,-(scale));
				curTime=C2.getTimeInMillis();
				C2.set(Calendar.HOUR_OF_DAY,23);
				C2.set(Calendar.MINUTE,59);
				C2.set(Calendar.SECOND,59);
				C2.set(Calendar.MILLISECOND,999);
				curTime=C2.getTimeInMillis();
				final ArrayList<CoffeeTableRow> set=new ArrayList<CoffeeTableRow>();
				for(int v=V.size()-1;v>=0;v--)
				{
					final CoffeeTableRow T=V.get(v);
					if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
					{
						set.add(T);
						V.remove(v);
					}
				}
				final long[] totals=new long[CoffeeTableRow.STAT_TOTAL];
				long highestCOnline=0;
				long numberCOnlineTotal=0;
				long highestPOnline=0;
				long numberPOnlineTotal=0;
				long numberOnlineCounter=0;
				for(int s=0;s<set.size();s++)
				{
					final CoffeeTableRow T=set.get(s);
					T.totalUp(code,totals);
					if(T.highestCharsOnline()>highestCOnline)
						highestCOnline=T.highestCharsOnline();
					numberCOnlineTotal+=T.numberCharsOnlineTotal();
					if(T.highestOnline()>highestPOnline)
						highestPOnline=T.highestOnline();
					numberPOnlineTotal+=T.numberOnlineTotal();
					numberOnlineCounter+=T.numberOnlineCounter();
				}
				if(players)
				{
					highestCOnline=highestPOnline;
					numberCOnlineTotal=numberPOnlineTotal;
				}
				totals[CoffeeTableRow.STAT_TICKSONLINE]=(totals[CoffeeTableRow.STAT_TICKSONLINE]*CMProps.getTickMillis())/scale/(1000*60);
				double avgOnline=(numberOnlineCounter>0)?CMath.div(numberCOnlineTotal,numberOnlineCounter):0.0;
				avgOnline=CMath.div(Math.round(avgOnline*10.0),10.0);
				table.append(CMStrings.padRight(CMLib.time().date2DateString(curTime+1)+" - "+CMLib.time().date2DateString(lastCur-1),25)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_WARRANTS],10)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_ARRESTS],10)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_PAROLES],10)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_JAILINGS],10)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_EXECUTIONS],10)+"\n\r");
				if(scale==0)
					break;
			}
			table.append("\n\r");
		}
		else
		if(areaStats)
		{
			lastCur=ENDQ.getTimeInMillis();
			final ArrayList<CoffeeTableRow> set=new ArrayList<CoffeeTableRow>();
			for(int v=V.size()-1;v>=0;v--)
			{
				final CoffeeTableRow T=V.get(v);
				if((T.startTime()>lastCur)&&(T.endTime()<=curTime))
				{
					set.add(T);
					V.remove(v);
				}
			}
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if(CMLib.flags().canAccess(mob,A)&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD))&&(!(A instanceof SpaceObject)))
				{
					code = "X"+A.Name().toUpperCase().replace(' ','_');
					final long[] totals=new long[CoffeeTableRow.STAT_TOTAL];
					long highestCOnline=0;
					long numberCOnlineTotal=0;
					long highestPOnline=0;
					long numberPOnlineTotal=0;
					long numberOnlineCounter=0;
					for(int s=0;s<set.size();s++)
					{
						final CoffeeTableRow T=set.get(s);
						T.totalUp(code,totals);
						if(T.highestCharsOnline()>highestCOnline)
							highestCOnline=T.highestCharsOnline();
						numberCOnlineTotal+=T.numberCharsOnlineTotal();
						if(T.highestOnline()>highestPOnline)
							highestPOnline=T.highestOnline();
						numberPOnlineTotal+=T.numberOnlineTotal();
						numberOnlineCounter+=T.numberOnlineCounter();
					}
					if(players)
					{
						highestCOnline=highestPOnline;
						numberCOnlineTotal=numberPOnlineTotal;
					}
					double avgOnline=(numberOnlineCounter>0)?CMath.div(numberCOnlineTotal,numberOnlineCounter):0.0;
					avgOnline=CMath.div(Math.round(avgOnline*10.0),10.0);
					totals[CoffeeTableRow.STAT_TICKSONLINE]=(totals[CoffeeTableRow.STAT_TICKSONLINE]*CMProps.getTickMillis())/scale/(1000*60);
					table.append(CMStrings.padRight(A.Name(),25)
								 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_LOGINS],5)
								 +CMStrings.centerPreserve(""+highestCOnline,5)
								 +CMStrings.centerPreserve(""+avgOnline,5)
								 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_TICKSONLINE],5)
								 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_NEWPLAYERS],5)
								 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_DEATHS],5)
								 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_PKDEATHS],5)
								 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_CLASSCHANGE],5)
								 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_PURGES],5)
								 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_MARRIAGES],5)+"\n\r");
				}
			}
		}
		else
		{
			while((V.size()>0)&&(curTime>(ENDQ.getTimeInMillis())))
			{
				lastCur=curTime;
				final Calendar C2=Calendar.getInstance();
				C2.setTimeInMillis(curTime);
				C2.add(Calendar.DATE,-(scale));
				curTime=C2.getTimeInMillis();
				C2.set(Calendar.HOUR_OF_DAY,23);
				C2.set(Calendar.MINUTE,59);
				C2.set(Calendar.SECOND,59);
				C2.set(Calendar.MILLISECOND,999);
				curTime=C2.getTimeInMillis();
				final ArrayList<CoffeeTableRow> set=new ArrayList<CoffeeTableRow>();
				for(int v=V.size()-1;v>=0;v--)
				{
					final CoffeeTableRow T=V.get(v);
					if((T.startTime()>curTime)&&(T.endTime()<=lastCur))
					{
						set.add(T);
						V.remove(v);
					}
				}
				final long[] totals=new long[CoffeeTableRow.STAT_TOTAL];
				long highestCOnline=0;
				long numberCOnlineTotal=0;
				long highestPOnline=0;
				long numberPOnlineTotal=0;
				long numberOnlineCounter=0;
				for(int s=0;s<set.size();s++)
				{
					final CoffeeTableRow T=set.get(s);
					T.totalUp(code,totals);
					if(T.highestCharsOnline()>highestCOnline)
						highestCOnline=T.highestCharsOnline();
					numberCOnlineTotal+=T.numberCharsOnlineTotal();
					if(T.highestOnline()>highestPOnline)
						highestPOnline=T.highestOnline();
					numberPOnlineTotal+=T.numberOnlineTotal();
					numberOnlineCounter+=T.numberOnlineCounter();
				}
				if(players)
				{
					highestCOnline=highestPOnline;
					numberCOnlineTotal=numberPOnlineTotal;
				}
				double avgOnline=(numberOnlineCounter>0)?CMath.div(numberCOnlineTotal,numberOnlineCounter):0.0;
				avgOnline=CMath.div(Math.round(avgOnline*10.0),10.0);
				totals[CoffeeTableRow.STAT_TICKSONLINE]=(totals[CoffeeTableRow.STAT_TICKSONLINE]*CMProps.getTickMillis())/scale/(1000*60);
				table.append(CMStrings.padRight(CMLib.time().date2DateString(curTime+1)+" - "+CMLib.time().date2DateString(lastCur-1),25)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_LOGINS],5)
							 +CMStrings.centerPreserve(""+highestCOnline,5)
							 +CMStrings.centerPreserve(""+avgOnline,5)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_TICKSONLINE],5)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_NEWPLAYERS],5)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_DEATHS],5)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_PKDEATHS],5)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_CLASSCHANGE],5)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_PURGES],5)
							 +CMStrings.centerPreserve(""+totals[CoffeeTableRow.STAT_MARRIAGES],5)+"\n\r");
				if(scale==0)
					break;
			}
		}
		mob.tell(table.toString());
		return false;
	}

	public int averageDamage(final MOB M)
	{
		double total=0;
		final double num=5000;
		for(int i=0;i<num;i++)
		{
			total+=CMLib.combat().adjustedDamage(M,null,null,0,true, false);
		}
		return (int)Math.round(Math.floor(total / num));
	}

	protected void addCharStatsChars(final CharStats cstats, final int headerWidth, final int numberWidth, final int[] col, final StringBuilder str)
	{
		for(int i=0;i<cstats.getStatCodes().length;i++)
		{
			str.append("^g"+CMStrings.padRight(cstats.getStatCodes()[i]+"^w", headerWidth));
			str.append(" ");
			str.append(CMStrings.padRight(""+cstats.getStat(cstats.getStatCodes()[i]), numberWidth));
			col[0]++;
			if(col[0]==4)
				str.append("\n\r");
		}
	}

	protected void addCharThing(final int headerWidth, final int numberWidth, final int[] col, final StringBuilder str, final String title, final String val)
	{
		str.append("^y"+CMStrings.padRight(title+"^w", headerWidth));
		str.append(" ");
		str.append(CMStrings.padRight(""+val, numberWidth));
		col[0]++;
		if(col[0]==4)
			str.append("\n\r");
	}

	protected void addCharStatsState(final CharState cstats, final int headerWidth, final int numberWidth, final int[] col, final StringBuilder str)
	{
		for(int i=0;i<cstats.getStatCodes().length;i++)
		{
			str.append("^y"+CMStrings.padRight(cstats.getStatCodes()[i]+"^w", headerWidth));
			str.append(" ");
			str.append(CMStrings.padRight(""+cstats.getStat(cstats.getStatCodes()[i]), numberWidth));
			col[0]++;
			if(col[0]==4)
				str.append("\n\r");
		}
	}

	protected MOB getMOBTarget(final MOB mob, final String MOBname)
	{
		MOB target=getTarget(mob,MOBname,true);
		if(target==null)
			target=CMLib.players().getLoadPlayerAllHosts(MOBname);
		if(target==null)
		{
			try
			{
				final List<MOB> inhabs=CMLib.hunt().findInhabitantsFavorExact(mob.location().getArea().getProperMap(), mob,MOBname,false,100);
				if(inhabs.size()==0)
					inhabs.addAll(CMLib.hunt().findInhabitantsFavorExact(CMLib.map().rooms(), mob,MOBname,false,100));
				for(final MOB mob2 : inhabs)
				{
					final Room R=mob2.location();
					if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.STAT))
					{
						target=mob2;
						break;
					}
				}
			}
			catch (final NoSuchElementException nse)
			{
			}
		}
		return target;
	}

	protected Item getItemTarget(final MOB mob, final String itemName)
	{
		Item target=null;
		//if(target == null)
			target=mob.findItem(itemName);
		if(target == null)
			target=mob.location().findItem(itemName);
		if(target==null)
		{
			try
			{
				final List<Item> items=CMLib.hunt().findRoomItems(mob.location().getArea().getProperMap(), mob,itemName,true,100);
				if(items.size()==0)
					items.addAll(CMLib.hunt().findRoomItems(CMLib.map().rooms(), mob,itemName,true,100));
				for(final Item item2 : items)
				{
					final Room R=CMLib.map().roomLocation(item2);
					if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.STAT))
					{
						target=item2;
						break;
					}
				}
			}
			catch (final NoSuchElementException nse)
			{
			}
		}
		if(target==null)
		{
			try
			{
				final List<Item> items=CMLib.hunt().findInventory(mob.location().getArea().getProperMap(), mob,itemName,100);
				if(items.size()==0)
					items.addAll(CMLib.hunt().findInventory(CMLib.map().rooms(), mob,itemName,100));
				for(final Item item2 : items)
				{
					final Room R=CMLib.map().roomLocation(item2);
					if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.STAT))
					{
						target=item2;
						break;
					}
				}
			}
			catch (final NoSuchElementException nse)
			{
			}
		}
		return target;
	}

	protected void addCharStatsPhys(final PhyStats pstats, final int headerWidth, final int numberWidth, final int[] col, final StringBuilder str)
	{
		for(int i=0;i<pstats.getStatCodes().length;i++)
		{
			str.append("^c"+CMStrings.padRight(pstats.getStatCodes()[i]+"^w", headerWidth));
			str.append(" ");
			str.append(CMStrings.padRight(""+pstats.getStat(pstats.getStatCodes()[i]), numberWidth));
			col[0]++;
			if(col[0]==4)
				str.append("\n\r");
		}
		for(int i=0;i<PhyStats.CAN_SEE_CODES.length;i++)
		{
			str.append("^c"+CMStrings.padRight(PhyStats.CAN_SEE_CODES[i]+"^w", headerWidth));
			str.append(" ");
			str.append(CMStrings.padRight(""+CMath.isSet(pstats.sensesMask(), i), numberWidth));
			col[0]++;
			if(col[0]==4)
				str.append("\n\r");
		}
		for(int i=0;i<PhyStats.IS_CODES.length;i++)
		{
			str.append("^c"+CMStrings.padRight(PhyStats.IS_CODES[i]+"^w", headerWidth));
			str.append(" ");
			str.append(CMStrings.padRight(""+CMath.isSet(pstats.disposition(), i), numberWidth));
			col[0]++;
			if(col[0]==4)
				str.append("\n\r");
		}
	}

	protected String getPrivilegedStat(final MOB mob, MOB target, final List<String> commands, final boolean overrideAuthCheck) throws IOException
	{
		StringBuilder str=new StringBuilder("");
		int ableTypes=-1;
		if(commands.size()>1)
		{
			final String s=commands.get(0).toUpperCase();
			for(int i=0;i<ABLETYPE_DESCS.length;i++)
			{
				for(int is=0;is<ABLETYPE_DESCS[i].length;is++)
				{
					if(s.equals(ABLETYPE_DESCS[i][is]))
					{
						ableTypes=-2 -i;
						commands.remove(0);
						break;
					}
				}
			}
			if(ableTypes==-1)
			{
				for(int a=0;a<Ability.ACODE.DESCS.size();a++)
				{
					if((Ability.ACODE.DESCS.get(a)+"S").equals(s)||(Ability.ACODE.DESCS.get(a)).equals(s))
					{
						ableTypes=a;
						commands.remove(0);
						break;
					}
				}
			}
		}
		final String MOBname=CMParms.combine(commands,0);
		if(target == null)
			target=getMOBTarget(mob,MOBname);
		if((target!=null)
		&&(((target.isMonster())&&(CMSecurity.isAllowed(mob, target.location(), CMSecurity.SecFlag.CMDMOBS)))
			||(overrideAuthCheck)
			||((target.isPlayer())&&(CMSecurity.isAllowed(mob, target.location(), CMSecurity.SecFlag.CMDPLAYERS)))))
		{
			if(ableTypes>=0)
			{
				final List<Integer> V=new ArrayList<Integer>();
				final int mask=Ability.ALL_ACODES;
				V.add(Integer.valueOf(ableTypes));
				str=getAbilities(mob,target,V,mask,false,-1);
			}
			else
			if(ableTypes==ABLETYPE_EQUIPMENT)
				str=CMLib.commands().getEquipment(mob,target);
			else
			if(ableTypes==ABLETYPE_INVENTORY)
				str=CMLib.commands().getInventory(mob,target);
			else
			if(ableTypes==ABLETYPE_QUESTWINS)
			{
				str.append(L("Quests won by @x1: ",target.Name()));
				final StringBuffer won=new StringBuffer("");
				for(int q=0;q<CMLib.quests().numQuests();q++)
				{
					final Quest Q=CMLib.quests().fetchQuest(q);
					final Long wonTime = Q.whenLastWon(target.Name());
					if(wonTime != null)
					{
						final String name=Q.displayName().trim().length()>0?Q.displayName():Q.name();
						won.append(" "+name+" on "+CMLib.time().date2String(wonTime.longValue())+" ,");
					}
				}
				if(won.length()==0)
					won.append(L(" None!"));
				won.deleteCharAt(won.length()-1);
				str.append(won);
				str.append("\n\r");
			}
			else
			if(ableTypes==ABLETYPE_TITLES)
			{
				str.append(L("Titles:"));
				final StringBuffer ttl=new StringBuffer("");
				if(target.playerStats()!=null)
				{
					final List<String> roTitles = target.playerStats().getTitles();
					for(int t=0;t<roTitles.size();t++)
					{
						final String title = roTitles.get(t);
						ttl.append(" "+title+",");
					}
				}
				if(ttl.length()==0)
					ttl.append(L(" None!"));
				ttl.deleteCharAt(ttl.length()-1);
				str.append(ttl);
				str.append("\n\r");
			}
			else
			if(ableTypes==ABLETYPE_SCRIPTS)
			{
				str.append(L("Scripts covered:\n\r"));
				int q=1;
				for(final Enumeration<ScriptingEngine> e=target.scripts();e.hasMoreElements();q++)
				{
					final ScriptingEngine SE=e.nextElement();
					str.append(L("Script #@x1\n\r",""+q));
					str.append(L("Quest: @x1\n\r",SE.defaultQuestName()));
					str.append(L("Savable: @x1\n\r",""+SE.isSavable()));
					str.append(L("Scope: @x1\n\r",SE.getVarScope()));
					str.append(L("Vars: @x1\n\r",SE.getLocalVarXML()));
					str.append(L("Script: @x1\n\r",SE.getScript()));
					str.append("\n\r");
				}
				str.append("\n\r");
			}
			else
			if(ableTypes==ABLETYPE_TATTOOS)
			{
				str.append(L("Tattoos:"));
				for(final Enumeration<Tattoo> e=target.tattoos();e.hasMoreElements();)
					str.append(" "+e.nextElement().getTattooName()+",");
				str.deleteCharAt(str.length()-1);
				str.append("\n\r");
			}
			else
			if(ableTypes==ABLETYPE_OBJECTS)
			{
				str.append(L("Player Objects:\n\r  "));
				final PlayerStats pstats = target.playerStats();
				if(pstats != null)
				{
					for(final Enumeration<Item> e=pstats.getExtItems().items();e.hasMoreElements();)
						str.append(" "+e.nextElement().name(mob)+", ");
					str.deleteCharAt(str.length()-1);
					str.deleteCharAt(str.length()-1);
					str.append("\n\r");
				}
			}
			else
			if(ableTypes==ABLETYPE_AFFECTS)
			{
				str.append(L("Effects:"));
				for(final Enumeration<Ability> e=target.effects();e.hasMoreElements();)
					str.append(" "+e.nextElement().Name()+",");
				str.deleteCharAt(str.length()-1);
				str.append("\n\r");
			}
			else
			if(ableTypes==ABLETYPE_FACTIONS)
			{
				str.append(L("Factions:\n\r"));
				for(final Enumeration<String> f=target.factions();f.hasMoreElements();)
				{
					final Faction F=CMLib.factions().getFaction(f.nextElement());
					if(F!=null)
						str.append("^W[^H"+F.name()+"^N("+F.factionID()+"): "+target.fetchFaction(F.factionID())+"^W]^N, ");
				}
				str.append("\n\r");
			}
			else
			if(ableTypes == ABLETYPE_LEVELTIMES)
			{
				if(target.playerStats() != null)
				{
					long lastDateTime=-1;
					for(int level=0;level<=target.phyStats().level();level++)
					{
						final long dateTime=target.playerStats().leveledDateTime(level);
						final long ageMinutes=target.playerStats().leveledMinutesPlayed(level);
					 	final String roomID=target.playerStats().leveledRoomID(level);
						if((dateTime>1529122205)&&(dateTime!=lastDateTime))
						{
							lastDateTime = dateTime;
							if(level==0)
							 	str.append(CMStrings.padRight(L("Created"),8));
							else
							 	str.append(CMStrings.padRight(""+level,8));
							str.append(CMStrings.padRight(CMLib.time().date2String(dateTime),21));
							str.append(CMStrings.padRight(""+CMLib.time().date2EllapsedTime(ageMinutes * 60000L,TimeUnit.MINUTES,true),17));
							final Room R=CMLib.map().getRoom(roomID);
							if(R==null)
								str.append(roomID);
							else
								str.append(CMStrings.limit(R.displayText(), 25)).append("("+roomID+")");
							str.append("\n\r");
						}
					}
				}
				str.append("\n\r");
			}
			else
			if(ableTypes==ABLETYPE_CHARSTATS)
			{
				str.append(L("^XCurrent Character Statistics:^.^N\n\r"));
				final int[] col={0};
				final int headerWidth=CMLib.lister().fixColWidth(12, mob);
				final int numberWidth=CMLib.lister().fixColWidth(6, mob);
				addCharStatsChars(target.charStats(), headerWidth, numberWidth, col, str);
				addCharStatsPhys(target.phyStats(), headerWidth, numberWidth, col, str);
				addCharStatsState(target.curState(), headerWidth, numberWidth, col, str);
				if(target.playerStats()!=null)
					addCharThing(headerWidth,numberWidth,col,str,"STINK",CMath.toPct(target.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT));
				str.append("\n\r\n\r");
				str.append(L("^XBase Character Statistics:^.^N\n\r"));
				col[0]=0;
				addCharStatsChars(target.baseCharStats(), headerWidth, numberWidth, col, str);
				addCharStatsPhys(target.basePhyStats(), headerWidth, numberWidth, col, str);
				addCharStatsState(target.baseState(), headerWidth, numberWidth, col, str);
				str.append("\n\r\n\r");
				str.append(L("^XMax Character State:^.^N\n\r"));
				col[0]=0;
				addCharStatsState(target.maxState(), headerWidth, numberWidth, col, str);
				str.append("\n\r");
			}
			else
			if(ableTypes==ABLETYPE_WORLDEXPLORED)
			{
				if(target.playerStats()!=null)
					str.append(L("@x1 has explored @x2% of the world.\n\r",target.name(),""+target.playerStats().percentVisited(target,null)));
				else
					str.append(L("Exploration data is not kept on mobs.\n\r"));
			}
			else
			if(ableTypes==ABLETYPE_AREASEXPLORED)
			{
				if(target.playerStats()!=null)
				{
					for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
					{
						final Area A=e.nextElement();
						final int pct=target.playerStats().percentVisited(target, A);
						if(pct>0)
							str.append("^H"+A.name()+"^N: "+pct+"%, ");
					}
					str=new StringBuilder(str.toString().substring(0,str.toString().length()-2)+"\n\r");
				}
				else
					str.append(L("Exploration data is not kept on mobs.\n\r"));
			}
			else
			if(ableTypes==ABLETYPE_ROOMSEXPLORED)
			{
				if(target.playerStats()!=null)
				{
					for(final Enumeration<Room> e=CMLib.map().rooms();e.hasMoreElements();)
					{
						final Room R=e.nextElement();
						if((R.roomID().length()>0)&&(target.playerStats().hasVisited(R)))
							str.append("^H"+R.roomID()+"^N, ");
					}
					str=new StringBuilder(str.toString().substring(0,str.toString().length()-2)+"\n\r");
				}
				else
					str.append(L("Exploration data is not kept on mobs.\n\r"));
			}
			else
			if(ableTypes==ABLETYPE_COMBAT)
			{
				final PlayerStats pStats = target.playerStats();
				if((pStats != null)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.COMBATSTATS)))
				{
					final int level=target.basePhyStats().level();
					long combats = pStats.bumpLevelCombatStat(PlayerCombatStat.COMBATS_TOTAL, level, 0);
					if(combats == 0)
						combats=1;
					long rounds = pStats.bumpLevelCombatStat(PlayerCombatStat.ROUNDS_TOTAL, level, 0);
					if(rounds == 0)
						rounds=1;
					final long xp = pStats.bumpLevelCombatStat(PlayerCombatStat.EXPERIENCE_TOTAL, level, 0);
					final long damage = pStats.bumpLevelCombatStat(PlayerCombatStat.DAMAGE_DONE, level, 0);
					final long hits = pStats.bumpLevelCombatStat(PlayerCombatStat.HITS_DONE, level, 0);
					final long hurt = pStats.bumpLevelCombatStat(PlayerCombatStat.DAMAGE_TAKEN, level, 0);
					final long hitstaken = pStats.bumpLevelCombatStat(PlayerCombatStat.HITS_TAKEN, level, 0);
					final long actions = pStats.bumpLevelCombatStat(PlayerCombatStat.ACTIONS_DONE, level, 0);
					str.append(L("Player Combat Summary for level @x1:\n\r",""+level));
					str.append(CMStrings.padRight(L("Total Combats"),20)).append(": ")
						.append(combats)
						.append("\n\r");
					str.append(CMStrings.padRight(L("Total Rounds"),20)).append(": ")
						.append(rounds)
						.append("\n\r");
					str.append(CMStrings.padRight(L("Total Kills"),20)).append(": ")
						.append(pStats.bumpLevelCombatStat(PlayerCombatStat.DEATHS_DONE, level, 0))
						.append("\n\r");
					str.append(CMStrings.padRight(L("Total Deaths"),20)).append(": ")
						.append(pStats.bumpLevelCombatStat(PlayerCombatStat.DEATHS_TAKEN, level, 0))
						.append("\n\r");
					str.append(CMStrings.padRight(L("Experience"),20)).append(": ")
						.append(CMStrings.padRight(""+xp,15))
						.append(" ").append(CMath.round(CMath.div(xp,combats),2)).append("/combat ")
						.append(", ").append(CMath.round(CMath.div(xp,rounds),2)).append("/round")
						.append("\n\r");
					str.append(CMStrings.padRight(L("Damage Done"),20)).append(": ")
						.append(CMStrings.padRight(""+damage,15))
						.append(" ").append(CMath.round(CMath.div(damage,combats),2)).append("/combat ")
						.append(", ").append(CMath.round(CMath.div(damage,rounds),2)).append("/round")
						.append("\n\r");
					str.append(CMStrings.padRight(L("Damage Taken"),20)).append(": ")
						.append(CMStrings.padRight(""+hurt,15))
						.append(" ").append(CMath.round(CMath.div(hurt,combats),2)).append("/combat ")
						.append(", ").append(CMath.round(CMath.div(hurt,rounds),2)).append("/round")
						.append("\n\r");
					str.append(CMStrings.padRight(L("Hits Done"),20)).append(": ")
						.append(CMStrings.padRight(""+hits,15))
						.append(" ").append(CMath.round(CMath.div(hits,combats),2)).append("/combat ")
						.append(", ").append(CMath.round(CMath.div(hits,rounds),2)).append("/round")
						.append("\n\r");
					str.append(CMStrings.padRight(L("Hits Taken"),20)).append(": ")
						.append(CMStrings.padRight(""+hitstaken,15))
						.append(" ").append(CMath.round(CMath.div(hitstaken,combats),2)).append("/combat ")
						.append(", ").append(CMath.round(CMath.div(hitstaken,rounds),2)).append("/round")
						.append("\n\r");
					str.append(CMStrings.padRight(L("Actions Done"),20)).append(": ")
						.append(CMStrings.padRight(""+actions,15))
						.append(" ").append(CMath.round(CMath.div(actions,combats),2)).append("/combat ")
						.append(", ").append(CMath.round(CMath.div(actions,rounds),2)).append("/round")
						.append("\n\r");
					str.append("^W-------------------------\n\r");
				}
				str.append(L("\n\r^cCombat summary:\n\r\n\r^N"));
				final MOB M=CMClass.getMOB("StdMOB");
				M.setBaseCharStats((CharStats)target.baseCharStats().copyOf());
				M.setBasePhyStats((PhyStats)target.basePhyStats().copyOf());
				M.setBaseState((CharState)target.baseState().copyOf());
				recoverMOB(target);
				recoverMOB(M);
				int base=M.basePhyStats().attackAdjustment();
				str.append("^c"+CMStrings.padRight(L("Base Attack"),40)+": ^W"+base+"\n\r");
				for(int i=0;i<target.numItems();i++)
				{
					final Item I=target.getItem(i);
					if ((I != null) && (!I.amWearingAt(Wearable.IN_INVENTORY)))
					{
						recoverMOB(M);
						base = M.phyStats().attackAdjustment();
						testMOB(target, M, I);
						final int diff = M.phyStats().attackAdjustment() - base;
						reportOnDiffMOB(I, diff, str);
					}
				}
				recoverMOB(M);
				for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if (A != null)
					{
						recoverMOB(M);
						base = M.phyStats().attackAdjustment();
						testMOB(target, M, A);
						final int diff = M.phyStats().attackAdjustment() - base;
						reportOnDiffMOB(A, diff, str);
					}
				}
				recoverMOB(target);
				recoverMOB(M);
				reportOnDiffMOB("Other Stuff", CMLib.combat().adjustedAttackBonus(target,null)-M.basePhyStats().attackAdjustment(), str);
				str.append("^W-------------------------\n\r");
				str.append("^C"+CMStrings.padRight(L("Total"),40)+": ^W"+CMLib.combat().adjustedAttackBonus(target,null)+"\n\r");
				str.append("\n\r");
				base=M.basePhyStats().armor();
				str.append("^C"+CMStrings.padRight(L("Base Armor"),40)+": ^W"+base+"\n\r");
				for(int i=0;i<target.numItems();i++)
				{
					final Item I=target.getItem(i);
					if (I != null)
					{
						recoverMOB(M);
						base = M.phyStats().armor();
						testMOB(target, M, I);
						final int diff = M.phyStats().armor() - base;
						reportOnDiffMOB(I, diff, str);
					}
				}
				recoverMOB(M);
				for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if (A != null)
					{
						recoverMOB(M);
						base = M.phyStats().armor();
						testMOB(target, M, A);
						final int diff = M.phyStats().armor() - base;
						reportOnDiffMOB(A, diff, str);
					}
				}
				recoverMOB(target);
				recoverMOB(M);
				reportOnDiffMOB("Other Stuff", CMLib.combat().adjustedArmor(target)-M.basePhyStats().attackAdjustment(), str);
				str.append("^W-------------------------\n\r");
				str.append("^C"+CMStrings.padRight(L("Total"),40)+": ^W"+CMLib.combat().adjustedArmor(target)+"\n\r");
				str.append("\n\r");
				base=M.basePhyStats().damage();
				str.append("^C"+CMStrings.padRight(L("Base Damage"),40)+": ^W"+base+"\n\r");
				for(int i=0;i<target.numItems();i++)
				{
					final Item I=target.getItem(i);
					if (I != null)
					{
						recoverMOB(M);
						base = M.phyStats().damage();
						testMOB(target, M, I);
						final int diff = M.phyStats().damage() - base;
						reportOnDiffMOB(I, diff, str);
					}
				}
				recoverMOB(M);
				for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if (A != null)
					{
						recoverMOB(M);
						base = M.phyStats().damage();
						testMOB(target, M, A);
						final int diff = M.phyStats().damage() - base;
						reportOnDiffMOB(A, diff, str);
					}
				}
				recoverMOB(target);
				recoverMOB(M);
				reportOnDiffMOB("Other Stuff", CMLib.combat().adjustedDamage(target,null,null,0, false, false)-M.basePhyStats().damage(), str);
				str.append("^W-------------------------\n\r");
				str.append("^C"+CMStrings.padRight(L("Total"),40)+": ^W"+CMLib.combat().adjustedDamage(target, null, null, 0, false, false)+"\n\r");
				str.append("\n\r");
				base=(int)Math.round(M.phyStats().speed()*100);
				str.append("^C"+CMStrings.padRight(L("Base Attacks%"),40)+": ^W"+base+"\n\r");
				for(int i=0;i<target.numItems();i++)
				{
					final Item I=target.getItem(i);
					if (I != null)
					{
						recoverMOB(M);
						base = (int) Math.round(M.phyStats().speed() * 100);
						testMOB(target, M, I);
						final int diff = (int) Math.round(M.phyStats().speed() * 100) - base;
						reportOnDiffMOB(I, diff, str);
					}
				}
				recoverMOB(M);
				for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if (A != null)
					{
						recoverMOB(M);
						base = (int) Math.round(M.phyStats().speed() * 100);
						testMOB(target, M, A);
						final int diff = (int) Math.round(M.phyStats().speed() * 100) - base;
						reportOnDiffMOB(A, diff, str);
					}
				}
				recoverMOB(target);
				recoverMOB(M);
				str.append("^W-------------------------\n\r");
				str.append("^C"+CMStrings.padRight(L("Total"),40)+": ^W"+(int)Math.round(target.phyStats().speed()*100)+"\n\r");
				str.append("\n\r");
				base=M.maxState().getHitPoints();
				str.append("^C"+CMStrings.padRight(L("Base Hit Points"),40)+": ^W"+base+"\n\r");
				for(int i=0;i<target.numItems();i++)
				{
					final Item I=target.getItem(i);
					if (I != null)
					{
						recoverMOB(M);
						base = M.maxState().getHitPoints();
						testMOB(target, M, I);
						final int diff = M.maxState().getHitPoints() - base;
						reportOnDiffMOB(I, diff, str);
					}
				}
				recoverMOB(M);
				for(int i=0;i<target.numAllEffects();i++)
				{
					final Ability A=target.fetchEffect(i);
					if (A != null)
					{
						recoverMOB(M);
						base = M.maxState().getHitPoints();
						testMOB(target, M, A);
						final int diff = M.maxState().getHitPoints() - base;
						reportOnDiffMOB(A, diff, str);
					}
				}
				recoverMOB(M);
				str.append("^W-------------------------\n\r");
				str.append("^C"+CMStrings.padRight(L("Total"),40)+": ^W"+target.maxState().getHitPoints()+"\n\r");
				recoverMOB(target);
			}
			else
			{
				if((target.playerStats()!=null)&&(CMProps.isUsingAccountSystem()))
					str.append(L("\n\r^xMember of Account:^.^N ^w@x1^?",(target.playerStats().getAccount()!=null)?target.playerStats().getAccount().getAccountName():L("None"))).append("\n\r");
				str.append(CMLib.commands().getScore(target));
				for(final Enumeration<Quest> q= CMLib.quests().enumQuests();q.hasMoreElements();)
				{
					final Quest Q=q.nextElement();
					if((Q!=null)
					&&(Q.running())
					&&(Q.isObjectInUse(target)))
						str.append(L("\n\r^xIn use by quest:^.^N ^w@x1^?",Q.name())).append("\n\r");
				}
				CMLib.genEd().genMiscSet(mob, target, -950);
			}
		}
		return str.toString();
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		commands.remove(0);
		final Set<String> allowedCharStats = new HashSet<String>();
		if((!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.STAT))
		&&(CMSecurity.isDisabled(DisFlag.FULLSTATS))
		&&(CMProps.getListFileStringList(CMProps.ListFile.LMT_STATS).length>0))
			allowedCharStats.addAll(Arrays.asList(CMProps.getListFileStringList(CMProps.ListFile.LMT_STATS)));
		if(((commands.size()>0)&&commands.get(0).equals("?"))
		||((commands.size()==0)&&(!(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.STAT)))))
		{
			final StringBuilder msg = new StringBuilder("STAT allows the following options: \n\r");
			if(allowedCharStats.size()>0)
			{
				for(final String stat : allowedCharStats)
					msg.append(stat).append(", ");
			}
			else
			{
				for(final String stat : mob.curState().getStatCodes())
					msg.append(stat).append(", ");
				for(final String stat : mob.curState().getStatCodes())
					msg.append("MAX"+stat).append(", ");
				for(final String stat : mob.charStats().getStatCodes())
					msg.append(stat).append(", ");
				for(final String stat : mob.charStats().getStatCodes())
					msg.append("BASE"+stat).append(", ");
				for(final String stat : mob.phyStats().getStatCodes())
					msg.append(stat).append(", ");
				msg.append("STINK, XP, XPTNL, XPFNL, QUESTPOINTS, TRAINS, PRACTICES, HEALTH, RESISTS, ATTRIBUTES");
				for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
				{
					final Faction F=f.nextElement();
					if((F!=null)&&(F.showInScore()))
						msg.append(", "+F.name().toUpperCase().replace(' ','_'));
				}
				if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.STAT))
				{
					msg.append(L(", [MOB/PLAYER NAME], [NUMBER] [DAYS/WEEKS/MONTHS], "));
					for (final String[] element : ABLETYPE_DESCS)
						msg.append(element[0]+", ");
					msg.append(CMParms.toListString(Ability.ACODE.DESCS));
				}
			}
			final String msgStr = msg.toString().trim();
			if(msgStr.endsWith(","))
				mob.tell(msgStr.substring(0,msgStr.length()-1));
			else
				mob.tell(msgStr);
			return false;
		}
		final StringBuilder str=new StringBuilder("");
		if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.STAT))
		{
			if(commands.size()==0)
				commands.add("TODAY");
			final String s1=(commands.size()>0)?commands.get(0).toUpperCase():"";
			final String s2=(commands.size()>1)?commands.get(1).toUpperCase():"";
			if(s1.equalsIgnoreCase("TODAY"))
				return showTableStats(mob,1,1,CMParms.combine(commands,1));
			else
			if((commands.size()==1)
			&&(s1.equalsIgnoreCase("SKILLUSE")||s1.equalsIgnoreCase("AREA")||s1.equalsIgnoreCase("QUEST")||s1.equalsIgnoreCase("CRIME")))
				return showTableStats(mob,1,1,CMParms.combine(commands,0));
			else
			if(commands.size()>1)
			{
				final String rest=(commands.size()>2)?CMParms.combine(commands,2):"";
				if(s2.equals("DAY")&&(CMath.isNumber(s1)))
					return showTableStats(mob,(CMath.s_int(s1)),1,rest);
				else
				if(s2.equals("DAYS")&&(CMath.isNumber(s1)))
					return showTableStats(mob,(CMath.s_int(s1)),1,rest);
				else
				if(s2.equals("WEEK")&&(CMath.isNumber(s1)))
					return showTableStats(mob,(CMath.s_int(s1)*7),7,rest);
				else
				if(s2.equals("WEEKS")&&(CMath.isNumber(s1)))
					return showTableStats(mob,(CMath.s_int(s1)*7),7,rest);
				else
				if(s2.equals("MONTH")&&(CMath.isNumber(s1)))
					return showTableStats(mob,(CMath.s_int(s1)*30),30,rest);
				else
				if(s2.equals("MONTHS")&&(CMath.isNumber(s1)))
					return showTableStats(mob,(CMath.s_int(s1)*30),30,rest);
				else
				if(s2.equals("YEAR")&&(CMath.isNumber(s1)))
					return showTableStats(mob,(CMath.s_int(s1)*365),365,rest);
				else
				if(s2.equals("YEARS")&&(CMath.isNumber(s1)))
					return showTableStats(mob,(CMath.s_int(s1)*365),365,rest);
			}
			str.append(this.getPrivilegedStat(mob, null, commands, false));
		}
		if((commands.size()>0)&&(str.length()==0))
		{
			String mobName=commands.get(commands.size()-1).toString();
			MOB M=mob;
			if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.STAT))
			{
				final String firstWord = (commands.size()> 1) ? commands.get(0) : "";
				final String restWords = (commands.size() > 1) ? CMParms.combine(commands,1) : "";
				mobName = CMParms.combine(commands,0);
				if(mobName.equalsIgnoreCase("ROOM"))
				{
					CMLib.genEd().modifyRoom(mob, mob.location(), -950);
					return true;
				}
				else
				if(mobName.equalsIgnoreCase("AREA"))
				{
					final Set<Area> alsoUpdateAreas=new HashSet<Area>();
					CMLib.genEd().modifyArea(mob, mob.location().getArea(), alsoUpdateAreas, -950);
					return true;
				}
				else
				if(firstWord.equalsIgnoreCase("CLAN"))
				{
					final Clan C=CMLib.clans().findClan(restWords);
					if(C!=null)
					{
						str.setLength(0);
						str.append(CMStrings.padRight(""+C.getClanLevel(),7)).append(": ").append(L("Clan Level")).append("\n\r");
						for(final Clan.Trophy t : Clan.Trophy.values())
							str.append(CMStrings.padRight(""+C.getTrophyData(t),7)).append(": ").append(t.codeString()).append("\n\r");
						mob.tell(str.toString());
						return true;
					}
					mob.tell(L("You can't stat clan '@x1'!",restWords));
					return false;
				}
				else
				if(firstWord.equalsIgnoreCase("EXIT"))
				{
					Environmental itarget=getItemTarget(mob, mobName);
					if(itarget==null)
					{
						itarget=mob.location().fetchExit(mobName);
						return true;
					}
					mob.tell(L("You can't stat exit '@x1'!",restWords));
					return false;
				}
				else
				if(firstWord.equalsIgnoreCase("ITEM"))
				{
					final Environmental itarget=getItemTarget(mob, restWords);
					if(itarget!=null)
					{
						for(final Enumeration<Quest> q= CMLib.quests().enumQuests();q.hasMoreElements();)
						{
							final Quest Q=q.nextElement();
							if((Q!=null)
							&&(Q.running())
							&&(Q.isObjectInUse(itarget)))
								mob.tell(L("\n\r^xIn use by quest:^.^N ^w@x1^?",Q.name()));
						}
						CMLib.genEd().genMiscSet(mob, itarget, -950);
						return true;
					}
					mob.tell(L("You can't stat item '@x1'!",restWords));
					return false;
				}
				else
				if(firstWord.equalsIgnoreCase("ROOM"))
				{
					Environmental itarget = null;
					try
					{
						final List<Room> rooms=CMLib.hunt().findRooms(mob.location().getArea().getProperMap(), mob,restWords,true,100);
						if(rooms.size()==0)
							rooms.addAll(CMLib.hunt().findRooms(mob.location().getArea().getProperMap(), mob,restWords,false,100));
						if(rooms.size()==0)
							rooms.addAll(CMLib.hunt().findRooms(CMLib.map().rooms(), mob,restWords,true,100));
						if(rooms.size()==0)
							rooms.addAll(CMLib.hunt().findRooms(CMLib.map().rooms(), mob,restWords,false,100));
						for(final Room room : rooms)
						{
							final Room R=CMLib.map().roomLocation(room);
							if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.STAT))
							{
								itarget=room;
								break;
							}
						}
					}
					catch (final NoSuchElementException nse)
					{
					}
					if(itarget != null)
					{
						CMLib.genEd().modifyRoom(mob, (Room)itarget, -950);
						return true;
					}
					else
					{
						mob.tell(L("You can't stat room '@x1'!",restWords));
						return false;
					}
				}
				else
				if(firstWord.equalsIgnoreCase("AREA"))
				{
					final Environmental itarget = CMLib.map().findArea(restWords);
					if(itarget != null)
					{
						final Set<Area> alsoUpdateAreas=new HashSet<Area>();
						CMLib.genEd().modifyArea(mob, (Area)itarget, alsoUpdateAreas, -950);
						return true;
					}
					mob.tell(L("You can't stat area '@x1'!",restWords));
					return false;
				}
				else
				{
					mobName=commands.get(commands.size()-1).toString();
					final MOB target=this.getMOBTarget(mob, mobName);
					if((target!=null)
					&&(((target.isMonster())&&(CMSecurity.isAllowed(mob, target.location(), CMSecurity.SecFlag.CMDMOBS)))
						||((target.isPlayer())&&(CMSecurity.isAllowed(mob, target.location(), CMSecurity.SecFlag.CMDPLAYERS)))))
					{
						M=target;
						commands.remove(commands.size()-1);
					}
					else
					{
						mobName = CMParms.combine(commands,0);
						Environmental itarget=null;
						//if(itarget==null)
							itarget=this.getItemTarget(mob, mobName);
						if(itarget==null)
							itarget=mob.location().fetchExit(mobName);
						if(itarget!=null)
						{
							for(final Enumeration<Quest> q= CMLib.quests().enumQuests();q.hasMoreElements();)
							{
								final Quest Q=q.nextElement();
								if((Q!=null)
								&&(Q.running())
								&&(Q.isObjectInUse(itarget)))
									mob.tell(L("\n\r^xIn use by quest:^.^N ^w@x1^?",Q.name()));
							}
							CMLib.genEd().genMiscSet(mob, itarget, -950);
							return true;
						}
						else
						{
							itarget = CMLib.map().findArea(mobName);
							if(itarget != null)
							{
								final Set<Area> alsoUpdateAreas=new HashSet<Area>();
								CMLib.genEd().modifyArea(mob, (Area)itarget, alsoUpdateAreas, -950);
								return true;
							}
							else
							{
								try
								{
									final List<Room> rooms=CMLib.hunt().findRooms(mob.location().getArea().getProperMap(), mob,mobName,true,100);
									if(rooms.size()==0)
										rooms.addAll(CMLib.hunt().findRooms(mob.location().getArea().getProperMap(), mob,mobName,false,100));
									if(rooms.size()==0)
										rooms.addAll(CMLib.hunt().findRooms(CMLib.map().rooms(), mob,mobName,true,100));
									if(rooms.size()==0)
										rooms.addAll(CMLib.hunt().findRooms(CMLib.map().rooms(), mob,mobName,false,100));
									for(final Room room : rooms)
									{
										final Room R=CMLib.map().roomLocation(room);
										if(CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.STAT))
										{
											itarget=room;
											break;
										}
									}
								}
								catch (final NoSuchElementException nse)
								{
								}
								if(itarget != null)
								{
									CMLib.genEd().modifyRoom(mob, (Room)itarget, -950);
									return true;
								}
								else
								{
									mob.tell(L("You can't stat mob/player/item/exit/clan/room/whatever '@x1'!",mobName));
									return false;
								}
							}
						}
					}
				}
			}

			for(int i=0;i<commands.size()-1;i++)
			{
				if(commands.get(i).toString().toUpperCase().equals("MAX"))
				{
					commands.remove(i);
					commands.set(i,"MAX"+commands.get(i).toString());
				}
			}
			for(int i=0;i<commands.size()-1;i++)
			{
				if(commands.get(i).toString().toUpperCase().equals("BASE"))
				{
					commands.remove(i);
					commands.set(i,"BASE"+commands.get(i).toString());
				}
			}
			for(int i=0;i<commands.size()-1;i++)
				commands.set(i,CMStrings.replaceAll(commands.get(i).toString()," ",""));

			for(int i=0;i<commands.size();i++)
			{
				final String thisStat=commands.get(i).toString().toUpperCase().trim();
				if(allowedCharStats.size()>0)
				{
					if(!allowedCharStats.contains(thisStat))
					{
						str.append(" *UNKNOWN:"+thisStat+"* ");
						continue;
					}
				}
				boolean found=false;
				if(thisStat.equals("MAXHUNGER"))
				{
					str.append(M.maxState().maxHunger(M.baseWeight())).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("MAXTHIRST"))
				{
					str.append(M.maxState().maxThirst(M.baseWeight())).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("XP"))
				{
					str.append(M.getExperience()).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("ATTRIBUTES"))
				{
					final CharStats CT=mob.charStats();
					for(final int stat : CharStats.CODES.BASECODES())
						str.append(CMStrings.capitalizeAndLower(CharStats.CODES.NAME(stat))+"("+CT.getStat(stat)).append(") ");
					found=true;
				}
				else
				if(thisStat.equals("RESISTS"))
				{
					final CharStats CT=mob.charStats();
					for(final int stat : CharStats.CODES.SAVING_THROWS())
						str.append(CMStrings.capitalizeAndLower(CharStats.CODES.NAME(stat))+"("+CT.getStat(stat)).append(") ");
					found=true;
				}
				else
				if(thisStat.equals("HEALTH"))
				{
					for(final String stat : M.curState().getStatCodes())
						str.append(CMStrings.capitalizeAndLower(stat)).append("(").append(M.curState().getStat(stat)).append(") ");
					found=true;
				}
				else
				if(thisStat.equals("TRAINS"))
				{
					str.append(M.getTrains()).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("PRACTICES"))
				{
					str.append(M.getPractices()).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("ATTACK"))
				{
					str.append(CMLib.combat().fightingProwessStr(M)).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("ARMOR"))
				{
					str.append(CMLib.combat().armorStr(M)).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("DAMAGE"))
				{
					M.tell(""+M.phyStats().damage());
					str.append(CMLib.combat().damageProwessStr(M)).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("QUESTPOINTS"))
				{
					str.append(M.getQuestPoint()).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("XPTNL"))
				{
					str.append(M.getExpNeededLevel()).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("XPFNL"))
				{
					str.append(M.getExpNextLevel()).append(" ");
					found=true;
				}
				else
				if(thisStat.equals("XPSTATS"))
				{
					str.append(M.getExpPrevLevel()).append(" < ");
					str.append(M.getExperience()).append(" > ");
					str.append(M.getExpNextLevel()).append(" = ");
					str.append(M.getExpNeededLevel()).append(" (");
					str.append(M.getExpNeededDelevel()).append(")");
					found=true;
				}
				else
				if(thisStat.equals("MAXHUNGER"))
				{
					str.append(M.maxState().maxHunger(M.baseWeight()));
					found=true;
				}
				if(!found)
				{
					for(final String stat : M.curState().getStatCodes())
					{
						if(stat.equals(thisStat))
						{
							str.append(M.curState().getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if((!found)&&(thisStat.equals("STINK"))&&(M.playerStats()!=null))
				{
					str.append(CMath.toPct(M.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT)).append(" ");
					found=true;
				}
				if((!found)&&(thisStat.startsWith("MAX")))
				{
					for(final String stat : M.maxState().getStatCodes())
					{
						if(stat.equals(thisStat.substring(3)))
						{
							str.append(M.maxState().getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if((!found)&&(thisStat.startsWith("BASE")))
				{
					for(final String stat : M.baseCharStats().getStatCodes())
					{
						if(stat.equals(thisStat.substring(4)))
						{
							final CharStats base=(CharStats)M.baseCharStats().copyOf();
							//M.baseCharStats().getMyRace().affectCharStats(M, base);
							str.append(base.getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if(!found)
				{
					for(final String stat : M.charStats().getStatCodes())
					{
						if(stat.equals(thisStat))
						{
							if(stat.startsWith("MAX"))
								str.append(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+CMath.s_int(M.charStats().getStat(stat))).append(" ");
							else
								str.append(M.charStats().getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if(!found)
				{
					for(final String stat : M.phyStats().getStatCodes())
					{
						if(stat.equals(thisStat))
						{
							if(stat.equals("SENSES"))
								str.append(doSenses(CMath.s_int(M.phyStats().getStat(stat))));
							else
							if(stat.equals("DISPOSITION"))
								str.append(doDisposition(CMath.s_int(M.phyStats().getStat(stat))));
							else
								str.append(M.phyStats().getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if(!found)
				{
					for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
					{
						final Faction F=f.nextElement();
						if((F!=null)
						&&(F.showInScore())
						&&(thisStat.equals(F.name().toUpperCase().replace(' ','_'))))
						{
							str.append(M.fetchFaction(F.factionID())).append(" ");
							found=true;
						}
					}
				}
				if(!found)
				{
					if(CMLib.coffeeMaker().isAnyGenStat(M, thisStat))
					{
						str.append(CMLib.coffeeMaker().getAnyGenStat(M, thisStat)).append(" ");
						found=true;
					}
				}
				if(!found)
				{
					for(final String stat : M.curState().getStatCodes())
					{
						if(stat.startsWith(thisStat))
						{
							str.append(M.curState().getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if((!found)&&(thisStat.startsWith("MAX")))
				{
					for(final String stat : M.maxState().getStatCodes())
					{
						if(stat.startsWith(thisStat.substring(3)))
						{
							str.append(M.maxState().getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if((!found)&&(thisStat.startsWith("BASE")))
				{
					for(final String stat : M.maxState().getStatCodes())
					{
						if(stat.startsWith(thisStat.substring(4)))
						{
							final CharStats base=(CharStats)M.baseCharStats().copyOf();
							//M.baseCharStats().getMyRace().affectCharStats(M, base);
							str.append(base.getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if(!found)
				{
					for(final String stat : M.charStats().getStatCodes())
					{
						if(stat.startsWith(thisStat))
						{
							if(stat.startsWith("MAX"))
								str.append(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+CMath.s_int(M.charStats().getStat(stat))).append(" ");
							else
								str.append(M.charStats().getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if(!found)
				{
					for(final String stat : M.phyStats().getStatCodes())
					{
						if(stat.startsWith(thisStat))
						{
							if(stat.equals("SENSES"))
								str.append(doSenses(CMath.s_int(M.phyStats().getStat(stat))));
							else
							if(stat.equals("DISPOSITION"))
								str.append(doDisposition(CMath.s_int(M.phyStats().getStat(stat))));
							else
								str.append(M.phyStats().getStat(stat)).append(" ");
							found=true;
							break;
						}
					}
				}
				if(!found)
				{
					for(final Enumeration<Faction> f=CMLib.factions().factions();f.hasMoreElements();)
					{
						final Faction F=f.nextElement();
						if((F!=null)
						&&(F.showInScore())
						&&(F.name().toUpperCase().replace(' ','_').startsWith(thisStat)))
						{
							str.append(M.fetchFaction(F.factionID())).append(" ");
							found=true;
						}
					}
				}
				if(!found)
					str.append(" *UNKNOWN:"+thisStat+"* ");
			}
		}
		if(!mob.isMonster())
			mob.session().wraplessPrintln(str.toString());
		return false;
	}

	public void recoverMOB(final MOB M)
	{
		M.recoverCharStats();
		M.recoverPhyStats();
		M.recoverMaxState();
		M.resetToMaxState();
	}

	public void testMOB(final MOB target,final MOB M, final Environmental test)
	{
		test.affectCharStats(target,M.charStats());
		test.affectPhyStats(target,M.phyStats());
		test.affectCharState(target,M.maxState());
	}

	public void reportOnDiffMOB(final String name, final int diff, final StringBuilder str)
	{
		if(diff>0)
			str.append("^C"+CMStrings.padRight(name,40)+": ^W+"+diff+"\n\r");
		else
		if(diff<0)
			str.append("^C"+CMStrings.padRight(name,40)+": ^W"+diff+"\n\r");
	}

	public void reportOnDiffMOB(final Environmental test, final int diff, final StringBuilder str)
	{
		reportOnDiffMOB(test.Name(),diff,str);
	}

	private final static Class<?>[][] internalParameters=new Class<?>[][]{{String.class,MOB.class}};

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		final String statName=(String)args[0];
		final MOB M=(MOB)args[1];
		final List<String> cmds=new XVector<String>("STAT", statName.toUpperCase().trim(), M.Name());
		return getPrivilegedStat(mob, M, cmds, true);
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return true;
	}
}
