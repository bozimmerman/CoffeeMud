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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2003-2016 Bo Zimmerman

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
/**
 * Electronics are items that can be turned on and off before their
 * interesting behavior or ability is available, which requires "power"
 * to be operated, and has some capacitance for power that keeps it 
 * running for some dramatic amount of time while it is no longer
 * receiving new power.  Electronics have manufacturers who make them,
 * and can be of many different sorts.
 * @author Bo Zimmerman
 *
 */
public interface Electronics extends Item, Technical
{
	/**
	 * Gets the maximum amount of capacitance supported by
	 * this electrical item.  This is the maximum power the item
	 * can store up and utilize before it shuts off. Only the
	 * item itself knows how much it wants to use at any particular
	 * time.
	 * @see Electronics#setPowerCapacity(long)
	 * @return the maximum amount of stored power capacity
	 */
	public long powerCapacity();
	
	/**
	 * Sets the maximum amount of capacitance supported by
	 * this electrical item.  This is the maximum power the item
	 * can store up and utilize before it shuts off. Only the
	 * item itself knows how much it wants to use at any particular
	 * time.
	 * @see Electronics#powerCapacity()
	 * @param capacity the maximum amount of stored power capacity
	 */
	public void setPowerCapacity(long capacity);

	/**
	 * Gets the amount of power capacitance remaining in this
	 * electrical item.  The item will continue to use this
	 * power until it doesn't have enough to operate, and
	 * then turn off.
	 * @see Electronics#setPowerRemaining(long)
	 * @return the amount of power capacitance remaining
	 */
	public long powerRemaining();

	/**
	 * Sets the amount of power capacitance remaining in this
	 * electrical item.  The item will continue to use this
	 * power until it doesn't have enough to operate, and
	 * then turn off.
	 * @see Electronics#powerRemaining()
	 * @param remaining the amount of power capacitance remaining
	 */
	public void setPowerRemaining(long remaining);

	/**
	 * Returns the immediate power needs of this electrical item.
	 * Typically powerCapacity - powerAvailable
	 * @see Electronics#powerRemaining()
	 * @see Electronics#powerCapacity()
	 * @return the amount of power this item can still absorb
	 */
	public int powerNeeds();

	/**
	 * Gets whether this electrical item is "turned on".
	 * An activated item can do the stuff it is supposed to,
	 * but off it cannot. 
	 * @see Electronics#activate(boolean)
	 * @return whether this electrical item is "turned on"
	 */
	public boolean activated();

	/**
	 * Sets whether this electrical item is "turned on".
	 * An activated item can do the stuff it is supposed to,
	 * but off it cannot. 
	 * @see Electronics#activated()
	 * @param truefalse true to activate, false to deactivate
	 */
	public void activate(boolean truefalse);

	/**
	 * Gets the Manufacturer ID/Name that made this electrical
	 * item.  This is important because benefits and detriments
	 * can come along with the manufacturer.
	 * @see Electronics#setManufacturerName(String)
	 * @see Manufacturer
	 * @return the Manufacturer ID/Name that made this
	 */
	public String getManufacturerName();

	/**
	 * Sets the Manufacturer ID/Name that made this electrical
	 * item.  This is important because benefits and detriments
	 * can come along with the manufacturer.
	 * @see Electronics#getManufacturerName()
	 * @see Electronics#getFinalManufacturer()
	 * @see Manufacturer
	 * @param name the Manufacturer ID/Name that made this
	 */
	public void setManufacturerName(String name);
	
	/**
	 * Returns the Manufacturer object of the manufacturer that
	 * made this electrical item.  This is important because 
	 * benefits and detriments can come along with the manufacturer.
	 * @see Electronics#getManufacturerName()
	 * @see Electronics#setManufacturerName(String)
	 * @see Manufacturer
	 * @return the Manufacturer that made this electrical item
	 */
	public Manufacturer getFinalManufacturer();

