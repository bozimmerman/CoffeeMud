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
public class StdShipProgram extends StdProgram
{
	public String ID(){	return "StdShipProgram";}
	
	protected volatile long nextPowerCycleTmr = System.currentTimeMillis()+(8*1000);
	
	protected String noActivationMenu="^rNo engine systems found.\n\r";
	
	protected volatile List<ShipEngine> engines=null;
	
	public StdShipProgram()
	{
		super();
		setName("a shuttle operations disk");
		setDisplayText("a small software disk sits here.");
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
		str.append("^X").append(CMStrings.centerPreserve(" -- Fight Status -- ",60)).append("^.^N\n\r");
		final SpaceObject spaceObject=CMLib.map().getSpaceObject(this,true);
		final SpaceShip ship=(spaceObject instanceof SpaceShip)?(SpaceShip)spaceObject:null;
		final SpaceObject shipSpaceObject=(ship==null)?null:ship.getShipSpaceObject();
		if(ship==null)
			str.append("^Z").append(CMStrings.centerPreserve(" -- Can Not Determine -- ",60)).append("^.^N\n\r");
		else
		if(ship.getIsDocked() != null)
		{
			str.append("^H").append(CMStrings.padRight("Docked at ^w"+ship.getIsDocked().displayText(null),60)).append("^.^N\n\r");
			SpaceObject planet=CMLib.map().getSpaceObject(ship.getIsDocked(), true);
			if(planet!=null)
				str.append("^H").append(CMStrings.padRight("On Planet ^w"+planet.Name(),60)).append("^.^N\n\r");
		}
		else
		if((shipSpaceObject==null)||(!CMLib.map().isObjectInSpace(shipSpaceObject)))
			str.append("^Z").append(CMStrings.centerPreserve(" -- System Malfunction-- ",60)).append("^.^N\n\r");
		else
		{
			long thirdOfOrbiting=SpaceObject.DISTANCE_ORBITING/2;
			List<SpaceObject> orbs=CMLib.map().getSpaceObjectsWithin(shipSpaceObject,SpaceObject.DISTANCE_ORBITING,SpaceObject.DISTANCE_ORBITING+thirdOfOrbiting);
			SpaceObject orbiting=null;
			for(SpaceObject orb : orbs)
				if(orb instanceof Area)
				{
					orbiting=orb; // since they are sorted, this would be the nearest.
					break;
				}
			
			str.append("^H").append(CMStrings.padRight("Speed",10));
			str.append("^N").append(CMStrings.padRight(Long.toString(ship.speed()),20));
			str.append("^H").append(CMStrings.padRight("Direction",10));
			String dirStr=new StringBuilder(""+Math.round(Math.toDegrees(ship.direction()[0])*100)/100.0).append(" mark ").append(Math.round(Math.toDegrees(ship.direction()[0])*100)/100.0).toString();
			str.append("^N").append(CMStrings.padRight(dirStr,20));
			str.append("\n\r");
			str.append("^H").append(CMStrings.padRight("Location",10));
			if(orbiting!=null)
				str.append("^N").append(CMStrings.padRight("orbiting "+orbiting.name(),50));
			else
				str.append("^N").append(CMStrings.padRight(CMParms.toStringList(shipSpaceObject.coordinates()),50));
			str.append("^H").append(CMStrings.padRight("Facing",10));
			String facStr=new StringBuilder(""+Math.round(Math.toDegrees(ship.facing()[0])*100)/100.0).append(" mark ").append(Math.round(Math.toDegrees(ship.facing()[0])*100)/100.0).toString();
			str.append("^N").append(CMStrings.padRight(facStr,20));
			if(orbiting!=null)
			{
				str.append("^H").append(CMStrings.padRight("Altitude",10));
				str.append("^N").append(CMStrings.padRight(Long.toString((CMLib.map().getDistanceFrom(shipSpaceObject, orbiting)-orbiting.radius())/10)+"km",20));
			}
			else
			{
				str.append("\n\r");
			}
			str.append("\n\r");
		}
		str.append("^N\n\r");
		
		if((engines==null)||(engines.size()==0))
			str.append(noActivationMenu);
		else
		{
			str.append("^X").append(CMStrings.centerPreserve(" -- Engines -- ",60)).append("^.^N\n\r");
			int engineNumber=1;
			for(ShipEngine engine : engines)
			{
				str.append("^H").append(CMStrings.padRight("ENGINE"+engineNumber,9));
				str.append(CMStrings.padRight(engine.activated()?"^gACTIVE":"^rINACTIVE",9));
				str.append("^H").append(CMStrings.padRight("Fuel",5));
				str.append("^N").append(CMStrings.padRight(Long.toString(engine.getFuelRemaining()),11));
				str.append("^H").append(CMStrings.padRight(engine.Name(),24));
				str.append("^.^N\n\r");
				engineNumber++;
			}
			str.append("^N\n\r");
			str.append("^X").append(CMStrings.centerPreserve(" -- Commands -- ",60)).append("^.^N\n\r");
			str.append("^H").append(CMStrings.padRight("[ENGINEHELP] : Give details about engine commands.",60)).append("\n\r");
			str.append("^H").append(CMStrings.padRight("[ENGINE#/NAME] ([AFT/PORT/STARBOARD/DORSEL/VENTRAL]) [AMT]",60)).append("\n\r");
			str.append("^X").append(CMStrings.centerPreserve("",60)).append("^.^N\n\r");
			str.append("^N\n\r");
		}
		return str.toString();
	}
	
