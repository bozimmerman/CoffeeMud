package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_OpenNeedle extends Trap_Open
{
	public Trap_OpenNeedle()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		canTargetCode=0;
		canAffectCode=Ability.CAN_EXITS|Ability.CAN_ITEMS;
		baseEnvStats().setAbility(Trap.TRAP_NEEDLE);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Trap_OpenNeedle();
	}
}