	/**
	 * An interface for an Electrical item that produces rather
	 * than consumes power, though it may consume something else
	 * to generate that power.  This includes both generators
	 * and batteries.
	 * @author Bo Zimmerman
	 *
	 */
	public interface PowerSource extends Electronics
	{
	}

	/**
	 * An interface for an Electrical item that also consumes
	 * some other sort of fuel other than electrical power, if
	 * it consumes any electricity at all.  Because they require
	 * fuel, these items are typically containers that can only
	 * hold the type of fuel they need to operate.
	 * Generators are typical of Fuel Consumers.
	 * @author Bo Zimmerman
	 *
	 */
	public interface FuelConsumer extends Electronics, Container
	{
		/**
		 * Gets an array of RawMaterial codes representing the
		 * type of fuel that must be put inside this fuel container
		 * for it to consume it.
		 * @see FuelConsumer#setConsumedFuelType(int[])
		 * @see RawMaterial
		 * @return an array of RawMaterial codes
		 */
		public int[] getConsumedFuelTypes();
		
		/**
		 * Sets an array of RawMaterial codes representing the
		 * type of fuel that must be put inside this fuel container
		 * for it to consume it.
		 * @see FuelConsumer#getConsumedFuelTypes()
		 * @see RawMaterial
		 * @param resources an array of RawMaterial codes
		 */
		public void setConsumedFuelType(int[] resources);
		
		/**
		 * Gets the number of ticks between each consumption of fuel.
		 * This determines the rate of fuel consumption, assuming the
		 * amount of fuel itself is fixed, or determined internally.
		 * This is the only way variation in consumption is controlled
		 * from outside.
		 * @see FuelConsumer#setTicksPerFuelConsume(int)
		 * @return the number of ticks between each consumption of fuel.
		 */
		public int getTicksPerFuelConsume();
		
		/**
		 * Sets the number of ticks between each consumption of fuel.
		 * This determines the rate of fuel consumption, assuming the
		 * amount of fuel itself is fixed, or determined internally.
		 * This is the only way variation in consumption is controlled
		 * from outside.
		 * @see FuelConsumer#getTicksPerFuelConsume()
		 * @param tick the number of ticks between each consumption of fuel.
		 */
		public void setTicksPerFuelConsume(int tick);
		
		/**
		 * Returns the amount of fuel remaining in this container.
		 * @see FuelConsumer#getTotalFuelCapacity()
		 * @return the amount of fuel remaining in this container.
		 */
		public int getFuelRemaining();
		
		/**
		 * Forces this fuel consumer to consumer some amount of its
		 * fuel, without any other effect. If there was not enough
		 * fuel to be consumed, it might result in de-activation.
		 * @param amount the amount of fuel to consume
		 * @return true if there was NOT enough fuel, false if fuel was consumed OK.
		 */
		public boolean consumeFuel(int amount);
		
		/**
		 * Returns the amount of total fuel this container can hold.
		 * @see FuelConsumer#getFuelRemaining()
		 * @return the amount of total fuel this container can hold.
		 */
		public int getTotalFuelCapacity();
	}

	/**
	 * An interface that combines both a PowerSource and and FuelConsumer
	 * to produce a Power Generator, or converter from fuel to electricity.
	 * @see FuelConsumer
	 * @see PowerSource
	 * @author Bo Zimmerman
	 *
	 */
	public interface PowerGenerator extends PowerSource, FuelConsumer
	{
		/**
		 * Gets the amount of power generated every tick.  This is fed into
		 * the Electronics capacitance.
		 * @see Electronics#powerCapacity()
		 * @see Electronics#powerRemaining()
		 * @see PowerGenerator#setGeneratedAmountPerTick(int)
		 * @return the amount of power generated every tick
		 */
		public int getGeneratedAmountPerTick();
		
