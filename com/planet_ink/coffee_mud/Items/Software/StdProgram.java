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
public class StdProgram extends StdItem implements Software
{
	public String ID(){	return "StdProgram";}
	
	protected StringBuilder nextMsg = new StringBuilder("");
	protected String currentScreen="";
	protected String circuitKey="";

	public StdProgram()
	{
		super();
		setName("a software disk");
		setDisplayText("a small disk sits here.");
		setDescription("It appears to be a general software program.");

		material=RawMaterial.RESOURCE_STEEL;
		baseGoldValue=1000;
		recoverPhyStats();
	}
	
	public void setCircuitKey(String key) 
	{ 
		circuitKey=(key==null)?"":key; 
	}

	public int techLevel() { return phyStats().ability();}
	public void setTechLevel(int lvl) { basePhyStats.setAbility(lvl); recoverPhyStats(); }
	
	public String getParentMenu() { return ""; }
	public String getInternalName() { return "";}
	
	public boolean isActivationString(String word) { return false; }
	public boolean isDeActivationString(String word) { return false; }
	public boolean isCommandString(String word, boolean isActive) { return false; }

	public String getActivationMenu() { return ""; }
	
	public String getCurrentScreenDisplay()
	{
		return currentScreen;
	}

	public void setCurrentScreenDisplay(String msg)
	{
		this.currentScreen=msg;
	}
	
	public String getScreenMessage() 
	{
		synchronized(nextMsg)
		{
			final String msg=nextMsg.toString();
			nextMsg.setLength(0);
			return msg; 
		}
	}
	
	public void addScreenMessage(String msg)
	{
		synchronized(nextMsg)
		{
			nextMsg.append(msg).append("\n\r");
		}
	}
	
	protected void forceUpMenu()
	{
		if((container() instanceof Electronics.Computer)&&(((Electronics.Computer)container()).getActiveMenu().equals(getInternalName())))
			((Electronics.Computer)container()).setActiveMenu(getParentMenu());
	}
	
	protected void forceNewMessageScan()
	{
		if(container() instanceof Electronics.Computer)
			((Electronics.Computer)container()).forceReadersSeeNew();
	}
	
	protected void forceNewMenuRead()
	{
		if(container() instanceof Electronics.Computer)
			((Electronics.Computer)container()).forceReadersMenu();
	}
	
	public boolean checkActivate(MOB mob, String message)
	{
		return true;
	}
	
	public boolean checkDeactivate(MOB mob, String message)
	{
		return true;
	}
	
	public boolean checkTyping(MOB mob, String message)
	{
		return true;
	}
	
	public boolean checkPowerCurrent(int value)
	{
		return true;
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
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

	public void onActivate(MOB mob, String message)
	{
		
	}
	
	public void onDeactivate(MOB mob, String message)
	{
		
	}
	
	public void onTyping(MOB mob, String message)
	{
		
	}
	
	public void onPowerCurrent(int value)
	{
		
	}
	
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				onActivate(msg.source(),msg.targetMessage());
				break;
			case CMMsg.TYP_DEACTIVATE:
				onDeactivate(msg.source(),msg.targetMessage());
				break;
			case CMMsg.TYP_WRITE:
				onTyping(msg.source(),msg.targetMessage());
				break;
			case CMMsg.TYP_POWERCURRENT:
				onPowerCurrent(msg.value());
				break;
			}
		}
		super.executeMsg(host, msg);
	}
}
