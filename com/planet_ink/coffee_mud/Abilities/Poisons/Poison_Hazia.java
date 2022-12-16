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
   Copyright 2022-2022 Bo Zimmerman

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
public class Poison_Hazia extends Poison
{
	@Override
	public String ID()
	{
		return "Poison_Hazia";
	}

	private final static String localizedName = CMLib.lang().L("Hazia");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"HAZIA"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Hazed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int POISON_TICKS()
	{
		return 250;
	}

	@Override
	protected int POISON_DELAY()
	{
		return 5;
	}

	@Override
	protected String POISON_DONE()
	{
		return "The hazia runs its course.";
	}

	@Override
	protected String POISON_START()
	{
		return "^G<S-NAME> seem(s) to be in a haze!^?";
	}

	@Override
	protected String POISON_AFFECT()
	{
		return "^G<S-YOUPOSS> eyes glaze over.";
	}

	@Override
	protected String POISON_CAST()
	{
		return "^F^<FIGHT^><S-NAME> poison(s) <T-NAMESELF>!^</FIGHT^>^?";
	}

	@Override
	protected String POISON_FAIL()
	{
		return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";
	}

	@Override
	protected int POISON_DAMAGE()
	{
		return 0;
	}

	@Override
	protected int POISON_ADDICTION_CHANCE()
	{
		return 25;
	}

	protected volatile int decrease = 0;

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		decrease = 0;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		if(poisonTick==POISON_DELAY())
		{
			int amt = mob.baseState().getMana() / 20;
			if(amt < 1)
				amt = 1;
			decrease += amt;
			mob.recoverMaxState();
			if(mob.curState().getMana() > mob.maxState().getMana())
				mob.curState().setMana(mob.maxState().getMana());
			mob.curState().adjFatigue(CharState.FATIGUED_MILLIS, mob.maxState());
			if((mob.curState().getFatigue() >= CharState.FATIGUED_EXHAUSTED_MILLIS)
			&&(CMLib.flags().isStanding(mob))
			&&(CMLib.flags().isAliveAwakeMobile(mob, true)))
				CMLib.commands().forceStandardCommand(mob,"Sit",new XVector<String>("SIT"));
		}
		return true;
	}

	@Override
	public void affectCharState(final MOB affected, final CharState affectableStats)
	{
		if(affectableStats.getMana() > decrease)
			affectableStats.setMana(affectableStats.getMana() - decrease);
		else
			affectableStats.setMana(0);
	}


}
