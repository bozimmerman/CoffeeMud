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
   Copyright 2018-2025 Bo Zimmerman

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
public class Prop_UseEmoter2 extends Prop_UseEmoter
{
	@Override
	public String ID()
	{
		return "Prop_UseEmoter2";
	}

	@Override
	public String name()
	{
		return "Emoting when used differently";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(processing)
			return;
		try
		{
			processing=true;

			if(affected==null)
				return;
			final Item myItem=(Item)affected;
			if(myItem.owner()==null)
				return;
			if((msg.amITarget(affected))
			&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
			&&(CMLib.flags().canSmell(msg.source(),myItem))
			&&(smells!=null)
			&&((mask==null)||(CMLib.masking().maskCheck(mask, msg.source(), true))))
			{
				processing=false;
				super.executeMsg(myHost, msg);
			}
			else
			if((mask==null)||(CMLib.masking().maskCheck(mask, msg.source(), true)))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_DRINK:
					if((myItem instanceof Drink)
					&&(msg.amITarget(myItem)))
						super.emoteNow(msg);
					break;
				case CMMsg.TYP_POUR:
					if((myItem instanceof Drink)
					&&(msg.tool()==myItem)
					&&(msg.target() instanceof Physical))
						super.emoteNow(msg);
					break;
				case CMMsg.TYP_EAT:
					if((myItem instanceof Food)
					&&(msg.amITarget(myItem)))
						super.emoteNow(msg);
					break;
				case CMMsg.TYP_GET:
					if((!(myItem instanceof Drink))
					&&(!(myItem instanceof Food))
					&&(msg.amITarget(myItem)))
						super.emoteNow(msg);
					break;
				}
			}
		}
		finally
		{
			processing=false;
		}
	}
}
