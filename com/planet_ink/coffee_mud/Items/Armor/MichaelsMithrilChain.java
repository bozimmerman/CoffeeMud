package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class MichaelsMithrilChain extends StdArmor
{
	public MichaelsMithrilChain()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a chain mail vest made of mithril";
		displayText="a chain mail vest made from the dwarven alloy mithril";
		description="This chain mail vest is made from a dwarven alloy called mithril, making it very light.";
		properWornBitmap=Item.ON_TORSO;
		secretIdentity="Michael\\`s Mithril Chain! (Armor Value:+75, Protection from Lightning)";
		baseGoldValue+=10000;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(50);
		baseEnvStats().setWeight(40);
		baseEnvStats().setAbility(75);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
		material=EnvResource.RESOURCE_MITHRIL;
	}

	public boolean okAffect(Affect affect)
	{
		if((affect.target()==null)||(!(affect.target() instanceof MOB)))
			return true;

		MOB mob=(MOB)affect.target();
		if((affect.targetMinor()==affect.TYP_ELECTRIC)
		&&(!this.amWearingAt(Item.INVENTORY))
		&&(!this.amWearingAt(Item.HELD))
		&&(mob.isMine(this)))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> appear(s) to be unaffected.");
			return false;
		}
		return true;
	}

	public Environmental newInstance()
	{
		return new MichaelsMithrilChain();
	}
}
