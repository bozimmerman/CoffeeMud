package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class TrappedDoor extends StdClosedDoorway
{
	public String ID(){	return "TrappedDoor";}
	public TrappedDoor()
	{
		super();
		Trap t=(Trap)CMClass.getAbility("Trap_Open");
		if(t!=null) CoffeeUtensils.setTrapped(this,t,true);
	}
}
