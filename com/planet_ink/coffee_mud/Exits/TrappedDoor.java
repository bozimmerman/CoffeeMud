package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.*;

public class TrappedDoor extends StdClosedDoorway
{
	public TrappedDoor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		Trap t=new Trap_Open();
		Thief_Trap.setTrapped(this,true);
	}
	public Environmental newInstance()
	{
		return new TrappedDoor();
	}
}
