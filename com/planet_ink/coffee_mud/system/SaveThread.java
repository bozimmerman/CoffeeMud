package com.planet_ink.coffee_mud.system;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class SaveThread extends Thread
{
	public static boolean started=false;
	private static boolean shutDown=false;

	public Calendar lastDateTime=Calendar.getInstance();
	
	
	public SaveThread()
	{
		super("SaveThread");
	}	
	
	public void checkHealth()
	{
		Calendar itemKillTime=Calendar.getInstance();
		itemKillTime.add(Calendar.HOUR,-20);
		
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
				checkHealth();
				lastDateTime=Calendar.getInstance();
				int processed=0;
				Thread.sleep(10*60000);
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
		Log.sysOut("SaveThread","Shutdown complete.");
	}
}
