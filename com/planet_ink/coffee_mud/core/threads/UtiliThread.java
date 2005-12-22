package com.planet_ink.coffee_mud.core.threads;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.core.database.DBConnection;
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
public class UtiliThread extends Thread
{
	public static boolean started=false;
	private static boolean shutDown=false;
	public long lastStart=0;
	public long lastStop=0;
	public static long milliTotal=0;
	public static long tickTotal=0;
	public static String status="";

	public UtiliThread()
	{
		super("UtiliThread");
		setName("UtiliThread");
	}

	public void itemSweep()
	{
		boolean corpsesOnly=CMSecurity.isSaveFlag("ROOMITEMS");
		status="item sweeping";
		long itemKillTime=System.currentTimeMillis();
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
			    Room R=(Room)r.nextElement(); 
			    
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if((I!=null)
					&&((!corpsesOnly)||(I instanceof DeadBody))
					&&(I.dispossessionTime()!=0)
					&&(I.owner()==R))
					{
						if(itemKillTime>I.dispossessionTime())
						{
							status="destroying "+I.Name();
							I.destroy();
							status="item sweeping";
							i=i-1;
						}
					}
				}
			}
	    }
	    catch(java.util.NoSuchElementException e){}
	}

    public void insertOrderDeathInOrder(DVector DV, long lastStart, String msg, Tick tock)
    {
        if(DV.size()>0)
        for(int i=0;i<DV.size();i++)
        {
            if(((Long)DV.elementAt(i,1)).longValue()>lastStart)
            {
                DV.insertElementAt(i,new Long(lastStart),msg,tock);
                return;
            }
        }
        DV.addElement(new Long(lastStart),msg,tock);
    }
    
	public void checkHealth()
	{
		long lastDateTime=System.currentTimeMillis();
		lastDateTime-=(20*TimeManager.MILI_MINUTE);
		status="checking";
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB mob=R.fetchInhabitant(m);
					if((mob!=null)&&(mob.lastTickedDateTime()>0)&&(mob.lastTickedDateTime()<lastDateTime))
					{
						boolean ticked=CMLib.threads().isTicking(mob,MudHost.TICK_MOB);
						boolean isDead=mob.amDead();
						String wasFrom=((mob.getStartRoom()!=null)?mob.getStartRoom().roomID():"NULL");
						if(!ticked)
						{
                            if(CMLib.map().getPlayer(mob.Name())==null)
                                Log.errOut("UtiliThread",mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been destroyed. May he rest in peace."));
                            else
                                Log.errOut("UtiliThread","Player "+mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been put aside."));
							status="destroying unticked mob "+mob.name();
							if(CMLib.map().getPlayer(mob.Name())==null) mob.destroy();
							R.delInhabitant(mob);
							status="checking";
						}
					}
				}
			}
	    }
	    catch(java.util.NoSuchElementException e){}

		status="checking tick groups.";
        DVector orderedDeaths=new DVector(3);
		try
		{
			for(Enumeration v=CMLib.threads().tickGroups();v.hasMoreElements();)
			{
				Tick almostTock=(Tick)v.nextElement();
				if((almostTock.awake)
				&&(almostTock.lastStop<lastDateTime))
				{
					TockClient client=almostTock.lastClient;
					if(client!=null)
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
								str=new StringBuffer("LOCKED GROUP "+almostTock.getCounter()+" : "+obj.name()+" ("+((Environmental)obj).ID()+") @"+CMLib.time().date2String(client.lastStart)+", Status="+code+" ("+codeWord+"), tickID "+client.tickID);
							else
								str=new StringBuffer("LOCKED GROUP "+almostTock.getCounter()+": "+obj.name()+", Status="+code+" ("+codeWord+") @"+CMLib.time().date2String(client.lastStart)+", tickID "+client.tickID);
	
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
					else
                        insertOrderDeathInOrder(orderedDeaths,0,"LOCKED GROUP "+almostTock.getCounter()+"! No further information.",almostTock);
				}
			}
	    }
	    catch(java.util.NoSuchElementException e){}
        for(int i=0;i<orderedDeaths.size();i++)
            Log.errOut("UtiliThread",(String)orderedDeaths.elementAt(i,2));
			
		status="killing tick groups.";
		for(int x=0;x<orderedDeaths.size();x++)
		{
			Tick almostTock=(Tick)orderedDeaths.elementAt(x,3);
			Vector objs=new Vector();
            try{
    			for(Enumeration e=almostTock.tickers();e.hasMoreElements();)
    				objs.addElement(e.nextElement());
            }catch(NoSuchElementException e){}
			almostTock.shutdown();
			if(CMLib.threads() instanceof ServiceEngine)
				((ServiceEngine)CMLib.threads()).delTickGroup(almostTock);
			for(int i=0;i<objs.size();i++)
			{
				TockClient c=(TockClient)objs.elementAt(i);
				CMLib.threads().startTickDown(c.clientObject,c.tickID,c.reTickDown);
			}
		}

		status="checking CMLib.sessions().";
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session S=CMLib.sessions().elementAt(s);
			long time=System.currentTimeMillis()-S.lastLoopTime();
			if(time>0)
			{
				if((S.mob()!=null))
				{
					long check=60000;

					if((S.previousCMD()!=null)
					&&(S.previousCMD().size()>0)
					&&(((String)S.previousCMD().firstElement()).equalsIgnoreCase("IMPORT")
					   ||((String)S.previousCMD().firstElement()).equalsIgnoreCase("EXPORT")
					   ||((String)S.previousCMD().firstElement()).equalsIgnoreCase("MERGE")))
						check=check*60;
					else
					if(CMSecurity.isAllowed(S.mob(),S.mob().location(),"CMDROOMS"))
						check=check*15;
					else
					if(S.getStatus()==Session.STATUS_LOGIN)
						check=check*5;

					if(time>(check*10))
					{
						Log.errOut("UtiliThread","KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+", out for "+time);
						Log.errOut("UtiliThread","STATUS  was :"+S.getStatus()+", "+"LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
						status="killing session ";
						S.logoff();
						S.logoff();
						CMLib.sessions().removeElement(S);
						status="checking CMLib.sessions().";
					}
					else
					if(time>check)
					{
                        if((S.mob()==null)||(S.mob().Name()==null)||(S.mob().Name().length()==0))
                        {
                            S.logoff();
                            S.logoff();
                            CMLib.sessions().removeElement(S);
                        }
                        else
                        {
    						Log.errOut("UtiliThread","Suspect Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+", out for "+time);
    						if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
    							Log.errOut("UtiliThread","STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
                        }
					}
				}
				else
				if(time>(60000))
				{
					Log.errOut("UtiliThread","KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+", out for "+time);
					if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
					Log.errOut("UtiliThread","STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
					status="killing session ";
					S.logoff();
					S.logoff();
					CMLib.sessions().removeElement(S);
					status="checking CMLib.sessions().";
				}
			}
		}
	}

	public void shutdown()
	{
		shutDown=true;
		this.interrupt();
	}

	public void run()
	{
		lastStart=System.currentTimeMillis();
		if(started)
		{
			Log.errOut("UtiliThread","DUPLICATE UTILITHREAD RUNNING!!");
			return;
		}
		started=true;
        shutDown=false;
		
		// now start the thread
		while(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
		{ 
			try{Thread.sleep(2000);}catch(Exception e){}
		}
		
		try{Thread.sleep(MudHost.TICK_TIME*2);}catch(Exception e){started=false;}
		
		while(started)
		{
			try
			{
                while(CMLib.threads().isAllSuspended())
                    try{Thread.sleep(2000);}catch(Exception e){}
				if(!CMSecurity.isDisabled("UTILITHREAD"))
				{
					itemSweep();
					checkHealth();
					lastStop=System.currentTimeMillis();
					milliTotal+=(lastStop-lastStart);
					tickTotal++;
					status="sleeping";
					Thread.sleep(MudHost.TIME_UTILTHREAD_SLEEP);
					lastStart=System.currentTimeMillis();
				}
				else
				{
					status="sleeping";
					Thread.sleep(MudHost.TIME_UTILTHREAD_SLEEP);
				}
			}
			catch(InterruptedException ioe)
			{
				Log.sysOut("UtiliThread","Interrupted!");
				if(shutDown)
				{
					shutDown=false;
					break;
				}
			}
			catch(Exception e)
			{
				Log.errOut("UtiliThread",e);
			}
		}

		// force final time tick!
		Vector timeObjects=new Vector();
		for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
		{
			Area A=((Area)e.nextElement());
			if(!timeObjects.contains(A.getTimeObj()))
				timeObjects.addElement(A.getTimeObj());
		}
		for(int t=0;t<timeObjects.size();t++)
			((TimeClock)timeObjects.elementAt(t)).save();
		
		Log.sysOut("UtiliThread","Shutdown complete.");
	}
}
