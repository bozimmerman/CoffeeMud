package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenCorpse extends GenContainer implements DeadBody
{
	Room roomLocation=null;

	public GenCorpse()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="an anonymous corpse";
		displayText="a corpse lies here.";
		description="Looks dead.";
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
	public boolean tick(int tickID)
	{
		if(tickID==Host.DEADBODY_DECAY)
		{
			destroyThis();
			roomLocation.recoverRoomStats();
			return false;
		}
		else
			return super.tick(tickID);
	}
}
