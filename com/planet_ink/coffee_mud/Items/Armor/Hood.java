package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class Hood extends StdArmor
{
	public Hood()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a cloth hood";
		displayText="a cloth hood sits here.";
		description="This is a cloth hood that covers the head and shoulders.";
		properWornBitmap=Item.ON_HEAD;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(2);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=EnvResource.RESOURCE_COTTON;
	}
	public Environmental newInstance()
	{
		return new Hood();
	}
}
