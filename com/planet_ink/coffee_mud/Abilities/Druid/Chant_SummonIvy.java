package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_SummonIvy extends Chant_SummonPlants
{
	public String ID() { return "Chant_SummonIvy"; }
	public String name(){ return "Summon Ivy";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	private boolean processing=false;

	public static Item buildIvy(MOB mob, Room room)
	{
		Item newItem=(Item)CMClass.getItem("GenItem");
		newItem.setMaterial(EnvResource.RESOURCE_GREENS);
		switch(Dice.roll(1,5,0))
		{
		case 1:
		case 4:
			newItem.setName("poison ivy");
			newItem.setDisplayText("a lovely trifoliate is growing here.");
			newItem.setDescription("");
			break;
		case 2:
			newItem.setName("poison sumac");
			newItem.setDisplayText("a small pinnately leafletted tree grows here");
			newItem.setDescription("");
			break;
		case 3:
		case 5:
			newItem.setName("poison oak");
			newItem.setDisplayText("a lovely wrinkled plant grows here");
			newItem.setDescription("");
			break;
		}
		newItem.baseEnvStats().setWeight(1);
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		newItem.addNonUninvokableEffect(CMClass.getAbility("Disease_PoisonIvy"));
		room.addItem(newItem);
		newItem.setDispossessionTime(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" sprout(s) up here.");
		Chant_SummonIvy newChant=new Chant_SummonIvy();
		newChant.PlantsLocation=room;
		newChant.littlePlants=newItem;
		if(CoffeeUtensils.doesOwnThisProperty(mob,room))
		{
			newChant.setInvoker(mob);
			newChant.setMiscText(mob.name());
			newItem.addNonUninvokableEffect(newChant);
		}
		else
			newChant.beneficialAffect(mob,newItem,0,(newChant.adjustedLevel(mob,0)*240)+450);
		room.recoverEnvStats();
		return newItem;
	}

	public Item buildMyPlant(MOB mob, Room room)
	{
		return buildIvy(mob,room);
	}
}
