package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_UnlockBlade extends Trap_Unlock
{
	public String ID() { return "Trap_UnlockBlade"; }
	public String name(){ return "Unlock Blade Trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapType(){return TRAP_PIT_BLADE;}
	public Environmental newInstance(){	return new Trap_UnlockBlade();}
}
