package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class WoodRoom extends StdRoom
{
	public WoodRoom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_WOOD;
		domainCondition=Room.CONDITION_NORMAL;
		baseMove=1;
	}
	public Environmental newInstance()
	{
		return new WoodRoom();
	} 
}
