package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import java.util.*;

public class SewerMaze extends StdMaze
{
	public String ID(){return "SewerMaze";}
	public SewerMaze()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_DARK);
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;
		domainCondition=Room.CONDITION_WET;
	}

	public String getChildLocaleID(){return "SewerRoom";}
	public Vector resourceChoices(){return new Vector();}
}
