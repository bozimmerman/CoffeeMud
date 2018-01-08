package com.planet_ink.coffee_mud.Items.MiscMagic;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class Wand_Nourishment extends StdWand
{
	@Override
	public String ID()
	{
		return "Wand_Nourishment";
	}

	public Wand_Nourishment()
	{
		super();

		setName("a wooden wand");
		setDisplayText("a small wooden wand is here.");
		setDescription("A wand made out of wood");
		secretIdentity="The wand of nourishment.  Hold the wand say \\`shazam\\` to it.";
		baseGoldValue=200;
		material=RawMaterial.RESOURCE_OAK;
		recoverPhyStats();
		secretWord="SHAZAM";
	}

	@Override
	public void setSpell(Ability theSpell)
	{
		super.setSpell(theSpell);
		secretWord="SHAZAM";
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord="SHAZAM";
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WAND_USE:
				if((mob.isMine(this))
				&&(!amWearingAt(Wearable.IN_INVENTORY))
				&&(msg.targetMessage()!=null))
				{
					if(msg.targetMessage().toUpperCase().indexOf("SHAZAM")>=0)
					{
						if(mob.curState().adjHunger(50,mob.maxState().maxHunger(mob.baseWeight())))
							mob.tell(L("You are full."));
						else
							mob.tell(L("You feel nourished."));
					}
				}
				return;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}
}
