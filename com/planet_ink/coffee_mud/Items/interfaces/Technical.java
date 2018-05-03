package com.planet_ink.coffee_mud.Items.interfaces;

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
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.List;

/*
   Copyright 2011-2018 Bo Zimmerman

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
 * This is the base class for all tech items.
 */
public interface Technical extends Item
{

	/**
	 * Standard technical item types
	 * 
	 * @author Bo Zimmerman
	 */
	public static enum TechType
	{
		ANY("Any", "Tech"),
		GIZMO("Gizmo", "Gizmo"),
		CONTROL_PANEL("Control Panel","Controls"),
		PERSONAL_WEAPON("Personal Weapon", "Weapon"),
		PERSONAL_SENSOR("Portable Sensor", "Sensor"),
		PERSONAL_SHIELD("Personal Shield", "Shield"),
		PERSONAL_SOFTWARE("Micro Software", "Software"),
		PERSONAL_TRACTOR("Personal Tractor", "Tractor"),
		PERSONAL_ATMOSUIT("Personal Atmosphere Suit", "Atmo. Suit"),
		SHIP_SPACESHIP("Space Ship", "Ship"),
		SHIP_PANEL("Ship Panel", "Panel"),
		SHIP_WEAPON("Ship Weapon", "Weapon"),
		SHIP_SHIELD("Ship Shield", "Shield"),
		SHIP_ENGINE("Ship Engine", "Engine"),
		SHIP_SENSOR("Ship Sensor", "Sensor"),
		SHIP_POWER("Ship Power System", "Power"),
		SHIP_COMPUTER("Ship Computer", "Computer"),
		SHIP_SOFTWARE("Ship Software", "Software"),
		SHIP_ENVIRO_CONTROL("Ship Environmental System", "Env. System"),
		SHIP_GENERATOR("Ship Power Generator", "Generator"),
		SHIP_DAMPENER("Ship Inertial Dampener", "Inertial"),
		SHIP_TRACTOR("Ship Tractor", "Tractor"),
		SHIP_REPLICATOR("Ship Food Replicator", "Replicat.")
		;
		private final String	friendlyName;
		private final String	shortFriendlyName;

		private TechType(String name, String shorter)
		{
			this.friendlyName = name;
			this.shortFriendlyName = shorter;
		}

		/**
		 * The long friendly name of this technical item
		 * @return long friendly name of this technical item
		 */
		public String getDisplayName()
		{
			return this.friendlyName;
		}

		/**
		 * The shorter friendly name of this technical item
		 * @return shorter friendly name of this technical item
		 */
		public String getShortDisplayName()
		{
			return this.shortFriendlyName;
		}
	}

	/**
	 * One of the most important methods in the tech game, this denotes
	 * the level of technology of the specific instance of this item.
	 * Technology progresses ever onward, and the users equipment can
	 * become outdated, and be outperformed by items with higher tech
	 * level.  For that reason, they must constantly be looking to keeping
	 * their gear updates.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TechLibrary#getGlobalTechLevel()
	 * @see Technical#setTechLevel(int)
	 * @return the absolute tech level of this item
	 */
	public int techLevel();

	/**
	 * One of the most important methods in the tech game, this denotes
	 * the level of technology of the specific instance of this item.
	 * Technology progresses ever onward, and the users equipment can
	 * become outdated, and be outperformed by items with higher tech
	 * level.  For that reason, they must constantly be looking to keeping
	 * their gear updates.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.TechLibrary#getGlobalTechLevel()
	 * @see Technical#techLevel()
	 * @param level the absolute tech level of this item
	 */
	public void setTechLevel(int level);

	/**
	 * Returns the tech type enum for this technical object, which describes more
	 * about its functionality, and is in fact a kind of class proxy.  I hope this
	 * doesn't bog things down too much.
	 * @see Technical.TechType
	 * @return the tech type eum
	 */
	public TechType getTechType();

	/**
	 * A TechCommand is an internal message that is only understood between electrical
	 * objects, typically ship components, but potentially between computer components
	 * of all sorts.  
	 * @author Bo Zimmerman
	 *
	 */
	public static enum TechCommand
	{
		THRUST(TechComponent.ShipDir.class, Double.class),
		THRUSTED(TechComponent.ShipDir.class, Double.class),
		ACCELLLERATION(TechComponent.ShipDir.class, Double.class, Boolean.class),
		COMPONENTFAILURE(Technical.TechType.class, String[].class),
		SENSE(),
		AIRREFRESH(Double.class, Integer.class), 
		POWERSET(Long.class),
		WEAPONTARGETSET(Double.class,Double.class),
		WEAPONFIRE(),
		SHIELDSET(ShipDir.class,Integer.class),
		GRAVITYCHANGE(Boolean.class);
		private final Class<?>[]	parms;

