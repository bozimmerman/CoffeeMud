package com.planet_ink.coffee_mud.Items.MiscTech;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class StdComputerConsole extends StdRideable implements ShipComponent, Electronics.Computer
{
	public String ID(){	return "StdComputerConsole";}

	protected volatile String circuitKey		= null;
	protected final TimeMs    nextPowerCycleTmr = new TimeMs(System.currentTimeMillis()+(8*1000));
	protected short 		  powerRemaining	= 0;
	protected MOB 			  lastReader		= null;
	protected ElecPanelType   panelType		 	= Electronics.ElecPanel.ElecPanelType.COMPUTER;
	protected final TimeMs    nextSoftwareCheck = new TimeMs(System.currentTimeMillis()+(10*1000));
	protected List<Software>  software		 	= null;
	protected boolean 		  activated		 	= false;
	protected String 		  currentMenu		= "";
	
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
		setLidsNLocks(false,true,false,false);
		capacity=500;
		material=RawMaterial.RESOURCE_STEEL;
		recoverPhyStats();
	}

	public int fuelType(){return RawMaterial.RESOURCE_ENERGY;}
	public void setFuelType(int resource){}
	public long powerCapacity(){return 1;}
	public void setPowerCapacity(long capacity){}
	public long powerRemaining(){return powerRemaining;}
	public void setPowerRemaining(long remaining){ powerRemaining=(remaining>0)?(short)1:(short)0; }
	public boolean activated(){return activated;}
	public void activate(boolean truefalse){activated=truefalse;}
	public void setActiveMenu(String internalName) { currentMenu=internalName; }
	public String getActiveMenu() { return currentMenu; }
	
	public ElecPanelType panelType(){return panelType;}
	public void setPanelType(ElecPanelType type){panelType=type;}
	
	public boolean canContain(Environmental E)
	{
		return E instanceof Software;
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(affected instanceof Room)
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ROOMCIRCUITED);
	}

	public List<Software> getSoftware()
	{
		if((software==null)||(nextSoftwareCheck.isNowLaterThan()))
		{
			final List<Item> list=getContents();
			final LinkedList<Software> softwareList=new LinkedList<Software>();
			for(Item I : list)
				if(I instanceof Software)
					softwareList.add((Software)I);
			nextSoftwareCheck.setToLater(10*1000);
			software=softwareList;
		}
		return software;
	}
	
	public String readableText()
	{
		final StringBuilder str=new StringBuilder(super.readableText());
		str.append("\n\r");
		if(!activated())
			str.append("The screen is blank.  Try activating/booting it first.");
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
					str.append("\n\rEnter \"<\" to return to the previous menu.");
				}
				else
				if(software.size()>0)
				{
					str.append("\n\rEnter a command:");
				}
				else
				{
					str.append("\n\rThis system is ready to receive software.");
				}
			}
		}
		
		return str.toString();
	}
	
	public List<MOB> getCurrentReaders()
	{
		List<MOB> readers=new LinkedList<MOB>();
		final MOB lastReader=this.lastReader;
		if(lastReader!=null)
		{
			if(((lastReader==owner())||(lastReader.location()==owner()))
			&&(CMLib.flags().isInTheGame(lastReader, true)))
				readers.add(lastReader);
			else
				this.lastReader=null;
		}
		for(Rider R : riders)
			if(R instanceof MOB)
				readers.add((MOB)R);
		return readers;
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_POWERCURRENT:
				return true;
			case CMMsg.TYP_READ:
			case CMMsg.TYP_WRITE:
				if(!activated())
				{
					msg.source().tell(name()+" is not activated/booted up.");
					return false;
				}
				return true;
			case CMMsg.TYP_ACTIVATE:
				if((msg.targetMessage()==null)&&(activated()))
				{
					msg.source().tell(name()+" is already booted up.");
					return false;
				}
				else
				if(!StdElecItem.isAllWiringConnected(this))
				{
					if(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG))
						msg.source().tell("The panel containing "+name()+" is not activated or connected.");
					return false;
				}
				else
				if(powerRemaining()<=0)
				{
					if((!CMLib.tech().seekBatteryPower(this, this.circuitKey))
					||(powerRemaining()<=0))
					{
						msg.source().tell(name()+" won't seem to power up. Perhaps it needs power?");
						return false;
					}
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if((msg.targetMessage()==null)&&(!activated()))
				{
					msg.source().tell(name()+" is already shut down.");
					return false;
				}
				break;
			}
		}
		return super.okMessage(host,msg);
	}

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
					List<CMMsg> msgs=new LinkedList<CMMsg>();
					synchronized(software)
					{
						for(final Software S : software)
						{
							if(S.getInternalName().equals(currentMenu))
							{
								if(msg.targetMessage().trim().equals("<"))
								{
									msgs.add(CMClass.getMsg(msg.source(),S,null,CMMsg.NO_EFFECT,CMMsg.MASK_ALWAYS|CMMsg.TYP_DEACTIVATE,CMMsg.NO_EFFECT,null));
								}
								else
								if(S.isActivationString(msg.targetMessage(), true))
								{
									msgs.add(CMClass.getMsg(msg.source(),S,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_WRITE,msg.targetMessage(),CMMsg.NO_EFFECT,null));
								}
							}
							else
							if((S.getParentMenu().equals(currentMenu))
							&&(S.isActivationString(msg.targetMessage(), false)))
							{
								msgs.add(CMClass.getMsg(msg.source(),S,null,CMMsg.NO_EFFECT,null,CMMsg.MASK_ALWAYS|CMMsg.TYP_ACTIVATE,msg.targetMessage(),CMMsg.NO_EFFECT,null));
							}
						}
					}
					boolean readFlag=false;
					for(CMMsg msg2 : msgs)
					{
						if(msg2.target().okMessage(msg.source(), msg2))
						{
							msg2.target().executeMsg(msg.source(), msg2);
							if(msg2.target() instanceof Software)
							{
								Software sw=(Software)msg2.target();
								if(msg2.targetMinor()==CMMsg.TYP_ACTIVATE)
								{
									setActiveMenu(sw.getInternalName());
								}
								else
								if(msg2.targetMinor()==CMMsg.TYP_DEACTIVATE)
								{
									setActiveMenu(sw.getParentMenu());
								}
								readFlag=true;
							}
						}
					}
					if(readFlag)
						forceReadersSeeNew();
				}
				break;
			}
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUT:
				nextSoftwareCheck.set(0);
				break;
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
					msg.source().tell(name()+" is currently "+(activated()?"booted up and the screen ready to be read.\n\r":"deactivated.\n\r"));
				return;
			case CMMsg.TYP_ACTIVATE:
				if(!activated())
				{
					activate(true);
					setActiveMenu("");
					if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
						msg.source().location().show(msg.source(),this,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> boot(s) up <T-NAME>.");
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if(activated())
				{
					activate(false);
					if((msg.source().location()!=null)&&(!CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
						msg.source().location().show(msg.source(),this,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> shut(s) down <T-NAME>.");
					deactivateSystem();
				}
				break;
			case CMMsg.TYP_POWERCURRENT:
				{
					final int powerToGive=msg.value();
					if(powerToGive>0)
					{
						if(powerRemaining()==0)
							setPowerRemaining(1);
						nextPowerCycleTmr.setToLater(8*1000);
					}
					if(activated())
					{
						final List<Software> software=getSoftware();
						CMMsg msg2=CMClass.getMsg(msg.source(), null, null, CMMsg.NO_EFFECT,null,CMMsg.MSG_POWERCURRENT,null,CMMsg.NO_EFFECT,null);
						synchronized(software)
						{
							for(Software sw : software)
							{
								msg2.setTarget(sw);
								msg2.setValue(((powerToGive>0)?1:0)+(this.getActiveMenu().equals(sw.getInternalName())?1:0));
								if(sw.okMessage(host, msg2))
									sw.executeMsg(host, msg2);
							}
						}
					}
					forceReadersSeeNew();
				}
				break;
			}
		}
		else
		if((msg.source()==this.lastReader)&&(msg.target() instanceof Electronics.ElecPanel)&&(msg.targetMinor()==CMMsg.TYP_READ))
			this.lastReader=null;
		super.executeMsg(host,msg);
	}
	
	public void forceReadersSeeNew()
	{
		if(activated())
		{
			final List<Software> software=getSoftware();
			synchronized(software)
			{
				final StringBuilder newMsgs=new StringBuilder();
				for(Software sw : software)
					newMsgs.append(sw.getScreenMessage());
				if(newMsgs.length()>0)
				{
					List<MOB> readers=getCurrentReaders();
					for(MOB M : readers)
						if(CMLib.flags().canBeSeenBy(this, M))
							M.location().show(M, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, "<T-NAME> says '^N\n\r"+newMsgs.toString()+"\n\r^.^N'");
				}
			}
		}
	}
	
	public void forceReadersMenu()
	{
		if(activated())
		{
			List<MOB> readers=getCurrentReaders();
			for(MOB M : readers)
				CMLib.commands().postRead(M, this, "", true);
		}
	}

	public void destroy()
	{
		if((!destroyed)&&(circuitKey!=null))
		{
			CMLib.tech().unregisterElectronics(this,circuitKey);
			circuitKey=null;
			CMLib.threads().deleteTick(this,Tickable.TICKID_ELECTRONICS);
		}
		super.destroy();
	}
	
	public void setOwner(ItemPossessor owner)
	{
		final ItemPossessor prevOwner=super.owner;
		super.setOwner(owner);
		if(prevOwner != owner)
		{
			if(owner instanceof Room)
			{
				if(!CMLib.threads().isTicking(this, Tickable.TICKID_ELECTRONICS))
					CMLib.threads().startTickDown(this, Tickable.TICKID_ELECTRONICS, 1);
				circuitKey=CMLib.tech().registerElectrics(this,circuitKey);
			}
			else
			{
				CMLib.threads().deleteTick(this,Tickable.TICKID_ELECTRONICS);
				CMLib.tech().unregisterElectronics(this,circuitKey);
				circuitKey=null;
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(tickID==Tickable.TICKID_ELECTRONICS)
		{
			if(!activated())
			{
			}
			else
			if(nextPowerCycleTmr.isNowLaterThan())
			{
				deactivateSystem();
			}
		}
		return true;
	}
	
	public void deactivateSystem()
	{
		if(activated())
		{
			final List<Software> software=getSoftware();
			final Room locR=CMLib.map().roomLocation(this);
			CMMsg msg2=CMClass.getMsg(CMLib.map().getFactoryMOB(locR), null, null, CMMsg.NO_EFFECT,null,CMMsg.MSG_DEACTIVATE,null,CMMsg.NO_EFFECT,null);
			synchronized(software)
			{
				for(Software sw : software)
				{
					msg2.setTarget(sw);
					if(sw.okMessage(msg2.source(), msg2))
						sw.executeMsg(msg2.source(), msg2);
				}
			}
			setPowerRemaining(0);
			activate(false);
			List<MOB> readers=getCurrentReaders();
			for(MOB M : readers)
				if(CMLib.flags().canBeSeenBy(this, M))
					M.location().show(M, this, null, CMMsg.MASK_ALWAYS|CMMsg.TYP_OK_VISUAL, CMMsg.NO_EFFECT, CMMsg.NO_EFFECT, "The screen on <T-NAME> goes blank.");
		}
	}
}
