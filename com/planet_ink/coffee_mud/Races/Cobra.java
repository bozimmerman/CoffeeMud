package com.planet_ink.coffee_mud.Races;

public class Cobra extends Snake
{
	public Cobra()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Cobra";
		// inches
		shortestMale=6;
		shortestFemale=6;
		heightVariance=3;
		// pounds
		lightestWeight=15;
		weightVariance=20;
	}
}
