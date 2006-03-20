package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.sql.*;
import java.net.*;
import java.util.*;


/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class Tick extends Thread implements TickableGroup, Cloneable
{
	public long lastStart=0;
	public long lastStop=0;
	public long milliTotal=0;
	public long tickTotal=0;
	public ThreadEngine myEngine=null;
	public boolean solitaryTicker=false;
	private static int tickObjReference=0;
    private int tickObjectCounter=0;
    public long TICK_TIME=Tickable.TIME_TICK;
	protected Vector tickers=new Vector();
	protected Hashtable set=new Hashtable(TickableGroup.MAX_TICK_CLIENTS);
	
	public Tick(ThreadEngine theEngine, long sleep)
	{
		super("Tick."+(tickObjReference+1));
        tickObjectCounter=tickObjReference++;
		myEngine=theEngine;
        TICK_TIME=sleep;
	}

	public Tick copyOf()
	{
		try{
			Tick T=(Tick)this.clone();
			T.tickers=(Vector)tickers.clone();
			T.set=(Hashtable)set.clone();
			return T;
		}
		catch(Exception e){}
		return this;
	}
	
	public Iterator tickers(){return ((Vector)tickers.clone()).iterator();}
	public int numTickers(){return tickers.size();}
	public TockClient fetchTicker(int i){
		try{
			return (TockClient)tickers.elementAt(i);
		}catch(Exception e){}
		return null;
	}
	
	public Iterator getTickSet(Tickable T, int tickID)
	{
		Vector V=(Vector)set.get(T);
		if(V==null) return null;
		V=(Vector)V.clone();
		if(tickID<0) return V.iterator();
		for(int v=V.size()-1;v>=0;v--)
			if(((TockClient)V.elementAt(v)).tickID!=tickID)
				V.removeElementAt(v);
		if(V.size()==0) return null;
		return V.iterator();
	}
	
	public boolean contains(Tickable T, int tickID)
	{
		Vector V=(Vector)set.get(T);
		if(V==null) return false;
		if(tickID<0) return true;
		V=(Vector)V.clone();
		TockClient C=null;
		for(int v=0;v<V.size();v++)
		{
			C=(TockClient)V.elementAt(v);
			if(C.tickID==tickID)
				return true;
		}
		return false;
	}
    
    public int getCounter(){return tickObjectCounter;}
    
	public void delTicker(TockClient C)
	{
		synchronized(tickers)
		{
			tickers.removeElement(C);
			Vector V=(Vector)set.get(C.clientObject);
			if(V!=null)
			{
				V.remove(C);
				if(V.size()==0) 
					set.remove(C.clientObject);
			}
		}
	}
	public void addTicker(TockClient C)
	{
		synchronized(tickers)
		{
			tickers.addElement(C);
			Vector V=(Vector)set.get(C.clientObject);
			if(V==null)
			{
				V=new Vector();
				set.put(C.clientObject,V);
			}
			else
			if(V.contains(C))
				return;
			V.addElement(C);
		}
	}


	public Tick(long sleep)
	{
        super("Tick."+(tickObjReference+1));
        tickObjectCounter=tickObjReference++;
        TICK_TIME=sleep;
		this.start();
	}

	public Tick(String a_name, long sleep)
	{
        super("Tick."+ a_name + "." +(tickObjReference+1));
        tickObjectCounter=tickObjReference++;
        TICK_TIME=sleep;
		this.start();
	}

	public boolean awake=false;
	public TockClient lastClient=null;
    public Tickable lastTicked()
    {
        return lastClient!=null?lastClient.clientObject:null;
    }

	public void shutdown()
	{
		tickers.removeAllElements();
		set.clear();
		this.interrupt();
	}

	public static boolean tickTicker(TockClient C, boolean allSuspended)
	{
		if((C.suspended)||(allSuspended))
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
				Thread.sleep(TICK_TIME);
				awake=true;
				lastStart=System.currentTimeMillis();
				lastClient=null;
				if((CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
                &&(!CMLib.threads().isAllSuspended()))
				{
                    
					for(Iterator i=tickers();i.hasNext();)
					{
						TockClient client=(TockClient)i.next();
						lastClient=client;
						if((client.lastStart!=0)&&(client.lastStop!=0))
						{
							client.milliTotal+=(client.lastStop-client.lastStart);
							client.tickTotal++;
						}
						client.lastStart=System.currentTimeMillis();
						if(tickTicker(client,CMLib.threads().isAllSuspended()))
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
				if(CMLib.threads() instanceof ServiceEngine)
					((ServiceEngine)CMLib.threads()).delTickGroup(this);
				break;
			}
		}
	}
}
