package com.planet_ink.coffee_mud.core.threads;

import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.coffee_mud.core.collections.PairSVector;
import com.planet_ink.coffee_mud.core.collections.SLinkedList;
import com.planet_ink.coffee_mud.core.collections.STreeMap;
import com.planet_ink.coffee_mud.core.collections.STreeSet;
import com.planet_ink.coffee_mud.core.collections.UniqueEntryBlockingQueue;
/* 
Copyright 2000-2011 Bo Zimmerman

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
	protected PairSVector<Thread,CMRunnable> active = new PairSVector<Thread,CMRunnable>();
	protected LinkedList<CMRunnable> waitingQueue = new LinkedList<CMRunnable>();
	protected long 				timeoutMillis;
	protected CMThreadFactory 	threadFactory;
	protected int 			   	queueSize = 0;
	
	public CMThreadPoolExecutor(String poolName,
								int corePoolSize, int maximumPoolSize,
								long keepAliveTime, TimeUnit unit, 
								long timeoutMins, int queueSize) 
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new SynchronousQueue<Runnable>());
		timeoutMillis=timeoutMins * 60 * 1000;
		threadFactory=new CMThreadFactory(poolName);
		setThreadFactory(threadFactory);
		this.queueSize=queueSize;
	}

    protected void beforeExecute(Thread t, Runnable r) 
    { 
    	synchronized(active)
    	{
	    	if(r instanceof CMRunnable)
	    		active.add(t, (CMRunnable)r);
    	}
    }
    
    protected void afterExecute(Runnable r, Throwable t) 
    { 
    	synchronized(active)
    	{
	    	if(r instanceof CMRunnable)
	    		active.removeSecond((CMRunnable)r);
    	}
    	if(waitingQueue.size()>0)
    	{
    		CMRunnable runner=null;
			synchronized(waitingQueue)
			{
		    	if(waitingQueue.size()>0)
					runner=waitingQueue.removeFirst();
			}
			if(runner!=null)
				execute(runner);
    	}
    }

    public void execute(Runnable r)
    {
    	try
    	{
    		super.execute(r);
    	}
    	catch(RejectedExecutionException e)
    	{
    		if(r instanceof CMRunnable)
    		{
	    		Collection<CMRunnable> runsKilled = getTimeoutOutRuns(1);
	    		for(CMRunnable runnable : runsKilled)
		    		Log.errOut("CMThreadPoolExecutor","Old(er) Runnable killed: "+runnable.toString());
				synchronized(waitingQueue)
				{
					if(waitingQueue.contains(r))
						return;
					if(waitingQueue.size() < queueSize)
						waitingQueue.addLast((CMRunnable)r);
					else
			    		Log.errOut("CMThreadPoolExecutor","Thread not executed due to: "+e.getMessage());
				}
    		}
    		Log.errOut("CMThreadPoolExecutor",e.getMessage());
    	}
    }
    
	public Collection<CMRunnable> getTimeoutOutRuns(int maxToKill)
    {
    	LinkedList<CMRunnable> timedOut=new LinkedList<CMRunnable>();
    	if(timeoutMillis<=0) return timedOut;
		LinkedList<Thread> killedOut=new LinkedList<Thread>();
    	synchronized(active)
    	{
	    	try
	    	{
	    		for(Enumeration<Pair<Thread,CMRunnable>> e = active.elements();e.hasMoreElements();)
	    		{
	    			Pair<Thread,CMRunnable> p=e.nextElement();
	    			if(p.second.activeTimeMillis() > timeoutMillis)
	    			{
	    				if(timedOut.size() > maxToKill)
	    				{
	    					CMRunnable leastWorstOffender=null;
	    					for(CMRunnable r : timedOut)
	    						if((leastWorstOffender != null)
	    						&&(r.activeTimeMillis() < leastWorstOffender.activeTimeMillis()))
	    							leastWorstOffender=r;
	    					if(p.second.activeTimeMillis() < leastWorstOffender.activeTimeMillis())
	    						continue;
	    					else
	    						timedOut.remove(leastWorstOffender);
	    				}
		    			timedOut.add(p.second);
		    			killedOut.add(p.first);
	    			}
	    		}
	    	}
	    	catch(Throwable t)
	    	{
	    	}
    	}
    	try
    	{
    		while(killedOut.size()>0)
    		{
				Thread t = killedOut.remove();
				active.remove(t);
				CMLib.killThread(t,100,3);
    		}
    	}
    	catch(Throwable t)
    	{
    	}
    	return timedOut;
    }
}
