package com.planet_ink.coffee_mud.Races;

public class GardenSnake extends Snake
{
	public GardenSnake()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Garden Snake";
	}
}
