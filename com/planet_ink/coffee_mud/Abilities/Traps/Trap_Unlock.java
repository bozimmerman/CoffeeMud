package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_Unlock extends Trap_Trap
{
	public Trap_Unlock()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		canTargetCode=0;
		canAffectCode=Ability.CAN_EXITS|Ability.CAN_ITEMS;
	}

	public Environmental newInstance()
	{
		return new Trap_Unlock();
	}

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
			if((affect.targetMinor()==Affect.TYP_UNLOCK)
			||(affect.targetMinor()==Affect.TYP_DELICATE_HANDS_ACT))
			{
				spring(affect.source());
			}
		}
	}
}
