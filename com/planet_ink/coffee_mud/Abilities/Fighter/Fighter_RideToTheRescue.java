package com.planet_ink.coffee_mud.Abilities.Fighter;
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

import java.io.IOException;
import java.util.*;

/*
   Copyright 2023-2023 Bo Zimmerman

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
public class Fighter_RideToTheRescue extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_RideToTheRescue";
	}

	private final static String localizedName = CMLib.lang().L("Ride To The Rescue");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"RRESCUE"});
	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_ACROBATIC;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Rideable ridden = mob.riding();
		if(!auto)
		{
			if((!CMLib.flags().isMobileMounted(mob))||(ridden == null))
			{
				mob.tell(L("You must be riding a mount to use this skill."));
				return false;
			}
			if(mob.isInCombat())
			{
				mob.tell(L("You are too busy fighting to do that."));
				return false;
			}
			if(ridden.numRiders()>=ridden.riderCapacity())
			{
				mob.tell(L("You've no free space on @x1 to do that!",ridden.name(mob)));
				return false;
			}
		}

		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		final MOB monster=target.getVictim();
		if((target.amDead())||(monster==null)||(monster.amDead()))
		{
			mob.tell(L("@x1 isn't fighting anyone!",target.charStats().HeShe()));
			return false;
		}
		if(!auto)
		{
			if(!mob.getGroupMembers(new XTreeSet<MOB>()).contains(target))
			{
				mob.tell(L("@x1 is not a member of your group, and might not appreciate being rescued.",target.name(mob)));
				return false;
			}
			if((ridden !=null) && (target.phyStats().weight()>ridden.phyStats().weight()/2))
			{
				mob.tell(L("@x1 is too large to scoop up.",target.name(mob)));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			str=L("<S-NAME> ride(s) in to rescue <T-NAMESELF>!");
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.makePeace(false);
				for(final Enumeration<MOB> m = mob.location().inhabitants();m.hasMoreElements();)
				{
					final MOB M = m.nextElement();
					if((M != null)
					&&(M.getVictim()==target))
						M.makePeace(false);
				}
				if(ridden != null)
				{
					final Command mountC = CMClass.getCommand("Mount");
					final String mountNm = mob.location().getContextName(ridden);
					final List<String> V = new XVector<String>("MOUNT",mountNm);
					try
					{
						mountC.execute(target, V, MUDCmdProcessor.METAFLAG_FORCED);
					}
					catch (final IOException e)
					{
						Log.errOut(ID(),e);
					}
				}
			}
		}
		else
		{
			str=L("<S-NAME> ride(s) in and attempt(s) to rescue <T-NAMESELF>, but fail(s).");
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,str);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}

		return success;
	}

}
