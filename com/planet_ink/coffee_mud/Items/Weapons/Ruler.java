package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Ruler extends Quarterstaff
{
	public String ID(){	return "Ruler";}
	public Ruler()
	{
		super();

		setName("a ruler");
		setDisplayText("a ruler has been left here.");
		setDescription("It`s long and wooden, with little tick marks on it.");
		material=EnvResource.RESOURCE_OAK;
	}

	public Environmental newInstance()
	{
		return new Ruler();
	}
}
