package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_Unlock extends Trap_Trap
{
	public String ID() { return "Trap_Unlock"; }
	public String name(){ return "Unlock Trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Trap_Unlock();}

	public void affect(Environmental myHost, Affect affect)
	{
		if(sprung)
		{
			if(affect.source().isMine(affected))
				unInvoke();
			else
				super.affect(myHost,affect);
			return;
		}
		super.affect(myHost,affect);

		if(affect.amITarget(affected))
		{
			if((affect.targetMinor()==Affect.TYP_UNLOCK)
			||(affect.targetMinor()==Affect.TYP_JUSTICE)
			||(affect.targetMinor()==Affect.TYP_DELICATE_HANDS_ACT))
			{
				spring(affect.source());
			}
		}
	}
}
