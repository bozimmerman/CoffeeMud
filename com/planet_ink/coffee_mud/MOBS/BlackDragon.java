package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.interfaces.Environmental;

public class BlackDragon extends Dragon
{
	public String ID(){return "BlackDragon";}
	public BlackDragon()
	{
		// ===== call the super class constructor that creates a White
		super(BLACK);
	}
	
	public Environmental newInstance()
	{
		// ===== Hatch one!
		return new BlackDragon();
	}
}
