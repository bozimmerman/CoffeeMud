package com.planet_ink.coffee_mud.Items.Software;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
import com.planet_ink.coffee_mud.Items.interfaces.ShipComponent.ShipEngine;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.net.*;
import java.io.*;
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
public class StdShipProgram extends StdProgram implements ArchonOnly
{
	public String ID(){	return "StdShipProgram";}
	
	protected volatile long nextPowerCycleTmr = System.currentTimeMillis()+(8*1000);
	
	protected String noActivationMenu="^rNo engine systems found.\n\r";
	
	protected volatile List<ShipEngine> engines=null;
	
	public StdShipProgram()
	{
		super();
		setName("a shuttle operations disk");
		setDisplayText("a small computer disk sits here.");
		setDescription("It appears to be a program to operate a small shuttle or rocket.");

		material=RawMaterial.RESOURCE_STEEL;
		baseGoldValue=1000;
		recoverPhyStats();
	}
	
	@Override public String getParentMenu() { return ""; }
	
	@Override public String getInternalName() { return "SHIP";}
	
	protected String buildActivationMenu(List<ShipEngine> engines)
	{
		StringBuilder str=new StringBuilder();
		str.append("^X").append(CMStrings.centerPreserve(" -- Fight Status -- ",60)).append("\n\r^.^N");
		final SpaceObject spaceObject=CMLib.map().getSpaceObject(this,true);
		final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
		final SpaceObject shipSpaceObject=(ship==null)?null:ship.getShipSpaceObject();
		if(ship==null)
			str.append("^Z").append(CMStrings.centerPreserve(" -- Can Not Determine -- ",60)).append("\n\r^.^N");
		else
		if(ship.getIsDocked() != null)
			str.append("^H").append(CMStrings.padRight("Docked at ^w"+ship.getIsDocked().roomTitle(null),60)).append("\n\r^.^N");
		else
		if((shipSpaceObject==null)||(!CMLib.map().isObjectInSpace(shipSpaceObject)))
			str.append("^Z").append(CMStrings.centerPreserve(" -- System Malfunction-- ",60)).append("\n\r^.^N");
		else
		{
			long thirdOfOrbiting=SpaceObject.DISTANCE_ORBITING/2;
			List<SpaceObject> orbs=CMLib.map().getSpaceObjectsWithin(shipSpaceObject,SpaceObject.DISTANCE_ORBITING-thirdOfOrbiting,SpaceObject.DISTANCE_ORBITING+thirdOfOrbiting);
			
			str.append("^H").append(CMStrings.padRight("Velocity",10));
			str.append("^N").append(CMStrings.padRight(Long.toString(ship.velocity()),20));
			str.append("^H").append(CMStrings.padRight("Location",10));
			SpaceObject orbiting=null;
			for(SpaceObject orb : orbs)
				if(orb instanceof Area)
					orbiting=orb;
			if(orbiting!=null)
				str.append("^N").append(CMStrings.padRight("orbiting "+orbiting.name(),34));
			else
				str.append("^N").append(CMStrings.padRight(CMParms.toStringList(shipSpaceObject.coordinates()),34));
			str.append("\n\r");
		}
		
		str.append("^X").append(CMStrings.centerPreserve("",60)).append("\n\r");
		str.append("^N\n\r");
		
		if((engines==null)||(engines.size()==0))
			str.append(noActivationMenu);
		else
		{
			str.append("^X").append(CMStrings.centerPreserve(" -- Engines -- ",60)).append("\n\r^.^N");
			int engineNumber=1;
			for(ShipEngine engine : engines)
			{
				str.append("^H").append(CMStrings.padRight("ENGINE"+engineNumber,10));
				str.append(CMStrings.padRight(engine.activated()?"^gACTIVE":"^rINACTIVE",10));
				str.append("^H").append(CMStrings.padRight("Fuel",6));
				str.append("^N").append(CMStrings.padRight(Long.toString(engine.powerRemaining()),14));
				str.append("^H").append(CMStrings.padRight("Model",6));
				str.append("^N").append(CMStrings.padRight(engine.Name(),34));
				engineNumber++;
				
			}
			str.append("^X").append(CMStrings.centerPreserve(" -- Commands -- ",60)).append("\n\r");
			str.append("^H").append(CMStrings.padRight("[ENGINE#/NAME] ([AFT/PORT/STARBOARD]) [AMOUNT]",60)).append("\n\r");
			str.append("^H").append(CMStrings.padRight("Instructs the given engine to fire in the appropriate",60)).append("\n\r");
			str.append("^H").append(CMStrings.padRight("direction. What happens, and how quickly, depends",60)).append("\n\r");
			str.append("^H").append(CMStrings.padRight("largely on the capabilities of the engine. Direction",60)).append("\n\r");
			str.append("^H").append(CMStrings.padRight("is optional, and if not given, AFT is assumed.",60)).append("\n\r");
			str.append("^X").append(CMStrings.centerPreserve("",60)).append("\n\r");
			str.append("^N\n\r");
		}
		return str.toString();
	}
	
	protected synchronized List<ShipEngine> getEngines()
	{
		if(engines == null)
		{
			if(circuitKey.length()==0)
				engines=new Vector<ShipEngine>(0);
			else
			{
				List<Electronics> electronics=CMLib.tech().getMakeRegisteredElectronics(circuitKey);
				engines=new Vector<ShipEngine>(1);
				for(Electronics E : electronics)
					if(E instanceof ShipComponent.ShipEngine)
						engines.add((ShipComponent.ShipEngine)E);
				
			}
		}
		return engines;
	}
	
	@Override public boolean isActivationString(String word) 
	{ 
		return isCommandString(word,false); 
	}
	
	@Override public boolean isDeActivationString(String word) 
	{ 
		return isCommandString(word,false); 
	}
	
	@Override public void onDeactivate(MOB mob, String message)
	{
	}

	@Override public boolean isCommandString(String word, boolean isActive)
	{
		if(!isActive)
		{
			word=word.toUpperCase();
			return (word.startsWith("TELNET ")||word.equals("TELNET"));
		}
		else
		{
			return true;
		}
	}

	@Override public String getActivationMenu()
	{
		return buildActivationMenu(getEngines());
	}

	@Override public boolean checkActivate(MOB mob, String message)
	{
		//List<String> parsed=CMParms.parse(message);
		try
		{
			return false;
		}
		catch(Exception e)
		{
			mob.tell("Ship software failure: "+e.getMessage());
			return false;
		}
	}
	
	@Override public boolean checkDeactivate(MOB mob, String message)
	{
		return true;
	}
	
	@Override public boolean checkTyping(MOB mob, String message)
	{
		return true;
	}
	
	@Override public boolean checkPowerCurrent(int value)
	{
		return true;
	}
	
	@Override public void onTyping(MOB mob, String message)
	{
		synchronized(this)
		{
			
		}
	}
	
	@Override public void onPowerCurrent(int value)
	{
		if(System.currentTimeMillis()>nextPowerCycleTmr)
		{
			engines=null;
			nextPowerCycleTmr = System.currentTimeMillis()+(8*1000);
		}
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUT:
				engines=null;
				break;
			}
		}
		super.executeMsg(host,msg);
	}
}
