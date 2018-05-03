package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2012-2018 Bo Zimmerman

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
public class Prop_ReRollStats extends Property
{
	@Override
	public String ID()
	{
		return "Prop_ReRollStats";
	}

	@Override
	public String name()
	{
		return "Re Roll Stats";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	protected int bonusPointsPerStat=0;
	protected boolean reRollFlag=true;
	protected boolean rePickClass=false;

	@Override
	public String accountForYourself()
	{
		return "Will cause a player to re-roll their stats.";
	}

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		bonusPointsPerStat=CMParms.getParmInt(newMiscText, "BONUSPOINTS", 0);
		rePickClass=CMParms.getParmBool(newMiscText, "PICKCLASS", false);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((reRollFlag)
		&&(affected instanceof MOB)
		&&(msg.sourceMinor()==CMMsg.TYP_LOOK)
		&&(msg.source()==affected))
		{
			final MOB M=msg.source();
			if((M.session()!=null)
			&&(M.playerStats()!=null))
			{
				final Ability me=this;
				CMLib.threads().executeRunnable(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							CMLib.login().promptBaseCharStats(M.playerStats().getTheme(), M, 300, M.session(), bonusPointsPerStat);
							M.recoverCharStats();
							if(rePickClass)
								M.baseCharStats().setCurrentClass(CMLib.login().promptCharClass(M.playerStats().getTheme(), M, M.session()));
							M.recoverCharStats();
							M.delEffect(me);
							M.baseCharStats().getCurrentClass().grantAbilities(M, false);
						}
						catch (final IOException e)
						{
						}
					}
				});
			}
		}
	}
}
