package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class RingOfElectricity extends Ring_Protection implements MiscMagic
{
	public String ID(){	return "RingOfElectricity";}
	public RingOfElectricity()
	{
		super();
		this.baseEnvStats().setLevel(COPPER_RING);
		material=EnvResource.RESOURCE_COPPER;
		this.recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new RingOfElectricity();
	}
}
