package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdKey extends StdItem implements Key
{

	public StdKey()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a metal key";
		displayText="a small metal key sits here.";
		description="You can't tell what it\\`s to by looking at it.";

		material=EnvResource.RESOURCE_STEEL;
		baseGoldValue=0;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StdKey();
	}

	public void setKey(String keyName){miscText=keyName;}
	public String getKey(){return miscText;}
}
