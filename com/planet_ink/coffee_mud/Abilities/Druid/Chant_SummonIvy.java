package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

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
    public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}

	public static Item buildIvy(MOB mob, Room room)
	{
		Item newItem=CMClass.getItem("GenItem");
		newItem.setMaterial(RawMaterial.RESOURCE_GREENS);
		switch(CMLib.dice().roll(1,5,0))
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
		Chant_SummonIvy newChant=new Chant_SummonIvy();
		newItem.baseEnvStats().setLevel(10+newChant.getX1Level(mob));
		newItem.baseEnvStats().setWeight(1);
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		newItem.addNonUninvokableEffect(CMClass.getAbility("Disease_PoisonIvy"));
		room.addItem(newItem);
		newItem.setExpirationDate(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" sprout(s) up here.");
		newChant.PlantsLocation=room;
		newChant.littlePlants=newItem;
		if(CMLib.law().doesOwnThisProperty(mob,room))
		{
			newChant.setInvoker(mob);
			newChant.setMiscText(mob.Name());
			newItem.addNonUninvokableEffect(newChant);
		}
		else
			newChant.beneficialAffect(mob,newItem,0,(newChant.adjustedLevel(mob,0)*240)+450);
		room.recoverEnvStats();
		return newItem;
	}

    protected Item buildMyPlant(MOB mob, Room room)
	{
		return buildIvy(mob,room);
	}
}
