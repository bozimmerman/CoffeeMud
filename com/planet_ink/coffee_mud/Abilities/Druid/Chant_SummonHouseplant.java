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

public class Chant_SummonHouseplant extends Chant_SummonPlants
{
	@Override
	public String ID()
	{
		return "Chant_SummonHouseplant";
	}

	private final static String localizedName = CMLib.lang().L("Summon Houseplant");

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
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_STONE)
		&&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WOOD))
		{
			mob.tell(L("This is not the place for a houseplant."));
			return false;
		}
		return true;
	}

	public Item buildHouseplant(MOB mob, Room room)
	{
		final Item newItem=CMClass.getItem("GenItem");
		newItem.setMaterial(RawMaterial.RESOURCE_GREENS);
		switch(CMLib.dice().roll(1,7,0))
		{
		case 1:
			newItem.setName(L("a potted rose"));
			newItem.setDisplayText(L("a potted rose is here."));
			newItem.setDescription("");
			break;
		case 2:
			newItem.setName(L("a potted daisy"));
			newItem.setDisplayText(L("a potted daisy is here."));
			newItem.setDescription("");
			break;
		case 3:
			newItem.setName(L("a potted carnation"));
			newItem.setDisplayText(L("a potted white carnation is here"));
			newItem.setDescription("");
			break;
		case 4:
			newItem.setName(L("a potted sunflower"));
			newItem.setDisplayText(L("a potted sunflowers is here."));
			newItem.setDescription(L("Happy flowers have little yellow blooms."));
			break;
		case 5:
			newItem.setName(L("a potted gladiola"));
			newItem.setDisplayText(L("a potted gladiola is here."));
			newItem.setDescription("");
			break;
		case 6:
			newItem.setName(L("a potted fern"));
			newItem.setDisplayText(L("a potted fern is here."));
			newItem.setDescription(L("Like a tiny bush, this dark green plant is lovely."));
			break;
		case 7:
			newItem.setName(L("a potted patch of bluebonnets"));
			newItem.setDisplayText(L("a potted patch of bluebonnets is here."));
			newItem.setDescription(L("Happy flowers with little blue and purple blooms."));
			break;
		}
		newItem.setSecretIdentity(mob.Name());
		newItem.setMiscText(newItem.text());
		Druid_MyPlants.addNewPlant(mob, newItem);
		room.addItem(newItem);
		final Chant_SummonHouseplant newChant=new Chant_SummonHouseplant();
		newItem.basePhyStats().setWeight(1);
		newItem.basePhyStats().setLevel(10+newChant.getX1Level(mob));
		newItem.setExpirationDate(0);
		room.showHappens(CMMsg.MSG_OK_ACTION,CMLib.lang().L("Suddenly, @x1 appears here.",newItem.name()));
		newChant.plantsLocationR=room;
		newChant.littlePlantsI=newItem;
		if(CMLib.law().doesOwnThisProperty(mob,room))
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
		return buildHouseplant(mob,room);
	}
}
