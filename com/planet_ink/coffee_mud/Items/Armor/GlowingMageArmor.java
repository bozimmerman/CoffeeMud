package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;

public class GlowingMageArmor extends Armor
{
	public GlowingMageArmor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a glowing breast plate of mystical energy";
		displayText="is this is sitting around somewhere, something is wrong!";
		description="This suit of armor is made from magical energy, but looks sturdy and protective.";
		properWornBitmap=Item.ON_TORSO;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(45);
		baseEnvStats().setWeight(0);
		baseEnvStats().setAbility(0);
		baseGoldValue=40000;
		material=Armor.CLOTH;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GlowingMageArmor();
	}
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		
		MOB mob=affect.source();
		if(!affect.amITarget(this))
			return true;
		else
		switch(affect.targetType())
		{
		case Affect.HANDS:
			switch(affect.targetCode())
			{
			case Affect.HANDS_GET:
				mob.tell("The mage armor cannot be removed from where it is.");
				return false;
			}
		}
		return true;
	}
}
