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
	public boolean started=false;
	private boolean shutDown=false;
	public long lastStart=0;
	public long lastStop=0;
	public long milliTotal=0;
	public long tickTotal=0;
	public String status="";
	private boolean debugging=false;
	
	public UtiliThread()
	{
		super("UtiliThread");
		setName("UtiliThread");
        setDaemon(true);
	}

	private void status(String s)
	{
		status=s;
		if(debugging) Log.debugOut("SaveThread",s);
	}
	
	public void vacuum()
	{
		boolean corpsesOnly=CMSecurity.isSaveFlag("ROOMITEMS");
		boolean noMobs=CMSecurity.isSaveFlag("ROOMMOBS");
		status("expiration sweep");
		long currentTime=System.currentTimeMillis();
		boolean debug=CMSecurity.isDebugging("VACUUM");
		try
		{
			Vector stuffToGo=new Vector();
			Item I=null;
			MOB M=null;
			Room R=null;
			Vector roomsToGo=new Vector();
			boolean success=true;
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
			    R=(Room)r.nextElement();
			    if((R.expirationDate()!=0)
			    &&(currentTime>R.expirationDate())
				&&(R.okMessage(R,CMClass.getMsg(CMLib.map().god(R),R,null,CMMsg.MSG_EXPIRE,null))))
			    	roomsToGo.addElement(R);
			    else
			    if(!R.amDestroyed())
			    {
			    	stuffToGo.clear();
					for(int i=0;i<R.numItems();i++)
					{
						I=R.fetchItem(i);
						if((I!=null)
						&&((!corpsesOnly)||(I instanceof DeadBody))
						&&(I.expirationDate()!=0)
						&&(I.owner()==R)
						&&(currentTime>I.expirationDate()))
							stuffToGo.add(I);
					}
					for(int i=0;i<R.numInhabitants();i++)
					{
						M=R.fetchInhabitant(i);
						if((M!=null)
						&&(!noMobs)
						&&(M.expirationDate()!=0)
						&&(currentTime>M.expirationDate()))
							stuffToGo.add(M);
					}
			    }
			    if(stuffToGo.size()>0)
			    {
			    	MOB god=CMLib.map().god(R);
				    for(int s=0;s<stuffToGo.size();s++)
				    {
				    	Environmental E=(Environmental)stuffToGo.elementAt(s);
						status("expiring "+E.Name());
				    	success=R.showOthers(god,E,CMMsg.MSG_EXPIRE,null);
				    	if(debug) Log.sysOut("UTILITHREAD","Expired "+E.Name()+" in "+CMLib.map().getExtendedRoomID(R)+": "+success);
				    }
				    stuffToGo.clear();
			    }
			}
			for(int r=0;r<roomsToGo.size();r++)
			{
				R=(Room)roomsToGo.elementAt(r);
		    	MOB god=CMLib.map().god(R);
				status("expirating room "+CMLib.map().getExtendedRoomID(R));
				if(debug)
				{
					String roomID=CMLib.map().getExtendedRoomID(R);
					if(roomID.length()==0) roomID="(unassigned grid room, probably in the air)";
			    	if(debug) Log.sysOut("UTILITHREAD","Expired "+roomID+".");
				}
				R.sendOthers(god,CMClass.getMsg(god,R,null,CMMsg.MSG_EXPIRE,null));
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

    public void debugDumpStack(Thread theThread)
    {
    	// I wish Java had compiler directives.  Would be great to un-comment this for 1.5 JVMs
		//java.lang.StackTraceElement[] s=(java.lang.StackTraceElement[])Thread.getAllStackTraces().get(theThread);
		//for(int i=0;i<s.length;i++)
		//	Log.debugOut("UtiliDump","   "+s[i].getClassName()+": "+s[i].getMethodName()+"("+s[i].getFileName()+": "+s[i].getLineNumber()+")");
    }
    
	public void checkHealth()
	{
		long lastDateTime=System.currentTimeMillis()-(5*TimeManager.MILI_MINUTE);
		long longerDateTime=System.currentTimeMillis()-(120*TimeManager.MILI_MINUTE);
		status("checking");
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
						boolean ticked=CMLib.threads().isTicking(mob,Tickable.TICKID_MOB);
						boolean isDead=mob.amDead();
						String wasFrom=((mob.getStartRoom()!=null)?mob.getStartRoom().roomID():"NULL");
						if(!ticked)
						{
                            if(CMLib.map().getPlayer(mob.Name())==null)
                                Log.errOut("UtiliThread",mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been destroyed. May he rest in peace."));
                            else
                                Log.errOut("UtiliThread","Player "+mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+CMLib.time().date2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been put aside."));
							status("destroying unticked mob "+mob.name());
							if(CMLib.map().getPlayer(mob.Name())==null) mob.destroy();
							R.delInhabitant(mob);
							status("checking");
						}
					}
				}
			}
	    }
	    catch(java.util.NoSuchElementException e){}

		status("checking tick groups.");
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
                    debugDumpStack(almostTock);
				}
			}
	    }
	    catch(java.util.NoSuchElementException e){}
        for(int i=0;i<orderedDeaths.size();i++)
            Log.errOut("UtiliThread",(String)orderedDeaths.elementAt(i,2));
			
		status("killing tick groups.");
		for(int x=0;x<orderedDeaths.size();x++)
		{
			Tick almostTock=(Tick)orderedDeaths.elementAt(x,3);
			Vector objs=new Vector();
            try{
    			for(Iterator e=almostTock.tickers();e.hasNext();)
    				objs.addElement(e.next());
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

		status("checking player sessions.");
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
						String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
                        if((S.previousCMD()==null)||(S.previousCMD().size()==0))
                            Log.errOut("UtiliThread","Kicking out: "+((S.mob()==null)?"Unknown":S.mob().Name())+" who has spent "+time+" millis in creation (probably).");
                        else
                        {
    						Log.errOut("UtiliThread","KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time);
    						Log.errOut("UtiliThread","STATUS  was :"+S.getStatus()+", "+"LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
                        }
						if(S instanceof Thread)
							debugDumpStack((Thread)S);
						status("killing session ");
                        CMLib.sessions().stopSessionAtAllCosts(S);
						status("checking player sessions.");
					}
					else
					if(time>check)
					{
                        if((S.mob()==null)||(S.mob().Name()==null)||(S.mob().Name().length()==0))
                            CMLib.sessions().stopSessionAtAllCosts(S);
                        else
                        if((S.previousCMD()!=null)&&(S.previousCMD().size()>0))
                        {
    						String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
                            if((S.isLockedUpWriting())
                            &&(CMLib.flags().isInTheGame(S.mob(),true)))
                            {
                                Log.errOut("UtiliThread","LOGGED OFF Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time+": "+S.isLockedUpWriting());
                                CMLib.sessions().stopSessionAtAllCosts(S);
                            }
                            else
        						Log.errOut("UtiliThread","Suspect Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time);
    						if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
    							Log.errOut("UtiliThread","STATUS  is :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
    						else
    							Log.errOut("UtiliThread","STATUS  is :"+S.getStatus()+", no last command available.");
                        }
					}
				}
				else
				if(time>(60000))
				{
					String roomID=S.mob()!=null?CMLib.map().getExtendedRoomID(S.mob().location()):"";
					Log.errOut("UtiliThread","KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+" ("+roomID+"), out for "+time);
					if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
					Log.errOut("UtiliThread","STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
					status("killing session ");
					if(S instanceof Thread)
						debugDumpStack((Thread)S);
                    CMLib.sessions().stopSessionAtAllCosts(S);
					status("checking player sessions");
				}
			}
		}
	}

	public void shutdown()
	{
		shutDown=true;
		CMLib.killThread(this,500,1);
        started=false;
	}

	public void forceTick()
	{
		if(status.equalsIgnoreCase("sleeping"))
		{
			interrupt();
			return;
		}
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
		
		try{Thread.sleep(Tickable.TIME_TICK*2);}catch(Exception e){started=false;}
		
		while(started)
		{
			try
			{
                while(CMLib.threads().isAllSuspended())
                    try{Thread.sleep(2000);}catch(Exception e){}
				if(!CMSecurity.isDisabled("UTILITHREAD"))
				{
					lastStop=System.currentTimeMillis();
					if(lastStart>0)
					{
						milliTotal+=(lastStop-lastStart);
						tickTotal++;
					}
					status("sleeping");
					Thread.sleep(MudHost.TIME_UTILTHREAD_SLEEP);
					lastStart=System.currentTimeMillis();
					debugging=CMSecurity.isDebugging("UTILITHREAD");
					vacuum();
					checkHealth();
				}
				else
				{
					status("sleeping");
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
