package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;


public class DruidicMonument extends StdItem implements MiscMagic
{
	public String ID(){	return "DruidicMonument";}
	public DruidicMonument()
	{
		super();

		name="the druidic stones";
		displayText="druidic stones are arrayed here.";
		description="These large mysterious monuments have a power and purpose only the druid understands.";
		secretIdentity="DRUIDIC STONES";
		baseEnvStats().setLevel(1);
		setMaterial(EnvResource.RESOURCE_STONE);
		setGettable(false);
		baseEnvStats().setWeight(1000);
		baseGoldValue=0;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new DruidicMonument();
	}
}
