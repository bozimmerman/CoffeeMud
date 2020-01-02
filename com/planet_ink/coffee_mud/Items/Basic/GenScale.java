package com.planet_ink.coffee_mud.Items.Basic;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class GenScale extends GenRideable
{
	@Override
	public String ID()
	{
		return "GenScale";
	}

	public GenScale()
	{
		this.name="a scale";
		this.displayText="a scale sits here";
		this.description="It has a fulcrum and an arm under which hang two large plates, upon which things may be placed, or upon which people may sit.";
		super.containType = Container.CONTAIN_ANYTHING;
		super.capacity = 10000;
		super.rideBasis = Rideable.RIDEABLE_SIT;
		super.riderCapacity = 2;
	}

	@Override
	public int riderCapacity()
	{
		final int contents = this.getContents().size();
		if(contents > 0)
			return (contents > 2) ? 0 : 2-contents;
		return 2;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.target()==this)
		&&(msg.targetMinor() ==  CMMsg.TYP_PUT)
		&&(msg.tool() instanceof Item))
		{
			final int riders = numRiders();
			final List<Item> items = this.getContents();
			final int contents = items.size();
			if((riders + contents) >= 2)
			{
				if(msg.tool() instanceof RawMaterial)
				{
					if(items.size()>0)
					{
						final Item I=items.get(0);
						if(I instanceof RawMaterial)
						{
							final RawMaterial fI=(RawMaterial)msg.tool();
							final RawMaterial sI=(RawMaterial)I;
							if((fI.material()==sI.material())
							&&(fI.getSubType().equals(sI.getSubType())))
								return true;
						}
					}

				}
				//TOOD: check if they are trying to pile more of a rawmaterial that will end up stacking.
				msg.source().tell(L("Nothing more can fit on @x1.",name(msg.source())));
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.target() == this)
		&&((msg.targetMinor() ==  CMMsg.TYP_LOOK)
			||(msg.targetMinor() ==  CMMsg.TYP_EXAMINE)))
		{
			final List<Item> items = new XVector<Item>(this.getContents());
			Physical item1 = null;
			Physical item2 = null;
			if(numRiders() > 0)
			{
				item1=this.fetchRider(0);
				if(numRiders() > 1)
					item2=this.fetchRider(1);
			}
			if(item1 == null)
			{
				if(items.size()==0)
					return;
				item1=items.remove(0);
			}
			if(item2 == null)
			{
				if(items.size()==0)
				{
					if(item1.phyStats().weight()>0)
						msg.source().tell(L("@x1 is heavier than nothing at all.",item1.name(msg.source())));
					else
						msg.source().tell(L("@x1 weighs nothing at all.",item1.name(msg.source())));
					return;
				}
				item2=items.remove(0);
			}
			if(item1.phyStats().weight()>item2.phyStats().weight())
				msg.source().tell(L("@x1 is heavier than @x2.",item1.name(msg.source()),item2.name(msg.source())));
			else
			if(item1.phyStats().weight()==item2.phyStats().weight())
				msg.source().tell(L("@x1 weighs the same as @x2.",item1.name(msg.source()),item2.name(msg.source())));
			else
				msg.source().tell(L("@x1 is heavier than @x2.",item2.name(msg.source()),item1.name(msg.source())));
		}
	}

}
