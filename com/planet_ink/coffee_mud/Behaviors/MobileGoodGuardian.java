package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class MobileGoodGuardian extends Mobile
{
	@Override
	public String ID()
	{
		return "MobileGoodGuardian";
	}

	@Override
	public String accountForYourself()
	{
		return "wandering protectiveness against aggression, evilness, or thieflyness";
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		tickStatus=Tickable.STATUS_MISC+0;
		super.tick(ticking,tickID);

		tickStatus=Tickable.STATUS_MISC+1;
		if(tickID!=Tickable.TICKID_MOB)
		{
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
		if(!canFreelyBehaveNormal(ticking))
		{
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}
		final MOB mob=(MOB)ticking;

		// ridden things dont wander!
		if(ticking instanceof Rideable)
		{
			if(((Rideable)ticking).numRiders()>0)
			{
				tickStatus=Tickable.STATUS_NOT;
				return true;
			}
		}
		tickStatus=Tickable.STATUS_MISC+2;
		if(((mob.amFollowing()!=null)&&(mob.location()==mob.amFollowing().location()))
		||(!CMLib.flags().canTaste(mob)))
		{
			tickStatus=Tickable.STATUS_NOT;
			return true;
		}

		tickStatus=Tickable.STATUS_MISC+3;
		final Room thisRoom=mob.location();
		MOB victim=GoodGuardian.anyPeaceToMake(mob.location(),mob);
		GoodGuardian.keepPeace(mob,victim);
		victim=null;
		int dirCode=-1;
		tickStatus=Tickable.STATUS_MISC+4;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			tickStatus=Tickable.STATUS_MISC+5+d;
			final Room room=thisRoom.getRoomInDir(d);
			final Exit exit=thisRoom.getExitInDir(d);
			if((room!=null)&&(exit!=null)&&(okRoomForMe(mob,thisRoom,room,false)))
			{
				tickStatus=Tickable.STATUS_MISC+20+d;
				if(exit.isOpen())
				{
					tickStatus=Tickable.STATUS_MISC+40+d;
					victim=GoodGuardian.anyPeaceToMake(room,mob);
					if(victim!=null)
					{
						dirCode=d;
						break;
					}
					tickStatus=Tickable.STATUS_MISC+60+d;
				}
				tickStatus=Tickable.STATUS_MISC+80+d;
			}
			if(dirCode>=0)
				break;
			tickStatus=Tickable.STATUS_MISC+100+d;
		}
		tickStatus=Tickable.STATUS_MISC+120;
		if((dirCode>=0)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MOBILITY)))
		{
			tickStatus=Tickable.STATUS_MISC+121;
			CMLib.tracking().walk(mob,dirCode,false,false);
			tickStatus=Tickable.STATUS_MISC+122;
			GoodGuardian.keepPeace(mob,victim);
			tickStatus=Tickable.STATUS_MISC+123;
		}
		tickStatus=Tickable.STATUS_NOT;
		return true;
	}
}
