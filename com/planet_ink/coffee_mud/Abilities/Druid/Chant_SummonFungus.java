package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Chant_SummonFungus extends Chant_SummonPlants
{
	@Override
	public String ID()
	{
		return "Chant_SummonFungus";
	}

	private final static String localizedName = CMLib.lang().L("Summon Fungus");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTGROWTH;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected boolean processing = false;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(littlePlantsI))
		&&(!processing)
		&&((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL)))
		{
			processing=true;
			final Ability A=littlePlantsI.fetchEffect(ID());
			if(A!=null)
			{
				CMLib.threads().deleteTick(A,-1);
				littlePlantsI.delEffect(A);
				littlePlantsI.setSecretIdentity("");
			}
			if(littlePlantsI.fetchBehavior("Decay")==null)
			{
				final Behavior B=CMClass.getBehavior("Decay");
				B.setParms("min="+CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH)+" max="+CMProps.getIntVar(CMProps.Int.TICKSPERMUDMONTH)+" chance=100");
				littlePlantsI.addBehavior(B);
				B.executeMsg(myHost,msg);
			}
			processing=false;
		}
	}

	@Override
	public boolean rightPlace(MOB mob,boolean auto)
	{
		if((!auto)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		&&((mob.location().getAtmosphere()&RawMaterial.MATERIAL_ROCK)==0))
		{
			mob.tell(L("This is not the place for fungus."));
			return false;
		}
		return true;
	}

	public Item buildFungus(MOB mob, Room room)
	{
		final Item newItem;
		if( CMLib.flags().isWateryRoom(room)
		&& (room instanceof Drink)
		&& (((Drink)room).liquidType() == RawMaterial.RESOURCE_FRESHWATER))
		{
			newItem=CMClass.getItem("GenResource");
			newItem.setMaterial(RawMaterial.RESOURCE_FUNGUS);
			switch(CMLib.dice().roll(1,6,0))
			{
			case 1:
				newItem.setName(L("an aquaphila fungus"));
				newItem.setDisplayText(L("an aquaphila fungus grows here."));
				newItem.setDescription("");
				break;
			case 2:
				newItem.setName(L("a canalisporum fungus"));
				newItem.setDisplayText(L("a canalisporum fungus grows here."));
				newItem.setDescription("");
				break;
			case 3:
				newItem.setName(L("an elagantimyces fungus"));
				newItem.setDisplayText(L("an elagantimyces fungus grows here"));
				newItem.setDescription("");
				break;
			case 4:
				newItem.setName(L("a jeranium fungus"));
				newItem.setDisplayText(L("a jeranium fungus grows here."));
				newItem.setDescription("");
				break;
			case 5:
				newItem.setName(L("a yinmingella  fungus"));
				newItem.setDisplayText(L("a yinmingella fungus grows here."));
				newItem.setDescription("");
				break;
			default:
			case 6:
				newItem.setName(L("a nidulispora fungus"));
				newItem.setDisplayText(L("a nidulispora fungus grows here."));
				newItem.setDescription("");
				break;
			}
		}
		else
		{
			newItem=CMClass.getItem("GenFoodResource");
			newItem.setMaterial(RawMaterial.RESOURCE_MUSHROOMS);
			switch(CMLib.dice().roll(1,6,0))
			{
			case 1:
				newItem.setName(L("a mushroom"));
				newItem.setDisplayText(L("a mushroom is here."));
				newItem.setDescription("");
				break;
			case 2:
				newItem.setName(L("a shiitake mushroom"));
				newItem.setDisplayText(L("a shiitake mushroom grows here."));
				newItem.setDescription("");
				break;
			case 3:
				newItem.setName(L("a cremini mushroom"));
				newItem.setDisplayText(L("a cremini mushroom grows here"));
				newItem.setDescription("");
				break;
			case 4:
				newItem.setName(L("a white mushroom"));
				newItem.setDisplayText(L("a white mushroom grows here."));
				newItem.setDescription("");
				break;
			case 5:
				newItem.setName(L("a portabello mushroom"));
				newItem.setDisplayText(L("a portabello mushroom grows here."));
				newItem.setDescription("");
				break;
			default:
			case 6:
				newItem.setName(L("a wood ear"));
				newItem.setDisplayText(L("a wood ear grows here."));
				newItem.setDescription("");
				break;
			}
		}
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		Druid_MyPlants.addNewPlant(mob, newItem);
		room.addItem(newItem);
		final Chant_SummonFungus newChant=new Chant_SummonFungus();
		newItem.basePhyStats().setLevel(10+newChant.getX1Level(mob));
		newItem.basePhyStats().setWeight(1);
		newItem.setExpirationDate(0);
		CMLib.materials().addEffectsToResource(newItem);
		room.showHappens(CMMsg.MSG_OK_ACTION,CMLib.lang().L("Suddenly, @x1 sprouts up here.",newItem.name()));
		newChant.plantsLocationR=room;
		newChant.littlePlantsI=newItem;
		if(CMLib.law().doesOwnThisLand(mob,room))
		{
			newChant.setInvoker(mob);
			newChant.setMiscText(mob.Name());
			newItem.addNonUninvokableEffect(newChant);
		}
		else
			newChant.beneficialAffect(mob,newItem,0,(newChant.adjustedLevel(mob,0)*240)+450);
		room.recoverPhyStats();
		return newItem;
	}

	@Override
	protected Item buildMyPlant(MOB mob, Room room)
	{
		return buildFungus(mob,room);
	}
}
