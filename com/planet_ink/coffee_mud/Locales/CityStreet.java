package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class CityStreet extends StdRoom
{
	public CityStreet()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="the street";
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_CITY;
		domainCondition=Room.CONDITION_NORMAL;
		baseMove=1;
	}
	public Environmental newInstance()
	{
		return new CityStreet();
	}
}
