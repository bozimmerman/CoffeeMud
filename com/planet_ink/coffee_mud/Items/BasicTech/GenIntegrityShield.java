package com.planet_ink.coffee_mud.Items.BasicTech;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2025 Bo Zimmerman

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
public class GenIntegrityShield extends GenPersonalShield
{
	@Override
	public String ID()
	{
		return "GenIntegrityShield";
	}

	public GenIntegrityShield()
	{
		super();
		setName("an integrity shield generator");
		setDisplayText("an integrity shield generator sits here.");
		setDescription("The integrity shield generator is worn about the body and activated to use. It protects against disruption and disintegration beams. ");
	}

	@Override
	protected String fieldOnStr(final MOB viewerM)
	{
		return L((owner() instanceof MOB)?
			"An integrity field surrounds <O-NAME>.":
			"An integrity field surrounds <T-NAME>.");
	}

	@Override
	protected String fieldDeadStr(final MOB viewerM)
	{
		return L((owner() instanceof MOB)?
			"The integrity field around <O-NAME> flickers and dies out.":
			"The integrity field around <T-NAME> flickers and dies out.");
	}

	@Override
	protected boolean doShield(final MOB mob, final CMMsg msg, final double successFactor)
	{
		if(mob.location()!=null)
		{
			if(msg.tool() instanceof Weapon)
			{
				final String s="^F"+((Weapon)msg.tool()).hitString(0)+"^N";
				if(s.indexOf("<DAMAGE> <T-HIM-HER>")>0)
					mob.location().show(msg.source(),msg.target(),msg.tool(),CMMsg.MSG_OK_VISUAL,CMStrings.replaceAll(s, "<DAMAGE>", L("it is neutralized by the shield around")));
				else
				if(s.indexOf("<DAMAGES> <T-HIM-HER>")>0)
					mob.location().show(msg.source(),msg.target(),msg.tool(),CMMsg.MSG_OK_VISUAL,CMStrings.replaceAll(s, "<DAMAGES>", L("is neutralized by the shield around")));
				else
					mob.location().show(mob,msg.source(),msg.tool(),CMMsg.MSG_OK_VISUAL,L("The field around <S-NAME> neutralizes the <O-NAMENOART> damage."));
			}
			else
				mob.location().show(mob,msg.source(),msg.tool(),CMMsg.MSG_OK_VISUAL,L("The field around <S-NAME> neutralizes the <O-NAMENOART> damage."));
		}
		return false;
	}

	@Override
	protected boolean doesShield(final MOB mob, final CMMsg msg, final double successFactor)
	{
		if(!activated())
			return false;
		if((msg.tool() instanceof Electronics)
		&& (msg.tool() instanceof Weapon)
		&& (Math.random() >= successFactor)
		&& ((((Weapon)msg.tool()).weaponDamageType()==Weapon.TYPE_MELTING)||(((Weapon)msg.tool()).weaponDamageType()==Weapon.TYPE_SLASHING)))
		{
			return true;
		}
		return false;
	}
}
