package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
public class Kitten extends Cat
{
	public Kitten()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
		// inches
		shortestMale=4;
		shortestFemale=4;
		heightVariance=3;
		// pounds
		lightestWeight=7;
		weightVariance=10;
		forbiddenWornBits=Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_FEET;
	}
}
