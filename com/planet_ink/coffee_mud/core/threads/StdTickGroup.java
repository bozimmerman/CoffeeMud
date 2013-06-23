package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.sql.*;
import java.net.*;
import java.util.*;


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
public class StdTickGroup implements TickableGroup, Cloneable
{
	private final ThreadEngine myEngine;
	private final long tickTime;
	private volatile long nextTickTime;
	private final int tickObjectCounter;
	
	private volatile long lastStart=0;
	private volatile long lastStop=0;
	private long milliTotal=0;
	private long tickTotal=0;
	private boolean solitaryTicker;
	private volatile TickClient lastClient=null;
	private final String name;
	private final String threadGroupName;
	private volatile Thread currentThread = null;

	private static volatile int tickObjReference=0;
	
	private final STreeSet<TickClient> tickers=new STreeSet<TickClient>();
	
	public StdTickGroup(long sleep, String threadGroupName, boolean isSolitary)
	{
		name = "Tick."+(tickObjReference+1);
		tickObjectCounter=tickObjReference++;
		tickTime=sleep;
		nextTickTime=System.currentTimeMillis() + tickTime;
		myEngine=null;
		solitaryTicker=isSolitary;
		this.threadGroupName=threadGroupName;
	}

	public StdTickGroup(String a_name, long sleep, String threadGroupName, boolean isSolitary)
	{
		name = "Tick."+ a_name + "." +(tickObjReference+1);
		tickObjectCounter=tickObjReference++;
		tickTime=sleep;
		nextTickTime=System.currentTimeMillis() + tickTime;
		myEngine=null;
		solitaryTicker=isSolitary;
		this.threadGroupName=threadGroupName;
	}
	
	public StdTickGroup(ThreadEngine theEngine, long sleep, String threadGroupName, boolean isSolitary)
	{
		name = "Tick."+(tickObjReference+1);
		tickObjectCounter=tickObjReference++;
		myEngine=theEngine;
		tickTime=sleep;
		nextTickTime=System.currentTimeMillis() + tickTime;
		solitaryTicker=isSolitary;
		this.threadGroupName=threadGroupName;
	}

	public StdTickGroup copyOf()
	{
		try
		{
			StdTickGroup T=(StdTickGroup)this.clone();
			T.tickers.clear();
			T.tickers.addAll(tickers);
			return T;
		}
		catch(Exception e){}
		return this;
	}
	
	public TickClient fetchTickerByIndex(int i)
	{
		int x=0;
		for(final TickClient C : tickers)
			if(i==(x++))
				return C;
		return null;
	}
	
	public Thread getCurrentThread() { return currentThread; }
	
	public String getThreadGroupName() { return threadGroupName; }
	
	public long getLastStartTime() { return lastStart;}
	
	public long getLastStopTime() { return lastStop; }
	
	public long getMilliTotal() { return milliTotal; }
	
	public long getTickTotal() { return tickTotal; }
	
	public boolean isSolitaryTicker() { return solitaryTicker; }
	
	public boolean isAwake() { return currentThread != null; }
	
	public Iterator<TickClient> tickers(){return tickers.iterator();}
	public int numTickers(){return tickers.size();}
	public Iterator<TickClient> getTickSet(final Tickable T, final int tickID)
	{
		final LinkedList<TickClient> subSet = new LinkedList<TickClient>();
		if(tickID < 0)
			subSet.addAll(tickers.subSet(new StdTickClient(T,0,0), true, new StdTickClient(T,0,Integer.MAX_VALUE), true));
		else
			subSet.addAll(tickers.subSet(new StdTickClient(T,0,tickID), true, new StdTickClient(T,0,tickID), true));
		return subSet.iterator();
	}
	
