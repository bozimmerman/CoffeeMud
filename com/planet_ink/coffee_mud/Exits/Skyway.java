package com.planet_ink.coffee_mud.Exits;
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
public class Skyway extends Open
{
	@Override
	public String ID()
	{
		return "Skyway";
	}

	public Skyway()
	{
		super();
		recoverPhyStats();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)
			||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		{
			final MOB mob=msg.source();
			final Room R=mob.location();
			if((R != null)
			&&(CMLib.map().hasASky(R))
			&&(R.getArea()!=null)
			&&(CMLib.flags().canSee(mob)))
			{
				msg.addTrailerRunnable(new Runnable() {
					final Room room = R;
					final Area A = R.getArea();
					final MOB tellM = mob;
					final Climate C = A.getClimateObj();
					@Override
					public void run()
					{
						final StringBuilder str = new StringBuilder("");
						switch(A.getTimeObj().getTODCode())
						{
						case DAWN:
						case DUSK:
							str.append(L("It is @x1.  ",A.getTimeObj().getTODCode().name().toLowerCase()));
							break;
						case DAY:
						case NIGHT:
							str.append(L("It is @x1time.  ",A.getTimeObj().getTODCode().name().toLowerCase()));
							break;
						}
						str.append(C.weatherDescription(room)).append("  ");
						if(C.canSeeTheMoon(room, null))
							str.append(room.getArea().getTimeObj().getMoonPhase(room).getDesc());
						tellM.tell(str.toString());
					}
				});
			}
		}
	}
}
