package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.database.DBInterface;
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

import java.util.*;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class ServiceEngine implements ThreadEngine
{
	public static final  long STATUS_ALLMISCTICKS=Tickable.STATUS_MISC|Tickable.STATUS_MISC2|Tickable.STATUS_MISC3|Tickable.STATUS_MISC4|Tickable.STATUS_MISC5|Tickable.STATUS_MISC6;
	private static final long SHORT_TICK_TIMEOUT = (5*TimeManager.MILI_MINUTE);
	private static final long LONG_TICK_TIMEOUT  = (120*TimeManager.MILI_MINUTE);

	private Thread  				drivingThread		= null;
	private TickClient 				supportClient		= null;
	protected List<TickableGroup>	allTicks			= new SLinkedList<TickableGroup>();
	private boolean 				isSuspended			= false;
	private int 					max_objs_per_thread	= 0;
	private CMThreadPoolExecutor[]	threadPools			= new CMThreadPoolExecutor[256];
	private volatile long			globalTickID		= 0;
	private final long 				globalStartTime		= System.currentTimeMillis();
	private volatile CMRunnable[]	unsuspendedRunnables= null;
	private volatile long			nextWakeAtTime		= 0;
	private final List<CMRunnable>	schedTicks			= new LinkedList<CMRunnable>();

	@Override
	public String ID()
	{
		return "ServiceEngine";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new ServiceEngine();
		}
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public void initializeClass()
	{
	}
	
	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject) this.clone();
		}
		catch (final Exception e)
		{
			return newInstance();
		}
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void propertiesLoaded()
	{
	}

	@Override
	public TickClient getServiceClient()
	{
		return supportClient;
	}

	public ServiceEngine()
	{
		initializeClass();
	}

	protected CMThreadPoolExecutor getPoolExecutor(final char threadGroupNum)
	{
		final CMThreadPoolExecutor pool = threadPools[threadGroupNum];
		if(pool != null)
			return pool;
		final int minThreads = CMProps.getIntVar(CMProps.Int.MINWORKERTHREADS);
		int maxThreads = CMProps.getIntVar(CMProps.Int.MAXWORKERTHREADS);
		if(maxThreads<=0)
			maxThreads=Integer.MAX_VALUE;
		final String sessionThreadGroupName="Worker"+threadGroupNum;
		threadPools[threadGroupNum] = new CMThreadPoolExecutor(sessionThreadGroupName,minThreads, maxThreads, CMProps.getTickMillis()*2, TimeUnit.MILLISECONDS, (LONG_TICK_TIMEOUT/60000), 1024);
		threadPools[threadGroupNum].setThreadFactory(new CMThreadFactory(sessionThreadGroupName));
		return threadPools[threadGroupNum];
	}

	protected CMThreadPoolExecutor getPoolExecutor(String threadGroupName)
	{
		
		if(threadGroupName == null)
			return getPoolExecutor(Thread.currentThread().getThreadGroup().getName().charAt(0));
		else
			return getPoolExecutor(threadGroupName.charAt(0));
	}

	@Override
	public Iterator<TickableGroup> tickGroups()
	{
		return allTicks.iterator();
	}

	@Override
	public Runnable findRunnableByThread(final Thread thread)
	{
		if((thread==null)||(threadPools==null))
			return null;
		final Runnable possR=(thread instanceof CMFactoryThread)?((CMFactoryThread)thread).getRunnable():null;
		for (final CMThreadPoolExecutor threadPool : threadPools)
		{
			final CMThreadPoolExecutor executor=threadPool;
			if(executor==null)
				continue;
			if(possR!=null)
			{
				synchronized(executor.active)
				{
					if(executor.active.get(possR)==thread)
						return possR;
				}
			}
			if((executor.getThreadFactory() instanceof CMThreadFactory)
			&&(!((CMThreadFactory)executor.getThreadFactory()).getThreads().contains(thread)))
				continue;
			synchronized(executor.active)
			{
				if(!executor.active.containsValue(thread))
					continue;
				for(final Map.Entry<Runnable, Thread> e : executor.active.entrySet())
				{
					if(e.getValue()==thread)
						return e.getKey();
				}
			}
		}
		return null;
	}

	@Override
    public void scheduleRunnable(final Runnable R, long ellapsedMs)
	{
		try
		{
			synchronized(this.schedTicks)
			{
				final long myNextTime = System.currentTimeMillis() + ellapsedMs;
				final char currentThreadId = Thread.currentThread().getThreadGroup().getName().charAt(0);
				schedTicks.add(new CMRunnable()
				{
					@Override 
					public void run() 
					{
						try
						{
							R.run();
						}
						catch(Throwable t)
						{
							Log.errOut(t);
						}
					}

					@Override
					public long activeTimeMillis()
					{
						return System.currentTimeMillis() - getStartTime();
					}

					@Override
					public long getStartTime()
					{
						return myNextTime;
					}

					@Override
					public int getGroupID()
					{
						return currentThreadId;
					}
				});
				if((this.nextWakeAtTime == 0) || (myNextTime < this.nextWakeAtTime))
				{
					this.nextWakeAtTime = myNextTime;
					if(drivingThread!=null)
						drivingThread.interrupt();
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("ServiceEngine","ExecRun: "+e.getMessage());
			Log.debugOut("ServiceEngine",e);
		}
	}
	
	@Override
	public void executeRunnable(String threadGroupName, Runnable R)
	{
		try
		{
			getPoolExecutor(threadGroupName).execute(R);
		}
		catch(final Exception e)
		{
			Log.errOut("ServiceEngine","ExecRun: "+e.getMessage());
			Log.debugOut("ServiceEngine",e);
		}
	}

	@Override
	public void executeRunnable(Runnable R)
	{
		try
		{
			getPoolExecutor(null).execute(R);
		}
		catch(final Exception e)
		{
			Log.errOut("ServiceEngine","ExecRun: "+e.getMessage());
			Log.debugOut("ServiceEngine",e);
		}
	}

	public int getMaxObjectsPerThread()
	{
		if(max_objs_per_thread>0)
			return max_objs_per_thread;
		max_objs_per_thread = CMProps.getIntVar(CMProps.Int.OBJSPERTHREAD);
		if(max_objs_per_thread>0)
			return max_objs_per_thread;
		max_objs_per_thread=0;
		return 128;
	}

	@Override
	public long getTicksEllapsedSinceStartup()
	{
		return globalTickID;
	}

	protected void delTickGroup(TickableGroup tock)
	{
		allTicks.remove(tock);
		if(drivingThread!=null)
			drivingThread.interrupt();
	}

	protected void addTickGroup(TickableGroup tock)
	{
		if(!allTicks.contains(tock))
			allTicks.add(tock);
		if(drivingThread!=null)
			drivingThread.interrupt();
	}

	@Override
	public TickClient startTickDown(Tickable E, int tickID, int numTicks)
	{
		return startTickDown(E,tickID,CMProps.getTickMillis(),numTicks);
	}

	@Override
	public TickClient startTickDown(Tickable E, int tickID, long TICK_TIME, int numTicks)
	{
		return startTickDown(CMLib.map().getOwnedThreadGroup(E),E,tickID,TICK_TIME,numTicks);
	}

	public synchronized TickClient startTickDown(ThreadGroup group, Tickable E, int tickID, long tickTime, int numTicks)
	{
		TickableGroup tock=null;
		if(group==null)
			group=Thread.currentThread().getThreadGroup();
		final char threadGroupNum=group.getName().charAt(0);
		for(final TickableGroup almostTock : allTicks)
		{
			if(almostTock.contains(E,tickID))
			{
				for(final Iterator<TickClient> set=almostTock.getTickSet(E,tickID);set.hasNext();)
					set.next().setSuspended(false);
				return null;
			}
			if((tock==null)
			&&(almostTock.getTickInterval()==tickTime)
			&&(!almostTock.isSolitaryTicker())
			&&(almostTock.numTickers()<getMaxObjectsPerThread()))
			{
				final String name = almostTock.getThreadGroupName();
				if((name!=null)
				&&(name.charAt(0)==threadGroupNum))
					tock=almostTock;
			}
		}
		final boolean isSolitary = ((tickID&Tickable.TICKID_SOLITARYMASK)==Tickable.TICKID_SOLITARYMASK);
		if((tock==null)||isSolitary)
		{
			tock=new StdTickGroup(this, tickTime, Thread.currentThread().getThreadGroup().getName(), isSolitary);
			addTickGroup(tock);
		}

		final TickClient newC=new StdTickClient(E,numTicks,tickID);
		tock.addTicker(newC);
		return newC;
	}

	@Override
	public synchronized boolean deleteTick(Tickable E, int tickID)
	{
		boolean foundOne=false;
		for(final TickableGroup almostTock : allTicks)
		{
			final Iterator<TickClient> set=almostTock.getTickSet(E,tickID);
			foundOne = foundOne || set.hasNext();
			for(;set.hasNext();)
				almostTock.delTicker(set.next());
		}
		return foundOne;
	}

	@Override
	public synchronized boolean setTickPending(Tickable E, int tickID)
	{
		boolean foundOne=false;
		for(final TickableGroup almostTock : allTicks)
		{
			final Iterator<TickClient> set=almostTock.getTickSet(E,tickID);
			foundOne = foundOne || set.hasNext();
			for(;set.hasNext();)
			{
				final TickClient C = set.next();
				C.setCurrentTickDownPending();
			}
		}
		return foundOne;
	}

	@Override
	public long msToNextTick(Tickable E, int tickID)
	{
		for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			final TickableGroup almostTock=e.next();
			final Iterator<TickClient> set=almostTock.getTickSet(E,tickID);
			if(set.hasNext())
			{
				TickClient client = set.next();
				return client.getTimeMSToNextTick();
			}
		}
		return -1;
	}

	@Override
	public boolean isTicking(Tickable E, int tickID)
	{
		for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			final TickableGroup almostTock=e.next();
			final Iterator<TickClient> set=almostTock.getTickSet(E,tickID);
			if(set.hasNext())
				return true;
		}
		return false;
	}

	@Override
	public boolean isAllSuspended()
	{
		return isSuspended;
	}

	@Override
	public void suspendAll(CMRunnable[] exceptRs)
	{
		unsuspendedRunnables = exceptRs;
		isSuspended = true;
	}

	@Override
	public void resumeAll()
	{
		unsuspendedRunnables = null;
		isSuspended = false;
	}

	@Override
	public void suspendTicking(Tickable E, int tickID)
	{
		suspendResumeTicking(E, tickID, true);
	}

	@Override
	public void resumeTicking(Tickable E, int tickID)
	{
		suspendResumeTicking(E, tickID, false);
	}

	protected boolean suspendResumeTicking(Tickable E, int tickID, boolean suspend)
	{
		for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			final TickableGroup almostTock=e.next();
			for(final Iterator<TickClient> set=almostTock.getTickSet(E,tickID);set.hasNext();)
				set.next().setSuspended(suspend);
		}
		return false;
	}

	@Override
	public boolean isSuspended(Tickable E, int tickID)
	{
		for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			final TickableGroup almostTock=e.next();
			final Iterator<TickClient> set=almostTock.getTickSet(E,tickID);
			if(set.hasNext() && set.next().isSuspended())
				return true;
		}
		return false;
	}

	@Override
	public String systemReport(final String itemCode)
	{
		long totalMOBMillis=0;
		long totalMOBTicks=0;
		long topMOBMillis=0;
		long topMOBTicks=0;
		MOB topMOBClient=null;
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			totalMOBMillis+=S.getTotalMillis();
			totalMOBTicks+=S.getTotalTicks();
			if(S.getTotalMillis()>topMOBMillis)
			{
				topMOBMillis=S.getTotalMillis();
				topMOBTicks=S.getTotalTicks();
				topMOBClient=S.mob();
			}
		}

		if(itemCode.equalsIgnoreCase("totalMOBMillis"))
			return ""+totalMOBMillis;
		else
		if(itemCode.equalsIgnoreCase("totalMOBMillisTime"))
			return CMLib.english().returnTime(totalMOBMillis,0);
		else
		if(itemCode.equalsIgnoreCase("totalMOBMillisTimePlusAverage"))
			return CMLib.english().returnTime(totalMOBMillis,totalMOBTicks);
		else
		if(itemCode.equalsIgnoreCase("totalMOBTicks"))
			return ""+totalMOBTicks;
		else
		if(itemCode.equalsIgnoreCase("topMOBMillis"))
			return ""+topMOBMillis;
		else
		if(itemCode.equalsIgnoreCase("topMOBMillisTime"))
			return CMLib.english().returnTime(topMOBMillis,0);
		else
		if(itemCode.equalsIgnoreCase("topMOBMillisTimePlusAverage"))
			return CMLib.english().returnTime(topMOBMillis,topMOBTicks);
		else
		if(itemCode.equalsIgnoreCase("topMOBTicks"))
			return ""+topMOBTicks;
		else
		if(itemCode.equalsIgnoreCase("topMOBClient"))
		{
			if(topMOBClient!=null)
				return topMOBClient.Name();
			return "";
		}
		else
		if(itemCode.toLowerCase().startsWith("tickerproblems"))
		{
			final int x=itemCode.indexOf('-');
			int total=10;
			if(x>0)
				total=CMath.s_int(itemCode.substring(x+1));
			final Vector<Triad<Long,Integer,Integer>> list=new Vector<Triad<Long,Integer,Integer>>();
			int group=0;
			for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
			{
				final TickableGroup almostTock=e.next();
				int tick=0;
				for(final Iterator<TickClient> et=almostTock.tickers();et.hasNext();)
				{
					final TickClient C=et.next();
					if(C.getTickTotal()==0)
						continue;
					final Long avg=Long.valueOf(C.getMilliTotal()/C.getTickTotal());
					int i=0;
					for(;i<list.size();i++)
					{
						final Triad<Long,Integer,Integer> t=list.get(i);
						if(avg.longValue()>=t.first.longValue())
						{
							list.add(i,new Triad<Long,Integer,Integer>(avg,Integer.valueOf(group),Integer.valueOf(tick)));
							break;
						}
					}
					if((list.size()==0)||((i>=list.size())&&(list.size()<total)))
						list.add(new Triad<Long,Integer,Integer>(avg,Integer.valueOf(group),Integer.valueOf(tick)));
					while(list.size()>total)
						list.remove(list.size()-1);
					tick++;
				}
				group++;
			}
			if(list.size()==0)
				return "";
			final StringBuilder str=new StringBuilder("");
			for(final Triad<Long,Integer,Integer> t : list)
				str.append(';').append(t.second).append(',').append(t.third);
			return str.toString().substring(1);
		}

		int totalTickers=0;
		long totalMillis=0;
		long totalTicks=0;
		int topGroupNumber=-1;
		long topGroupMillis=-1;
		long topGroupTicks=0;
		long topObjectMillis=-1;
		long topObjectTicks=0;
		int topObjectGroup=0;
		Tickable topObjectClient=null;
		int num=0;
		TickableGroup almostTock = null;
		for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			totalTickers+=almostTock.numTickers();
			totalMillis+=almostTock.getMilliTotal();
			totalTicks+=almostTock.getTickTotal();
			if(almostTock.getMilliTotal()>topGroupMillis)
			{
				topGroupMillis=almostTock.getMilliTotal();
				topGroupTicks=almostTock.getTickTotal();
				topGroupNumber=num;
			}
			try
			{
				for(final Iterator<TickClient> et=almostTock.tickers();et.hasNext();)
				{
					final TickClient C=et.next();
					if(C.getMilliTotal()>topObjectMillis)
					{
						topObjectMillis=C.getMilliTotal();
						topObjectTicks=C.getTickTotal();
						topObjectClient=C.getClientObject();
						topObjectGroup=num;
					}
				}
			}
			catch(final NoSuchElementException ex)
			{
			}
			num++;
		}
		if(itemCode.equalsIgnoreCase("freeMemory"))
			return ""+(Runtime.getRuntime().freeMemory()/1000);
		else
		if(itemCode.equalsIgnoreCase("totalMemory"))
			return ""+(Runtime.getRuntime().totalMemory()/1000);
		else
		if(itemCode.equalsIgnoreCase("totalTime"))
			return ""+CMLib.english().returnTime(System.currentTimeMillis()-CMSecurity.getStartTime(),0);
		else
		if(itemCode.equalsIgnoreCase("startTime"))
			return CMLib.time().date2String(CMSecurity.getStartTime());
		else
		if(itemCode.equalsIgnoreCase("currentTime"))
			return CMLib.time().date2String(System.currentTimeMillis());
		else
		if(itemCode.equalsIgnoreCase("totalTickers"))
			return ""+totalTickers;
		else
		if(itemCode.equalsIgnoreCase("totalMillis"))
			return ""+totalMillis;
		else
		if(itemCode.equalsIgnoreCase("totalMillisTime"))
			return CMLib.english().returnTime(totalMillis,0);
		else
		if(itemCode.equalsIgnoreCase("totalMillisTimePlusAverage"))
			return CMLib.english().returnTime(totalMillis,totalTicks);
		else
		if(itemCode.equalsIgnoreCase("totalTicks"))
			return ""+totalTicks;
		else
		if(itemCode.equalsIgnoreCase("tickgroupsize"))
			return ""+allTicks.size();
		else
		if(itemCode.equalsIgnoreCase("numthreads"))
			return ""+getPoolExecutor(null).getPoolSize();
		else
		if(itemCode.equalsIgnoreCase("numactivethreads"))
			return ""+getPoolExecutor(null).getActiveCount();
		else
		if(itemCode.equalsIgnoreCase("topGroupNumber"))
			return ""+topGroupNumber;
		else
		if(itemCode.equalsIgnoreCase("topGroupMillis"))
			return ""+topGroupMillis;
		else
		if(itemCode.equalsIgnoreCase("topGroupMillisTime"))
			return CMLib.english().returnTime(topGroupMillis,0);
		else
		if(itemCode.equalsIgnoreCase("topGroupMillisTimePlusAverage"))
			return CMLib.english().returnTime(topGroupMillis,topGroupTicks);
		else
		if(itemCode.equalsIgnoreCase("topGroupTicks"))
			return ""+topGroupTicks;
		else
		if(itemCode.equalsIgnoreCase("topObjectMillis"))
			return ""+topObjectMillis;
		else
		if(itemCode.equalsIgnoreCase("topObjectMillisTime"))
			return CMLib.english().returnTime(topObjectMillis,0);
		else
		if(itemCode.equalsIgnoreCase("topObjectMillisTimePlusAverage"))
			return CMLib.english().returnTime(topObjectMillis,topObjectTicks);
		else
		if(itemCode.equalsIgnoreCase("topObjectTicks"))
			return ""+topObjectTicks;
		else
		if(itemCode.equalsIgnoreCase("topObjectGroup"))
			return ""+topObjectGroup;
		else
		if(itemCode.toLowerCase().startsWith("thread"))
		{
			final int xstart="thread".length();
			int xend=xstart;
			while((xend<itemCode.length())&&(Character.isDigit(itemCode.charAt(xend))))
				xend++;
			final int threadNum=CMath.s_int(itemCode.substring(xstart,xend));
			int curThreadNum=0;
			for(final Enumeration<CMLibrary> e=CMLib.libraries();e.hasMoreElements();)
			{
				final CMLibrary lib=e.nextElement();
				final TickClient serviceClient=lib.getServiceClient();
				if(serviceClient!=null)
				{
					if(curThreadNum==threadNum)
					{
						final String instrCode=itemCode.substring(xend);
						if(instrCode.equalsIgnoreCase("activeMiliTotal"))
							return ""+serviceClient.getMilliTotal();
						if(instrCode.equalsIgnoreCase("milliTotal"))
							return ""+serviceClient.getMilliTotal();
						if(instrCode.equalsIgnoreCase("status"))
							return ""+serviceClient.getStatus();
						if(instrCode.equalsIgnoreCase("name"))
							return ""+serviceClient.getName();
						if(instrCode.equalsIgnoreCase("MilliTotalTime"))
							return CMLib.english().returnTime(serviceClient.getMilliTotal(),0);
						if(instrCode.equalsIgnoreCase("MiliTotalTime"))
							return CMLib.english().returnTime(serviceClient.getMilliTotal(),0);
						if(instrCode.equalsIgnoreCase("MilliTotalTimePlusAverage")||instrCode.equalsIgnoreCase("MiliTotalTimePlusAverage"))
							return CMLib.english().returnTime(serviceClient.getMilliTotal(),serviceClient.getTickTotal());
						if(instrCode.equalsIgnoreCase("TickTotal"))
							return ""+serviceClient.getTickTotal();
						break;
					}
					curThreadNum++;
				}
			}

		}
		if(itemCode.equalsIgnoreCase("topObjectClient"))
		{
			if(topObjectClient!=null)
				return topObjectClient.name();
			return "";
		}

		return "";
	}

	@Override
	public void rejuv(Room here, int tickID)
	{
		TickableGroup almostTock=null;
		TickClient C=null;
		Tickable E2=null;
		final boolean doItems=((tickID<=0)||(tickID==Tickable.TICKID_ROOM_ITEM_REJUV));
		final boolean doMobs=((tickID<=0)||(tickID==Tickable.TICKID_MOB));
		for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			try
			{
				for(final Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
				{
					C=i.next();
					E2=C.getClientObject();
					if((doItems)
					&&(E2 instanceof ItemTicker)
					&&((here==null)||(((ItemTicker)E2).properLocation()==here)))
					{
						if(C.tickTicker(true))
							almostTock.delTicker(C);
					}
					else
					if((doMobs)
					&&(E2 instanceof MOB)
					&&(((MOB)E2).amDead())
					&&((here==null)||(((MOB)E2).getStartRoom()==here))
					&&(((MOB)E2).basePhyStats().rejuv()>0)
					&&(((MOB)E2).basePhyStats().rejuv()!=PhyStats.NO_REJUV)
					&&(((MOB)E2).phyStats().rejuv()>0))
					{
						((MOB)E2).phyStats().setRejuv(-1);
						if(C.tickTicker(true))
							almostTock.delTicker(C);
					}
				}
			}
			catch(final NoSuchElementException ex)
			{
			}
		}
	}

	@Override
	public void tickAllTickers(Room here)
	{
		TickableGroup almostTock=null;
		TickClient C=null;
		Tickable E2=null;
		final WorldMap map=CMLib.map();
		for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			try
			{
				for(final Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
				{
					C=i.next();
					E2=C.getClientObject();
					if(here==null)
					{
						if(C.tickTicker(false))
							almostTock.delTicker(C);
					}
					else
					if(map.isHere(E2,here))
					{
						if(C.tickTicker(false))
							almostTock.delTicker(C);
					}
					else
					if((E2 instanceof Ability)
					&&(map.isHere(((Ability)E2).affecting(),here)))
					{
						if(C.tickTicker(false))
							almostTock.delTicker(C);
					}
				}
			}
			catch(final NoSuchElementException ex)
			{
			}
		}
	}

	@Override
	public void suspendResumeRecurse(CMObject O, boolean skipEmbeddedAreas, boolean suspend)
	{
		if(O instanceof Item)
		{
			final Item I=(Item)O;
			suspendResumeTicking(I, -1, suspend);
			if((I instanceof BoardableShip)&&(!skipEmbeddedAreas))
				suspendResumeRecurse(((BoardableShip)I).getShipArea(),true,suspend);
		}
		else
		if(O instanceof MOB)
		{
			final MOB M=(MOB)O;
			for(int i=0;i<M.numItems();i++)
			{
				final Item I=M.getItem(i);
				suspendResumeRecurse(I,skipEmbeddedAreas,suspend);
			}
			final PlayerStats pStats=M.playerStats();
			if(pStats!=null)
			{
				final ItemCollection collection=pStats.getExtItems();
				if(collection!=null)
				for(int i=0;i<collection.numItems();i++)
				{
					final Item I=collection.getItem(i);
					suspendResumeRecurse(I,skipEmbeddedAreas,suspend);
				}
			}
			else
			{
				// dont suspend players -- they are handled differently
				suspendResumeTicking(M, -1, suspend);
			}
		}
		else
		if(O instanceof Room)
		{
			final Room R=(Room)O;
			suspendResumeTicking(R, -1, suspend);
			for(int i=0;i<R.numInhabitants();i++)
				suspendResumeRecurse(R.fetchInhabitant(i),skipEmbeddedAreas,suspend);
			for(int i=0;i<R.numItems();i++)
				suspendResumeRecurse(R.getItem(i),skipEmbeddedAreas,suspend);
		}
		else
		if(O instanceof Area)
		{
			final Area A=(Area)O;
			A.setAreaState(suspend?Area.State.FROZEN:Area.State.ACTIVE);
			for(final Enumeration<Room> r=A.getFilledProperMap();r.hasMoreElements();)
				suspendResumeRecurse(r.nextElement(),skipEmbeddedAreas,suspend);
		}
	}

	@Override
	public boolean deleteAllTicks(Tickable ticker)
	{
		if(ticker==null)
			return false;
		deleteTick(ticker, -1);
		final WorldMap map=CMLib.map();
		boolean deleted = false;
		if(ticker instanceof Room)
		{
			TickableGroup almostTock=null;
			TickClient C=null;
			Tickable E2=null;
			for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
			{
				almostTock=e.next();
				try
				{
					for(final Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
					{
						C=i.next();
						E2=C.getClientObject();
						if(map.isHere(E2,(Room)ticker))
							deleted = almostTock.delTicker(C) || deleted;
						else
						if((E2 instanceof Ability)
						&&(map.isHere(((Ability)E2).affecting(),(Room)ticker)))
							deleted = almostTock.delTicker(C) || deleted;
						else
							continue;

						try
						{
							if(almostTock.numTickers()==0)
								allTicks.remove(almostTock);
						}
						catch (final Exception e2)
						{
						}
					}
				}
				catch(final NoSuchElementException ex)
				{
				}
			}
		}
		else
		if(ticker instanceof Area)
		{
			TickableGroup almostTock=null;
			TickClient C=null;
			Tickable E2=null;
			for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
			{
				almostTock=e.next();
				try
				{
					for(final Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
					{
						C=i.next();
						E2=C.getClientObject();
						if(map.isHere(E2,(Area)ticker))
							deleted = almostTock.delTicker(C) || deleted;
						else
						if((E2 instanceof Ability)
						&&(map.isHere(((Ability)E2).affecting(),(Area)ticker)))
							deleted = almostTock.delTicker(C) || deleted;
						else
							continue;

						try
						{
							if(almostTock.numTickers()==0)
								allTicks.remove(almostTock);
						}
						catch (final Exception e2)
						{
						}
					}
				}
				catch(final NoSuchElementException ex)
				{
				}
			}
		}
		return deleted;
	}

	@Override
	public String tickInfo(String which)
	{
		int grpstart=-1;
		for(int i=0;i<which.length();i++)
		{
			if(Character.isDigit(which.charAt(i)))
			{
				grpstart=i;
				break;
			}
		}
		if(which.equalsIgnoreCase("tickGroupSize"))
			return ""+allTicks.size();
		else
		if(which.toLowerCase().startsWith("tickerssize"))
		{
			if(grpstart<0)
				return"";
			final int group=CMath.s_int(which.substring(grpstart));
			if((group>=0)&&(group<allTicks.size()))
			{
				final List<TickableGroup> enumeratedTicks=new XVector<TickableGroup>(allTicks);
				return ""+enumeratedTicks.get(group).numTickers();
			}
			return "";
		}
		int group=-1;
		int client=-1;
		final int clistart=which.indexOf('-');
		if((grpstart>=0)&&(clistart>grpstart))
		{
			group=CMath.s_int(which.substring(grpstart,clistart));
			client=CMath.s_int(which.substring(clistart+1));
		}

		if((group<0)||(client<0)||(group>=allTicks.size()))
			return "";
		final List<TickableGroup> enumeratedTicks=new XVector<TickableGroup>(allTicks);
		if((group<0)||(client<0)||(group>=enumeratedTicks.size()))
			return "";
		final TickableGroup almostTock=enumeratedTicks.get(group);

		if(client>=almostTock.numTickers())
			return "";
		final TickClient C=almostTock.fetchTickerByIndex(client);
		if(C==null)
			return "";

		if(which.toLowerCase().startsWith("tickername"))
		{
			Tickable E=C.getClientObject();
			if((E instanceof Ability)&&(E.ID().equals("ItemRejuv")))
				E=((Ability)E).affecting();
			if(E instanceof Room)
				return CMLib.map().getExtendedRoomID((Room)E);
			if(E!=null)
				return E.name();
			return "!NULL!";
		}
		else
		if(which.toLowerCase().startsWith("tickerid"))
			return ""+C.getTickID();
		else
		if(which.toLowerCase().startsWith("tickerstatusstr"))
			return C.getStatus();
		else
		if(which.toLowerCase().startsWith("tickerstatus"))
			return ((C.getClientObject()==null)?"":(""+C.getClientObject().getTickStatus()));
		else
		if(which.toLowerCase().startsWith("tickercodeword"))
			return getTickStatusSummary(C.getClientObject());
		else
		if(which.toLowerCase().startsWith("tickertickdown"))
			return ""+C.getCurrentTickDown();
		else
		if(which.toLowerCase().startsWith("tickerretickdown"))
			return ""+C.getTotalTickDown();
		else
		if(which.toLowerCase().startsWith("tickermillitotal"))
			return ""+C.getMilliTotal();
		else
		if(which.toLowerCase().startsWith("tickermilliavg"))
		{
			if(C.getTickTotal()==0)
				return "0";
			return ""+(C.getMilliTotal()/C.getTickTotal());
		}
		else
		if(which.toLowerCase().startsWith("tickerlaststartmillis"))
			return ""+C.getLastStartTime();
		else
		if(which.toLowerCase().startsWith("tickerlaststopmillis"))
			return ""+C.getLastStopTime();
		else
		if(which.toLowerCase().startsWith("tickerlaststartdate"))
			return CMLib.time().date2String(C.getLastStartTime());
		else
		if(which.toLowerCase().startsWith("tickerlaststopdate"))
			return CMLib.time().date2String(C.getLastStopTime());
		else
		if(which.toLowerCase().startsWith("tickerlastduration"))
		{
			if(C.getLastStopTime()>=C.getLastStartTime())
				return CMLib.english().returnTime(C.getLastStopTime()-C.getLastStartTime(),0);
			return CMLib.english().returnTime(System.currentTimeMillis()-C.getLastStartTime(),0);
		}
		else
		if(which.toLowerCase().startsWith("tickersuspended"))
			return ""+C.isSuspended();
		return "";
	}

	@Override
	public boolean shutdown()
	{
		//int numTicks=tickGroup.size();
		while(allTicks.size()>0)
		{
			//Log.sysOut("ServiceEngine","Shutting down all tick "+which+"/"+numTicks+"...");
			final TickableGroup tock=allTicks.iterator().next();
			if(tock!=null)
			{
				CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...shutting down Service Engine: killing "+tock.getName()+": "+tock.getStatus());
				tock.shutdown();
				allTicks.remove(tock);
			}
			CMLib.s_sleep(100);
		}
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...shutting down Service Engine: "+ID()+": thread shutdown");
		CMLib.killThread(drivingThread,100,10);
		// force final time tick!
		final Vector<TimeClock> timeObjects=new Vector<TimeClock>();
		for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			final Area A=(e.nextElement());
			if(!timeObjects.contains(A.getTimeObj()))
				timeObjects.addElement(A.getTimeObj());
		}
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...shutting down Service Engine: "+ID()+": saving time objects");
		for(int t=0;t<timeObjects.size();t++)
			timeObjects.elementAt(t).save();
		for(final CMThreadPoolExecutor pool : threadPools)
		{
			if(pool != null)
			{
				pool.shutdown();
				try
				{
					pool.awaitTermination(2, TimeUnit.SECONDS);
				}
				catch (final InterruptedException e)
				{
					pool.shutdownNow();
					try
					{
						pool.awaitTermination(3, TimeUnit.SECONDS);
					}
					catch (final InterruptedException e2)
					{
					}
				}
			}
		}
		Log.sysOut("ServiceEngine","Shutdown complete.");
		return true;
	}

	@Override
	public synchronized void clearDebri(Room room, int taskCode)
	{
		TickableGroup almostTock=null;
		TickClient C=null;
		ItemTicker  I=null;
		Iterator<TickClient> roomSet;
		MOB mob=null;
		for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			roomSet=almostTock.getLocalItems(taskCode,room);
			if(roomSet!=null)
			{
				for(;roomSet.hasNext();)
				{
					C=roomSet.next();
					if(C.getClientObject() instanceof ItemTicker)
					{
						I=(ItemTicker)C.getClientObject();
						almostTock.delTicker(C);
						I.setProperLocation(null);
					}
					else
					if(C.getClientObject() instanceof MOB)
					{
						mob=(MOB)C.getClientObject();
						if((mob.isMonster())
						&&(!room.isInhabitant(mob))
						&&((mob.amFollowing()==null)
							||(!mob.amUltimatelyFollowing().isPlayer())
							||(!CMLib.flags().isInTheGame(mob.amUltimatelyFollowing(), true))))
						{
							mob.destroy();
							almostTock.delTicker(C);
						}
					}
				}
			}
		}
	}

	@Override
	public List<Tickable> getNamedTickingObjects(String name)
	{
		final Vector<Tickable> V=new Vector<Tickable>();
		TickableGroup almostTock=null;
		TickClient C=null;
		name=name.toUpperCase().trim();
		for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			for(final Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
			{
				C=i.next();
				if((C.getClientObject()!=null)
				&&(C.getClientObject().name().toUpperCase().indexOf(name)>=0)
				&&(!V.contains(C.getClientObject())))
					V.addElement(C.getClientObject());
			}
		}
		return V;
	}

	@Override
	public String getTickStatusSummary(Tickable obj)
	{
		if(obj==null)
			return "";
		final long code=obj.getTickStatus();
		if(obj instanceof Environmental)
		{
			if(CMath.bset(code,Tickable.STATUS_BEHAVIOR) && (obj instanceof PhysicalAgent))
			{
				final long b=(code-Tickable.STATUS_BEHAVIOR);
				String codeWord="Behavior #"+b;
				if((b>=0)&&(b<((PhysicalAgent)obj).numBehaviors()))
				{
					final Behavior B=((PhysicalAgent)obj).fetchBehavior((int)b);
					codeWord+=" ("+B.name()+": "+B.getTickStatus();
				}
				else
					codeWord+=" (#Error#)";
				return codeWord;
			}
			else
			if(CMath.bset(code,Tickable.STATUS_SCRIPT)&&(obj instanceof MOB))
			{
				final long b=(code-Tickable.STATUS_SCRIPT);
				String codeWord="Script #"+b;
				if((b>=0)&&(b<((MOB)obj).numScripts()))
				{
					final ScriptingEngine S=((MOB)obj).fetchScript((int)b);
					codeWord+=" ("+CMStrings.limit(S.getScript(),20)+": "+S.getTickStatus();
				}
				return codeWord;
			}
			else
			if((code&STATUS_ALLMISCTICKS)>0)
			{
				final long base=(code&STATUS_ALLMISCTICKS);
				int num=0;
				for(int i=1;i<6;i++)
				{
					if((1<<(10+i))==base)
					{
						num = i;
						break;
					}
				}
				return "Misc"+num+" Activity #"+(code-base);
			}
			else
			if(CMath.bset(code,Tickable.STATUS_AFFECT) && (obj instanceof Physical))
			{
				final long b=(code-Tickable.STATUS_AFFECT);
				String codeWord="Effect #"+b;
				if((b>=0)&&(b<((Physical)obj).numEffects()))
				{
					final Environmental E=((Physical)obj).fetchEffect((int)b);
					codeWord+=" ("+E.name()+": "+E.getTickStatus()+")";
				}
				return codeWord;
			}
		}
		String codeWord=null;
		if(CMath.bset(code,Tickable.STATUS_BEHAVIOR))
			codeWord="Behavior?!";
		else
		if(CMath.bset(code,Tickable.STATUS_SCRIPT))
			codeWord="Script?!";
		 else
		if(CMath.bset(code,Tickable.STATUS_AFFECT))
			codeWord="Effect?!";
		else
		switch((int)code)
		{
		case Tickable.STATUS_ALIVE:
			codeWord="Alive"; break;
		case Tickable.STATUS_REBIRTH:
			codeWord="Rebirth"; break;
		case Tickable.STATUS_CLASS:
			codeWord="Class"; break;
		case Tickable.STATUS_DEAD:
			codeWord="Dead"; break;
		case Tickable.STATUS_END:
			codeWord="End"; break;
		case Tickable.STATUS_FIGHT:
			codeWord="Fighting"; break;
		case Tickable.STATUS_NOT:
			codeWord="!"; break;
		case Tickable.STATUS_OTHER:
			codeWord="Other"; break;
		case Tickable.STATUS_RACE:
			codeWord="Race"; break;
		case Tickable.STATUS_START:
			codeWord="Start"; break;
		case Tickable.STATUS_WEATHER:
			codeWord="Weather"; break;
		default:
			codeWord="?"; break;
		}
		return codeWord;
	}

	public void insertOrderDeathInOrder(DVector DV, long lastStart, String msg, TickableGroup tock)
	{
		if(DV.size()>0)
		for(int i=0;i<DV.size();i++)
		{
			if(((Long)DV.elementAt(i,1)).longValue()>lastStart)
			{
				DV.insertElementAt(i,Long.valueOf(lastStart),msg,tock);
				return;
			}
		}
		DV.addElement(Long.valueOf(lastStart),msg,tock);
	}

	public void setSupportStatus(String s)
	{
		if(supportClient != null)
		{
			supportClient.setStatus(s);
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.UTILITHREAD))
				Log.debugOut("ServiceEngine",s);
		}
	}

	@Override
	public void debugDumpStack(final String ID, Thread theThread)
	{
		// I wish Java had compiler directives.  Would be great to un-comment this for 1.5 JVMs
		final StringBuffer dump = new StringBuffer("");
		if(theThread == null)
			dump.append("NULL!!");
		else
		{
			final java.lang.StackTraceElement[] s=theThread.getStackTrace();
			for (final StackTraceElement element : s)
				dump.append("\n   "+element.getClassName()+": "+element.getMethodName()+"("+element.getFileName()+": "+element.getLineNumber()+")");
		}
		Log.debugOut(ID,dump.toString());
	}

	public final void checkHealth()
	{
		final long lastDateTime=System.currentTimeMillis()-SHORT_TICK_TIMEOUT;
		final long longerDateTime=System.currentTimeMillis()-LONG_TICK_TIMEOUT;
		setSupportStatus("checking");

		setSupportStatus("checking tick groups.");
		final DVector orderedDeaths=new DVector(3);
		try
		{
			TickableGroup almostTock = null;
			for(final Iterator<TickableGroup> e=tickGroups();e.hasNext();)
			{
				almostTock=e.next();
				if((almostTock.isAwake())
				&&(almostTock.getLastStartTime()!=0)
				&&(almostTock.getLastStartTime()<lastDateTime))
				{
					final TickClient tickClient=almostTock.getLastTicked();
					final Tickable ticker=(tickClient!=null) ? tickClient.getClientObject() : null;
					if(tickClient==null)
						insertOrderDeathInOrder(orderedDeaths,0,"LOCKED GROUP "+almostTock.getName()+": "+almostTock.getStatus()+"! No further information.",almostTock);
					else
					if((!CMath.bset(tickClient.getTickID(),Tickable.TICKID_LONGERMASK))||(almostTock.getLastStopTime()<longerDateTime))
					{
						if(ticker==null)
							insertOrderDeathInOrder(orderedDeaths,0,"LOCKED GROUP "+almostTock.getName()+": "+almostTock.getStatus()+": NULL @"+CMLib.time().date2String(tickClient.getLastStartTime())+", tickID "+tickClient.getTickID(),almostTock);
						else
						{
							StringBuffer logError=null;
							final long code=ticker.getTickStatus();
							final String codeWord=getTickStatusSummary(ticker);
							String log=null;
							if(ticker instanceof Environmental)
								logError=new StringBuffer("LOCKED GROUP "+almostTock.getName()+": "+almostTock.getStatus()+": "+ticker.name()+" ("+((Environmental)ticker).ID()+") @"+CMLib.time().date2String(tickClient.getLastStartTime())+", status("+code+" ("+codeWord+"), tickID "+tickClient.getTickID());
							else
								logError=new StringBuffer("LOCKED GROUP "+almostTock.getName()+": "+almostTock.getStatus()+": "+ticker.name()+", status ("+code+"/"+codeWord+") @"+CMLib.time().date2String(tickClient.getLastStartTime())+", tickID "+tickClient.getTickID());

							if((ticker instanceof MOB)&&(((MOB)ticker).location()!=null))
								log=logError.toString()+" in "+((MOB)ticker).location().roomID();
							else
							if((ticker instanceof Item)&&(((Item)ticker).owner()!=null)&&(((Item)ticker).owner() instanceof Room))
								log=logError.toString()+" in "+((Room)((Item)ticker).owner()).roomID();
							else
							if((ticker instanceof Item)&&(((Item)ticker).owner()!=null)&&(((Item)ticker).owner() instanceof MOB))
								log=logError.toString()+" owned by "+((MOB)((Item)ticker).owner()).name();
							else
							if(ticker instanceof Room)
								log=logError.toString()+" is "+((Room)ticker).roomID();
							else
								log=logError.toString();
							insertOrderDeathInOrder(orderedDeaths,tickClient.getLastStartTime(),log,almostTock);
						}
					}
					// no isDEBUGGING check -- just always let her rip.
					debugDumpStack(Thread.currentThread().getName(),almostTock.getCurrentThread());
				}
			}
		}
		catch (final java.util.NoSuchElementException e)
		{
		}
		for(int i=0;i<orderedDeaths.size();i++)
			Log.errOut(Thread.currentThread().getName(),(String)orderedDeaths.elementAt(i,2));

		setSupportStatus("killing tick groups.");
		for(int x=0;x<orderedDeaths.size();x++)
		{
			final TickableGroup almostTock=(TickableGroup)orderedDeaths.elementAt(x,3);
			final Vector<TickClient> tockClients=new Vector<TickClient>();
			try
			{
				for(final Iterator<TickClient> e=almostTock.tickers();e.hasNext();)
					tockClients.addElement(e.next());
			}
			catch (final NoSuchElementException e)
			{
			}
			try
			{
				almostTock.shutdown();
			}
			catch(final java.lang.ThreadDeath d)
			{
				Log.errOut("ThreadDeath killing "+almostTock.getName());
			}
			catch(final Throwable t)
			{
				Log.errOut("Error "+t.getMessage()+" killing "+almostTock.getName());
			}
			if(CMLib.threads() instanceof ServiceEngine)
				((ServiceEngine)CMLib.threads()).delTickGroup(almostTock);
			for(int i=0;i<tockClients.size();i++)
			{
				final TickClient c=tockClients.elementAt(i);
				startTickDown(c.getClientObject(),c.getTickID(),c.getTotalTickDown());
			}
		}

		setSupportStatus("Checking mud threads");
		for(int m=0;m<CMLib.hosts().size();m++)
		{
			final List<Runnable> badThreads=CMLib.hosts().get(m).getOverdueThreads();
			for(final Runnable T : badThreads)
			{
				if(T instanceof Thread)
				{
					String threadName=((Thread)T).getName();
					if(T instanceof Tickable)
						threadName=((Tickable)T).name()+" ("+((Tickable)T).ID()+"): "+((Tickable)T).getTickStatus();
					setSupportStatus("Killing "+threadName);
					Log.errOut("Killing stray thread: "+threadName);
					CMLib.killThread((Thread)T,100,1);
				}
				else
					Log.errOut("Unable to kill stray runnable: "+T.toString());
			}
		}
		setSupportStatus("Done checking threads");
	}

	@Override
	public void run()
	{
		while(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			CMLib.s_sleep(1000);
		final CMProps props = CMProps.instance();
		final CMProps.Bool MUDSHUTTINGDOWN=CMProps.Bool.MUDSHUTTINGDOWN;
		while(!props.getBool(MUDSHUTTINGDOWN))
		{
			try
			{
				while(isAllSuspended() && (!props.getBool(MUDSHUTTINGDOWN)))
				{
					if((unsuspendedRunnables!=null)&&(unsuspendedRunnables.length>0))
					{
						Thread.sleep(100);
						for(CMRunnable runnable : unsuspendedRunnables)
						{
							try 
							{
								runnable.run();
							} 
							catch(Exception e) 
							{
								Log.errOut(e);
							}
						}
					}
					else
						Thread.sleep(2000);
				}
				final long now=System.currentTimeMillis();
				globalTickID = (now - globalStartTime) / props.tickMillis();
				long nextWake=now + 3600000;
				if(this.schedTicks.size() > 0)
				{
					final List<CMRunnable> runThese = new LinkedList<CMRunnable>();
					synchronized(this.schedTicks)
					{
						if(this.schedTicks.size() > 0)
						{
							for(Iterator<CMRunnable> r=this.schedTicks.iterator(); r.hasNext();) 
							{
								final CMRunnable R=r.next();
								if(now >= R.getStartTime())
								{
									runThese.add(R);
									r.remove();
								}
								else
								if(R.getStartTime() < nextWake)
									nextWake = R.getStartTime();
							}
						}
					}
					for(CMRunnable R : runThese)
						getPoolExecutor((char)R.getGroupID()).execute(R);
				}
				synchronized(allTicks)
				{
					for(final TickableGroup T : allTicks)
					{
						if(!T.isAwake() && (!getPoolExecutor(T.getThreadGroupName()).isActiveOrQueued(T)))
						{
							if (T.getNextTickTime() <= now)
							{
								if(now + T.getTickInterval() < nextWake)
									nextWake = now + T.getTickInterval();
								getPoolExecutor(T.getThreadGroupName()).execute(T);
							}
							else
							if (nextWake > T.getNextTickTime())
							{
								nextWake = T.getNextTickTime();
							}
						}
						else
						if(now + T.getTickInterval() < nextWake)
							nextWake = now + T.getTickInterval();
					}
				}
				if(nextWake > now)
				{
					nextWakeAtTime = nextWake;
					Thread.sleep(nextWake-now);
				}
			}
			catch(final InterruptedException e)
			{
			}
			catch(final Exception e)
			{
				Log.errOut("Scheduler",e);
			}
		}
		threadPools=new CMThreadPoolExecutor[256];
		drivingThread=null;
		supportClient=null;
		Log.sysOut("ServiceEngine","Scheduler stopped");
	}

	@Override
	public boolean activate()
	{
		getPoolExecutor(null); // will cause the creation

		if(supportClient==null)
		supportClient=startTickDown(null,new Tickable()
		{
			private int tickStatus = Tickable.STATUS_NOT;

				@Override
				public String ID()
				{
					return "THThreads";
				}

				@Override
				public CMObject newInstance()
				{
					return this;
				}

				@Override
				public CMObject copyOf()
				{
					return this;
				}

				@Override
				public void initializeClass()
				{
				}

				@Override
				public int compareTo(CMObject o)
				{
					return (o == this) ? 0 : 1;
				}

				@Override
				public String name()
				{
					return ID();
				}

				@Override
				public int getTickStatus()
				{
					return tickStatus;
				}

				@Override
				public boolean tick(Tickable ticking, int tickID)
				{
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.UTILITHREAD))
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.THREADTHREAD)))
				{
					tickStatus=Tickable.STATUS_ALIVE;
					checkHealth();
					Resources.removeResource("SYSTEM_HASHED_MASKS");
					tickStatus=Tickable.STATUS_NOT;
					setSupportStatus("Sleeping");
				}
				return true;
			}
		}, Tickable.TICKID_MISCELLANEOUS|Tickable.TICKID_SOLITARYMASK, MudHost.TIME_UTILTHREAD_SLEEP, 1);
		if(drivingThread==null)
		{
			drivingThread=new Thread(Thread.currentThread().getThreadGroup(),this,"Scheduler"+Thread.currentThread().getThreadGroup().getName().charAt(0));
			drivingThread.setDaemon(true);
			drivingThread.start();
		}
		return true;
	}
}
