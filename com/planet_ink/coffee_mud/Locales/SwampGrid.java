package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class SwampGrid extends StdGrid
{
	public SwampGrid()
	{
		super();
		name="the swamp";
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_HILLS;
		domainCondition=Room.CONDITION_NORMAL;
		baseMove=3;
	}
	public Environmental newInstance()
	{
		return new SwampGrid();
	}
	public String getChildLocaleID(){return "Swamp";}
	public Vector resourceChoices(){return Swamp.roomResources;}
}
