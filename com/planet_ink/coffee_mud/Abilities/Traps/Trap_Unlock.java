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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(sprung)
		{
			if(msg.source().isMine(affected))
				unInvoke();
			else
				super.executeMsg(myHost,msg);
			return;
		}
		super.executeMsg(myHost,msg);

		if(msg.amITarget(affected))
		{
			if((msg.targetMinor()==CMMsg.TYP_UNLOCK)
			||(msg.targetMinor()==CMMsg.TYP_JUSTICE)
			||(msg.targetMinor()==CMMsg.TYP_DELICATE_HANDS_ACT))
			{
				spring(msg.source());
			}
		}
	}
}
