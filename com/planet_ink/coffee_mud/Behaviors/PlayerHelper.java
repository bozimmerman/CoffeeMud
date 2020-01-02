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
public class PlayerHelper extends StdBehavior
{
	@Override
	public String ID()
	{
		return "PlayerHelper";
	}

	@Override
	public String accountForYourself()
	{
		return "protectiveness of heroes";
	}

	protected int		num			= 999;
	protected String	msg			= null;

	@Override
	public void startBehavior(final PhysicalAgent forMe)
	{
		super.startBehavior(forMe);
		if(forMe instanceof MOB)
		{
			if(parms.length()>0)
			{
				msg=CMParms.getParmStr(parms, "MSG", null);
				final List<String> V=CMParms.parse(parms.toUpperCase());
				for(int i=V.size()-1;i>=0;i--)
				{
					if(CMath.isInteger(V.get(i)))
					{
						num=CMath.s_int(V.get(i));
						V.remove(i);
					}
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental affecting, final CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((msg.target()==null)||(!(msg.target() instanceof MOB)))
			return;
		final MOB mob=msg.source();
		final MOB monster;
		if(affecting instanceof MOB)
			monster=(MOB)affecting;
		else
		if((affecting instanceof Item)&&(((Item)affecting).owner() instanceof MOB))
			monster=(MOB)((Item)affecting).owner();
		else
			return;
		final MOB target=(MOB)msg.target();

		if((mob!=monster)
		&&(target!=monster)
		&&(mob!=target)
		&&(CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(!monster.isInCombat())
		&&(CMLib.flags().canBeSeenBy(mob,monster))
		&&(CMLib.flags().canBeSeenBy(target,monster))
		&&(!target.isMonster()))
		{
			final Room R=mob.location();
			if(R!=null)
			{
				int numInFray=0;
				if((num > 0) && (num < 999))
				{
					for(int m=0;m<R.numInhabitants();m++)
					{
						final MOB M=R.fetchInhabitant(m);
						if((M!=null)&&(M.getVictim()==mob))
							numInFray++;
					}
				}
				if(((num==0)||(numInFray<num)))
					Aggressive.startFight(monster,mob,false,false,this.msg);
			}
		}
	}
}
