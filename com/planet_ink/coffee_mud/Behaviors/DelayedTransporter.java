package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class DelayedTransporter extends ActiveTicker
{
	private Hashtable transportees=new Hashtable();
	private Vector destRoomNames=new Vector();
	
	public DelayedTransporter()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
		destRoomNames=new Vector();
		int x=myParms.indexOf(";");
		if(x>0)
		{
			String parmText=myParms.substring(0,x);
			myParms=myParms.substring(x+1);
			super.setParms(parmText);
		}
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
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		Room room=this.getBehaversRoom(ticking);
		if(room!=null)
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB inhab=room.fetchInhabitant(i);
			if((inhab!=null)&&(!inhab.isMonster()))
			{
				Integer I=(Integer)transportees.get(inhab.name());
				if(I==null)
				{
					I=new Integer(0);
					transportees.put(inhab.name(),I);
				}
				boolean gone=false;
				if(I.intValue()>=minTicks)
					if((Dice.rollPercentage()<chance)||(I.intValue()>maxTicks))
					{
						String roomName=(String)destRoomNames.elementAt(Dice.roll(1,destRoomNames.size(),0)-1);
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
					transportees.put(inhab.name(),I);
				}
			}
		}
	}
}
