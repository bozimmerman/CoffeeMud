package com.planet_ink.coffee_mud.Items.Software;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.BasicTech.GenElecItem;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
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
   Copyright 2013-2018 Bo Zimmerman

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
public class GenShipProgram extends GenSoftware
{
	@Override
	public String ID()
	{
		return "GenShipProgram";
	}

	protected String	circuitKey		= "";
	protected String	readableText	= "";

	public GenShipProgram()
	{
		super();
		setName("a software disk");
		setDisplayText("a small disk sits here.");
		setDescription("It appears to be a general software program.");
	}

	@Override
	public void setCircuitKey(String key)
	{
		circuitKey=(key==null)?"":key;
	}

	@Override
	public TechType getTechType()
	{
		return TechType.SHIP_SOFTWARE;
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
	public boolean isActivationString(String word)
	{
		return super.isActivationString(word);
	}

	@Override
	public boolean isDeActivationString(String word)
	{
		return super.isDeActivationString(word);
	}

	@Override
	public boolean isCommandString(String word, boolean isActive)
	{
		return super.isCommandString(word, isActive);
	}

	@Override
	public String getActivationMenu()
	{
		return super.getActivationMenu();
	}

	@Override
	public boolean checkActivate(MOB mob, String message)
	{
		return super.checkActivate(mob, message);
	}

	@Override
	public boolean checkDeactivate(MOB mob, String message)
	{
		return super.checkDeactivate(mob, message);
	}

	@Override
	public boolean checkTyping(MOB mob, String message)
	{
		return super.checkTyping(mob, message);
	}

	@Override
	public boolean checkPowerCurrent(int value)
	{
		return super.checkPowerCurrent(value);
	}

	@Override
	public void onActivate(MOB mob, String message)
	{
		super.onActivate(mob, message);
	}

	@Override
	public void onDeactivate(MOB mob, String message)
	{
		super.onDeactivate(mob, message);
	}

	@Override
	public void onTyping(MOB mob, String message)
	{
		super.onTyping(mob, message);
	}

	@Override
	public void onPowerCurrent(int value)
	{
		super.onPowerCurrent(value);
	}

}
