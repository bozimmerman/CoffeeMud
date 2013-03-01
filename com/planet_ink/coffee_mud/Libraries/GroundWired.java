package com.planet_ink.coffee_mud.Libraries;
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
public class GroundWired extends StdLibrary implements TechLibrary, Runnable
{
	public String ID(){return "GroundWired";}
	
	public final Map<String,List<Electronics>> sets=new Hashtable<String,List<Electronics>>();
	
	public final AtomicInteger nextKey = new AtomicInteger(0);
	
	public synchronized String registerElectrics(final Electronics E, final String oldKey)
	{
		final ItemPossessor possessor=E.owner();
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
			List<Electronics> set=sets.get(newKey);
			if(set==null)
			{
				set=new ArrayList<Electronics>();
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
	
	private CMSupportThread thread=null;
	protected STreeMap<Electronics.PowerGenerator,Pair<List<Electronics.PowerSource>,List<Electronics>>> currents 
													= new STreeMap<Electronics.PowerGenerator,Pair<List<Electronics.PowerSource>,List<Electronics>>>(); 
	public CMSupportThread getSupportThread() { return thread;}
	protected CMMsg powerMsg = null;
	
	protected CMMsg getPowerMsg(int powerAmt)
	{
		if(powerMsg==null)
		{
			MOB powerMOB=CMClass.getMOB("electricity");
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
		if(thread==null)
			thread=new CMSupportThread("THWired"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					CMProps.getTickMillis(), this, CMSecurity.isDebugging(CMSecurity.DbgFlag.UTILITHREAD), 
					CMSecurity.DisFlag.ELECTRICTHREAD);
		if(!thread.isStarted())
		{
			thread.setStatus("sleeping");
			thread.disableDBCheck();
			thread.start();
		}
		return true;
	}
	
	public boolean shutdown() 
	{
		sets.clear();
		thread.shutdown();
		return true;
	}
	
	public void run()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.ELECTRICTHREAD))
			return;
		thread.setStatus("pushing electric currents");

		List<String> keys;
		synchronized(this)
		{
			keys=new XVector<String>(sets.keySet());
		}
		for(String key : keys)
		{
			try
			{
				LinkedList<Electronics.PowerGenerator> generators = new LinkedList<Electronics.PowerGenerator>();
				LinkedList<Electronics.PowerSource> batteries = new LinkedList<Electronics.PowerSource>();
				LinkedList<Electronics> panels = new LinkedList<Electronics>();
				synchronized(this)
				{
					List<Electronics> rawSet=sets.get(key);
					if(rawSet!=null)
					{
						for(Electronics E : rawSet)
							if(E instanceof Electronics.PowerGenerator)
								generators.add((Electronics.PowerGenerator)E);
							else
							if(E instanceof Electronics.PowerSource)
								batteries.add((Electronics.PowerSource)E);
							else
								panels.add(E);
					}
				}
				CMMsg powerMsg=getPowerMsg(0);
				for(Electronics.PowerGenerator E : generators)
				{
					powerMsg.setTarget(E);
					powerMsg.setValue(0);
					final Room R=CMLib.map().roomLocation(E);
					if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
						R.send(powerMsg.source(), powerMsg);
				}
				long remainingPowerToDistribute=0;
				long availablePowerToDistribute=0;
				for(Electronics.PowerGenerator G : generators)
					if(G.activated())
					{
						availablePowerToDistribute+=G.powerRemaining();
						G.setPowerRemaining(0);
					}
				for(Electronics.PowerSource B : batteries)
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
					for(Electronics.PowerSource E : batteries)
					{
						powerMsg.setTarget(E);
						powerMsg.setValue(0);
						final Room R=CMLib.map().roomLocation(E);
						if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
							R.send(powerMsg.source(), powerMsg);
					}
					continue;
				}
				else
				{
					remainingPowerToDistribute=availablePowerToDistribute;
					int panelsLeft=panels.size();
					for(Electronics E : panels)
					{
						powerMsg.setTarget(E);
						int amountToDistribute=(int)(remainingPowerToDistribute/(long)panelsLeft);
						powerMsg.setValue(amountToDistribute);
						final Room R=CMLib.map().roomLocation(E);
						if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
							R.send(powerMsg.source(), powerMsg);
						remainingPowerToDistribute-=(powerMsg.value()<0)?amountToDistribute:(amountToDistribute-powerMsg.value());
						panelsLeft--;
					}
					int batteriesLeft=batteries.size();
					for(Electronics.PowerSource E : batteries)
					{
						powerMsg.setTarget(E);
						int amountToDistribute=(int)(remainingPowerToDistribute/(long)batteriesLeft);
						powerMsg.setValue(amountToDistribute);
						final Room R=CMLib.map().roomLocation(E);
						if((R!=null)&&(R.okMessage(powerMsg.source(), powerMsg)))
							R.send(powerMsg.source(), powerMsg);
						panelsLeft--;
						remainingPowerToDistribute-=(powerMsg.value()<0)?amountToDistribute:(amountToDistribute-powerMsg.value());
					}
					int amountLeftOver=(int)((availablePowerToDistribute-remainingPowerToDistribute)/(long)generators.size());
					for(Electronics.PowerGenerator G : generators)
						if(G.activated())
							G.setPowerRemaining(amountLeftOver>G.powerCapacity()?G.powerCapacity():amountLeftOver);
				}
			}
			catch(Exception e)
			{
				Log.errOut("GroundWired",e);
			}
		}
		thread.setStatus("sleeping");
	}
}
