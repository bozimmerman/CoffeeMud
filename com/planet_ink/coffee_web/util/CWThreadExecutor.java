package com.planet_ink.coffee_web.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.logging.Logger;

/*
   Copyright 2012-2018 Bo Zimmerman

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

/**
 * Thread executor with a waiting queue plus one extra feature -- a passive system for
 * timing out threads that have been active longer than we want.
 * This system kicks in whenever a thread execute attempt is rejected by the underlying
 * executor due to the queue being full.  In that case, it will look through the active
 * threads to find one that can be timed out, and will interrupt it -- as if that will work. :/
 * @author Bo Zimmerman
 *
 */
public class CWThreadExecutor extends ThreadPoolExecutor 
{
	protected HashMap<Runnable,RunWrap>active 		= new HashMap<Runnable,RunWrap>();
	protected long  				 	 timeoutMillis;
	protected CWThreadFactory   	 	 threadFactory;
	protected String				 	 poolName 		= "Pool";
	protected volatile long 		 	 lastRejectTime = 0;
	protected volatile int  		 	 rejectCount 	= 0;
	protected final Logger				 logger;

	protected static class CMLinkedBlockingQueue<E> extends ArrayBlockingQueue<E>{
		private static final long serialVersionUID = -4357809818979881831L;
		public CWThreadExecutor executor = null;
		public CMLinkedBlockingQueue(int capacity) { super(capacity);}
		@Override public boolean offer(E o)
		{
			final int allWorkingThreads = executor.getActiveCount() + super.size();
			return (allWorkingThreads < executor.getPoolSize()) && super.offer(o);
		}
	}
	
	public CWThreadExecutor(String poolName,
							CWConfig config,
							int corePoolSize, int maximumPoolSize,
							long keepAliveTime, TimeUnit unit, 
							long timeoutSecs, int queueSize) 
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new CMLinkedBlockingQueue<Runnable>(queueSize));
		((CMLinkedBlockingQueue<Runnable>)getQueue()).executor=this;
		timeoutMillis=timeoutSecs * 1000L;
		this.poolName=poolName;
		threadFactory=new CWThreadFactory(poolName, config);
		setThreadFactory(threadFactory);
		this.logger=config.getLogger();
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
			try
			{
				active.put(r,new RunWrap(r,t));
			}
			catch(final Throwable e)
			{
				logger.throwing("", "", e);
			}
		}
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) 
	{ 
		synchronized(active)
		{
			try
			{
				active.remove(r);
			}
			catch(final Throwable e)
			{
				logger.throwing("", "", e);
			}
		}
	}

	@Override
	public int getActiveCount()
	{
		return active.size();
	} 
	
	@Override
	public void execute(Runnable r)
	{
		try
		{
			if(this.getQueue().contains(r))
				return;
			super.execute(r);
			
			// an optomization for logging purposes.  When my smtp server gets hit
			// by a spam-bot, the log fills up my hard drive.  this helps prevent that.
			if((rejectCount>0)&&(System.currentTimeMillis()-lastRejectTime)>5000)
			{
				logger.warning(rejectCount+" Pool_"+poolName+": Threads rejected.");
				rejectCount=0;
			}
		}
		catch(final RejectedExecutionException e)
		{
			// a thread is rejected only when the queue is filled.  Look for the blockages!
			final Collection<RunWrap> runsKilled = getTimeoutOutRuns(1);
			for(final RunWrap runnable : runsKilled)
				logger.severe("Pool_"+poolName+": Old(er) Runnable killed: "+runnable.toString());
			lastRejectTime=System.currentTimeMillis();
			rejectCount++;
		}
	}
	
	/**
	 * Scans the list of active running threads/runnables for ones that have
	 * been active longer than permitted.  It will attempt to kill those, returning
	 * those threads which were timed out.  There's also the funny feature that
	 * it will kill only the number you ask for, starting with the oldest offenders
	 * and working towards the youngest.
	 * @param maxToKill the maximum number of threads to kill
	 * @return the runnable/thread combo that was killed.
	 */
	public Collection<RunWrap> getTimeoutOutRuns(int maxToKill)
	{
		final LinkedList<RunWrap> timedOut=new LinkedList<RunWrap>();
		if(timeoutMillis<=0) return timedOut;
		final LinkedList<Thread> killedOut=new LinkedList<Thread>();
		synchronized(active)
		{
			try
			{
				for(final Iterator<RunWrap> i = active.values().iterator();i.hasNext();)
				{
					final RunWrap runnable = i.next();
					if(runnable.activeTimeMillis() > timeoutMillis)
					{
						if(timedOut.size() >= maxToKill)
						{
							RunWrap leastWorstOffender=null;
							for(final RunWrap r : timedOut)
							{
								if((leastWorstOffender != null)
								&&(r.activeTimeMillis() < leastWorstOffender.activeTimeMillis()))
									leastWorstOffender=r;
							}
							if(leastWorstOffender!=null)
							{
								if(runnable.activeTimeMillis() < leastWorstOffender.activeTimeMillis())
									continue;
								else
									timedOut.remove(leastWorstOffender);
							}
						}
						timedOut.add(runnable);
						final Thread thread = runnable.getThread();
						killedOut.add(thread);
						i.remove();
					}
				}
			}
			catch(final Exception e)
			{ /**/ }
		}
		while(killedOut.size()>0)
		{
			final Thread thread = killedOut.remove();
			try
			{
				thread.interrupt();
				//CMLib.killThread(t,100,3);
			}
			catch(final Exception e)
			{ /**/ }
		}
		return timedOut;
	}
}
