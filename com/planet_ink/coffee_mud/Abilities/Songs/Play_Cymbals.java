package com.planet_ink.coffee_mud.Abilities.Songs;

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
import com.planet_ink.coffee_mud.Items.interfaces.MusicalInstrument.InstrumentType;
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
public class Play_Cymbals extends Play_Instrument
{
	@Override
	public String ID()
	{
		return "Play_Cymbals";
	}

	private final static String	localizedName	= CMLib.lang().L("Cymbals");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected InstrumentType requiredInstrumentType()
	{
		return InstrumentType.CYMBALS;
	}

	@Override
	public String mimicSpell()
	{
		return "Spell_Knock";
	}

	private static Ability	theSpell	= null;

	@Override
	protected Ability getSpell()
	{
		if (theSpell != null)
			return theSpell;
		if (mimicSpell().length() == 0)
			return null;
		theSpell = CMClass.getAbility(mimicSpell());
		return theSpell;
	}

	@Override
	protected void inpersistentAffect(MOB mob)
	{
		if (getSpell() != null)
		{
			final Room R = mob.location();
			if (R != null)
			{
				final List<Physical> knockables = new LinkedList<Physical>();
				int dirCode = -1;
				if (mob == invoker())
				{
					for (int d = Directions.NUM_DIRECTIONS() - 1; d >= 0; d--)
					{
						final Exit e = R.getExitInDir(d);
						if ((e != null) && (e.hasADoor()) && (e.hasALock()) && (e.isLocked()))
						{
							knockables.add(e);
							dirCode = d;
						}
					}
					for (int i = 0; i < R.numItems(); i++)
					{
						final Item I = R.getItem(i);
						if ((I != null) && (I instanceof Container) && (I.container() == null))
						{
							final Container C = (Container) I;
							if (C.hasADoor() && C.hasALock() && C.isLocked())
								knockables.add(C);
						}
					}
				}
				for (int i = 0; i < mob.numItems(); i++)
				{
					final Item I = mob.getItem(i);
					if ((I != null) && (I instanceof Container) && (I.container() == null))
					{
						final Container C = (Container) I;
						if (C.hasADoor() && C.hasALock() && C.isLocked())
							knockables.add(C);
					}
				}
				for (final Physical P : knockables)
				{
					int levelDiff = P.phyStats().level() - (mob.phyStats().level() + (2 * super.getXLEVELLevel(mob)));
					if (levelDiff < 0)
						levelDiff = 0;
					if (proficiencyCheck(mob, -(levelDiff * 25), false))
					{
						CMMsg msg = CMClass.getMsg(mob, P, this, CMMsg.MSG_CAST_VERBAL_SPELL, L("@x1 begin(s) to glow!", P.name()));
						if (R.okMessage(mob, msg))
						{
							R.send(mob, msg);
							msg = CMClass.getMsg(mob, P, null, CMMsg.MSG_UNLOCK, null);
							CMLib.utensils().roomAffectFully(msg, R, dirCode);
							msg = CMClass.getMsg(mob, P, null, CMMsg.MSG_OPEN, L("<T-NAME> opens."));
							CMLib.utensils().roomAffectFully(msg, R, dirCode);
						}
					}
				}
			}
		}
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}
}
