package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.interfaces.Environmental;

public class RedDragon extends Dragon
{
	public String ID(){return "RedDragon";}
	public RedDragon()
	{
		// ===== call the super class constructor 
		super(RED);
	}
	
	public Environmental newInstance()
	{
		// ===== Hatch one!
		return new RedDragon();
	}
}
