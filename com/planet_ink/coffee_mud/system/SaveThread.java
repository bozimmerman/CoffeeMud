package com.planet_ink.coffee_mud.system;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class SaveThread extends Thread
{
	public static boolean started=false;
	private static boolean shutDown=false;
	public boolean reset=true;
	public int timeSaveTicker=0;
	public long lastStart=0;
	public long lastStop=0;
	public static long milliTotal=0;
	public static long tickTotal=0;

	public SaveThread()
	{
		super("SaveThread");
	}	
	
	public void itemSweep()
	{
		long itemKillTime=System.currentTimeMillis();
		for(Iterator r=CMMap.rooms();r.hasNext();)
		{
			Room R=(Room)r.next();
			LandTitle T=null;
			for(int a=0;a<R.numAffects();a++)
			{
				Ability A=R.fetchAffect(a);
				if((A!=null)&&(A instanceof LandTitle))
					T=(LandTitle)A;
			}
			if(T!=null)	T.updateLot(R,T);
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)&&(I.dispossessionTime()!=0)&&(I.owner()==R))
				{
					if(itemKillTime>I.dispossessionTime())
					{
						I.destroyThis();
						i=i-1;
					}
				}
			}
		}
	}
	
	public void tickTock()
	{
		if(CMMap.numAreas()<=0)
			return;
		Area A=CMMap.getFirstArea();
		if(reset)
		{
			reset=false;
			StringBuffer timeRsc=Resources.getFileResource("time.txt");
			if(timeRsc.length()<30)
			{
				timeRsc=new StringBuffer("<TIME>-1</TIME><DAY>1</DAY><MONTH>1</MONTH><YEAR>1</YEAR>");
				Resources.updateResource("time.txt",timeRsc);
				Resources.saveFileResource("time.txt");
			}
			Vector V=XMLManager.parseAllXML(timeRsc.toString());
			A.setTimeOfDay(XMLManager.getIntFromPieces(V,"TIME"));
			A.setDayOfMonth(XMLManager.getIntFromPieces(V,"DAY"));
			A.setMonth(XMLManager.getIntFromPieces(V,"MONTH"));
			A.setYear(XMLManager.getIntFromPieces(V,"YEAR"));
		}
		boolean raiseLowerTheSun=A.setTimeOfDay(A.getTimeOfDay()+1);
		if(A.getTimeOfDay()>=Area.A_FULL_DAY)
		{
			raiseLowerTheSun=A.setTimeOfDay(0);
			A.setDayOfMonth(A.getDayOfMonth()+1);
			if(A.getDayOfMonth()>Area.DAYS_IN_MONTH)
			{
				A.setDayOfMonth(1);
				A.setMonth(A.getMonth()+1);
				if(A.getMonth()>Area.MONTHS_IN_YEAR)
				{
					A.setMonth(1);
					A.setYear(A.getYear()+1);
				}
			}
		}
		if(raiseLowerTheSun) raiseLowerTheSunEverywhere();
		
		if((++timeSaveTicker)<Host.TIME_SAVE_DELAY)
			return;
		timeSaveTicker=0;
		StringBuffer timeRsc=new StringBuffer("<DAY>"+A.getDayOfMonth()+"</DAY><MONTH>"+A.getMonth()+"</MONTH><YEAR>"+A.getYear()+"</YEAR>");
		Resources.updateResource("time.txt",timeRsc);
		Resources.saveFileResource("time.txt");
	}
	
	public void checkHealth()
	{
		long lastDateTime=System.currentTimeMillis();
		lastDateTime-=(20*IQCalendar.MILI_MINUTE);
		for(Iterator r=CMMap.rooms();r.hasNext();)
		{
			Room R=(Room)r.next();
			for(int m=0;m<R.numInhabitants();m++)
			{
				MOB mob=(MOB)R.fetchInhabitant(m);
				if((mob!=null)&&(mob.lastTickedDateTime()<lastDateTime))
				{
					boolean ticked=ServiceEngine.isTicking(mob,Host.MOB_TICK);
					Log.errOut("SaveThread",mob.name()+" in room "+R.ID()+" unticked ("+(!ticked)+") since: "+IQCalendar.d2String(mob.lastTickedDateTime())+".");
				}
			}
		}

		for(int v=0;v<ServiceEngine.tickGroup.size();v++)
		{
			Tick almostTock=(Tick)ServiceEngine.tickGroup.elementAt(v);
			if((almostTock.awake)
			&&(almostTock.lastStop<lastDateTime))
			{
				TockClient client=almostTock.lastClient;
				if(client!=null)
					Log.errOut("SaveThread","Dead tick group! Last serviced: "+client.clientObject.ID()+", tickID "+client.tickID+".");
				else
					Log.errOut("SaveThread","Dead tick group! No further information.");

			}
		}
		
		for(int s=0;s<Sessions.size();s++)
		{
			TelnetSession S=(TelnetSession)Sessions.elementAt(s);
			long time=System.currentTimeMillis()-S.lastLoopTime();
			if(time>0)
			{
				if((S.mob()!=null))
				{
					long check=60000;
					if(S.mob().isASysOp(null))
						check=check*10;
					if(S.getStatus()==Session.STATUS_LOGIN)
						check=check*5;
					if(time>(check*10))
					{
						Log.errOut("SaveThread","KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().name())+", out for "+time);
						Log.errOut("SaveThread","STATUS  was :"+S.getStatus());
						Log.errOut("SaveThread","LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
						S.interrupt();
					}
					else
					if(time>check)
						Log.errOut("SaveThread","Suspect Session: "+((S.mob()==null)?"Unknown":S.mob().name())+", out for "+time);
				}
				else
				if(time>(60000))
				{
					Log.errOut("SaveThread","KILLING DEAD Session: "+((S.mob()==null)?"Unknown":S.mob().name())+", out for "+time);
					Log.errOut("SaveThread","STATUS  was :"+S.getStatus());
					Log.errOut("SaveThread","LASTCMD was :"+((S.previousCMD()!=null)?S.previousCMD().toString():""));
					S.interrupt();
				}
			}
		}
		
		StringBuffer ok=DBConnector.errorStatus();
		if(ok.length()!=0)
			Log.errOut("Save Thread","DB: "+ok);

	}
	
	public void raiseLowerTheSunEverywhere()
	{
		if(CMMap.numAreas()==0) return;
		
		Area A=CMMap.getFirstArea();
		for(Iterator r=CMMap.rooms();r.hasNext();)
		{
			Room R=(Room)r.next();
			if((R!=null)&&((R.numInhabitants()>0)||(R.numItems()>0)))
			{
				R.recoverEnvStats();
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB mob=R.fetchInhabitant(m);
					if(!mob.isMonster())
					{
						if(((R.domainType()&Room.INDOORS)==0)
						&&(!Sense.isSleeping(mob))
						&&(Sense.canSee(mob)))
						{
							switch(A.getTODCode())
							{
							case Area.TIME_DAWN:
								mob.tell("The sun begins to rise in the west."); 
								break;
							case Area.TIME_DAY:
								break;
								//mob.tell("The sun is now shining brightly."); break;
							case Area.TIME_DUSK:
								mob.tell("The sun begins to set in the east."); break;
							case Area.TIME_NIGHT:
								mob.tell("The sun has set and darkness again covers the world."); break;
							}
						}
						else
						{
							switch(A.getTODCode())
							{
							case Area.TIME_DAWN:
								mob.tell("It is now daytime."); break;
							case Area.TIME_DAY: break;
								//mob.tell("The sun is now shining brightly."); break;
							case Area.TIME_DUSK: break;
								//mob.tell("It is almost nighttime."); break;
							case Area.TIME_NIGHT:
								mob.tell("It is nighttime."); break;
							}
						}
					}
				}
			}
			R.recoverRoomStats();
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
				tickTock();
				int processed=0;
				lastStop=System.currentTimeMillis();
				milliTotal+=(lastStop-lastStart);
				tickTotal++;
				Thread.sleep(Host.TIME_TICK_DELAY);
				lastStart=System.currentTimeMillis();
				for(Iterator p=CMMap.players();p.hasNext();)
				{
					MOB mob=(MOB)p.next();
					if(!mob.isMonster())
					{
						MOBloader.DBUpdate(mob);
						MOBloader.DBUpdateFollowers(mob);
						processed++;
					}
					else
					if((mob.lastUpdated()==0)||(mob.lastUpdated()<mob.lastDateTime()))
					{
						MOBloader.DBUpdate(mob);
						processed++;
					}
				}
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
		timeSaveTicker=19;
		tickTock();
		
		Log.sysOut("SaveThread","Shutdown complete.");
	}
}
