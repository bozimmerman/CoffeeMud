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
import java.lang.ref.WeakReference;
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

	public Manufacturer defaultManufacturer=null; // must always be DefaultManufacturer, w/o changes.

	public final Map<String,Manufacturer> manufacturers = new SHashtable<String,Manufacturer>();

	public final Map<String,LinkedList<WeakReference<Electronics>>> sets=new Hashtable<String,LinkedList<WeakReference<Electronics>>>();

	public final static List<PowerGenerator> emptyGeneratorList=new ArrayList<PowerGenerator>();

	public final AtomicInteger nextKey = new AtomicInteger(0);

	private TickClient serviceClient=null;

	protected CMMsg powerMsg = null;

	public void initializeClass()
	{
		super.initializeClass();
		loadAllManufacturers();
	}
	
	public synchronized String registerElectrics(final Electronics E, final String oldKey)
	{
		final ItemPossessor possessor=(E==null)?null:E.owner();
		if((E != null) && (possessor instanceof Room))
		{
			Room R=(Room)possessor;
			String newKey;
			if(R.getArea() instanceof SpaceShip)
			{
				newKey=R.getArea().Name();
				String registryNum=R.getArea().getBlurbFlag("REGISTRY");
				if(registryNum!=null) 
					newKey+=registryNum;
			}
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
				unregisterElectronics(E,oldKey);
			}
			LinkedList<WeakReference<Electronics>> set=sets.get(newKey);
			if(set==null)
			{
				set=new LinkedList<WeakReference<Electronics>>();
				sets.put(newKey, set);
			}
			set.add(new WeakReference<Electronics>(E));
			return newKey;
		}
		return null;
	}

	public synchronized List<Electronics> getMakeRegisteredElectronics(String key)
	{
		LinkedList<WeakReference<Electronics>> set=sets.get(key);
		LinkedList<Electronics> list=new LinkedList<Electronics>();
		if(set==null)
			return list;
		for(WeakReference<Electronics> e : set)
			if(e.get()!=null)
				list.add(e.get());
		return list;
	}
	
	public synchronized List<String> getMakeRegisteredKeys()
	{
		List<String> keys=new Vector<String>(sets.size());
		keys.addAll(sets.keySet());
		return keys;
	}
	
	public synchronized void unregisterElectronics(final Electronics E, final String oldKey)
	{
		if((oldKey!=null)&&(E!=null))
		{
			LinkedList<WeakReference<Electronics>> oldSet=sets.get(oldKey);
			if(oldSet!=null)
			{
				for(Iterator<WeakReference<Electronics>> e=oldSet.iterator();e.hasNext();)
				{
					WeakReference<Electronics> w=e.next();
					if(w.get()==E)
					{
						e.remove();
						if(oldSet.size()==0)
							sets.remove(oldSet);
						return;
					}
				}
			}
		}
	}
	
	public TickClient getServiceClient() { return serviceClient;}
	protected STreeMap<PowerGenerator,Pair<List<PowerSource>,List<Electronics>>> currents 
													= new STreeMap<PowerGenerator,Pair<List<PowerSource>,List<Electronics>>>(); 
	
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
		manufacturers.clear();
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
			double totalPowerNeeded=0.0;
			for(Electronics E : panels)
				totalPowerNeeded+=((E.powerNeeds()<=0)?1.0:E.powerNeeds());
			if(totalPowerNeeded>0.0)
			{
				for(Electronics E : panels)
				{
					powerMsg.setTarget(E);
					int powerToTake=0;
					if(remainingPowerToDistribute>0)
					{
						double pctToTake=CMath.div(((E.powerNeeds()<=0)?1:E.powerNeeds()),totalPowerNeeded);
						powerToTake=(int)Math.round(pctToTake * remainingPowerToDistribute);
						if(powerToTake<1)
							powerToTake=1;
					}
					powerMsg.setValue(powerToTake);
					final Room R=CMLib.map().roomLocation(E);
					if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
						R.send(powerMsg.source(), powerMsg);
					remainingPowerToDistribute-=(powerMsg.value()<0)?powerToTake:(powerToTake-powerMsg.value());
				}
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
			LinkedList<WeakReference<Electronics>> rawSet=sets.get(key);
			if(rawSet!=null)
			{
				for(Iterator<WeakReference<Electronics>> w=rawSet.iterator(); w.hasNext(); )
				{
					WeakReference<Electronics> W=w.next();
					Electronics E=W.get();
					if(E instanceof PowerGenerator)
						generators.add((PowerGenerator)E);
					else
					if(E instanceof PowerSource)
						batteries.add((PowerSource)E);
					else
					if(E!=null)
						panels.add(E);
					else
						w.remove();
				}
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
							LinkedList<WeakReference<Electronics>> rawSet=sets.get(key);
							if((rawSet!=null) && (rawSet.size()>0) && (rawSet.getLast().get() != battery))
							{
								for(Iterator<WeakReference<Electronics>> w=rawSet.iterator(); w.hasNext(); )
								{
									WeakReference<Electronics> W=w.next();
									if(W.get()==battery)
									{
										w.remove();
										break;
									}
								}
								rawSet.addLast(new WeakReference<Electronics>(battery));
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
	
	public Manufacturer getDefaultManufacturer()
	{
		if(defaultManufacturer==null)
			defaultManufacturer=(Manufacturer)CMClass.getCommon("DefaultManufacturer");
		return defaultManufacturer;
	}
	
	public void addManufacturer(Manufacturer manufacturer)
	{
		if(manufacturer==null) return;
		manufacturers.put(manufacturer.name().toUpperCase().trim(), manufacturer);
		saveAllManufacturers();
	}
	
	public void delManufacturer(Manufacturer manufacturer)
	{
		if(manufacturer==null) return;
		Manufacturer found=getManufacturer(manufacturer.name());
		if(found==manufacturer)
			manufacturers.remove(manufacturer.name().toUpperCase().trim());
		saveAllManufacturers();
	}
	
	public void updateManufacturer(Manufacturer manufacturer)
	{
		if(manufacturer==null) return;
		Manufacturer found=getManufacturer(manufacturer.name());
		if((found==null)||(found!=manufacturer))
		{
			for(String manName : manufacturers.keySet())
				if(manufacturers.get(manName)==manufacturer)
				{
					manufacturers.remove(manName);
					break;
				}
			addManufacturer(manufacturer);
		}
		saveAllManufacturers();
	}
	
	public Manufacturer getManufacturer(String name)
	{
		if(name==null) return null;
		return manufacturers.get(name.toUpperCase().trim());
	}
	
	public Iterator<Manufacturer> manufacterers() 
	{ 
		return new ReadOnlyIterator<Manufacturer>(manufacturers.values().iterator());
	}

	protected String getManufacturersFilename()
	{
		return "/resources/tech/manufacturers.xml";
	}
	
	protected synchronized void saveAllManufacturers()
	{
		final String filename=getManufacturersFilename();
		CMFile xmlFile=new CMFile(filename, null, false, true);
		if(!xmlFile.exists())
			xmlFile=new CMFile("::"+filename, null, false, true);
		final StringBuilder xmlStr=new StringBuilder("<MANUFACTURERS>");
		for(Manufacturer man : manufacturers.values())
			if(man != defaultManufacturer)
				xmlStr.append("<MANUFACTURER>").append(man.getXml()).append("</MANUFACTURER>");
		xmlStr.append("</MANUFACTURERS>");
		xmlFile.saveText(xmlStr.toString());
	}

	protected void loadAllManufacturers()
	{
		final String filename=getManufacturersFilename();
		CMFile xmlFile=new CMFile(filename, null, false, true);
		manufacturers.clear();
		if(xmlFile.exists() && xmlFile.canRead())
		{
			List<XMLLibrary.XMLpiece> xDoc=CMLib.xml().parseAllXML(xmlFile.text());
			List<XMLLibrary.XMLpiece> xMans=new SLinkedList<XMLLibrary.XMLpiece>();
			for(XMLLibrary.XMLpiece x : xDoc)
				if(x.tag.equalsIgnoreCase("MANUFACTURER"))
					xMans.add(x);
				else
				if(x.tag.equalsIgnoreCase("MANUFACTURERS"))
					xMans.addAll(x.contents);
			for(XMLLibrary.XMLpiece x : xMans)
			{
				Manufacturer man =(Manufacturer)CMClass.getCommon("DefaultManufacturer");
				man.setXml(x.value);
				addManufacturer(man);
			}
		}
	}
}
