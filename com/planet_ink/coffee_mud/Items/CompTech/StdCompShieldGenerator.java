package com.planet_ink.coffee_mud.Items.CompTech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.BasicTech.StdElecItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipEngine;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2016-2016 Bo Zimmerman

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
public class StdCompShieldGenerator extends StdElecCompItem implements TechComponent
{
	@Override
	public String ID()
	{
		return "StdElecCompSensor";
	}

	@Override
	public TechType getTechType()
	{
		return Technical.TechType.SHIP_SHIELD;
	}
	
	private volatile long	lastPowerConsumption = 0;
	private volatile long	powerSetting		 = Integer.MAX_VALUE;
	
	private volatile ShipDir[]  		  currCoverage = null;
	private volatile Reference<SpaceShip> myShip 	   = null;

	@Override
	public void setOwner(ItemPossessor container)
	{
		super.setOwner(container);
		myShip = null;
	}

	@Override
	public int powerNeeds()
	{
		return (int) Math.min((int) Math.min(powerCapacity,powerSetting) - power, maxRechargePer);
	}
	
	protected synchronized SpaceShip getMyShip()
	{
		if(myShip == null)
		{
			final Area area = CMLib.map().areaLocation(this);
			if(area instanceof SpaceShip)
				myShip = new WeakReference<SpaceShip>((SpaceShip)area);
			else
				myShip = new WeakReference<SpaceShip>(null);
		}
		return myShip.get();
	}
	
	protected ShipDir[] getPermittedDirections()
	{
		return ShipDir.values();
	}
	
	protected int getPermittedNumDirections()
	{
		return ShipDir.values().length;
	}
	
	protected ShipDir[] getCurrentCoveredDirections()
	{
		if(this.currCoverage == null)
		{
			final ShipDir[] permitted = getPermittedDirections(); 
			int numDirs = getPermittedNumDirections();
			if(numDirs >= permitted.length)
				currCoverage = getPermittedDirections();
			else
			{
				int centralIndex = CMLib.dice().roll(1, numDirs, -1);
				List<ShipDir> theDirs = new ArrayList<ShipDir>(numDirs);
				int offset = 0;
				while(theDirs.size() < numDirs)
				{
					if(!theDirs.contains(permitted[centralIndex+offset]))
						theDirs.add(permitted[centralIndex+offset]);
					if(!theDirs.contains(permitted[centralIndex-offset]))
						theDirs.add(permitted[centralIndex-offset]);
					offset+=1;
				}
				currCoverage = theDirs.toArray(new ShipDir[theDirs.size()]);
			}
		}
		return currCoverage;
	}
	
	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		final SpaceShip ship = getMyShip(); 
		if(msg.target() == ship)
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DAMAGE: // laser, energy, some other kind of directed damage
			{
				if((msg.value() > 0)&&(this.lastPowerConsumption>0)&&(msg.tool() instanceof SpaceObject))
				{
					final SpaceObject weaponO=(SpaceObject)msg.tool();
					// first decide if it came from a direction im handling
					// if that matters
					boolean absorbs = false;
					if((getCurrentCoveredDirections().length >= ShipDir.values().length))
						absorbs = true;
					else
					if(weaponO.knownSource() != null)
					{
						final double[] directionToMe = CMLib.map().getDirection(weaponO.knownSource(), ship);
						final ShipDir dir = CMLib.map().getDirectionFromDir(ship.facing(), directionToMe);
						absorbs = CMParms.contains(getCurrentCoveredDirections(), dir);
					}

					int shieldHurtConsumption = msg.value();
					// this shield can handle it, do deal out any tech-diff adjustments
					if(msg.tool() instanceof Technical)
					{
						if(this.techLevel() > ((Technical)msg.tool()).techLevel())
						{
							double pct = 1.0 - CMath.div(this.techLevel() - ((Technical)msg.tool()).techLevel(),10.0);
							if(pct <= 0)
							{
								shieldHurtConsumption = 0;
								msg.setValue((int)Math.round(msg.value() * 0.05));
							}
							else
							{
								shieldHurtConsumption = (int)Math.round(shieldHurtConsumption * pct);
								msg.setValue((int)Math.round(msg.value() * pct));
							}
						}
					}
					// next do actual shield-based mitigations
					if(absorbs)
					{
						//TODO: finish
					}
					
				}
				break;
			}
			}
		}
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
			case CMMsg.TYP_ACTIVATE:
			{
				final LanguageLibrary lang=CMLib.lang();
				final String[] parts=msg.targetMessage().split(" ");
				final TechCommand command=TechCommand.findCommand(parts);
				final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
				final MOB mob=msg.source();
				if(command==null)
					reportError(this, controlI, mob, lang.L("@x1 does not respond.",me.name(mob)), lang.L("Failure: @x1: control failure.",me.name(mob)));
				else
				{
					final Object[] parms=command.confirmAndTranslate(parts);
					if(parms==null)
						reportError(this, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control syntax failure.",me.name(mob)));
					else
					if(command == TechCommand.POWERSET)
					{
						powerSetting=((Long)parms[0]).intValue();
						if(powerSetting<0)
							powerSetting=0;
						else
						if(powerSetting > powerCapacity())
							powerSetting = powerCapacity();
					}
					else
						reportError(this, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
				}
				break;
			}
			case CMMsg.TYP_POWERCURRENT:
				// shields should constantly consume what they have
				if(activated())
				{
					this.lastPowerConsumption = this.power;
					this.power = 0;
				}
				else
				{
					this.lastPowerConsumption = 0;
					this.power = 0;
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				this.activate(false);
				this.lastPowerConsumption = 0;
				this.power = 0;
				//TODO:what does the ship need to know?
				break;
			}
		}
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdCompShieldGenerator))
			return false;
		return super.sameAs(E);
	}
}
