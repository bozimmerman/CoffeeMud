package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class TreeSurface extends ClimbableSurface
{
	public TreeSurface()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="the tree";
		baseEnvStats.setWeight(4);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_WOODS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new TreeSurface();
	}
}
