package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class GlowingMageArmor extends StdArmor
{
	public String ID(){	return "GlowingMageArmor";}
	public GlowingMageArmor()
	{
		super();

		setName("a mystical glowing breast plate");
		setDisplayText("If this is sitting around somewhere, something is wrong!");
		setDescription("This suit of armor is made from magical energy, but looks sturdy and protective.");
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(45);
		baseEnvStats().setWeight(0);
		baseEnvStats().setAbility(0);
		baseGoldValue=40000;
		material=EnvResource.RESOURCE_NOTHING;
		recoverEnvStats();
	}


	public boolean savable(){return false;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((amWearingAt(Item.INVENTORY)||(owner()==null)||(owner() instanceof Room))
		&&(!amDestroyed()))
			destroy();

		MOB mob=msg.source();
		if(!msg.amITarget(this))
			return true;
		else
		if((msg.targetMinor()==CMMsg.TYP_GET)
		||(msg.targetMinor()==CMMsg.TYP_REMOVE))
		{
			mob.tell("The mage armor cannot be removed from where it is.");
			return false;
		}
		return true;
	}
}
