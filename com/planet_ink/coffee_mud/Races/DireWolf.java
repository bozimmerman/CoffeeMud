package com.planet_ink.coffee_mud.Races;
public class DireWolf extends GiantWolf
{
	public DireWolf()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dire Wolf";
	}
}
