package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.interfaces.Environmental;
public class Firebreath extends Dragonbreath
{
	public Firebreath()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Firebreath";
		setMiscText("fire");
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Firebreath();
	}
}
