package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class LargeCaveRoom extends CaveRoom
{
	public String ID(){return "LargeCaveRoom";}
	public LargeCaveRoom()
	{
		super();
		baseEnvStats.setWeight(4);
		recoverEnvStats();
		maxRange=5;
	}
	public Environmental newInstance()
	{
		return new LargeCaveRoom();
	} 
}
