package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class ThreadbareRobes extends StdArmor
{
	public String ID(){	return "ThreadbareRobes";}
	public ThreadbareRobes()
	{
		super();
		setName("a set of worn robes");
		setDisplayText("a set of worn robes");
		setDescription("These robes are patched, yet still gape with ragged holes. Evidently having seen years of use, they are vitualy worthless");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(5);
		baseEnvStats().setWeight(2);
		baseEnvStats().setAbility(0);
		baseGoldValue=1;
		material=EnvResource.RESOURCE_COTTON;
		recoverEnvStats();
	}

}
