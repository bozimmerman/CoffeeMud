package com.planet_ink.coffee_mud.Abilities.Druid;
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

public class Chant_PredictTides extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_PredictTides";
	}

	private final static String	localizedName	= CMLib.lang().L("Predict Tides");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_MOONSUMMONING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		boolean isWateryEnough = CMLib.flags().isWateryRoom(R);
		if(!isWateryEnough)
		{
			if((R.resourceChoices()!=null)
			&&(R.resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_FISH))))
				isWateryEnough = true;
			final Area A=R.getArea();
			if(A instanceof BoardableShip)
			{
				final Room R2=CMLib.map().roomLocation(((BoardableShip)A).getShipItem());
				if(R2!=null)
				{
					if(CMLib.flags().isWateryRoom(R2))
						isWateryEnough = true;
					else
					if((R2.resourceChoices()!=null)
					&&(R2.resourceChoices().contains(Integer.valueOf(RawMaterial.RESOURCE_FISH))))
						isWateryEnough = true;
				}
			}
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final String msgStr = isWateryEnough ? 
					 L("^S<S-NAME> chant(s) and gaze(s) upon the waters.^?")
					:L("^S<S-NAME> chant(s) and gaze(s) toward the distant waters.^?");
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":msgStr);
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				mob.tell(R.getArea().getTimeObj().getTidePhase(R).getDesc());
			}
		}
		else
		{
			final String msgStr = isWateryEnough ? 
					 L("^S<S-NAME> chant(s) and gaze(s) upon the waters, but the magic fizzles.^?")
					:L("^S<S-NAME> chant(s) and gaze(s) toward the distant waters, but the magic fizzles.^?");
			beneficialVisualFizzle(mob,null,msgStr);
		}

		return success;
	}
}
