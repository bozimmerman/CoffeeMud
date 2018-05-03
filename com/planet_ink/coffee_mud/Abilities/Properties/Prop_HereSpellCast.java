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

public class Prop_HereSpellCast extends Prop_HaveSpellCast
{
	@Override
	public String ID()
	{
		return "Prop_HereSpellCast";
	}

	@Override
	public String name()
	{
		return "Casting spells when here";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}

	protected int lastNum=-1;
	private Vector<MOB> lastMOBs=new Vector<MOB>();

	@Override
	public String accountForYourself()
	{
		return spellAccountingsWithMask("Casts "," on those here.");
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ENTER;
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		lastMOBs=new Vector<MOB>();
	}

	public void process(MOB mob, Room room, int code) // code=0 add/sub, 1=addon, 2=subon
	{
		if((code==2)||((code==0)&&(lastNum!=room.numInhabitants())))
		{
			for(int v=lastMOBs.size()-1;v>=0;v--)
			{
				final MOB lastMOB=lastMOBs.elementAt(v);
				if((lastMOB.location()!=room)
				||((mob==lastMOB)&&(code==2)))
				{
					removeMyAffectsFrom(lastMOB);
					lastMOBs.removeElementAt(v);
				}
			}
			lastNum=room.numInhabitants();
		}
		if((!lastMOBs.contains(mob))
		&&((code==1)||((code==0)&&(room.isInhabitant(mob)))))
		{
			if(addMeIfNeccessary(mob,mob,true,0,maxTicks))
				lastMOBs.addElement(mob);
		}
	}

	@Override
	public void executeMsg(Environmental host, CMMsg msg)
	{
		if(processing)
			return;
		if((((msg.targetMinor()==CMMsg.TYP_ENTER)&&(msg.target()==affected))
			||((msg.targetMinor()==CMMsg.TYP_RECALL)&&(msg.target()==affected)))
		&&(affected instanceof Room))
			process(msg.source(),(Room)affected,1);
		else
		if((((msg.targetMinor()==CMMsg.TYP_LEAVE)&&(msg.target()==affected))
			||((msg.targetMinor()==CMMsg.TYP_RECALL)&&(msg.target()!=affected)))
		&&(affected instanceof Room))
			process(msg.source(),(Room)affected,2);
	}

	@Override
	public void affectPhyStats(Physical host, PhyStats affectableStats)
	{
		if(processing)
			return;
		try
		{
			processing=true;
			if((host instanceof MOB)&&(affected instanceof Room))
				process((MOB)host, (Room)affected,0);
		}
		finally
		{
			processing=false;
		}
	}
}
