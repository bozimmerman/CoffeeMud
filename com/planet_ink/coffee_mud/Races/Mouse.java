package com.planet_ink.coffee_mud.Races;

public class Mouse extends Rodent
{
	public Mouse()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mouse";
	}
}
