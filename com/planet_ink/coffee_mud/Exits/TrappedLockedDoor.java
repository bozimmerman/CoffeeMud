package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class TrappedLockedDoor extends StdLockedDoorway
{
	public String ID(){	return "TrappedLockedDoor";}
	public TrappedLockedDoor()
	{
		super();
		Trap t=(Trap)CMClass.getAbility("Trap_Trap");
		if(t!=null) CMClass.setTrapped(this,t,true);
	}
	public Environmental newInstance()
	{
		return new TrappedLockedDoor();
	}
}
