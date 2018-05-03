package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.PlanarAbility;
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

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

public class Chant_PlaneWalking extends PlanarAbility
{
	@Override
	public String ID()
	{
		return "Chant_PlaneWalking";
	}

	private final static String	localizedName	= CMLib.lang().L("Plane Walking");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_CONJURATION;
	}

	@Override
	public long flags()
	{
		return 0;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL - 90;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "CHANT", "CH" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}
	
	@Override
	protected String castingMessage(MOB mob, boolean auto)
	{
		return auto?L("<S-NAME> <S-IS-ARE> drawn to another plane of existence!"):L("^S<S-NAME> walks around chanting!^?");
	}
	
	@Override
	protected String failMessage(MOB mob, boolean auto)
	{
		return L("^S<S-NAME> attempt(s) to chant, and fails.");
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!Chant.chantAlignmentCheck(this,mob,false,auto))
			return false;
		final Room R=mob.location();
		if((R!=null)&&(!auto))
		{
			this.alwaysRandomArea=true;
			if(super.getPlanarAbility(R.getArea())==null)
			{
				final boolean hereok=mob.location().findItem(null,"DruidicMonument")!=null;
				if(!hereok)
				{
					mob.tell(L("There is no druidic monument here.  You can only begin this chant in a druidic grove."));
					return false;
				}
			}
			else
			{
				final Item otherPlant = Druid_MyPlants.myPlant(R, mob, 0);
				if(otherPlant == null)
				{
					mob.tell(L("There is none of your plants here.  You can travel from here through one of your plants."));
					return false;
				}
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		return true;
	}
}
