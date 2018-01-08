package com.planet_ink.coffee_mud.Locales;
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
public class HideoutShelter extends MagicShelter
{
	@Override
	public String ID()
	{
		return "HideoutShelter";
	}

	public HideoutShelter()
	{
		super();
		name="the hideout";
		displayText=L("Secret Hideout");
		setDescription("You are in a small dark room.");
		basePhyStats.setWeight(0);
		basePhyStats.setDisposition(PhyStats.IS_DARK);
		recoverPhyStats();
		Ability A=CMClass.getAbility("Prop_PeaceMaker");
		if(A!=null)
		{
			A.setSavable(false);
			addEffect(A);
		}
		A=CMClass.getAbility("Prop_NoRecall");
		if(A!=null)
		{
			A.setSavable(false);
			addEffect(A);
		}
		A=CMClass.getAbility("Prop_NoSummon");
		if(A!=null)
		{
			A.setSavable(false);
			addEffect(A);
		}
		A=CMClass.getAbility("Prop_NoTeleport");
		if(A!=null)
		{
			A.setSavable(false);
			addEffect(A);
		}
		A=CMClass.getAbility("Prop_NoTeleportOut");
		if(A!=null)
		{
			A.setSavable(false);
			addEffect(A);
		}
		climask=Places.CLIMASK_NORMAL;
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_INDOORS_WOOD;
	}
}