	public Iterator<TickClient> getLocalItems(int itemTypes, Room R)
	{
		LinkedList<TickClient> localItems=null;
		for(final Iterator<TickClient> e=tickers.iterator();e.hasNext();)
		{
			final TickClient C=e.next();
			switch(itemTypes)
			{
			case 0:
				if(C.getClientObject() instanceof MOB)
				{
					if(((MOB)C.getClientObject()).getStartRoom()==R)
					{
						if(localItems==null) localItems=new LinkedList<TickClient>();
						localItems.add(C);
					}
				}
				else
				if((C.getClientObject() instanceof ItemTicker)
				&&((((ItemTicker)C.getClientObject()).properLocation()==R)))
				{
					if(localItems==null) localItems=new LinkedList<TickClient>();
					localItems.add(C);
				}
				break;
			case 1:
				if((C.getClientObject() instanceof ItemTicker)
				&&((((ItemTicker)C.getClientObject()).properLocation()==R)))
				{
					if(localItems==null) localItems=new LinkedList<TickClient>();
					localItems.add(C);
				}
				break;
			case 2:
				if((C.getClientObject() instanceof MOB)
				&&(((MOB)C.getClientObject()).getStartRoom()==R))
				{
					if(localItems==null) localItems=new LinkedList<TickClient>();
					localItems.add(C);
				}
				break;
			}
		}
		if(localItems == null) return null;
		return localItems.iterator();
	}
	
	public String getName() {
		return name;
	}
	
	public long getTickInterval() {
		return tickTime;
	}
	
	public long getNextTickTime() {
		return nextTickTime;
	}
	
	public boolean contains(final Tickable T, final int tickID)
	{
		if(tickID >= 0)
			return tickers.contains(new StdTickClient(T,0,tickID));
		return tickers.subSet(new StdTickClient(T,0,-1), true, new StdTickClient(T,0,Integer.MAX_VALUE), true).size()>0;
	}
	
	public int getCounter(){return tickObjectCounter;}
	
	public void delTicker(TickClient C)
	{
		tickers.remove(C);
	}
	public void addTicker(TickClient C)
	{
		if(!tickers.contains(C))
			tickers.add(C);
	}
	
	public TickClient getLastTicked()
	{
		return lastClient;
	}

	public String getStatus() 
	{
		final TickClient lastTicked = getLastTicked();
		if((lastTicked==null)||(myEngine==null)||(lastTicked.getClientObject()==null))
			return "Asleep or Shutdown";
		if(!isAwake())
			return "Sleeping";
		final Tickable ticker = lastTicked.getClientObject();
		return "Ticking: "+ticker.ID()+": "+ticker.name()+": "+((myEngine!=null)?myEngine.getTickStatusSummary(ticker):"null");
	}
	
	public void shutdown()
	{
		tickers.clear();
		if(CMLib.threads() instanceof ServiceEngine)
			((ServiceEngine)CMLib.threads()).delTickGroup(this);
	}

	public void run()
	{
		
		nextTickTime=System.currentTimeMillis() + tickTime;
		//final String oldThreadName=Thread.currentThread().getName();
		try
		{
			currentThread=Thread.currentThread();
			lastStart=System.currentTimeMillis();
			lastClient=null;
			final boolean allSuspended=CMLib.threads().isAllSuspended();
			if((CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			&&(!allSuspended))
			{
				for(Iterator<TickClient> i=tickers();i.hasNext();)
				{
					final TickClient client=i.next();
					lastClient=client;
					//if(client.getCurrentTickDown()<=1) currentThread.setName(oldThreadName+":"+getName()+":"+client.getName());
					if(client.tickTicker(false))
					{
						delTicker(client); // cant do i.remove, its an streeset
					}
				}
			}
		}
		finally
		{
			lastStop=System.currentTimeMillis();
			milliTotal+=(lastStop-lastStart);
			tickTotal++;
			currentThread=null;
			//Thread.currentThread().setName(oldThreadName);
		}
		if(tickers.size()==0)
		{
			if(CMLib.threads() instanceof ServiceEngine)
				((ServiceEngine)CMLib.threads()).delTickGroup(this);
		}
	}

	@Override
	public long activeTimeMillis() 
	{
		if(this.isAwake())
			return System.currentTimeMillis()-this.lastStart;
		return 0;
	}
}
