package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2023-2024 Bo Zimmerman

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
public class GenGiftContainer extends GenContainer
{
	@Override
	public String ID()
	{
		return "GenGiftContainer";
	}

	public GenGiftContainer()
	{
		super();
		setName("a generic giftwrapped container");
		basePhyStats.setWeight(1);
		setDisplayText("a generic giftwrapped container sits here.");
		setDescription("Open it and find out what's in it!");
		baseGoldValue=1;
		material=RawMaterial.RESOURCE_PAPER;
		capacity=50;
		super.hasALid=true;
		super.isOpen=true;
		recoverPhyStats();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(msg.amITarget(this) && (msg.targetMinor()==CMMsg.TYP_OPEN))
		{
			if((!hasALid)||(!isOpen)||(isLocked))
				return;
			super.emptyPlease(false);
			destroy();
		}
	}
}
