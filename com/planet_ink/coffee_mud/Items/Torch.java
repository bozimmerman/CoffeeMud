package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Torch extends LightSource
{
	public String ID(){	return "Torch";}
	public Torch()
	{
		super();
		setName("a torch");
		setDisplayText("a small straw torch sits here.");
		setDescription("It looks like it is lightly covered in oil near the end.");
		durationTicks=30;

		material=EnvResource.RESOURCE_OAK;
		this.destroyedWhenBurnedOut=true;
		this.goesOutInTheRain=true;
		baseGoldValue=1;
	}
	public Environmental newInstance()
	{
		return new Torch();
	}


}
