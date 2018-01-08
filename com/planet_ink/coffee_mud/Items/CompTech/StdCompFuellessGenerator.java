package com.planet_ink.coffee_mud.Items.CompTech;
import com.planet_ink.coffee_mud.Items.Basic.StdContainer;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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
public class StdCompFuellessGenerator extends StdElecCompItem implements PowerGenerator
{
	@Override
	public String ID()
	{
		return "StdCompFuellessGenerator";
	}

	public StdCompFuellessGenerator()
	{
		super();
		setName("a generator");
		setDisplayText("a generator sits here.");
		setDescription("If you put the right place, under the right circumstances, I'll bet it makes power.");

		material=RawMaterial.RESOURCE_STEEL;
		setPowerCapacity(1000);
		setPowerRemaining(0);
		baseGoldValue=0;
		basePhyStats.setWeight(500);
		recoverPhyStats();
	}

	protected int   generatedAmtPerTick = 1;

	protected int getAdjustedGeneratedAmountPerTick()
	{
		return generatedAmtPerTick;
	}

	@Override
	public int getGeneratedAmountPerTick()
	{
		return generatedAmtPerTick;
	}

	@Override
	public void setGeneratedAmountPerTick(int amt)
	{
		generatedAmtPerTick=amt;
	}

	@Override
	public TechType getTechType()
	{
		return TechType.SHIP_GENERATOR;
	}

	protected boolean canGenerateRightNow()
	{
		return true;
	}
	
	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
				break;
			case CMMsg.TYP_INSTALL:
				break;
			case CMMsg.TYP_ACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, L("<S-NAME> power(s) up <T-NAME>."));
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					msg.source().location().show(msg.source(), this, CMMsg.MSG_OK_VISUAL, L("<S-NAME> shut(s) down <T-NAME>."));
				this.activate(false);
				break;
			case CMMsg.TYP_LOOK:
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
					msg.source().tell(L("@x1 is currently @x2",name(),(activated()?"delivering power.\n\r":"deactivated/shut down.\n\r")));
				return;
			case CMMsg.TYP_POWERCURRENT:
				if((msg.value()==0) && (activated()))
				{
					if((((powerCapacity() - powerRemaining()) >= getGeneratedAmountPerTick())
						||(powerRemaining() < getGeneratedAmountPerTick()))
					&&(this.canGenerateRightNow()))
					{
						double generatedAmount = getAdjustedGeneratedAmountPerTick();
						generatedAmount *= this.getComputedEfficiency() * this.getFinalManufacturer().getEfficiencyPct();
						long newAmount=powerRemaining() + Math.round(generatedAmount);
						if(newAmount > powerCapacity())
							newAmount=powerCapacity();
						setPowerRemaining(newAmount);
					}
				}
				break;
			}
		}
	}
	
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdCompFuellessGenerator))
			return false;
		return super.sameAs(E);
	}

}
