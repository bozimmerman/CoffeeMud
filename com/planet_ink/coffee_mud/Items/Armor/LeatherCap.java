package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class LeatherCap extends Armor
{
	public LeatherCap()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a leather cap";
		displayText="a round leather cap sits here.";
		description="It looks like its made of cured leather hide, with metal bindings.";
		properWornBitmap=Item.ON_HEAD;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(4);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=Armor.LEATHER;
	}
	public Environmental newInstance()
	{
		return new LeatherCap();
	}
}
