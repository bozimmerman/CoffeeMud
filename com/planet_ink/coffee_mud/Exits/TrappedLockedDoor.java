package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class TrappedLockedDoor extends StdLockedDoorway
{
	public TrappedLockedDoor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		Trap t=(Trap)CMClass.getAbility("Trap_Trap");
		if(t!=null) t.setTrapped(this,true);
	}
	public Environmental newInstance()
	{
		return new TrappedLockedDoor();
	}
}
