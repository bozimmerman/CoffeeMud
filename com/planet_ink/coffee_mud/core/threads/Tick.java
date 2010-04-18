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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
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
    private long SUBTRACT_TIME=0;
	protected TreeSet<TockClient> tickers=new TreeSet<TockClient>();
	protected boolean shutdown=false;
	
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
			T.tickers=(TreeSet<TockClient>)tickers.clone();
			return T;
		}
		catch(Exception e){}
		return this;
	}
	
	public TockClient fetchTicker(int i)
	{
		synchronized(tickers)
		{
			int x=0;
			for(TockClient C : tickers)
				if(i==(x++))
					return C;
			return null;
		}
	}
	
	public Iterator<TockClient> tickers(){return ((TreeSet<TockClient>)tickers.clone()).iterator();}
	public int numTickers(){return tickers.size();}
	public Iterator<TockClient> getTickSet(Tickable T, int tickID)
	{
		synchronized(tickers)
		{
			LinkedList<TockClient> subSet = new LinkedList<TockClient>();
			if(tickID >= 0)
				subSet.addAll(tickers.subSet(new TockClient(T,0,-1), true, new TockClient(T,0,Integer.MAX_VALUE), true));
			else
				subSet.addAll(tickers.subSet(new TockClient(T,0,tickID), true, new TockClient(T,0,tickID), true));
			return subSet.iterator();
		}
	}
	
	public Iterator<TockClient> getLocalItems(int itemTypes, Room R)
	{
		synchronized(tickers)
		{
			LinkedList<TockClient> localItems=null;
			TockClient C;
			for(Iterator e=tickers.iterator();e.hasNext();)
			{
				C=(TockClient)e.next();
				switch(itemTypes)
				{
				case 0:
					if(C.clientObject instanceof MOB)
					{
						if(((MOB)C.clientObject).getStartRoom()==R)
                        {
                            if(localItems==null) localItems=new LinkedList<TockClient>();
							localItems.add(C);
                        }
					}
					else
					if((C.clientObject instanceof ItemTicker)
					&&((((ItemTicker)C.clientObject).properLocation()==R)))
                    {
                        if(localItems==null) localItems=new LinkedList<TockClient>();
						localItems.add(C);
                    }
					break;
				case 1:
					if((C.clientObject instanceof ItemTicker)
					&&((((ItemTicker)C.clientObject).properLocation()==R)))
                    {
                        if(localItems==null) localItems=new LinkedList<TockClient>();
						localItems.add(C);
                    }
					break;
				case 2:
					if((C.clientObject instanceof MOB)
					&&(((MOB)C.clientObject).getStartRoom()==R))
                    {
                        if(localItems==null) localItems=new LinkedList<TockClient>();
						localItems.add(C);
                    }
					break;
				}
			}
			if(localItems == null) return null;
			return localItems.iterator();
		}
	}
	
	
	public boolean contains(Tickable T, int tickID)
	{
		synchronized(tickers)
		{
			if(tickID >= 0)
				return tickers.contains(new TockClient(T,0,tickID));
			return tickers.subSet(new TockClient(T,0,-1), true, new TockClient(T,0,Integer.MAX_VALUE), true).size()>0;
		}
	}
    
    public int getCounter(){return tickObjectCounter;}
    
	public void delTicker(TockClient C)
	{
		synchronized(tickers)
		{
			tickers.remove(C);
		}
	}
	public void addTicker(TockClient C)
	{
		synchronized(tickers)
		{
    		if((C==null)||(C.clientObject==null)) return;
    		tickers.add(C);
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
        setDaemon(true);
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

    public String getStatus() {
    	Tickable lastTicked = lastTicked();
    	if((lastTicked==null)||(myEngine==null))
    		return "Asleep or Shutdown";
    	if(!awake)
    		return "Sleeping";
    	return "Ticking: "+lastTicked.ID()+": "+lastTicked.name()+": "+((myEngine!=null)?myEngine.getTickStatusSummary(lastTicked):"null");
    }
    
	public void shutdown()
	{
		tickers.clear();
		CMLib.killThread(this,10,1);
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
				{
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
		shutdown=false;
		while(true)
		{
			try
			{
				lastStop=System.currentTimeMillis();
				SUBTRACT_TIME=(lastStop-lastStart);
				milliTotal+=(lastStop-lastStart);
				tickTotal++;
				awake=false;
                long timeToSleep=TICK_TIME;
				if(SUBTRACT_TIME<timeToSleep)
				{
                    timeToSleep-=SUBTRACT_TIME;
                    SUBTRACT_TIME=0;
				}
				if(timeToSleep>0)
				    Thread.sleep(timeToSleep);
				awake=true;
				lastStart=System.currentTimeMillis();
				lastClient=null;
				if((CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
                &&(!CMLib.threads().isAllSuspended()))
				{
                    TockClient client=null;
					for(Iterator<TockClient> i=tickers.iterator();i.hasNext();)
					{
						client=i.next();
						lastClient=client;
						if((client.lastStart!=0)&&(client.lastStop!=0))
						{
							client.milliTotal+=(client.lastStop-client.lastStart);
							client.tickTotal++;
						}
						client.lastStart=System.currentTimeMillis();
						if(tickTicker(client,CMLib.threads().isAllSuspended()))
                        {
                            delTicker(client);
                        }
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
		shutdown=true;
	}
}
