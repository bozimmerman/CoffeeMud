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
   Copyright 2005-2020 Bo Zimmerman

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

	protected StringBuilder nextMsg = new StringBuilder("");
	protected String currentScreen="";

	public StdProgram()
	{
		super();
		setName("a software disk");
		setDisplayText("a small disk sits here.");
		setDescription("It appears to be a general software program.");

		basePhyStats.setWeight(1);
		material=RawMaterial.RESOURCE_STEEL;
		baseGoldValue=1000;
		recoverPhyStats();
	}

	@Override
	public void setCircuitKey(final String key)
	{
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
	public String getInternalName()
	{
		return "";
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

	public boolean checkActivate(final MOB mob, final String message)
	{
		return true;
	}

	public boolean checkDeactivate(final MOB mob, final String message)
	{
		return true;
	}

	public boolean checkTyping(final MOB mob, final String message)
	{
		return true;
	}

	public boolean checkPowerCurrent(final int value)
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

	public void onActivate(final MOB mob, final String message)
	{

	}

	public void onDeactivate(final MOB mob, final String message)
	{

	}

	public void onTyping(final MOB mob, final String message)
	{

	}

	public void onPowerCurrent(final int value)
	{

	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
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
}
