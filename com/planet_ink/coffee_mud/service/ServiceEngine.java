package com.planet_ink.coffee_mud.service;

import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.ItemRejuv;

public class ServiceEngine
{
	public static final int MOB_TICK=0;
	
	public static final int EXIT_REOPEN=2;
	public static final int DEADBODY_DECAY=3;
	public static final int LIGHT_FLICKERS=4;
	public static final int TRAP_RESET=5;
	public static final int TRAP_DESTRUCTION=6;
	public static final int ITEM_BOUNCEBACK=7;
	
	public final static int ROOM_ITEM_REJUV=10;
	
	public final static int SPELL_AFFECT=12;
	
	public static Vector tickGroup=new Vector();
	
	public static Tick confirmAndGetTickThread(Environmental E, int tickID)
	{
		Tick tock=null;
		
		int v=tickGroup.size()-1;
		while(v>=0)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			if(almostTock.tickers.size()==0)
				tickGroup.removeElementAt(v);
			else
			{
				if((tock==null)&&(almostTock.tickers.size()<25))
					tock=almostTock;
				for(int t=0;t<almostTock.tickers.size();t++)
				{
					TockClient client=(TockClient)almostTock.tickers.elementAt(t);
					if((client.clientObject==E)&&(client.tickID==tickID))
						return null;
				}
			}
			v--;
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
		
		boolean ok=true;
		TockClient client=new TockClient(E,numTicks,tickID);
		if(client!=null)
			tock.tickers.addElement(client);
	}
	
	public static boolean deleteTick(Environmental E, int tickID)
	{
		for(int v=0;v<tickGroup.size();v++)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			for(int t=0;t<almostTock.tickers.size();t++)
			{
				TockClient C=(TockClient)almostTock.tickers.elementAt(t);
				Environmental E2=C.clientObject;
				if((E==E2)&&(tickID==C.tickID))
				{
					almostTock.tickers.removeElement(C);
					return true;
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
				if((E==E2)&&(tickID==C.tickID))
					return true;
			}
		}
		return false;
	}
	
	public static StringBuffer listTicks()
	{
		StringBuffer msg=new StringBuffer("");
		msg.append(Util.padRight("Grp",4)+Util.padRight("Client",18)+" "+Util.padRight("ID",5)+Util.padRight("Time",10));
		msg.append(Util.padRight("Grp",4)+Util.padRight("Client",18)+" "+Util.padRight("ID",5)+Util.padRight("Time",10)+"\n\r");
		int col=0;
		for(int v=0;v<tickGroup.size();v++)
		{
			Tick almostTock=(Tick)tickGroup.elementAt(v);
			for(int t=0;t<almostTock.tickers.size();t++)
			{
				TockClient C=(TockClient)almostTock.tickers.elementAt(t);
				Environmental E=C.clientObject;
				
				if(E instanceof ItemRejuv)
					E=((ItemRejuv)E).affecting();
				
				int id=C.tickID;
				int pr=C.tickDown;
				int oo=C.reTickDown;
				if((col++)==2)
				{
					msg.append("\n\r");
					col=1;
				}
				msg.append(Util.padRight(""+v,4)+Util.padRight(E.ID(),18)+" "+Util.padRight(id+"",5)+Util.padRight(pr+"/"+oo,10));
			}
		}
		return msg;
	}
}
