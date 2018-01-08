package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2011-2018 Bo Zimmerman

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

public class Prayer_SeekersPrayer extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_SeekersPrayer";
	}

	private final static String localizedName = CMLib.lang().L("Seekers Prayer");

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
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.isInCombat())&&(!auto))
		{
			mob.tell(L("Not while you're fighting!"));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Physical target=mob;
		if((auto)&&(givenTarget!=null))
			target=givenTarget;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,null,auto),auto?"":L("^S<T-NAME> @x1 for knowledge of seekers.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int numSeekers=super.getXLEVELLevel(mob) + 2;
				if(CMLib.ableMapper().qualifyingLevel(mob, this)>1)
					numSeekers += (super.adjustedLevel(mob, 0) / CMLib.ableMapper().qualifyingLevel(mob, this));
				final List<Quest> seeks=new Vector<Quest>();
				for(final Enumeration<Quest> q = CMLib.quests().enumQuests(); q.hasMoreElements();)
				{
					final Quest Q = q.nextElement();
					final MOB M=Q.getQuestMob(1);
					if( Q.name().equalsIgnoreCase("holidays")
					|| !Q.running()
					|| (M==null)
					|| (!CMLib.flags().isInTheGame(M,true))
					|| (!CMLib.flags().canAccess(mob, M.location())) )
					{
						continue;
					}
					seeks.add(Q);
				}
				if(seeks.size()==0)
					mob.tell(L("You receive no visions of seekers."));
				else
				{
					while(seeks.size() > numSeekers)
						seeks.remove(CMLib.dice().roll(1, seeks.size(), -1));
					String starting;
					switch(CMLib.dice().roll(1, 10, 0))
					{
					case 1: starting="The visions show an image of "; break;
					case 2: starting="You see "; break;
					case 3: starting="You receive divine feelings of "; break;
					case 4: starting="A voice tells you of"; break;
					case 5: starting="Someone whispers about"; break;
					case 6: starting="It is revealed to you that"; break;
					case 7: starting="In your visions, you see "; break;
					case 8: starting="In your mind you hear about"; break;
					case 9: starting="Your spirit tells you about"; break;
					default: starting="You know of"; break;
					}
					final StringBuilder message=new StringBuilder(starting);
					for(int p=0;p<seeks.size();p++)
					{
						final Quest Q=seeks.get(p);
						final MOB M=Q.getQuestMob(1);
						if((p==seeks.size()-1)&&(p>0))
							message.append(", and");
						else
						if(p>0)
							message.append(",");
						message.append(" ").append(M.name(mob)).append(" in \"").append(M.location().getArea().name(mob)).append("\"");
					}
					message.append(".");
					mob.tell(message.toString());
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<T-NAME> @x1, but nothing is revealed.",prayWord(mob)));

		return success;
	}
}
