package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.Rideable.Basis;
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
   Copyright 2002-2023 Bo Zimmerman

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
public class Chant_CalmWind extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_CalmWind";
	}

	private final static String localizedName = CMLib.lang().L("Calm Wind");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_WEATHERAFFECTING;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_WEATHER_MASTERY;
	}

	public static void xpWorthyChange(final MOB mob, final Climate oldC, final Climate newC)
	{
		if((oldC.nextWeatherType(null)!=Climate.WEATHER_CLEAR)
		&&(oldC.nextWeatherType(null)!=Climate.WEATHER_CLOUDY)
		&&((newC.nextWeatherType(null)==Climate.WEATHER_CLEAR)
			||(newC.nextWeatherType(null)==Climate.WEATHER_CLOUDY))
		&&((newC.weatherType(null)==Climate.WEATHER_CLEAR)
				||(newC.weatherType(null)==Climate.WEATHER_CLOUDY)))
		{
			mob.tell(CMLib.lang().L("^YYou have restored balance to the weather!^N"));
			CMLib.leveler().postExperience(mob,null,null,25,false);
		}
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if(CMath.bset(weatherQue(R),WEATHERQUE_CALM))
					return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source().riding() instanceof NavigableItem)
		&&((msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			||((msg.targetMinor()==CMMsg.TYP_LEAVE)&&(msg.target() == affected)))
		&&(msg.source().isMonster())
		&&(((NavigableItem)msg.source().riding()).navBasis() == Basis.WATER_BASED)
		&&(!(msg.source().riding() instanceof Technical))
		&&(affected instanceof Room))
		{
			final Room R=(Room)affected;
			final Item I = ((NavigableItem)msg.source().riding()).getBoardableItem();
			if((R!=null)&&(I!=null))
				R.show(msg.source(), I, CMMsg.MSG_OK_VISUAL, L("The still air prevents @x1 from going anywhere.",I.name()));
			return false;
		}
		return super.okMessage(myHost, msg);
	}

	protected void sayToEveryoneInRoom(final Room R, final String say)
	{
		if(CMLib.map().hasASky(R))
		{
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB mob=R.fetchInhabitant(i);
				if((mob!=null)
				&&(!mob.isMonster()))
					mob.tell(say);
			}
		}
		for(final Enumeration<Boardable> s =CMLib.map().ships();s.hasMoreElements();)
		{
			final Boardable ship = s.nextElement();
			if((ship != null) && (R == CMLib.map().roomLocation(ship.getBoardableItem())))
			{
				final Area inA=ship.getArea();
				if(inA!=null)
				{
					for(final Enumeration<Room> r=inA.getFilledProperMap();r.hasMoreElements();)
						sayToEveryoneInRoom(r.nextElement(),say);
				}
			}
		}
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof Room)
				sayToEveryoneInRoom((Room)affected, L("The air moves normally again."));
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Room R=mob.location();
		if(R==null)
			return false;
		Area A = R.getArea();
		if(A instanceof Boardable)
			A=CMLib.map().areaLocation(((Boardable)A).getBoardableItem());
		if(A==null)
			return false;
		if(A.fetchEffect(ID())!=null)
		{
			mob.tell(L("This air here is already as calm as it can get."));
			return false;
		}
		if(((R.domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell(L("You must be outdoors for this chant to work."));
			return false;
		}
		boolean stillWind = false;
		switch(A.getClimateObj().weatherType(R))
		{
		case Climate.WEATHER_WINDY:
		case Climate.WEATHER_THUNDERSTORM:
		case Climate.WEATHER_BLIZZARD:
		case Climate.WEATHER_DUSTSTORM:
			break;
		case Climate.WEATHER_HAIL:
		case Climate.WEATHER_SLEET:
		case Climate.WEATHER_SNOW:
		case Climate.WEATHER_RAIN:
			mob.tell(L("The weather is nasty, but not especially windy any more."));
			return false;
		default:
			stillWind = true;
			break;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int size=mob.location().getArea().numberOfProperIDedRooms();
		size=size/(mob.phyStats().level()+(2*getXLEVELLevel(mob)));
		if(size<0)
			size=0;
		final boolean success=proficiencyCheck(mob,-size,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),
					auto?L("^JThe swirling sky changes color!^?"):L("^S<S-NAME> chant(s) into the swirling sky!^?"));
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
				final Climate C=A.getClimateObj();
				final Climate oldC=(Climate)C.copyOf();
				switch(C.weatherType(R))
				{
				case Climate.WEATHER_WINDY:
					C.setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_THUNDERSTORM:
					C.setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_BLIZZARD:
					C.setNextWeatherType(Climate.WEATHER_SNOW);
					break;
				case Climate.WEATHER_DUSTSTORM:
					C.setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				default:
					break;
				}
				if(stillWind)
				{
					final int duration = 4;
					int bonus = (int)Math.round(Math.floor(CMath.div(adjustedLevel(mob,asLevel),30.0)));
					bonus += super.getXTIMELevel(mob);
					if(beneficialAffect(mob, R, asLevel, duration + bonus) != null)
						sayToEveryoneInRoom(R, L("The air becomes extremely still."));
				}
				else
				{
					C.forceWeatherTick(A);
					Chant_CalmWeather.xpWorthyChange(mob,A,oldC,C);
				}
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) into the sky, but the magic fizzles."));

		return success;
	}
}
