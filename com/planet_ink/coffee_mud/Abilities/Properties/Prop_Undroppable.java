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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2017-2025 Bo Zimmerman

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
public class Prop_Undroppable extends Property
{
	@Override
	public String ID()
	{
		return "Prop_Undroppable";
	}

	@Override
	public String name()
	{
		return "Undroppable stuff";
	}

	protected Reference<MOB> owner	= null;
	protected String	ambiance	= null;
	protected boolean	lostOff		= false;
	protected String	message		= L("You don't want to drop that.");

	@Override
	public void setMiscText(final String newMiscText)
	{
		message=CMParms.getParmStr(newMiscText, "MESSAGE", L("You don't want to drop that."));
		ambiance= CMParms.getParmStr(newMiscText, "AMBIANCE", null);
		if((message != null)&&(affected != null))
			message=L(message,affected.name());
		lostOff = CMParms.getParmBool(newMiscText, "LOSTOFF", false);
		super.setMiscText(newMiscText);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(ambiance != null)
			affectableStats.addAmbiance(ambiance);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ITEMNODROP);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		final Physical affected=this.affected;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_DROP:
		{
			if((msg.target()==affected)
			||((affected instanceof Container)
				&&(msg.target() instanceof Item)
				&&(((Item)msg.target()).ultimateContainer(affected)==affected)))
			{
				msg.source().tell(message);
				return false;
			}
			break;
		}
		default:
			if((lostOff) && (affected instanceof Item))
			{
				final Item I = (Item)affected;
				if((owner == null)&&(I.owner() instanceof MOB))
					owner = new WeakReference<MOB>((MOB)I.owner());
				else
				if(I.owner() != owner.get())
					I.delEffect(this);
			}
			break;
		}
		return true;
	}
}
