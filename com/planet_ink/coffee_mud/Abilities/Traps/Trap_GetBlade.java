package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_GetBlade extends Trap_Get
{
	public String ID() { return "Trap_GetBlade"; }
	public String name(){ return "Get Blade Trap";}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapType(){return TRAP_PIT_BLADE;}
}
