package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.interfaces.Environmental;

public class BronzeDragon extends Dragon
{
	public String ID(){return "BronzeDragon";}
	public BronzeDragon()
	{
		// ===== call the super class constructor 
		super(BRONZE);
	}
	
	public Environmental newInstance()
	{
		// ===== Hatch one!
		return new BronzeDragon();
	}
}
