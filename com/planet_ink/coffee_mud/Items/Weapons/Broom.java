package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Broom extends Quarterstaff
{
	public String ID(){	return "Broom";}
	public Broom()
	{
		super();

		setName("a broom");
		setDisplayText("a broom lies in the corner of the room.");
		setDescription("It`s long and wooden, with lots of bristles on one end.");
		material=EnvResource.RESOURCE_OAK;
	}


}
