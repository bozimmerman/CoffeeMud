package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.EachApplicable.ApplyAffectPhyStats;
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

import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class StdPortalWrapper extends StdRideableWrapper implements Item, Container, Rideable, Exit, CMObjectWrapper
{
	@Override
	public String ID()
	{
		return "StdPortalWrapper";
	}

	protected Exit exit = null;

	@Override
	public void setWrappedObject(final CMObject obj)
	{
		super.setWrappedObject(obj);
		if(obj instanceof Exit)
		{
			exit=(Exit)obj;
		}
	}

	@Override
	public CMObject newInstance()
	{
		return new StdPortalWrapper();
	}

	@Override
	public short exitUsage(final short change)
	{
		return (exit == null) ? 0 : exit.exitUsage(change);
	}

	@Override
	public StringBuilder viewableText(final MOB mob, final Room myRoom)
	{
		return (exit == null) ? new StringBuilder("") : exit.viewableText(mob, myRoom);
	}

	@Override
	public String doorName()
	{
		return (exit == null) ? "" : exit.doorName();
	}

	@Override
	public String closeWord()
	{
		return (exit == null) ? "" : exit.closeWord();
	}

	@Override
	public String openWord()
	{
		return (exit == null) ? "" : exit.openWord();
	}

	@Override
	public String closedText()
	{
		return (exit == null) ? "" : exit.closedText();
	}

	@Override
	public void setExitParams(final String newDoorName, final String newCloseWord, final String newOpenWord, final String newClosedText)
	{
	}

	@Override
	public String temporaryDoorLink()
	{
		return (exit == null) ? "" : exit.temporaryDoorLink();
	}

	@Override
	public void setTemporaryDoorLink(final String link)
	{
	}

	@Override
	public Room lastRoomUsedFrom(final Room fromRoom)
	{
		return (exit == null) ? null : exit.lastRoomUsedFrom(fromRoom);
	}

}
