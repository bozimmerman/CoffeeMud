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

public class Chant_SummonHouseplant extends Chant_SummonPlants
{
	public String ID() { return "Chant_SummonHouseplant"; }
	public String name(){ return "Summon Houseplant";}
    public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	protected boolean processing=false;

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
				CMLib.threads().deleteTick(A,-1);
				littlePlants.delEffect(A);
				littlePlants.setSecretIdentity("");
			}
			if(littlePlants.fetchBehavior("Decay")==null)
			{
				Behavior B=CMClass.getBehavior("Decay");
				B.setParms("min="+CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH)+" max="+CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDMONTH)+" chance=100");
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
		Item newItem=CMClass.getItem("GenItem");
		newItem.setMaterial(RawMaterial.RESOURCE_GREENS);
		switch(CMLib.dice().roll(1,7,0))
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
		Chant_SummonHouseplant newChant=new Chant_SummonHouseplant();
		newItem.baseEnvStats().setWeight(1);
		newItem.baseEnvStats().setLevel(10+newChant.getX1Level(mob));
		newItem.setExpirationDate(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" appears here.");
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
		return buildHouseplant(mob,room);
	}
}
