package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class LargeSack extends StdContainer
{
	public LargeSack()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a large sack";
		displayText="a large sack is crumpled up here.";
		description="A nice big berlap sack to put your things in.";
		capacity=100;
		baseGoldValue=5;
		setMaterial(EnvResource.RESOURCE_COTTON);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new LargeSack();
	}

}
