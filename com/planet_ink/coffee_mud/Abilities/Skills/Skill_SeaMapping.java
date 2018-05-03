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
   Copyright 2003-2018 Bo Zimmerman

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
public class Skill_SeaMapping extends Skill_Map
{
	@Override
	public String ID()
	{
		return "Skill_SeaMapping";
	}

	private final static String	localizedName	= CMLib.lang().L("Make Sea Maps");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Sea Mapping)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}
	
	private static final String[]	triggerStrings	= I(new String[] { "SEAMAPPING","SEAMAP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected boolean isTheMapMsg(final MOB mob, final MOB srcM)
	{
		if((mob!=null)&&(srcM!=null))
		{
			if((mob.riding() !=null) 
			&& (mob.riding().rideBasis() == Rideable.RIDEABLE_WATER)
			&&(mob == srcM))
				return true;
			
			if((srcM.riding() instanceof BoardableShip)
			&&(mob.location()!=null)
			&&(mob.location().getArea() == ((BoardableShip)srcM.riding()).getShipArea()))
				return true;
		}
		return false;
	}

	@Override
	protected Room getCurrentRoom(final MOB mob)
	{
		if((mob.riding() !=null) && (mob.riding().rideBasis() == Rideable.RIDEABLE_WATER))
			return mob.location();
		
		final Room R=mob.location();
		if(R!=null)
		{
			if(R.getArea() instanceof BoardableShip)
			{
				Item I = ((BoardableShip)R.getArea()).getShipItem();
				if(I!=null)
					return CMLib.map().roomLocation(I);
			}
		}
		return mob.location();
	}

	@Override
	protected String getMapClass()
	{
		return "SeaMap";
	}

	@Override
	protected boolean doExtraChecks(final MOB mob)
	{
		final Room R=mob.location();
		if(R!=null)
		{
			if((mob.riding() !=null) && (mob.riding().rideBasis() == Rideable.RIDEABLE_WATER))
				return true;
			
			if(R.getArea() instanceof BoardableShip)
			{
				return true;
			}
			else
			{
				mob.tell(L("This skill only works on board a ship or boat."));
				return false;
			}
		}
		return false;
	}
}
