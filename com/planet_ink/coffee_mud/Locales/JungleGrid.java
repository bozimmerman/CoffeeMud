package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class JungleGrid extends StdGrid
{
	public JungleGrid()
	{
		super();
		name="the jungle";
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_JUNGLE;
		domainCondition=Room.CONDITION_HOT;
		baseMove=3;
	}
	public Environmental newInstance()
	{
		return new JungleGrid();
	}
	public String getChildLocaleID(){return "Jungle";}
}
