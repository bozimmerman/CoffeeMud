package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.interfaces.Environmental;
public class Gasbreath extends Dragonbreath
{
	public Gasbreath()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Gasbreath";
		setMiscText("gas");
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Gasbreath();
	}
}
