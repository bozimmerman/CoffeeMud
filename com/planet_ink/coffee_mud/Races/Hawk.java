package com.planet_ink.coffee_mud.Races;

public class Hawk extends GreatBird
{
	public Hawk()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hawk";
	}
}
