package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdFood extends StdItem implements Food
{
	public String ID(){	return "StdFood";}
	protected int amountOfNourishment=500;

	public StdFood()
	{
		super();
		setName("a bit of food");
		baseEnvStats.setWeight(2);
		setDisplayText("a bit of food is here.");
		setDescription("Looks like some mystery meat");
		baseGoldValue=5;
		material=EnvResource.RESOURCE_MEAT;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdFood();
	}

	public int nourishment()
	{
		return amountOfNourishment;
	}
	public void setNourishment(int amount)
	{
		amountOfNourishment=amount;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				if(mob.isMine(this))
					return true;
				else
				{
					mob.tell("You don't have that.");
					return false;
				}
			}
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_EAT:
				boolean hungry=mob.curState().getHunger()<=0;
				boolean full=!mob.curState().adjHunger(amountOfNourishment,mob.maxState());
				if(hungry)
					mob.tell("You are no longer hungry.");
				else
				if(full)
					mob.tell("You are full.");
				this.destroy();
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
				break;
			default:
				break;
			}
		}
	}
}
