package com.planet_ink.coffee_mud.Libraries;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.*;

/*
   Copyright 2005-2025 Bo Zimmerman

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
public class StdLibrary implements CMLibrary, Tickable
{
	@Override
	public String ID()
	{
		return "StdLibrary";
	}

	protected String	name	= ID();

	@Override
	public String name()
	{
		return name;
	}

	protected volatile int	tickStatus		= Tickable.STATUS_NOT;
	protected TickClient	serviceClient	= null;
	protected boolean		isDebugging		= false;

	protected static class CallerFinder extends SecurityManager
	{
		public Class<?> getCaller()
		{
			return super.getClassContext()[2];
		}
	}
	protected static final CallerFinder FINDER = new CallerFinder();

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().getDeclaredConstructor().newInstance();
		}
		catch(final Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdLibrary();
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject) this.clone();
		}
		catch (final Exception e)
		{
			return newInstance();
		}
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(FINDER.getCaller(), str, xs);
	}

	@Override
	public String L(final Class<?> clazz, final String str, final String ... xs)
	{
		return CMLib.lang().fullSessionTranslation(clazz, str, xs);
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public boolean activate()
	{
		name=ID()+Thread.currentThread().getThreadGroup().getName().charAt(0);
		return true;
	}

	@Override
	public void propertiesLoaded()
	{
	}

	@Override
	public boolean shutdown()
	{
		return true;
	}

	@Override
	public TickClient getServiceClient()
	{
		return serviceClient;
	}

	public void setThreadStatus(final TickClient C, final String msg)
	{
		if(C!=null)
		{
			C.setStatus(msg);
			if(isDebugging)
				Log.debugOut(C.getName(),msg);
		}
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return false;
	}

	protected boolean checkDatabase()
	{
		setThreadStatus(serviceClient,"pinging connections");
		CMLib.database().pingAllConnections();
		setThreadStatus(serviceClient,"checking database health");
		String ok=CMLib.database().errorStatus();
		if((ok.length()!=0)&&(!ok.startsWith("OK")))
		{
			Log.errOut("DB: "+ok+" for "+serviceClient.getName());
			CMLib.database().pingAllConnections();
			ok=CMLib.database().errorStatus();
			if((ok.length()!=0)&&(!ok.startsWith("OK")))
			{
				Log.errOut("DB: "+ok+": "+serviceClient.getName()+" skipped.");
				return false;
			}
		}
		return true;
	}
}
