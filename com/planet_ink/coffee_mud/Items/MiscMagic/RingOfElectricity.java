package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class RingOfElectricity extends Ring_Protection implements MiscMagic
{
	public RingOfElectricity()
	{
		super();
		this.baseEnvStats().setLevel(COPPER_RING);
		this.recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new RingOfElectricity();
	}
}
