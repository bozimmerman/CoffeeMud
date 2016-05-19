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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
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
public class StdShipWeapon extends StdElecCompItem implements ShipWarComponent
{
	@Override
	public String ID()
	{
		return "StdShipWeapon";
	}

	@Override
	public TechType getTechType()
	{
		return Technical.TechType.SHIP_WEAPON;
	}
	
	private ShipDir[]		allPossDirs			= ShipDir.values();
	private int				numPermitDirs		= ShipDir.values().length;
	private int[]			damageMsgTypes		= AVAIL_DAMAGE_TYPES;
	private volatile long	powerSetting		= Integer.MAX_VALUE;

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
		return (int) Math.min((int) Math.min(powerCapacity,powerSetting) - power, getRechargeRate());
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
	
	@Override
	public void setPermittedDirections(ShipDir[] newPossDirs)
	{
		this.allPossDirs = newPossDirs;
	}
	
	@Override
	public ShipDir[] getPermittedDirections()
	{
		return allPossDirs;
	}

	@Override
	public void setPermittedNumDirections(int numDirs)
	{
		this.numPermitDirs = numDirs;
	}
	
	@Override
	public int getPermittedNumDirections()
	{
		return numPermitDirs;
	}
	
	@Override
	public void setDamageMsgTypes(int[] newTypes)
	{
		this.damageMsgTypes = newTypes;
	}
	
	@Override
	public int[] getDamageMsgTypes()
	{
		return damageMsgTypes;
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
				final List<ShipDir> permittedDirs = new XVector<ShipDir>(permitted);
				permittedDirs.addAll(Arrays.asList(permitted));
				permittedDirs.addAll(Arrays.asList(permitted));
				while(theDirs.size() < numDirs)
				{
					if(!theDirs.contains(permittedDirs.get(centralIndex+offset)))
						theDirs.add(permittedDirs.get(centralIndex+offset));
					if(!theDirs.contains(permittedDirs.get(centralIndex-offset)))
						theDirs.add(permittedDirs.get(centralIndex-offset));
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
		if((msg.target() == ship) && (activated()))
		{
		}
		return true;
	}

	protected static void sendComputerMessage(final ShipWarComponent me, final String circuitKey, final MOB mob, final Item controlI, final String code)
	{
		for(final Iterator<Computer> c=CMLib.tech().getComputers(circuitKey);c.hasNext();)
		{
			final Computer C=c.next();
			if((controlI==null)||(C!=controlI.owner()))
			{
				final CMMsg msg2=CMClass.getMsg(mob, C, me, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
				if(C.okMessage(mob, msg2))
					C.executeMsg(mob, msg2);
			}
		}
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
				final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
				final MOB mob=msg.source();
				if(msg.targetMessage()==null)
				{
					powerSetting = powerCapacity();
				}
				else
				{
					final String[] parts=msg.targetMessage().split(" ");
					final TechCommand command=TechCommand.findCommand(parts);
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
						if(command == TechCommand.WEAPONSET)
						{
							ShipDir centerDir = (ShipDir)parms[0];
							int centralIndex = CMParms.indexOf(this.getPermittedDirections(),centerDir);
							if(centralIndex < 0)
								reportError(this, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control port failure.",me.name(mob)));
							else
							{
								//TODO:
							}
						}
						else
							reportError(this, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
					}
				}
				break;
			}
			case CMMsg.TYP_POWERCURRENT:
				break;
			case CMMsg.TYP_DEACTIVATE:
				this.activate(false);
				this.power = 0;
				break;
			}
		}
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdShipWeapon))
			return false;
		return super.sameAs(E);
	}
}
