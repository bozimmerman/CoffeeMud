package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_UnlockSpell extends Trap_Unlock
{
	public String ID() { return "Trap_UnlockSpell"; }
	public String name(){ return "Unlock Spell Trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected int trapType(){return TRAP_SPELL;}
	public Trap_UnlockSpell()
	{
		super();
		setMiscText("Spell_Sleep");
	}
	public Environmental newInstance(){	return new Trap_UnlockSpell();}
}
