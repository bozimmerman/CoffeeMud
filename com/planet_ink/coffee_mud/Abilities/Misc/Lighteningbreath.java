package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.interfaces.Environmental;
public class Lighteningbreath extends Dragonbreath
{
	public Lighteningbreath()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Lightningbreath";
		setMiscText("lightning");
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Lighteningbreath();
	}
}
