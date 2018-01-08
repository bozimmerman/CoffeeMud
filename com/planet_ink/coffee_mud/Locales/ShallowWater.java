package com.planet_ink.coffee_mud.Locales;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class ShallowWater extends StdRoom implements Drink
{
	@Override
	public String ID()
	{
		return "ShallowWater";
	}

	protected int liquidType = RawMaterial.RESOURCE_FRESHWATER;
	
	public ShallowWater()
	{
		super();
		name = "the water";
		basePhyStats.setWeight(2);
		recoverPhyStats();
		climask = Places.CLIMASK_WET;
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_WATERSURFACE;
	}

	@Override
	protected int baseThirst()
	{
		return 0;
	}

	@Override
	public long decayTime()
	{
		return 0;
	}

	@Override
	public void setDecayTime(long time)
	{
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
		{
			if(liquidType()==RawMaterial.RESOURCE_SALTWATER)
			{
				msg.source().tell(L("You don't want to be drinking saltwater."));
				return false;
			}
			return true;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		CMLib.commands().handleHygienicMessage(msg, 100, PlayerStats.HYGIENE_WATERCLEAN);

		if(msg.amITarget(this)&&(msg.targetMinor()==CMMsg.TYP_DRINK))
		{
			final MOB mob=msg.source();
			final boolean thirsty=mob.curState().getThirst()<=0;
			final boolean full=!mob.curState().adjThirst(thirstQuenched(),mob.maxState().maxThirst(mob.baseWeight()));
			if(thirsty)
				mob.tell(L("You are no longer thirsty."));
			else
			if(full)
				mob.tell(L("You have drunk all you can."));
		}
	}

	@Override
	public int thirstQuenched()
	{
		return 500;
	}

	@Override
	public int liquidHeld()
	{
		return Integer.MAX_VALUE - 1000;
	}

	@Override
	public int liquidRemaining()
	{
		return Integer.MAX_VALUE - 1000;
	}

	@Override
	public int liquidType()
	{
		return liquidType;
	}

	@Override
	public void setLiquidType(int newLiquidType)
	{
		liquidType = newLiquidType;
	}

	@Override
	public void setThirstQuenched(int amount)
	{
	}

	@Override
	public void setLiquidHeld(int amount)
	{
	}

	@Override
	public void setLiquidRemaining(int amount)
	{
	}

	@Override
	public boolean disappearsAfterDrinking()
	{
		return false;
	}

	@Override
	public boolean containsDrink()
	{
		return true;
	}

	@Override
	public int amountTakenToFillMe(Drink theSource)
	{
		return 0;
	}

	@Override
	public List<Integer> resourceChoices()
	{
		return UnderWater.roomResources;
	}
}
