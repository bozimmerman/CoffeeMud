package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class MichaelsMithrilChain extends StdArmor
{
	public String ID(){	return "MichaelsMithrilChain";}
	public MichaelsMithrilChain()
	{
		super();

		setName("a chain mail vest made of mithril");
		setDisplayText("a chain mail vest made from the dwarven alloy mithril");
		setDescription("This chain mail vest is made from a dwarven alloy called mithril, making it very light.");
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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.target()==null)||(!(msg.target() instanceof MOB)))
			return true;

		MOB mob=(MOB)msg.target();
		if((msg.targetMinor()==CMMsg.TYP_ELECTRIC)
		&&(!this.amWearingAt(Item.INVENTORY))
		&&(!this.amWearingAt(Item.HELD))
		&&(mob.isMine(this)))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> appear(s) to be unaffected.");
			return false;
		}
		return true;
	}


}
