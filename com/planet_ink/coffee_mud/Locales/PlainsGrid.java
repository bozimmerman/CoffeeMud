package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class PlainsGrid extends StdGrid
{
	public String ID(){return "PlainsGrid";}
	public PlainsGrid()
	{
		super();
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_PLAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new PlainsGrid();
	}
	public String getChildLocaleID(){return "Plains";}
	public Vector resourceChoices(){return Plains.roomResources;}
}
