package com.planet_ink.coffee_mud.system;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class ServiceEngine
{
	private static Vector tickGroup=new Vector();
	public static Enumeration tickGroups(){return tickGroup.elements();}
	public static void delTickGroup(Tick tock)
	{
		synchronized(tickGroup)
		{
			tickGroup.removeElement(tock);
		}
	}
	public static void addTickGroup(Tick tock)
	{
		synchronized(tickGroup)
		{
			tickGroup.addElement(tock);
		}
	}

	public static Tick confirmAndGetTickThread(Tickable E, int tickID)
	{
		Tick tock=null;

		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			Tick almostTock=(Tick)v.nextElement();
			if((tock==null)&&(almostTock.numTickers()<Host.MAX_TICK_CLIENTS))
				tock=almostTock;
			for(Enumeration t=almostTock.tickers();t.hasMoreElements();)
			{
				TockClient client=(TockClient)t.nextElement();
				if((client.clientObject==E)&&(client.tickID==tickID))
					return null;
			}
		}

		if(tock!=null)
			return tock;
		else
		{
			tock=new Tick();
			addTickGroup(tock);
			return tock;
		}
	}

	public static void startTickDown(Tickable E,
									 int tickID,
									 int numTicks)
	{
		Tick tock=confirmAndGetTickThread(E,tickID);
		if(tock==null) return;

		TockClient client=new TockClient(E,numTicks,tickID);
		if(client!=null)
			tock.addTicker(client);
	}

	public static boolean deleteTick(Tickable E, int tickID)
	{
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			Tick almostTock=(Tick)v.nextElement();
			for(Enumeration e=almostTock.tickers();e.hasMoreElements();)
			{
				TockClient C=(TockClient)e.nextElement();
				Tickable E2=C.clientObject;
				if((E==E2)&&((tickID==C.tickID)||(tickID<0)))
				{
					almostTock.delTicker(C);
					if(tickID>=0)
						return true;
				}
			}
		}
		return false;
	}

	public static boolean isTicking(Tickable E, int tickID)
	{
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			Tick almostTock=(Tick)v.nextElement();
			for(Enumeration e=almostTock.tickers();e.hasMoreElements();)
			{
				TockClient C=(TockClient)e.nextElement();
				Tickable E2=C.clientObject;
				if((E==E2)&&((tickID==C.tickID)||(tickID<0)))
					return true;
			}
		}
		return false;
	}

	public static void suspendTicking(Tickable E, int tickID){suspendResumeTicking(E,tickID,true);}
	public static void resumeTicking(Tickable E, int tickID){suspendResumeTicking(E,tickID,false);}
	private static boolean suspendResumeTicking(Tickable E, int tickID, boolean suspend)
	{
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			Tick almostTock=(Tick)v.nextElement();
			for(Enumeration e=almostTock.tickers();e.hasMoreElements();)
			{
				TockClient C=(TockClient)e.nextElement();
				Tickable E2=C.clientObject;
				if((E==E2)&&((tickID==C.tickID)||(tickID<0)))
					C.suspended=suspend;
			}
		}
		return false;
	}

	public static boolean isHere(Tickable E2, Room here)
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
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if(here.rawExits()[d]==E2)
					return true;
		}
		return false;
	}


	public static String report(String itemCode)
	{
		long totalMOBMillis=0;
		long totalMOBTicks=0;
		long topMOBMillis=0;
		long topMOBTicks=0;
		MOB topMOBClient=null;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
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
			return Util.returnTime(totalMOBMillis,0);
		else
		if(itemCode.equalsIgnoreCase("totalMOBMillisTimePlusAverage"))
			return Util.returnTime(totalMOBMillis,totalMOBTicks);
		else
		if(itemCode.equalsIgnoreCase("totalMOBTicks"))
			return ""+totalMOBTicks;
		else
		if(itemCode.equalsIgnoreCase("topMOBMillis"))
			return ""+topMOBMillis;
		else
		if(itemCode.equalsIgnoreCase("topMOBMillisTime"))
			return Util.returnTime(topMOBMillis,0);
		else
		if(itemCode.equalsIgnoreCase("topMOBMillisTimePlusAverage"))
			return Util.returnTime(topMOBMillis,topMOBTicks);
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
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			Tick almostTock=(Tick)v.nextElement();
			totalTickers+=almostTock.numTickers();
			totalMillis+=almostTock.milliTotal;
			totalTicks+=almostTock.tickTotal;
			if(almostTock.milliTotal>topGroupMillis)
			{
				topGroupMillis=almostTock.milliTotal;
				topGroupTicks=almostTock.tickTotal;
				topGroupNumber=num;
			}
			for(Enumeration e=almostTock.tickers();e.hasMoreElements();)
			{
				TockClient C=(TockClient)e.nextElement();
				if(C.milliTotal>topObjectMillis)
				{
					topObjectMillis=C.milliTotal;
					topObjectTicks=C.tickTotal;
					topObjectClient=C.clientObject;
					topObjectGroup=num;
				}
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
			return ""+Util.returnTime(System.currentTimeMillis()-ExternalPlay.getStartTime(),0);
		else
		if(itemCode.equalsIgnoreCase("startTime"))
			return IQCalendar.d2String(ExternalPlay.getStartTime());
		else
		if(itemCode.equalsIgnoreCase("currentTime"))
			return IQCalendar.d2String(System.currentTimeMillis());
		else
		if(itemCode.equalsIgnoreCase("totalTickers"))
			return ""+totalTickers;
		else
		if(itemCode.equalsIgnoreCase("totalMillis"))
			return ""+totalMillis;
		else
		if(itemCode.equalsIgnoreCase("totalMillisTime"))
			return Util.returnTime(totalMillis,0);
		else
		if(itemCode.equalsIgnoreCase("totalMillisTimePlusAverage"))
			return Util.returnTime(totalMillis,totalTicks);
		else
		if(itemCode.equalsIgnoreCase("totalTicks"))
			return ""+totalTicks;
		else
		if(itemCode.equalsIgnoreCase("tickgroupsize"))
			return ""+tickGroup.size();
		else
		if(itemCode.equalsIgnoreCase("topGroupNumber"))
			return ""+topGroupNumber;
		else
		if(itemCode.equalsIgnoreCase("topGroupMillis"))
			return ""+topGroupMillis;
		else
		if(itemCode.equalsIgnoreCase("topGroupMillisTime"))
			return Util.returnTime(topGroupMillis,0);
		else
		if(itemCode.equalsIgnoreCase("topGroupMillisTimePlusAverage"))
			return Util.returnTime(topGroupMillis,topGroupTicks);
		else
		if(itemCode.equalsIgnoreCase("topGroupTicks"))
			return ""+topGroupTicks;
		else
		if(itemCode.equalsIgnoreCase("topObjectMillis"))
			return ""+topObjectMillis;
		else
		if(itemCode.equalsIgnoreCase("topObjectMillisTime"))
			return Util.returnTime(topObjectMillis,0);
		else
		if(itemCode.equalsIgnoreCase("topObjectMillisTimePlusAverage"))
			return Util.returnTime(topObjectMillis,topObjectTicks);
		else
		if(itemCode.equalsIgnoreCase("topObjectTicks"))
			return ""+topObjectTicks;
		else
		if(itemCode.equalsIgnoreCase("topObjectGroup"))
			return ""+topObjectGroup;
		else
		if(itemCode.equalsIgnoreCase("saveThreadMilliTotal"))
			return ""+SaveThread.milliTotal;
		else
		if(itemCode.equalsIgnoreCase("saveThreadMilliTotalTime"))
			return Util.returnTime(SaveThread.milliTotal,0);
		else
		if(itemCode.equalsIgnoreCase("saveThreadMilliTotalTimePlusAverage"))
			return Util.returnTime(SaveThread.milliTotal,SaveThread.tickTotal);
		else
		if(itemCode.equalsIgnoreCase("saveThreadTickTotal"))
			return ""+SaveThread.tickTotal;
		else
		if(itemCode.equalsIgnoreCase("topObjectClient"))
		{
			if(topObjectClient!=null)
				return topObjectClient.name();
			else
				return "";
		}
		
		
		return "";
	}

	public static void tickAllTickers(Room here)
	{
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			Tick almostTock=(Tick)v.nextElement();
			for(Enumeration e=almostTock.tickers();e.hasMoreElements();)
			{
				TockClient C=(TockClient)e.nextElement();
				Tickable E2=C.clientObject;
				if(isHere(E2,here))
				{
					if(Tick.tickTicker(C))
						almostTock.delTicker(C);
				}
				else
				if((E2 instanceof Ability)
				&&(isHere(((Ability)E2).affecting(),here)))
				{
					if(Tick.tickTicker(C))
						almostTock.delTicker(C);
				}
			}
		}
	}

	public static String tickInfo(String which)
	{
		int grpstart=-1;
		for(int i=0;i<which.length();i++)
			if(Character.isDigit(which.charAt(i)))
			{
				grpstart=i;
				break;
			}
		if(which.equalsIgnoreCase("tickGroupSize"))
			return ""+tickGroup.size();
		else
		if(which.toLowerCase().startsWith("tickerssize"))
		{
			if(grpstart<0) return"";
			int group=Util.s_int(which.substring(grpstart));
			if((group>=0)&&(group<tickGroup.size()))
				return ""+((Tick)tickGroup.elementAt(group)).numTickers();
			return "";
		}
		int group=-1;
		int client=-1;
		int clistart=which.indexOf("-");
		if((grpstart>=0)&&(clistart>grpstart))
		{
			group=Util.s_int(which.substring(grpstart,clistart));
			client=Util.s_int(which.substring(clistart+1));
		}
		if((group<0)||(client<0)||(group>=tickGroup.size())) return "";
		Tick almostTock=(Tick)tickGroup.elementAt(group);
		if(client>=almostTock.numTickers()) return "";
		TockClient C=(TockClient)almostTock.fetchTicker(client);
		if(C==null) return "";
		
		if(which.toLowerCase().startsWith("tickername"))
		{
			Tickable E=C.clientObject;
			if((E instanceof Ability)&&(E.ID().equals("ItemRejuv")))
				E=((Ability)E).affecting();
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
			return C.tickCodeWord();
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
		if(which.toLowerCase().startsWith("tickerlaststartdate"))
			return IQCalendar.d2String(C.lastStart);
		else
		if(which.toLowerCase().startsWith("tickerlaststopdate"))
			return IQCalendar.d2String(C.lastStop);
		else
		if(which.toLowerCase().startsWith("tickerlastduration"))
		{
			if(C.lastStop>C.lastStart)
				return Util.returnTime(C.lastStop-C.lastStart,0);
			else
				return Util.returnTime(System.currentTimeMillis()-C.lastStart,0);
		}
		else
		if(which.toLowerCase().startsWith("tickersuspended"))
			return ""+C.suspended;
		return "";
	}

	public static void shutdownAll()
	{
		Log.sysOut("ServiceEngine","Shutting down all ticks...");
		while(tickGroup.size()>0)
		{
			Tick tock=null;
			synchronized(tickGroup){tock=(Tick)tickGroup.elementAt(0);}
			if(tock!=null) tock.shutdown();
			try{Thread.sleep(100);}catch(Exception e){}
		}
		Log.sysOut("ServiceEngine","Shutdown complete.");
	}

	public synchronized static void clearDebri(Room room, int taskCode)
	{
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			Tick almostTock=(Tick)v.nextElement();
			for(Enumeration e=almostTock.tickers();e.hasMoreElements();)
			{
				TockClient C=(TockClient)e.nextElement();
				if((C.clientObject instanceof ItemTicker)&&(taskCode<2))
				{
					ItemTicker I=(ItemTicker)C.clientObject;
					if(I.properLocation()==room)
					{
						almostTock.delTicker(C);
						I.setProperLocation(null);
					}
				}
				else
				if((C.clientObject instanceof MOB)&&((taskCode==0)||(taskCode==2)))
				{
					MOB mob=(MOB)C.clientObject;
					if((mob.getStartRoom()==room)
					&&(mob.isMonster())
					&&(!room.isInhabitant(mob)))
					{
						mob.destroy();
						almostTock.delTicker(C);
					}
				}
			}
		}
	}
}
