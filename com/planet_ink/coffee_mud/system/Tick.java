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
	public Calendar lastAwoke=Calendar.getInstance();
	public TockClient lastClient=null;

	public void shutdown()
	{
		tickers.removeAllElements();
		this.interrupt();
	}

	public void run()
	{
		while(true)
		{
			try
			{
				awake=false;
				Thread.sleep(Host.TICK_TIME);
				awake=true;
				lastAwoke=Calendar.getInstance();
				lastClient=null;

				int i=0;
				while(i<tickers.size())
				{
					TockClient client=(TockClient)tickers.elementAt(i);
					lastClient=client;
					if((--client.tickDown)<1)
					{
						client.tickDown=client.reTickDown;
						try
						{
							boolean ok=client.clientObject.tick(client.tickID);
							if(!ok)
							{
								tickers.removeElement(client);
							}
							else
								i++;
						}
						catch(Exception t)
						{
							Log.errOut("Tick",t);
							i++;
						}
					}
					else
						i++;
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
