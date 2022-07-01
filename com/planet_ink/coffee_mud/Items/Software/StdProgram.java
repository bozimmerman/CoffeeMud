package com.planet_ink.coffee_mud.Items.Software;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.*;

/*
   Copyright 2005-2022 Bo Zimmerman

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
public class StdProgram extends StdItem implements Software
{
	@Override
	public String ID()
	{
		return "StdProgram";
	}

	protected StringBuilder	nextMsg			= new StringBuilder("");
	protected String		currentScreen	= "";
	protected String		manufacturer	= "RANDOM";
	protected Manufacturer	cachedManufact	= null;
	protected String		circuitKey		= "";

	protected volatile boolean	isActivated	= false;

	protected Map<SWServices,Set<Software>> svcs = new Hashtable<SWServices,Set<Software>>();

	public StdProgram()
	{
		super();
		setName("a software disk");
		setDisplayText("a small disk sits here.");
		setDescription("It appears to be a general software program.");

		basePhyStats().setWeight(1);
		phyStats().setWeight(1);
		material=RawMaterial.RESOURCE_STEEL;
		baseGoldValue=1000;
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("a disk");
	}

	@Override
	public void setCircuitKey(final String key)
	{
		circuitKey=(key==null)?"":key;
	}


	@Override
	public int techLevel()
	{
		return phyStats().ability();
	}

	@Override
	public void setTechLevel(final int lvl)
	{
		basePhyStats.setAbility(lvl);
		recoverPhyStats();
	}

	@Override
	public String getParentMenu()
	{
		return "";
	}

	@Override
	public void setParentMenu(final String name)
	{
	}

	@Override
	public String getInternalName()
	{
		return "";
	}

	@Override
	public void setInternalName(final String name)
	{

	}

	@Override
	public boolean isActivationString(final String word)
	{
		return false;
	}

	@Override
	public boolean isDeActivationString(final String word)
	{
		return false;
	}

	@Override
	public boolean isCommandString(final String word, final boolean isActive)
	{
		return false;
	}

	@Override
	public TechType getTechType()
	{
		return TechType.PERSONAL_SOFTWARE;
	}

	protected SWServices[] getProvidedServices()
	{
		return new SWServices[0];
	}

	protected SWServices[] getRequiredServices()
	{
		return new SWServices[0];
	}

	protected SWServices[] getAppreciatedServices()
	{
		return new SWServices[0];
	}

	@Override
	public String getSettings()
	{
		return miscText;
	}

	@Override
	public void setSettings(final String var)
	{
		miscText=var;
	}

	@Override
	public String getActivationMenu()
	{
		return "";
	}

	@Override
	public String getCurrentScreenDisplay()
	{
		return currentScreen;
	}

	public void setCurrentScreenDisplay(final String msg)
	{
		this.currentScreen=msg;
	}

	@Override
	public String getScreenMessage()
	{
		synchronized(nextMsg)
		{
			final String msg=nextMsg.toString();
			nextMsg.setLength(0);
			return msg;
		}
	}

	@Override
	public void addScreenMessage(final String msg)
	{
		synchronized(nextMsg)
		{
			nextMsg.append(msg).append("\n\r");
		}
	}

	protected void forceUpMenu()
	{
		if((container() instanceof Computer)&&(((Computer)container()).getActiveMenu().equals(getInternalName())))
			((Computer)container()).setActiveMenu(getParentMenu());
	}

	protected void forceNewMessageScan()
	{
		if(container() instanceof Computer)
			((Computer)container()).forceReadersSeeNew();
	}

	protected void forceNewMenuRead()
	{
		if(container() instanceof Computer)
			((Computer)container()).forceReadersMenu();
	}

	protected boolean checkActivate(final MOB mob, final String message)
	{
		return true;
	}

	protected boolean checkDeactivate(final MOB mob, final String message)
	{
		return true;
	}

	protected boolean checkTyping(final MOB mob, final String message)
	{
		return true;
	}

	protected boolean checkPowerCurrent(final int value)
	{
		return true;
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(msg.isTarget(CMMsg.MASK_CNTRLMSG))
					return true;
				else
				if(!checkActivate(msg.source(),msg.targetMessage()))
					return false;
				break;
			case CMMsg.TYP_DEACTIVATE:
				if(!checkDeactivate(msg.source(),msg.targetMessage()))
					return false;
				break;
			case CMMsg.TYP_WRITE:
				if(!checkTyping(msg.source(),msg.targetMessage()))
					return false;
				break;
			case CMMsg.TYP_POWERCURRENT:
				if(!checkPowerCurrent(msg.value()))
					return false;
				break;
			}
		}
		return super.okMessage(host,msg);
	}

	protected Computer getMyComputer()
	{
		if((owner() instanceof Room)
		&&(container() instanceof Computer))
			return (Computer)container();
		return null;
	}

	protected Set<Computer> getResponseComputers(final Software SW)
	{
		final Set<Computer> puters;
		if((SW.owner() instanceof Room)
		&&(SW instanceof Item)
		&&(((Item)SW).container() instanceof Computer))
			puters=new XHashSet<Computer>((Computer)((Item)SW).container());
		else
			puters=getPeerComputers();
		return puters;
	}

	protected Set<Computer> getPeerComputers()
	{
		final Set<Computer> puters=new HashSet<Computer>();
		final Computer cC=getMyComputer();
		if((cC==null)||(circuitKey==null)||(circuitKey.length()==0))
			return puters;
		final List<Electronics> electronics=CMLib.tech().getMakeRegisteredElectronics(circuitKey);
		for(final Electronics E : electronics)
		{
			if((E instanceof Computer)
			&&(!puters.contains(E)))
				puters.add((Computer)E);
		}
		return puters;
	}

	protected List<Room> getPeerComputingRooms()
	{
		final List<Room> rooms=new LinkedList<Room>();
		final Set<Computer> puters = getPeerComputers();
		if(puters.size()==0)
			return rooms;
		final WorldMap map=CMLib.map();
		for(final Computer C : puters)
		{
			final Room R=map.roomLocation(C);
			if((R!=null)
			&&(!rooms.contains(R)))
				rooms.add(R);
		}
		return rooms;
	}

	protected List<String[]> doServiceTransaction(final SWServices service, final String[] parms)
	{
		final List<String[]> lst = new ArrayList<String[]>();
		final Set<Software> sws = svcs.get(service);
		if(sws == null)
			return lst;
		for(final Software SW : sws)
		{
			final MOB factoryMOB = CMClass.getFactoryMOB(name(), 1, CMLib.map().roomLocation(this));
			try
			{
				final String code=TechCommand.SWSVCREQ.makeCommand(service,parms);
				final CMMsg msg=CMClass.getMsg(factoryMOB, SW, this,
												CMMsg.NO_EFFECT, null,
												CMMsg.MSG_ACTIVATE|CMMsg.MASK_ALWAYS|CMMsg.MASK_CNTRLMSG, code,
												CMMsg.NO_EFFECT, null);
				sendServiceMsg(factoryMOB, getResponseComputers(SW), msg);
				if((msg.trailerMsgs()!=null)
				&&(msg.trailerMsgs().iterator().hasNext()))
				{
					for(final CMMsg rmsg : msg.trailerMsgs())
					{
						if((rmsg.targetMinor()==CMMsg.TYP_ACTIVATE)
						&&(rmsg.targetMajor(CMMsg.MASK_CNTRLMSG)))
						{
							final TechCommand command=TechCommand.findCommand(msg.targetMessage());
							if(command == TechCommand.SWSVCRES)
							{
								final Object[] rparms=(command != null)?command.confirmAndTranslate(msg.targetMessage()):null;
								if((rparms!=null)
								&&(rparms.length>0)
								&&(rparms[0] == service))
								{
									final String[] args=new String[rparms.length-1];
									for(int i=1;i<rparms.length;i++)
										args[i-1]=rparms[i].toString();
									lst.add(args);
								}
							}
						}
					}
				}
			}
			finally
			{
				factoryMOB.destroy();
			}
		}
		return lst;
	}

	protected void sendServiceMsg(final MOB mob, final Set<Computer> puters, final CMMsg msg)
	{
		final WorldMap map = CMLib.map();
		for(final Computer C : puters)
		{
			final Room R = map.roomLocation(C);
			final CMMsg msg2=(CMMsg)msg.copyOf();
			msg2.setTarget(C);
			if((R!=null)
			&&(R.okMessage(mob, msg2)))
				R.send(mob, msg2);
		}
	}

	protected void doServiceRequests(final MOB mob)
	{
		final CMMsg msg=CMClass.getMsg(mob, null, this,
				CMMsg.NO_EFFECT, null,
				CMMsg.MSG_ACTIVATE|CMMsg.MASK_ALWAYS|CMMsg.MASK_CNTRLMSG, null,
				CMMsg.NO_EFFECT, null);
		final Set<Computer> puters = this.getPeerComputers();
		this.svcs.clear();
		final String[] parm = new String[] { "PLEASE" };
		for(final SWServices service : getProvidedServices())
		{
			final String code=TechCommand.SWSVCALLOW.makeCommand(service);
			msg.setTargetMessage(code);
			sendServiceMsg(mob,puters,msg);
		}
		for(final SWServices service : getRequiredServices())
		{
			final String code=TechCommand.SWSVCNEED.makeCommand(service,parm);
			msg.setTargetMessage(code);
			sendServiceMsg(mob,puters,msg);
		}
		for(final SWServices service : getAppreciatedServices())
		{
			final String code=TechCommand.SWSVCNEED.makeCommand(service,parm);
			msg.setTargetMessage(code);
			sendServiceMsg(mob,puters,msg);
		}
		for(final SWServices service : getRequiredServices())
		{
			if(!svcs.containsKey(service))
			{
				addScreenMessage(L("Software @x1 failure: no @x2 service found.",name(),service.name().toLowerCase()));
				forceNewMessageScan();
				final CMMsg msg2=CMClass.getMsg(mob, this, null,
						CMMsg.NO_EFFECT, null,
						CMMsg.MSG_DEACTIVATE, null,
						CMMsg.NO_EFFECT, null);
				if(okMessage(mob, msg2))
				{
					executeMsg(mob, msg2);
					if(owner() instanceof Computer)
						((Computer)owner()).setActiveMenu("");
				}
				break;
			}
		}
	}

	protected void onActivate(final MOB mob, final String message)
	{
		doServiceRequests(mob);
	}

	protected void onDeactivate(final MOB mob, final String message)
	{

	}

	protected void onTyping(final MOB mob, final String message)
	{

	}

	protected void onPowerCurrent(final int value)
	{
		if((value > 0) // >0 means its getting power because its active
		&&(!isActivated))
		{
			final MOB factoryMOB = CMClass.getFactoryMOB(name(), 1, CMLib.map().roomLocation(this));
			try
			{
				isActivated=true;
				onActivate(factoryMOB, null);
			}
			finally
			{
				factoryMOB.destroy();
			}
		}
	}

	protected void provideService(final SWServices service, final Software S, final String[] parms, final CMMsg msg)
	{
		// if you get a request, and can provide, then provide

	}

	protected void sendSoftwareRespMsg(final Software S, final CMMsg msg)
	{
		if((S.owner() instanceof Room)
		&&(((Room)S.owner()).okMessage(msg.source(), msg)))
			((Room)S.owner()).send(msg.source(), msg);
		else
		if(S.okMessage(msg.source(), msg))
			S.executeMsg(msg.source(), msg);
	}

	protected void handleServices(final Environmental host, final CMMsg msg)
	{
		if((!(msg.tool() instanceof Software))
		||(msg.tool()==this))
			return;
		if((getProvidedServices().length==0)
		&&(getRequiredServices().length==0)
		&&(getAppreciatedServices().length==0))
			return;
		final Software SW = (Software)msg.tool();
		final TechCommand command=TechCommand.findCommand(msg.targetMessage());
		final Object[] parms=(command != null)?command.confirmAndTranslate(msg.targetMessage()):null;
		if((command!=null)
		&&(parms!=null)
		&&(parms.length>0)
		&&(parms[0] instanceof SWServices))
		{
			final SWServices service = (SWServices)parms[0];
			final String[] args=(parms.length>1)?(String[])parms[1]:new String[0];
			switch(command)
			{
			case SWSVCALLOW:
				if(CMParms.contains(getRequiredServices(), service)
				||CMParms.contains(getAppreciatedServices(), service))
				{
					if(!svcs.containsKey(service))
						svcs.put(service, new SHashSet<Software>());
					svcs.get(service).add(SW);
				}
				break;
			case SWSVCNEED:
				if(CMParms.contains(getProvidedServices(), service))
				{
					final CMMsg msg2=((CMMsg)msg.copyOf()).setTool(this).setTarget(SW);
					final String code=TechCommand.SWSVCALLOW.makeCommand(service);
					msg2.setTargetMessage(code);
					if(owner() == SW.owner())
						msg.addTrailerMsg(msg2);
					else
						sendSoftwareRespMsg(SW,msg2);
				}
				break;
			case SWSVCREQ: // request
				provideService(service, SW, args, msg);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				if(msg.isTarget(CMMsg.MASK_CNTRLMSG))
					handleServices(host,msg);
				else
				if(!isActivated)
				{
					isActivated=true;
					onActivate(msg.source(),msg.targetMessage());
				}
				break;
			case CMMsg.TYP_DEACTIVATE:
				if(isActivated)
				{
					isActivated=false;
					onDeactivate(msg.source(),msg.targetMessage());
				}
				break;
			case CMMsg.TYP_WRITE:
			case CMMsg.TYP_REWRITE:
				onTyping(msg.source(),msg.targetMessage());
				break;
			case CMMsg.TYP_POWERCURRENT:
				onPowerCurrent(msg.value());
				break;
			}
		}
		super.executeMsg(host, msg);
	}

	public String display(final long d)
	{
		return CMLib.english().sizeDescShort(d);
	}

	public String display(final long[] coords)
	{
		return CMLib.english().coordDescShort(coords);
	}

	public String display(final double[] dir)
	{
		return CMLib.english().directionDescShortest(dir);
	}

	public String displayPerSec(final long speed)
	{
		return CMLib.english().speedDescShort(speed);
	}

	@Override
	public String getManufacturerName()
	{
		return manufacturer;
	}

	@Override
	public void setManufacturerName(final String name)
	{
		cachedManufact = null;
		if (name != null)
			manufacturer = name;
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

	protected String trimColorsAndTrim(String s)
	{
		s=s.trim();
		while((s.length()>1)
		&&(s.charAt(s.length()-2)=='^'))
			s=s.substring(0,s.length()-2).trim();
		return s;
	}

}
