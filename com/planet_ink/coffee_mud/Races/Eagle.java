package com.planet_ink.coffee_mud.Races;

public class Eagle extends GreatBird
{
	public Eagle()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Eagle";
	}
}
