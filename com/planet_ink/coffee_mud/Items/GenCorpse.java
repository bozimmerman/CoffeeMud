package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenCorpse extends GenContainer implements DeadBody
{
	public String ID(){	return "GenCorpse";}
	protected Room roomLocation=null;
	protected CharStats charStats=null;

	public GenCorpse()
	{
		super();
		setName("an anonymous corpse");
		setDisplayText("a corpse lies here.");
		setDescription("Looks dead.");
		isReadable=false;
		setMaterial(EnvResource.RESOURCE_MEAT);
	}

	public Environmental newInstance()
	{
		return new GenCorpse();
	}
	public boolean isGeneric(){return true;}

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
	public CharStats charStats(){return charStats;}
	public void setCharStats(CharStats newStats){charStats=newStats;}
}
