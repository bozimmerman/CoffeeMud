package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Races.interfaces.Race;

import java.util.*;

/*
   Copyright 2017-2020 Bo Zimmerman

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
public class Prop_Unsellable extends Property
{
	@Override
	public String ID()
	{
		return "Prop_Unsellable";
	}

	@Override
	public String name()
	{
		return "Unsellable stuff";
	}

	protected String	ambiance	= null;
	protected boolean	dropOff		= false;
	protected String	message		= L("You can't sell that.");

	@Override
	public void setMiscText(final String newMiscText)
	{
		message=CMParms.getParmStr(newMiscText, "MESSAGE", L("You can't sell that."));
		ambiance= CMParms.getParmStr(newMiscText, "AMBIANCE", null);
		if((message != null)&&(affected != null))
			message=L(message,affected.name());
		dropOff = CMParms.getParmBool(newMiscText, "DROPOFF", false);
		super.setMiscText(newMiscText);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(ambiance != null)
			affectableStats.addAmbiance(ambiance);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		final Physical affected=this.affected;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_SELL:
			if((msg.tool()==affected)
			||((affected instanceof Container)
				&&(msg.tool() instanceof Item)
				&&(((Item)msg.tool()).ultimateContainer(affected)==affected)))
			{
				msg.source().tell(message);
				return false;
			}
			break;
		case CMMsg.TYP_DROP:
			{
				if((msg.target()==affected)
				&&(affected != null)
				&&(dropOff))
				{
					this.unInvoke();
					affected.delEffect(this);
				}
			}
			break;
		default:
			break;
		}
		return true;
	}
}
