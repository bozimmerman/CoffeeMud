package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Abilities.*;

public class NeedleChest extends LargeChest
{
	public NeedleChest()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		Trap t=new Trap_Open();
		t.baseEnvStats().setAbility(Trap.TRAP_NEEDLE);
		t.recoverEnvStats();
		Thief_Trap.setTrapped(this,t,true);
		isLocked=false;
	}
	
	public Environmental newInstance()
	{
		return new NeedleChest();
	}
}
