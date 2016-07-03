package com.planet_ink.coffee_mud.Abilities.Languages;
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

public class Semaphore extends StdLanguage
{
	@Override
	public String ID()
	{
		return "Semaphore";
	}

	private final static String localizedName = CMLib.lang().L("Semaphore");
	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String translate(String language, String word)
	{
		return fixCase(word,"flag");
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected instanceof MOB)
		&&(beingSpoken(ID()))
		&&(msg.source()==affected)
		&&(msg.sourceMessage()!=null)
		&&(msg.tool()==null)
		&&(msg.sourceMinor()==CMMsg.TYP_SPEAK))
		{
			final MOB mob=(MOB)affected;
			Room R=CMLib.map().roomLocation(mob);
			if(!super.okMessage(myHost, msg))
				return false;
			if((msg.tool()==this)&&(R!=null))
			{
				if((R.getArea() instanceof BoardableShip)
				&&((R.domainType()&Room.INDOORS)==0))
				{
					final Room room=CMLib.map().roomLocation(((BoardableShip)R.getArea()).getShipItem());
					if(room != null)
					{
						final CMMsg outerMsg=(CMMsg)msg.copyOf();
						msg.modify(msg.source(), null,null, CMMsg.NO_EFFECT,null);
						msg.addTrailerRunnable(new Runnable(){
							@Override
							public void run()
							{
								room.send(msg.source(), outerMsg);
							}
						});
					}
				}
			}
			return true;
		}
		else
			return super.okMessage(myHost, msg);
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!auto)
		{
			boolean isCurrentlySpeaking = false;
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A!=null)&&(A instanceof Language))
				{
					if(A.ID().equals(ID()))
						isCurrentlySpeaking = ((Language)A).beingSpoken(ID());
				}
			}
			if(!isCurrentlySpeaking)
			{
				final Room R=mob.location();
				if((R!=null)
				&&(R.getArea() instanceof BoardableShip)
				||((mob.riding()!=null)&&(mob.riding().rideBasis()==Rideable.RIDEABLE_WATER)))
				{
					// fine.
				}
				else
				{
					mob.tell(L("You must be on a ship or boat to speak this."));
					return false;
				}
			}
		}
		
		super.invoke(mob, commands, givenTarget, auto, asLevel);
		
		return true;
	}

}
