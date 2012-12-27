package com.planet_ink.coffee_mud.core.threads;

import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;

/*
Portions Copyright 2002 Jeff Kamenek
Portions Copyright 2002-2012 Bo Zimmerman

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
public class CMSupportThread extends Thread implements CMRunnable
{
	private volatile boolean  	started=false;
	private volatile boolean  	shutDown=false;
	private volatile long 	 	lastStart=0;
	private volatile long 	 	lastStop=0;
	private volatile long 	 	milliTotal=0;
	private volatile long 	 	tickTotal=0;
	private String			 	status="unknown";
	private final boolean		debugging;
	private volatile boolean  	checkDBHealth=true;
	private final long 	 		sleepTime;
	private final Runnable 		engine;

	private final CMSecurity.DisFlag  disableFlag;
	
	public CMSupportThread(String name, long sleep, Runnable engine, boolean debugging, CMSecurity.DisFlag disableFlag) {
		this.engine=engine;
		sleepTime=sleep;
		this.debugging=debugging;
		this.disableFlag=disableFlag;
		setName(name);
		setDaemon(true);
	}
	
	public void setStatus(String s)
	{
		status=s;
		if(debugging) Log.debugOut(getName(),getStatus());
	}
	
	public boolean isStarted()
	{
		return started;
	}
	
	public String getStatus()
	{
		if(status.equals("{LASTSTART}"))
		{
			return "started "+CMLib.time().date2BriefString(lastStart);
		}
		return status;
	}

	public void disableDBCheck()
	{
		checkDBHealth=false;
	}
	
	public boolean shutdown() {
		shutDown=true;
		CMLib.killThread(this,500,30);
		return true;
	}
	
	public void debugDumpStack(final String ID, Thread theThread)
	{
		// I wish Java had compiler directives.  Would be great to un-comment this for 1.5 JVMs
		java.lang.StackTraceElement[] s=theThread.getStackTrace();
		StringBuffer dump = new StringBuffer("");
		for(int i=0;i<s.length;i++)
			dump.append("\n   "+s[i].getClassName()+": "+s[i].getMethodName()+"("+s[i].getFileName()+": "+s[i].getLineNumber()+")");
		Log.debugOut(ID,dump.toString());
	}
	
	public void run()
	{
		try {
			lastStart=System.currentTimeMillis();
			if(started)
			{
				Log.errOut(getName(),"DUPLICATE "+getName().toUpperCase()+" RUNNING!!");
				return;
			}
			started=true;
			shutDown=false;

			while(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
				try{Thread.sleep(1000);}catch(Exception e){}
			lastStart=System.currentTimeMillis();
			setStatus("sleeping");
			while(started)
			{
				try
				{
					while(CMLib.threads().isAllSuspended())
						try{
							Thread.sleep(2000);
						}catch(InterruptedException e){
							if(CMProps.getBoolVar(CMProps.SYSTEMB_MUDSHUTTINGDOWN))
								throw e;
						}
					try
					{
						if(!CMSecurity.isDisabled(disableFlag))
						{
							if(checkDBHealth)
							{
								setStatus("checking database health");
								String ok=CMLib.database().errorStatus();
								setStatus("sleeping");
								if((ok.length()!=0)&&(!ok.startsWith("OK")))
								{
									Log.errOut(getName(),"DB: "+ok);
									Thread.sleep(10000);
									continue;
								}
							}
							lastStop=System.currentTimeMillis();
							milliTotal+=(lastStop-lastStart);
							tickTotal++;
							//setStatus("stopped at "+lastStop+", prev was "+(lastStop-lastStart)+"ms");
							Thread.sleep(sleepTime);
							lastStart=System.currentTimeMillis();
							setStatus("LASTSTART");
							engine.run();
						}
						else
						{
							Thread.sleep(sleepTime);
						}
					}
					finally
					{
						setStatus("sleeping");
					}
				}
				catch(InterruptedException ioe)
				{
					Log.sysOut(getName(),"Interrupted!");
					if(shutDown)
					{
						shutDown=false;
						started=false;
						break;
					}
				}
				catch(Exception e)
				{
					Log.errOut(getName(),e);
				}
			}
		} finally {
			started=false;
		}
		Log.sysOut(getName(),"Shutdown complete.");
	}

	@Override
	public long activeTimeMillis() 
	{
		final long ls = lastStart;
		final long le = lastStop;
		final long mt = milliTotal;
		if(le > ls)
			return mt;
		return mt + (System.currentTimeMillis()-ls);
	}
	
	public long getTotalTicks() { return tickTotal; }
}
