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
public class GroundWired extends StdLibrary implements TechLibrary
{
	public String ID(){return "GroundWired";}
	
	public String getGeneratorKey(Electronics E)
	{
		if((E != null)&&(E.owner() instanceof Room))
		{
			Room R=(Room)E.owner();
			if(E.container() instanceof Electronics.ElecPanel)
			{
				LandTitle title = CMLib.law().getLandTitle(R);
				if(title != null)
					return title.getUniqueLotID();
				else
					return "";
			}
			else
			if(E.container() instanceof Electronics) // must be a gizmo that requires a battery or generator
			{
				return E.container().toString();
			}
		}
		return "";
	}
	
	/*
	private CMSupportThread thread=null;
	protected STreeMap<Electronics.PowerGenerator,Pair<List<Electronics.PowerSource>,List<Electronics>>> currents 
													= new STreeMap<Electronics.PowerGenerator,Pair<List<Electronics.PowerSource>,List<Electronics>>>(); 
	public CMSupportThread getSupportThread() { return thread;}
	protected CMMsg powerMsg = null;
	
	public boolean activate() 
	{
		if(thread==null)
			thread=new CMSupportThread("THWired"+Thread.currentThread().getThreadGroup().getName().charAt(0), 
					100, this, CMSecurity.isDebugging(CMSecurity.DbgFlag.UTILITHREAD), CMSecurity.DisFlag.ELECTRICTHREAD);
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
		thread.shutdown();
		return true;
	}
	
	public void run()
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.ELECTRICTHREAD))
			return;
		thread.setStatus("pushing electric currents");
		HashSet<Electronics.PowerSource> doneBatteries = new HashSet<Electronics.PowerSource>();
		for(Electronics.PowerGenerator currentGeneratorI : currents.keySet())
		{
			List<Electronics.PowerSource> batterySet = currents.get(currentGeneratorI).first;
			List<Electronics> currentSet = currents.get(currentGeneratorI).second;
			if(servicedItems == null)
			{
				List<Electronics> serviceableItems=new LinkedList<Electronics>();
				Room R=(Room)owner();
				Iterator<Room> r;
				LandTitle title = CMLib.law().getLandTitle(R);
				if(title != null)
					r=title.getPropertyRooms().iterator();
				else
					r=new EnumerationIterator<Room>(R.getArea().getMetroMap());
				for(;r.hasNext();)
				{
					R=r.next();
					for(int i=0;i<R.numItems();i++)
					{
						Item I=R.getItem(i);
						if(!(I instanceof Electronics.PowerSource))
						{
							if(I instanceof Electronics)
								serviceableItems.add((Electronics)I);
						}
					}
				}
				servicedItems = serviceableItems;
			}
			int currentSize = currentSet.size();
			for(Iterator<Electronics> i=currentSet.iterator();i.hasNext();)
			{
				final Electronics E=i.next();
				if(!E.activated())
					currentSize--;
			}
			if(currentGeneratorI.activated())
			{
				if(powerMsg == null)
					powerMsg = CMClass.getMsg(CMClass.sampleMOB(),null,currentGeneratorI,CMMsg.MSG_POWERCURRENT, null);
				else
					powerMsg.setTool(currentGeneratorI);
				powerMsg.setTarget(currentGeneratorI);
				if(currentGeneratorI.okMessage(currentGeneratorI, powerMsg))
					currentGeneratorI.executeMsg(currentGeneratorI, powerMsg);
				if(currentSize>0)
				{
        			powerMsg.setValue((int)(currentGeneratorI.powerRemaining()/currentSize));
        			for(Iterator<Electronics> i=currentSet.iterator();i.hasNext();)
        			{
        				Electronics E=i.next();
        				powerMsg.setTarget(E);
        				if(E.activated() && E.okMessage(E, powerMsg))
            				E.executeMsg(E, powerMsg);
        			}
				}
			}
			if(batterySet.size()>0)
			{
				if(currentGeneratorI.activated())
				{
        			powerMsg.setValue((int)(currentGeneratorI.powerRemaining()/batterySet.size()));
        			for(Iterator<Electronics.PowerSource> i=batterySet.iterator();i.hasNext();)
        			{
        				Electronics.PowerSource E=i.next();
        				powerMsg.setTarget(E);
        				if(E.okMessage(E, powerMsg)) // even unactivated batteries draw from the generators
            				E.executeMsg(E, powerMsg);
        			}
				}
				if(currentSize>0)
				{
        			for(Iterator<Electronics.PowerSource> b=batterySet.iterator();b.hasNext();)
        			{
        				Electronics.PowerSource batterE=b.next();
            			if((batterE.activated())&&(!doneBatteries.contains(batterE)))
            			{
            				doneBatteries.add(batterE);
            				powerMsg.setTool(batterE);
        	    			powerMsg.setValue((int)(batterE.powerRemaining()/currentSize));
        	    			for(Iterator<Electronics> i=currentSet.iterator();i.hasNext();)
        	    			{
        	    				Electronics E=i.next();
        	    				powerMsg.setTarget(E);
        	    				if(E.activated() && E.okMessage(E, powerMsg))
        	        				E.executeMsg(E, powerMsg);
        	    			}
            			}
        			}
				}
			}
			
		}
		thread.setStatus("sleeping");
	}
	*/
}
