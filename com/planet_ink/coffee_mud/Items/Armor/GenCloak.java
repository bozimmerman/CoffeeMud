package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class GenCloak extends GenArmor
{
	public String ID(){	return "GenCloak";}
	public GenCloak()
	{
		super();

		setName("a hooded cloak");
		setDisplayText("a hooded cloak is here");
		setDescription("");
		properWornBitmap=Item.ABOUT_BODY;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(1);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=1;
		recoverEnvStats();
		material=EnvResource.RESOURCE_COTTON;
		readableText="a hooded figure";
	}

	public void affectEnvStats(Environmental host, EnvStats stats)
	{
		if(!amWearingAt(Item.INVENTORY))
			stats.setName(readableText());
	}
}
