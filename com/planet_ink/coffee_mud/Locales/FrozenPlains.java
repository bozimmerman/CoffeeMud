package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class FrozenPlains extends Plains
{
	public String ID(){return "FrozenPlains";}
	public FrozenPlains()
	{
		super();
		recoverEnvStats();
		domainCondition=Room.CONDITION_COLD;
	}

	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_FUR)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Plains.roomResources;}
}
