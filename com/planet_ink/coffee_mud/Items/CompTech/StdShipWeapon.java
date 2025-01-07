package com.planet_ink.coffee_mud.Items.CompTech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Thief.Thief_Superstition;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.BasicTech.StdElecItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
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
   Copyright 2016-2024 Bo Zimmerman

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
	public StdShipWeapon()
	{
		super();
		this.maxRechargePer = 0.2f;
		basePhyStats.setDamage(100);
	}

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

	private ShipDir[]		allPossDirs			= new ShipDir[] { ShipDir.FORWARD };
	private int				numPermitDirs		= 1;
	private int[]			damageMsgTypes		= new int[] { CMMsg.TYP_ELECTRIC };
	private volatile long	powerSetting		= Integer.MAX_VALUE;
	private final Dir3D	targetDirection			= new Dir3D();

	private volatile ShipDir[]  		  currCoverage = null;
	private volatile Reference<SpaceShip> myShip 	   = null;

	@Override
	public void setOwner(final ItemPossessor container)
	{
		super.setOwner(container);
		myShip = null;
	}

	@Override
	public int powerNeeds()
	{
		return (int) Math.min((int) Math.min(powerCapacity(),powerTarget()) - power, (int)Math.round((double)powerCapacity*getRechargeRate()*this.getComputedEfficiency()));
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
	public long powerTarget()
	{
		if(powerSetting<0)
			return 0;
		return powerSetting>powerCapacity()?powerCapacity():powerSetting;
	}

	@Override
	public void setPowerTarget(final long capacity)
	{
		powerSetting = capacity;
	}

	@Override
	public void setPermittedDirections(final ShipDir[] newPossDirs)
	{
		this.allPossDirs = newPossDirs;
	}

	@Override
	public ShipDir[] getPermittedDirections()
	{
		return allPossDirs;
	}

	@Override
	public void setPermittedNumDirections(final int numDirs)
	{
		this.numPermitDirs = numDirs;
	}

	@Override
	public int getPermittedNumDirections()
	{
		return numPermitDirs;
	}

	@Override
	public void setDamageMsgTypes(final int[] newTypes)
	{
		this.damageMsgTypes = newTypes;
	}

	@Override
	public int[] getDamageMsgTypes()
	{
		return damageMsgTypes;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host, msg))
			return false;
		return true;
	}

	protected static void sendComputerMessage(final Technical me, final String circuitKey, final MOB mob, final Item controlI, final String code)
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

	protected ShipDir[] getCurrentBattleCoveredDirections()
	{
		if(this.currCoverage == null)
			this.currCoverage = CMLib.space().getCurrentBattleCoveredDirections(this);
		return this.currCoverage;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		return true;
	}

	protected int getBleedAmount()
	{
		final double bleedAmt = CMath.mul(powerCapacity, 0.1-(getComputedEfficiency()*.1));
		return (int)Math.round(bleedAmt);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			default:
				super.executeMsg(myHost, msg);
				break;
			case CMMsg.TYP_ACTIVATE:
			{
				super.executeMsg(myHost, msg);
				final LanguageLibrary lang=CMLib.lang();
				final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
				final MOB mob=msg.source();
				if(msg.targetMessage()==null)
					powerSetting = powerCapacity();
				else
				{
					final TechCommand command=TechCommand.findCommand(msg.targetMessage());
					if(command==null)
						reportError(this, controlI, mob, lang.L("@x1 does not respond.",me.name(mob)), lang.L("Failure: @x1: control failure.",me.name(mob)));
					else
					{
						final Object[] parms=command.confirmAndTranslate(msg.targetMessage());
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
						if(command == TechCommand.AIMSET)
						{
							final SpaceObject ship = CMLib.space().getSpaceObject(this, true);
							if(ship == null)
								reportError(this, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control syntax failure.",me.name(mob)));
							else
							{
								if(ship instanceof SpaceShip)
								{
									final Dir3D proposedDirection=new Dir3D(new double[] {((Double)parms[0]).doubleValue(),((Double)parms[1]).doubleValue()});
									final ShipDir dir = CMLib.space().getDirectionFromDir(((SpaceShip)ship).facing(), ((SpaceShip)ship).roll(), proposedDirection);
									if(!CMParms.contains(getCurrentBattleCoveredDirections(), dir))
										reportError(this, controlI, mob, null, lang.L("Failure: @x1: weapon is not facing correctly for that target direction.",me.name(mob)));
									else
									{
										targetDirection.xy(((Double)parms[0]).doubleValue());
										targetDirection.z(((Double)parms[1]).doubleValue());
									}
								}
								else
								{
									targetDirection.xy(((Double)parms[0]).doubleValue());
									targetDirection.z(((Double)parms[1]).doubleValue());
								}
							}
						}
						else
						if(command == TechCommand.FIRE)
						{
							final SpaceObject ship = CMLib.space().getSpaceObject(this, true);
							if(ship == null)
								reportError(this, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control syntax failure.",me.name(mob)));
							else
							if(this.power < Math.min(powerCapacity(),powerTarget()) - getBleedAmount())
								reportError(this, controlI, mob, lang.L("@x1 is not charged up.",me.name(mob)), lang.L("Failure: @x1: weapon is not charged.",me.name(mob)));
							else
							{
								if(ship instanceof SpaceShip)
								{
									final ShipDir dir = CMLib.space().getDirectionFromDir(((SpaceShip)ship).facing(), ((SpaceShip)ship).roll(), targetDirection);
									if(!CMParms.contains(getCurrentBattleCoveredDirections(), dir))
									{
										reportError(this, controlI, mob, null, lang.L("Failure: @x1: weapon is not targeted correctly for its field of fire.",me.name(mob)));
										return;
									}
								}
								final SpaceObject weaponO=(SpaceObject)CMClass.getTech("StdSpaceTechWeapon");
								int damageMsgType = CMMsg.TYP_ELECTRIC;
								if(getDamageMsgTypes().length>0)
									damageMsgType = getDamageMsgTypes()[CMLib.dice().roll(1, getDamageMsgTypes().length, -1)];
								final Integer weaponDamageType = Weapon.MSG_TYPE_MAP.get(Integer.valueOf(damageMsgType));
								if((weaponDamageType != null)&&(weaponDamageType.intValue()>=0))
									((Weapon)weaponO).setWeaponDamageType(weaponDamageType.intValue());
								else
									((Weapon)weaponO).setWeaponDamageType(Weapon.TYPE_BASHING);
								((Weapon)weaponO).setWeaponClassification(Weapon.CLASS_RANGED);
								switch(damageMsgType)
								{
								case CMMsg.TYP_COLLISION:
									weaponO.setName(L("a metal slug"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_STEEL);
									break;
								case CMMsg.TYP_ELECTRIC:
									weaponO.setName(L("an energy beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_ENERGY);
									break;
								case CMMsg.TYP_ACID:
									weaponO.setName(L("a disruptor beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_CHLORINE);
									break;
								case CMMsg.TYP_COLD:
									weaponO.setName(L("a distintegration beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_FRESHWATER);
									break;
								case CMMsg.TYP_FIRE:
									weaponO.setName(L("a photonic beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_ENERGY);
									break;
								case CMMsg.TYP_GAS:
									weaponO.setName(L("a particle beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_AIR);
									break;
								case CMMsg.TYP_LASER:
									weaponO.setName(L("a laser beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_ENERGY);
									break;
								case CMMsg.TYP_PARALYZE:
									weaponO.setName(L("a fusion beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_ENERGY);
									break;
								case CMMsg.TYP_POISON:
									weaponO.setName(L("a magnetic beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_ENERGY);
									break;
								case CMMsg.TYP_SONIC:
									weaponO.setName(L("a tight radio beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_ENERGY);
									break;
								case CMMsg.TYP_UNDEAD:
									weaponO.setName(L("an anti-matter beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_ANTIMATTER);
									break;
								case CMMsg.TYP_WATER:
									weaponO.setName(L("a graviton beam"));
									((Technical)weaponO).setMaterial(RawMaterial.RESOURCE_ENERGY);
									break;
								}
								weaponO.setKnownSource(ship);
								final int weaponRadius = 10;
								final int accellerationOfShipInSameDirectionAsWeapon = 4;
								final Coord3D firstCoords = CMLib.space().moveSpaceObject(ship.coordinates(), targetDirection,
										(int)Math.round(ship.radius()+weaponRadius+ship.speed()+accellerationOfShipInSameDirectionAsWeapon));
								//TODO: adjust targeting based on tech, efficiency, installed, etc, etc.
								weaponO.setCoords(firstCoords);
								weaponO.setRadius(weaponRadius);
								weaponO.setDirection(targetDirection);
								weaponO.setSpeed(SpaceObject.VELOCITY_LIGHT);
								((Technical)weaponO).setTechLevel(techLevel());
								((Technical)weaponO).basePhyStats().setWeight(0);
								((Technical)weaponO).phyStats().setWeight(0);
								((Technical)weaponO).basePhyStats().setDamage((int)Math.round(CMath.mul(phyStats().damage(),super.getComputedEfficiency())));
								((Technical)weaponO).phyStats().setDamage((int)Math.round(CMath.mul(phyStats().damage(),super.getComputedEfficiency())));
								CMLib.threads().startTickDown(weaponO, Tickable.TICKID_BALLISTICK, 10);
								CMLib.space().addObjectToSpace(weaponO, firstCoords);
								setPowerRemaining(0);
							}
						}
						else
							reportError(this, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
					}
				}
				break;
			}
			case CMMsg.TYP_POWERCURRENT:
				if(powerRemaining() > 0)
					setPowerRemaining(powerRemaining()-Math.min(getBleedAmount(),1));
				super.executeMsg(myHost, msg);
				break;
			case CMMsg.TYP_DEACTIVATE:
				super.executeMsg(myHost, msg);
				this.activate(false);
				this.power = 0;
				break;
			}
		}
		else
			super.executeMsg(myHost, msg);
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof StdShipWeapon))
			return false;
		return super.sameAs(E);
	}
}
