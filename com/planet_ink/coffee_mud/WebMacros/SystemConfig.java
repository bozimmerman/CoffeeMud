package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.CompoundingRule;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class SystemConfig extends StdWebMacro
{
	@Override
	public String name()
	{
		return "SystemConfig";
	}

	protected String returnDisabledValue(final boolean enabled, final boolean disabled, final boolean value)
	{
		if(enabled)
			return "false";
		else
		if(disabled)
			return "true";
		return "";
	}

	protected String returnEnabledValue(final boolean enabled, final boolean disabled, final String value)
	{
		if(enabled)
			return "true";
		else
		if(disabled)
			return "false";
		return value;
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);

		final boolean enabled = parms.containsKey("ENABLED");
		final boolean disabled = parms.containsKey("DISABLED");
		final boolean value = parms.containsKey("VALUE");

		if(parms.containsKey("MANACOMPOUND"))
		{
			final Enumeration<CompoundingRule> compoundingRules=CMLib.ableMapper().compoundingRules();

			if(compoundingRules.hasMoreElements())
			{
				if(parms.containsKey("COOLDOWN"))
				{
					for(final Enumeration<CompoundingRule> c=compoundingRules;c.hasMoreElements();)
					{
						final CompoundingRule rule=c.nextElement();
						if((rule.mobMask()==null)
						&&(rule.compoundingTicks()>0)
						&&(rule.amtPenalty()<0))
							return returnEnabledValue(enabled, disabled,""+rule.compoundingTicks());
					}
				}
				else
				{
					for(final Enumeration<CompoundingRule> c=compoundingRules;c.hasMoreElements();)
					{
						final CompoundingRule rule=c.nextElement();
						if((rule.mobMask()==null)
						&&(rule.compoundingTicks()>0)
						&&(rule.amtPenalty()>-1)
						&&((rule.amtPenalty()>0)||(rule.pctPenalty()>0)))
							return returnEnabledValue(enabled, disabled,""+rule.compoundingTicks());
					}
				}
				return returnDisabledValue(enabled, disabled, value);
			}
			return returnDisabledValue(enabled, disabled, value);
		}
		return "";
	}
}
