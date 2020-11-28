package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Songs.Skill_Disguise;
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
   Copyright 2020-2020 Bo Zimmerman

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
public class Thief_InciteDivineFeud extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_InciteDivineFeud";
	}

	private final static String localizedName = CMLib.lang().L("Incite Divine Feud");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Incite Divine Feud)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}


	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"INCITEDIVINEFEUD"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_INFLUENTIAL;
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Deity deity1M=mob.charStats().getMyDeity();
		if((deity1M==null)
		||(deity1M==mob.baseCharStats().getMyDeity()))
		{
			mob.tell(L("You must have false faith in a deity other than your own to do this."));
			return false;
		}
		if(commands.size()==0)
		{
			mob.tell(L("You must specify a deity to start a feud with @x1 with, or STOP to stop the feud.",deity1M.Name()));
			return false;
		}

		String deityName = CMParms.combine(commands,0);
		final Deity deity2M=CMLib.map().getDeity(deityName);
		if(deity2M==null)
		{
			mob.tell(L("You don't know of a deity called @x1.",deityName));
			return false;
		}
		
		if(deity2M==deity1M)
		{
			mob.tell(L("You can't get a deity to feud against itself."));
			return false;
		}
		
		
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),
					L("<S-NAME> begin(s) plotting a feud between @x1 and @x2.",deity1M.Name(),deity2M.Name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Thief_InciteDivineFeud dA=(Thief_InciteDivineFeud)beneficialAffect(mob,mob,asLevel,Integer.MAX_VALUE/10);
				if(dA!=null)
					dA.makeLongLasting();
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to plot a feud, but is drawing a blank."));

		return success;
	}
}
