package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class FrozenMountains extends Mountains
{
	public FrozenMountains()
	{
		super();
		recoverEnvStats();
		domainCondition=Room.CONDITION_COLD;
	}
	public Environmental newInstance()
	{
		return new FrozenMountains();
	}
	public Vector resourceChoices(){return Mountains.roomResources;}
}
