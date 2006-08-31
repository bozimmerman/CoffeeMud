package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.core.database.DBInterface;
import com.planet_ink.coffee_mud.core.http.ProcessHTTPrequest;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;


/* 
   Copyright 2000-2006 Bo Zimmerman

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
    
    public String ID(){return "ServiceEngine";}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new ServiceEngine();}}
    public CMObject copyOf(){try{return (CMObject)this.clone();}catch(Exception e){return newInstance();}}
    public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    
	protected Vector tickGroup=new Vector();
	public Enumeration tickGroups(){return ((Vector)tickGroup.clone()).elements();}
    private boolean isSuspended=false;
	
	public void delTickGroup(Tick tock)
	{
		synchronized(tickGroup)
		{
			tickGroup.removeElement(tock);
		}
	}
	public void addTickGroup(Tick tock)
	{
		synchronized(tickGroup)
		{
			tickGroup.addElement(tock);
		}
	}
	
	public Tick confirmAndGetTickThread(Tickable E, long TICK_TIME, int tickID)
	{
		Tick tock=null;
        Tick almostTock=null;
		for(Enumeration v=tickGroups();v.hasMoreElements();)
		{
			almostTock=(Tick)v.nextElement();
			if((tock==null)
            &&(almostTock.TICK_TIME==TICK_TIME)
            &&(!almostTock.solitaryTicker)
            &&(almostTock.numTickers()<TickableGroup.MAX_TICK_CLIENTS))
				tock=almostTock;
        	if(almostTock.contains(E,tickID)) 
        		return null;
		}

		if(tock!=null) return tock;
		tock=new Tick(TICK_TIME);
		addTickGroup(tock);
		return tock;
	}

    public void startTickDown(Tickable E, int tickID, int numTicks)
    { startTickDown(E,tickID,Tickable.TIME_TICK,numTicks); }
    
	public void startTickDown(Tickable E,
							  int tickID,
                              long TICK_TIME,
							  int numTicks)
	{
		Tick tock=confirmAndGetTickThread(E,TICK_TIME,tickID);
		if(tock==null) return;

		TockClient client=new TockClient(E,numTicks,tickID);
		if(client!=null)
		{
			if((tickID&65536)==65536)
				tock.solitaryTicker=true;
			tock.addTicker(client);
		}
	}

	public boolean deleteTick(Tickable E, int tickID)
	{
        Tick almostTock=null;
        Iterator set=null;
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			almostTock=(Tick)v.nextElement();
			set=almostTock.getTickSet(E,tickID);
			if(set!=null)
			for(;set.hasNext();)
				almostTock.delTicker((TockClient)set.next());
		}
		return false;
	}

	public boolean isTicking(Tickable E, int tickID)
	{
        Tick almostTock=null;
        Iterator set;
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			almostTock=(Tick)v.nextElement();
			set=almostTock.getTickSet(E,tickID);
			if(set!=null) return true;
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
        Tick almostTock=null;
        Iterator set=null;
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			almostTock=(Tick)v.nextElement();
			set=almostTock.getTickSet(E,tickID);
			if(set!=null)
			for(;set.hasNext();)
				((TockClient)set.next()).suspended=suspend;
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
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if(here.rawExits()[d]==E2)
					return true;
		}
		return false;
	}


	public String systemReport(String itemCode)
	{
		long totalMOBMillis=0;
		long totalMOBTicks=0;
		long topMOBMillis=0;
		long topMOBTicks=0;
		MOB topMOBClient=null;
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
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
            try{
    			for(Iterator e=almostTock.tickers();e.hasNext();)
    			{
    				TockClient C=(TockClient)e.next();
    				if(C.milliTotal>topObjectMillis)
    				{
    					topObjectMillis=C.milliTotal;
    					topObjectTicks=C.tickTotal;
    					topObjectClient=C.clientObject;
    					topObjectGroup=num;
    				}
    			}
            }catch(NoSuchElementException e){}
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
			return ""+tickGroup.size();
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
		if(itemCode.equalsIgnoreCase("saveThreadMilliTotal"))
			return ""+SaveThread.milliTotal;
		else
		if(itemCode.equalsIgnoreCase("saveThreadStatus"))
			return ""+SaveThread.status;
		else
		if(itemCode.equalsIgnoreCase("saveThreadMilliTotalTime"))
			return CMLib.english().returnTime(SaveThread.milliTotal,0);
		else
		if(itemCode.equalsIgnoreCase("utilThreadMilliTotal"))
			return ""+UtiliThread.milliTotal;
		else
		if(itemCode.equalsIgnoreCase("utilThreadStatus"))
			return ""+UtiliThread.status;
		else
		if(itemCode.equalsIgnoreCase("utilThreadMilliTotalTime"))
			return CMLib.english().returnTime(UtiliThread.milliTotal,0);
		else
		if(itemCode.equalsIgnoreCase("saveThreadMilliTotalTimePlusAverage"))
			return CMLib.english().returnTime(SaveThread.milliTotal,SaveThread.tickTotal);
		else
		if(itemCode.equalsIgnoreCase("saveThreadTickTotal"))
			return ""+SaveThread.tickTotal;
		else
		if(itemCode.equalsIgnoreCase("utilThreadMilliTotalTimePlusAverage"))
			return CMLib.english().returnTime(UtiliThread.milliTotal,UtiliThread.tickTotal);
		else
		if(itemCode.equalsIgnoreCase("utilThreadTickTotal"))
			return ""+UtiliThread.tickTotal;
		else
		if(itemCode.equalsIgnoreCase("topObjectClient"))
		{
			if(topObjectClient!=null)
				return topObjectClient.name();
			return "";
		}


		return "";
	}

	public void tickAllTickers(Room here)
	{
        Tick almostTock=null;
        TockClient C=null;
        Tickable E2=null;
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			almostTock=(Tick)v.nextElement();
            try
            {
    			for(Iterator e=almostTock.tickers();e.hasNext();)
    			{
    				C=(TockClient)e.next();
    				E2=C.clientObject;
    				if(isHere(E2,here))
    				{
    					if(Tick.tickTicker(C,isSuspended))
    						almostTock.delTicker(C);
    				}
    				else
    				if((E2 instanceof Ability)
    				&&(isHere(((Ability)E2).affecting(),here)))
    				{
    					if(Tick.tickTicker(C,isSuspended))
    						almostTock.delTicker(C);
    				}
    			}
            }catch(NoSuchElementException e){}
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
			return ""+tickGroup.size();
		else
		if(which.toLowerCase().startsWith("tickerssize"))
		{
			if(grpstart<0) return"";
			int group=CMath.s_int(which.substring(grpstart));
			if((group>=0)&&(group<tickGroup.size()))
				return ""+((Tick)tickGroup.elementAt(group)).numTickers();
			return "";
		}
		int group=-1;
		int client=-1;
		int clistart=which.indexOf("-");
		if((grpstart>=0)&&(clistart>grpstart))
		{
			group=CMath.s_int(which.substring(grpstart,clistart));
			client=CMath.s_int(which.substring(clistart+1));
		}

		if((group<0)||(client<0)||(group>=tickGroup.size())) return "";
		Tick almostTock=(Tick)tickGroup.elementAt(group);
		
		if(client>=almostTock.numTickers()) return "";
		TockClient C=almostTock.fetchTicker(client);
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

	public void shutdownAll()
	{
		int numTicks=tickGroup.size();
		int which=0;
		while(tickGroup.size()>0)
		{
			Log.sysOut("ServiceEngine","Shutting down all tick "+which+"/"+numTicks+"...");
			Tick tock=null;
			synchronized(tickGroup){tock=(Tick)tickGroup.elementAt(0);}
			if(tock!=null) tock.shutdown();
			try{Thread.sleep(100);}catch(Exception e){}
			which++;
		}
		Log.sysOut("ServiceEngine","Shutdown complete.");
	}

	public synchronized void clearDebri(Room room, int taskCode)
	{
        Tick almostTock=null;
        TockClient C=null;
        ItemTicker  I=null;
        MOB mob=null;
        Vector roomSet=null;
		for(Enumeration v=tickGroup.elements();v.hasMoreElements();)
		{
			almostTock=(Tick)v.nextElement();
			roomSet=almostTock.getLocalItems(taskCode,room);
			if(roomSet!=null)
				for(Enumeration e=roomSet.elements();e.hasMoreElements();)
				{
    				C=(TockClient)e.nextElement();
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
    
    public String getTickStatusSummary(Tickable obj)
    {
        if(obj==null) return "";
        long code=obj.getTickStatus();
        if(obj instanceof Environmental)
        {
            if(CMath.bset(code,Tickable.STATUS_BEHAVIOR))
            {
                long b=(code-Tickable.STATUS_BEHAVIOR);
                String codeWord="Behavior #"+b;
                if((b>=0)&&(b<((Environmental)obj).numBehaviors()))
                {
                    Behavior B=((Environmental)obj).fetchBehavior((int)b);
                    codeWord+=" ("+B.name()+": "+B.getTickStatus();
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
            if(CMath.bset(code,Tickable.STATUS_AFFECT))
            {
                long b=(code-Tickable.STATUS_AFFECT);
                String codeWord="Effect #"+b;
                if((b>=0)&&(b<((Environmental)obj).numEffects()))
                {
                    Environmental E=((Environmental)obj).fetchEffect((int)b);
                    codeWord+=" ("+E.name()+": "+E.getTickStatus()+")";
                }
                return codeWord;
            }
        }
        String codeWord=null;
        if(CMath.bset(code,Tickable.STATUS_BEHAVIOR))
           codeWord="Behavior?!";
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
        if(T instanceof UtiliThread)
            return " ("+UtiliThread.status+")";
        else
        if(T instanceof SaveThread)
            return " ("+SaveThread.status+")";
        else
        if(T instanceof MudHost)
            return " ("+((MudHost)T).getStatus()+")";
        else
        if(T instanceof ExternalHTTPRequests)
            return " ("+((ExternalHTTPRequests)T).getHTTPstatus()+" - "+((ExternalHTTPRequests)T).getHTTPstatusInfo()+")";
        return "";
        
    }
}
