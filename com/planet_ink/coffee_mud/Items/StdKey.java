package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdKey extends StdItem implements Key
{
	public String ID(){	return "StdKey";}
	public StdKey()
	{
		super();
		setName("a metal key");
		setDisplayText("a small metal key sits here.");
		setDescription("You can't tell what it\\`s to by looking at it.");

		material=EnvResource.RESOURCE_STEEL;
		baseGoldValue=0;
		recoverEnvStats();
	}


	public void setKey(String keyName){miscText=keyName;}
	public String getKey(){return miscText;}
}
