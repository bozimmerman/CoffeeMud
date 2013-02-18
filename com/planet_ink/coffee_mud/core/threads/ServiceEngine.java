package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.http.ProcessHTTPrequest;
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
public class ServiceEngine implements ThreadEngine
{
	public static final long STATUS_ALLMISCTICKS=Tickable.STATUS_MISC|Tickable.STATUS_MISC2|Tickable.STATUS_MISC3|Tickable.STATUS_MISC4|Tickable.STATUS_MISC5|Tickable.STATUS_MISC6;
	
	private CMSupportThread  		thread=null;
	protected SLinkedList<Tick> 	ticks=new SLinkedList<Tick>();
	private boolean 				isSuspended=false;
	private int 					max_objects_per_thread=0;
	private CMThreadPoolExecutor[]	threadPools=new CMThreadPoolExecutor[256];
	
	public String ID(){return "ServiceEngine";}
	public String name() { return ID();}
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new ServiceEngine();}}
	
	public void initializeClass() 
	{
	}
	public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public void propertiesLoaded(){}
	public CMSupportThread getSupportThread() { return thread;}
	
	public ServiceEngine() 
	{ 
		initializeClass();
	}

	protected CMThreadPoolExecutor getPoolExecutor()
	{
		final char threadGroupNum=Thread.currentThread().getThreadGroup().getName().charAt(0);
		final CMThreadPoolExecutor pool = threadPools[threadGroupNum];
		if(pool != null) return pool;
		final int minThreads = CMProps.getIntVar(CMProps.SYSTEMI_MINSESSIONTHREADS);
		int maxThreads = CMProps.getIntVar(CMProps.SYSTEMI_MAXSESSIONTHREADS);
		if(maxThreads<=0) maxThreads=Integer.MAX_VALUE;
		final String sessionThreadGroupName="Sess"+threadGroupNum;
		threadPools[threadGroupNum] = new CMThreadPoolExecutor(sessionThreadGroupName,minThreads, maxThreads, 5, TimeUnit.MINUTES, 5, 1024);
		threadPools[threadGroupNum].setThreadFactory(new CMThreadFactory(sessionThreadGroupName));
		return threadPools[threadGroupNum];
	}
	
	public Iterator<Tick> tickGroups()
	{
		return ticks.iterator();
	}
	
	public void executeRunnable(Runnable R)
	{
		try
		{
			getPoolExecutor().execute(R);
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
		max_objects_per_thread = CMProps.getIntVar(CMProps.SYSTEMI_OBJSPERTHREAD);
		if(max_objects_per_thread>0) return max_objects_per_thread; 
		max_objects_per_thread=0;
		return 128;
	}
	
	protected void delTickGroup(Tick tock)
	{
		ticks.remove(tock);
	}
	protected void addTickGroup(Tick tock)
	{
		if(!ticks.contains(tock))
			ticks.add(tock);
	}
	
	public void startTickDown(Tickable E, int tickID, int numTicks)
	{ 
		startTickDown(E,tickID,CMProps.getTickMillis(),numTicks); 
	}
	
	public synchronized void startTickDown(Tickable E, int tickID, long TICK_TIME, int numTicks)
	{
		Tick tock=null;
		ThreadGroup grp=null;
		char threadGroupNum=Thread.currentThread().getThreadGroup().getName().charAt(0);
		for(Tick almostTock : ticks)
		{
			if(almostTock.contains(E,tickID)) 
				return;
			if((tock==null)
			&&(almostTock.TICK_TIME==TICK_TIME)
			&&(!almostTock.solitaryTicker)
			&&(almostTock.numTickers()<getMaxObjectsPerThread()))
			{
				grp = almostTock.getThreadGroup();
				if((grp!=null)
				&&(grp.getName().charAt(0)==threadGroupNum))
					tock=almostTock;
			}
		}
		boolean isSolitary = ((tickID&65536)==65536); 
		if((tock==null)||isSolitary)
		{
			tock=new Tick(TICK_TIME);
			tock.solitaryTicker = isSolitary;
			addTickGroup(tock);
		}

		tock.addTicker(new TockClient(E,numTicks,tickID));
	}

	public synchronized boolean deleteTick(Tickable E, int tickID)
	{
		for(final Tick almostTock : ticks)
		{
			final Iterator<TockClient> set=almostTock.getTickSet(E,tickID);
			for(;set.hasNext();)
				almostTock.delTicker((TockClient)set.next());
		}
		return false;
	}

	public boolean isTicking(Tickable E, int tickID)
	{
		for(final Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			final Tick almostTock=e.next();
			final Iterator<TockClient> set=almostTock.getTickSet(E,tickID);
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
		for(final Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			final Tick almostTock=e.next();
			final Iterator<TockClient> set=almostTock.getTickSet(E,tickID);
			for(;set.hasNext();)
				((TockClient)set.next()).suspended=suspend;
		}
		return false;
	}
	
	public boolean isSuspended(Tickable E, int tickID)
	{
		for(final Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			final Tick almostTock=e.next();
			final Iterator<TockClient> set=almostTock.getTickSet(E,tickID);
			if(set.hasNext() && ((TockClient)set.next()).suspended)
				return true;
		}
		return false;
	}
	

	public boolean isHere(Tickable E2, Room here)
	{
		if(E2==null)
			return false;
		else
		if(E2==here)
			return true;
		else
		if((E2 instanceof MOB)
		&&(((MOB)E2).location()==here))
			return true;
		else
		if((E2 instanceof Item)
		&&(((Item)E2).owner()==here))
			return true;
		else
		if((E2 instanceof Item)
		&&(((Item)E2).owner()!=null)
		&&(((Item)E2).owner() instanceof MOB)
		&&(((MOB)((Item)E2).owner()).location()==here))
			return true;
		else
		if(E2 instanceof Exit)
		{
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if(here.getRawExit(d)==E2)
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
			for(Iterator<Tick> e=tickGroups();e.hasNext();)
			{
				final Tick almostTock=e.next();
				int tick=0;
				for(Iterator<TockClient> et=almostTock.tickers();et.hasNext();)
				{
					TockClient C=(TockClient)et.next();
					if(C.tickTotal==0) continue;
					Long avg=Long.valueOf(C.milliTotal/C.tickTotal);
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
		Tick almostTock = null;
		for(Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			totalTickers+=almostTock.numTickers();
			totalMillis+=almostTock.milliTotal;
			totalTicks+=almostTock.tickTotal;
			if(almostTock.milliTotal>topGroupMillis)
			{
				topGroupMillis=almostTock.milliTotal;
				topGroupTicks=almostTock.tickTotal;
				topGroupNumber=num;
			}
			try
			{
				for(Iterator<TockClient> et=almostTock.tickers();et.hasNext();)
				{
					TockClient C=(TockClient)et.next();
					if(C.milliTotal>topObjectMillis)
					{
						topObjectMillis=C.milliTotal;
						topObjectTicks=C.tickTotal;
						topObjectClient=C.clientObject;
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
			return ""+ticks.size();
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
				CMLibrary lib=(CMLibrary)e.nextElement();
				CMSupportThread thread=lib.getSupportThread();
				if(thread!=null) {
					if(curThreadNum==threadNum) {
						String instrCode=itemCode.substring(xend);
						if(instrCode.equalsIgnoreCase("miliTotal"))
							return ""+thread.activeTimeMillis();
						if(instrCode.equalsIgnoreCase("milliTotal"))
							return ""+thread.activeTimeMillis();
						if(instrCode.equalsIgnoreCase("status"))
							return ""+thread.getStatus();
						if(instrCode.equalsIgnoreCase("name"))
							return ""+thread.getName();
						if(instrCode.equalsIgnoreCase("MilliTotalTime"))
							return CMLib.english().returnTime(thread.activeTimeMillis(),0);
						if(instrCode.equalsIgnoreCase("MiliTotalTime"))
							return CMLib.english().returnTime(thread.activeTimeMillis(),0);
						if(instrCode.equalsIgnoreCase("MilliTotalTimePlusAverage"))
							return CMLib.english().returnTime(thread.activeTimeMillis(),thread.getTotalTicks());
						if(instrCode.equalsIgnoreCase("MiliTotalTimePlusAverage"))
							return CMLib.english().returnTime(thread.activeTimeMillis(),thread.getTotalTicks());
						if(instrCode.equalsIgnoreCase("TickTotal"))
							return ""+thread.getTotalTicks();
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
		Tick almostTock=null;
		TockClient C=null;
		Tickable E2=null;
		boolean doItems=((tickID==0)||(tickID==Tickable.TICKID_ROOM_ITEM_REJUV));
		boolean doMobs=((tickID==0)||(tickID==Tickable.TICKID_MOB));
		for(Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			try
			{
				for(Iterator<TockClient> i=almostTock.tickers();i.hasNext();)
				{
					C=(TockClient)i.next();
					E2=C.clientObject;
					if((doItems)
					&&(E2 instanceof ItemTicker)
					&&((here==null)||(((ItemTicker)E2).properLocation()==here)))
					{
						C.tickDown=0;
						if(C.tickTicker())
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
						C.tickDown=0;
						((MOB)E2).phyStats().setRejuv(PhyStats.NO_REJUV);
						if(C.tickTicker())
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
		Tick almostTock=null;
		TockClient C=null;
		Tickable E2=null;
		for(Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			try
			{
				for(Iterator<TockClient> i=almostTock.tickers();i.hasNext();)
				{
					C=i.next();
					E2=C.clientObject;
					if(here==null)
					{
						if(C.tickTicker())
							almostTock.delTicker(C);
					}
					else
					if(isHere(E2,here))
					{
						if(C.tickTicker())
							almostTock.delTicker(C);
					}
					else
					if((E2 instanceof Ability)
					&&(isHere(((Ability)E2).affecting(),here)))
					{
						if(C.tickTicker())
							almostTock.delTicker(C);
					}
				}
			}
			catch(NoSuchElementException ex)
			{
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
			return ""+ticks.size();
		else
		if(which.toLowerCase().startsWith("tickerssize"))
		{
			if(grpstart<0) return"";
			int group=CMath.s_int(which.substring(grpstart));
			if((group>=0)&&(group<ticks.size()))
			{
				List<Tick> enumeratedTicks=new XVector<Tick>(ticks);
				return ""+((Tick)enumeratedTicks.get(group)).numTickers();
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

		if((group<0)||(client<0)||(group>=ticks.size())) return "";
		List<Tick> enumeratedTicks=new XVector<Tick>(ticks);
		if((group<0)||(client<0)||(group>=enumeratedTicks.size())) return "";
		Tick almostTock=(Tick)enumeratedTicks.get(group);
		
		if(client>=almostTock.numTickers()) return "";
		TockClient C=almostTock.fetchTickerByIndex(client);
		if(C==null) return "";

		if(which.toLowerCase().startsWith("tickername"))
		{
			Tickable E=C.clientObject;
			if((E instanceof Ability)&&(E.ID().equals("ItemRejuv")))
				E=((Ability)E).affecting();
			if(E instanceof Room)
				return CMLib.map().getExtendedRoomID((Room)E);
			if(E!=null) return E.name();
			return "!NULL!";
		}
		else
		if(which.toLowerCase().startsWith("tickerid"))
			return ""+C.tickID;
		else
		if(which.toLowerCase().startsWith("tickerstatus"))
			return ((C.clientObject==null)?"":(""+C.clientObject.getTickStatus()));
		else
		if(which.toLowerCase().startsWith("tickercodeword"))
			return getTickStatusSummary(C.clientObject);
		else
		if(which.toLowerCase().startsWith("tickertickdown"))
			return ""+C.tickDown;
		else
		if(which.toLowerCase().startsWith("tickerretickdown"))
			return ""+C.reTickDown;
		else
		if(which.toLowerCase().startsWith("tickermillitotal"))
			return ""+C.milliTotal;
		else
		if(which.toLowerCase().startsWith("tickermilliavg"))
		{
			if(C.tickTotal==0) return "0";
			return ""+(C.milliTotal/C.tickTotal);
		}
		else
		if(which.toLowerCase().startsWith("tickerlaststartmillis"))
			return ""+C.lastStart;
		else
		if(which.toLowerCase().startsWith("tickerlaststopmillis"))
			return ""+C.lastStop;
		else
		if(which.toLowerCase().startsWith("tickerlaststartdate"))
			return CMLib.time().date2String(C.lastStart);
		else
		if(which.toLowerCase().startsWith("tickerlaststopdate"))
			return CMLib.time().date2String(C.lastStop);
		else
		if(which.toLowerCase().startsWith("tickerlastduration"))
		{
			if(C.lastStop>=C.lastStart)
				return CMLib.english().returnTime(C.lastStop-C.lastStart,0);
			return CMLib.english().returnTime(System.currentTimeMillis()-C.lastStart,0);
		}
		else
		if(which.toLowerCase().startsWith("tickersuspended"))
			return ""+C.suspended;
		return "";
	}

	public boolean shutdown() {
		//int numTicks=tickGroup.size();
		while(ticks.size()>0)
		{
			//Log.sysOut("ServiceEngine","Shutting down all tick "+which+"/"+numTicks+"...");
			Tick tock=ticks.getFirst();
			if(tock!=null)
			{
				CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...shutting down Service Engine: killing Tick#" + tock.getCounter()+": "+tock.getStatus());
				tock.shutdown();
				ticks.remove(tock);
			}
			try{Thread.sleep(100);}catch(Exception e){}
		}
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...shutting down Service Engine: "+ID()+": thread shutdown");
		thread.shutdown();
		// force final time tick!
		Vector<TimeClock> timeObjects=new Vector<TimeClock>();
		for(Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			Area A=((Area)e.nextElement());
			if(!timeObjects.contains(A.getTimeObj()))
				timeObjects.addElement(A.getTimeObj());
		}
		CMProps.setUpAllLowVar(CMProps.SYSTEM_MUDSTATUS,"Shutting down...shutting down Service Engine: "+ID()+": saving time objects");
		for(int t=0;t<timeObjects.size();t++)
			((TimeClock)timeObjects.elementAt(t)).save();
		for(CMThreadPoolExecutor pool : threadPools)
			if(pool != null)
				pool.shutdown();
		Log.sysOut("ServiceEngine","Shutdown complete.");
		return true;
	}

	public synchronized void clearDebri(Room room, int taskCode)
	{
		Tick almostTock=null;
		TockClient C=null;
		ItemTicker  I=null;
		Iterator<TockClient> roomSet;
		MOB mob=null;
		for(Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			roomSet=almostTock.getLocalItems(taskCode,room);
			if(roomSet!=null)
				for(;roomSet.hasNext();)
				{
					C=(TockClient)roomSet.next();
					if(C.clientObject instanceof ItemTicker)
					{
						I=(ItemTicker)C.clientObject;
						almostTock.delTicker(C);
						I.setProperLocation(null);
					}
					else
					if(C.clientObject instanceof MOB)
					{
						mob=(MOB)C.clientObject;
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
		Tick almostTock=null;
		TockClient C=null;
		name=name.toUpperCase().trim();
		for(Iterator<Tick> e=tickGroups();e.hasNext();)
		{
			almostTock=e.next();
			for(Iterator<TockClient> i=almostTock.tickers();i.hasNext();)
			{
				C=(TockClient)i.next();
				if((C.clientObject!=null)
				&&(C.clientObject.name().toUpperCase().indexOf(name)>=0)
				&&(!V.contains(C.clientObject)))
					V.addElement(C.clientObject);
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
	public String getServiceThreadSummary(Thread T)
	{
		if(T instanceof CMSupportThread)
			return " ("+((CMSupportThread)T).getStatus()+")";
		else
		if(T instanceof MudHost)
			return " ("+((MudHost)T).getStatus()+")";
		else
		if(T instanceof ExternalHTTPRequests)
			return " ("+((ExternalHTTPRequests)T).getHTTPstatus()+" - "+((ExternalHTTPRequests)T).getHTTPstatusInfo()+")";
		return "";
		
	}
	
	public void insertOrderDeathInOrder(DVector DV, long lastStart, String msg, Tick tock)
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

	public void checkHealth()
	{
		long lastDateTime=System.currentTimeMillis()-(5*TimeManager.MILI_MINUTE);
		long longerDateTime=System.currentTimeMillis()-(120*TimeManager.MILI_MINUTE);
		thread.setStatus("checking");

		thread.setStatus("checking tick groups.");
		DVector orderedDeaths=new DVector(3);
		try
		{
			Tick almostTock = null;
			for(Iterator<Tick> e=tickGroups();e.hasNext();)
			{
				almostTock=e.next();
				if((almostTock.awake)
				&&(almostTock.lastStop<lastDateTime))
				{
					TockClient client=almostTock.lastClient;
					if(client==null)
						insertOrderDeathInOrder(orderedDeaths,0,"LOCKED GROUP "+almostTock.getCounter()+"! No further information.",almostTock);
					else
					if((!CMath.bset(client.tickID,Tickable.TICKID_LONGERMASK))||(almostTock.lastStop<longerDateTime))
					{
						if(client.clientObject==null)
							insertOrderDeathInOrder(orderedDeaths,0,"LOCKED GROUP "+almostTock.getCounter()+": NULL @"+CMLib.time().date2String(client.lastStart)+", tickID "+client.tickID,almostTock);
						else
						{
							StringBuffer str=null;
							Tickable obj=client.clientObject;
							long code=client.clientObject.getTickStatus();
							String codeWord=CMLib.threads().getTickStatusSummary(client.clientObject);
							String msg=null;
							if(obj instanceof Environmental)
								str=new StringBuffer("LOCKED GROUP "+almostTock.getCounter()+" : "+obj.name()+" ("+((Environmental)obj).ID()+") @"+CMLib.time().date2String(client.lastStart)+", status("+code+" ("+codeWord+"), tickID "+client.tickID);
							else
								str=new StringBuffer("LOCKED GROUP "+almostTock.getCounter()+": "+obj.name()+", status("+code+" ("+codeWord+") @"+CMLib.time().date2String(client.lastStart)+", tickID "+client.tickID);
	
							if((obj instanceof MOB)&&(((MOB)obj).location()!=null))
								msg=str.toString()+" in "+((MOB)obj).location().roomID();
							else
							if((obj instanceof Item)&&(((Item)obj).owner()!=null)&&(((Item)obj).owner() instanceof Room))
								msg=str.toString()+" in "+((Room)((Item)obj).owner()).roomID();
							else
							if((obj instanceof Item)&&(((Item)obj).owner()!=null)&&(((Item)obj).owner() instanceof MOB))
								msg=str.toString()+" owned by "+((MOB)((Item)obj).owner()).name();
							else
							if(obj instanceof Room)
								msg=str.toString()+" is "+((Room)obj).roomID();
							else
								msg=str.toString();
							insertOrderDeathInOrder(orderedDeaths,client.lastStart,msg,almostTock);
						}
					}
					// no isDEBUGGING check -- just always let her rip.
					thread.debugDumpStack("ThreadHealth",almostTock);
				}
			}
		}
		catch(java.util.NoSuchElementException e){}
		for(int i=0;i<orderedDeaths.size();i++)
			Log.errOut(thread.getName(),(String)orderedDeaths.elementAt(i,2));
			
		thread.setStatus("killing tick groups.");
		for(int x=0;x<orderedDeaths.size();x++)
		{
			Tick almostTock=(Tick)orderedDeaths.elementAt(x,3);
			Vector<TockClient> tockClients=new Vector<TockClient>();
			try{
				for(Iterator<TockClient> e=almostTock.tickers();e.hasNext();)
					tockClients.addElement(e.next());
			}catch(NoSuchElementException e){}
			try
			{
				almostTock.shutdown();
			}
			catch(java.lang.ThreadDeath d)
			{
				Log.errOut("ThreadDeath killing Tick#"+almostTock.getId());
			}
			catch(Throwable t)
			{
				Log.errOut("Error "+t.getMessage()+" killing Tick#"+almostTock.getId());
			}
			if(CMLib.threads() instanceof ServiceEngine)
				((ServiceEngine)CMLib.threads()).delTickGroup(almostTock);
			for(int i=0;i<tockClients.size();i++)
			{
				TockClient c=(TockClient)tockClients.elementAt(i);
				CMLib.threads().startTickDown(c.clientObject,c.tickID,c.reTickDown);
			}
		}

		thread.setStatus("Checking mud threads");
		for(int m=0;m<CMLib.hosts().size();m++)
		{
			List<Runnable> badThreads=((MudHost)CMLib.hosts().get(m)).getOverdueThreads();
			for(Runnable T : badThreads)
				if(T instanceof Thread)
				{
					String threadName=((Thread)T).getName();
					if(T instanceof Tickable)
						threadName=((Tickable)T).name()+" ("+((Tickable)T).ID()+"): "+((Tickable)T).getTickStatus();
					thread.setStatus("Killing "+threadName);
					Log.errOut("Killing stray thread: "+threadName);
					CMLib.killThread((Thread)T,100,1);
				}
				else
					Log.errOut("Unable to kill stray runnable: "+T.toString());
		}
		
		thread.setStatus("Done checking threads");
	}

	public void run()
	{
		while(isAllSuspended())
			try{Thread.sleep(2000);}catch(Exception e){}
			
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.UTILITHREAD))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.THREADTHREAD)))
		{
			checkHealth();
			Resources.removeResource("SYSTEM_HASHED_MASKS");
		}
	}
	
	public boolean activate() {
		if(thread==null)
			thread=new CMSupportThread("THThreads"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					MudHost.TIME_UTILTHREAD_SLEEP, this, CMSecurity.isDebugging(CMSecurity.DbgFlag.UTILITHREAD),CMSecurity.DisFlag.UTILITHREAD);
		if(!thread.isStarted())
			thread.start();
		return true;
	}
	
}
