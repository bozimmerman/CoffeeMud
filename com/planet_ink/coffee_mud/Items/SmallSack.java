package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;


public class SmallSack extends Container
{
	public SmallSack()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a small sack";
		displayText="a small sack is crumpled up here.";
		description="A nice berlap sack to put your things in.";
		capacity=25;
		baseGoldValue=1;
		recoverEnvStats();
	}
	
	public Environmental newInstance()
	{
		return new SmallSack();
	}
	
}
