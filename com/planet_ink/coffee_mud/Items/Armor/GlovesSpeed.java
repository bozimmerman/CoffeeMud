package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class GlovesSpeed extends StdArmor
{
	public String ID(){	return "GlovesSpeed";}
	public GlovesSpeed()
	{
		super();

		setName("a pair of gloves");
		setDisplayText("a pair of finely crafted gloves is found on the ground.");
		setDescription("This is a pair of very nice gloves.");
		secretIdentity="Gloves of the blinding strike (Double attack speed, truely usable only by fighters.)";
		baseGoldValue+=10000;
		properWornBitmap=Item.ON_HANDS;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(15);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(1);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();

	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!this.amWearingAt(Item.INVENTORY))&&(!this.amWearingAt(Item.HELD)))
		{
			affectableStats.setSpeed(affectableStats.speed() * 2.0);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + 10);
		}
	}

	public Environmental newInstance()
	{
		return new GlovesSpeed();
	}
}
