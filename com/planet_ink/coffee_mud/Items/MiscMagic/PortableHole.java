package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class PortableHole extends BagOfHolding implements MiscMagic
{
	public String ID(){	return "PortableHole";}
	public PortableHole()
	{
		super();

		name="a small disk";
		displayText="a small black disk can be found up here.";
		description="It looks like a small disk.";
		secretIdentity="A Portable Hole";
		baseEnvStats().setLevel(1);
		capacity=200 * baseEnvStats().level();

		baseGoldValue=15000;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();



	}

	public Environmental newInstance()
	{
		return new PortableHole();
	}

}
