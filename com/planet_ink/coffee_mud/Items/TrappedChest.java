package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Abilities.*;

public class TrappedChest extends LargeChest
{
	public TrappedChest()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		Thief_Trap.setTrapped(this,true);
		isLocked=false;
	}
	
	public Environmental newInstance()
	{
		return new TrappedChest();
	}
}
