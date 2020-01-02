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
public class Song_Serenity extends Song
{
	@Override
	public String ID()
	{
		return "Song_Serenity";
	}

	private final static String localizedName = CMLib.lang().L("Serenity");

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
	protected boolean HAS_QUANTITATIVE_ASPECT()
	{
		return false;
	}

	@Override
	protected boolean maliciousButNotAggressiveFlag()
	{
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(affected==null)
			return super.okMessage(myHost,msg);
		if(!(affected instanceof MOB))
			return super.okMessage(myHost,msg);
		if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
		&&(CMLib.flags().canBeHeardSpeakingBy(invoker,msg.source()))
		&&(msg.target() instanceof MOB)
		&&((!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
			||((msg.tool() instanceof Ability)&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)))
		&&((invoker()==null)
			||(CMLib.dice().rollPercentage()>((msg.source().phyStats().level()-invoker().phyStats().level()-getXLEVELLevel(invoker()))*20)))
		)
		{
			if((msg.tool() instanceof Ability)
			&&(((Ability)msg.tool()).invoker()==invoker)
			&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
			&&(msg.target() instanceof MOB)
			&&(((MOB)msg.target()).fetchEffect(msg.tool().ID())!=null)
			&&(((Ability)msg.tool()).canBeUninvoked()))
				((Ability)msg.tool()).unInvoke();
			else
				msg.source().tell(L("You feel too peaceful to fight."));
			msg.source().makePeace(true);
			if(msg.target() instanceof MOB)
			{
				final MOB targetM=(MOB)msg.target();
				if(targetM.getVictim()==msg.source())
					targetM.setVictim(null);
			}
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
