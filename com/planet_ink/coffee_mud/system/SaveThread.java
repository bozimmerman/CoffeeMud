package com.planet_ink.coffee_mud.system;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class SaveThread extends Thread
{
	public static boolean started=false;
	private static boolean shutDown=false;
	public long lastStart=0;
	public long lastStop=0;
	public static long milliTotal=0;
	public static long tickTotal=0;
	public static String status="";

	public SaveThread()
	{
		super("SaveThread");
	}
	
	public void itemSweep()
	{
		status="sweeping";
		long itemKillTime=System.currentTimeMillis();
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			LandTitle T=null;
			for(int a=0;a<R.numAffects();a++)
			{
				Ability A=R.fetchAffect(a);
				if((A!=null)&&(A instanceof LandTitle))
					T=(LandTitle)A;
			}
			if(T!=null){
				status="updating title in "+R.roomID();
				T.updateLot(R,T);
				status="sweeping";
			}
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
				if((mob!=null)&&(mob.lastTickedDateTime()<lastDateTime))
				{
					boolean ticked=ServiceEngine.isTicking(mob,Host.MOB_TICK);
					boolean isDead=mob.amDead();
					String wasFrom=((mob.getStartRoom()!=null)?mob.getStartRoom().roomID():"NULL");
					Log.errOut("SaveThread",mob.name()+" in room "+R.roomID()+" unticked (is ticking="+(ticked)+", dead="+isDead+", Home="+wasFrom+") since: "+IQCalendar.d2String(mob.lastTickedDateTime())+"."+(ticked?"":"  This mob has been destroyed. May he rest in peace."));
					if(!ticked)
					{
						status="destroying unticked mob "+mob.name();
						mob.destroy();
						R.delInhabitant(mob);
						status="checking";
					}
				}
			}
		}

		status="checking tick groups.";
		Vector tryToKill=new Vector();
		for(Enumeration v=ServiceEngine.tickGroups();v.hasMoreElements();)
		{
			Tick almostTock=(Tick)v.nextElement();
			if((almostTock.awake)
			&&(almostTock.lastStop<lastDateTime))
			{
				TockClient client=almostTock.lastClient;
				if(client!=null)
				{
					if(client.clientObject==null)
						Log.errOut("SaveThread","Dead tick group! Last serviced: NULL, tickID "+client.tickID);
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
							Log.errOut("SaveThread",str.toString()+" in "+((MOB)obj).location().roomID());
						else
						if((obj instanceof Item)&&(((Item)obj).owner()!=null)&&(((Item)obj).owner() instanceof Room))
							Log.errOut("SaveThread",str.toString()+" in "+((Room)((Item)obj).owner()).roomID());
						else
						if((obj instanceof Item)&&(((Item)obj).owner()!=null)&&(((Item)obj).owner() instanceof MOB))
							Log.errOut("SaveThread",str.toString()+" owned by "+((MOB)((Item)obj).owner()).name());
						else
						if(obj instanceof Room)
							Log.errOut("SaveThread",str.toString()+" is "+((Room)obj).roomID());
						else
							Log.errOut("SaveThread",str.toString());
					}
				}
				else
					Log.errOut("SaveThread","Dead tick group! No further information.");
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
			ServiceEngine.delTickGroup(almostTock);
			for(int i=0;i<objs.size();i++)
			{
				TockClient c=(TockClient)objs.elementAt(i);
				ServiceEngine.startTickDown(c.clientObject,c.tickID,c.reTickDown);
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
					if(S.mob().isASysOp(null))
						check=check*15;
					else
					if(S.getStatus()==Session.STATUS_LOGIN)
						check=check*5;
					
					if(time>(check*10))
					{
						Log.errOut("SaveThread","KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+", out for "+time);
						Log.errOut("SaveThread","STATUS  was :"+S.getStatus());
						Log.errOut("SaveThread","LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
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
						Log.errOut("SaveThread","Suspect Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+", out for "+time);
						Log.errOut("SaveThread","STATUS  was :"+S.getStatus());
						Log.errOut("SaveThread","LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
					}
				}
				else
				if(time>(60000))
				{
					Log.errOut("SaveThread","KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().Name())+", out for "+time);
					Log.errOut("SaveThread","STATUS  was :"+S.getStatus());
					Log.errOut("SaveThread","LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
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

		status="checking database health";
		StringBuffer ok=DBConnector.errorStatus();
		if(ok.length()!=0)
			Log.errOut("Save Thread","DB: "+ok);
		else
		{
			long[] levels=new long[2001];
			for(int i=0;i<levels.length;i++) levels[i]=0;
			String mask=CommonStrings.getVar(CommonStrings.SYSTEM_AUTOPURGE);
			Vector maskV=Util.parseCommas(mask.trim());
			for(int mv=0;mv<maskV.size();mv++)
			{
				Vector V=Util.parse(((String)maskV.elementAt(mv)).trim());
				if(V.size()<2) continue;
				int val=Util.s_int((String)V.lastElement());
				if(val<=0) continue;
				String cond=Util.combine(V,0,V.size()-1).trim();
				int start=0;
				int finish=levels.length-1;
				if(cond.startsWith("<="))
					finish=Util.s_int(cond.substring(2).trim());
				else
				if(cond.startsWith(">="))
					start=Util.s_int(cond.substring(2).trim());
				else
				if(cond.startsWith("=="))
				{
					start=Util.s_int(cond.substring(2).trim());
					finish=start;
				}
				else
				if(cond.startsWith(">"))
					start=Util.s_int(cond.substring(2).trim())+1;
				else
				if(cond.startsWith("<"))
					finish=Util.s_int(cond.substring(2).trim())-1;
				
				if((start>=0)&&(finish<levels.length)&&(start<=finish))
				{
					long realVal=System.currentTimeMillis()-(val*1000*60*60*24);
					for(int s=start;s<=finish;s++)
						if(levels[s]==0) levels[s]=realVal;
				}
			}
			
			
			status="autopurge process";
			Vector allUsers=ExternalPlay.getUserList();
			for(int u=0;u<allUsers.size();u++)
			{
				Vector user=(Vector)allUsers.elementAt(u);
				String name=(String)user.elementAt(0);
				if(CMMap.getPlayer(name)!=null) continue;
				int level=Util.s_int((String)user.elementAt(3));
				long last=Util.s_long((String)user.elementAt(5));
				long when=Long.MAX_VALUE;
				if(level>levels.length)
					when=levels[levels.length-1];
				else
				if(level>=0)
					when=levels[level];
				else
					continue;
				
				if(last<when)
				{
					MOB M=CMMap.getLoadPlayer(name);
					if(M!=null)
					{
						ExternalPlay.destroyUser(M);
						Log.sysOut("SaveThread","AutoPurged user "+name+". Last logged in "+(new IQCalendar(last).d2String())+".");
					}
				}
			}
		}
	}

	public void shutdown()
	{
		shutDown=true;
		this.interrupt();
	}

	public int savePlayers()
	{
		int processed=0;
		for(Enumeration p=CMMap.players();p.hasMoreElements();)
		{
			MOB mob=(MOB)p.nextElement();
			if(!mob.isMonster())
			{
				status="saving "+mob.Name();
				MOBloader.DBUpdate(mob);
				status="saving followers of "+mob.Name();
				MOBloader.DBUpdateFollowers(mob);
				processed++;
			}
			else
			if((mob.playerStats()!=null)
			&&((mob.playerStats().lastUpdated()==0)
			   ||(mob.playerStats().lastUpdated()<mob.playerStats().lastDateTime())))
			{
				status="just saving "+mob.Name();
				MOBloader.DBUpdate(mob);
				processed++;
			}
		}
		return processed;
	}
	
	public void run()
	{
		lastStart=System.currentTimeMillis();
		if(started)
		{
			System.out.println("DUPLICATE SAVETHREAD RUNNING!!");
			return;
		}
		started=true;
		while(true)
		{
			try
			{
				itemSweep();
				checkHealth();
				status="ticking the first area for time";
				if(CMMap.numAreas()>0) CMMap.getFirstArea().tickTock(1);
				lastStop=System.currentTimeMillis();
				milliTotal+=(lastStop-lastStart);
				tickTotal++;
				status="sleeping";
				Thread.sleep(Host.TIME_TICK_DELAY);
				lastStart=System.currentTimeMillis();
				int processed=savePlayers();
				//if(processed>0)
				//	Log.sysOut("SaveThread","Saved "+processed+" mobs.");
			}
			catch(InterruptedException ioe)
			{
				Log.sysOut("SaveThread","Interrupted!");
				if(shutDown)
				{
					shutDown=false;
					started=false;
					break;
				}
			}
			catch(Exception e)
			{
				Log.errOut("SaveThread",e);
			}
		}

		// force final time save!
		if(CMMap.numAreas()>0) CMMap.getFirstArea().tickTock(1);

		Log.sysOut("SaveThread","Shutdown complete.");
	}
}
