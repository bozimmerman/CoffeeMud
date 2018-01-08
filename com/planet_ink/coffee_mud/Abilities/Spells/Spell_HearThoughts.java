package com.planet_ink.coffee_mud.Abilities.Spells;
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

public class Spell_HearThoughts extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_HearThoughts";
	}

	private final static String localizedName = CMLib.lang().L("Hear Thoughts");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> concentrate(s) and listen(s) carefully!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int range=50 + super.getXLEVELLevel(mob)+(2*super.getXMAXRANGELevel(mob));
				final List<Room> rooms=CMLib.tracking().getRadiantRooms(mob.location(), CMLib.tracking().newFlags(), range);
				final List<MOB> mobs=new LinkedList<MOB>();
				int numMobs= 8 + super.getXLEVELLevel(mob);
				for(final Room R : rooms)
				{
					for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if((numMobs>0)&&(M!=mob))
						{
							mobs.add(M);
							numMobs--;
						}
					}
					if(numMobs<=0)
						break;
				}
				rooms.clear();
				for(final MOB target : mobs)
				{
					final Room room=target.location();
					if(room==null)
						continue;
					String adjective="";
					if(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>=18)
						adjective+="massively intelligent, ";
					else
					if(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>=13)
						adjective+="very intelligent, ";
					else
					if(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>=10)
						adjective+="intelligent, ";
					if(target.charStats().getStat(CharStats.STAT_WISDOM)>=18)
						adjective+="incredibly wise, ";
					else
					if(target.charStats().getStat(CharStats.STAT_WISDOM)>=13)
						adjective+="very wise, ";
					else
					if(target.charStats().getStat(CharStats.STAT_WISDOM)>=10)
						adjective+="wise, ";
					mob.tell(L("Regarding @x1, a @x2@x3 @x4 at @x5:",target.Name(),adjective,target.charStats().getMyRace().name(),target.charStats().getCurrentClass().name(),room.displayText(mob)));
					final StringBuilder thoughts=new StringBuilder("");
					final LegalBehavior LB=CMLib.law().getLegalBehavior(target.location());
					final Area AO=CMLib.law().getLegalObject(target.location());
					if((LB!=null)&&(AO!=null))
					{
						if(LB.isJudge(AO, target))
							thoughts.append("You detect the legalese thoughts of a judge.  ");
						else
						if(LB.isAnyOfficer(AO, target))
							thoughts.append("You detect the stern thoughts of a law officer.  ");
					}
					for(final Enumeration<Behavior> b=target.behaviors();b.hasMoreElements();)
					{
						final Behavior B=b.nextElement();
						final String accounting=B.accountForYourself();
						if(accounting.length()==0)
							continue;
						String prefix;
						switch(CMLib.dice().roll(1, 4, 0))
						{
						case 1: prefix="You sense thoughts of "; break;
						case 2: prefix="You hear thoughts of "; break;
						case 3: prefix="You detect thoughts of "; break;
						default: prefix="You can see thoughts of "; break;
						}
						thoughts.append(prefix).append(accounting).append("  ");
					}
					if(thoughts.length()==0)
						mob.tell(L("You don't detect any other thoughts.\n\r"));
					else
						mob.tell(thoughts.append("\n\r").toString());
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> concentrate(s), but look(s) frustrated."));

		// return whether it worked
		return success;
	}
}
