package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class Hat_Intelligence extends Armor
{
	public Hat_Intelligence()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a feathered cap";
		displayText="a feathered cap.";
		description="It looks like a regular cap with long feather.";
		secretIdentity="Hat of Intelligence (Increases IQ)";
		properWornBitmap=Item.ON_HEAD;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(2);
		baseEnvStats().setWeight(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=6000;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|Sense.IS_BONUS);
		recoverEnvStats();
		material=Armor.CLOTH;
	}
	public Environmental newInstance()
	{
		return new Hat_Intelligence();
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setIntelligence(affectableStats.getIntelligence() + 4);
	}

}
