package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_OpenGas extends Trap_Open
{
	public String ID() { return "Trap_OpenGas"; }
	public String name(){ return "Open Gas Trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapType(){return TRAP_GAS;}
	public Environmental newInstance(){	return new Trap_OpenGas();}
}
