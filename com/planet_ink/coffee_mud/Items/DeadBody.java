package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class DeadBody extends Container
{
	protected Room roomLocation=null;
	
	public DeadBody()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="the body of someone";
		displayText="the body of someone lies here.";
		description="Bloody and bruised, obviously mistreated.";
		properWornBitmap=0;
		baseEnvStats.setWeight(150);
		baseEnvStats.setRejuv(100);
		capacity=5;
		baseGoldValue=0;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new DeadBody();
	}
	public void startTicker(Room thisRoom)
	{
		roomLocation=thisRoom;
		ServiceEngine.startTickDown(this,ServiceEngine.DEADBODY_DECAY,envStats().rejuv());
	}
	public boolean tick(int tickID)
	{
		if(tickID==ServiceEngine.DEADBODY_DECAY)
		{
			destroyThis();
			roomLocation.recoverRoomStats();
			return false;
		}
		else
			return super.tick(tickID);
	}
	
}
