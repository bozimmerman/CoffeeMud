package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class TrappedDoor extends StdClosedDoorway
{
	public TrappedDoor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		Trap t=(Trap)CMClass.getAbility("Trap_Open");
		if(t!=null) t.setTrapped(this,true);
	}
	public Environmental newInstance()
	{
		return new TrappedDoor();
	}
}
