package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_EnterBlade extends Trap_Enter
{
	public String ID() { return "Trap_EnterBlade"; }
	public String name(){ return "Entry Blade Trap";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapType(){return TRAP_PIT_BLADE;}
	public Environmental newInstance(){	return new Trap_EnterBlade();}
}
