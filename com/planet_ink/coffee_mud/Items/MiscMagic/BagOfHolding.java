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
		capacity=1000;
		baseGoldValue=25000;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new BagOfHolding();
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((msg.targetMinor()==CMMsg.TYP_PUT)
		&&(msg.target() instanceof BagOfHolding)
		&&(msg.tool() instanceof BagOfHolding))
		{
			((Item)msg.target()).destroy();
			((Item)msg.tool()).destroy();
			msg.source().tell("The bag implodes in your hands!");
		}
	}

	public void recoverEnvStats()
	{
		baseEnvStats().setWeight(0);
		super.recoverEnvStats();
		baseEnvStats().setWeight(-recursiveWeight(this));
		super.recoverEnvStats();
	}
}
