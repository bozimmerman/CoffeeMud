package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class MountainSurface extends ClimbableSurface
{
	public MountainSurface()
	{
		super();
		baseEnvStats.setWeight(6);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_MOUNTAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new MountainSurface();
	}
	public Vector resourceChoices(){return Mountains.roomResources;}
}
