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
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
   Copyright 2011-2023 Bo Zimmerman

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
		SHIP_GRAVGEN("Ship Gravity Generator", "Grav. Gen"),
		SHIP_TRACTOR("Ship Tractor", "Tractor"),
		SHIP_REPLICATOR("Ship Food Replicator", "Replicat."),
		SHIP_LAUNCHER("Ship Launcher", "Launcher"),
		COMP_TORPEDO("Launchable Torpedo", "Torpedo"),
		COMP_PROBE("Launchable Probe", "Probe"),
		;
		private final String	friendlyName;
		private final String	shortFriendlyName;

		private TechType(final String name, final String shorter)
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
	 * Gets the Manufacturer ID/Name that made this electrical
	 * item.  This is important because benefits and detriments
	 * can come along with the manufacturer.
	 * @see Technical#setManufacturerName(String)
	 * @see Manufacturer
	 * @return the Manufacturer ID/Name that made this
	 */
	public String getManufacturerName();

	/**
	 * Sets the Manufacturer ID/Name that made this electrical
	 * item.  This is important because benefits and detriments
	 * can come along with the manufacturer.
	 * @see Technical#getManufacturerName()
	 * @see Technical#getFinalManufacturer()
	 * @see Manufacturer
	 * @param name the Manufacturer ID/Name that made this
	 */
	public void setManufacturerName(String name);

	/**
	 * Returns the Manufacturer object of the manufacturer that
	 * made this electrical item.  This is important because
	 * benefits and detriments can come along with the manufacturer.
	 * @see Technical#getManufacturerName()
	 * @see Technical#setManufacturerName(String)
	 * @see Manufacturer
	 * @return the Manufacturer that made this electrical item
	 */
	public Manufacturer getFinalManufacturer();

	/**
	 * A TechCommand is an internal message that is only understood between electrical
	 * objects, typically ship components, but potentially between computer components
	 * of all sorts.
	 * @author Bo Zimmerman
	 *
	 */
	public static enum TechCommand
	{
		THRUST(ShipDirectional.ShipDir.class, Double.class),
		ACCELERATED(ShipDirectional.ShipDir.class, Double.class),
		ACCELERATION(ShipDirectional.ShipDir.class, Double.class, Boolean.class),
		COMPONENTFAILURE(Technical.TechType.class, String[].class),
		SENSE(TechComponent.class, Boolean.class),
		AIRREFRESH(Double.class, Integer.class),
		POWERSET(Long.class),
		DIRSET(ShipDirectional.ShipDir.class),
		AIMSET(Double.class,Double.class),
		TARGETSET(Long.class,Long.class,Long.class),
		FIRE(),
		SHIELDSET(ShipDir.class,Integer.class),
		GRAVITYCHANGE(Boolean.class),
		SWSVCALLOW(Software.SWServices.class),
		SWSVCNEED(Software.SWServices.class, String[].class),
		SWSVCREQ(Software.SWServices.class, String[].class),
		SWSVCRES(Software.SWServices.class, String[].class)
		;
		private final Class<?>[]	parms;

		private TechCommand(final Class<?>... parms)
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
		public String makeCommand(final Object... parts)
		{
			if ((parts == null) || (parts.length != parms.length))
				return "";
			final List<String> fparts=new ArrayList<String>(parts.length);
			fparts.add(name());
			for (int i = 0; i < parms.length; i++)
			{
				if (parts[i] == null)
					return "";
				else
				if (parms[i] == String[].class)
				{
					if(parts[i].getClass() == String[].class)
					{
						final List<String> lst = Arrays.asList((String[])parts[i]);
						fparts.add(CMParms.combineQuoted(lst,0));
					}
					else
					{
						final StringBuilder str=new StringBuilder("");
						for (; i < parms.length; i++)
							str.append(" ").append(parts[i].toString());
						fparts.add(str.toString().trim());
					}
					break;
				}
				else
				if (!parms[i].isAssignableFrom(parts[i].getClass()))
					return "";
				else
				if(parts[i] instanceof Double)
				{
					final DecimalFormat df = new DecimalFormat("#");
					df.setMaximumFractionDigits(8);
					fparts.add(df.format(((Double)parts[i]).doubleValue()));
				}
				else
					fparts.add(parts[i].toString());
			}
			return CMParms.combineQuoted(fparts,0);
		}

		/**
		 * When a tech command of this enum type is received with its parameters,
		 * the parameters are parsed passed to this method to parse into a list and
		 * confirm their types, translate them to the appropriate types, and
		 * return the parameters as their original objects in an Object array.
		 * null is returned if anything goes wrong
		 * @param partStr the command parameters as a string to parse
		 * @return the command parameters as their original objects, or null
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Object[] confirmAndTranslate(final String partStr)
		{
			final List<String> parts=CMParms.parse(partStr);
			if (parts.size() != parms.length + 1)
				return null;
			final Object[] resp = new Object[parts.size() - 1];
			for (int i = 0; i < parms.length; i++)
			{
				if (parms[i].isEnum())
				{
					resp[i] = CMath.s_valueOf((Class<? extends Enum>) parms[i], parts.get(i + 1));
					if (resp[i] == null)
						return null;
				}
				else
				if (Integer.class.isAssignableFrom(parms[i]) || Long.class.isAssignableFrom(parms[i]))
				{
					if (!CMath.isLong(parts.get(i + 1)))
						return null;
					if (Integer.class.isAssignableFrom(parms[i]))
						resp[i] = Integer.valueOf(parts.get(i + 1));
					else
						resp[i] = Long.valueOf(parts.get(i + 1));
				}
				else
				if (Double.class.isAssignableFrom(parms[i]) || Float.class.isAssignableFrom(parms[i]))
				{
					if (!CMath.isNumber(parts.get(i + 1)))
						return null;
					if (Float.class.isAssignableFrom(parms[i]))
						resp[i] = Float.valueOf(parts.get(i + 1));
					else
						resp[i] = Double.valueOf(parts.get(i + 1));
				}
				else
				if (Boolean.class.isAssignableFrom(parms[i]))
				{
					if (!CMath.isBool(parts.get(i + 1)))
						return null;
					resp[i] = Boolean.valueOf(parts.get(i + 1));
				}
				else
				if (String.class.isAssignableFrom(parms[i]))
				{
					resp[i] = parts.get(i + 1);
				}
				else
				if (String[].class.isAssignableFrom(parms[i]))
				{
					final List<String> reParsed=CMParms.parse(parts.get(i+1));
					resp[i] = reParsed.toArray(new String[0]);
					return resp;
				}
			}
			return resp;
		}

		/**
		 * Returns the techcommand object that matches the first word in this
		 * parsed command string.  Only the first entry matters.
		 * @param parts the entire command string list
		 * @return the techcommand that matches the first string, or null
		 */
		public static TechCommand findCommand(final String parts)
		{
			if (parts.length() == 0)
				return null;
			final int x=parts.indexOf(' ');
			final String cmd = (x>0)?parts.substring(0,x):parts;
			return (TechCommand) CMath.s_valueOf(TechCommand.class, cmd.toUpperCase().trim());
		}
	}
}
