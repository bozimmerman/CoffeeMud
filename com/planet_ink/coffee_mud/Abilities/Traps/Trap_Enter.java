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

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(sprung) return super.okAffect(myHost,affect);
		if(!super.okAffect(myHost,affect))
			return false;

		   if((affect.amITarget(affected))
 			||((affect.tool()!=null)&&(affect.tool()==affected)))
		{
			if((affect.targetMinor()==Affect.TYP_ENTER)
			||(affect.targetMinor()==Affect.TYP_LEAVE)
			||(affect.targetMinor()==Affect.TYP_FLEE))
			{
				if(affect.targetMinor()==Affect.TYP_LEAVE)
					return true;
				else
				{
					spring(affect.source());
					return false;
				}
			}
		}
		return true;
	}
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if(sprung)
			return;

		if((affect.amITarget(affected))||((affect.tool()!=null)&&(affect.tool()==affected)))
		{
			if(affect.targetMinor()==Affect.TYP_LEAVE)
			{
				spring(affect.source());
			}
		}
	}
}
