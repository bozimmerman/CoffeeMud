package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class RingOfJustice extends Ring_Protection implements MiscMagic
{
	public RingOfJustice()
	{
		super();
		this.baseEnvStats().setLevel(GOLD_RING_SAPPHIRE);
		material=EnvResource.RESOURCE_GOLD;
		this.recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new RingOfJustice();
	}
}
