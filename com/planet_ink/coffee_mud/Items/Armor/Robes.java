package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class Robes extends Armor
{
	public Robes()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a set of robes";
		displayText="a set of robes is folded nice and neatly here.";
		description="It is a finely crafted set of robes.";
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(8);
		baseEnvStats().setWeight(10);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=Armor.CLOTH;
	}
	public Environmental newInstance()
	{
		return new Robes();
	}
	
}
