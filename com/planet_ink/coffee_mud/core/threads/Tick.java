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
public class Tick extends Thread implements TickableGroup, Cloneable
{
	private final ThreadEngine myEngine;
	public final long TICK_TIME;
	private final int tickObjectCounter;
	
	public volatile long lastStart=0;
	public volatile long lastStop=0;
	public long milliTotal=0;
	public long tickTotal=0;
	public boolean solitaryTicker=false;
	public volatile boolean awake=false;
	public TockClient lastClient=null;

	private static volatile int tickObjReference=0;
	
	private final STreeSet<TockClient> tickers=new STreeSet<TockClient>();
	
	public Tick(long sleep)
	{
		super("Tick."+(tickObjReference+1));
		tickObjectCounter=tickObjReference++;
		TICK_TIME=sleep;
		myEngine=null;
		this.start();
	}

	public Tick(String a_name, long sleep)
	{
		super("Tick."+ a_name + "." +(tickObjReference+1));
		setDaemon(true);
		tickObjectCounter=tickObjReference++;
		TICK_TIME=sleep;
		myEngine=null;
		this.start();
	}
	
	public Tick(ThreadEngine theEngine, long sleep)
	{
		super("Tick."+(tickObjReference+1));
		tickObjectCounter=tickObjReference++;
		myEngine=theEngine;
		TICK_TIME=sleep;
	}

	public Tick copyOf()
	{
		try
		{
			Tick T=(Tick)this.clone();
			T.tickers.clear();
			T.tickers.addAll(tickers);
			return T;
		}
		catch(Exception e){}
		return this;
	}
	
	public TockClient fetchTickerByIndex(int i)
	{
		int x=0;
		for(final TockClient C : tickers)
			if(i==(x++))
				return C;
		return null;
	}
	
	public Iterator<TockClient> tickers(){return tickers.iterator();}
	public int numTickers(){return tickers.size();}
	public Iterator<TockClient> getTickSet(final Tickable T, final int tickID)
	{
		final LinkedList<TockClient> subSet = new LinkedList<TockClient>();
		if(tickID < 0)
			subSet.addAll(tickers.subSet(new TockClient(T,0,0), true, new TockClient(T,0,Integer.MAX_VALUE), true));
		else
			subSet.addAll(tickers.subSet(new TockClient(T,0,tickID), true, new TockClient(T,0,tickID), true));
		return subSet.iterator();
	}
	
	public Iterator<TockClient> getLocalItems(int itemTypes, Room R)
	{
		LinkedList<TockClient> localItems=null;
		for(final Iterator<TockClient> e=tickers.iterator();e.hasNext();)
		{
			final TockClient C=e.next();
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
	
	
	public boolean contains(final Tickable T, final int tickID)
	{
		if(tickID >= 0)
			return tickers.contains(new TockClient(T,0,tickID));
		return tickers.subSet(new TockClient(T,0,-1), true, new TockClient(T,0,Integer.MAX_VALUE), true).size()>0;
	}
	
	public int getCounter(){return tickObjectCounter;}
	
	public void delTicker(TockClient C)
	{
		tickers.remove(C);
	}
	public void addTicker(TockClient C)
	{
		if(!tickers.contains(C))
			tickers.add(C);
	}
	
	public Tickable lastTicked()
	{
		return lastClient!=null?lastClient.clientObject:null;
	}

	public String getStatus() 
	{
		final Tickable lastTicked = lastTicked();
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

	public void run()
	{
		lastStart=System.currentTimeMillis();
		while(true)
		{
			try
			{
				lastStop=System.currentTimeMillis();
				long SUBTRACT_TIME=(lastStop-lastStart);
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
				final boolean allSuspended=CMLib.threads().isAllSuspended();
				if((CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
				&&(!allSuspended))
				{
					for(Iterator<TockClient> i=tickers();i.hasNext();)
					{
						final TockClient client=i.next();
						lastClient=client;
						client.lastStart=System.currentTimeMillis();
						if(client.tickTicker())
						{
							delTicker(client); // cant do i.remove, its an streeset
						}
						client.lastStop=System.currentTimeMillis();
						client.milliTotal+=(client.lastStop-client.lastStart);
						client.tickTotal++;
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
