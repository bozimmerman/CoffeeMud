package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class EternityLeafShield extends StdShield
{
	public EternityLeafShield()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a huge leaf";
		displayText="a huge and very rigid leaf lays on the ground.";
		description="a very huge and very rigid leaf";
		secretIdentity="A shield made from one of the leaves of the Fox god\\`s Eternity Trees.  (Armor:  30)";
		properWornBitmap=Item.HELD;
		wornLogicalAnd=true;
		baseGoldValue+=15000;
		baseEnvStats().setArmor(30);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(15);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
		material=Armor.LEATHER;
	}

	public Environmental newInstance()
	{
		return new EternityLeafShield();
	}
}
