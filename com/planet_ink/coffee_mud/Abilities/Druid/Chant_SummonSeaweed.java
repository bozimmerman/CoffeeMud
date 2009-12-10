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

public class Chant_SummonSeaweed extends Chant_SummonPlants
{
	public String ID() { return "Chant_SummonSeaweed"; }
	public String name(){ return "Summon Seaweed";}
    public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected boolean seaOk(){return true;}

	public static Item buildSeaweed(MOB mob, Room room)
	{
		Item newItem=CMClass.getItem("GenItem");
		newItem.setMaterial(RawMaterial.RESOURCE_SEAWEED);
		switch(CMLib.dice().roll(1,5,0))
		{
		case 1:
			newItem.setName("some algae");
			newItem.setDisplayText("some algae is here.");
			newItem.setDescription("");
			break;
		case 2:
			newItem.setName("some seaweed");
			newItem.setDisplayText("some seaweed is here.");
			newItem.setDescription("");
			break;
		case 3:
			newItem.setName("some kelp");
			newItem.setDisplayText("some kelp is here");
			newItem.setDescription("");
			break;
		case 4:
			newItem.setName("some coral");
			newItem.setDisplayText("some coral is here.");
			newItem.setDescription("");
			break;
		case 5:
			newItem.setName("some sponge");
			newItem.setDisplayText("some sponge is here.");
			newItem.setDescription("");
			break;
		}
		Chant_SummonSeaweed newChant=new Chant_SummonSeaweed();
		newItem.baseEnvStats().setLevel(10+newChant.getX1Level(mob));
		newItem.baseEnvStats().setWeight(1);
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
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
		return buildSeaweed(mob,room);
	}

	public boolean rightPlace(MOB mob,boolean auto)
	{
		if((!auto)&&(mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}

		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
		   &&(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE))
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		return true;
	}


}
