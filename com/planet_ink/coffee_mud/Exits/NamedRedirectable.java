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
   Copyright 2019-2020 Bo Zimmerman

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
public class NamedRedirectable extends StdOpenDoorway
{
	@Override
	public String ID()
	{
		return "NamedRedirectable";
	}

	private String	name		= "a walkway";
	private String	display		= "";
	private String	description	= "";
	private Room	redirRoom	= null;

	@Override
	public String Name()
	{
		return name;
	}

	@Override
	public void setName(final String newName)
	{
		this.name=newName;
	}

	@Override
	public String displayText()
	{
		return display;
	}

	@Override
	public void setDisplayText(final String newDisplayText)
	{
		this.display=newDisplayText;
	}

	@Override
	public String description()
	{
		return description;
	}

	@Override
	public void setDescription(final String newDescription)
	{
		this.description=newDescription;
	}

	@Override
	public Room lastRoomUsedFrom(final Room fromRoom)
	{
		if((fromRoom != null)
		&&(redirRoom==null))
			this.redirRoom = fromRoom;
		return this.redirRoom;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(redirRoom!=null)
		&&(msg.target() instanceof Room)
		&&(msg.tool()==this)
		&&(msg.target() != redirRoom))
		{
			msg.setTarget(redirRoom);
		}
		return true;
	}
}
