package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.SmallSack;


public class BagOfHolding extends SmallSack implements MiscMagic
{
	public String ID(){	return "BagOfHolding";}
	public BagOfHolding()
	{
		super();

		setName("a small sack");
		setDisplayText("a small black sack is crumpled up here.");
		setDescription("A nice silk sack to put your things in.");
		secretIdentity="A Bag of Holding";
		baseEnvStats().setLevel(1);
		capacity=Integer.MAX_VALUE-1000;

		baseGoldValue=10000;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new BagOfHolding();
	}

	public void recoverEnvStats()
	{
		baseEnvStats.setWeight(0);
		super.recoverEnvStats();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(Sense.isLightSource(this))
		{
			if((!(affected instanceof Room))&&(rawWornCode()!=Item.INVENTORY))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_LIGHTSOURCE);
			if(Sense.isInDark(affected))
				affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_DARK);
		}
		if(!this.amWearingAt(Item.FLOATING_NEARBY))
			affectableStats.setWeight(affectableStats.weight()+0);
	}
	protected int recursiveRoomWeight(MOB mob, Item thisContainer)
	{
		return 0;
	}

}
