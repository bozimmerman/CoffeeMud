package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_EnterPit extends Trap_Enter
{
	public String ID() { return "Trap_EnterPit"; }
	public String name(){ return "Entry Pit Trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS;}
	protected int canTargetCode(){return 0;}
	protected int trapType(){return TRAP_PIT_BLADE;}
	public Environmental newInstance(){	return new Trap_Enter();}
}
