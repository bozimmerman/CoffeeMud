package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class UnderSaltWaterGrid extends UnderWaterGrid
{
	public String ID(){return "UnderSaltWaterGrid";}
	public UnderSaltWaterGrid()
	{
		super();
		baseEnvStats().setSensesMask(baseEnvStats().sensesMask()|EnvStats.CAN_NOT_BREATHE);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_SWIMMING);
		baseEnvStats.setWeight(3);
		setDisplayText("Under the water");
		setDescription("");
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_UNDERWATER;
		domainCondition=Room.CONDITION_WET;
		baseThirst=0;
	}

	public Environmental newInstance()
	{
		return new UnderSaltWaterGrid();
	}
	public String getChildLocaleID(){return "UnderSaltWater";}

	public Vector resourceChoices(){return UnderSaltWater.roomResources;}
}
