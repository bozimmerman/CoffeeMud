package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.interfaces.Environmental;

public class CopperDragon extends Dragon
{
	
	public CopperDragon()
	{
		// ===== call the super class constructor 
		super(COPPER);
	}
	
	public Environmental newInstance()
	{
		// ===== Hatch one!
		return new CopperDragon();
	}
}
