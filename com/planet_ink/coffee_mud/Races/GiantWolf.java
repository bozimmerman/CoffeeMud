package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
public class GiantWolf extends Wolf
{
	public GiantWolf()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Giant Wolf";
		// inches
		shortestMale=26;
		shortestFemale=26;
		heightVariance=12;
		// pounds
		lightestWeight=80;
		weightVariance=60;
		forbiddenWornBits=Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_FEET-Item.ON_NECK;
	}
}
