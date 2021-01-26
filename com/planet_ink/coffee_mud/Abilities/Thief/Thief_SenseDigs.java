package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
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
   Copyright 2020-2021 Bo Zimmerman

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
public class Thief_SenseDigs extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_SenseDigs";
	}

	private final static String localizedName = CMLib.lang().L("Sense Digs");

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
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
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

	protected final LimitedTreeSet<Room> lastRooms=new LimitedTreeSet<Room>(3600000,200,false);

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_NATURELORE;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final Physical affected=this.affected;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			if((R!=null)
			&&(!mob.isMonster())
			&&(!lastRooms.contains(R)))
			{
				final StringBuffer buf=new StringBuffer("");
				if(Thief_Digsite.lastRooms.contains(R)
				&&(proficiencyCheck(mob,0,false)))
					buf.append(L("This place is a former Digsite.  "));
				final Item hI=R.findItem(null, "HoleInTheGround");
				if((hI!=null)&&(hI.ID().equalsIgnoreCase("HoleInTheGround")))
				{
					int totalValue=1;
					int highestLevel=0;
					if(!hI.readableText().equalsIgnoreCase(mob.Name()))
					{
						for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if((I!=null)
							&&(I.ultimateContainer(hI)==hI))
							{
								totalValue += I.baseGoldValue();
								if(I.phyStats().level()>highestLevel)
									highestLevel=I.phyStats().level();
							}
						}
					}
					if(totalValue > 0)
					{
						int levelNegs=(((adjustedLevel(mob,0)-highestLevel))*3);
						if(levelNegs>0)
							levelNegs=0;
						final int valueNegs = -(totalValue/100);
						if(proficiencyCheck(mob,levelNegs+valueNegs+(super.getXLEVELLevel(mob)*3),false))
							buf.append(L("This dirt here has been disturbed by a dig."));
					}
				}

				if(buf.length()>0)
				{
					mob.tell(L("You sense: @x1",buf.toString().trim()));
					helpProficiency(mob, 0);
				}
				lastRooms.add(R);
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean autoInvocation(final MOB mob, final boolean force)
	{
		return super.autoInvocation(mob, force);
	}
}
