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
public class StdShipShieldGenerator extends StdElecCompItem implements ShipWarComponent
{
	@Override
	public String ID()
	{
		return "StdShipShieldGenerator";
	}
	
	public StdShipShieldGenerator()
	{
		super();
		super.setRechargeRate(0.1f);
		setName("a ship shield generator");
		setDisplayText("a ship shield generator sits here.");
		setDescription("");
	}

	@Override
	public TechType getTechType()
	{
		return Technical.TechType.SHIP_SHIELD;
	}
	
	private ShipDir[]		allPossDirs			= ShipDir.values();
	private int				numPermitDirs		= ShipDir.values().length;
	private int[]			shieldedMsgTypes	= AVAIL_DAMAGE_TYPES;
	private volatile long	lastPowerConsumption= 0;
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
		return (int) Math.min((int) Math.min(powerCapacity,powerSetting) - power, (int)Math.round((double)powerCapacity*getRechargeRate()));
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
		this.shieldedMsgTypes = newTypes;
	}
	
	@Override
	public int[] getDamageMsgTypes()
	{
		return shieldedMsgTypes;
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
		if((msg.target() == ship)
		&&(activated())
		&&(CMParms.contains(this.getDamageMsgTypes(), msg.sourceMinor())))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DAMAGE: // laser, energy, some other kind of directed damage
			{
				if((msg.value() > 0)
				&&(this.lastPowerConsumption>0)
				&&(msg.tool() instanceof SpaceObject))
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
						final ShipDir dir = CMLib.map().getDirectionFromDir(ship.facing(), ship.roll(), directionToMe);
						absorbs = CMParms.contains(getCurrentCoveredDirections(), dir);
					}

					if(absorbs)
					{
						double shieldHurtMultiplier = 1.0;
						// this shield can handle it, do deal out any tech-diff adjustments
						if(msg.tool() instanceof Technical)
						{
							if(this.techLevel() > ((Technical)msg.tool()).techLevel())
							{
								double pct = 1.0 - CMath.div(this.techLevel() - ((Technical)msg.tool()).techLevel(),10.0);
								if(pct <= 0)
								{
									shieldHurtMultiplier = 0.0;
									msg.setValue((int)Math.round(msg.value() * 0.05));
								}
								else
								{
									shieldHurtMultiplier = pct;
									msg.setValue((int)Math.round(msg.value() * pct));
								}
							}
						}
						// next do actual shield-based mitigations
						if(msg.value() > 0)
						{
							double pctShields = CMath.div(lastPowerConsumption,powerCapacity());
							double efficiency = this.getFinalManufacturer().getEfficiencyPct();
							double reliability = this.getFinalManufacturer().getReliabilityPct();
							double wearAndTear = 1.0;
							if(this.subjectToWearAndTear() && this.usesRemaining()<100)
								wearAndTear =CMath.div(this.usesRemaining(), 100);
							int newVal = (int)Math.round(msg.value() - CMath.mul(msg.value(), pctShields * efficiency * wearAndTear));
							int shieldDamage = (int)Math.round(50.0 * shieldHurtMultiplier * (1.0-pctShields) * (1.0-reliability));
							if(shieldDamage > 0)
							{
								CMMsg msg2=(CMMsg)msg.copyOf();
								msg2.setValue(shieldDamage);
								msg2.setTarget(this);
								sendLocalMessage(msg2);
							}
							msg.setValue(newVal);
						}
					}
				}
				break;
			}
			}
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
			case CMMsg.TYP_DAMAGE:
			{
				if(this.subjectToWearAndTear() && (this.usesRemaining()<100) && (msg.value()>0))
				{
					int shieldDamage = msg.value();
					if(shieldDamage > usesRemaining())
					{
						setUsesRemaining(0);
						CMMsg msg2=CMClass.getMsg(msg.source(), this, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, "", CMMsg.NO_EFFECT,null);
						this.sendLocalMessage(msg2);
						final String code=Technical.TechCommand.COMPONENTFAILURE.makeCommand(TechType.SHIP_SHIELD, "Failure: "+me.name()+": shield_failure.");
						sendComputerMessage(this,circuitKey,msg.source(),null,code);
					}
					else
						setUsesRemaining(usesRemaining()-shieldDamage);
				}
				break;
			}
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
						if(command == TechCommand.SHIELDSET)
						{
							ShipDir centerDir = (ShipDir)parms[0];
							int centralIndex = CMParms.indexOf(this.getPermittedDirections(),centerDir);
							if(centralIndex < 0)
								reportError(this, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control port failure.",me.name(mob)));
							else
							{
								int numDirs = ((Integer)parms[1]).intValue();
								if(numDirs > this.getPermittedNumDirections())
									numDirs = this.getPermittedNumDirections();
								if(numDirs >= this.getPermittedDirections().length)
									currCoverage = getPermittedDirections();
								else
								{
									final List<ShipDir> permittedDirs = new XVector<ShipDir>(this.getPermittedDirections());
									permittedDirs.addAll(Arrays.asList(this.getPermittedDirections()));
									permittedDirs.addAll(Arrays.asList(this.getPermittedDirections()));
									centralIndex += this.getPermittedDirections().length;
									final List<ShipDir> theDirs = new ArrayList<ShipDir>(numDirs);
									int offset = 0;
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
							
						}
						else
							reportError(this, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
					}
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
		if(!(E instanceof StdShipShieldGenerator))
			return false;
		return super.sameAs(E);
	}
}
