package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class HiddenClosedDoorway extends StdExit
{
	public HiddenClosedDoorway()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Door";
		description="a cleverly concealed door.";
		displayText="an open door";
		closedText="a closed door";
		miscText="KEY";
		hasADoor=true;
		isOpen=false;
		hasALock=false;
		isLocked=false;
		doorDefaultsClosed=true;
		doorDefaultsLocked=false;
		openDelayTicks=45;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|Sense.IS_HIDDEN);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new HiddenClosedDoorway();
	}

}
