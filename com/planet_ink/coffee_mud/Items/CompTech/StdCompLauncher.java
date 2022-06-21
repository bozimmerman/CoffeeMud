package com.planet_ink.coffee_mud.Items.CompTech;
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
   Copyright 2022-2022 Bo Zimmerman

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
public class StdCompLauncher extends StdElecCompContainer implements ElecPanel, TechComponent, ShipDirectional
{
	@Override
	public String ID()
	{
		return "StdCompLauncher";
	}

	private ShipDir[]						allPossDirs		= new ShipDir[] { ShipDir.FORWARD };
	private int								numPermitDirs	= 1;
	protected volatile int					powerNeeds		= 0;
	protected volatile String				circuitKey		= null;
	private volatile ShipDir[]				currCoverage	= null;
	private volatile Reference<SpaceShip>	myShip			= null;
	private volatile long					powerSetting	= Integer.MAX_VALUE;
	private final double[]					targetDirection	= new double[] { 0.0, 0.0 };

	public StdCompLauncher()
	{
		super();
		setName("a launcher tube");
		setDisplayText("a launcher tube is mounted here");
		setDescription("Probably requires particular things to launcher out of it.");
		super.setDoorsNLocks(true, true, true,false, false,false);
		basePhyStats().setSensesMask(basePhyStats().sensesMask()|PhyStats.SENSE_ALWAYSCOMPRESSED|PhyStats.SENSE_ITEMNOTGET);
		this.openDelayTicks=0;
		this.recoverPhyStats();
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
	public TechType panelType()
	{
		return panelType;
	}

	@Override
	public void setPanelType(final TechType type)
	{
		panelType = type;
	}

	@Override
	public int powerNeeds()
	{
		return powerNeeds;
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
					this.activate(true);
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
						if(command == TechCommand.TARGETSET)
						{
							final SpaceObject ship = CMLib.space().getSpaceObject(this, true);
							if(ship == null)
								reportError(this, controlI, mob, lang.L("@x1 did not respond.",me.name(mob)), lang.L("Failure: @x1: control syntax failure.",me.name(mob)));
							else
							{
								if(ship instanceof SpaceShip)
								{
									final double[] proposedDirection=new double[] {((Double)parms[0]).doubleValue(),((Double)parms[1]).doubleValue()};
									final ShipDir dir = CMLib.space().getDirectionFromDir(((SpaceShip)ship).facing(), ((SpaceShip)ship).roll(), proposedDirection);
									if(!CMParms.contains(getCurrentBattleCoveredDirections(), dir))
										reportError(this, controlI, mob, null, lang.L("Failure: @x1: weapon is not facing correctly for that target direction.",me.name(mob)));
									else
									{
										targetDirection[0] = ((Double)parms[0]).doubleValue();
										targetDirection[1] = ((Double)parms[1]).doubleValue();
									}
								}
								else
								{
									targetDirection[0] = ((Double)parms[0]).doubleValue();
									targetDirection[1] = ((Double)parms[1]).doubleValue();
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
								if(ship instanceof SpaceShip)
								{
									final ShipDir dir = CMLib.space().getDirectionFromDir(((SpaceShip)ship).facing(), ((SpaceShip)ship).roll(), targetDirection);
									if(!CMParms.contains(getCurrentBattleCoveredDirections(), dir))
									{
										reportError(this, controlI, mob, null, lang.L("Failure: @x1: launcher is not targeted correctly.",me.name(mob)));
										return;
									}
								}
								//CMLib.threads().startTickDown(weaponO, Tickable.TICKID_BEAMWEAPON, 10);
								//CMLib.space().addObjectToSpace(weaponO, firstCoords);
								setPowerRemaining(0);
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
}
