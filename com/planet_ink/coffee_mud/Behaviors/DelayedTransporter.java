package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class DelayedTransporter extends ActiveTicker
{
	public String ID(){return "DelayedTransporter";}
	private Hashtable transportees=new Hashtable();
	private Vector destRoomNames=new Vector();
	protected int canImproveCode(){return Behavior.CAN_ITEMS|Behavior.CAN_MOBS|Behavior.CAN_ROOMS;}

	public DelayedTransporter()
	{
		minTicks=5;maxTicks=5;chance=100;
		tickReset();
	}
	public Behavior newInstance()
	{
		return new DelayedTransporter();
	}

	public void setParms(String newParms)
	{
		String myParms=newParms;
		int x=myParms.indexOf(";");
		if(x>0)
		{
			String parmText=myParms.substring(0,x);
			myParms=myParms.substring(x+1);
			super.setParms(parmText);
		}
		destRoomNames=new Vector();
		while(myParms.length()>0)
		{
			String thisRoom=myParms;
			x=myParms.indexOf(";");
			if(x>0)
			{
				thisRoom=myParms.substring(0,x);
				myParms=myParms.substring(x+1);
			}
			else
				myParms="";

			if(CMMap.getRoom(thisRoom)!=null)
				destRoomNames.addElement(thisRoom);
		}
		parms=newParms;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		Room room=this.getBehaversRoom(ticking);
		if((room!=null)&&(destRoomNames!=null)&&(destRoomNames.size()>0))
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB inhab=room.fetchInhabitant(i);
			if(inhab!=null)
			{
				Integer I=(Integer)transportees.get(inhab.Name());
				if(I==null)
				{
					I=new Integer(0);
					transportees.put(inhab.Name(),I);
				}
				boolean gone=false;
				if(I.intValue()>=minTicks)
					if((Dice.rollPercentage()<chance)||(I.intValue()>maxTicks))
					{
						String roomName=(String)destRoomNames.elementAt(Dice.roll(1,destRoomNames.size(),-1));
						Room otherRoom=CMMap.getRoom(roomName);
						if(otherRoom==null)
							inhab.tell("You are whisked nowhere at all, since '"+roomName+"' is nowhere to be found.");
						else
							otherRoom.bringMobHere(inhab,true);
						transportees.remove(I);
						gone=true;
					}
				if(!gone)
				{
					I=new Integer(I.intValue()+1);
					transportees.put(inhab.Name(),I);
				}
			}
		}
		return true;
	}
}
