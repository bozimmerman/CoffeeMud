package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class RingOfColdProtection extends Ring_Protection implements MiscMagic
{
	public RingOfColdProtection()
	{
		super();
		this.baseEnvStats().setLevel(SILVER_RING);
		this.recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new RingOfColdProtection();
	}
}
