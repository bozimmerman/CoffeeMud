package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import java.util.*;

public class UnderSaltWaterMaze extends StdMaze
{
	public String ID(){return "UnderSaltWaterMaze";}
	public UnderSaltWaterMaze()
	{
		super();
		baseEnvStats().setSensesMask(baseEnvStats().sensesMask()|EnvStats.CAN_NOT_BREATHE);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_SWIMMING);
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_UNDERWATER;
		domainCondition=Room.CONDITION_WET;
		baseThirst=0;
	}

	public Environmental newInstance()
	{
		return new UnderSaltWaterMaze();
	}
	public String getChildLocaleID(){return "UnderSaltWater";}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
	}
	public Vector resourceChoices(){return UnderSaltWater.roomResources;}
}
