package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class RingMail extends Armor
{
	public RingMail()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="suit of ring mail";
		displayText="a suit of armor made with large metal rings fastened to leather";
		description="A suit of ring mail including everything to protect the body, legs and arms.";
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(22);
		baseEnvStats().setWeight(50);
		baseEnvStats().setAbility(0);
		baseGoldValue=200;
		recoverEnvStats();
		material=Armor.METAL;
	}
	public Environmental newInstance()
	{
		return new RingMail();
	}
}
