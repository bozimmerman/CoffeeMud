package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class Shirt extends Armor
{
	public Shirt()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a nice looking shirt";
		displayText="a shirt is folded nice and neatly here.";
		description="It is a finely crafted shirt.";
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(6);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=Armor.CLOTH;
	}
	public Environmental newInstance()
	{
		return new Shirt();
	}
}
