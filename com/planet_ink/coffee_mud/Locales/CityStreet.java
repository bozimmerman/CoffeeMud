package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class CityStreet extends StdRoom
{
	public String ID(){return "CityStreet";}
	public CityStreet()
	{
		super();
		name="the street";
		baseEnvStats.setWeight(1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_CITY;
		domainCondition=Room.CONDITION_NORMAL;
	}

}
