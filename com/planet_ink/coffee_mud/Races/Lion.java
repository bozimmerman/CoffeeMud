package com.planet_ink.coffee_mud.Races;
public class Lion extends GreatCat
{
	public Lion()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
	}
}
