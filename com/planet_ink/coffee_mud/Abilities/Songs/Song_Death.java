package com.planet_ink.coffee_mud.Abilities.Songs;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2020 Bo Zimmerman

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
public class Song_Death extends Song
{
	@Override
	public String ID()
	{
		return "Song_Death";
	}

	private final static String localizedName = CMLib.lang().L("Death");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int getXMAXRANGELevel(final MOB mob)
	{
		return 0;
	} // people are complaining about multi-room death

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		final MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		if(mob==invoker)
			return true;
		final MOB invoker=(invoker()!=null) ? invoker() : mob;
		int hpLoss=(int)Math.round(Math.floor(mob.curState().getHitPoints()*(0.07+(0.02*(1+super.getXLEVELLevel(invoker()))))));
		if(invoker != null)
		{
			final Room R=invoker.location();
			if((R != mob.location())
			&&(R!=null))
			{
				hpLoss /= 4;
				boolean found=false;
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					final Room R2=R.getRoomInDir(d);
					if(mob.location()==R2)
						found=true;
				}
				if(!found)
					hpLoss /= 4;
				if(hpLoss < 1)
					hpLoss=1;
			}
			CMLib.combat().postDamage(invoker,mob,this,hpLoss,CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,L("^SThe painful song <DAMAGE> <T-NAME>!^?"));
		}
		return true;
	}

}
