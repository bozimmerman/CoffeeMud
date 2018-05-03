package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class CoffeeTables extends StdLibrary implements StatisticsLibrary
{
	@Override
	public String ID()
	{
		return "CoffeeTables";
	}

	public CoffeeTableRow todays=null;

	@Override
	public void update()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.STATS))
			return;
		if(todays!=null)
			CMLib.database().DBUpdateStat(todays.startTime(),todays.data());
	}

	@Override
	public void bump(CMObject E, int type)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.STATS))
			return;
		if(todays==null)
		{
			final Calendar S=Calendar.getInstance();
			S.set(Calendar.HOUR_OF_DAY,0);
			S.set(Calendar.MINUTE,0);
			S.set(Calendar.SECOND,0);
			S.set(Calendar.MILLISECOND,0);
			todays=CMLib.database().DBReadStat(S.getTimeInMillis());
			if(todays==null)
			{
				synchronized(this)
				{
					if(todays==null)
					{
						final Calendar C=Calendar.getInstance();
						C.set(Calendar.HOUR_OF_DAY,23);
						C.set(Calendar.MINUTE,59);
						C.set(Calendar.SECOND,59);
						C.set(Calendar.MILLISECOND,999);
						todays=(CoffeeTableRow)CMClass.getCommon("DefaultCoffeeTableRow");
						todays.setStartTime(S.getTimeInMillis());
						todays.setEndTime(C.getTimeInMillis());
						CMLib.database().DBCreateStat(todays.startTime(),todays.endTime(),todays.data());
					}
				}
			}
			return;
		}
		final long now=System.currentTimeMillis();
		if((now>todays.endTime())
		&&(!CMLib.time().date2MonthDateString(now, true).equals(CMLib.time().date2MonthDateString(todays.endTime(), true))))
		{
			synchronized(this)
			{
				if((now>todays.endTime())
				&&(!CMLib.time().date2MonthDateString(now, true).equals(CMLib.time().date2MonthDateString(todays.endTime(), true))))
				{
					CMLib.database().DBUpdateStat(todays.startTime(),todays.data());
					final Calendar S=Calendar.getInstance();
					S.set(Calendar.HOUR_OF_DAY,0);
					S.set(Calendar.MINUTE,0);
					S.set(Calendar.SECOND,0);
					S.set(Calendar.MILLISECOND,0);
					final Calendar C=Calendar.getInstance();
					C.set(Calendar.HOUR_OF_DAY,23);
					C.set(Calendar.MINUTE,59);
					C.set(Calendar.SECOND,59);
					C.set(Calendar.MILLISECOND,999);
					todays=(CoffeeTableRow)CMClass.getCommon("DefaultCoffeeTableRow");
					todays.setStartTime(S.getTimeInMillis());
					todays.setEndTime(C.getTimeInMillis());
					final CoffeeTableRow testRow=CMLib.database().DBReadStat(todays.startTime());
					if(testRow!=null)
						todays=testRow;
					else
					if(!CMLib.database().DBCreateStat(todays.startTime(),todays.endTime(),todays.data()))
					{
						Log.errOut("CoffeeTables","Unable to manage daily-stat transition");
					}
				}
			}
		}
		todays.bumpVal(E,type);
	}

	@Override
	public boolean activate()
	{
		if(serviceClient==null)
		{
			name="THStats"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_SAVETHREAD_SLEEP, 1);
		}
		return true;
	}

	@Override 
	public boolean tick(Tickable ticking, int tickID)
	{
		try
		{
			if((!CMSecurity.isDisabled(CMSecurity.DisFlag.SAVETHREAD))
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.STATSTHREAD)))
			{
				tickStatus=Tickable.STATUS_ALIVE;
				isDebugging=CMSecurity.isDebugging(DbgFlag.STATSTHREAD);
				setThreadStatus(serviceClient,"checking database health");
				final String ok=CMLib.database().errorStatus();
				if((ok.length()!=0)&&(!ok.startsWith("OK")))
				{
					Log.errOut(serviceClient.getName(),"DB: "+ok);
					CMLib.s_sleep(100000);
				}
				else
				{
					CMLib.coffeeTables().bump(null,CoffeeTableRow.STAT_SPECIAL_NUMONLINE);
					CMLib.coffeeTables().update();
				}
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
			setThreadStatus(serviceClient,"sleeping");
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}
}
