package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class ChainMailVest extends Armor
{
	public ChainMailVest()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a chain mail vest";
		displayText="a chain mail vest sits here.";
		description="This is fairly solid looking vest made of chain mail.";
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(25);
		baseEnvStats().setWeight(30);
		baseEnvStats().setAbility(0);
		baseGoldValue=75;
		recoverEnvStats();
		material=Armor.METAL;
	}
	public Environmental newInstance()
	{
		return new ChainMailVest();
	}
}
