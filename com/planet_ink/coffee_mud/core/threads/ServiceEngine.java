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
import com.planet_ink.miniweb.interfaces.HTTPRequest;


/* 
   Copyright 2000-2014 Bo Zimmerman

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
	
	
	private Thread  					 drivingThread=null;
	private TickClient 					 supportClient=null;
	protected SLinkedList<TickableGroup> allTicks=new SLinkedList<TickableGroup>();
	private boolean 					 isSuspended=false;
	private int 						 max_objects_per_thread=0;
	private CMThreadPoolExecutor[]		 threadPools=new CMThreadPoolExecutor[256];
	
	public String ID(){return "ServiceEngine";}
	public String name() { return ID();}
	public CMObject newInstance(){try{return getClass().newInstance();}catch(Exception e){return new ServiceEngine();}}
	
	public void initializeClass() 
	{
	}
	public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public void propertiesLoaded(){}

	public TickClient getServiceClient() 
	{
		return supportClient;
	}
	
	public ServiceEngine() 
	{ 
		initializeClass();
	}

	protected CMThreadPoolExecutor getPoolExecutor(String threadGroupName)
	{
		if(threadGroupName == null)
			threadGroupName = Thread.currentThread().getThreadGroup().getName();
		final char threadGroupNum=threadGroupName.charAt(0);
		final CMThreadPoolExecutor pool = threadPools[threadGroupNum];
		if(pool != null) return pool;
		final int minThreads = CMProps.getIntVar(CMProps.Int.MINWORKERTHREADS);
		int maxThreads = CMProps.getIntVar(CMProps.Int.MAXWORKERTHREADS);
		if(maxThreads<=0) maxThreads=Integer.MAX_VALUE;
		final String sessionThreadGroupName="Worker"+threadGroupNum;
		threadPools[threadGroupNum] = new CMThreadPoolExecutor(sessionThreadGroupName,minThreads, maxThreads, CMProps.getTickMillis()*2, TimeUnit.MILLISECONDS, (LONG_TICK_TIMEOUT/60000), 1024);
		threadPools[threadGroupNum].setThreadFactory(new CMThreadFactory(sessionThreadGroupName));
		return threadPools[threadGroupNum];
	}
	
	public Iterator<TickableGroup> tickGroups()
	{
		return allTicks.iterator();
	}
	
	public Runnable findRunnableByThread(final Thread thread)
	{
		if((thread==null)||(threadPools==null))
			return null;
		final Runnable possR=(thread instanceof CMFactoryThread)?((CMFactoryThread)thread).getRunnable():null;
		for(int i=0;i<threadPools.length;i++)
		{
			final CMThreadPoolExecutor executor=threadPools[i];
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
				for(Map.Entry<Runnable, Thread> e : executor.active.entrySet())
				{
					if(e.getValue()==thread)
						return e.getKey();
				}
			}
		}
		return null;
	}
	
	public void executeRunnable(String threadGroupName, Runnable R)
	{
		try
		{
			getPoolExecutor(threadGroupName).execute(R);
		}
		catch(Exception e)
		{
			Log.errOut("ServiceEngine","ExecRun: "+e.getMessage());
			Log.debugOut("ServiceEngine",e);
		}
	}
	
	public void executeRunnable(Runnable R)
	{
		try
		{
			getPoolExecutor(null).execute(R);
		}
		catch(Exception e)
		{
			Log.errOut("ServiceEngine","ExecRun: "+e.getMessage());
			Log.debugOut("ServiceEngine",e);
		}
	}
	
	public int getMaxObjectsPerThread()
	{
		if(max_objects_per_thread>0) return max_objects_per_thread;
		max_objects_per_thread = CMProps.getIntVar(CMProps.Int.OBJSPERTHREAD);
		if(max_objects_per_thread>0) return max_objects_per_thread; 
		max_objects_per_thread=0;
		return 128;
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
	
	public TickClient startTickDown(Tickable E, int tickID, int numTicks)
	{ 
		return startTickDown(E,tickID,CMProps.getTickMillis(),numTicks); 
	}
	
	public TickClient startTickDown(Tickable E, int tickID, long TICK_TIME, int numTicks)
	{
		return startTickDown(CMLib.map().getOwnedThreadGroup(E),E,tickID,TICK_TIME,numTicks); 
	}
	
	public synchronized TickClient startTickDown(ThreadGroup group, Tickable E, int tickID, long TICK_TIME, int numTicks)
	{
		TickableGroup tock=null;
		if(group==null)
			group=Thread.currentThread().getThreadGroup();
		char threadGroupNum=group.getName().charAt(0);
		for(TickableGroup almostTock : allTicks)
		{
			if(almostTock.contains(E,tickID)) 
				return null;
			if((tock==null)
			&&(almostTock.getTickInterval()==TICK_TIME)
			&&(!almostTock.isSolitaryTicker())
			&&(almostTock.numTickers()<getMaxObjectsPerThread()))
			{
				final String name = almostTock.getThreadGroupName();
				if((name!=null)
				&&(name.charAt(0)==threadGroupNum))
					tock=almostTock;
			}
		}
		boolean isSolitary = ((tickID&Tickable.TICKID_SOLITARYMASK)==Tickable.TICKID_SOLITARYMASK); 
		if((tock==null)||isSolitary)
		{
			tock=new StdTickGroup(this,TICK_TIME, Thread.currentThread().getThreadGroup().getName(), isSolitary);
			addTickGroup(tock);
		}

		TickClient newC=new StdTickClient(E,numTicks,tickID);
		tock.addTicker(newC);
		return newC;
	}

	public synchronized boolean deleteTick(Tickable E, int tickID)
	{
		for(final TickableGroup almostTock : allTicks)
		{
			final Iterator<TickClient> set=almostTock.getTickSet(E,tickID);
			for(;set.hasNext();)
				almostTock.delTicker(set.next());
		}
		return false;
	}

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

	public boolean isAllSuspended(){return isSuspended;}
	public void suspendAll(){isSuspended=true;}
	public void resumeAll(){isSuspended=false;}
	public void suspendTicking(Tickable E, int tickID){suspendResumeTicking(E,tickID,true);}
	public void resumeTicking(Tickable E, int tickID){suspendResumeTicking(E,tickID,false);}
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
	
	public String systemReport(final String itemCode)
	{
		long totalMOBMillis=0;
		long totalMOBTicks=0;
		long topMOBMillis=0;
		long topMOBTicks=0;
		MOB topMOBClient=null;
		for(Session S : CMLib.sessions().localOnlineIterable())
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
			int x=itemCode.indexOf('-');
			int total=10;
			if(x>0)
				total=CMath.s_int(itemCode.substring(x+1));
			Vector<Triad<Long,Integer,Integer>> list=new Vector<Triad<Long,Integer,Integer>>();
			int group=0;
			for(Iterator<TickableGroup> e=tickGroups();e.hasNext();)
			{
				final TickableGroup almostTock=e.next();
				int tick=0;
				for(Iterator<TickClient> et=almostTock.tickers();et.hasNext();)
				{
					TickClient C=et.next();
					if(C.getTickTotal()==0) continue;
					Long avg=Long.valueOf(C.getMilliTotal()/C.getTickTotal());
					int i=0;
					for(;i<list.size();i++)
					{
						Triad<Long,Integer,Integer> t=list.get(i);
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
			StringBuilder str=new StringBuilder("");
			for(Triad<Long,Integer,Integer> t : list)
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
		for(Iterator<TickableGroup> e=tickGroups();e.hasNext();)
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
				for(Iterator<TickClient> et=almostTock.tickers();et.hasNext();)
				{
					TickClient C=et.next();
					if(C.getMilliTotal()>topObjectMillis)
					{
						topObjectMillis=C.getMilliTotal();
						topObjectTicks=C.getTickTotal();
						topObjectClient=C.getClientObject();
						topObjectGroup=num;
					}
				}
			}
			catch(NoSuchElementException ex)
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
			int xstart="thread".length();
			int xend=xstart;
			while((xend<itemCode.length())&&(Character.isDigit(itemCode.charAt(xend))))
				xend++;
			int threadNum=CMath.s_int(itemCode.substring(xstart,xend));
			int curThreadNum=0;
			for(Enumeration<CMLibrary> e=CMLib.libraries();e.hasMoreElements();)
			{
				CMLibrary lib=e.nextElement();
				TickClient serviceClient=lib.getServiceClient();
				if(serviceClient!=null) {
					if(curThreadNum==threadNum) {
						String instrCode=itemCode.substring(xend);
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

	public void rejuv(Room here, int tickID)
	{
		TickableGroup almostTock=null;
		TickClient C=null;
		Tickable E2=null;
		boolean doItems=((tickID<=0)||(tickID==Tickable.TICKID_ROOM_ITEM_REJUV));
		boolean doMobs=((tickID<=0)||(tickID==Tickable.TICKID_MOB));
		for(Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			try
			{
				for(Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
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
			catch(NoSuchElementException ex)
			{
			}
		}
	}
	
	
	public void tickAllTickers(Room here)
	{
		TickableGroup almostTock=null;
		TickClient C=null;
		Tickable E2=null;
		final WorldMap map=CMLib.map();
		for(Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			try
			{
				for(Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
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
			catch(NoSuchElementException ex)
			{
			}
		}
	}

	public void suspendResumeRecurse(CMObject O, boolean skipEmbeddedAreas, boolean suspend)
	{
		if(O instanceof Item)
		{
			Item I=(Item)O;
			suspendResumeTicking(I, -1, suspend);
			if((I instanceof SpaceShip)&&(!skipEmbeddedAreas))
				suspendResumeRecurse(((SpaceShip)I).getShipArea(),true,suspend);
		}
		else
		if(O instanceof MOB)
		{
			MOB M=(MOB)O;
			for(int i=0;i<M.numItems();i++)
			{
				Item I=M.getItem(i);
				suspendResumeRecurse(I,skipEmbeddedAreas,suspend);
			}
			final PlayerStats pStats=M.playerStats();
			if(pStats!=null)
			{
				ItemCollection collection=pStats.getExtItems();
				if(collection!=null)
				for(int i=0;i<collection.numItems();i++)
				{
					Item I=collection.getItem(i);
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
			Room R=(Room)O;
			suspendResumeTicking(R, -1, suspend);
			for(int i=0;i<R.numInhabitants();i++)
				suspendResumeRecurse(R.fetchInhabitant(i),skipEmbeddedAreas,suspend);
			for(int i=0;i<R.numItems();i++)
				suspendResumeRecurse(R.getItem(i),skipEmbeddedAreas,suspend);
		}
		else
		if(O instanceof Area)
		{
			Area A=(Area)O;
			A.setAreaState(suspend?Area.State.FROZEN:Area.State.ACTIVE);
			for(Enumeration<Room> r=A.getFilledProperMap();r.hasMoreElements();)
				suspendResumeRecurse(r.nextElement(),skipEmbeddedAreas,suspend);
		}
	}
	
	public void deleteAllTicks(Tickable ticker)
	{
		if(ticker==null)
			return;
		deleteTick(ticker, -1);
		WorldMap map=CMLib.map();
		if(ticker instanceof Room)
		{
			TickableGroup almostTock=null;
			TickClient C=null;
			Tickable E2=null;
			for(Iterator<TickableGroup> e=tickGroups();e.hasNext();)
			{
				almostTock=e.next();
				try
				{
					for(Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
					{
						C=i.next();
						E2=C.getClientObject();
						if(map.isHere(E2,(Room)ticker))
							almostTock.delTicker(C);
						else
						if((E2 instanceof Ability)
						&&(map.isHere(((Ability)E2).affecting(),(Room)ticker)))
							almostTock.delTicker(C);
						else
							continue;
						
						try
						{
							if(almostTock.numTickers()==0)
								allTicks.remove(almostTock);
						}
						catch(Exception e2) {}
					}
				}
				catch(NoSuchElementException ex)
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
			for(Iterator<TickableGroup> e=tickGroups();e.hasNext();)
			{
				almostTock=e.next();
				try
				{
					for(Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
					{
						C=i.next();
						E2=C.getClientObject();
						if(map.isHere(E2,(Area)ticker))
							almostTock.delTicker(C);
						else
						if((E2 instanceof Ability)
						&&(map.isHere(((Ability)E2).affecting(),(Area)ticker)))
							almostTock.delTicker(C);
						else
							continue;
						
						try
						{
							if(almostTock.numTickers()==0)
								allTicks.remove(almostTock);
						}
						catch(Exception e2) {}
					}
				}
				catch(NoSuchElementException ex)
				{
				}
			}
		}
	}
	
	public String tickInfo(String which)
	{
		int grpstart=-1;
		for(int i=0;i<which.length();i++)
			if(Character.isDigit(which.charAt(i)))
			{
				grpstart=i;
				break;
			}
		if(which.equalsIgnoreCase("tickGroupSize"))
			return ""+allTicks.size();
		else
		if(which.toLowerCase().startsWith("tickerssize"))
		{
			if(grpstart<0) return"";
			int group=CMath.s_int(which.substring(grpstart));
			if((group>=0)&&(group<allTicks.size()))
			{
				List<TickableGroup> enumeratedTicks=new XVector<TickableGroup>(allTicks);
				return ""+enumeratedTicks.get(group).numTickers();
			}
			return "";
		}
		int group=-1;
		int client=-1;
		int clistart=which.indexOf('-');
		if((grpstart>=0)&&(clistart>grpstart))
		{
			group=CMath.s_int(which.substring(grpstart,clistart));
			client=CMath.s_int(which.substring(clistart+1));
		}

		if((group<0)||(client<0)||(group>=allTicks.size())) return "";
		List<TickableGroup> enumeratedTicks=new XVector<TickableGroup>(allTicks);
		if((group<0)||(client<0)||(group>=enumeratedTicks.size())) return "";
		TickableGroup almostTock=enumeratedTicks.get(group);
		
		if(client>=almostTock.numTickers()) return "";
		TickClient C=almostTock.fetchTickerByIndex(client);
		if(C==null) return "";

		if(which.toLowerCase().startsWith("tickername"))
		{
			Tickable E=C.getClientObject();
			if((E instanceof Ability)&&(E.ID().equals("ItemRejuv")))
				E=((Ability)E).affecting();
			if(E instanceof Room)
				return CMLib.map().getExtendedRoomID((Room)E);
			if(E!=null) return E.name();
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
			if(C.getTickTotal()==0) return "0";
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

	public boolean shutdown() 
	{
		//int numTicks=tickGroup.size();
		while(allTicks.size()>0)
		{
			//Log.sysOut("ServiceEngine","Shutting down all tick "+which+"/"+numTicks+"...");
			TickableGroup tock=allTicks.getFirst();
			if(tock!=null)
			{
				CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...shutting down Service Engine: killing "+tock.getName()+": "+tock.getStatus());
				tock.shutdown();
				allTicks.remove(tock);
			}
			try{Thread.sleep(100);}catch(Exception e){}
		}
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...shutting down Service Engine: "+ID()+": thread shutdown");
		CMLib.killThread(drivingThread,100,10);
		// force final time tick!
		Vector<TimeClock> timeObjects=new Vector<TimeClock>();
		for(Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			Area A=(e.nextElement());
			if(!timeObjects.contains(A.getTimeObj()))
				timeObjects.addElement(A.getTimeObj());
		}
		CMProps.setUpAllLowVar(CMProps.Str.MUDSTATUS,"Shutting down...shutting down Service Engine: "+ID()+": saving time objects");
		for(int t=0;t<timeObjects.size();t++)
			timeObjects.elementAt(t).save();
		for(CMThreadPoolExecutor pool : threadPools)
			if(pool != null)
			{
				pool.shutdown();
				try { pool.awaitTermination(2, TimeUnit.SECONDS); } catch (InterruptedException e) {
					pool.shutdownNow();
					try { pool.awaitTermination(3, TimeUnit.SECONDS); } catch (InterruptedException e2) {}
				}
			}
		Log.sysOut("ServiceEngine","Shutdown complete.");
		return true;
	}

	public synchronized void clearDebri(Room room, int taskCode)
	{
		TickableGroup almostTock=null;
		TickClient C=null;
		ItemTicker  I=null;
		Iterator<TickClient> roomSet;
		MOB mob=null;
		for(Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			roomSet=almostTock.getLocalItems(taskCode,room);
			if(roomSet!=null)
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
						if((mob.isMonster())&&(!room.isInhabitant(mob)))
						{
							mob.destroy();
							almostTock.delTicker(C);
						}
					}
				}
		}
	}
	
	public List<Tickable> getNamedTickingObjects(String name)
	{
		Vector<Tickable> V=new Vector<Tickable>();
		TickableGroup almostTock=null;
		TickClient C=null;
		name=name.toUpperCase().trim();
		for(Iterator<TickableGroup> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			for(Iterator<TickClient> i=almostTock.tickers();i.hasNext();)
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
	
	public String getTickStatusSummary(Tickable obj)
	{
		if(obj==null) return "";
		long code=obj.getTickStatus();
		if(obj instanceof Environmental)
		{
			if(CMath.bset(code,Tickable.STATUS_BEHAVIOR) && (obj instanceof PhysicalAgent))
			{
				long b=(code-Tickable.STATUS_BEHAVIOR);
				String codeWord="Behavior #"+b;
				if((b>=0)&&(b<((PhysicalAgent)obj).numBehaviors()))
				{
					Behavior B=((PhysicalAgent)obj).fetchBehavior((int)b);
					codeWord+=" ("+B.name()+": "+B.getTickStatus();
				}
				else
					codeWord+=" (#Error#)";
				return codeWord;
			}
			else
			if(CMath.bset(code,Tickable.STATUS_SCRIPT)&&(obj instanceof MOB))
			{
				long b=(code-Tickable.STATUS_SCRIPT);
				String codeWord="Script #"+b;
				if((b>=0)&&(b<((MOB)obj).numScripts()))
				{
					ScriptingEngine S=((MOB)obj).fetchScript((int)b);
					codeWord+=" ("+CMStrings.limit(S.getScript(),20)+": "+S.getTickStatus();
				}
				return codeWord;
			}
			else
			if((code&STATUS_ALLMISCTICKS)>0)
			{
				long base=(code&STATUS_ALLMISCTICKS);
				int num=0;
				for(int i=1;i<6;i++)
					if((1<<(10+i))==base)
					{ num=i; break;}
				return "Misc"+num+" Activity #"+(code-base);
			}
			else
			if(CMath.bset(code,Tickable.STATUS_AFFECT) && (obj instanceof Physical))
			{
				long b=(code-Tickable.STATUS_AFFECT);
				String codeWord="Effect #"+b;
				if((b>=0)&&(b<((Physical)obj).numEffects()))
				{
					Environmental E=((Physical)obj).fetchEffect((int)b);
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
		case (int)Tickable.STATUS_ALIVE:
			codeWord="Alive"; break;
		case (int)Tickable.STATUS_REBIRTH:
			codeWord="Rebirth"; break;
		case (int)Tickable.STATUS_CLASS:
			codeWord="Class"; break;
		case (int)Tickable.STATUS_DEAD:
			codeWord="Dead"; break;
		case (int)Tickable.STATUS_END:
			codeWord="End"; break;
		case (int)Tickable.STATUS_FIGHT:
			codeWord="Fighting"; break;
		case (int)Tickable.STATUS_NOT:
			codeWord="!"; break;
		case (int)Tickable.STATUS_OTHER:
			codeWord="Other"; break;
		case (int)Tickable.STATUS_RACE:
			codeWord="Race"; break;
		case (int)Tickable.STATUS_START:
			codeWord="Start"; break;
		case (int)Tickable.STATUS_WEATHER:
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
			if(CMSecurity.isDebugging(CMSecurity.DbgFlag.UTILITHREAD)) Log.debugOut("ServiceEngine",s);
		}
	}
	
	public void debugDumpStack(final String ID, Thread theThread)
	{
		// I wish Java had compiler directives.  Would be great to un-comment this for 1.5 JVMs
		StringBuffer dump = new StringBuffer("");
		if(theThread == null)
			dump.append("NULL!!");
		else
		{
			java.lang.StackTraceElement[] s=theThread.getStackTrace();
			for(int i=0;i<s.length;i++)
				dump.append("\n   "+s[i].getClassName()+": "+s[i].getMethodName()+"("+s[i].getFileName()+": "+s[i].getLineNumber()+")");
		}
		Log.debugOut(ID,dump.toString());
	}
	
	
	public final void checkHealth()
	{
		long lastDateTime=System.currentTimeMillis()-SHORT_TICK_TIMEOUT;
		long longerDateTime=System.currentTimeMillis()-LONG_TICK_TIMEOUT;
		setSupportStatus("checking");

		setSupportStatus("checking tick groups.");
		DVector orderedDeaths=new DVector(3);
		try
		{
			TickableGroup almostTock = null;
			for(Iterator<TickableGroup> e=tickGroups();e.hasNext();)
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
							StringBuffer str=null;
							long code=ticker.getTickStatus();
							String codeWord=getTickStatusSummary(ticker);
							String msg=null;
							if(ticker instanceof Environmental)
								str=new StringBuffer("LOCKED GROUP "+almostTock.getName()+": "+almostTock.getStatus()+": "+ticker.name()+" ("+((Environmental)ticker).ID()+") @"+CMLib.time().date2String(tickClient.getLastStartTime())+", status("+code+" ("+codeWord+"), tickID "+tickClient.getTickID());
							else
								str=new StringBuffer("LOCKED GROUP "+almostTock.getName()+": "+almostTock.getStatus()+": "+ticker.name()+", status ("+code+"/"+codeWord+") @"+CMLib.time().date2String(tickClient.getLastStartTime())+", tickID "+tickClient.getTickID());
	
							if((ticker instanceof MOB)&&(((MOB)ticker).location()!=null))
								msg=str.toString()+" in "+((MOB)ticker).location().roomID();
							else
							if((ticker instanceof Item)&&(((Item)ticker).owner()!=null)&&(((Item)ticker).owner() instanceof Room))
								msg=str.toString()+" in "+((Room)((Item)ticker).owner()).roomID();
							else
							if((ticker instanceof Item)&&(((Item)ticker).owner()!=null)&&(((Item)ticker).owner() instanceof MOB))
								msg=str.toString()+" owned by "+((MOB)((Item)ticker).owner()).name();
							else
							if(ticker instanceof Room)
								msg=str.toString()+" is "+((Room)ticker).roomID();
							else
								msg=str.toString();
							insertOrderDeathInOrder(orderedDeaths,tickClient.getLastStartTime(),msg,almostTock);
						}
					}
					// no isDEBUGGING check -- just always let her rip.
					debugDumpStack(Thread.currentThread().getName(),almostTock.getCurrentThread());
				}
			}
		}
		catch(java.util.NoSuchElementException e){}
		for(int i=0;i<orderedDeaths.size();i++)
			Log.errOut(Thread.currentThread().getName(),(String)orderedDeaths.elementAt(i,2));
			
		setSupportStatus("killing tick groups.");
		for(int x=0;x<orderedDeaths.size();x++)
		{
			TickableGroup almostTock=(TickableGroup)orderedDeaths.elementAt(x,3);
			Vector<TickClient> tockClients=new Vector<TickClient>();
			try{
				for(Iterator<TickClient> e=almostTock.tickers();e.hasNext();)
					tockClients.addElement(e.next());
			}catch(NoSuchElementException e){}
			try
			{
				almostTock.shutdown();
			}
			catch(java.lang.ThreadDeath d)
			{
				Log.errOut("ThreadDeath killing "+almostTock.getName());
			}
			catch(Throwable t)
			{
				Log.errOut("Error "+t.getMessage()+" killing "+almostTock.getName());
			}
			if(CMLib.threads() instanceof ServiceEngine)
				((ServiceEngine)CMLib.threads()).delTickGroup(almostTock);
			for(int i=0;i<tockClients.size();i++)
			{
				TickClient c=tockClients.elementAt(i);
				startTickDown(c.getClientObject(),c.getTickID(),c.getTotalTickDown());
			}
		}

		setSupportStatus("Checking mud threads");
		for(int m=0;m<CMLib.hosts().size();m++)
		{
			List<Runnable> badThreads=CMLib.hosts().get(m).getOverdueThreads();
			for(Runnable T : badThreads)
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
		
		setSupportStatus("Done checking threads");
	}

	public void run()
	{
		while(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			try{Thread.sleep(1000);}catch(Exception e){}
		while(!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)) {
			try
			{
				while(isAllSuspended() && (!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
					Thread.sleep(2000);
				final long now=System.currentTimeMillis();
				long nextWake=System.currentTimeMillis() + 3600000;
				synchronized(allTicks) 
				{
					for(final TickableGroup T : allTicks) 
					{
						if(!T.isAwake() && (!getPoolExecutor(T.getThreadGroupName()).isActiveOrQueued(T))) 
						{
							if (T.getNextTickTime() <= now) {
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
				if(nextWake > now) {
					Thread.sleep(nextWake-now);
				}
			}
			catch(InterruptedException e) 
			{
			}
			catch(Exception e) 
			{
				Log.errOut("Scheduler",e);
			}
		}
		threadPools=new CMThreadPoolExecutor[256];
		drivingThread=null;
		supportClient=null;
		Log.sysOut("ServiceEngine","Scheduler stopped");
	}
	
	public boolean activate() 
	{
		getPoolExecutor(null); // will cause the creation
		
		if(supportClient==null)
		supportClient=startTickDown(null,new Tickable(){
			private long tickStatus = Tickable.STATUS_NOT;
			@Override public String ID() { return "THThreads"; }
			@Override public CMObject newInstance() { return this; }
			@Override public CMObject copyOf() { return this; }
			@Override public void initializeClass() {}
			@Override public int compareTo(CMObject o) { return (o==this)?0:1;}
			@Override public String name() { return ID(); }
			@Override public long getTickStatus() { return tickStatus; }
			@Override public boolean tick(Tickable ticking, int tickID) {
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
