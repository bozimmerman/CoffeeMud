package com.planet_ink.coffee_mud.Items.CompTech;
import com.planet_ink.coffee_mud.Items.Basic.StdRideable;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.TimeMs;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2012-2018 Bo Zimmerman

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
public class StdComputerConsole extends StdRideable implements TechComponent, Computer, ElecPanel
{
	@Override
	public String ID()
	{
		return "StdComputerConsole";
	}

	protected volatile String	circuitKey			= null;
	protected float				installedFactor		= 1.0F;
	protected short				powerRemaining		= 0;
	protected boolean			activated			= false;
	protected volatile long		nextPowerCycleTmr	= System.currentTimeMillis() + (8 * 1000);
	protected MOB				lastReader			= null;
	protected volatile long		nextSoftwareCheck	= System.currentTimeMillis() + (10 * 1000);
	protected List<Software>	software			= null;
	protected String			currentMenu			= "";
	protected String			manufacturer		= "RANDOM";
	protected Manufacturer		cachedManufact		= null;

	public StdComputerConsole()
	{
		super();
		setName("a computer console");
		setDisplayText("a computer console is here");
		basePhyStats.setWeight(20);
		setDescription("");
		baseGoldValue=5;
		containType=Container.CONTAIN_SSCOMPONENTS;
		rideBasis=Rideable.RIDEABLE_TABLE;
		riderCapacity=1;
		basePhyStats.setSensesMask(basePhyStats.sensesMask()|PhyStats.SENSE_ITEMREADABLE|PhyStats.SENSE_ITEMNOTGET);
		setDoorsNLocks(false,true,false,false,false,false);
		capacity=50;
		material=RawMaterial.RESOURCE_STEEL;
		setUsesRemaining(100);
		activate(true);
		recoverPhyStats();
	}

	@Override
	public float getInstalledFactor()
	{
		return installedFactor;
	}

	@Override
	public void setInstalledFactor(float pct)
	{
		if ((pct >= 0.0) && (pct <= 2.0))
			installedFactor = pct;
	}

	@Override
	public long powerCapacity()
	{
		return 1;
	}

	@Override
	public void setPowerCapacity(long capacity)
	{
	}

	@Override
	public int powerNeeds()
	{
		return 1;
	}

	@Override
	public void setRechargeRate(float pctCapPer)
	{
		
	}

	@Override
	public float getRechargeRate()
	{
		return 1;
	}
	
	@Override
	public long powerRemaining()
	{
		return powerRemaining;
	}

	@Override
	public void setPowerRemaining(long remaining)
	{
		powerRemaining = (remaining > 0) ? (short) 1 : (short) 0;
	}

	@Override
	public boolean activated()
	{
		return activated;
	}

	@Override
	public void activate(boolean truefalse)
	{
		activated = truefalse;
	}

	@Override
	public void setActiveMenu(String internalName)
	{
		currentMenu = internalName;
	}

	@Override
	public String getActiveMenu()
	{
		return currentMenu;
	}

	@Override
	public int techLevel()
	{
		return phyStats().ability();
	}

	@Override
	public void setTechLevel(int lvl)
	{
		basePhyStats.setAbility(lvl);
		recoverPhyStats();
	}

	@Override
	public String getManufacturerName()
	{
		return manufacturer;
	}

	@Override
	public void setManufacturerName(String name)
	{
		cachedManufact = null;
		if (name != null)
			manufacturer = name;
	}

	@Override
	public TechType getTechType()
	{
		return TechType.SHIP_COMPUTER;
	}

	protected double getComputedEfficiency()
	{
		double generatedAmount = 1.0;
		if(subjectToWearAndTear() && (usesRemaining()<=200))
			generatedAmount *= CMath.div(usesRemaining(), 100.0);
		return generatedAmount * this.getInstalledFactor();
	}
	
	@Override
	public Manufacturer getFinalManufacturer()
	{
		if(cachedManufact==null)
		{
			cachedManufact=CMLib.tech().getManufacturerOf(this,manufacturer.toUpperCase().trim());
			if(cachedManufact==null)
				cachedManufact=CMLib.tech().getDefaultManufacturer();
		}
		return cachedManufact;
	}

	@Override
	public TechType panelType()
	{
		return TechType.SHIP_SOFTWARE;
	}

	@Override
	public void setPanelType(TechType type)
	{
	}

	@Override
	public boolean canContain(Item I)
	{
		return (I instanceof Software) && (((Software)I).getTechType()==TechType.SHIP_SOFTWARE);
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return((usesRemaining()<=1000)&&(usesRemaining()>=0));
	}

