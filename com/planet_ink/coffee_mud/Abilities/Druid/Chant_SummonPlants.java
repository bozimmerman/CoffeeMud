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

public class Chant_SummonPlants extends Chant
{
	public String ID() { return "Chant_SummonPlants"; }
	public String name(){ return "Summon Plants";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected Room PlantsLocation=null;
	protected Item littlePlants=null;

	public void unInvoke()
	{
		if(PlantsLocation==null)
			return;
		if(littlePlants==null)
			return;
		if(canBeUninvoked())
			PlantsLocation.showHappens(CMMsg.MSG_OK_VISUAL,littlePlants.name()+" wither"+(littlePlants.name().startsWith("s")?"":"s")+" away.");
		super.unInvoke();
		if(canBeUninvoked())
		{
			Item plants=littlePlants; // protects against uninvoke loops!
			littlePlants=null;
			plants.destroy();
			PlantsLocation.recoverRoomStats();
			PlantsLocation=null;
		}
	}

	public String text()
	{
		if((miscText.length()==0)
		&&(invoker()!=null))
			miscText=invoker().Name();
		return super.text();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(littlePlants))
		&&(msg.targetMinor()==CMMsg.TYP_GET))
			msg.addTrailerMsg(new FullMsg(msg.source(),littlePlants,null,CMMsg.MSG_OK_VISUAL,CMMsg.MASK_GENERAL|CMMsg.MSG_DEATH,-1,null));
	}

	public static Item buildPlant(MOB mob, Room room)
	{
		Item newItem=CMClass.getItem("GenItem");
		newItem.setMaterial(EnvResource.RESOURCE_GREENS);
		switch(Dice.roll(1,5,0))
		{
		case 1:
			newItem.setName("some happy flowers");
			newItem.setDisplayText("some happy flowers are growing here.");
			newItem.setDescription("Happy flowers with little red and yellow blooms.");
			break;
		case 2:
			newItem.setName("some happy weeds");
			newItem.setDisplayText("some happy weeds are growing here.");
			newItem.setDescription("Long stalked little plants with tiny bulbs on top.");
			break;
		case 3:
			newItem.setName("a pretty fern");
			newItem.setDisplayText("a pretty fern is growing here.");
			newItem.setDescription("Like a tiny bush, this dark green plant is lovely.");
			break;
		case 4:
			newItem.setName("a patch of sunflowers");
			newItem.setDisplayText("a patch of sunflowers is growing here.");
			newItem.setDescription("Happy flowers with little yellow blooms.");
			break;
		case 5:
			newItem.setName("a patch of bluebonnets");
			newItem.setDisplayText("a patch of bluebonnets is growing here.");
			newItem.setDescription("Happy flowers with little blue and purple blooms.");
			break;
		}
		newItem.baseEnvStats().setWeight(1);
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		newItem.setDispossessionTime(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" sprout(s) up here.");
		Chant_SummonPlants newChant=new Chant_SummonPlants();
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
		return buildPlant(mob,room);
	}

	public boolean rightPlace(MOB mob,boolean auto)
	{
		if((!auto)&&(mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}

		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!rightPlace(mob,auto)) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the ground.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				buildMyPlant(mob,mob.location());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the ground, but nothing happens.");

		// return whether it worked
		return success;
	}
}
