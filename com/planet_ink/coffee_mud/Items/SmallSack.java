package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class SmallSack extends StdContainer
{
	public String ID(){	return "SmallSack";}
	public SmallSack()
	{
		super();
		setName("a small sack");
		setDisplayText("a small sack is crumpled up here.");
		setDescription("A nice berlap sack to put your things in.");
		capacity=25;
		material=EnvResource.RESOURCE_COTTON;
		baseGoldValue=1;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new SmallSack();
	}

}
