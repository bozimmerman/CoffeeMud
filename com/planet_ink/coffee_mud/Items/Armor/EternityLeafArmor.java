package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class EternityLeafArmor extends StdArmor
{
	public EternityLeafArmor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a suit of Eternity Tree Leaf Armor";
		displayText="a suit of Eternity tree leaf armor sits here.";
		description="This suit of armor is made from the leaves of the Eternity Tree, a true gift from the Fox god himself.  (armor:  50, grants a modest degree of stealth, and is as light as cloth.)";
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseGoldValue+=25000;
		baseEnvStats().setArmor(50);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(15);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
		material=EnvResource.RESOURCE_SEAWEED;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((!this.amWearingAt(Item.INVENTORY))&&(!this.amWearingAt(Item.HELD)))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SNEAKING);
	}

	public Environmental newInstance()
	{
		return new EternityLeafArmor();
	}
}
