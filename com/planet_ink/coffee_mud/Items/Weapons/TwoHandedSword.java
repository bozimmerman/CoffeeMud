package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class TwoHandedSword extends Sword
{
	public TwoHandedSword()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a two-handed sword";
		displayText="a heavy two-handed sword hangs on the wall.";
		miscText="";
		description="It has a metallic pommel, and a very large blade.";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(15);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(10);
		baseGoldValue=50;
		wornLogicalAnd=true;
		properWornBitmap=Item.HELD|Item.WIELD;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_SLASHING;
	}

	public Environmental newInstance()
	{
		return new TwoHandedSword();
	}
}
