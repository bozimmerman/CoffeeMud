package com.planet_ink.coffee_mud.system;

import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Tick extends Thread
{
	public long lastStart=0;
	public long lastStop=0;
	public long milliTotal=0;
	public long tickTotal=0;
	public ThreadEngine myEngine=null;
	public boolean solitaryTicker=false;
	private static int tickObjCounter=0;
	
	public Tick(ThreadEngine theEngine)
	{
		super("Tick."+tickObjCounter);
		tickObjCounter++;
		myEngine=theEngine;
	}
	
	private Vector tickers=new Vector();

	public Enumeration tickers(){return ((Vector)tickers.clone()).elements();}
	public int numTickers(){return tickers.size();}
	public TockClient fetchTicker(int i){
		try{
			return (TockClient)tickers.elementAt(i);
		}catch(Exception e){}
		return null;
	}
	public void delTicker(TockClient C)
	{
		synchronized(tickers)
		{
			tickers.removeElement(C);
		}
	}
	public void addTicker(TockClient C)
	{
		synchronized(tickers)
		{
			tickers.addElement(C);
		}
	}


	public Tick()
	{
		super("Tick."+tickObjCounter);
		++tickObjCounter;
		this.start();
	}

	public Tick(String a_name)
	{
		super("Tick." + a_name + "." + tickObjCounter);
		++tickObjCounter;
		this.start();
	}

	public boolean awake=false;
	public TockClient lastClient=null;

	public void shutdown()
	{
		tickers.removeAllElements();
		this.interrupt();
	}

	public static boolean tickTicker(TockClient C)
	{
		if(C.suspended)
			return false;

		if((--C.tickDown)<1)
		{
			C.tickDown=C.reTickDown;
			try
			{
				if(!C.clientObject.tick(C.clientObject,C.tickID))
					return true;
			}
			catch(Exception t)
			{
				Log.errOut("ServiceEngine",t);
			}
		}
		return false;
	}

	public void run()
	{
		lastStart=System.currentTimeMillis();
		while(true)
		{
			try
			{
				lastStop=System.currentTimeMillis();
				milliTotal+=(lastStop-lastStart);
				tickTotal++;
				awake=false;
				Thread.sleep(MudHost.TICK_TIME);
				awake=true;
				lastStart=System.currentTimeMillis();
				lastClient=null;
				if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
				{
					for(Enumeration e=tickers();e.hasMoreElements();)
					{
						TockClient client=(TockClient)e.nextElement();
						lastClient=client;
						if((client.lastStart!=0)&&(client.lastStop!=0))
						{
							client.milliTotal+=(client.lastStop-client.lastStart);
							client.tickTotal++;
						}
						client.lastStart=System.currentTimeMillis();
						if(tickTicker(client))
							delTicker(client);
						client.lastStop=System.currentTimeMillis();
					}
				}
			}
			catch(InterruptedException ioe)
			{
				// a perfectly good and normal thing
			}
			if(tickers.size()==0)
			{
				if(CMClass.ThreadEngine() instanceof ServiceEngine)
					((ServiceEngine)CMClass.ThreadEngine()).delTickGroup(this);
				break;
			}
		}
	}
}
