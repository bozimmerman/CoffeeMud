package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Corpse extends GenContainer implements DeadBody
{
	public String ID(){	return "Corpse";}
	protected Room roomLocation=null;
	protected CharStats charStats=null;

	public Corpse()
	{
		super();

		name="the body of someone";
		displayText="the body of someone lies here.";
		description="Bloody and bruised, obviously mistreated.";
		properWornBitmap=0;
		baseEnvStats.setWeight(150);
		baseEnvStats.setRejuv(100);
		capacity=5;
		baseGoldValue=0;
		recoverEnvStats();
		material=EnvResource.RESOURCE_MEAT;
	}
	public void setMiscText(String newText)
	{
		miscText="";
		if(newText.length()>30) 
			super.setMiscText(newText);
	}
	public Environmental newInstance()
	{
		return new Corpse();
	}
	public void startTicker(Room thisRoom)
	{
		roomLocation=thisRoom;
		ExternalPlay.startTickDown(this,Host.DEADBODY_DECAY,envStats().rejuv());
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.DEADBODY_DECAY)
		{
			destroy();
			roomLocation.recoverRoomStats();
			return false;
		}
		else
			return super.tick(ticking,tickID);
	}
	public CharStats charStats()
	{
		if(charStats==null)
			charStats=new DefaultCharStats();
		return charStats;
	}
	public void setCharStats(CharStats newStats){charStats=newStats;}

}