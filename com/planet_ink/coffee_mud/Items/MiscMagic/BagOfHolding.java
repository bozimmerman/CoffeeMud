package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.SmallSack;


public class BagOfHolding extends SmallSack implements MiscMagic
{
	public BagOfHolding()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a small sack";
		displayText="a small black sack is crumpled up here.";
		description="A nice silk sack to put your things in.";
		secretIdentity="A Bag of Holding";
		baseEnvStats().setLevel(1);
		capacity=100 * baseEnvStats().level();

		baseGoldValue=10000;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();



	}

	public Environmental newInstance()
	{
		return new BagOfHolding();
	}

}
