package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.*;

public class PitOpen extends StdOpenDoorway
{
	public PitOpen()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		Trap t=new Trap_Enter();
		t.baseEnvStats().setAbility(Trap.TRAP_PIT_BLADE);
		t.recoverEnvStats();
		Thief_Trap.setTrapped(this,t,true);
	}
	public Environmental newInstance()
	{
		return new PitOpen();
	}
}
