package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
			    if((!hungry)
			    &&(mob.curState().getHunger()>=mob.maxState().getHunger())
				&&(Dice.roll(1,500,0)==1)
				&&(!Sense.isGolem(msg.source()))
				&&(msg.source().fetchEffect("Disease_Obesity")==null))
				{
				    Ability A=CMClass.getAbility("Disease_Obesity");
				    if(A!=null){A.invoke(mob,mob,true,0);}
				}
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
