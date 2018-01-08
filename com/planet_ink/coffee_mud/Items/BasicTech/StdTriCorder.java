package com.planet_ink.coffee_mud.Items.BasicTech;
import com.planet_ink.coffee_mud.Items.Basic.StdContainer;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class StdTriCorder extends StdElecContainer implements Computer
{
	@Override
	public String ID()
	{
		return "StdTriCorder";
	}

	protected final static int POWER_RATE		= 4; // how often (in ticks) an activated tricorder loses a tick of power. at 1000 power, this is 1 hr/rate (4 hrs total)

	protected MOB 			  lastReader		= null;
	protected volatile long   nextSoftwareCheck = System.currentTimeMillis()+(10*1000);
	protected List<Software>  software		 	= null;
	protected String 		  currentMenu		= "";
	protected int			  nextPowerCycleCtr = POWER_RATE+1;

	public StdTriCorder()
	{
		super();
		setName("a tri-corder");
		basePhyStats.setWeight(2);
		setDisplayText("a personal scanning device sits here.");
		setDescription("For all your scanning and mobile computing needs.");
		baseGoldValue=2500;
		basePhyStats().setLevel(1);
		setMaterial(RawMaterial.RESOURCE_STEEL);
		super.activate(true);
		setDoorsNLocks(false,true,false,false,false,false);
		setCapacity(3);
		super.setPowerCapacity(1000);
		super.setPowerRemaining(1000);
		basePhyStats.setSensesMask(basePhyStats.sensesMask()|PhyStats.SENSE_ITEMREADABLE);
		recoverPhyStats();
	}

	@Override
	public TechType panelType()
	{
		return TechType.PERSONAL_SOFTWARE;
	}

	@Override
	public void setPanelType(TechType type)
	{
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
	public TechType getTechType()
	{
		return TechType.PERSONAL_SENSOR;
	}

	@Override
	public boolean canContain(Item I)
	{
		return (I instanceof Software) && (((Software)I).getTechType()==TechType.PERSONAL_SOFTWARE);
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
					softwareList.add((Software)I);
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
		if(owner() instanceof MOB)
			readers.add((MOB)owner());
		return readers;
	}

	@Override
	public void setOwner(ItemPossessor owner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(owner);
		if((prevOwner != owner)&&(owner!=null))
		{
			if(!CMLib.threads().isTicking(this, Tickable.TICKID_ELECTRONICS))
				CMLib.threads().startTickDown(this, Tickable.TICKID_ELECTRONICS, 1);
		}
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_READ:
			case CMMsg.TYP_WRITE:
				if(this.amWearingAt(Wearable.IN_INVENTORY))
				{
					msg.source().tell(L("@x1 needs to be held first.",name()));
					return false;
				}
				if(!activated())
				{
					msg.source().tell(L("@x1 is not activated/booted up.",name()));
					return false;
				}
				return true;
			case CMMsg.TYP_ACTIVATE:
				if(this.amWearingAt(Wearable.IN_INVENTORY) && (!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
				{
					msg.source().tell(L("@x1 needs to be held first.",name()));
					return false;
				}
				if((msg.targetMessage()==null)&&(activated()))
				{
					msg.source().tell(L("@x1 is already booted up.",name()));
					return false;
				}
				else
				if(powerRemaining()<=0)
				{
					msg.source().tell(L("@x1 won't seem to power up. Perhaps it needs power?",name()));
					return false;
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if(this.amWearingAt(Wearable.IN_INVENTORY) && (!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
				{
					msg.source().tell(L("@x1 needs to be held first.",name()));
					return false;
				}
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
							if(S.getInternalName().equals(currentMenu))
							{
								if(msg.targetMessage().trim().equals("<") && (currentMenu.length()>0))
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
					if(readFlag)
						forceReadersSeeNew();
					if(menuRead)
						forceReadersMenu();
				}
				break;
			}
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_INSTALL:
				nextSoftwareCheck=0;
				break;
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()) && (!amWearingAt(Wearable.IN_INVENTORY)))
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
						M.location().show(M, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, L("<T-NAME> says '^N\n\rUnknown activation command. Please read the screen for a menu of TYPEable commands.\n\r^.^N'"));
					else
					for(final CMMsg msg2 : msgs)
					{
						if(msg2.target().okMessage(M, msg2))
							msg2.target().executeMsg(M, msg2);
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
							if(S.isDeActivationString(msg.targetMessage()))
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
			}
		}
		else
		if((msg.source()==this.lastReader)&&(msg.targetMinor()==CMMsg.TYP_READ)&&(msg.target() instanceof ElecPanel))
			this.lastReader=null; // whats this do?
		super.executeMsg(host,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(tickID==Tickable.TICKID_ELECTRONICS)
		{
			if(activated() && owner() instanceof MOB)
			{
				final MOB mob=(MOB)owner();
				final List<Software> software=getSoftware();
				final CMMsg msg2=CMClass.getMsg(mob, null, null, CMMsg.NO_EFFECT,null,CMMsg.MSG_POWERCURRENT,null,CMMsg.NO_EFFECT,null);
				synchronized(software) // this is how software ticks, even tricorder software...
				{
					for(final Software sw : software)
					{
						msg2.setTarget(sw);
						msg2.setValue(1+(this.getActiveMenu().equals(sw.getInternalName())?1:0));
						if(sw.okMessage(mob, msg2))
							sw.executeMsg(mob, msg2);
					}
				}
			}
			forceReadersSeeNew();
			if(--nextPowerCycleCtr<=0)
			{
				nextPowerCycleCtr=POWER_RATE;
				setPowerRemaining(this.powerRemaining()-1);
				if(powerRemaining()<=0)
					deactivateSystem();
			}
		}
		return true;
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
			activate(false);
			if(owner() instanceof MOB)
			{
				final MOB M=(MOB)owner();
				if(CMLib.flags().canBeSeenBy(this, M))
					M.location().show(M, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, L("The screen on <T-NAME> goes blank."));
			}
		}
	}
}
