package com.planet_ink.coffee_mud.Abilities.Poisons;
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
   Copyright 2003-2025 Bo Zimmerman

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
public class Poison_BeeSting extends Poison
{
	@Override
	public String ID()
	{
		return "Poison_BeeSting";
	}

	private final static String localizedName = CMLib.lang().L("Bee Sting");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"POISONBEESTING"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int POISON_TICKS()
	{
		return 10;
	} // 0 means no adjustment!

	@Override
	protected int POISON_DELAY()
	{
		return 2;
	}

	@Override
	protected String POISON_DONE()
	{
		return "The stinging poison runs its course.";
	}

	@Override
	protected String POISON_START()
	{
		return "^G<S-NAME> turn(s) green.^?";
	}

	@Override
	protected String POISON_AFFECT()
	{
		return "<S-NAME> cringe(s) from the poisonous itch.";
	}

	@Override
	protected String POISON_CAST()
	{
		return "^F^<FIGHT^><S-NAME> sting(s) <T-NAMESELF>!^</FIGHT^>^?";
	}

	@Override
	protected String POISON_FAIL()
	{
		return "<S-NAME> attempt(s) to sting <T-NAMESELF>, but fail(s).";
	}

	@Override
	protected int POISON_DAMAGE()
	{
		return (invoker!=null)?(1+(int)Math.round(rank)):0;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_POTENTIALLY_DEADLY;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-(int)Math.round(rank));
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)-(int)Math.round(rank));
		if(affectableStats.getStat(CharStats.STAT_CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.STAT_CONSTITUTION,1);
		if(affectableStats.getStat(CharStats.STAT_STRENGTH)<=0)
			affectableStats.setStat(CharStats.STAT_STRENGTH,1);
	}
}
