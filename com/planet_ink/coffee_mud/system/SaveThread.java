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
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			LandTitle T=CoffeeUtensils.getLandTitle(R);
			if(T!=null)
			{
				status="updating title in "+R.roomID();
				T.updateLot();
				status="sweeping";
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
				MOBloader.DBUpdateJustMOB(mob);
				if((mob.Name().length()==0)||(mob.playerStats()==null))
					continue;
				status="saving "+mob.Name()+", "+mob.inventorySize()+"items";
				MOBloader.DBUpdateItems(mob);
				status="saving "+mob.Name()+", "+mob.numLearnedAbilities()+"abilities";
				MOBloader.DBUpdateAbilities(mob);
				status="saving "+mob.numFollowers()+" followers of "+mob.Name();
				MOBloader.DBUpdateFollowers(mob);
				mob.playerStats().setUpdated(System.currentTimeMillis());
				processed++;
			}
			else
			if((mob.playerStats()!=null)
			&&((mob.playerStats().lastUpdated()==0)
			   ||(mob.playerStats().lastUpdated()<mob.playerStats().lastDateTime())))
			{
				status="just saving "+mob.Name();
				MOBloader.DBUpdateJustMOB(mob);
				if((mob.Name().length()==0)||(mob.playerStats()==null))
					continue;
				status="just saving "+mob.Name()+", "+mob.inventorySize()+" items";
				MOBloader.DBUpdateItems(mob);
				status="just saving "+mob.Name()+", "+mob.numLearnedAbilities()+" abilities";
				MOBloader.DBUpdateAbilities(mob);
				mob.playerStats().setUpdated(System.currentTimeMillis());
				processed++;
			}
		}
		return processed;
	}

	public boolean autoPurge()
	{
		long[] levels=new long[2001];
		for(int i=0;i<levels.length;i++) levels[i]=0;
		String mask=CommonStrings.getVar(CommonStrings.SYSTEM_AUTOPURGE);
		Vector maskV=Util.parseCommas(mask.trim(),false);
		for(int mv=0;mv<maskV.size();mv++)
		{
			Vector V=Util.parse(((String)maskV.elementAt(mv)).trim());
			if(V.size()<2) continue;
			long val=Util.s_long((String)V.lastElement());
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
			if(cond.startsWith("="))
			{
				start=Util.s_int(cond.substring(1).trim());
				finish=start;
			}
			else
			if(cond.startsWith(">"))
				start=Util.s_int(cond.substring(1).trim())+1;
			else
			if(cond.startsWith("<"))
				finish=Util.s_int(cond.substring(1).trim())-1;

			if((start>=0)&&(finish<levels.length)&&(start<=finish))
			{
				long realVal=System.currentTimeMillis()-((long)(val*1000*60*60*24));
				for(int s=start;s<=finish;s++)
					if(levels[s]==0) levels[s]=realVal;
			}

		}
		status="autopurge process";
		Vector allUsers=CMClass.DBEngine().getUserList();
		Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
		if(protectedOnes==null) protectedOnes=new Vector();

		for(int u=0;u<allUsers.size();u++)
		{
			Vector user=(Vector)allUsers.elementAt(u);
			String name=(String)user.elementAt(0);
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
				boolean protectedOne=false;
				for(int p=0;p<protectedOnes.size();p++)
				{
					String P=(String)protectedOnes.elementAt(p);
					if(P.equalsIgnoreCase(name))
					{ protectedOne=true; break;	}
				}
				if(!protectedOne)
				{
					MOB M=CMMap.getLoadPlayer(name);
					if(M!=null)
					{
						CoffeeUtensils.obliteratePlayer(M,true);
						Log.sysOut("SaveThread","AutoPurged user "+name+". Last logged in "+(new IQCalendar(last).d2String())+".");
					}
				}
			}
		}
		return true;
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
				if(!CommonStrings.isDisabled("SAVETHREAD"))
				{
					status="checking database health";
					StringBuffer ok=DBConnector.errorStatus();
					if((ok.length()!=0)&&(!ok.toString().startsWith("OK")))
						Log.errOut("Save Thread","DB: "+ok);
					else
					{
						itemSweep();
						autoPurge();
						CoffeeTables.bump(null,CoffeeTables.STAT_SPECIAL_NUMONLINE);
						CoffeeTables.update();
						lastStop=System.currentTimeMillis();
						milliTotal+=(lastStop-lastStart);
						tickTotal++;
						status="sleeping";
						Thread.sleep(MudHost.TIME_SAVETHREAD_SLEEP);
						lastStart=System.currentTimeMillis();
						savePlayers();
						//if(processed>0)
						//	Log.sysOut("SaveThread","Saved "+processed+" mobs.");
					}
				}
				else
				{
					status="sleeping";
					Thread.sleep(MudHost.TIME_SAVETHREAD_SLEEP);
				}
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

		Log.sysOut("SaveThread","Shutdown complete.");
	}
}
