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
   Copyright 2023-2025 Bo Zimmerman

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
public class Poison_Sickening extends Poison
{
	@Override
	public String ID()
	{
		return "Poison_Sickening";
	}

	private final static String localizedName = CMLib.lang().L("Sickening");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"POISONSICKEN"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int POISON_TICKS()
	{
		return 50;
	} // 0 means no adjustment!

	@Override
	protected int POISON_DELAY()
	{
		return 5;
	}

	@Override
	protected boolean POISON_AFFECTTARGET()
	{
		if((affected instanceof Food)
		||(affected instanceof Drink)
		||(affected instanceof MOB))
			return true;
		return false;
	}

	@Override
	protected String POISON_START_TARGETONLY()
	{
		if((affected instanceof Food)
		||(affected instanceof Drink)
		||(affected instanceof MOB))
			return "^G"+affected.name()+" was sickening!^?";
		return "";
	}

	@Override
	protected String POISON_START()
	{
		return "^G<S-NAME> bend(s) over with horrid stomach pains!^?";
	}

	@Override
	protected String POISON_AFFECT()
	{
		if(CMLib.dice().roll(1, 2,0)==1)
			return "^G<S-NAME> moan(s) and clutch(es) <S-HIS-HER> stomach.";
		else
			return "^G<S-NAME> puke(s) the contents of <S-HIS-HER> stomach.";
	}

	@Override
	protected String POISON_CAST()
	{
		return "^F^<FIGHT^><S-NAME> sicken(s) <T-NAMESELF>!^</FIGHT^>^?";
	}

	@Override
	protected String POISON_FAIL()
	{
		return "<S-NAME> attempt(s) to sicken <T-NAMESELF>, but fail(s).";
	}

	@Override
	protected int POISON_DAMAGE()
	{
		return CMLib.dice().roll(1,2,1);
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_POTENTIALLY_DEADLY;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-9-(int)Math.round(rank));
		if(affectableStats.getStat(CharStats.STAT_CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.STAT_CONSTITUTION,1);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)-7-(int)Math.round(rank));
		if(affectableStats.getStat(CharStats.STAT_STRENGTH)<=0)
			affectableStats.setStat(CharStats.STAT_STRENGTH,1);
	}
}

