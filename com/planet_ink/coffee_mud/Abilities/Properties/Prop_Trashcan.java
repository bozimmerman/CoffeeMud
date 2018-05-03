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
   Copyright 2003-2018 Bo Zimmerman

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
public class Prop_Trashcan extends Property
{
	@Override
	public String ID()
	{
		return "Prop_Trashcan";
	}

	@Override
	public String name()
	{
		return "Auto purges items put into a container";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_ROOMS;
	}

	protected SLinkedList<Item> trashables=new SLinkedList<Item>();
	protected int tickDelay=0;
	protected volatile long lastAddition=0;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(tickID==Tickable.TICKID_PROPERTY_SPECIAL)
		{
			synchronized(trashables)
			{
				if((System.currentTimeMillis()-lastAddition)<((tickDelay-1)*CMProps.getTickMillis()))
					return true;
				for(final Item I : trashables)
					I.destroy();
				lastAddition=0;
				trashables.clear();
				CMLib.threads().deleteTick(this, Tickable.TICKID_PROPERTY_SPECIAL);
			}
			return false;
		}
		return true;
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		tickDelay=CMParms.getParmInt(newMiscText, "DELAY", 0);
	}

	protected void process(Item I)
	{
		if(tickDelay<=0)
			I.destroy();
		else
		synchronized(trashables)
		{
			if(lastAddition==0)
			{
				CMLib.threads().deleteTick(this, Tickable.TICKID_PROPERTY_SPECIAL);
				CMLib.threads().startTickDown(this, Tickable.TICKID_PROPERTY_SPECIAL, tickDelay);
			}
			lastAddition=System.currentTimeMillis()-10;
			trashables.add(I);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof Item)
		&&(msg.targetMinor()==CMMsg.TYP_PUT)
		&&(msg.amITarget(affected))
		&&(msg.tool() instanceof Item))
			process((Item)msg.tool());
		else
		if((affected instanceof Room)
		&&(msg.targetMinor()==CMMsg.TYP_DROP)
		&&(msg.target() instanceof Item))
			process((Item)msg.target());
	}
}
