package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2024-2025 Bo Zimmerman

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
public class FlowerArranging extends CommonSkill
{
	@Override
	public String ID()
	{
		return "FlowerArranging";
	}

	private final static String	localizedName	= CMLib.lang().L("Flower Arranging");

	@Override
	public String name()
	{
		return localizedName;
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
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ARTISTIC;
	}

	public FlowerArranging()
	{
		super();
		displayText="";
		canBeUninvoked=false;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if(((host instanceof MOB)
		&&(msg.amISource((MOB)host)))
		&&(msg.targetMinor()==CMMsg.TYP_PUT)
		&&(msg.target() instanceof Container)
		&&(!((Container)msg.target()).hasADoor())
		&&(msg.tool() instanceof Item)
		&&((((Item)msg.tool()).material()&RawMaterial.RESOURCE_MASK)==RawMaterial.RESOURCE_FLOWERS)
		&&(proficiencyCheck(null,(10*getXLEVELLevel((MOB)host)),false)))
		{
			if(CMLib.dice().rollPercentage()==1)
				helpProficiency((MOB)host,0);
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),msg.tool(),CMMsg.MSG_OK_VISUAL,L("<S-NAME> arrange(s) <O-NAME> in <T-NAME>")));
		}
		super.executeMsg(host,msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return true;
	}
}
