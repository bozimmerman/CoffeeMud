package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class Backpack extends CloseableContainer
{
	public String ID(){	return "Backpack";}
	public Backpack()
	{
		super();
		setName("a backpack");
		setDisplayText("a backpack sits here.");
		setDescription("The straps are a little worn, but it\\`s in nice shape!");
		capacity=25;
		baseGoldValue=5;
		properWornBitmap=Item.ON_BACK|Item.HELD;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Backpack();
	}

}
