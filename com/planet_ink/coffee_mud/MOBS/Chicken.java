package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class Chicken extends StdMOB
{
	public String ID(){return "Chicken";}
	public Chicken()
	{
		super();
		Username="a chicken";
		setDescription("a fat, short winged bird");
		setDisplayText("A chicken is here, not flying.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(0);

		baseEnvStats().setDamage(1);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(90);
		baseCharStats().setMyRace(CMClass.getRace("Chicken"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==MudHost.TICK_MOB)
		{
			if(Dice.rollPercentage()>95)
			{
				Item I=CMClass.getItem("GenFoodResource");
				I.setName("an egg");
				I.setDisplayText("an egg has been left here.");
				I.setMaterial(EnvResource.RESOURCE_EGGS);
				I.setDescription("It looks like a chicken egg!");
				I.baseEnvStats().setWeight(1);
				addInventory((Item)I.copyOf());
			}
			if((inventorySize()>8)&&(location()!=null))
			{
				Item I=fetchInventory(Dice.roll(1,inventorySize(),-1));
				if(I.name().equals("an egg"))
				{
					location().show(this,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> lay(s) an egg.");
					I.removeFromOwnerContainer();
					location().addItemRefuse(I,Item.REFUSE_RESOURCE);
					location().recoverRoomStats();
				}
			}
		}
		return true;
	}
}
