package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class ChainMailArmor extends Armor
{
	public ChainMailArmor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a suit of chain mail armor";
		displayText="a suit of chain mail armor sits here.";
		description="This suit includes a fairly solid looking hauberk with leggings and a coif.";
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(37);
		baseEnvStats().setWeight(60);
		baseEnvStats().setAbility(0);
		baseGoldValue=150;
		recoverEnvStats();
		material=Armor.METAL;
	}
	public Environmental newInstance()
	{
		return new ChainMailArmor();
	}
}
