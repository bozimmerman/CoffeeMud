package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.service.*;
import java.util.*;

public class Trap_Get extends Trap
{
	public Trap_Get()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	
	public Environmental newInstance()
	{
		return new Trap_Get();
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
			if(affect.targetCode()==Affect.HANDS_GET)
			{
				spring(affect.source());
			}
		}
	}
}
