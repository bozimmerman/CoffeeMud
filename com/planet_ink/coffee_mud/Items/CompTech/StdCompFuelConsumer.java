package com.planet_ink.coffee_mud.Items.CompTech;
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
public class StdCompFuelConsumer extends StdElecCompContainer implements FuelConsumer
{
	@Override
	public String ID()
	{
		return "StdCompFuelConsumer";
	}

	protected int[] generatedFuelTypes;
	protected int   ticksPerFuelConsume = 10;
	protected volatile int fuelTickDown	= 0;

	public StdCompFuelConsumer()
	{
		super();
		setName("a fuel consuming engine");
		basePhyStats.setWeight(5000);
		setDisplayText("a fuel consuming engine sits here.");
		setDescription("");
		baseGoldValue=5000;
		basePhyStats().setLevel(1);
		basePhyStats.setWeight(500);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		setConsumedFuelType(new int[]{RawMaterial.RESOURCE_DEUTERIUM});
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdCompFuelConsumer))
			return false;
		return super.sameAs(E);
	}

	protected boolean willConsumeFuelIdle()
	{
		return true;
	}

	@Override
	public long containTypes()
	{
		return Container.CONTAIN_RAWMATERIALS;
	}

	@Override
	public void setContainTypes(long containTypes)
	{
		containType = CONTAIN_RAWMATERIALS;
	}

	@Override
	public int getTicksPerFuelConsume()
	{
		return ticksPerFuelConsume;
	}

	@Override
	public void setTicksPerFuelConsume(int tick)
	{
		ticksPerFuelConsume = tick;
	}

	@Override
	public int[] getConsumedFuelTypes()
	{
		return generatedFuelTypes;
	}

	@Override
	public void setConsumedFuelType(int[] resources)
	{
		generatedFuelTypes = resources;
	}

	@Override
	public int getFuelRemaining()
	{
		int amt=0;
		for(final Item I : getFuel())
		{
			if(I instanceof RawMaterial)
				amt+=I.phyStats().weight();
		}
		return amt;
	}

	@Override
	public int getTotalFuelCapacity()
	{
		return capacity();
	}

	@Override
	public boolean canContain(Item I)
	{
		if(!super.canContain(I))
			return false;
		if(I instanceof RawMaterial)
			return CMParms.contains(this.getConsumedFuelTypes(), ((RawMaterial)I).material());
		return false;
	}

	protected void engineShutdown()
	{
		final MOB deity=CMLib.map().deity();
		final CMMsg msg=CMClass.getMsg(CMLib.map().deity(), this,null,CMMsg.MSG_DEACTIVATE, L("<T-NAME> sputters and shuts itself down."));
		final Room R=CMLib.map().roomLocation(this);
		if((R!=null)&&(R.okMessage(deity, msg)))
			R.send(deity, msg);
	}

	protected volatile List<Item> fuelCache=null;

	protected synchronized List<Item> getFuel()
	{
		if(fuelCache==null)
		{
			fuelCache=getContents();
		}
		return fuelCache;
	}

	protected synchronized void clearFuelCache()
	{
		fuelCache=null;
	}

	@Override
	public boolean consumeFuel(int amount)
	{
		final List<Item> fuel=getFuel();
		boolean didSomething =false;
		for(final Item I : fuel)
		{
			if((I instanceof RawMaterial)
			&&(!I.amDestroyed())
			&&CMParms.contains(this.getConsumedFuelTypes(), ((RawMaterial)I).material()))
			{
				amount-=CMLib.materials().destroyResourcesAmt(fuel, amount, ((RawMaterial)I).material(),this);
				if(!I.amDestroyed()) // why is this necessary
					I.recoverPhyStats();
				didSomething=true;
				if(amount<=0)
					break;
			}
		}
		if(amount>0)
			engineShutdown();
		if(didSomething)
			clearFuelCache();
		return amount<=0;
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
				clearFuelCache();
				break;
			case CMMsg.TYP_INSTALL:
				clearFuelCache();
				break;
			case CMMsg.TYP_ACTIVATE:
				clearFuelCache();
				this.activate(true);
				break;
			case CMMsg.TYP_DEACTIVATE:
				clearFuelCache();
				this.activate(false);
				break;
			case CMMsg.TYP_LOOK:
				return;
			case CMMsg.TYP_POWERCURRENT:
				if(msg.value()==0)
				{
					fuelTickDown--;
				}
				if(activated() && (willConsumeFuelIdle()))
				{
					if(fuelTickDown <= 0)
					{
						fuelTickDown=getTicksPerFuelConsume();
						consumeFuel(1);
					}
				}
				break;
			}
		}
	}
}
