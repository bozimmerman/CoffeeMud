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

		name="a mystical glowing breast plate";
		displayText="If this is sitting around somewhere, something is wrong!";
		description="This suit of armor is made from magical energy, but looks sturdy and protective.";
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(45);
		baseEnvStats().setWeight(0);
		baseEnvStats().setAbility(0);
		baseGoldValue=40000;
		material=EnvResource.RESOURCE_NOTHING;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GlowingMageArmor();
	}
	
	public boolean savable(){return false;}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((amWearingAt(Item.INVENTORY)||(owner()==null)||(owner() instanceof Room))
		&&(!amDestroyed()))
			destroyThis();
		
		MOB mob=affect.source();
		if(!affect.amITarget(this))
			return true;
		else
		if(affect.targetMinor()==Affect.TYP_GET)
		{
			mob.tell("The mage armor cannot be removed from where it is.");
			return false;
		}
		return true;
	}
}
