package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Chant extends StdAbility
{
	@Override
	public String ID()
	{
		return "Chant";
	}

	private final static String	localizedName	= CMLib.lang().L("a Druidic Chant");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "(" + name() + ")";
	}

	protected boolean		renderedMundane	= false;

	/** codes: -1=do nothing, 1=wind, 2=rain, 4=hot, 8=cold, 16=calm */
	public final static int WEATHERQUE_NADA=0;
	public final static int WEATHERQUE_WIND=1;
	public final static int WEATHERQUE_RAIN=2;
	public final static int WEATHERQUE_HOT=4;
	public final static int WEATHERQUE_COLD=8;
	public final static int WEATHERQUE_CALM=16;

	@Override
	protected int verbalCastCode(MOB mob, Physical target, boolean auto)
	{
		if(renderedMundane)
		{
			int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
			affectType=CMMsg.MSG_NOISE|CMMsg.MASK_MOUTH;
			if(abstractQuality()==Ability.QUALITY_MALICIOUS)
				affectType=affectType|CMMsg.MASK_MALICIOUS;
			if(auto)
				affectType=affectType|CMMsg.MASK_ALWAYS;
			return affectType;
		}
		return super.verbalCastCode(mob,target,auto);
	}
	
	private static final String[]	triggerStrings	= I(new String[] { "CHANT", "CH" });

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public void setMiscText(String newText)
	{
		if(newText.equalsIgnoreCase("render mundane"))
			renderedMundane=true;
		else
			super.setMiscText(newText);
	}

	@Override
	public int classificationCode()
	{
		return renderedMundane ? Ability.ACODE_SKILL : Ability.ACODE_CHANT;
	}

	// codes: -1=do nothing, 1=wind, 2=rain, 4=hot, 8=cold, 16=calm
	public int weatherQue(Room R)
	{
		if(R==null)
			return WEATHERQUE_NADA;
		if((R.domainType()&Room.INDOORS)>0)
			return WEATHERQUE_NADA;
		switch(R.getArea().getClimateObj().weatherType(R))
		{
		case Climate.WEATHER_BLIZZARD:
		case Climate.WEATHER_THUNDERSTORM:
		case Climate.WEATHER_HEAT_WAVE:
			return WEATHERQUE_NADA;
		case Climate.WEATHER_CLEAR:
			return WEATHERQUE_WIND | WEATHERQUE_RAIN | WEATHERQUE_HOT | WEATHERQUE_COLD;
		case Climate.WEATHER_CLOUDY:
			return WEATHERQUE_WIND | WEATHERQUE_RAIN;
		case Climate.WEATHER_DROUGHT:
			return WEATHERQUE_RAIN | WEATHERQUE_COLD;
		case Climate.WEATHER_DUSTSTORM:
			return WEATHERQUE_RAIN | WEATHERQUE_CALM | WEATHERQUE_COLD;
		case Climate.WEATHER_HAIL:
			return WEATHERQUE_HOT | WEATHERQUE_CALM;
		case Climate.WEATHER_RAIN:
			return WEATHERQUE_WIND | WEATHERQUE_RAIN;
		case Climate.WEATHER_SLEET:
			return WEATHERQUE_HOT;
		case Climate.WEATHER_SNOW:
			return WEATHERQUE_WIND;
		case Climate.WEATHER_WINDY:
			return WEATHERQUE_RAIN;
		case Climate.WEATHER_WINTER_COLD:
			return WEATHERQUE_RAIN;
		default:
			return WEATHERQUE_CALM;
		}
	}
	
	protected static boolean chantAlignmentCheck(StdAbility A, MOB mob, boolean renderedMundane, boolean auto)
	{
		if((!auto)
		&&(!mob.isMonster())
		&&(!A.disregardsArmorCheck(mob))
		&&(mob.isMine(A))
		&&(!renderedMundane)
		&&(CMLib.dice().rollPercentage()<50))
		{
			if(!A.appropriateToMyFactions(mob))
			{
				mob.tell(A.L("Extreme emotions disrupt your chant."));
				return false;
			}
			else
			if(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,A.L("<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!"));
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!chantAlignmentCheck(this,mob,renderedMundane,auto))
			return false;
		return true;
	}
}
