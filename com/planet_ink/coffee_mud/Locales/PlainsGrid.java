package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class PlainsGrid extends StdGrid
{
	public PlainsGrid()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_PLAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new PlainsGrid();
	}
	public String getChildLocaleID(){return "Plains";}
}
