package com.planet_ink.coffee_mud.system;

import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class Tick extends Thread
{
	public Calendar lastStart=null;
	public Calendar lastStop=null;
	public long milliTotal=0;
	public long tickTotal=0;
	
	public Vector tickers=new Vector();

	private static int tickObjCounter=0;
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

	public static boolean tickTicker(TockClient C, Vector tickers)
	{
		if(C.suspended) 
			return false;
		
		if((--C.tickDown)<1)
		{
			C.tickDown=C.reTickDown;
			try
			{
				boolean ok=C.clientObject.tick(C.tickID);
				if(!ok)
					synchronized(tickers)
					{
						tickers.removeElement(C);
						return true;
					}
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
		lastStart=Calendar.getInstance();
		while(true)
		{
			try
			{
				lastStop=Calendar.getInstance();
				milliTotal+=(lastStop.getTimeInMillis()-lastStart.getTimeInMillis());
				tickTotal++;
				awake=false;
				Thread.sleep(Host.TICK_TIME);
				awake=true;
				lastStart=Calendar.getInstance();
				lastClient=null;
				if(ExternalPlay.getSystemStarted())
				{
					int i=0;
					while(i<tickers.size())
					{
						TockClient client=(TockClient)tickers.elementAt(i);
						lastClient=client;
						if((client.lastStart!=null)&&(client.lastStop!=null))
						{
							client.milliTotal+=(client.lastStop.getTimeInMillis()-client.lastStart.getTimeInMillis());
							client.tickTotal++;
						}
						client.lastStart=Calendar.getInstance();
						if(!tickTicker(client,tickers))
							i++;
						client.lastStop=Calendar.getInstance();
					}
				}
			}
			catch(InterruptedException ioe)
			{
				Log.sysOut("Tick","Interrupted!");
			}
			if(tickers.size()==0)
			{
				ServiceEngine.tickGroup.removeElement(this);
				break;
			}
		}
		Log.sysOut("Tick","Shutdown.");
	}
}
