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
	public long lastStart=0;
	public long lastStop=0;
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
		this.stop();
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
				boolean ok=C.clientObject.tick(C.clientObject,C.tickID);
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
		lastStart=System.currentTimeMillis();
		while(true)
		{
			try
			{
				lastStop=System.currentTimeMillis();
				milliTotal+=(lastStop-lastStart);
				tickTotal++;
				awake=false;
				Thread.sleep(Host.TICK_TIME);
				awake=true;
				lastStart=System.currentTimeMillis();
				lastClient=null;
				if(ExternalPlay.getSystemStarted())
				{
					int i=0;
					while(i<tickers.size())
					{
						TockClient client=(TockClient)tickers.elementAt(i);
						lastClient=client;
						if((client.lastStart!=0)&&(client.lastStop!=0))
						{
							client.milliTotal+=(client.lastStop-client.lastStart);
							client.tickTotal++;
						}
						client.lastStart=System.currentTimeMillis();
						if(!tickTicker(client,tickers))
							i++;
						client.lastStop=System.currentTimeMillis();
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
	}
}
