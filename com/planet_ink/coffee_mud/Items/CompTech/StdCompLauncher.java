package com.planet_ink.coffee_mud.Items.CompTech;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
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
   Copyright 2022-2024 Bo Zimmerman

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
public class StdCompLauncher extends StdElecCompContainer implements TechComponent, ElecPanel, ShipDirectional
{
	@Override
	public String ID()
	{
		return "StdCompLauncher";
	}

	private ShipDir[]						allPossDirs		= new ShipDir[] { ShipDir.FORWARD };
	private int								numPermitDirs	= 1;
	protected volatile String				circuitKey		= null;
	private volatile ShipDir[]				currCoverage	= null;
	private volatile Reference<SpaceShip>	myShip			= null;
	private volatile long					powerSetting	= Integer.MAX_VALUE;
	private final Dir3D						targetDirection	= new Dir3D();
	private volatile SpaceObject			targetSet		= null;
	private TechType						techType		= TechType.ANY;

	public StdCompLauncher()
	{
		super();
		setName("a launcher tube");
		setDisplayText("a launcher tube is mounted here");
		setDescription("Probably requires particular things to launcher out of it.");
		super.setDoorsNLocks(true, true, true,false, false,false);
		basePhyStats().setSensesMask(basePhyStats().sensesMask()|PhyStats.SENSE_ITEMNOTGET);
		this.openDelayTicks=0;
		super.containType=Container.CONTAIN_SSCOMPONENTS;
		this.recoverPhyStats();
		super.maxRechargePer=10;
	}

	@Override
	public void setOwner(final ItemPossessor container)
	{
		super.setOwner(container);
		myShip = null;
	}

	@Override
	public TechType getTechType()
	{
		return TechType.SHIP_LAUNCHER;
	}

	protected TechType	panelType	= TechType.ANY;

	@Override
	public long powerTarget()
	{
		return powerSetting > powerCapacity() ? powerCapacity() : powerSetting;
	}

	@Override
	public void setPowerTarget(final long capacity)
	{
		powerSetting = capacity;
	}

