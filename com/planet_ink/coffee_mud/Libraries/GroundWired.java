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
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
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
   Copyright 2012-2025 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "GroundWired";
	}

	protected final static class ElecList extends LinkedList<WeakReference<Electronics>>
	{
		private static final long serialVersionUID = -8313278188033918238L;

	}

	protected final static class WireHookups
	{
		ElecList set = new ElecList();
		Map<TechCommand, ElecList> listeners = new Hashtable<TechCommand, ElecList>();
	}

	protected Manufacturer defaultManufacturer=null; // must always be DefaultManufacturer, w/o changes.

	protected final Map<String,Manufacturer> manufacturers = new SHashtable<String,Manufacturer>();

	protected final Map<String,WireHookups> sets=new Hashtable<String,WireHookups>();

	//protected final Map<PowerGenerator,Pair<List<PowerSource>,List<Electronics>>> currents	= new STreeMap<PowerGenerator,Pair<List<PowerSource>,List<Electronics>>>();

	protected final static List<PowerGenerator> emptyGeneratorList=new ArrayList<PowerGenerator>();

	protected final AtomicInteger nextKey = new AtomicInteger(0);

	public int globalTechLevel = 0;
	public long globalTechReachedOn=0;

	protected CMMsg powerMsg = null;

	@Override
	public void initializeClass()
	{
		super.initializeClass();
		loadAllManufacturers();
		globalTechLevel=CMath.s_int(Resources.getPropResource("TECH", "GLOBALLEVEL"));
		globalTechReachedOn=CMath.s_long(Resources.getPropResource("TECH", "GLOBALREACHEDON"));
	}

	@Override
	public int getGlobalTechLevel()
	{
		return globalTechLevel;
	}

	@Override
	public int getRandomGlobalTechLevel()
	{
		return  CMLib.dice().rollLowBiased(1, 10, globalTechLevel-1);
	}

	protected void bumpTechLevel()
	{
		globalTechLevel++;
		Resources.setPropResource("TECH", "GLOBALLEVEL",""+globalTechLevel);
		Resources.setPropResource("TECH", "GLOBALREACHEDON","0");
	}

	@Override
	public void fixItemTechLevel(final Technical I, final int newTechLevel)
	{
		if((!CMSecurity.isDisabled(CMSecurity.DisFlag.TECHLEVEL))
		&& (I.getManufacturerName().equalsIgnoreCase("RANDOM")))
		{
			I.setManufacturerName(I.getFinalManufacturer().name());
			if(newTechLevel >= 0)
				I.setTechLevel(newTechLevel);
			else
				I.setTechLevel(getRandomGlobalTechLevel());
			final String oldName=I.Name();
			String newName=CMLib.english().startWithAorAn(I.getFinalManufacturer().name()+" "+CMLib.english().removeArticleLead(oldName));
			I.setName(newName);
			final String[] marks=CMProps.getListFileStringList(CMProps.ListFile.TECH_LEVEL_NAMES);
			if(marks.length>0)
				newName+=" "+marks[I.techLevel()%marks.length];
			if(I.displayText().indexOf(oldName)>0)
				I.setDisplayText(CMStrings.replaceAll(I.displayText(), oldName, newName));
			else
				I.setDisplayText(L("@x1 is here.",newName));
		}
	}

	@Override
	public String getElectronicsKey(final CMObject o)
	{
		if((o instanceof SpaceShip)&&(o instanceof Item))
			return getElectronicsKey(((SpaceShip)o).getArea());
		else
		if(o instanceof Electronics)
			return getElectronicsKey(((Electronics)o).owner());
		else
		if(o instanceof Area)
		{
			final Area A=(Area)o;
			String newKey=A.Name();
			final String registryNum=A.getBlurbFlag("REGISTRY");
			if(registryNum!=null)
				newKey+=registryNum;
			return newKey.toLowerCase();
		}
		else
		if(o instanceof Room)
		{
			final Room R=(Room)o;
			if(R.getArea() instanceof SpaceShip)
				return getElectronicsKey(R.getArea());
			final LandTitle title = CMLib.law().getLandTitle(R);
			if(title != null)
				return title.getUniqueLotID().toLowerCase();
			else
				return CMLib.map().getExtendedRoomID(R).toLowerCase();
		}
		return null;
	}

	@Override
	public synchronized String registerElectrics(final Electronics E, final String oldKey)
	{
		return registerElectrics(E, oldKey, null);
	}

	@Override
	public String registerElectrics(final Electronics E, final String oldKey, final TechCommand[] listenFors)
	{
		final boolean debugging = CMSecurity.isDebugging(DbgFlag.ELECTRICTHREAD);
		final ItemPossessor possessor=(E==null)?null:E.owner();
		if((E != null) && (possessor instanceof Room))
		{
			final Room R=(Room)possessor;
			final String newKey=getElectronicsKey(R);
			if(R.getArea() instanceof SpaceShip)
			{
				if(((SpaceShip)R.getArea()).getShipSpaceObject() instanceof LandTitle)
				{
					// if this is from a ship for sale, don't register, and go home.
					if(((LandTitle)((SpaceShip)R.getArea()).getShipSpaceObject()).getOwnerName().length()==0)
						return newKey;
				}
			}
			if(oldKey!=null)
			{
				if(newKey.equalsIgnoreCase(oldKey))
					return oldKey.toLowerCase();
				unregisterElectronics(E,oldKey);
			}
			WireHookups hookups;
			final WeakReference<Electronics> ref;
			synchronized(this)
			{
				hookups = sets.get(newKey);
				if(hookups==null)
				{
					hookups=new WireHookups();
					sets.put(newKey, hookups);
				}
				ref = new WeakReference<Electronics>(E);
				if(!hookups.set.contains(ref))
					hookups.set.add(ref);
			}
			if(listenFors != null)
			{
				for(final TechCommand command : listenFors)
				{
					ElecList refs = hookups.listeners.get(command);
					if(refs == null)
					{
						refs = new ElecList();
						hookups.listeners.put(command, refs);
					}
					synchronized(refs)
					{
						if(!refs.contains(ref))
							refs.add(ref);
					}
				}
			}
			if(debugging)
				Log.debugOut("GroundWired","Registered: "+E.Name()+": "+newKey+", was: "+oldKey);
			return newKey;
		}
		else
		if(debugging)
			Log.debugOut("GroundWired","Not registered: "+((E==null)?"null":E.Name())+": "+oldKey);
		return null;
	}

	@Override
	public synchronized List<Electronics> getMakeRegisteredElectronics(final String key)
	{
		final LinkedList<Electronics> list=new LinkedList<Electronics>();
		if(key == null)
			return list;
		final WireHookups set=sets.get(key.toLowerCase());
		if(set==null)
			return list;
		for(final WeakReference<Electronics> e : set.set)
		{
			if(e.get()!=null)
				list.add(e.get());
		}
		return list;
	}

	@Override
	public synchronized List<String> getMakeRegisteredKeys()
	{
		final List<String> keys=new Vector<String>(sets.size());
		keys.addAll(sets.keySet());
		return keys;
	}

	@Override
	public boolean okMessage(final String oldKey, final TechCommand command, final Environmental myHost, final CMMsg msg)
	{
		if((oldKey!=null)&&(command!=null))
		{
			ElecList refs = null;
			synchronized(this)
			{
				final WireHookups oldSet=sets.get(oldKey.toLowerCase());
				if(oldSet!=null)
					refs = oldSet.listeners.get(command);
			}
			if(refs != null)
			{
				synchronized(refs)
				{
					for(final WeakReference<Electronics> ref : refs)
					{
						final Electronics E = ref.get();
						if((E != null) && (!E.okMessage(myHost, msg)))
							return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final String oldKey, final TechCommand command, final Environmental myHost, final CMMsg msg)
	{
		if((oldKey!=null)&&(command!=null))
		{
			ElecList refs = null;
			synchronized(this)
			{
				final WireHookups oldSet=sets.get(oldKey.toLowerCase());
				if(oldSet!=null)
					refs = oldSet.listeners.get(command);
			}
			if(refs != null)
			{
				synchronized(refs)
				{
					for(final WeakReference<Electronics> ref : refs)
					{
						final Electronics E = ref.get();
						if(E != null)
							E.executeMsg(myHost, msg);
					}
				}
			}
		}
	}

	@Override
	public void unregisterElectronics(final Electronics E, final String oldKey)
	{
		if((oldKey!=null)&&(E!=null))
		{
			final WireHookups oldSet;
			WeakReference<Electronics> ref = null;
			synchronized(this)
			{
				oldSet=sets.get(oldKey.toLowerCase());
				if(oldSet!=null)
				{
					for(final Iterator<WeakReference<Electronics>> e=oldSet.set.iterator();e.hasNext();)
					{
						final WeakReference<Electronics> w=e.next();
						if(w.get()==E)
						{
							ref = w;
							if(CMSecurity.isDebugging(DbgFlag.ELECTRICTHREAD))
								Log.debugOut("GroundWired","Unegistered: "+E.Name());
							e.remove();
							break;
						}
					}
					if(oldSet.set.size()==0)
					{
						if(CMSecurity.isDebugging(DbgFlag.ELECTRICTHREAD))
							Log.debugOut("GroundWired","Unegistered: "+oldKey);
						sets.remove(oldKey);
						ref = null;
					}
				}
			}
			if((ref != null) && (oldSet != null))
			{
				for(final Iterator<TechCommand> e=oldSet.listeners.keySet().iterator();e.hasNext();)
				{
					final ElecList refs = oldSet.listeners.get(e.next());
					if(refs != null)
					{
						synchronized(refs)
						{
							refs.remove(ref);
						}
					}
				}
			}
		}
	}

	@Override
	public synchronized void unregisterAllElectronics(final String oldKey)
	{
		if(oldKey!=null)
		{
			final WireHookups oldSet=sets.get(oldKey.toLowerCase());
			if(oldSet!=null)
			{
				sets.remove(oldKey.toLowerCase());
				if(CMSecurity.isDebugging(DbgFlag.ELECTRICTHREAD))
					Log.debugOut("GroundWired","Unegistered: "+oldKey);
			}
		}
	}

	@Override
	public TickClient getServiceClient()
	{
		return serviceClient;
	}

	protected final static Iterator<Computer> emptyComputerIterator= new Iterator<Computer>()
	{
		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Computer next()
		{
			return null;
		}

		@Override
		public void remove()
		{
		}
	};

	protected final static Iterator<Room> emptyComputerRoomIterator= new Iterator<Room>()
	{
		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Room next()
		{
			return null;
		}

		@Override
		public void remove()
		{
		}
	};

	protected final static Filterer<WeakReference<Electronics>> computerFilterer=new Filterer<WeakReference<Electronics>>()
	{
		@Override
		public boolean passesFilter(final WeakReference<Electronics> obj)
		{
			return obj.get() instanceof Computer;
		}
	};

	protected final static Converter<WeakReference<Electronics>,Computer> computerConverter=new Converter<WeakReference<Electronics>,Computer>()
	{
		@Override
		public Computer convert(final WeakReference<Electronics> obj)
		{
			return (Computer) obj.get();
		}
	};

	protected final static Converter<Computer,Room> computerRoomConverter=new Converter<Computer,Room>()
	{
		@Override
		public Room convert(final Computer obj)
		{
			return CMLib.map().roomLocation(obj);
		}
	};

	@Override
	public synchronized Iterator<Computer> getComputers(final String key)
	{
		final WireHookups oldSet=sets.get(key.toLowerCase());
		if(oldSet==null)
			return emptyComputerIterator;
		return new ConvertingIterator<WeakReference<Electronics>,Computer>(
				new FilteredIterator<WeakReference<Electronics>>(oldSet.set.iterator(), computerFilterer),computerConverter);
	}

	@Override
	public synchronized Iterator<Room> getComputerRooms(final String key)
	{
		return new FilteredIterator<Room>(new ConvertingIterator<Computer,Room>(getComputers(key),computerRoomConverter), new Filterer<Room>()
		{
			private final Set<Room> done=new HashSet<Room>();

			@Override
			public boolean passesFilter(final Room obj)
			{
				if(done.contains(obj))
					return false;
				done.add(obj);
				return true;
			}
		});
	}

	protected CMMsg getPowerMsg(final int powerAmt)
	{
		if(powerMsg==null)
		{
			final MOB powerMOB=CMClass.getMOB("StdMOB");
			powerMOB.baseCharStats().setMyRace(CMClass.getRace("ElectricityElemental"));
			powerMOB.setSavable(false);
			final Room R=CMClass.getLocale("StdRoom");
			final Area A=CMClass.getAreaType("StdArea");
			A.setTheme(Area.THEME_TECHNOLOGY);
			R.setArea(A);
			powerMOB.setLocation(R);
			powerMOB.recoverCharStats();
			powerMsg=CMClass.getMsg(powerMOB, CMMsg.MSG_POWERCURRENT, null);
		}
		powerMsg.setValue(powerAmt);
		return powerMsg;
	}

	@Override
	public boolean activate()
	{
		if(!super.activate())
			return false;
		if(serviceClient==null)
		{
			name="THWired"+Thread.currentThread().getThreadGroup().getName().charAt(0);
			serviceClient=CMLib.threads().startTickDown(this, Tickable.TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK, CMProps.getTickMillis(), 1);
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		try
		{
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.ELECTRICTHREAD))
			{
				isDebugging=CMSecurity.isDebugging(DbgFlag.UTILITHREAD);
				tickStatus=Tickable.STATUS_ALIVE;
				runElectricCurrents();
			}
		}
		finally
		{
			tickStatus=Tickable.STATUS_NOT;
			setThreadStatus(serviceClient,"sleeping");
		}
		return true;
	}

	@Override
	public boolean shutdown()
	{
		sets.clear();
		manufacturers.clear();
		if(CMLib.threads().isTicking(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK))
		{
			CMLib.threads().deleteTick(this, TICKID_SUPPORT|Tickable.TICKID_SOLITARYMASK);
			serviceClient=null;
		}
		return true;
	}

	protected void processElectricCurrents(final String key, final List<PowerGenerator> generators, final List<PowerSource> batteries, final List<ElecPanel> panels) throws Exception
	{
		final boolean debugging = CMSecurity.isDebugging(DbgFlag.ELECTRICTHREAD);
		final CMMsg powerMsg=getPowerMsg(0);
		for(final PowerGenerator E : generators)
		{
			powerMsg.setTarget(E);
			powerMsg.setValue(0);
			final Room R=CMLib.map().roomLocation(E);
			if(R!=null)
			{
				powerMsg.source().setLocation(R);
				if(R.okMessage(powerMsg.source(), powerMsg))
					R.send(powerMsg.source(), powerMsg);
			}
		}
		long remainingPowerToDistribute=0;
		long availablePowerToDistribute=0;
		long availablePowerFromBatteries=0;
		for(final PowerGenerator G : generators)
		{
			if(G.activated())
			{
				availablePowerToDistribute+=G.powerRemaining();
				if(debugging)
					Log.debugOut("Current "+key+": Generator: "+G.Name()+" generated "+availablePowerToDistribute);
				G.setPowerRemaining(0);
			}
		}
		for(final PowerSource B : batteries)
		{
			if(B.activated())
			{
				if(debugging)
					Log.debugOut("Current "+key+": PowerSource: "+B.Name()+" generated "+B.powerRemaining());
				availablePowerToDistribute+=B.powerRemaining();
				availablePowerFromBatteries+=B.powerRemaining();
				B.setPowerRemaining(0);
			}
		}
		if(availablePowerToDistribute==0)
		{
			for(final ElecPanel E : panels)
			{
				powerMsg.setTarget(E);
				powerMsg.setValue(0);
				final Room R=CMLib.map().roomLocation(E);
				if(R!=null)
				{
					powerMsg.source().setLocation(R);
					if(R.okMessage(powerMsg.source(), powerMsg))
					{
						R.send(powerMsg.source(), powerMsg);
						if(debugging)
							Log.debugOut("Current "+key+": Panel: "+E.Name()+" emer current "+powerMsg.value());
					}
				}
			}
			for(final PowerSource E : batteries)
			{
				powerMsg.setTarget(E);
				powerMsg.setValue(0);
				final Room R=CMLib.map().roomLocation(E);
				if(R!=null)
				{
					powerMsg.source().setLocation(R);
					if(R.okMessage(powerMsg.source(), powerMsg))
					{
						R.send(powerMsg.source(), powerMsg);
						if(debugging)
							Log.debugOut("Current "+key+": PowerSource: "+E.Name()+" emer current "+powerMsg.value());
					}
				}
			}
		}
		else
		{
			remainingPowerToDistribute=availablePowerToDistribute;
			double totalPowerNeeded=0.0;
			for(final ElecPanel E : panels)
				totalPowerNeeded+=((E.powerNeeds()<=0)?1.0:E.powerNeeds());
			if(debugging)
				Log.debugOut("Current "+key+": All power needed: "+totalPowerNeeded);
			if(totalPowerNeeded>0.0)
			{
				for(final ElecPanel E : panels)
				{
					powerMsg.setTarget(E);
					int powerToTake=0;
					if(remainingPowerToDistribute>0)
					{
						final double pctToTake=CMath.div(((E.powerNeeds()<=0)?1:E.powerNeeds()),totalPowerNeeded);
						powerToTake=(int)Math.round(pctToTake * remainingPowerToDistribute);
						if(powerToTake<1)
							powerToTake=1;
					}
					powerMsg.setValue(powerToTake);
					final Room R=CMLib.map().roomLocation(E);
					if(R!=null)
					{
						powerMsg.source().setLocation(R);
						if(R.okMessage(powerMsg.source(), powerMsg))
						{
							R.send(powerMsg.source(), powerMsg);
							if(debugging)
								Log.debugOut("Current "+key+": Panel: "+E.Name()+": Power taken: "+(powerToTake -powerMsg.value()));
						}
					}
					remainingPowerToDistribute-=(powerMsg.value()<0)?powerToTake:(powerToTake-powerMsg.value());
				}
			}

			// first restore what was taken from batteries!
			long amountToGiveBackToBatteriesFreely = remainingPowerToDistribute;
			if(amountToGiveBackToBatteriesFreely > availablePowerFromBatteries)
				amountToGiveBackToBatteriesFreely = availablePowerFromBatteries;
			remainingPowerToDistribute -= amountToGiveBackToBatteriesFreely;
			boolean batteryStuffToDo=true;
			while(batteryStuffToDo)
			{
				batteryStuffToDo=false;
				int batteriesLeft=0;
				for(final PowerSource E : batteries)
				{
					if(E.activated() && E.powerRemaining() < E.powerCapacity())
						batteriesLeft++;
				}
				if(batteriesLeft>0)
				{
					for(final PowerSource E : batteries)
					{
						if(E.activated() && (E.powerRemaining() < E.powerCapacity()))
						{
							long amountToDistribute=(int)(amountToGiveBackToBatteriesFreely/batteriesLeft);
							if(amountToDistribute > (E.powerCapacity() - E.powerRemaining()))
								amountToDistribute = (E.powerCapacity() - E.powerRemaining());
							if(amountToDistribute>0)
							{
								E.setPowerRemaining(E.powerRemaining() + amountToDistribute);
								if(debugging)
									Log.debugOut("Current "+key+": Battery: "+E.Name()+": Power reimbursed: "+amountToDistribute+", now="+E.powerRemaining());
								amountToGiveBackToBatteriesFreely -= amountToDistribute;
								batteryStuffToDo=true;
							}
							batteriesLeft--;
						}
					}
				}
			}
			remainingPowerToDistribute += amountToGiveBackToBatteriesFreely;
			// then do any recharging
			int batteriesLeft=batteries.size();
			for(final PowerSource E : batteries)
			{
				powerMsg.setTarget(E);
				final int amountToDistribute=(int)(remainingPowerToDistribute/batteriesLeft);
				powerMsg.setValue(amountToDistribute<0?0:amountToDistribute);
				final Room R=CMLib.map().roomLocation(E);
				if(R!=null)
				{
					powerMsg.source().setLocation(R);
					if(R.okMessage(powerMsg.source(), powerMsg))
					{
						R.send(powerMsg.source(), powerMsg);
						if(debugging)
							Log.debugOut("Current "+key+": Battery: "+E.Name()+": Power charged: "+amountToDistribute+": "+powerMsg.value()+", now="+E.powerRemaining());
					}
				}
				batteriesLeft--;
				remainingPowerToDistribute-=(powerMsg.value()<0)?amountToDistribute:(amountToDistribute-powerMsg.value());
			}
			// finally, generators get whats left over
			if(generators.size()>0)
			{
				final int amountLeftOver=(int)((availablePowerToDistribute-remainingPowerToDistribute)/generators.size());
				for(final PowerGenerator G : generators)
				{
					if(G.activated())
					{
						G.setPowerRemaining(amountLeftOver>G.powerCapacity()?G.powerCapacity():amountLeftOver);
						if(debugging)
							Log.debugOut("Current "+key+": generator: "+G.Name()+": Power reimbursed: "+amountLeftOver+": "+G.powerRemaining());
					}
				}
			}
		}
	}

	protected Area fillCurrentLists(final String key, final List<PowerGenerator> generators, final List<PowerSource> batteries, final List<ElecPanel> panels)
	{
		Area areaLocation=null;
		synchronized(this)
		{
			final WireHookups rawSet=sets.get(key.toLowerCase());
			if(rawSet!=null)
			{
				final Set<Electronics> rawSetSet = new HashSet<Electronics>();
				for(final Iterator<WeakReference<Electronics>> w=rawSet.set.iterator(); w.hasNext(); )
				{
					final WeakReference<Electronics> W=w.next();
					final Electronics E=W.get();
					if(E==null)
						w.remove();
					else
						rawSetSet.add(E);
				}
				for(final Iterator<WeakReference<Electronics>> w=rawSet.set.iterator(); w.hasNext(); )
				{
					final WeakReference<Electronics> W=w.next();
					final Electronics E=W.get();
					if(E==null)
						w.remove();
					else
					{
						if((!(E instanceof TechComponent))||(((TechComponent)E).isInstalled()))
						{
							if(E instanceof PowerGenerator)
								generators.add((PowerGenerator)E);
							else
							if(E instanceof PowerSource)
								batteries.add((PowerSource)E);
							else
							if(E instanceof ElecPanel)
								panels.add((ElecPanel)E);
							else
							if(E.owner() instanceof ElecPanel)
							{
								final ElecPanel pI = (ElecPanel)E.owner();
								if(!rawSetSet.contains(pI))
								{
									rawSetSet.add((ElecPanel)E.owner());
									panels.add((ElecPanel)E.owner());
								}
							}
						}
						if(areaLocation == null)
							areaLocation=CMLib.map().areaLocation(E);
					}
				}
				if(rawSet.set.size()==0)
					sets.remove(key);
			}
		}
		return areaLocation;
	}

	@Override
	public boolean isCurrentActive(final String key)
	{
		try
		{
			synchronized(this)
			{
				final WireHookups rawSet=sets.get(key.toLowerCase());
				if(rawSet!=null)
				{
					for(final Iterator<WeakReference<Electronics>> w=rawSet.set.iterator(); w.hasNext(); )
					{
						final WeakReference<Electronics> W=w.next();
						final Electronics E=W.get();
						if(E==null)
							w.remove();
						else
						{
							final Area A=CMLib.map().areaLocation(E);
							if(A!=null)
								return A.getAreaState()==Area.State.ACTIVE;
						}
					}
					if(rawSet.set.size()==0)
						sets.remove(key);
				}
			}
		}
		catch(final Exception e)
		{
			Log.errOut("GroundWired",e);
		}
		return true;
	}

	protected void runElectricCurrent(final String key)
	{
		try
		{
			final List<PowerGenerator> generators = new LinkedList<PowerGenerator>();
			final List<PowerSource> batteries = new LinkedList<PowerSource>();
			final List<ElecPanel> panels = new LinkedList<ElecPanel>();

			final Area A=fillCurrentLists(key,generators,batteries,panels);
			if((A!=null)&&(A.getAreaState()!=Area.State.ACTIVE))
				return;

			processElectricCurrents(key, generators, batteries, panels);
		}
		catch(final Exception e)
		{
			Log.errOut("GroundWired",e);
		}
	}

	@Override
	public boolean seekBatteryPower(final ElecPanel E, final String key)
	{
		final List<PowerGenerator> generators = new LinkedList<PowerGenerator>();
		final List<PowerSource> batteries = new LinkedList<PowerSource>();
		final List<ElecPanel> panels = new LinkedList<ElecPanel>();
		fillCurrentLists(key,generators,batteries,panels);

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
							final WireHookups rawSet=sets.get(key.toLowerCase());
							if((rawSet!=null)
							&& (rawSet.set.size()>0)
							&& (rawSet.set.getLast().get() != S))
							{
								WeakReference<Electronics> ref = null;
								for(final Iterator<WeakReference<Electronics>> w=rawSet.set.iterator(); w.hasNext(); )
								{
									final WeakReference<Electronics> W=w.next();
									if(W.get()==S)
									{
										ref = W;
										w.remove();
										break;
									}
								}
								if(ref != null)
									rawSet.set.addLast(ref);
								else
									rawSet.set.addLast(new WeakReference<Electronics>(S));
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
			final List<ElecPanel> finalPanel=new XVector<ElecPanel>(E);
			final List<PowerSource> finalBatteries=new XVector<PowerSource>(battery);
			processElectricCurrents(key,emptyGeneratorList, finalBatteries, finalPanel);
			return true;
		}
		catch(final Exception e)
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
		for(final String key : keys)
		{
			runElectricCurrent(key);
		}
		setThreadStatus(serviceClient,"sleeping");
	}

	@Override
	public Manufacturer getDefaultManufacturer()
	{
		if(defaultManufacturer==null)
			defaultManufacturer=(Manufacturer)CMClass.getCommon("DefaultManufacturer");
		return defaultManufacturer;
	}

	@Override
	public void addManufacturer(final Manufacturer manufacturer)
	{
		if((manufacturer==null)||(manufacturer==defaultManufacturer))
			return;
		manufacturers.put(manufacturer.name().toUpperCase().trim(), manufacturer);
		saveAllManufacturers();
	}

	@Override
	public void delManufacturer(final Manufacturer manufacturer)
	{
		if((manufacturer==null)||(manufacturer==defaultManufacturer))
			return;
		final Manufacturer found=getManufacturer(manufacturer.name());
		if(found==manufacturer)
			manufacturers.remove(manufacturer.name().toUpperCase().trim());
		saveAllManufacturers();
	}

	@Override
	public void updateManufacturer(final Manufacturer manufacturer)
	{
		if((manufacturer==null)||(manufacturer==defaultManufacturer))
			return;
		final Manufacturer found=getManufacturer(manufacturer.name());
		if((found==null)||(found!=manufacturer))
		{
			for(final String manName : manufacturers.keySet())
			{
				if(manufacturers.get(manName)==manufacturer)
				{
					manufacturers.remove(manName);
					break;
				}
			}
			addManufacturer(manufacturer);
		}
		saveAllManufacturers();
	}

	@Override
	public Manufacturer getManufacturer(final String name)
	{
		if(name==null)
			return null;
		if(name.equals("RANDOM"))
			return null;
		return manufacturers.get(name.toUpperCase().trim());
	}

	@Override
	public Manufacturer getManufacturerOf(final Technical E, final String name)
	{
		if(name==null)
			return null;
		if(manufacturers.size()==0)
			return getDefaultManufacturer();
		if(name.equals("RANDOM"))
		{
			if(E==null)
				return null;
			final List<Manufacturer> subManufacturers=new ArrayList<Manufacturer>();
			for(final Manufacturer f : manufacturers.values())
			{
				if(CMLib.masking().maskCheck(f.getItemMask(), E, true))
					subManufacturers.add(f);
			}
			for(final Iterator<Manufacturer> f =subManufacturers.iterator();f.hasNext();)
			{
				final Manufacturer M=f.next();
				if((E.techLevel() < globalTechLevel+M.getMinTechLevelDiff())
				||(E.techLevel() > globalTechLevel+M.getMaxTechLevelDiff()))
					f.remove();
			}
			if(subManufacturers.size()==0)
				return getDefaultManufacturer();
			return subManufacturers.get(CMLib.dice().roll(1, subManufacturers.size(), -1));
		}
		return manufacturers.get(name.toUpperCase().trim());
	}

	@Override
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
		CMFile xmlFile=new CMFile(filename, null, CMFile.FLAG_FORCEALLOW);
		if(!xmlFile.exists())
			xmlFile=new CMFile("::"+filename, null, CMFile.FLAG_FORCEALLOW);
		final StringBuilder xmlStr=new StringBuilder("<MANUFACTURERS>");
		for(final Manufacturer man : manufacturers.values())
		{
			if(man != defaultManufacturer)
				xmlStr.append("<MANUFACTURER>").append(man.getXML()).append("</MANUFACTURER>");
		}
		xmlStr.append("</MANUFACTURERS>");
		xmlFile.saveText(xmlStr.toString());
	}

	protected void loadAllManufacturers()
	{
		final String filename=getManufacturersFilename();
		CMFile xmlFile=new CMFile(filename, null, CMFile.FLAG_FORCEALLOW);
		if((!xmlFile.exists())||(!xmlFile.canRead()))
			xmlFile=new CMFile("/resources/examples/manufacturers.xml", null, CMFile.FLAG_FORCEALLOW);
		manufacturers.clear();
		if(xmlFile.exists() && xmlFile.canRead())
		{
			final List<XMLLibrary.XMLTag> xDoc=CMLib.xml().parseAllXML(xmlFile.text());
			final List<XMLLibrary.XMLTag> xMans=new SLinkedList<XMLLibrary.XMLTag>();
			for(final XMLLibrary.XMLTag x : xDoc)
			{
				if(x.tag().equalsIgnoreCase("MANUFACTURER"))
					xMans.add(x);
				else
				if(x.tag().equalsIgnoreCase("MANUFACTURERS"))
					xMans.addAll(x.contents());
			}
			for(final XMLTag x : xMans)
			{
				final Manufacturer man =(Manufacturer)CMClass.getCommon("DefaultManufacturer");
				man.setXML(x.value());
				addManufacturer(man);
			}
		}
	}
}
