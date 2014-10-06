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
import com.planet_ink.coffee_mud.Items.interfaces.ShipComponent.ShipEngine;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2012-2014 Bo Zimmerman

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
 * This is the base class for all tech items
 */
public interface Technical
{

	/**
	 * Standard technical item types
	 * @author Bo Zimmerman
	 */
	public static enum TechType
	{
		ANY("Any"), GIZMO("Gizmo"),
		PERSONAL_WEAPON("Personal Weapon"), PERSONAL_SENSOR("Portable Sensor"),
		PERSONAL_SHIELD("Personal Shield"), PERSONAL_SOFTWARE("Micro Software"),
		PERSONAL_TRACTOR("Personal Tractor"), PERSONAL_ATMOSUIT("Personal Atmosphere Suit"),
		SHIP_SPACESHIP("Space Ship"),SHIP_PANEL("Ship Panel"),
		SHIP_WEAPON("Ship Weapon"),SHIP_SHIELD("Ship Shield"),SHIP_ENGINE("Ship Engine"),
		SHIP_SENSOR("Ship Sensor"),SHIP_POWER("Ship Power System"),SHIP_COMPUTER("Ship Computer"),
		SHIP_SOFTWARE("Ship Software"),SHIP_ENVIRO_CONTROL("Ship Environmental System"),
		SHIP_GENERATOR("Ship Power Generator"),SHIP_DAMPENER("Ship Inertial Dampener"),
		SHIP_TRACTOR("Ship Tractor"),SHIP_REPLICATOR("Ship Food Replicator")
		;
		private final String friendlyName;
		private TechType(String name)
		{
			this.friendlyName=name;
		}
		public String getDisplayName()
		{
			return this.friendlyName;
		}
	}

	public int techLevel();
	public void setTechLevel(int level);

	public TechType getTechType();

	public static enum TechCommand
	{
		THRUST(ShipEngine.ThrustPort.class, Integer.class),
		ACCELLLERATION(ShipEngine.ThrustPort.class, Integer.class, Long.class),
		COMPONENTFAILURE(Technical.TechType.class, String[].class),
		AIRREFRESH(Double.class,Integer.class),
		;
		private final Class<?>[] parms;
		private TechCommand(Class<?>... parms )
		{
			this.parms=parms;
		}
		public Class<?>[] getParms() { return parms; }

		public String makeCommand(Object... parts)
		{
			if((parts==null)||(parts.length!=parms.length))
				return "";
			final StringBuilder str=new StringBuilder(toString());
			for(int i=0;i<parms.length;i++)
				if(parts[i]==null)
					return "";
				else
				if(parms[i]==String[].class)
				{
					for(;i<parms.length;i++)
						str.append(" ").append(parts[i].toString());
					break;
				}
				else
				if(!parms[i].isAssignableFrom(parts[i].getClass()))
					 return "";
				else
					str.append(" ").append(parts[i].toString());
			return str.toString();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Object[] confirmAndTranslate(String[] parts)
		{
			if(parts.length!=parms.length+1)
				return null;
			final Object[] resp=new Object[parts.length-1];
			for(int i=0;i<parms.length;i++)
				if(parms[i].isEnum())
				{
					resp[i]=CMath.s_valueOf((Class<? extends Enum>)parms[i], parts[i+1]);
					if(resp[i]==null)
						return null;
				}
				else
				if(Integer.class.isAssignableFrom(parms[i]) || Long.class.isAssignableFrom(parms[i]))
				{
					if(!CMath.isLong(parts[i+1]))
						return null;
					if(Integer.class.isAssignableFrom(parms[i]))
						resp[i]=Integer.valueOf(parts[i+1]);
					else
						resp[i]=Long.valueOf(parts[i+1]);
				}
				else
				if(Double.class.isAssignableFrom(parms[i]) || Float.class.isAssignableFrom(parms[i]))
				{
					if(!CMath.isDouble(parts[i+1]))
						return null;
					if(Float.class.isAssignableFrom(parms[i]))
						resp[i]=Float.valueOf(parts[i+1]);
					else
						resp[i]=Double.valueOf(parts[i+1]);
				}
				else
				if(Boolean.class.isAssignableFrom(parms[i]))
				{
					if(!CMath.isBool(parts[i+1]))
						return null;
					resp[i]=Boolean.valueOf(parts[i+1]);
				}
				else
				if(String.class.isAssignableFrom(parms[i]))
				{
					resp[i]=parts[i+1];
				}
				else
				if(String[].class.isAssignableFrom(parms[i]))
				{
					final StringBuilder rebuilt=new StringBuilder(parts[i+1]);
					for(i=i+2;i<parts.length;i++)
						rebuilt.append(" ").append(parts[i]);
					resp[i]=rebuilt.toString();
					return resp;
				}
			return resp;
		}

		public static TechCommand findCommand(String[] parts)
		{
			if(parts.length==0)
				return null;
			return (TechCommand)CMath.s_valueOf(TechCommand.class, parts[0].toUpperCase().trim());
		}
	}
}
