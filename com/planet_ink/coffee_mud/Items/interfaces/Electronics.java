package com.planet_ink.coffee_mud.Items.interfaces;
import java.util.List;

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
import com.planet_ink.coffee_mud.Items.interfaces.ShipComponent.ShipEngine;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public interface Electronics extends Item, Technical
{
	public long powerCapacity();
	public void setPowerCapacity(long capacity);

	public long powerRemaining();
	public void setPowerRemaining(long remaining);

	public int powerNeeds();

	public boolean activated();
	public void activate(boolean truefalse);

	public String getManufacturerName();
	public void setManufacturerName(String name);
	public Manufacturer getFinalManufacturer();

	public interface PowerSource extends Electronics
	{
	}

	public interface FuelConsumer extends Electronics
	{
		public int[] getConsumedFuelTypes();
		public void setConsumedFuelType(int[] resources);
		public int getTicksPerFuelConsume();
		public void getTicksPerFuelConsume(int tick);
		public int getFuelRemaining();
		public boolean consumeFuel(int amount);
		public int getTotalFuelCapacity();
	}

	public interface PowerGenerator extends PowerSource, FuelConsumer
	{
		public int getGeneratedAmountPerTick();
		public void setGenerationAmountPerTick(int amt);
	}

	public interface ElecPanel extends Electronics, Container
	{
		public static final TechType[] PANELTYPES={
			TechType.ANY,TechType.SHIP_WEAPON,TechType.SHIP_SHIELD,TechType.SHIP_ENGINE,TechType.SHIP_SENSOR,
			TechType.SHIP_POWER,TechType.SHIP_COMPUTER,TechType.SHIP_SOFTWARE,
			TechType.SHIP_ENVIRO_CONTROL,TechType.SHIP_GENERATOR,TechType.SHIP_DAMPENER,
			TechType.SHIP_TRACTOR
		};
		public TechType panelType();
		public void setPanelType(TechType type);
	}

	public interface Computer extends Electronics.ElecPanel
	{
		public List<Software> getSoftware();
		public List<MOB> getCurrentReaders();
		public void forceReadersMenu();
		public void forceReadersSeeNew();
		public void setActiveMenu(String internalName);
		public String getActiveMenu();
	}

}