	@Override
	public String putString(Rider R)
	{
		if((R==null)||(putString.length()==0))
			return "in";
		return putString;
	}

	@Override
	public String stateStringSubject(Rider R)
	{
		if((R==null)||(stateSubjectStr.length()==0))
			return "being used by";
		return stateSubjectStr;
	}

	@Override
	public List<Software> getSoftware()
	{
		if((software==null)||(System.currentTimeMillis()>nextSoftwareCheck))
		{
			final List<Item> list=getContents();
			final LinkedList<Software> softwareList=new LinkedList<Software>();
			for(final Item I : list)
			{
				if(I instanceof Software)
				{
					((Software)I).setCircuitKey(circuitKey);
					softwareList.add((Software)I);
				}
			}
			nextSoftwareCheck=System.currentTimeMillis()+(10*1000);
			software=softwareList;
		}
		return software;
	}

	@Override
	public void setReadableText(String text)
	{
		// important that this does nothing
	}

	@Override
	public boolean isInstalled()
	{
		if(!CMLib.flags().isGettable(this))
			return true;
		if(this.container() instanceof ElecPanel)
			return (!CMLib.flags().isGettable(this.container()));
		if(this.container() instanceof TechComponent)
			return ((TechComponent)this.container()).isInstalled();
		return false;
	}

	@Override
	public String readableText()
	{
		final StringBuilder str=new StringBuilder("");
		str.append("\n\r");
		if(!activated())
			str.append(L("The screen is blank.  Try activating/booting it first."));
		else
		{
			final List<Software> software=getSoftware();
			synchronized(software)
			{
				boolean isInternal=false;
				for(final Software S : software)
				{
					if(S.getInternalName().equals(currentMenu))
					{
						str.append(S.getCurrentScreenDisplay());
						isInternal=true;
					}
					else
					if(S.getParentMenu().equals(currentMenu))
					{
						str.append(S.getActivationMenu()).append("\n\r");
					}
				}
				if(isInternal)
				{
					str.append(L("\n\rEnter \"<\" to return to the previous menu."));
				}
				else
				if(software.size()>0)
				{
					str.append(L("\n\rType in a command:"));
				}
				else
				{
					str.append(L("\n\rThis system is ready to receive software."));
				}
			}
		}

		return str.toString();
	}

