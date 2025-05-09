package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2023-2025 Bo Zimmerman

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
public class Paladin_Corruption extends PaladinSkill
{
	@Override
	public String ID()
	{
		return "Paladin_Corruption";
	}

	private final static String localizedName = CMLib.lang().L("Paladin`s Corruption");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_CORRUPTION;
	}

	public Paladin_Corruption()
	{
		super();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(!(affected instanceof MOB))
			return true;

		if((msg.sourceMinor()==CMMsg.TYP_FACTIONCHANGE)
		&&(msg.source()==affected)
		&&(msg.tool()!=null)
		&&(!msg.source().isMine(msg.tool()))
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().equalsIgnoreCase(CMLib.factions().getAlignmentID())
			||msg.othersMessage().equalsIgnoreCase(CMLib.factions().getInclinationID()))
		&&(appropriateToMyFactions(invoker))
		&&(proficiencyCheck(msg.source(), 0, false))
		&&(msg.value()<0))
		{
			msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> corruption enhance(s) <S-HIS-HER> wicked influence."));
			msg.setValue(msg.value()*2);
			return false;
		}
		return true;
	}
}
