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
			if(msg.targetMinor()==CMMsg.TYP_OPEN)
				spring(msg.source());
		}
	}
}
