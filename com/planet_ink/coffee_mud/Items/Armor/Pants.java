package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class Pants extends Armor
{
	public Pants()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a pair of pants";
		displayText="a pair of pants lies here";
		description="a well tailored pair of pants.";
		properWornBitmap=Item.ON_LEGS;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(1);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=Armor.CLOTH;
	}
	public Environmental newInstance()
	{
		return new Pants();
	}
}
