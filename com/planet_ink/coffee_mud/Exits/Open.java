package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Open extends StdOpenDoorway
{
	public String ID(){	return "Open";}
	public Environmental newInstance()
	{
		return new Open();
	}

}
