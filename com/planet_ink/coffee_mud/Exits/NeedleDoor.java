package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class NeedleDoor extends StdClosedDoorway
{
	public String ID(){	return "NeedleDoor";}
	public NeedleDoor()
	{
		super();
		Trap t=(Trap)CMClass.getAbility("Trap_Open");
		if(t!=null)
		{
			t.baseEnvStats().setAbility(Trap.TRAP_NEEDLE);
			t.recoverEnvStats();
			t.setTrapped(this,t,true);
		}
	}
	public Environmental newInstance()
	{
		return new NeedleDoor();
	}
}
