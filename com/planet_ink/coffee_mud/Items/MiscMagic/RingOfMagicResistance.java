package com.planet_ink.coffee_mud.Items.MiscMagic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class RingOfMagicResistance extends Ring_Protection implements MiscMagic
{
	public String ID(){	return "RingOfMagicResistance";}
	public RingOfMagicResistance()
	{
		super();
		this.baseEnvStats().setLevel(GOLD_RING_OPAL);
		this.recoverEnvStats();
		material=EnvResource.RESOURCE_GOLD;
	}

}
