package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class StdKey extends StdItem
{

	public StdKey()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a metal key";
		displayText="a small metal key sits here.";
		description="You can't tell what it\\`s to by looking at it.";
		
		baseGoldValue=0;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StdKey();
	}
}
