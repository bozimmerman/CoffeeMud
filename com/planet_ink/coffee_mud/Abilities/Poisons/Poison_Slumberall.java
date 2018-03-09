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

public class Poison_Slumberall extends Poison
{
	@Override
	public String ID()
	{
		return "Poison_Slumberall";
	}

	private final static String localizedName = CMLib.lang().L("Slumberall");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"POISONSLEEP"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override protected int POISON_TICKS(){return 50;} // 0 means no adjustment!
	@Override
	protected int POISON_DELAY()
	{
		return 1;
	}

	@Override
	protected String POISON_DONE()
	{
		return "You don't feel so drowsy anymore.";
	}

	@Override
	protected String POISON_START()
	{
		return null;
	}

	@Override
	protected String POISON_AFFECT()
	{
		return "";
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

	protected boolean fallenYet=false;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(affected instanceof MOB)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)&&(super.canBeUninvoked()))
		{
			final MOB mob=(MOB)affected;
			CMLib.commands().postStand(mob,true);
		}
		super.unInvoke();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(msg.amITarget(mob)&&(fallenYet)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
			unInvoke();
		else
		if((msg.amISource(mob))
		&&(!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&(msg.sourceMajor()>0)
		&&(msg.sourceMinor()!=CMMsg.TYP_SLEEP))
		{
			mob.tell(L("You are way too drowsy."));
			return false;
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(!(affected instanceof MOB))
			return true;

		final MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		if((!fallenYet)&&(mob.location()!=null))
		{
			fallenYet=true;
			mob.location().show(mob,null,CMMsg.MSG_SLEEP,L("<S-NAME> fall(s) asleep!"));
			mob.recoverPhyStats();
		}
		return true;
	}
}
