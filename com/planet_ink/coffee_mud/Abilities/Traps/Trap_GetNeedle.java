package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_GetNeedle extends Trap_Get
{
	public String ID() { return "Trap_GetNeedle"; }
	public String name(){ return "Get Needle Trap";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapType(){return TRAP_NEEDLE;}
	public Environmental newInstance(){	return new Trap_GetNeedle();}
}
