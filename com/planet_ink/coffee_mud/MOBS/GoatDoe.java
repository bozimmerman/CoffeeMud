package com.planet_ink.coffee_mud.MOBS;
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
   Copyright 2013-2018 Bo Zimmerman

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
public class GoatDoe extends Goat implements Drink
{
	@Override
	public String ID()
	{
		return "GoatDoe";
	}

	protected int liquidType = RawMaterial.RESOURCE_MILK;
	
	public GoatDoe()
	{
		super();
		baseCharStats().setStat(CharStats.STAT_GENDER, 'F');
		setDescription("Looks like a goat doe. Nimble and lively, it has short hair and dangeous looking hooves and horns.");
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
			return true;
		return super.okMessage(myHost,msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
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
		else
		if((msg.tool()==this)
		&&(msg.targetMinor()==CMMsg.TYP_FILL)
		&&(msg.target() instanceof Container)
		&&(((Container)msg.target()).capacity()>0))
		{
			final Container container=(Container)msg.target();
			final Item I=CMClass.getItem("GenLiquidResource");
			I.setName(L("some goat milk"));
			I.setDisplayText(L("some goat milk has been left here."));
			I.setDescription(L("It looks like goat milk"));
			I.setMaterial(RawMaterial.RESOURCE_MILK);
			I.setBaseValue(RawMaterial.CODES.VALUE(RawMaterial.RESOURCE_MILK));
			I.basePhyStats().setWeight(1);
			CMLib.materials().addEffectsToResource(I);
			I.recoverPhyStats();
			I.setContainer(container);
			if(container.owner()!=null)
			{
				if(container.owner() instanceof MOB)
					((MOB)container.owner()).addItem(I);
				else
				if(container.owner() instanceof Room)
					((Room)container.owner()).addItem(I,ItemPossessor.Expire.Resource);
			}
		}
	}

	@Override
	public int thirstQuenched()
	{
		return 100;
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
	public boolean disappearsAfterDrinking()
	{
		return false;
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
	public boolean containsDrink()
	{
		return true;
	}

	@Override
	public int amountTakenToFillMe(Drink theSource)
	{
		return 0;
	}
}
