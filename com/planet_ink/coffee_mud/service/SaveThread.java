package com.planet_ink.coffee_mud.service;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.db.*;
import java.util.*;

public class SaveThread extends Thread 
{
	public static boolean started=false;
	
	public Calendar lastDateTime=Calendar.getInstance();
	public void checkHealth()
	{
		for(int mn=0;mn<MUD.map.size();mn++)
		{
			Room room=(Room)MUD.map.elementAt(mn);
			for(int m=0;m<room.numInhabitants();m++)
			{
				MOB mob=(MOB)room.fetchInhabitant(m);
				if(mob instanceof StdMOB)
				{
					if(((StdMOB)mob).lastTickedDateTime.before(lastDateTime))
					{
						boolean ticked=ServiceEngine.isTicking(mob,ServiceEngine.MOB_TICK);
						Log.errOut("SaveThread",mob.name()+" in room "+room.ID()+" unticked ("+(!ticked)+") since: "+new IQCalendar(((StdMOB)mob).lastTickedDateTime).d2String()+".");
					}
				}
				
			}
		}
		
		for(int v=0;v<ServiceEngine.tickGroup.size();v++)
		{
			Tick almostTock=(Tick)ServiceEngine.tickGroup.elementAt(v);
			if((almostTock.awake)
			||(almostTock.lastAwoke.before(lastDateTime)))
			{
				TockClient client=almostTock.lastClient;
				if(client!=null)
					Log.errOut("SaveThread","Dead tick group! Last serviced: "+client.clientObject.ID()+", tickID "+client.tickID+".");
				else
					Log.errOut("SaveThread","Dead tick group! No further information.");
				
			}
		}
		StringBuffer ok=MUD.DBs.errorStatus();
		if(ok.length()!=0)
			Log.errOut("Save Thread","DB: "+ok);
			
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
				Vector V=null;
				for(Enumeration e=MOBloader.MOBs.elements();e.hasMoreElements();)
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
				Log.errOut("SaveThread","Interrupted!");
			}
		}
	}
}
