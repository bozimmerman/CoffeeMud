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
   Copyright 2000-2013 Bo Zimmerman

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
	public String ID(){return "CoffeeTables";}
	public CoffeeTableRow todays=null;
	
	private TickClient serviceClient=null;
	public TickClient getServiceClient() { return serviceClient;}
	
	public void update()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.STATS))
			return;
		if(todays!=null)
			CMLib.database().DBUpdateStat(todays.startTime(),todays.data());
	}
	
	public void bump(CMObject E, int type)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.STATS))
			return;
		if(todays==null)
		{
			Calendar S=Calendar.getInstance();
			S.set(Calendar.HOUR_OF_DAY,0);
			S.set(Calendar.MINUTE,0);
			S.set(Calendar.SECOND,0);
			S.set(Calendar.MILLISECOND,0);
			todays=(CoffeeTableRow)CMLib.database().DBReadStat(S.getTimeInMillis());
			if(todays==null)
			{
				synchronized(this)
				{
					if(todays==null)
					{
						Calendar C=Calendar.getInstance();
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
		long now=System.currentTimeMillis();
		if((now>todays.endTime())
		&&(!CMLib.time().date2MonthDateString(now, true).equals(CMLib.time().date2MonthDateString(todays.endTime(), true))))
		{
			synchronized(this)
			{
				if((now>todays.endTime())
				&&(!CMLib.time().date2MonthDateString(now, true).equals(CMLib.time().date2MonthDateString(todays.endTime(), true))))
				{
					CMLib.database().DBUpdateStat(todays.startTime(),todays.data());
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
					todays=(CoffeeTableRow)CMClass.getCommon("DefaultCoffeeTableRow");
					todays.setStartTime(S.getTimeInMillis());
					todays.setEndTime(C.getTimeInMillis());
					CoffeeTableRow testRow=(CoffeeTableRow)CMLib.database().DBReadStat(todays.startTime());
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
	
	public boolean activate() 
	{
		if(serviceClient==null)
			serviceClient=CMLib.threads().startTickDown(new Tickable(){
				private long tickStatus=Tickable.STATUS_NOT;
				@Override public String ID() { return "THStats"+Thread.currentThread().getThreadGroup().getName().charAt(0); }
				@Override public CMObject newInstance() { return this; }
				@Override public CMObject copyOf() { return this; }
				@Override public void initializeClass() { }
				@Override public int compareTo(CMObject o) { return (o==this)?0:1; }
				@Override public String name() { return ID(); }
				@Override public long getTickStatus() { return tickStatus; }
				@Override public boolean tick(Tickable ticking, int tickID) {
					if((!CMSecurity.isDisabled(CMSecurity.DisFlag.SAVETHREAD))
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.STATSTHREAD)))
					{
						tickStatus=Tickable.STATUS_ALIVE;
						isDebugging=CMSecurity.isDebugging(DbgFlag.STATSTHREAD);
						setThreadStatus(serviceClient,"checking database health");
						String ok=CMLib.database().errorStatus();
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
						setThreadStatus(serviceClient,"sleeping");
					}
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}
			}, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_SAVETHREAD_SLEEP, 1);
		return true;
	}
	
	public boolean shutdown() 
	{
		if((serviceClient!=null)&&(serviceClient.getClientObject()!=null))
		{
			CMLib.threads().deleteTick(serviceClient.getClientObject(), Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}
}
