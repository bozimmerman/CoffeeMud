package com.planet_ink.coffee_mud.Exits;
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

/*
   Copyright 2001-2025 Bo Zimmerman

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
public class Grate extends StdClosedDoorway
{
	@Override
	public String ID()
	{
		return "Grate";
	}

	private final static String localizedName = CMLib.lang().L("a barred grate");
	private final static String localizedDName = CMLib.lang().L("grate");
	private final static String localizedCText = CMLib.lang().L("a closed grate");
	private final static String localizedDesc = CMLib.lang().L("A metal grate of thick steel bars is inset here.");
	private final static String localizedCWord = CMLib.lang().L("close");
	private final static String localizedOWord = CMLib.lang().L("remove");

	@Override
	public String Name()
	{
		return localizedName;
	}

	@Override
	public String doorName()
	{
		return localizedDName;
	}

	@Override
	public String closedText()
	{
		return localizedCText;
	}

	@Override
	public String description()
	{
		return localizedDesc;
	}

	@Override
	public String closeWord()
	{
		return localizedCWord;
	}

	@Override
	public String openWord()
	{
		return localizedOWord;
	}
}
