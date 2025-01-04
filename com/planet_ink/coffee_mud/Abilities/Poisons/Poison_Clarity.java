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
   Copyright 2024-2025 Bo Zimmerman

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
public class Poison_Clarity extends Poison {
	@Override
	public String ID()
	{
		return "Poison_Clarity";
	}

	private final static String localizedName = CMLib.lang().L("Clarity");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Clarity)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	private static final String[] triggerStrings =I(new String[] {"POISONCLARITY"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int POISON_TICKS()
	{
		return 30;
	} // 0 means no adjustment!

	@Override
	protected int POISON_DELAY()
	{
		return 5;
	}

	@Override
	protected String POISON_DONE()
	{
		return "The clarity fades.";
	}

	@Override
	protected String POISON_START()
	{
		return "^G<S-NAME> seem(s) clear and alert!^?";
	}

	@Override
	protected String POISON_AFFECT()
	{
		return "";
	}

	@Override
	protected String POISON_CAST()
	{
		return "^F^<FIGHT^><S-NAME> clarif(ys) <T-NAMESELF>!^</FIGHT^>^?";
	}

	@Override
	protected String POISON_FAIL()
	{
		return "<S-NAME> attempt(s) to clarity <T-NAMESELF>, but fail(s).";
	}

	@Override
	protected int POISON_DAMAGE()
	{
		return 0;
	}

	@Override
	protected int POISON_ADDICTION_CHANCE()
	{
		return 2;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)+(int)Math.round(rank));
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+(10*(int)Math.round(rank)));
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_MINDALTERING;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if ((msg.target() != null)
		&& (msg.tool() instanceof Ability )
		&&(msg.sourceMinor()!=CMMsg.TYP_TEACH)
		&& (msg.amITarget(affected))
		&&(msg.tool() instanceof Poison_Liquor))
		{
			if(msg.target() instanceof MOB)
				((MOB)msg.target()).tell(L("You are immune to @x1.",msg.tool().name()));
			if(msg.source()!=msg.target())
				msg.source().tell(msg.source(),msg.target(),msg.tool(),L("<T-NAME> seem(s) immune to <O-NAME>."));
			return false;
		}
		return super.okMessage(myHost, msg);
	}
}
