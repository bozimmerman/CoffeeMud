package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_Open extends Trap_Trap
{
	public String ID() { return "Trap_Open"; }
	public String name(){ return "Open Trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Trap_Open();}

	public void affect(Affect affect)
	{
		if(sprung)
		{
			if(affect.source().isMine(affected))
				unInvoke();
			else
				super.affect(affect);
			return;
		}
		super.affect(affect);

		if(affect.amITarget(affected))
		{
			if(affect.targetMinor()==Affect.TYP_OPEN)
				spring(affect.source());
		}
	}
}
