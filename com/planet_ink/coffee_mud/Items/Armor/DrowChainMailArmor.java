package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class DrowChainMailArmor extends StdArmor
{
	public String ID(){	return "DrowChainMailArmor";}
	public DrowChainMailArmor()
	{
		super();

		setName("a suit of dark chain mail armor");
		setDisplayText("a suit of chain mail armor made of dark material sits here.");
		setDescription("This suit includes a fairly solid looking hauberk with leggings and a coif, all constructed from a strong, dark metal.");
        secretIdentity="A suit of Drow Chain Mail Armor";
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(65);
		baseEnvStats().setWeight(60);
		baseEnvStats().setAbility(0);
		baseGoldValue=1500;
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
	}

}
