package com.planet_ink.coffee_mud.Items.CompTech;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
public class StdCompBattery extends StdElecCompItem implements PowerSource, TechComponent
{
	@Override
	public String ID()
	{
		return "StdCompBattery";
	}

	public StdCompBattery()
	{
		super();
		setName("a battery");
		basePhyStats.setWeight(2);
		setDisplayText("a battery sits here.");
		setDescription("");
		baseGoldValue=5;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		super.activate(true);
		super.setPowerCapacity(1000);
		super.setPowerRemaining(1000);
	}

	@Override
	public TechType getTechType()
	{
		return TechType.SHIP_POWER;
	}

	@Override
	public void setMiscText(String newText)
	{
		if(CMath.isInteger(newText))
			this.setPowerCapacity(CMath.s_int(newText));
		super.setMiscText(newText);
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdCompBattery))
			return false;
		return super.sameAs(E);
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_LOOK:
				super.executeMsg(host, msg);
				if(CMLib.flags().canBeSeenBy(this, msg.source()))
					msg.source().tell(L("@x1 is currently @x2",name(),(activated()?"delivering power.\n\r":"deactivated/disconnected.\n\r")));
				return;
			case CMMsg.TYP_POWERCURRENT:
				if(activated()
				&& ((subjectToWearAndTear())
				&&(usesRemaining()<=100)
				&&(Math.random()>CMath.div(usesRemaining(), 100))))
				{
					final Room R=CMLib.map().roomLocation(this);
					if(R!=null)
					{
						// malfunction!
						final CMMsg msg2=CMClass.getMsg(msg.source(), this, null, CMMsg.NO_EFFECT, null, CMMsg.MSG_DEACTIVATE|CMMsg.MASK_CNTRLMSG, "", CMMsg.NO_EFFECT,null);
						if(R.okMessage(msg.source(), msg2))
							R.send(msg.source(), msg2);
					}
					else
						activate(false);
				}
				break;
			}
		}
		super.executeMsg(host, msg);
	}
}
