package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_EnterNeedle extends Trap_Enter
{
	public String ID() { return "Trap_EnterNeedle"; }
	public String name(){ return "Entry Needle Trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapType(){return TRAP_NEEDLE;}
	public Environmental newInstance(){	return new Trap_EnterNeedle();}
}
