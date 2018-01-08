package com.planet_ink.coffee_mud.core.threads;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.planet_ink.coffee_mud.Common.interfaces.Session;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.coffee_mud.core.collections.PairSVector;
import com.planet_ink.coffee_mud.core.collections.SLinkedList;
import com.planet_ink.coffee_mud.core.collections.STreeMap;
import com.planet_ink.coffee_mud.core.collections.STreeSet;
import com.planet_ink.coffee_mud.core.collections.UniqueEntryBlockingQueue;
import com.planet_ink.coffee_mud.core.interfaces.TickableGroup;
/*
   Copyright 2010-2018 Bo Zimmerman

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
public class CMThreadPoolExecutor extends ThreadPoolExecutor
{
	protected Map<Runnable,Thread>	active = new HashMap<Runnable,Thread>();
	protected long  				timeoutMillis;
	protected CMThreadFactory		threadFactory;
	protected int   				queueSize = 0;
	protected String				poolName = "Pool";
	protected volatile long 		lastRejectTime = 0;
	protected volatile int  		rejectCount = 0;

	protected static class CMArrayBlockingQueue<E> extends ArrayBlockingQueue<E>{
		private static final long serialVersionUID = -4557809818979881831L;
		public CMThreadPoolExecutor executor = null;
		public CMArrayBlockingQueue(int capacity) { super(capacity);}
		@Override public boolean offer(E o)
		{
			final int allWorkingThreads = executor.getActiveCount() + super.size();
			return (allWorkingThreads < executor.getPoolSize()) && super.offer(o);
		}
	}

	public CMThreadPoolExecutor(String poolName,
								int corePoolSize, int maximumPoolSize,
								long keepAliveTime, TimeUnit unit,
								long timeoutMins, int queueSize)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new CMArrayBlockingQueue<Runnable>(queueSize));
		((CMArrayBlockingQueue<Runnable>)this.getQueue()).executor=this;
		timeoutMillis=timeoutMins * 60 * 1000;
		this.poolName=poolName;
		threadFactory=new CMThreadFactory(poolName);
		setThreadFactory(threadFactory);
		this.queueSize=queueSize;
		setRejectedExecutionHandler(new RejectedExecutionHandler()
		{
			@Override public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
			{
				try { executor.getQueue().put(r); } catch (final InterruptedException e) { throw new RejectedExecutionException(e); }
			}
		});
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r)
	{
		synchronized(active)
		{
			if(t instanceof CMFactoryThread)
				((CMFactoryThread)t).setRunnable(r);
			active.put(r,t);
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t)
	{
		synchronized(active)
		{
			final Thread th=active.get(r);
			if(th instanceof CMFactoryThread)
				((CMFactoryThread)th).setRunnable(null);
			active.remove(r);
		}
	}

	@Override
	public int getActiveCount()
	{
		return active.size();
	}

	public boolean isActive(Runnable r)
	{
		return active.containsKey(r);
	}

	public boolean isActiveOrQueued(Runnable r)
	{
		return active.containsKey(r) || getQueue().contains(r);
	}

	@Override
	public void execute(Runnable r)
	{
		try
		{
			if(this.getQueue().contains(r))
				return;
			super.execute(r);
			if((rejectCount>0)&&(System.currentTimeMillis()-lastRejectTime)>5000)
			{
				Log.warnOut(rejectCount+" Pool_"+poolName,"Threads rejected.");
				rejectCount=0;
			}
		}
		catch(final RejectedExecutionException e)
		{
			if(r instanceof CMRunnable)
			{
				final Collection<CMRunnable> runsKilled = getTimeoutOutRuns(1);
				for(final CMRunnable runnable : runsKilled)
				{
					if(runnable instanceof Session)
					{
						final Session S=(Session)runnable;
						final StringBuilder sessionInfo=new StringBuilder("");
						sessionInfo.append("status="+S.getStatus()+" ");
						sessionInfo.append("active="+S.activeTimeMillis()+" ");
						sessionInfo.append("online="+S.getMillisOnline()+" ");
						sessionInfo.append("lastloop="+(System.currentTimeMillis()-S.getInputLoopTime())+" ");
						sessionInfo.append("addr="+S.getAddress()+" ");
						sessionInfo.append("mob="+((S.mob()==null)?"null":S.mob().Name()));
						Log.errOut("Pool_"+poolName,"Timed-Out Runnable: "+sessionInfo.toString());
					}
					else
					if(runnable instanceof TickableGroup)
					{
						final TickableGroup G=(TickableGroup)runnable;
						Log.errOut("Pool_"+poolName,"Timed-Out Runnable: "+G.getName()+"-"+G.getStatus()+"\n\r");
					}
					else
						Log.errOut("Pool_"+poolName,"Timed-Out Runnable: "+runnable.toString());
				}
			}
			lastRejectTime=System.currentTimeMillis();
			rejectCount++;
		}
	}

	public Collection<CMRunnable> getTimeoutOutRuns(int maxToKill)
	{
		final LinkedList<CMRunnable> timedOut=new LinkedList<CMRunnable>();
		if(timeoutMillis<=0)
			return timedOut;
		final LinkedList<Thread> killedOut=new LinkedList<Thread>();
		synchronized(active)
		{
			try
			{
				for (final Runnable runnable : active.keySet())
				{
					if(runnable instanceof CMRunnable)
					{
						final CMRunnable cmRunnable=(CMRunnable)runnable;
						final Thread thread=active.get(runnable);
						if(cmRunnable.activeTimeMillis() > timeoutMillis)
						{
							if(timedOut.size() >= maxToKill)
							{
								CMRunnable leastWorstOffender=null;
								for(final CMRunnable r : timedOut)
								{
									if((leastWorstOffender != null)
									&&(r.activeTimeMillis() < leastWorstOffender.activeTimeMillis()))
										leastWorstOffender=r;
								}
								if(leastWorstOffender!=null)
								{
									if(cmRunnable.activeTimeMillis() < leastWorstOffender.activeTimeMillis())
										continue;
									else
										timedOut.remove(leastWorstOffender);
								}
							}
							timedOut.add(cmRunnable);
							killedOut.add(thread);
						}
					}
				}
			}
			catch(final Exception e)
			{
			}
		}
		try
		{
			while(killedOut.size()>0)
			{
				final Thread t = killedOut.remove();
				active.remove(t);
				CMLib.killThread(t,100,3);
			}
		}
		catch(final Exception e)
		{
		}
		return timedOut;
	}
}
