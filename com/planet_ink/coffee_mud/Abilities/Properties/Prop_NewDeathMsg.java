package com.planet_ink.coffee_mud.Abilities.Properties;
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

import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class Prop_NewDeathMsg extends Property
{
	@Override
	public String ID()
	{
		return "Prop_NewDeathMsg";
	}

	@Override
	public String name()
	{
		return "NewDeathMsg";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public String accountForYourself()
	{
		return "Changed death msg";
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==msg.source())
		&&(msg.targetMessage()==null)
		&&(msg.othersMinor()==CMMsg.TYP_DEATH)
		&&(text().length()>0)
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().toUpperCase().indexOf("<S-NAME> IS DEAD")>0))
		{
			final int x=msg.othersMessage().indexOf("\n\r");
			if(x>=0)
			{
				msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),text()+msg.othersMessage().substring(x),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),text()+msg.othersMessage().substring(x));
			}
		}
		return super.okMessage(myHost,msg);
	}
}
