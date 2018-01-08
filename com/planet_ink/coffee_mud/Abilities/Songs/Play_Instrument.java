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

public class Play_Instrument extends Play
{
	@Override
	public String ID()
	{
		return "Play_Instrument";
	}

	private final static String	localizedName	= CMLib.lang().L("Instruments");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected InstrumentType requiredInstrumentType()
	{
		return InstrumentType.WOODS;
	}

	public String mimicSpell()
	{
		return "";
	}

	@Override
	protected void inpersistentAffect(MOB mob)
	{
		Ability A = getSpell();
		if ((A != null) 
		&& ((mob != invoker()) || (getSpell().abstractQuality() != Ability.QUALITY_MALICIOUS)))
		{
			final Vector<String> chcommands = new Vector<String>();
			chcommands.add(mob.name());
			A = (Ability) A.copyOf();
			A.invoke(invoker(), chcommands, mob, true, adjustedLevel(invoker(), 0));
			if((A.abstractQuality()==Ability.QUALITY_MALICIOUS)
			&&(mob.isMonster())
			&&(!mob.isInCombat())
			&&(CMLib.flags().isMobile(mob))
			&&(!CMLib.flags().isATrackingMonster(mob))
			&&(mob.amFollowing()==null)
			&&(!mob.amDead())
			&&((!(mob instanceof Rideable))||(((Rideable)mob).numRiders()==0)))
			{
				A = CMClass.getAbility("Thief_Assassinate");
				if (A != null)
					A.invoke(mob, invoker(), true, 0);
			}
		}
	}

	@Override
	protected String songOf()
	{
		if (instrument != null)
			return instrument.name();
		return name();
	}

	protected Ability getSpell()
	{
		return null;
	}

	@Override
	public int abstractQuality()
	{
		if (getSpell() != null)
			return getSpell().abstractQuality();
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected boolean persistentSong()
	{
		return false;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}
}
