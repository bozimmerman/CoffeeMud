package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.*;

public class NeedleDoor extends StdClosedDoorway
{
	public NeedleDoor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		Trap t=new Trap_Open();
		t.baseEnvStats().setAbility(Trap.TRAP_NEEDLE);
		t.recoverEnvStats();
		Thief_Trap.setTrapped(this,t,true);
	}
	public Environmental newInstance()
	{
		return new NeedleDoor();
	}
}
