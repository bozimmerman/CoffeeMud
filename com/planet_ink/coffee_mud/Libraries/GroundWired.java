package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
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
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.Electronics.PowerGenerator;
import com.planet_ink.coffee_mud.Items.interfaces.Electronics.PowerSource;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.*;
import com.planet_ink.coffee_mud.core.collections.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.*;
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
public class GroundWired extends StdLibrary implements TechLibrary
{
	public String ID(){return "GroundWired";}
	
	public final Map<String,LinkedList<Electronics>> sets=new Hashtable<String,LinkedList<Electronics>>();
	
	public final static List<PowerGenerator> emptyGeneratorList=new ArrayList<PowerGenerator>();
	
	public final AtomicInteger nextKey = new AtomicInteger(0);
	
	public synchronized String registerElectrics(final Electronics E, final String oldKey)
	{
		final ItemPossessor possessor=(E==null)?null:E.owner();
		if((E != null) && (possessor instanceof Room))
		{
			Room R=(Room)possessor;
			String newKey;
			if(R.getArea() instanceof SpaceShip)
				newKey=R.getArea().Name();
			else
			{
				LandTitle title = CMLib.law().getLandTitle(R);
				if(title != null)
					newKey=title.getUniqueLotID();
				else
					newKey=CMLib.map().getExtendedRoomID(R);
			}
			if(oldKey!=null)
			{
				if(newKey.equals(oldKey))
					return oldKey;
				final List<Electronics> oldSet=sets.get(oldKey);
				if(oldSet!=null)
				{
					oldSet.remove(E);
					if(oldSet.size()==0)
						sets.remove(oldSet);
				}
			}
			LinkedList<Electronics> set=sets.get(newKey);
			if(set==null)
			{
				set=new LinkedList<Electronics>();
				sets.put(newKey, set);
			}
			set.add(E);
			return newKey;
		}
		return null;
	}
	
	public synchronized void unregisterElectronics(final Electronics E, final String oldKey)
	{
		if((oldKey!=null)&&(E!=null))
		{
			final List<Electronics> oldSet=sets.get(oldKey);
			if(oldSet!=null)
			{
				oldSet.remove(E);
				if(oldSet.size()==0)
					sets.remove(oldSet);
			}
		}
	}
	
	private TickClient serviceClient=null;
	public TickClient getServiceClient() { return serviceClient;}
	protected STreeMap<PowerGenerator,Pair<List<PowerSource>,List<Electronics>>> currents 
													= new STreeMap<PowerGenerator,Pair<List<PowerSource>,List<Electronics>>>(); 
	protected CMMsg powerMsg = null;
	
	protected CMMsg getPowerMsg(int powerAmt)
	{
		if(powerMsg==null)
		{
			MOB powerMOB=CMClass.getMOB("StdMOB");
			powerMOB.baseCharStats().setMyRace(CMClass.getRace("ElectricalElemental"));
			powerMOB.setSavable(false);
			powerMOB.setLocation(CMLib.map().getRandomRoom());
			powerMOB.recoverCharStats();
			powerMsg=CMClass.getMsg(powerMOB, CMMsg.MSG_POWERCURRENT, null);
		}
		powerMsg.setValue(powerAmt);
		return powerMsg;
	}
	
	public boolean activate()
	{
		if(serviceClient==null)
			serviceClient=CMLib.threads().startTickDown(new Tickable(){
				private long tickStatus=Tickable.STATUS_NOT;
				@Override public String ID() { return "THWired"+Thread.currentThread().getThreadGroup().getName().charAt(0); }
				@Override public CMObject newInstance() { return this; }
				@Override public CMObject copyOf() { return this; }
				@Override public void initializeClass() { }
				@Override public int compareTo(CMObject o) { return (o==this)?0:1; }
				@Override public String name() { return ID(); }
				@Override public long getTickStatus() { return tickStatus; }
				@Override public boolean tick(Tickable ticking, int tickID) {
					if(!CMSecurity.isDisabled(CMSecurity.DisFlag.ELECTRICTHREAD))
					{
						isDebugging=CMSecurity.isDebugging(DbgFlag.UTILITHREAD);
						tickStatus=Tickable.STATUS_ALIVE;
						runElectricCurrents();
						setThreadStatus(serviceClient,"sleeping");
					}
					tickStatus=Tickable.STATUS_NOT;
					return true;
				}
			}, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, CMProps.getTickMillis(), 1);
		return true;
	}
	
