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
   Copyright 2022-2023 Bo Zimmerman

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
public class Chant_SummonFog extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SummonFog";
	}

	private int oldWeatherType = -1;
	
	private final static String localizedName = CMLib.lang().L("Summon Fog");

	@Override
	public String name()
	{
		return localizedName;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_WEATHERAFFECTING;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL - 99;
	}
	
	@Override
	public void unInvoke()
	{
		final Physical affected = this.affected;
		boolean undo = super.canBeUninvoked();
		super.unInvoke();
		if(undo && (this.oldWeatherType >=0) && (affected instanceof Area))
		{
			final Area A=(Area)affected;
			final Climate C=A.getClimateObj();
			C.setNextWeatherType(this.oldWeatherType);
			C.forceWeatherTick(A);
		}
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room room=mob.location();
		if(room==null)
			return false;
		final Area A=room.getArea();
		if(((room.domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		if((A.fetchEffect(ID())!=null)||(A.getClimateObj().weatherType(room)==Climate.WEATHER_FOG))
		{
			mob.tell(L("It is already foggy here."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int size=A.numberOfProperIDedRooms();
		size=size/(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(size<0)
			size=0;
		final boolean success=proficiencyCheck(mob,-size,auto);
		if(success)
		{
			final Climate C=A.getClimateObj();
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?L("^JThe air becomes foggy!^?"):L("^S<S-NAME> chant(s) into the air for fog!^?"));
			if(room.okMessage(mob,msg))
			{
				room.send(mob,msg);
				final int oldWeatherCode = C.weatherType(null); /// null room gets raw weather code
				C.setNextWeatherType(Climate.WEATHER_FOG);
				C.forceWeatherTick(A);
				final Chant_SummonFog fogA = (Chant_SummonFog)beneficialAffect(mob, A, asLevel, 10);
				if(fogA!=null)
					fogA.oldWeatherType = oldWeatherCode;
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) into the air for fog, but the magic fizzles."));

		return success;
	}
}
