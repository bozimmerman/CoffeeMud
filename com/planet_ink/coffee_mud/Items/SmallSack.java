package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class SmallSack extends StdContainer
{
	public SmallSack()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a small sack";
		displayText="a small sack is crumpled up here.";
		description="A nice berlap sack to put your things in.";
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
