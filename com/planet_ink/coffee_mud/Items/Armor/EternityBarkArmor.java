package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class EternityBarkArmor extends StdArmor
{
	public String ID(){	return "EternityBarkArmor";}
	public EternityBarkArmor()
	{
		super();

		setName("a suit of Eternity Tree Bark Armor");
		setDisplayText("a suit of Eternity tree bark armor sits here.");
		setDescription("This suit of armor is made from the bark of the Fox god\\`s Eternity Tree(armor:  100 and as light as leather armor--wearable by theives)");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseGoldValue+=25000;
		baseEnvStats().setArmor(100);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(15);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
		material=EnvResource.RESOURCE_WOOD;
	}


}
