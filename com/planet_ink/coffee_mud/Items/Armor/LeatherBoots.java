package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;

public class LeatherBoots extends Armor
{
	public LeatherBoots()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a pair of leather boots";
		displayText="a pair of leather boots sits here.";
		description="They look like a rather nice pair of footwear.";
		properWornBitmap=Item.ON_FEET;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(1);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=5;
		recoverEnvStats();
		material=Armor.LEATHER;
	}
	public Environmental newInstance()
	{
		return new LeatherBoots();
	}
	
}
