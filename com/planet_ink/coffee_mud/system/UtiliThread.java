package com.planet_ink.coffee_mud.system;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
		status="sweeping";
		long itemKillTime=System.currentTimeMillis();
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)&&(I.dispossessionTime()!=0)&&(I.owner()==R))
				{
					if(itemKillTime>I.dispossessionTime())
					{
						status="destroying "+I.Name();
						I.destroy();
						status="sweeping";
						i=i-1;
					}
				}
			}
		}
	}

	public void checkHealth()
	{
		long lastDateTime=System.currentTimeMillis();
		lastDateTime-=(20*IQCalendar.MILI_MINUTE);
		status="checking";
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB mob=(MOB)R.fetchInhabitant(m);
				if((mob!=null)&&(mob.lastTickedDateTime()>0)&&(mob.lastTickedDateTime()<lastDateTime))
				{
					boolean ticked=CMClass.ThreadEngine().isTicking(mob,MudHost.TICK_MOB);
					boolean isDead=mob.amDead();
					String wasFrom=((mob.getStartRoom()!=null)?mob.getStartRoom().roomID():"NULL");
					if(CMMap.getPlayer(mob.Name())==null)
						Log.errOut("UtiliThread",mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+IQCalendar.d2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been destroyed. May he rest in peace."));
					else
						Log.errOut("UtiliThread","Player "+mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+IQCalendar.d2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been put aside."));
					if(!ticked)
					{
						status="destroying unticked mob "+mob.name();
						if(CMMap.getPlayer(mob.Name())==null) mob.destroy();
						R.delInhabitant(mob);
						status="checking";
					}
				}
			}
		}

		status="checking tick groups.";
		Vector tryToKill=new Vector();
		for(Enumeration v=CMClass.ThreadEngine().tickGroups();v.hasMoreElements();)
		{
			Tick almostTock=(Tick)v.nextElement();
			if((almostTock.awake)
			&&(almostTock.lastStop<lastDateTime))
			{
				TockClient client=almostTock.lastClient;
				if(client!=null)
				{
					if(client.clientObject==null)
						Log.errOut("UtiliThread","Dead tick group! Last serviced: NULL, tickID "+client.tickID);
					else
					{
						StringBuffer str=null;
						Tickable obj=client.clientObject;
						long code=client.clientObject.getTickStatus();
						String codeWord=client.tickCodeWord();
						if(obj instanceof Environmental)
							str=new StringBuffer("Dead tick group! Last serviced: "+obj.name()+" ("+((Environmental)obj).ID()+"), Status="+code+" ("+codeWord+"), tickID "+client.tickID);
						else
							str=new StringBuffer("Dead tick group! Last serviced: "+obj.name()+", Status="+code+" ("+codeWord+"), tickID "+client.tickID);

						if((obj instanceof MOB)&&(((MOB)obj).location()!=null))
							Log.errOut("UtiliThread",str.toString()+" in "+((MOB)obj).location().roomID());
						else
						if((obj instanceof Item)&&(((Item)obj).owner()!=null)&&(((Item)obj).owner() instanceof Room))
							Log.errOut("UtiliThread",str.toString()+" in "+((Room)((Item)obj).owner()).roomID());
						else
						if((obj instanceof Item)&&(((Item)obj).owner()!=null)&&(((Item)obj).owner() instanceof MOB))
							Log.errOut("UtiliThread",str.toString()+" owned by "+((MOB)((Item)obj).owner()).name());
						else
						if(obj instanceof Room)
							Log.errOut("UtiliThread",str.toString()+" is "+((Room)obj).roomID());
						else
							Log.errOut("UtiliThread",str.toString());
					}
				}
				else
					Log.errOut("UtiliThread","Dead tick group! No further information.");
				tryToKill.addElement(almostTock);
			}
		}
		status="killing tick groups.";
		for(int x=0;x<tryToKill.size();x++)
		{
			Tick almostTock=(Tick)tryToKill.elementAt(x);
			Vector objs=new Vector();
			for(Enumeration e=almostTock.tickers();e.hasMoreElements();)
				objs.addElement(e.nextElement());
			almostTock.shutdown();
			if(CMClass.ThreadEngine() instanceof ServiceEngine)
				((ServiceEngine)CMClass.ThreadEngine()).delTickGroup(almostTock);
			for(int i=0;i<objs.size();i++)
			{
				TockClient c=(TockClient)objs.elementAt(i);
				CMClass.ThreadEngine().startTickDown(c.clientObject,c.tickID,c.reTickDown);
			}
		}

		status="checking sessions.";
		for(int s=0;s<Sessions.size();s++)
		{
			TelnetSession S=(TelnetSession)Sessions.elementAt(s);
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
						S.setKillFlag(true);
						S.interrupt();
						try{Thread.sleep(500);}catch(Exception e){}
						S.interrupt();
						Sessions.removeElement(S);
						status="checking sessions.";
					}
					else
					if(time>check)
					{
						Log.errOut("UtiliThread","Suspect Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+", out for "+time);
						if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
							Log.errOut("UtiliThread","STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
					}
				}
				else
				if(time>(60000))
				{
					Log.errOut("UtiliThread","KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+", out for "+time);
					if((S.getStatus()!=1)||((S.previousCMD()!=null)&&(S.previousCMD().size()>0)))
					Log.errOut("UtiliThread","STATUS  was :"+S.getStatus()+", LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
					status="killing session ";
					S.setKillFlag(true);
					S.interrupt();
					try{Thread.sleep(500);}catch(Exception e){}
					S.interrupt();
					Sessions.removeElement(S);
					status="checking sessions.";
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
			System.out.println("DUPLICATE UTILITHREAD RUNNING!!");
			return;
		}
		started=true;
		
		// now start the thread
		while(true)
		{
			try
			{
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
					started=false;
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
		for(Enumeration e=CMMap.areas();e.hasMoreElements();)
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
