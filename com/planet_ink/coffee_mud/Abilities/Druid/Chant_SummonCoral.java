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
   Copyright 2016-2025 Bo Zimmerman

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
public class Chant_SummonCoral extends Chant_SummonPlants
{
	@Override
	public String ID()
	{
		return "Chant_SummonCoral";
	}

	private final static String localizedName = CMLib.lang().L("Summon Coral");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_PLANTGROWTH;
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

	protected boolean seaOk()
	{
		return true;
	}

	private final String[] coralTypes = new String[]
	{
		CMLib.lang().L("staghorn coral"),
		CMLib.lang().L("coral"),
		CMLib.lang().L("pillar coral"),
		CMLib.lang().L("elkhorn coral"),
		CMLib.lang().L("sea fan"),
		CMLib.lang().L("antipathes"),
		CMLib.lang().L("leptopsammia"),
		CMLib.lang().L("brain coral"),
		CMLib.lang().L("cup coral"),
		CMLib.lang().L("soft brush coral"),
		CMLib.lang().L("wire coral"),
		CMLib.lang().L("bottle brush coral"),
		CMLib.lang().L("lace coral"),
		CMLib.lang().L("blade coral"),
		CMLib.lang().L("box fire coral"),
		CMLib.lang().L("fleshy coral"),
		CMLib.lang().L("plate coral"),
		CMLib.lang().L("sheet coral")
	};

	public Item buildCoral(final MOB mob, final Room room)
	{

		final Item newItem=CMClass.getItem("GenItem");
		newItem.setMaterial(RawMaterial.RESOURCE_SEAWEED);
		final String name = coralTypes[CMLib.dice().roll(1, coralTypes.length, -1)];
		newItem.setName(name);
		newItem.setDisplayText(L("(a(n)) @x1 is here.",name));
		newItem.setDescription("");

		final Chant_SummonCoral newChant=new Chant_SummonCoral();
		newItem.basePhyStats().setLevel(10+newChant.getX1Level(mob));
		newItem.basePhyStats().setWeight(1);
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		room.addItem(newItem);
		Druid_MyPlants.addNewPlant(mob, newItem);
		newItem.setExpirationDate(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,CMLib.lang().L("Suddenly, @x1 appear(s) here.",newItem.name()));
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
	protected Item buildMyPlant(final MOB mob, final Room room)
	{
		return buildCoral(mob,room);
	}

	public boolean isSaltWaterRoom(final Room R)
	{
		if((R==null)
		||(!CMLib.flags().isUnderWateryRoom(R))
		||(!(R instanceof Drink))
		||(((Drink)R).liquidType()!=RawMaterial.RESOURCE_SALTWATER))
			return false;
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final Room R=CMLib.map().roomLocation(affected);
		if(isSaltWaterRoom(R) && (R.expirationDate() != 0))
		{
			R.setExpirationDate(System.currentTimeMillis() + WorldMap.ROOM_EXPIRATION_MILLIS);
		}
		return true;
	}

	@Override
	public boolean rightPlace(final MOB mob,final boolean auto)
	{
		final Room R=mob.location();
		if(!isSaltWaterRoom(R))
		{
			mob.tell(L("This magic will not work here."));
			return false;
		}
		return true;
	}

}
