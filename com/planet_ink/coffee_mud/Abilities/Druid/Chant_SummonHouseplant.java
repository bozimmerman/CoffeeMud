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

public class Chant_SummonHouseplant extends Chant_SummonPlants
{
	public String ID() { return "Chant_SummonHouseplant"; }
	public String name(){ return "Summon Houseplant";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	private boolean processing=false;

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(littlePlants))
		&&(!processing)
		&&(msg.targetMinor()==CMMsg.TYP_GET))
		{
			processing=true;
			Ability A=littlePlants.fetchEffect(ID());
			if(A!=null)
			{
				CMClass.ThreadEngine().deleteTick(A,-1);
				littlePlants.delEffect(A);
				littlePlants.setSecretIdentity("");
			}
			if(littlePlants.fetchBehavior("Decay")==null)
			{
				Behavior B=CMClass.getBehavior("Decay");
				B.setParms("min="+CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)+" max="+CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)+" chance=100");
				littlePlants.addBehavior(B);
				B.executeMsg(myHost,msg);
			}
			processing=false;
		}
	}
	public boolean rightPlace(MOB mob,boolean auto)
	{
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_STONE)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WOOD))
		{
			mob.tell("This is not the place for a houseplant.");
			return false;
		}
		return true;
	}

	public static Item buildHouseplant(MOB mob, Room room)
	{
		Item newItem=(Item)CMClass.getItem("GenItem");
		newItem.setMaterial(EnvResource.RESOURCE_GREENS);
		switch(Dice.roll(1,7,0))
		{
		case 1:
			newItem.setName("a potted rose");
			newItem.setDisplayText("a potted rose is here.");
			newItem.setDescription("");
			break;
		case 2:
			newItem.setName("a potted daisy");
			newItem.setDisplayText("a potted daisy is here.");
			newItem.setDescription("");
			break;
		case 3:
			newItem.setName("a potted carnation");
			newItem.setDisplayText("a potted white carnation is here");
			newItem.setDescription("");
			break;
		case 4:
			newItem.setName("a potted sunflower");
			newItem.setDisplayText("a potted sunflowers is here.");
			newItem.setDescription("Happy flowers have little yellow blooms.");
			break;
		case 5:
			newItem.setName("a potted gladiola");
			newItem.setDisplayText("a potted gladiola is here.");
			newItem.setDescription("");
			break;
		case 6:
			newItem.setName("a potted fern");
			newItem.setDisplayText("a potted fern is here.");
			newItem.setDescription("Like a tiny bush, this dark green plant is lovely.");
			break;
		case 7:
			newItem.setName("a potted patch of bluebonnets");
			newItem.setDisplayText("a potted patch of bluebonnets is here.");
			newItem.setDescription("Happy flowers with little blue and purple blooms.");
			break;
		}
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		newItem.baseEnvStats().setWeight(1);
		newItem.setDispossessionTime(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" appears here.");
		Chant_SummonFlower newChant=new Chant_SummonFlower();
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
		return buildHouseplant(mob,room);
	}
}
