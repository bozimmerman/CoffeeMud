package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class LargeStoneRoom extends StoneRoom
{
	public String ID(){return "LargeStoneRoom";}
	public LargeStoneRoom()
	{
		super();
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		maxRange=5;
	}
	public Environmental newInstance()
	{
		return new LargeStoneRoom();
	} 
}