		private TechCommand(Class<?>... parms)
		{
			this.parms = parms;
		}

		/**
		 * Returns the form of the parameters of this tech command
		 * @return  the form of the parameters of this tech command
		 */
		public Class<?>[] getParms()
		{
			return parms;
		}

		/**
		 * Creates a new tech command of this enums type using the given parameters,
		 * and returns the message as a string, or "" if an error occurred due to
		 * bad parameters.
		 * @param parts the parameters, which must be perfectly valid for this enum
		 * @return the encoded tech command
		 */
		public String makeCommand(Object... parts)
		{
			if ((parts == null) || (parts.length != parms.length))
				return "";
			final StringBuilder str = new StringBuilder(toString());
			for (int i = 0; i < parms.length; i++)
			{
				if (parts[i] == null)
					return "";
				else 
				if (parms[i] == String[].class)
				{
					for (; i < parms.length; i++)
						str.append(" ").append(parts[i].toString());
					break;
				}
				else 
				if (!parms[i].isAssignableFrom(parts[i].getClass()))
					return "";
				else
					str.append(" ").append(parts[i].toString());
			}
			return str.toString();
		}

		/**
		 * When a tech command of this enum type is received with its parameters,
		 * the parameters are parsed into a list and passed to this method to
		 * confirm their types, translate them to the appropriate types, and
		 * return the parameters as their original objects in an Object array.
		 * null is returned if anything goes wrong
		 * @param parts the command parameters as a string list
		 * @return the command parameters as their original objects, or null
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Object[] confirmAndTranslate(String[] parts)
		{
			if (parts.length != parms.length + 1)
				return null;
			final Object[] resp = new Object[parts.length - 1];
			for (int i = 0; i < parms.length; i++)
			{
				if (parms[i].isEnum())
				{
					resp[i] = CMath.s_valueOf((Class<? extends Enum>) parms[i], parts[i + 1]);
					if (resp[i] == null)
						return null;
				}
				else 
				if (Integer.class.isAssignableFrom(parms[i]) || Long.class.isAssignableFrom(parms[i]))
				{
					if (!CMath.isLong(parts[i + 1]))
						return null;
					if (Integer.class.isAssignableFrom(parms[i]))
						resp[i] = Integer.valueOf(parts[i + 1]);
					else
						resp[i] = Long.valueOf(parts[i + 1]);
				}
				else 
				if (Double.class.isAssignableFrom(parms[i]) || Float.class.isAssignableFrom(parms[i]))
				{
					if (!CMath.isDouble(parts[i + 1]))
						return null;
					if (Float.class.isAssignableFrom(parms[i]))
						resp[i] = Float.valueOf(parts[i + 1]);
					else
						resp[i] = Double.valueOf(parts[i + 1]);
				}
				else 
				if (Boolean.class.isAssignableFrom(parms[i]))
				{
					if (!CMath.isBool(parts[i + 1]))
						return null;
					resp[i] = Boolean.valueOf(parts[i + 1]);
				}
				else 
				if (String.class.isAssignableFrom(parms[i]))
				{
					resp[i] = parts[i + 1];
				}
				else 
				if (String[].class.isAssignableFrom(parms[i]))
				{
					final StringBuilder rebuilt = new StringBuilder(parts[i + 1]);
					for (i = i + 2; i < parts.length; i++)
						rebuilt.append(" ").append(parts[i]);
					resp[i] = rebuilt.toString();
					return resp;
				}
			}
			return resp;
		}

		/**
		 * Returns the techcommand object that matches the first word in this
		 * parsed command string list.  Only the first entry matters.
		 * @param parts the entire command string list
		 * @return the techcommand that matches the first string, or null
		 */
		public static TechCommand findCommand(String[] parts)
		{
			if (parts.length == 0)
				return null;
			return (TechCommand) CMath.s_valueOf(TechCommand.class, parts[0].toUpperCase().trim());
		}
	}
}