		/**
		 * Sets the amount of power generated every tick.  This is fed into
		 * the Electronics capacitance.
		 * @see Electronics#powerCapacity()
		 * @see Electronics#powerRemaining()
		 * @see PowerGenerator#getGeneratedAmountPerTick()
		 * @param amt the amount of power generated every tick
		 */
		public void setGeneratedAmountPerTick(int amt);
	}

	/**
	 * An interface for a particular kind of container that is invisible
	 * when closed, holds particular kinds of electrical gear, and allows
	 * some level of manipulation of the items inside.  It also may manage
	 * the power needs of all containing items, as well as a uniform way
	 * of activation.
	 * @author Bo Zimmerman
	 *
	 */
	public interface ElecPanel extends Electronics, Container
	{
		/**
		 * A list of TechType objects denoting what the
		 * valid types of panels there are.  These determine
		 * that kinds of items the panel can hold.
		 * @see com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType
		 */
		public static final TechType[] PANELTYPES=
		{
			TechType.ANY,
			TechType.SHIP_WEAPON,
			TechType.SHIP_SHIELD,
			TechType.SHIP_ENGINE,
			TechType.SHIP_SENSOR,
			TechType.SHIP_POWER,
			TechType.SHIP_COMPUTER,
			TechType.SHIP_SOFTWARE,
			TechType.SHIP_ENVIRO_CONTROL,
			TechType.SHIP_GENERATOR,
			TechType.SHIP_DAMPENER,
			TechType.SHIP_TRACTOR
		};
		
		/**
		 * Gets the type of panel this is, which shows what
		 * sorts of items can be "installed into it.  This method is
		 * a sort of companion to {@link Container#containTypes()}
		 * @see ElecPanel#setPanelType(com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType)
		 * @see com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType
		 * @return the type of panel this is
		 */
		public TechType panelType();
		
		/**
		 * Sets the type of panel this is, which shows what
		 * sorts of items can be "installed into it.  This method is
		 * a sort of companion to {@link Container#setContainTypes(long)}
		 * @see ElecPanel#setPanelType(com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType)
		 * @see com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType
		 * @param type the type of panel this is
		 */
		public void setPanelType(TechType type);
	}

	/**
	 * A computer is a particular type of electronics panel that holds
	 * software, and has readers who monitor the messages this panel
	 * generates, as well as use the Type command to enter data into
	 * this panel's software.
	 * @see Software
	 * @see ElecPanel
	 * @author Bo Zimmerman
	 *
	 */
	public interface Computer extends Electronics.ElecPanel
	{
		/**
		 * Returns the list of Software objects installed in this computer.
		 * @see Software
		 * @return the list of Software objects installed in this computer
		 */
		public List<Software> getSoftware();
		
		/**
		 * Returns the list of mobs currently monitoring the output of this
		 * computers software.
		 * @return the list of mobs currently monitoring the output
		 */
		public List<MOB> getCurrentReaders();
		
		/**
		 * Forces all the current readers to "read" the computer, typically
		 * seeing the menu.
		 * @see Computer#getCurrentReaders()
		 * @see Computer#forceReadersSeeNew()
		 */
		public void forceReadersMenu();
		
		/**
		 * Forces all the current readers to see any new messages that 
		 * should be seen by anyone monitoring the computer.
		 * @see Computer#getCurrentReaders()
		 * @see Computer#forceReadersMenu()
		 */
		public void forceReadersSeeNew();
		
		/**
		 * Most software supports different levels of menu, and some software
		 * is even a sub-menu unto itself.  This method forces the system to
		 * recognize one of those menus as current.  The software takes it
		 * from there.
		 * @see Computer#getActiveMenu()
		 * @param internalName the menu to set as current and active
		 */
		public void setActiveMenu(String internalName);
		
		/**
		 * Most software supports different levels of menu, and some software
		 * is even a sub-menu unto itself.  This method returns the current
		 * active menu.
		 * @see Computer#setActiveMenu(String)
		 * @return internalName the menu to set as current and active
		 */
		public String getActiveMenu();
	}

}
