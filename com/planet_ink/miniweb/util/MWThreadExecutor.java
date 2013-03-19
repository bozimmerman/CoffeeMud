package com.planet_ink.miniweb.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.*;
import java.util.logging.Logger;

/*
Copyright 2012-2013 Bo Zimmerman

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
public class MWThreadExecutor extends ThreadPoolExecutor 
{
	protected HashMap<Runnable,MWRunWrap>active 		= new HashMap<Runnable,MWRunWrap>();
	protected long  				 	 timeoutMillis;
	protected MWThreadFactory   	 	 threadFactory;
	protected String				 	 poolName 		= "Pool";
	protected volatile long 		 	 lastRejectTime = 0;
	protected volatile int  		 	 rejectCount 	= 0;
	protected final Logger				 logger;
	
	public MWThreadExecutor(String poolName,
							MiniWebConfig config,
							int corePoolSize, int maximumPoolSize,
							long keepAliveTime, TimeUnit unit, 
							long timeoutSecs, int queueSize) 
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>(queueSize));
		timeoutMillis=timeoutSecs * 1000L;
		this.poolName=poolName;
		threadFactory=new MWThreadFactory(poolName, config);
		setThreadFactory(threadFactory);
		this.logger=config.getLogger();
	}

	protected void beforeExecute(Thread t, Runnable r) 
	{ 
		synchronized(active)
		{
			try
			{
				active.put(r,new MWRunWrap(r,t));
			}
			catch(Throwable e)
			{
				logger.throwing("", "", e);
			}
		}
	}
	
	protected void afterExecute(Runnable r, Throwable t) 
	{ 
		synchronized(active)
		{
			try
			{
				active.remove(r);
			}
			catch(Throwable e)
			{
				logger.throwing("", "", e);
			}
		}
	}

	public void execute(Runnable r)
	{
		try
		{
			super.execute(r);
			
			// an optomization for logging purposes.  When my smtp server gets hit
			// by a spam-bot, the log fills up my hard drive.  this helps prevent that.
			if((rejectCount>0)&&(System.currentTimeMillis()-lastRejectTime)>5000)
			{
				logger.warning(rejectCount+" Pool_"+poolName+": Threads rejected.");
				rejectCount=0;
			}
		}
		catch(RejectedExecutionException e)
		{
			// a thread is rejected only when the queue is filled.  Look for the blockages!
			Collection<MWRunWrap> runsKilled = getTimeoutOutRuns(1);
			for(MWRunWrap runnable : runsKilled)
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
	public Collection<MWRunWrap> getTimeoutOutRuns(int maxToKill)
	{
		LinkedList<MWRunWrap> timedOut=new LinkedList<MWRunWrap>();
		if(timeoutMillis<=0) return timedOut;
		LinkedList<Thread> killedOut=new LinkedList<Thread>();
		synchronized(active)
		{
			try
			{
				for(Iterator<MWRunWrap> i = active.values().iterator();i.hasNext();)
				{
					final MWRunWrap runnable = i.next();
					if(runnable.activeTimeMillis() > timeoutMillis)
					{
						if(timedOut.size() >= maxToKill)
						{
							MWRunWrap leastWorstOffender=null;
							for(MWRunWrap r : timedOut)
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
						Thread thread = runnable.getThread();
						killedOut.add(thread);
						i.remove();
					}
				}
			}
			catch(Exception e) 
			{ /**/ }
		}
		while(killedOut.size()>0)
		{
			Thread thread = killedOut.remove();
			try
			{
				thread.interrupt();
				//CMLib.killThread(t,100,3);
			}
			catch(Exception e)  
			{ /**/ }
		}
		return timedOut;
	}
}