	@Override
	public List<MOB> getCurrentReaders()
	{
		final List<MOB> readers=new LinkedList<MOB>();
		if(amDestroyed())
			return readers;
		final MOB lastReader=this.lastReader;
		if(lastReader!=null)
		{
			if(((lastReader==owner())||(lastReader.location()==owner()))
			&&(CMLib.flags().isInTheGame(lastReader, true)))
				readers.add(lastReader);
			else
				this.lastReader=null;
		}
		for(final Rider R : riders)
		{
			if(R instanceof MOB)
				readers.add((MOB)R);
		}
		return readers;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_POWERCURRENT:
				break;
			case CMMsg.TYP_READ:
			case CMMsg.TYP_WRITE:
				if(!activated())
				{
					msg.source().tell(L("@x1 is not activated/booted up.",name()));
					return false;
				}
				return true;
			case CMMsg.TYP_ACTIVATE:
				if((msg.targetMessage()==null)&&(activated()))
				{
					msg.source().tell(L("@x1 is already booted up.",name()));
					return false;
				}
				else
				if(powerRemaining()<=0)
				{
					if((!CMLib.tech().seekBatteryPower(this, this.circuitKey))
					||(powerRemaining()<=0))
					{
						msg.source().tell(L("@x1 won't seem to power up. Perhaps it needs power?",name()));
						return false;
					}
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.targetMessage()==null)&&(!activated()))
				{
					msg.source().tell(L("@x1 is already shut down.",name()));
					return false;
				}
				break;
			}
		}
		return super.okMessage(host,msg);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DROP:
				setInstalledFactor((float)CMath.div(msg.value(),100.0));
				break;
			case CMMsg.TYP_READ:
				if(msg.source().riding()!=this)
					lastReader=msg.source();
				break;
			case CMMsg.TYP_WRITE:
			{
				if(msg.targetMessage()!=null)
				{
					final List<Software> software=getSoftware();
					final List<CMMsg> msgs=new LinkedList<CMMsg>();
					synchronized(software)
					{
						for(final Software S : software)
						{
							if(S.getInternalName().equals(currentMenu) && (currentMenu.length()>0))
							{
								if(msg.targetMessage().trim().equals("<"))
								{
									msgs.add(CMClass.getMsg(msg.source(),S,null,CMMsg.NO_EFFECT,CMMsg.MASK_ALWAYS|CMMsg.TYP_DEACTIVATE,CMMsg.NO_EFFECT,null));
								}
								else
								if(S.isCommandString(msg.targetMessage(), true))
								{
									msgs.add(CMClass.getMsg(msg.source(),S,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WRITE,msg.targetMessage(),CMMsg.NO_EFFECT,null));
								}
							}
							else
							if((S.getParentMenu().equals(currentMenu))
							&&(S.isCommandString(msg.targetMessage(), false)))
							{
								msgs.add(CMClass.getMsg(msg.source(),S,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACTIVATE,msg.targetMessage(),CMMsg.NO_EFFECT,null));
							}
						}
					}
					boolean readFlag=false;
					boolean menuRead=false;
					final MOB M=msg.source();
					if(msgs.size()==0)
						M.location().show(M, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, L("<T-NAME> says '^N\n\rUnknown command. Please read the screen for a menu.\n\r^.^N'"));
					else
					{
						if((!subjectToWearAndTear()) || (Math.random() < CMath.div(usesRemaining(), 100)))
						{
							for(final CMMsg msg2 : msgs)
							{
								if(msg2.target().okMessage(M, msg2))
								{
									msg2.target().executeMsg(M, msg2);
									if(msg2.target() instanceof Software)
									{
										final Software sw=(Software)msg2.target();
										if(msg2.targetMinor()==CMMsg.TYP_ACTIVATE)
										{
											setActiveMenu(sw.getInternalName());
											readFlag=true;
										}
										else
										if(msg2.targetMinor()==CMMsg.TYP_DEACTIVATE)
										{
											setActiveMenu(sw.getParentMenu());
											menuRead=true;
										}
										else
										{
											readFlag=true;
										}
									}
								}
							}
						}
						else
						{
							final List<MOB> readers=getCurrentReaders();
							for(final MOB M2 : readers)
							{
								if(CMLib.flags().canBeSeenBy(this, M2))
									M2.location().show(M2, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, L("<T-NAME> blue screens!!\n\r^.^N'"));
							}
							deactivateSystem();
						}
					}
					if(readFlag)
						forceReadersSeeNew();
					if(menuRead)
						forceReadersMenu();
				}
				break;
			}
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_INSTALL:
				nextSoftwareCheck=0;
				break;
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
					msg.source().tell(L("@x1 is currently @x2",name(),(activated()?"booted up and the screen ready to be read.\n\r":"deactivated.\n\r")));
				return;
			case CMMsg.TYP_ACTIVATE:
				if(!activated())
				{
					activate(true);
					setActiveMenu("");
					if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
					{
						msg.source().location().show(msg.source(),this,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> boot(s) up <T-NAME>."));
						forceReadersMenu();
					}
				}
				if((msg.targetMessage()!=null)&&(activated()))
				{
					final List<Software> software=getSoftware();
					final List<CMMsg> msgs=new LinkedList<CMMsg>();
					synchronized(software)
					{
						for(final Software S : software)
						{
							if(S.isActivationString(msg.targetMessage()))
							{
								msgs.add(CMClass.getMsg(msg.source(),S,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACTIVATE,msg.targetMessage(),CMMsg.NO_EFFECT,null));
							}
						}
					}
					final boolean readFlag=false;
					final MOB M=msg.source();
					if(msgs.size()==0)
					{
						final Room R=M.location();
						if(R!=null)
							R.show(M, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, L("<T-NAME> says '^N\n\rUnknown activation command. Please read the screen for a menu of TYPEable commands.\n\r^.^N'"));
					}
					else
					{
						double damageFailChance=1.0;
						if(subjectToWearAndTear() && (usesRemaining()<75))
						{
							damageFailChance = CMath.div(usesRemaining(), 100);
							damageFailChance += (0.35 * getFinalManufacturer().getReliabilityPct());
						}
						if((Math.random()<getInstalledFactor()) && (Math.random()<damageFailChance))
						{
							for(final CMMsg msg2 : msgs)
							{
								if(msg2.target().okMessage(M, msg2))
									msg2.target().executeMsg(M, msg2);
							}
						}
						else
						{
							final List<MOB> readers=getCurrentReaders();
							for(final MOB M2 : readers)
							{
								if(CMLib.flags().canBeSeenBy(this, M2))
									M2.location().show(M2, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, L("<T-NAME> blue screens!!\n\r^.^N'"));
							}
							deactivateSystem();
						}
					}
					if(readFlag)
						forceReadersSeeNew();
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.targetMessage()!=null)&&(activated()))
				{
					final List<Software> software=getSoftware();
					final List<CMMsg> msgs=new LinkedList<CMMsg>();
					synchronized(software)
					{
						for(final Software S : software)
						{
							if(S.isActivationString(msg.targetMessage()))
							{
								msgs.add(CMClass.getMsg(msg.source(),S,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_DEACTIVATE,msg.targetMessage(),CMMsg.NO_EFFECT,null));
							}
						}
					}
					final boolean readFlag=false;
					final MOB M=msg.source();
					if(msgs.size()==0)
						M.location().show(M, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, L("<T-NAME> says '^N\n\rUnknown deactivation command. Please read the screen for a menu of TYPEable commands.\n\r^.^N'"));
					else
					for(final CMMsg msg2 : msgs)
					{
						if(msg2.target().okMessage(M, msg2))
							msg2.target().executeMsg(M, msg2);
					}
					if(readFlag)
						forceReadersSeeNew();
				}
				else
				if(activated())
				{
					activate(false);
					if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
						msg.source().location().show(msg.source(),this,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> shut(s) down <T-NAME>."));
					deactivateSystem();
				}
				break;
			case CMMsg.TYP_POWERCURRENT:
				{
					if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
						nextPowerCycleTmr=System.currentTimeMillis()+(8*1000);
					final int powerToGive=msg.value();
					if(powerToGive>0)
					{
						if(powerRemaining()==0)
							setPowerRemaining(1);
						nextPowerCycleTmr=System.currentTimeMillis()+(8*1000);
					}
					if(activated())
					{
						final List<Software> software=getSoftware();
						final CMMsg msg2=CMClass.getMsg(msg.source(), null, null, CMMsg.NO_EFFECT,null,CMMsg.MSG_POWERCURRENT,null,CMMsg.NO_EFFECT,null);
						synchronized(software)
						{
							for(final Software sw : software)
							{
								msg2.setTarget(sw);
								msg2.setValue(((powerToGive>0)?1:0)+(this.getActiveMenu().equals(sw.getInternalName())?1:0));
								if(sw.okMessage(host, msg2))
									sw.executeMsg(host, msg2);
							}
						}
					}
					forceReadersSeeNew();
					if(System.currentTimeMillis()>nextPowerCycleTmr)
					{
						deactivateSystem();
					}
				}
				break;
			}
		}
		else
		if((msg.source()==this.lastReader)
		&&(msg.target() instanceof ElecPanel)
		&&(msg.targetMinor()==CMMsg.TYP_READ))
			this.lastReader=null;
		super.executeMsg(host,msg);
	}

	@Override
	public void forceReadersSeeNew()
	{
		if(activated())
		{
			final List<Software> software=getSoftware();
			synchronized(software)
			{
				final StringBuilder newMsgs=new StringBuilder();
				for(final Software sw : software)
					newMsgs.append(sw.getScreenMessage());
				if(newMsgs.length()>0)
				{
					final List<MOB> readers=getCurrentReaders();
					for(final MOB M : readers)
						if(CMLib.flags().canBeSeenBy(this, M))
							M.location().show(M, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, L("<T-NAME> says '^N\n\r@x1\n\r^.^N'",newMsgs.toString()));
				}
			}
		}
	}

	@Override
	public void forceReadersMenu()
	{
		if(activated())
		{
			final List<MOB> readers=getCurrentReaders();
			for(final MOB M : readers)
				CMLib.commands().postRead(M, this, "", true);
		}
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
	public void setOwner(ItemPossessor owner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(owner);
		if(prevOwner != owner)
		{
			if(owner instanceof Room)
				circuitKey=CMLib.tech().registerElectrics(this,circuitKey);
			else
				circuitKey=null;
		}
	}

	protected void deactivateSystem()
	{
		if(activated())
		{
			final List<Software> software=getSoftware();
			final Room locR=CMLib.map().roomLocation(this);
			final CMMsg msg2=CMClass.getMsg(CMLib.map().getFactoryMOB(locR), null, null, CMMsg.NO_EFFECT,null,CMMsg.MSG_DEACTIVATE,null,CMMsg.NO_EFFECT,null);
			synchronized(software)
			{
				for(final Software sw : software)
				{
					msg2.setTarget(sw);
					if(sw.okMessage(msg2.source(), msg2))
						sw.executeMsg(msg2.source(), msg2);
				}
			}
			setPowerRemaining(0);
			activate(false);
			final List<MOB> readers=getCurrentReaders();
			for(final MOB M : readers)
			{
				if(CMLib.flags().canBeSeenBy(this, M))
					M.location().show(M, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, L("The screen on <T-NAME> goes blank."));
			}
		}
	}
	
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdComputerConsole))
			return false;
		return super.sameAs(E);
	}
}
