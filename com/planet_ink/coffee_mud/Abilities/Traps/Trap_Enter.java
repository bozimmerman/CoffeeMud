package com.planet_ink.coffee_mud.Abilities.Traps;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Trap_Enter extends Trap_Trap
{
	public String ID() { return "Trap_Enter"; }
	public String name(){ return "Entry Trap";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Trap_Enter();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(sprung) return super.okMessage(myHost,msg);
		if(!super.okMessage(myHost,msg))
			return false;

		   if((msg.amITarget(affected))
 			||((msg.tool()!=null)&&(msg.tool()==affected)))
		{
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			||(msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.targetMinor()==CMMsg.TYP_FLEE))
			{
				if(msg.targetMinor()==CMMsg.TYP_LEAVE)
					return true;
				else
				{
					spring(msg.source());
					return false;
				}
			}
		}
		return true;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(sprung)
			return;

		if((msg.amITarget(affected))||((msg.tool()!=null)&&(msg.tool()==affected)))
		{
			if(msg.targetMinor()==CMMsg.TYP_LEAVE)
			{
				spring(msg.source());
			}
		}
	}
}
