package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.service.*;
import java.util.*;

public class Trap_Enter extends Trap
{
	public Trap_Enter()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	
	public Environmental newInstance()
	{
		return new Trap_Enter();
	}
	
	public boolean okAffect(Affect affect)
	{
		if(sprung) return super.okAffect(affect);
		if(!super.okAffect(affect))
			return false;
		
		if((affect.amITarget(affected))||((affect.tool()!=null)&&(affect.tool()==affected)))
		{
			if((affect.targetCode()==Affect.MOVE_ENTER)
			||(affect.targetCode()==Affect.MOVE_LEAVE)
			||(affect.targetCode()==Affect.MOVE_FLEE))
			{
				if(affect.targetCode()==Affect.MOVE_LEAVE)
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
	public void affect(Affect affect)
	{
		super.affect(affect);
		if(sprung) 
			return;
		
		if((affect.amITarget(affected))||((affect.tool()!=null)&&(affect.tool()==affected)))
		{
			if(affect.targetCode()==Affect.MOVE_LEAVE)
			{
				spring(affect.source());
			}
		}
	}
}