	public String getCurrentScreenDisplay()
	{
		return this.getActivationMenu();
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
	
	protected ShipEngine findEngineByName(String name)
	{
		List<ShipEngine> engines=getEngines();
		if(engines.size()==0) return null;
		name=name.toUpperCase();
		if(name.startsWith("ENGINE"))
		{
			String numStr=name.substring(6);
			if(!CMath.isInteger(numStr))
				return null;
			int num=CMath.s_int(numStr);
			if((num>0)&&(num<=engines.size()))
				return engines.get(num-1);
			return null;
		}
		ShipEngine E=(ShipEngine)CMLib.english().fetchEnvironmental(engines, name, true);
		if(E==null)
			E=(ShipEngine)CMLib.english().fetchEnvironmental(engines, name, false);
		return E;
	}
	
	@Override public boolean isCommandString(String word, boolean isActive)
	{
		Vector<String> parsed=CMParms.parse(word);
		if(parsed.size()==0)
			return false;
		String uword=parsed.get(0).toUpperCase();
		if(uword.startsWith("ENGINEHELP"))
			return true;
		return findEngineByName(uword)!=null;
	}

	@Override public String getActivationMenu()
	{
		return buildActivationMenu(getEngines());
	}

	@Override public boolean checkActivate(MOB mob, String message)
	{
		return true;
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
			Vector<String> parsed=CMParms.parse(message);
			if(parsed.size()==0)
			{
				super.addScreenMessage("Error: No command.");
				return;
			}
			String uword=parsed.get(0).toUpperCase();
			if(uword.equalsIgnoreCase("ENGINEHELP"))
			{
				super.addScreenMessage("^HENGINEHELP:^N\n\r^N"+"The ENGINE command instructs the given " +
						"engine number or name to fire in the appropriate direction. What happens, " +
						"and how quickly, depends largely on the capabilities of the engine. " +
						"Giving a direction is optional, and if not given, AFT is assumed. All "+
						"directions result in corrected bursts, except for AFT, which will result " +
						"in sustained accelleration.");
				return;
			}
			ShipEngine E=findEngineByName(uword);
			if(E==null)
			{
				super.addScreenMessage("Error: Unknown engine '"+uword+"'.");
				return;
			}
			int amount=0;
			ShipEngine.ThrustPort portDir=ShipEngine.ThrustPort.AFT;
			if(parsed.size()>3)
			{
				super.addScreenMessage("Error: Too many parameters.");
				return;
			}
			if(parsed.size()==1)
			{
				super.addScreenMessage("Error: No thrust amount given.");
				return;
			}
			if(!CMath.isInteger(parsed.get(parsed.size()-1)))
			{
				super.addScreenMessage("Error: '"+parsed.get(parsed.size()-1)+"' is not a valid amount.");
				return;
			}
			amount=CMath.s_int(parsed.get(1));
			if(parsed.size()==3)
			{
				portDir=(ShipEngine.ThrustPort)CMath.s_valueOf(ShipEngine.ThrustPort.class, parsed.get(1).toUpperCase().trim());
				if(portDir!=null) { }
				else
				if("aft".startsWith(parsed.get(1).toLowerCase()))
					portDir=ShipEngine.ThrustPort.AFT;
				else
				if("port".startsWith(parsed.get(1).toLowerCase()))
					portDir=ShipEngine.ThrustPort.PORT;
				else
				if("starboard".startsWith(parsed.get(1).toLowerCase()))
					portDir=ShipEngine.ThrustPort.STARBOARD;
				else
				if("ventral".startsWith(parsed.get(1).toLowerCase()))
					portDir=ShipEngine.ThrustPort.VENTRAL;
				else
				if("dorsel".startsWith(parsed.get(1).toLowerCase()))
					portDir=ShipEngine.ThrustPort.DORSEL;
				else
				{
					super.addScreenMessage("Error: '"+parsed.get(1)+" is not a valid direction: AFT, PORT, VENTRAL, DORSEL, or STARBOARD.");
					return;
				}
			}

			String code=Technical.TechCommand.THRUST.makeCommand(portDir,Integer.valueOf(amount));
			CMMsg msg=CMClass.getMsg(mob, E, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_ACTIVATE|CMMsg.MASK_CNTRLMSG, code, CMMsg.NO_EFFECT,null);
			if(E.owner() instanceof Room)
			{
				if(((Room)E.owner()).okMessage(mob, msg))
					((Room)E.owner()).send(mob, msg);
			}
			else
			if(E.okMessage(mob, msg))
				E.executeMsg(mob, msg);
		}
	}
	
	public void onActivate(MOB mob, String message)
	{
		onTyping(mob,message);
	}
	
	public void onDeactivate(MOB mob, String message)
	{
		Vector<String> parsed=CMParms.parse(message);
		if(parsed.size()==0)
		{
			super.addScreenMessage("Syntax Error!");
			return;
		}
		String uword=parsed.get(0).toUpperCase();
		ShipEngine E=findEngineByName(uword);
		if(E!=null)
		{
			onTyping(mob,"\""+uword+"\" "+0);
			return;
		}
		uword=message.toUpperCase();
		E=findEngineByName(uword);
		if(E==null)
		{
			super.addScreenMessage("Unknown engine '"+uword+"'!");
			return;
		}
		CMMsg msg=CMClass.getMsg(mob, E, this, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, "", CMMsg.NO_EFFECT,null);
		if(E.owner() instanceof Room)
		{
			if(((Room)E.owner()).okMessage(mob, msg))
				((Room)E.owner()).send(mob, msg);
		}
		else
		if(E.okMessage(mob, msg))
			E.executeMsg(mob, msg);
		return;
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
