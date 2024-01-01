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
   Copyright 2003-2024 Bo Zimmerman

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
public class Prop_NoPurge extends Property
{
	@Override
	public String ID()
	{
		return "Prop_NoPurge";
	}

	@Override
	public String name()
	{
		return "Prevents automatic purging";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_ITEMS;
	}

	protected long expirationDate = 0;

	@Override
	public void setMiscText(final String newMiscText)
	{
		expirationDate = 0;
		super.setMiscText(newMiscText);
		if((newMiscText!=null)&&(newMiscText.length()>0))
		{
			if(CMath.isLong(newMiscText))
				expirationDate = CMath.s_long(newMiscText);
			else
				expirationDate = CMLib.time().string2Millis(newMiscText);
		}
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected!=null)
		{
			if(affected instanceof Room)
			{
				final Room R=(Room)affected;
				for(int i=0;i<R.numItems();i++)
				{
					final Item I=R.getItem(i);
					if(I!=null)
						I.setExpirationDate(expirationDate);
				}
			}
			else
			if(affected instanceof Container)
			{
				if(((Container)affected).owner() instanceof Room)
				{
					((Container)affected).setExpirationDate(0);
					final List<Item> V=((Container)affected).getDeepContents();
					for(int v=0;v<V.size();v++)
						V.get(v).setExpirationDate(expirationDate);
				}
			}
			else
			if(affected instanceof Item)
				((Item)affected).setExpirationDate(expirationDate);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected!=null)
		{
			if(affected instanceof Room)
			{
				if((msg.targetMinor()==CMMsg.TYP_DROP)
				&&(msg.target() instanceof Item))
					((Item)msg.target()).setExpirationDate(expirationDate);
			}
			else
			if(affected instanceof Container)
			{
				if(((msg.targetMinor()==CMMsg.TYP_PUT)
					||(msg.targetMinor()==CMMsg.TYP_INSTALL))
				&&(msg.target()==affected)
				&&(msg.target() instanceof Item)
				&&(msg.tool() instanceof Item))
				{
					((Item)msg.target()).setExpirationDate(expirationDate);
					((Item)msg.tool()).setExpirationDate(expirationDate);
				}
			}
			else
			if(affected instanceof Item)
			{
				if((msg.targetMinor()==CMMsg.TYP_DROP)
				&&(msg.target() instanceof Item)
				&&(msg.target()==affected))
					((Item)msg.target()).setExpirationDate(expirationDate);
			}
			if((expirationDate > 0)
			&&(System.currentTimeMillis()> expirationDate))
				affected.delEffect(this);
		}
	}
}
