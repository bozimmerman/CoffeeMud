package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class FrozenPlains extends Plains
{
	public FrozenPlains()
	{
		super();
		recoverEnvStats();
		domainCondition=Room.CONDITION_COLD;
	}
	public Environmental newInstance()
	{
		return new FrozenPlains();
	}
	public Vector resourceChoices(){return Plains.roomResources;}
}
