package com.planet_ink.coffee_mud.system;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class SaveThread extends Thread
{
	public static boolean started=false;
	private static boolean shutDown=false;
	public final static int SAVE_DELAY=10*60000; // 10 minutes, right now.
	public boolean reset=true;
	public final static int TIME_SAVE_DELAY=18; // 3 hours...
	public int timeSaveTicker=0;

	public Calendar lastDateTime=Calendar.getInstance();
	
	
	public SaveThread()
	{
		super("SaveThread");
	}	
	
	public void itemSweep()
	{
		Calendar itemKillTime=Calendar.getInstance();
		itemKillTime.add(Calendar.HOUR,-20);
		
		for(int mn=0;mn<CMMap.numRooms();mn++)
		{
			Room room=CMMap.getRoom(mn);
			for(int i=0;i<room.numItems();i++)
			{
				Item I=room.fetchItem(i);
				if((I!=null)&&(I.possessionTime()!=null)&&(I.myOwner()==room))
				{
					if(itemKillTime.after(I.possessionTime()))
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
		Area A=CMMap.getArea(0);
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
		
		if((++timeSaveTicker)<TIME_SAVE_DELAY)
			return;
		timeSaveTicker=0;
		StringBuffer timeRsc=new StringBuffer("<DAY>"+A.getDayOfMonth()+"</DAY><MONTH>"+A.getMonth()+"</MONTH><YEAR>"+A.getYear()+"</YEAR>");
		Resources.updateResource("time.txt",timeRsc);
		Resources.saveFileResource("time.txt");
	}
	
	public void checkHealth()
	{
		for(int mn=0;mn<CMMap.numRooms();mn++)
		{
			Room room=CMMap.getRoom(mn);
			for(int m=0;m<room.numInhabitants();m++)
			{
				MOB mob=(MOB)room.fetchInhabitant(m);
				if((mob!=null)&&(mob.lastTickedDateTime().before(lastDateTime)))
				{
					boolean ticked=ServiceEngine.isTicking(mob,Host.MOB_TICK);
					Log.errOut("SaveThread",mob.name()+" in room "+room.ID()+" unticked ("+(!ticked)+") since: "+new IQCalendar(mob.lastTickedDateTime()).d2String()+".");
				}
			}
		}

		for(int v=0;v<ServiceEngine.tickGroup.size();v++)
		{
			Tick almostTock=(Tick)ServiceEngine.tickGroup.elementAt(v);
			if((almostTock.awake)
			&&(almostTock.lastAwoke.before(lastDateTime)))
			{
				TockClient client=almostTock.lastClient;
				if(client!=null)
					Log.errOut("SaveThread","Dead tick group! Last serviced: "+client.clientObject.ID()+", tickID "+client.tickID+".");
				else
					Log.errOut("SaveThread","Dead tick group! No further information.");

			}
		}
		StringBuffer ok=DBConnector.errorStatus();
		if(ok.length()!=0)
			Log.errOut("Save Thread","DB: "+ok);

	}
	
	public void raiseLowerTheSunEverywhere()
	{
		if(CMMap.numAreas()==0) return;
		
		Area A=CMMap.getArea(0);
		for(int r=0;r<CMMap.numRooms();r++)
		{
			Room room=CMMap.getRoom(r);
			if((room!=null)&&((room.numInhabitants()>0)||(room.numItems()>0)))
			{
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB mob=room.fetchInhabitant(m);
					if(!mob.isMonster())
					{
						if(((room.domainType()&Room.INDOORS)==0)
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
				room.recoverRoomStats();
			}
			else
				room.recoverEnvStats();
		}
	}

	public void shutdown()
	{
		shutDown=true;
		this.interrupt();
	}

	public void run()
	{
		if(started)
		{
			System.out.println("DUPLICATE SAVETHREAD RUNNING!!");
			return;
		}
		started=true;
		lastDateTime.add(Calendar.MINUTE,-20);
		while(true)
		{
			try
			{
				itemSweep();
				checkHealth();
				tickTock();
				lastDateTime=Calendar.getInstance();
				int processed=0;
				Thread.sleep(SAVE_DELAY);
				for(Enumeration e=CMMap.MOBs.elements();e.hasMoreElements();)
				{
					MOB mob=(MOB)e.nextElement();
					if(!mob.isMonster())
					{
						MOBloader.DBUpdate(mob);
						MOBloader.DBUpdateFollowers(mob);
						processed++;
					}
					else
					if(mob.lastDateTime().after(lastDateTime))
					{
						MOBloader.DBUpdate(mob);
						processed++;
					}
				}
				if(processed>0)
					Log.sysOut("SaveThread","Saved "+processed+" mobs.");
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
		}
		
		// force final time save!
		timeSaveTicker=19;
		tickTock();
		
		Log.sysOut("SaveThread","Shutdown complete.");
	}
}
