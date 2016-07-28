package com.planet_ink.coffee_mud.Abilities.Skills;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2016 Bo Zimmerman

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
public class Skill_MorseCode extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_MorseCode";
	}

	private final static String	localizedName	= CMLib.lang().L("Morse Code");

	@Override
	public String name()
	{
		return localizedName;
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
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_SEATRAVEL;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "MORSECODE", "MORSE" });

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
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(!msg.amISource((MOB)affected))
		&&(msg.tool() instanceof Ability)
		&&(msg.tool().ID().equals("Skill_MorseCode"))
		&&(msg.sourceMinor()==CMMsg.NO_EFFECT)
		&&(msg.targetMinor()==CMMsg.NO_EFFECT)
		&&(msg.targetMessage()!=null)
		&&(msg.othersMessage()!=null))
			msg.addTrailerMsg(CMClass.getMsg((MOB)affected,null,null,CMMsg.MSG_OK_VISUAL,L("The morse code signals seem to say '@x1'.",msg.targetMessage()),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((!auto)&&(mob.isInCombat()))
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}

		Room R=mob.location();

		if(commands.size()==0)
		{
			if(mob.isMonster())
				commands.add(L("@x1 is over here!",mob.Name()));
			else
			{
				mob.tell(L("You need to specify the message to send using morse code."));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,auto?L("<T-NAME> begin(s) morse coding!"):L("<S-NAME> puff(s) up a mighty series of smoke signals!"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final String str=CMParms.combine(commands,0);
				final CMMsg msg2=CMClass.getMsg(mob,null,this,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,str,CMMsg.MSG_OK_VISUAL,L("You see some smoke signals in the distance."));
				final TrackingLibrary.TrackingFlags flags=CMLib.tracking().newFlags();
				final List<Room> checkSet=CMLib.tracking().getRadiantRooms(mob.location(),flags,50);
				for(final Iterator<Room> r=checkSet.iterator();r.hasNext();)
				{
					R=r.next();
					if((R!=null)&&(R.okMessage(mob,msg2)))
						R.sendOthers(msg.source(),msg2);
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> attempt(s) to send morse code, but mess(es) it up."));
		return success;
	}
}
