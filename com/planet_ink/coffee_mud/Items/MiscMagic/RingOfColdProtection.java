package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class RingOfColdProtection extends Ring_Protection implements MiscMagic
{
	public RingOfColdProtection()
	{
		super();
		this.baseEnvStats().setLevel(SILVER_RING);
		material=EnvResource.RESOURCE_SILVER;
		this.recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new RingOfColdProtection();
	}
}
