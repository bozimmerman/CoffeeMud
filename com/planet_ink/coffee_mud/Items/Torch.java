package com.planet_ink.coffee_mud.Items;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Torch extends LightSource
{
	public Torch()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a torch";
		displayText="a small straw torch sits here.";
		description="It looks like it is lightly covered in oil near the end.";
		durationTicks=30;

		material=Item.WOODEN;
		this.destroyedWhenBurnedOut=true;
		this.goesOutInTheRain=true;
		baseGoldValue=1;
	}
	public Environmental newInstance()
	{
		return new Torch();
	}


}
