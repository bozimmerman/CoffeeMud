package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class TrappedDoor extends StdClosedDoorway
{
	public String ID(){	return "TrappedDoor";}
	public TrappedDoor()
	{
		super();
		Trap t=(Trap)CMClass.getAbility("Trap_Open");
		if(t!=null) CMClass.setTrapped(this,t,true);
	}
	public Environmental newInstance()
	{
		return new TrappedDoor();
	}
}
