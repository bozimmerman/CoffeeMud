package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.interfaces.Environmental;
public class Frostbreath extends Dragonbreath
{
	public Frostbreath()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Frostbreath";
		setMiscText("cold");
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Frostbreath();
	}
}
