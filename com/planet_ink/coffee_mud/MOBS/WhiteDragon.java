package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.interfaces.Environmental;

public class WhiteDragon extends Dragon
{
	
	public WhiteDragon()
	{
		// ===== call the super class constructor 
		super(WHITE);
	}
	
	public Environmental newInstance()
	{
		// ===== Hatch one!
		return new WhiteDragon();
	}
}
