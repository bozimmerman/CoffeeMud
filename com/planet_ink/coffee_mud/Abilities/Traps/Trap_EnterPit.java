package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_EnterPit extends Trap_Enter
{
	public Trap_EnterPit()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		canTargetCode=0;
		canAffectCode=Ability.CAN_EXITS;
		baseEnvStats().setAbility(Trap.TRAP_PIT_BLADE);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Trap_Enter();
	}
}
