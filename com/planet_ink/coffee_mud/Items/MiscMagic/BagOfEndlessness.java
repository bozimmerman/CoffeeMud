package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.SmallSack;

public class BagOfEndlessness extends BagOfHolding
{
	public String ID(){	return "BagOfEndlessness";}
	public BagOfEndlessness()
	{
		super();

		name="a small sack";
		displayText="a small black sack is crumpled up here.";
		description="A nice silk sack to put your things in.";
		secretIdentity="The Bag of Endless Stuff";
		baseEnvStats().setLevel(1);
		capacity=Integer.MAX_VALUE-1000;

		baseGoldValue=10000;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new BagOfEndlessness();
	}

	public void affect(Affect affect)
	{
		if(affect.amITarget(this)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Item))
		{
			Item newitem=(Item)affect.tool();
			if((newitem.container()==this)
			&&(newitem.owner() !=null))
			{
				Item neweritem=(Item)newitem.copyOf();
				neweritem.setContainer(this);
				if(newitem.owner() instanceof MOB)
					((MOB)newitem.owner()).addInventory(neweritem);
				else
				if(newitem.owner() instanceof Room)
				{
					((Room)newitem.owner()).addItem(neweritem);
					neweritem.setDispossessionTime(dispossessionTime());
				}
				neweritem.recoverEnvStats();
			}
		}
		super.affect(affect);
	}
}
