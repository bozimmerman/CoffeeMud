package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class TrappedChest extends LargeChest
{
	public String ID(){	return "TrappedChest";}
	public TrappedChest()
	{
		super();
		((Trap)CMClass.getAbility("Trap_Trap")).setTrapped(this,true);
		material=EnvResource.RESOURCE_OAK;
		isLocked=false;
	}

	public Environmental newInstance()
	{
		return new TrappedChest();
	}
}
