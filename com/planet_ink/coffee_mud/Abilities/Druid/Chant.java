package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

/*
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Chant extends StdAbility
{
	public String ID() { return "Chant"; }
	public String name(){ return "a Druidic Chant";}
	public String displayText(){return "("+name()+")";}
    protected boolean renderedMundane=false;
    
    /** codes: -1=do nothing, 1=wind, 2=rain, 4=hot, 8=cold, 16=calm */
    public final static int WEATHERQUE_NADA=0;
    public final static int WEATHERQUE_WIND=1;
    public final static int WEATHERQUE_RAIN=2;
    public final static int WEATHERQUE_HOT=4;
    public final static int WEATHERQUE_COLD=8;
    public final static int WEATHERQUE_CALM=16;
    
	protected int verbalCastCode(MOB mob, Environmental target, boolean auto)
    {
        if(renderedMundane)
        {
            int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
            affectType=CMMsg.MSG_NOISE|CMMsg.MASK_MOUTH;
            if(abstractQuality()==Ability.QUALITY_MALICIOUS)
                affectType=affectType|CMMsg.MASK_MALICIOUS;
            if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
            return affectType;
        }
    	return super.verbalCastCode(mob,target,auto);
	}
	private static final String[] triggerStrings = {"CHANT","CH"};
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

    public void setMiscText(String newText)
    {
        if(newText.equalsIgnoreCase("render mundane"))
            renderedMundane=true;
        else
            super.setMiscText(newText);
    }
	public int classificationCode()	{ return renderedMundane?Ability.ACODE_SKILL:Ability.ACODE_CHANT;}

    /** codes: -1=do nothing, 1=wind, 2=rain, 4=hot, 8=cold, 16=calm */
    public int weatherQue(Room R)
    {
        if(R==null) return WEATHERQUE_NADA;
        if((R.domainType()&Room.INDOORS)>0) return WEATHERQUE_NADA;
        switch(R.getArea().getClimateObj().weatherType(R))
        {
        case Climate.WEATHER_BLIZZARD:
        case Climate.WEATHER_THUNDERSTORM:
        case Climate.WEATHER_HEAT_WAVE:
            return WEATHERQUE_NADA;
        case Climate.WEATHER_CLEAR: return WEATHERQUE_WIND|WEATHERQUE_RAIN|WEATHERQUE_HOT|WEATHERQUE_COLD;
        case Climate.WEATHER_CLOUDY: return WEATHERQUE_WIND|WEATHERQUE_RAIN;
        case Climate.WEATHER_DROUGHT: return WEATHERQUE_RAIN|WEATHERQUE_COLD;
        case Climate.WEATHER_DUSTSTORM: return WEATHERQUE_RAIN|WEATHERQUE_CALM|WEATHERQUE_COLD;
        case Climate.WEATHER_HAIL: return WEATHERQUE_HOT|WEATHERQUE_CALM;
        case Climate.WEATHER_RAIN: return WEATHERQUE_WIND|WEATHERQUE_RAIN;
        case Climate.WEATHER_SLEET: return WEATHERQUE_HOT;
        case Climate.WEATHER_SNOW: return WEATHERQUE_WIND;
        case Climate.WEATHER_WINDY: return WEATHERQUE_RAIN;
        case Climate.WEATHER_WINTER_COLD: return WEATHERQUE_RAIN;
        default: return WEATHERQUE_CALM;
        }
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(mob.isMine(this))
        &&(!renderedMundane)
		&&(CMLib.dice().rollPercentage()<50))
		{
			if(!appropriateToMyFactions(mob))
			{
				mob.tell("Extreme emotions disrupt your chant.");
				return false;
			}
			else
			if(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
				return false;
			}
		}
		return true;
	}
}
