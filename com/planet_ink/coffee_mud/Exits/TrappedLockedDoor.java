package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class TrappedLockedDoor extends StdLockedDoorway
{
	public String ID(){	return "TrappedLockedDoor";}
	public TrappedLockedDoor()
	{
		super();
		Trap t=(Trap)CMClass.getAbility("Trap_Trap");
		if(t!=null) CoffeeUtensils.setTrapped(this,t,true);
	}
	public Environmental newInstance()
	{
		return new TrappedLockedDoor();
	}
}
