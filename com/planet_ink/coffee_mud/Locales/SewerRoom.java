package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class SewerRoom extends StdRoom
{
	public SewerRoom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="the sewers";
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_DARK);
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;
		domainCondition=Room.CONDITION_WET;
	}
	public Environmental newInstance()
	{
		return new SewerRoom();
	}
	public int getMaxRange()
	{
		if(maxRange>=0) return maxRange;
		return 5;
	}
}
