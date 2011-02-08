package com.planet_ink.coffee_mud.core.threads;

import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.planet_ink.coffee_mud.core.CMLib;
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
	protected long timeoutMillis;
	protected CMThreadFactory threadFactory;
	protected PairSVector<Thread,CMRunnable> active = new PairSVector<Thread,CMRunnable>();
	
	public CMThreadPoolExecutor(String poolName,
								int corePoolSize, int maximumPoolSize,
								long keepAliveTime, TimeUnit unit, 
								long timeoutMins, int queueSize) 
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new UniqueEntryBlockingQueue<Runnable>(queueSize));
		timeoutMillis=timeoutMins * 60 * 1000;
		threadFactory=new CMThreadFactory(poolName);
		setThreadFactory(threadFactory);
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
    }
    
	public Collection<CMRunnable> getTimeoutOutRuns()
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
