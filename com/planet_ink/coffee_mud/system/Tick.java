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
	
	private Vector tickers=new Vector();
	
	public Enumeration tickers(){return tickers.elements();}
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
				Thread.sleep(Host.TICK_TIME);
				awake=true;
				lastStart=System.currentTimeMillis();
				lastClient=null;
				if(ExternalPlay.getSystemStarted())
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
				Log.sysOut("Tick","Interrupted!");
			}
			if(tickers.size()==0)
			{
				ServiceEngine.delTickGroup(this);
				break;
			}
		}
	}
}