	public boolean shutdown() 
	{
		sets.clear();
		if((serviceClient!=null)&&(serviceClient.getClientObject()!=null))
		{
			CMLib.threads().deleteTick(serviceClient.getClientObject(), Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}

	protected void processElectricCurrents(final List<PowerGenerator> generators, final List<PowerSource> batteries, final List<Electronics> panels) throws Exception
	{
		CMMsg powerMsg=getPowerMsg(0);
		for(PowerGenerator E : generators)
		{
			powerMsg.setTarget(E);
			powerMsg.setValue(0);
			final Room R=CMLib.map().roomLocation(E);
			if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
				R.send(powerMsg.source(), powerMsg);
		}
		long remainingPowerToDistribute=0;
		long availablePowerToDistribute=0;
		for(PowerGenerator G : generators)
			if(G.activated())
			{
				availablePowerToDistribute+=G.powerRemaining();
				G.setPowerRemaining(0);
			}
		for(PowerSource B : batteries)
			if(B.activated())
				availablePowerToDistribute+=B.powerRemaining();
		if(availablePowerToDistribute==0)
		{
			for(Electronics E : panels)
			{
				powerMsg.setTarget(E);
				powerMsg.setValue(0);
				final Room R=CMLib.map().roomLocation(E);
				if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
					R.send(powerMsg.source(), powerMsg);
			}
			for(PowerSource E : batteries)
			{
				powerMsg.setTarget(E);
				powerMsg.setValue(0);
				final Room R=CMLib.map().roomLocation(E);
				if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
					R.send(powerMsg.source(), powerMsg);
			}
		}
		else
		{
			remainingPowerToDistribute=availablePowerToDistribute;
			int panelsLeft=panels.size();
			for(Electronics E : panels)
			{
				powerMsg.setTarget(E);
				int amountToDistribute=(int)(remainingPowerToDistribute/panelsLeft);
				powerMsg.setValue(amountToDistribute<0?0:amountToDistribute);
				final Room R=CMLib.map().roomLocation(E);
				if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
					R.send(powerMsg.source(), powerMsg);
				remainingPowerToDistribute-=(powerMsg.value()<0)?amountToDistribute:(amountToDistribute-powerMsg.value());
				panelsLeft--;
			}
			int batteriesLeft=batteries.size();
			for(PowerSource E : batteries)
			{
				powerMsg.setTarget(E);
				int amountToDistribute=(int)(remainingPowerToDistribute/batteriesLeft);
				powerMsg.setValue(amountToDistribute<0?0:amountToDistribute);
				final Room R=CMLib.map().roomLocation(E);
				if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
					R.send(powerMsg.source(), powerMsg);
				batteriesLeft--;
				remainingPowerToDistribute-=(powerMsg.value()<0)?amountToDistribute:(amountToDistribute-powerMsg.value());
			}
			if(generators.size()>0)
			{
				int amountLeftOver=(int)((availablePowerToDistribute-remainingPowerToDistribute)/generators.size());
				for(PowerGenerator G : generators)
					if(G.activated())
						G.setPowerRemaining(amountLeftOver>G.powerCapacity()?G.powerCapacity():amountLeftOver);
			}
		}
	}

	protected void fillElectronicLists(final String key, final List<PowerGenerator> generators, final List<PowerSource> batteries, final List<Electronics> panels)
	{
		synchronized(this)
		{
			List<Electronics> rawSet=sets.get(key);
			if(rawSet!=null)
			{
				for(Electronics E : rawSet)
					if(E instanceof PowerGenerator)
						generators.add((PowerGenerator)E);
					else
					if(E instanceof PowerSource)
						batteries.add((PowerSource)E);
					else
						panels.add(E);
			}
		}
	}
	
	protected void runElectricCurrent(final String key)
	{
		try
		{
			final List<PowerGenerator> generators = new LinkedList<PowerGenerator>();
			final List<PowerSource> batteries = new LinkedList<PowerSource>();
			final List<Electronics> panels = new LinkedList<Electronics>();
			fillElectronicLists(key,generators,batteries,panels);
			processElectricCurrents(generators, batteries, panels);
		}
		catch(Exception e)
		{
			Log.errOut("GroundWired",e);
		}
	}

	public boolean seekBatteryPower(final Electronics E, final String key)
	{
		final List<PowerGenerator> generators = new LinkedList<PowerGenerator>();
		final List<PowerSource> batteries = new LinkedList<PowerSource>();
		final List<Electronics> panels = new LinkedList<Electronics>();
		fillElectronicLists(key,generators,batteries,panels);
		
		PowerSource battery = null;
		final Room locR=CMLib.map().roomLocation(E);
		for(final PowerSource S : batteries)
		{
			if((!S.activated())&&(S.powerRemaining()>0))
			{
				final MOB M=CMLib.map().getFactoryMOB(locR);
				final CMMsg activateMsg = CMClass.getMsg(M, S, null, CMMsg.MASK_ALWAYS|CMMsg.MASK_CNTRLMSG|CMMsg.MSG_ACTIVATE,null);
				if(locR.okMessage(M, activateMsg))
				{
					locR.send(M, activateMsg);
					if(S.activated())
					{
						battery=S;
						break;
					}
					else
					{
						synchronized(this)
						{
							LinkedList<Electronics> rawSet=sets.get(key);
							if((rawSet!=null) && (rawSet.size()>0) && (rawSet.getLast() != battery))
							{
								rawSet.remove(battery);
								rawSet.addLast(battery);
							}
						}
					}
				}
			}
		}
		if(battery==null)
		{
			return false;
		}
		try
		{
			final List<Electronics> finalPanel=new XVector<Electronics>(E);
			final List<PowerSource> finalBatteries=new XVector<PowerSource>(battery);
			processElectricCurrents(emptyGeneratorList, finalBatteries, finalPanel);
			return true;
		}
		catch(Exception e)
		{
			Log.errOut("GroundWired",e);
			return false;
		}
	}
	
	protected void runElectricCurrents()
	{
		setThreadStatus(serviceClient,"pushing electric currents");

		List<String> keys;
		synchronized(this)
		{
			keys=new XVector<String>(sets.keySet());
		}
		for(String key : keys)
		{
			runElectricCurrent(key);
		}
		setThreadStatus(serviceClient,"sleeping");
	}
}
