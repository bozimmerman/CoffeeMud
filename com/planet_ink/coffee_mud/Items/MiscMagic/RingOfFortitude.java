package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class RingOfFortitude extends Ring_Protection implements MiscMagic
{
	public RingOfFortitude()
	{
		super();
		this.baseEnvStats().setLevel(MITHRIL_RING);
		material=EnvResource.RESOURCE_MITHRIL;
		this.recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new RingOfFortitude();
	}
}
