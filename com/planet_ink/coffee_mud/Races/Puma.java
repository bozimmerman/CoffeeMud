package com.planet_ink.coffee_mud.Races;
public class Puma extends GreatCat
{
	public Puma()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
	}
}
