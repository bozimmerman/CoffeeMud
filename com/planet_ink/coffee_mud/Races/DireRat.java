package com.planet_ink.coffee_mud.Races;
public class DireRat extends GiantRat
{
	public DireRat()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Dire Rat";
	}
}