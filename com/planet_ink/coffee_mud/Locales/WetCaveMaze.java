package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class WetCaveMaze extends Maze
{
	public WetCaveMaze()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats().setDisposition(Sense.IS_DARK);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;
		
		domainCondition=Room.CONDITION_WET;
	}
	public Environmental newInstance()
	{
		return new WetCaveMaze();
	}
}
