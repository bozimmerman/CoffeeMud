package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class PitOpen extends StdOpenDoorway
{
	public String ID(){	return "PitOpen";}
	public PitOpen()
	{
		super();
		Trap t=(Trap)CMClass.getAbility("Trap_Enter");
		if(t!=null)
		{
			t.baseEnvStats().setAbility(Trap.TRAP_PIT_BLADE);
			t.recoverEnvStats();
			t.setTrapped(this,t,true);
		}
	}
	public Environmental newInstance()
	{
		return new PitOpen();
	}
}
