package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.interfaces.Environmental;
public class Acidbreath extends Dragonbreath
{
	public Acidbreath()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Acidbreath";
		setMiscText("acid");
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Acidbreath();
	}
}
