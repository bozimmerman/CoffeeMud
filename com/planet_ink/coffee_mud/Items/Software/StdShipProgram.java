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
	protected String activationMenu=noActivationMenu;
	
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
	
	protected void rebuildActivationMenu(List<ShipEngine> engines)
	{
		StringBuilder str=new StringBuilder();
		if((engines==null)||(engines.size()==0))
			str.append(noActivationMenu);
		else
		{
			//TODO:
		}
		activationMenu=str.toString();
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
			rebuildActivationMenu(engines);
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
		return activationMenu;
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
