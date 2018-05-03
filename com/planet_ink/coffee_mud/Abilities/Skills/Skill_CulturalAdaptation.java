package com.planet_ink.coffee_mud.Abilities.Skills;
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
   Copyright 2016-2018 Bo Zimmerman

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
public class Skill_CulturalAdaptation extends StdSkill
{
	@Override
	public String ID()
	{
		return "Skill_CulturalAdaptation";
	}

	private final static String	localizedName	= CMLib.lang().L("Cultural Adaption");

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
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_INFLUENTIAL;
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
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		return super.tick(ticking,tickID);
	}

	protected double amtPerExp = 0.05;
	protected double baseAdjustment = 0.20;
	
	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()==0)
		{
			if(CMath.isPct(newMiscText))
			{
				amtPerExp = 0.0;
				baseAdjustment = CMath.s_pct(newMiscText);
			}
			else
			if(CMath.isInteger(newMiscText))
			{
				amtPerExp = 0.0;
				baseAdjustment = CMath.s_double(newMiscText) / 100.0;
			}
		}
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.sourceMinor()== CMMsg.TYP_FACTIONCHANGE)
		&&(msg.othersMessage()!=null)
		&&(!msg.othersMessage().equalsIgnoreCase(CMLib.factions().AlignID()))
		&&(msg.value()>0)
		&&(msg.value()<Integer.MAX_VALUE))
		{
			double prof=super.getXLEVELLevel(invoker());
			msg.setValue((int)Math.round(msg.value() * (1.0 + baseAdjustment + (prof*amtPerExp))));
		}
		super.executeMsg(myHost, msg);
	}

}