	@Override
	public int powerNeeds()
	{
		return (int) Math.min((int) Math.min(powerCapacity(),powerTarget()) - power, (int)Math.round((double)powerCapacity*getRechargeRate()));
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
	public boolean canContain(final Item I)
	{
		if(!super.canContain(I))
			return false;
		if((I instanceof Technical)
		&&((panelType()==((Technical)I).getTechType()))||(panelType()==Technical.TechType.ANY))
			return true;
		return false;
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

	protected static void sendComputerMessage(final Technical me, final String circuitKey, final MOB mob, final Item controlI, final String code)
	{
		for(final Iterator<Computer> c=CMLib.tech().getComputers(circuitKey);c.hasNext();)
		{
			final Computer C=c.next();
			if((controlI==null)
			||(C!=controlI.owner()))
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

	@Override
	public void destroy()
	{
		if((!destroyed)&&(circuitKey!=null))
		{
			CMLib.tech().unregisterElectronics(this,circuitKey);
			circuitKey=null;
		}
		super.destroy();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_PUT:
				break;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_PUT:
				if(msg.tool() instanceof TechComponent)
					((TechComponent)msg.tool()).setInstalledFactor(CMSecurity.isAllowed(msg.source(), msg.source().location(), CMSecurity.SecFlag.CMDITEMS)?1.0f:0.0f);
				break;
			case CMMsg.TYP_INSTALL:
				if((msg.tool() instanceof TechComponent)&&(msg.value()>=0))
				{
					if(msg.value()<=0)
						((TechComponent)msg.tool()).setInstalledFactor((float)1.0);
					else
						((TechComponent)msg.tool()).setInstalledFactor((float)CMath.div(msg.value(), 100.0));
					final CMMsg msg2=(CMMsg)msg.copyOf();
					msg2.setTargetCode(CMMsg.MSG_PUT);
					msg2.setSourceCode(CMMsg.MSG_PUT);
					msg2.setOthersCode(CMMsg.MSG_PUT);
					super.executeMsg(myHost, msg2);
				}
				break;
			case CMMsg.TYP_ACTIVATE:
				super.executeMsg(myHost, msg);
				final LanguageLibrary lang=CMLib.lang();
				final Software controlI=(msg.tool() instanceof Software)?((Software)msg.tool()):null;
				final MOB mob=msg.source();
				if(msg.targetMessage()==null)
				{
					powerSetting = powerCapacity();
					this.activate(true);
				}
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
						if(command == TechCommand.TARGETSET)
						{
							targetSet=null;
							final SpaceObject ship = CMLib.space().getSpaceObject(this, true);
							if(ship == null)
							{
								reportError(this, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control syntax failure.",me.name(mob)));
								return;
							}
							final Coord3D proposedLoc=
									new Coord3D(new long[] {((Long)parms[0]).longValue(), ((Long)parms[1]).longValue(), ((Long)parms[2]).longValue()});
							final List<SpaceObject> nearbies = CMLib.space().getSpaceObjectsByCenterpointWithin(proposedLoc, 0, 1000);
							SpaceObject nearestObj = null;
							long closest = Long.MAX_VALUE;
							for(final SpaceObject obj : nearbies)
							{
								if(obj != ship)
								{
									final long dist=CMLib.space().getDistanceFrom(proposedLoc, obj.coordinates());
									if(dist < closest)
									{
										nearestObj = obj;
										closest = dist;
									}
								}
							}
							if(nearestObj == null)
							{
								reportError(this, controlI, mob, null, lang.L("@x1 did not lock on.",me.name(mob)));
								return;
							}
							targetSet=nearestObj;
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
							{
								final List<Item> contents=getContents();
								if(contents.size()==0)
								{
									reportError(this, controlI, mob, null, lang.L("Failure: @x1: empty.",me.name(mob)));
									return;
								}
								if(ship instanceof SpaceShip)
								{
									final ShipDir dir = CMLib.space().getDirectionFromDir(((SpaceShip)ship).facing(), ((SpaceShip)ship).roll(), targetDirection);
									if(!CMParms.contains(getCurrentBattleCoveredDirections(), dir))
									{
										reportError(this, controlI, mob, null, lang.L("Failure: @x1: launcher is not targeted correctly.",me.name(mob)));
										return;
									}
								}
								if(power == 0.0)
								{
									reportError(this, controlI, mob, null, lang.L("Failure: @x1: launcher is not charged up.",me.name(mob)));
									return;
								}
								if(super.getComputedEfficiency()==0.0)
								{
									reportError(this, controlI, mob, null, lang.L("Failure: @x1: launcher is defective.",me.name(mob)));
									return;
								}
								final Item launchedI=contents.get(0);
								if(launchedI.owner() != null)
								{
									launchedI.owner().delItem(launchedI);
									launchedI.setOwner(null);
								}
								CMLib.threads().startTickDown(launchedI, Tickable.TICKID_BALLISTICK, 1000, 1);
								final boolean inSpace = CMLib.space().isObjectInSpace(ship);
								if(inSpace)
								{
									if(launchedI instanceof SpaceObject)
									{
										final SpaceObject launchedO=(SpaceObject)launchedI;
										launchedO.setKnownSource(ship);
										launchedO.setKnownTarget(targetSet);
										double launchSpeed = SpaceObject.VELOCITY_SOUND;
										if(launchedO.speed() > 0)
											launchSpeed = launchedO.speed();
										final int accellerationOfShipInSameDirectionAsWeapon = 4; //TODO: magic numbers suck
										//TODO: adding ship.speed() is wrong because you could be firing aft.
										final Coord3D firstCoords = CMLib.space().moveSpaceObject(ship.coordinates(), targetDirection,
												(int)Math.round(ship.radius()+launchedO.radius()+ship.speed()+accellerationOfShipInSameDirectionAsWeapon));
										launchedO.setCoords(firstCoords);
										launchedO.setDirection(targetDirection);
										launchSpeed = (power/100.0) * launchSpeed * getComputedEfficiency();
										launchedO.setSpeed(launchSpeed);
										if(launchedI instanceof Ammunition)
											launchedI.setSavable(false);
										CMLib.space().addObjectToSpace(launchedO, firstCoords);
									}
									else
									{
										launchedI.destroy();
									}
								}
								else
								if(ship instanceof SpaceShip)
								{
									Room R=((SpaceShip)ship).getIsDocked();
									if(R==null)
										R=CMLib.map().roomLocation(((SpaceShip)ship).getBoardableItem());
									if(R==null)
									{
										launchedI.destroy();
										reportError(this, controlI, mob, null, lang.L("Failure: @x1: Could not launch @x2",me.name(mob),launchedI.name(mob)));
										return;
									}
									R.showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 flies out of @x2.",launchedI.name(mob),ship.name()));
									R.addItem(launchedI, Expire.Player_Drop);
								}
								else
								{
									launchedI.destroy();
									reportError(this, controlI, mob, null, lang.L("Failure: @x1: Could not launch @x2",me.name(mob),launchedI.name(mob)));
									return;
								}
								setPowerRemaining(0);
								targetSet=null;
							}
						}
						else
							reportError(this, controlI, mob, lang.L("@x1 refused to respond.",me.name(mob)), lang.L("Failure: @x1: control command failure.",me.name(mob)));
					}
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
			{
				final Room locR=CMLib.map().roomLocation(this);
				final MOB M=CMLib.map().getFactoryMOB(locR);
				final CMMsg deactivateMsg = CMClass.getMsg(M, null, null, CMMsg.MASK_ALWAYS|CMMsg.MASK_CNTRLMSG|CMMsg.MSG_DEACTIVATE,null);
				for(final Item I : this.getDeepContents())
				{
					if(I instanceof Electronics)
					{
						deactivateMsg.setTarget(I);
						if(locR.okMessage(M, deactivateMsg))
							locR.send(M, deactivateMsg);
					}
				}
				return; // don't let comp container do its thing
			}
			}
			super.executeMsg(myHost, msg);
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof StdCompLauncher))
			return false;
		return super.sameAs(E);
	}

	@Override
	public TechType panelType()
	{
		return techType;
	}

	@Override
	public void setPanelType(final TechType type)
	{
		techType = type;
	}
}
