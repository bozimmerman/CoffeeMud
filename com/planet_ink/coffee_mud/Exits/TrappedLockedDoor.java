package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.*;

public class TrappedLockedDoor extends StdLockedDoorway
{
	public TrappedLockedDoor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		Thief_Trap.setTrapped(this,true);
	}
	public Environmental newInstance()
	{
		return new TrappedLockedDoor();
	}
}
