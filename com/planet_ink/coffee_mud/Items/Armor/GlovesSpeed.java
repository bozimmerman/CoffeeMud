package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class GlovesSpeed extends Armor
{
	public GlovesSpeed()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a pair of gloves";
		displayText="a pair of finely crafted gloves is found on the ground.";
		description="This is a pair of very nice gloves.";
		secretIdentity="Gloves of the blinding strike (Double attack speed, truely usable only by fighters.)";
		baseGoldValue+=10000;
		properWornBitmap=Item.ON_HANDS;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(15);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(1);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|Sense.IS_BONUS);
		recoverEnvStats();

	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!this.amWearingAt(Item.INVENTORY))&&(!this.amWearingAt(Item.HELD)))
			if(affected instanceof Fighter || affected instanceof Archon)
			{
				affectableStats.setSpeed(affectableStats.speed() * 2.0);
			}
			else
			{
				affectableStats.setSpeed(affectableStats.speed() * 2.0);
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment() + 100);
			}
	}

	public Environmental newInstance()
	{
		return new GlovesSpeed();
	}
}
