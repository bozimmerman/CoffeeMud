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

		setName("the body of someone");
		setDisplayText("the body of someone lies here.");
		setDescription("Bloody and bruised, obviously mistreated.");
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
		if(newText.length()>0)
			super.setMiscText(newText);
	}
	public Environmental newInstance()
	{
		return new Corpse();
	}
	public void startTicker(Room thisRoom)
	{
		roomLocation=thisRoom;
		ExternalPlay.startTickDown(this,MudHost.TICK_DEADBODY_DECAY,envStats().rejuv());
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_DEADBODY_DECAY)
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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((msg.amITarget(this)||(msg.tool()==this))
		&&(msg.targetMinor()==CMMsg.TYP_GET)
		&&((envStats().ability()>10)||(Sense.isABonusItems(this)))
		&&(rawSecretIdentity().indexOf("/")>=0)
		&&(rawSecretIdentity().toUpperCase().startsWith(msg.source().Name().toUpperCase()+"/"))
		&&(CMMap.getBodyRoom(msg.source())!=msg.source().location()))
		{
			msg.source().tell("You are prevented from touching "+name()+".");
			return false;
		}
		return true;
	}
}