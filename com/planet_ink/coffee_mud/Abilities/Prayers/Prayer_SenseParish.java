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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;

/*
   Copyright 2020-2024 Bo Zimmerman

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
public class Prayer_SenseParish extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_SenseParish";
	}

	private final static String localizedName = CMLib.lang().L("Sense Parish");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_COMMUNING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int overrideMana()
	{
		return 100;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		String deityName=CMParms.combine(commands);
		if(deityName.length()==0)
		{
			mob.tell(L("Sense the parish of which deity?  Use DEITIES for a list."));
			return false;
		}

		final Deity D=CMLib.map().getDeity(deityName);
		if(D==null)
		{
			mob.tell(L("You don't know any deity called '@x1'.",deityName));
			return false;
		}
		deityName=D.Name();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),L("^S<S-NAME> @x1 for knowledge of @x2's parish^?",prayWord(mob),D.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int exper=super.getXLEVELLevel(mob);
				final int range=(adjustedLevel(mob,asLevel)/2) + (2*exper)+(10*super.getXMAXRANGELevel(mob));
				final Faction incliF;
				if(CMLib.factions().isFactionLoaded(CMLib.factions().getInclinationID()))
					incliF=CMLib.factions().getFaction(CMLib.factions().getInclinationID());
				else
					incliF=null;
				final Faction alignF;
				if(CMLib.factions().isFactionLoaded(CMLib.factions().getAlignmentID()))
					alignF=CMLib.factions().getFaction(CMLib.factions().getAlignmentID());
				else
					alignF=null;
				final StringBuilder report=new StringBuilder("");
				for(final Enumeration<Room> r=CMLib.tracking().getRadiantRoomsEnum(mob.location(), null, null, range, null);r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					int countThisRoom=0;
					for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if((M!=null)
						&&(M!=mob)
						&&(M.charStats().deityName().equals(deityName)))
						{
							countThisRoom++;
							if(exper>2)
							{
								String alignStr="";
								if((exper>4)
								&&(incliF!=null)
								&&(M.fetchFaction(incliF.factionID())!=Integer.MAX_VALUE))
								{
									final Faction.FRange frange=incliF.fetchRange(M.fetchFaction(incliF.factionID()));
									if(frange!=null)
										alignStr=frange.name()+" ";
								}
								if((exper>6)
								&&(alignF!=null)
								&&(M.fetchFaction(alignF.factionID())!=Integer.MAX_VALUE))
								{
									final Faction.FRange frange=alignF.fetchRange(M.fetchFaction(alignF.factionID()));
									if(frange!=null)
										alignStr=((alignStr+frange.name()).trim())+" ";
								}
								String levelStr="";
								if(exper>8)
									levelStr=L("level @x1 ",""+M.phyStats().level());
								report.append(L("Found @x1@x2@x3 in @x4.\n\r",levelStr,alignStr,
										CMLib.english().removeArticleLead(M.name(mob)),R.displayText(mob)));
							}
						}
					}
					if((countThisRoom>0)
					&&(exper<3))
					{
						if(exper>0)
							report.append(L("Found @x1 in @x2.\n\r",""+countThisRoom,R.displayText(mob)));
						else
							report.append(L("Found in @x1.\n\r",R.displayText(mob)));
					}
				}
				if(report.length()==0)
					mob.tell(L("You don't sense any of @x1's followers nearby.",D.name(mob)));
				else
					mob.tell(report.toString());
			}
		}
		else
			beneficialWordsFizzle(mob,null,auto?"":L("<S-NAME> @x1 for knowledge of @x2's parish, but nothing happens.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
