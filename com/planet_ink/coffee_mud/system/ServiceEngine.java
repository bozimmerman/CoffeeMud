package com.planet_ink.coffee_mud.system;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class ServiceEngine
{
	public static Vector tickGroup=new Vector();

	public static Tick confirmAndGetTickThread(Environmental E, int tickID)
	{
		Tick tock=null;

		for(int v=0;v<tickGroup.size();v++)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			if((tock==null)&&(almostTock.tickers.size()<Host.MAX_TICK_CLIENTS))
				tock=almostTock;
			for(int t=0;t<almostTock.tickers.size();t++)
			{
				TockClient client=(TockClient)almostTock.tickers.elementAt(t);
				if((client.clientObject==E)&&(client.tickID==tickID))
					return null;
			}
		}

		if(tock!=null)
			return tock;
		else
		{
			tock=new Tick();
			tickGroup.addElement(tock);
			return tock;
		}
	}

	public static void startTickDown(Environmental E,
									 int tickID,
									 int numTicks)
	{
		Tick tock=confirmAndGetTickThread(E,tickID);
		if(tock==null) return;

		TockClient client=new TockClient(E,numTicks,tickID);
		if(client!=null)
			synchronized(tock.tickers)
			{
				tock.tickers.addElement(client);
			}
	}

	public static boolean deleteTick(Environmental E, int tickID)
	{
		for(int v=0;v<tickGroup.size();v++)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			synchronized (almostTock.tickers)
			{
				for(int t=almostTock.tickers.size()-1;t>=0;t--)
				{
					TockClient C=(TockClient)almostTock.tickers.elementAt(t);
					Environmental E2=C.clientObject;
					if((E==E2)&&((tickID==C.tickID)||(tickID<0)))
					{
						almostTock.tickers.removeElement(C);
						if(tickID>=0)
							return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isTicking(Environmental E, int tickID)
	{
		for(int v=0;v<tickGroup.size();v++)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			for(int t=0;t<almostTock.tickers.size();t++)
			{
				TockClient C=(TockClient)almostTock.tickers.elementAt(t);
				Environmental E2=C.clientObject;
				if((E==E2)&&((tickID==C.tickID)||(tickID<0)))
					return true;
			}
		}
		return false;
	}
	
	public static void suspendTicking(Environmental E, int tickID){suspendResumeTicking(E,tickID,true);}
	public static void resumeTicking(Environmental E, int tickID){suspendResumeTicking(E,tickID,false);}
	private static boolean suspendResumeTicking(Environmental E, int tickID, boolean suspend)
	{
		for(int v=0;v<tickGroup.size();v++)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			for(int t=0;t<almostTock.tickers.size();t++)
			{
				TockClient C=(TockClient)almostTock.tickers.elementAt(t);
				Environmental E2=C.clientObject;
				if((E==E2)&&((tickID==C.tickID)||(tickID<0)))
					C.suspended=suspend;
			}
		}
		return false;
	}

	public static boolean isHere(Environmental E2, Room here)
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

	
	public static StringBuffer report()
	{
		StringBuffer buf=new StringBuffer("");
		buf.append("\n\r^xService Engine report:^.^N\n\r");
		int totalTickers=0;
		long totalMillis=0;
		long totalTicks=0;
		int topGroupNumber=-1;
		long topGroupMillis=-1;
		long topGroupTicks=0;
		long topObjectMillis=-1;
		long topObjectTicks=0;
		int topObjectGroup=0;
		Environmental topObjectClient=null;
		for(int v=0;v<tickGroup.size();v++)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			totalTickers+=almostTock.tickers.size();
			totalMillis+=almostTock.milliTotal;
			totalTicks+=almostTock.tickTotal;
			if(almostTock.milliTotal>topGroupMillis)
			{
				topGroupMillis=almostTock.milliTotal;
				topGroupTicks=almostTock.tickTotal;
				topGroupNumber=v;
			}
			for(int i=0;i<almostTock.tickers.size();i++)
			{
				TockClient C=(TockClient)almostTock.tickers.elementAt(i);
				if(C.milliTotal>topObjectMillis)
				{
					topObjectMillis=C.milliTotal;
					topObjectTicks=C.tickTotal;
					topObjectClient=C.clientObject;
					topObjectGroup=v;
				}
			}
		}
			
		buf.append("There are ^H"+totalTickers+"^? ticking objects in ^H"+tickGroup.size()+"^? threads.\n\r");
		buf.append("The ticking objects have consumed: ^H"+Util.returnTime(totalMillis,totalTicks)+"^?.\n\r");
		buf.append("The most active group, #^H"+topGroupNumber+"^?, has consumed: ^H"+Util.returnTime(topGroupMillis,topGroupTicks)+"^?.\n\r");
		if(topObjectClient!=null)
		{
			buf.append("The most active object has been '^H"+topObjectClient.name()+"^?', from group #^H"+topObjectGroup+"^?.\n\r");
			buf.append("That object has consumed: ^H"+Util.returnTime(topObjectMillis,topObjectTicks)+"^?.\n\r");
		}
		buf.append("\n\r");
		buf.append("^xSave Thread report:^.^N\n\r");
		buf.append("The Save Thread has consumed: ^H"+Util.returnTime(SaveThread.milliTotal,SaveThread.tickTotal)+"^?.\n\r");
		buf.append("\n\r");
		buf.append("^xSession report:^.^N\n\r");
		long totalMOBMillis=0;
		long totalMOBTicks=0;
		long topMOBMillis=0;
		long topMOBTicks=0;
		MOB topMOBClient=null;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			totalMOBMillis+=S.getTotalMillis();
			totalMOBTicks+=S.getTotalTicks();
			if(S.getTotalMillis()>topMOBMillis)
			{
				topMOBMillis=S.getTotalMillis();
				topMOBTicks=S.getTotalTicks();
				topMOBClient=S.mob();
			}
		}
		buf.append("There are ^H"+Sessions.size()+"^? ticking players logged on.\n\r");
		buf.append("The ticking players have consumed: ^H"+Util.returnTime(totalMOBMillis,totalMOBTicks)+"^?.\n\r");
		if(topMOBClient!=null)
		{
			buf.append("The most active mob has been '^H"+topMOBClient.name()+"^?'\n\r");
			buf.append("That mob has consumed: ^H"+Util.returnTime(topMOBMillis,topMOBTicks)+"^?.\n\r");
		}
		return buf;
	}
	
	public static void tickAllTickers(Room here)
	{
		for(int v=0;v<tickGroup.size();v++)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			int t=0;
			while(t<almostTock.tickers.size())
			{
				TockClient C=(TockClient)almostTock.tickers.elementAt(t);
				Environmental E2=C.clientObject;
				if(isHere(E2,here))
				{
					if(!Tick.tickTicker(C,almostTock.tickers)) t++;
				}
				else
				if((E2 instanceof Ability)
				&&(isHere(((Ability)E2).affecting(),here)))
				{
					if(!Tick.tickTicker(C,almostTock.tickers)) t++;
				}
				else
					t++;
			}
		}
	}

	public static StringBuffer listTicks(int whichTick)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append(Util.padRight("Grp",4)+Util.padRight("Client",18)+" "+Util.padRight("ID",5)+Util.padRight("Time",10));
		msg.append(Util.padRight("Grp",4)+Util.padRight("Client",18)+" "+Util.padRight("ID",5)+Util.padRight("Time",10)+"\n\r");
		int col=0;
		for(int v=0;v<tickGroup.size();v++)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			if((whichTick<0)||(whichTick==v))
			for(int t=0;t<almostTock.tickers.size();t++)
			{
				TockClient C=(TockClient)almostTock.tickers.elementAt(t);
				Environmental E=C.clientObject;

				if((E instanceof Ability)&&(E.ID().equals("ItemRejuv")))
					E=((Ability)E).affecting();

				int id=C.tickID;
				int pr=C.tickDown;
				int oo=C.reTickDown;
				if((col++)==2)
				{
					msg.append("\n\r");
					col=1;
				}
				msg.append(Util.padRight(""+v,4)+Util.padRight(E.name(),18)+" "+Util.padRight(id+"",5)+Util.padRight(pr+"/"+(C.suspended?"??":""+oo),10));
			}
		}
		return msg;
	}

	public static void shutdownAll()
	{
		Log.errOut("ServiceEngine","Shutting down all ticks...");
		while(tickGroup.size()>0)
		{
			Tick tock=null;
			synchronized(tickGroup){tock=(Tick)tickGroup.elementAt(0);}
			if(tock!=null) tock.shutdown();
			try{Thread.sleep(100);}catch(Exception e){}
		}
		Log.errOut("ServiceEngine","Shutdown complete.");
	}
	
	public synchronized static void clearDebri(Room room, int taskCode)
	{
		for(int v=0;v<tickGroup.size();v++)
		{
			Tick tock=(Tick)tickGroup.elementAt(v);
			synchronized(tock.tickers)
			{
				for(int o=tock.tickers.size()-1;o>=0;o--)
				{
					TockClient C=(TockClient)tock.tickers.elementAt(o);
					if((C.clientObject instanceof ItemTicker)&&(taskCode<2))
					{
						ItemTicker I=(ItemTicker)C.clientObject;
						if(I.properLocation()==room)
						{
							tock.tickers.removeElement(C);
							I.setProperLocation(null);
						}
					}
					else
					if((C.clientObject instanceof MOB)&&((taskCode==0)||(taskCode==2)))
					{
						MOB mob=(MOB)C.clientObject;
						if((mob.getStartRoom()==room)
						&&(mob.isMonster())
						&&(!room.isInhabitant(mob)))
						{
							mob.destroy();
							tock.tickers.removeElement(C);
						}
					}
				}
			}
		}
	}
}
