package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class NeedleDoor extends StdClosedDoorway
{
	public String ID(){	return "NeedleDoor";}
	public NeedleDoor()
	{
		super();
		Trap t=(Trap)CMClass.getAbility("Trap_OpenNeedle");
		if(t!=null) CoffeeUtensils.setTrapped(this,t,true);
	}
	public Environmental newInstance()
	{
		return new NeedleDoor();
	}
}
