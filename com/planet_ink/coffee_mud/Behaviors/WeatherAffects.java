package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class WeatherAffects extends PuddleMaker
{
	public String ID(){return "WeatherAffects";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}
	protected int puddlepct=0;
	protected int windsheer=0;
    protected int rustDown=0;
    protected int botherDown=0;
    protected int rumbleDown=0;
    protected int gustDown=0;
    protected int tornadoDown=0;
    protected int lightningDown=0;
    protected int hailDown=0;
    protected int rainSlipChance=0;
    protected int snowSlipChance=0;
    protected int sleetSlipChance=0;
    protected int freezeOverChance=0;
    protected int dustDown=0;
    protected int diseaseDown=0;
    protected int droughtFireChance=0;
    
    private static final long[] ALL_COVERED_SPOTS={Item.ON_FEET,Item.ON_TORSO,Item.ON_LEGS};
    private static long ALL_COVERED_CODE=0;
    private static final long[] ALL_FROST_SPOTS={Item.ON_FEET,Item.ON_HANDS,Item.ON_HEAD};
    private static long ALL_FROST_CODE=0;
    static
    {
        for(int l=0;l<ALL_COVERED_SPOTS.length;l++)
            ALL_COVERED_CODE=ALL_COVERED_CODE|ALL_COVERED_SPOTS[l];
        for(int l=0;l<ALL_FROST_SPOTS.length;l++)
            ALL_FROST_CODE=ALL_FROST_CODE|ALL_FROST_SPOTS[l];
    }
	
	public int pct(){return puddlepct;} // for puddles only
	
	public void setParms(String newParms)
	{
		parms=newParms;
		puddlepct=Util.getParmInt(parms,"puddlepct",50);
		windsheer=Util.getParmInt(parms,"windsheer",10);
        rainSlipChance=Util.getParmInt(parms,"rainslipchance",1);
        snowSlipChance=Util.getParmInt(parms,"snowslipchance",5);
        sleetSlipChance=Util.getParmInt(parms,"sleetslipchance",10);
        freezeOverChance=Util.getParmInt(parms,"iceoverchance",50);
        droughtFireChance=Util.getParmInt(parms,"droughtfirechance",1);
        resetBotherTicks();
        resetDiseaseTicks();
        resetRustTicks();
        resetLightningTicks();
        resetRumbleTicks();
        resetGustTicks();
        resetTornadoTicks();
        resetHailTicks();
        resetDustTicks();
	}
	
    private void resetBotherTicks(){botherDown=Util.getParmInt(parms,"botherticks",Climate.WEATHER_TICK_DOWN/3);}
    private void resetDiseaseTicks(){diseaseDown=Util.getParmInt(parms,"diseaseticks",Climate.WEATHER_TICK_DOWN);}
    private void resetRustTicks(){rustDown=Util.getParmInt(parms,"rustticks",30);}
    private void resetLightningTicks(){lightningDown=Util.getParmInt(parms,"lightningticks",Climate.WEATHER_TICK_DOWN*4);}
    private void resetRumbleTicks(){rumbleDown=Util.getParmInt(parms,"rumbleticks",Climate.WEATHER_TICK_DOWN/4);}
    private void resetGustTicks(){gustDown=Util.getParmInt(parms,"gustticks",Climate.WEATHER_TICK_DOWN);}
    private void resetTornadoTicks(){tornadoDown=Util.getParmInt(parms,"tornadoticks",Climate.WEATHER_TICK_DOWN*10);}
    private void resetHailTicks(){hailDown=Util.getParmInt(parms,"hailticks",Climate.WEATHER_TICK_DOWN/2);}
    private void resetDustTicks(){dustDown=Util.getParmInt(parms,"dustticks",50);}
    
	public Area area(Environmental host)
	{
		Area A=(host instanceof Area)?(Area)host:CoffeeUtensils.roomLocation(host).getArea();
		return A;
	}
	
	public int weather(Environmental host, Room room)
	{
		if(room==null) return 0;
		Area A=(host instanceof Area)?(Area)host:CoffeeUtensils.roomLocation(host).getArea();
		if(A!=null) return A.getClimateObj().weatherType(room);
		return 0;
	}
	
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg)) return false;
        
		if((host instanceof Area)
        &&(msg.source().location()!=null)
        &&(msg.source().location().getArea()!=host))
            return true;
        int weather=weather(host,msg.source().location());
        // first handle the effect of storms on ranged
        // weapons
        if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
        &&(msg.source().rangeToTarget()!=0)
        &&(msg.tool() instanceof Item)
        &&(!(msg.tool() instanceof Electronics))
        &&((msg.sourceMinor()==CMMsg.TYP_THROW)
            ||((msg.tool() instanceof Weapon)
                &&((((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)
                   ||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_THROWN)))))
        {
            switch(weather)
            {
            case Climate.WEATHER_WINDY:
            case Climate.WEATHER_THUNDERSTORM:
            case Climate.WEATHER_BLIZZARD:
            case Climate.WEATHER_DUSTSTORM:
            {
				if((Dice.rollPercentage()<windsheer)
				&&(msg.source().location()!=null))
				{
					msg.source().location().show(msg.source(),msg.target(),msg.tool(),CMMsg.MSG_OK_ACTION,"The strong wind blows <S-YOUPOSS> attack against <T-NAMESELF> with <O-NAME> off target.");
					return false;
				}
                break;
			}
            }
		}
        // then try to handle slippage in wet weather
        if(((Util.bset(msg.sourceMajor(),CMMsg.MASK_MOVE)))
        &&(msg.source().location()!=null))
        {
            String what=null;
            switch(weather)
            {
                case Climate.WEATHER_BLIZZARD:
                case Climate.WEATHER_SNOW:
                    if(Dice.rollPercentage()<snowSlipChance)
                        what="cold wet";
                    break;
                case Climate.WEATHER_RAIN:
                case Climate.WEATHER_THUNDERSTORM:
                    if(Dice.rollPercentage()<rainSlipChance)
                        what="slippery wet";
                    break;
                case Climate.WEATHER_SLEET:
                    if(Dice.rollPercentage()<sleetSlipChance)
                        what="icey";
                    break;
            }
            if((what!=null)
            &&(!Sense.isInFlight(msg.source()))
            &&(Dice.rollPercentage()>((msg.source().charStats().getStat(CharStats.DEXTERITY)*3)+25)))
            {
                int oldDisposition=msg.source().baseEnvStats().disposition();
                oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
                msg.source().baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SITTING);
                msg.source().recoverEnvStats();
                msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> slip(s) on the "+what+" ground.");
                return false;
            }
        }
        if((msg.source().location()!=null)
        &&(weather==Climate.WEATHER_BLIZZARD))
        {
            Ability A=CMClass.getAbility("Spell_ObscureSelf");
            A.setAffectedOne(msg.source());
            if(!A.okMessage(msg.source(),msg))
                return false;
        }
		return true;
	}
    
    public boolean tick(Tickable ticking, int tickID)
    {
        int realLastWeather=super.lastWeather;
        if(!super.tick(ticking,tickID))
            return false;
        Area A=CoffeeUtensils.areaLocation(ticking);
        if(A==null) return false;
        Climate C=A.getClimateObj();
        if(C==null) return false;
        lastWeather=realLastWeather;
        
        // handle freeze overs
        if((coldWeather(lastWeather))
        &&(coldWeather(C.weatherType(null)))
        &&(lastWeather!=C.weatherType(null))
        &&(A.getTimeObj().getSeasonCode()==TimeClock.SEASON_WINTER)
        &&(Dice.rollPercentage()<freezeOverChance))
        {
            if(ticking instanceof Room)
            {
                Room R=(Room)ticking;
                if((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
                &&(Dice.rollPercentage()<freezeOverChance)
                &&(R instanceof Drink)
                &&(((Drink)R).liquidType()==EnvResource.RESOURCE_FRESHWATER))
                {
                    Ability A2=CMClass.getAbility("Spell_IceSheet");
                    if(A2!=null)
                    {
                        MOB mob=CMMap.god(R);
                        A2.invoke(mob,R,true,0);
                    }
                }
            }
            else
            for(Enumeration e=A.getProperMap();e.hasMoreElements();)
            {
                Room R=(Room)e.nextElement();
                if((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
                &&(Dice.rollPercentage()<freezeOverChance))
                {
                    Ability A2=CMClass.getAbility("Spell_IceSheet");
                    if(A2!=null)
                    {
                        MOB mob=CMMap.god(R);
                        A2.invoke(mob,R,true,0);
                    }
                }
            }
        }
        if((botherDown--)==1)
        {
            resetBotherTicks();
            switch(C.weatherType(null))
            {
            case Climate.WEATHER_BLIZZARD:
            case Climate.WEATHER_SLEET:
            case Climate.WEATHER_SNOW:
            case Climate.WEATHER_HAIL:
            case Climate.WEATHER_THUNDERSTORM:
            case Climate.WEATHER_RAIN:
                for(Enumeration r=A.getProperMap();r.hasMoreElements();)
                {
                    Room R=(Room)r.nextElement();
                    if(CoffeeUtensils.hasASky(R))
                        for(int i=0;i<R.numInhabitants();i++)
                        {
                            MOB mob=R.fetchInhabitant(i);
                            if((mob!=null)
                            &&(!mob.isMonster())
                            &&(Sense.aliveAwakeMobile(mob,true))
                            &&(Util.bset(mob.getBitmap(),MOB.ATT_AUTOWEATHER)))
                                mob.tell(C.getWeatherDescription(A));
                        }
                }
                break;
            }
        }
        if((diseaseDown--)==1)
        {
            resetDiseaseTicks();
            int coldChance=0;
            int fluChance=0;
            int frostBiteChance=0;
            int heatExhaustionChance=0;
            switch(C.weatherType(null))
            {
            case Climate.WEATHER_BLIZZARD:
            case Climate.WEATHER_SLEET:
            case Climate.WEATHER_SNOW:
                coldChance=99;
                fluChance=25;
                frostBiteChance=15;
                break;
            case Climate.WEATHER_HAIL:
                coldChance=50;
                frostBiteChance=10;
                break;
            case Climate.WEATHER_THUNDERSTORM:
            case Climate.WEATHER_RAIN:
                coldChance=25;
                break;
            case Climate.WEATHER_WINTER_COLD:
                coldChance=75;
                fluChance=10;
                frostBiteChance=5;
                break;
            case Climate.WEATHER_HEAT_WAVE:
                heatExhaustionChance=15;
                break;
            case Climate.WEATHER_DROUGHT:
                heatExhaustionChance=20;
                break;
            }

            for(int s=0;s<Sessions.size();s++)
            {
                Session S=Sessions.elementAt(s);
                if((S.mob()==null)
                ||(S.mob().location()==null)
                ||(S.mob().location().getArea()!=A)
                ||(S.mob().isMonster()))
                    continue;

                MOB M=S.mob();
                Room R=M.location();

                
                if((R.domainConditions()&Room.CONDITION_COLD)>0) 
                {
                    if(coldChance>0) coldChance+=10;
                    if(coldChance>0) fluChance+=5; // yes, cold is related this way to flu
                    if(frostBiteChance>0) frostBiteChance=frostBiteChance+(int)Math.round(Util.mul(frostBiteChance,0.5));
                }
                if((R.domainConditions()&Room.CONDITION_HOT)>0) 
                {
                    if(heatExhaustionChance>0) heatExhaustionChance+=10;
                }
                if((R.domainConditions()&Room.CONDITION_WET)>0)
                {
                    if(coldChance>0) coldChance+=5;
                    if(heatExhaustionChance>5) heatExhaustionChance-=5;
                    if(frostBiteChance>0) frostBiteChance=frostBiteChance+(int)Math.round(Util.mul(frostBiteChance,0.25));
                }
                int save=(M.charStats().getStat(CharStats.SAVE_COLD)+M.charStats().getStat(CharStats.SAVE_WATER))/2;
                if((Dice.rollPercentage()<(coldChance-save))
                &&((C.weatherType(M.location())!=Climate.WEATHER_CLEAR)))
                {
                    long coveredPlaces=0;
                    for(int l=0;l<ALL_COVERED_SPOTS.length;l++)
                        if(M.getWearPositions(ALL_COVERED_SPOTS[l])==0)
                            coveredPlaces=coveredPlaces|ALL_COVERED_SPOTS[l];
                    Item I=null;
                    for(int i=0;i<M.inventorySize();i++)
                    {
                        I=M.fetchInventory(i);
                        if((I==null)||(I.amWearingAt(Item.INVENTORY)))
                           continue;
                        if(I.amWearingAt(Item.ABOUT_BODY))
                            coveredPlaces=coveredPlaces|Item.ON_TORSO|Item.ON_LEGS;
                        for(int l=0;l<ALL_COVERED_SPOTS.length;l++)
                            if(I.amWearingAt(ALL_COVERED_SPOTS[l]))
                                coveredPlaces=coveredPlaces|ALL_COVERED_SPOTS[l];
                    }
                    if(coveredPlaces!=ALL_COVERED_CODE)
                    {
                        Ability COLD=CMClass.getAbility("Disease_Cold");
                        if(Dice.rollPercentage()<(fluChance+(((M.location().domainConditions()&Room.CONDITION_WET)>0)?10:0)))
                            COLD=CMClass.getAbility("Disease_Flu");
                        if((COLD!=null)&&(M.fetchEffect(COLD.ID())==null))
                            COLD.invoke(M,M,true,0);
                    }
                }
                if((Dice.rollPercentage()<(frostBiteChance-save))
                &&((C.weatherType(M.location())!=Climate.WEATHER_CLEAR)))
                {
                    long unfrostedPlaces=0;
                    for(int l=0;l<ALL_FROST_SPOTS.length;l++)
                        if(M.getWearPositions(ALL_FROST_SPOTS[l])==0)
                            unfrostedPlaces=unfrostedPlaces|ALL_FROST_SPOTS[l];
                    Item I=null;
                    for(int i=0;i<M.inventorySize();i++)
                    {
                        I=M.fetchInventory(i);
                        if((I==null)||(I.amWearingAt(Item.INVENTORY)))
                           continue;
                        for(int l=0;l<ALL_FROST_SPOTS.length;l++)
                            if(I.amWearingAt(ALL_FROST_SPOTS[l]))
                                unfrostedPlaces=unfrostedPlaces|ALL_FROST_SPOTS[l];
                    }
                    if(unfrostedPlaces!=ALL_FROST_CODE)
                    {
                        Ability COLD=CMClass.getAbility("Disease_FrostBite");
                        if((COLD!=null)&&(M.fetchEffect(COLD.ID())==null))
                            COLD.invoke(M,M,true,0);
                    }
                }
                if((heatExhaustionChance>0)
                &&(Dice.rollPercentage()<(heatExhaustionChance-M.charStats().getStat(CharStats.SAVE_FIRE)))
                &&(C.weatherType(M.location())!=Climate.WEATHER_CLEAR))
                {
                    Ability COLD=CMClass.getAbility("Disease_HeatExhaustion");
                    if((COLD!=null)&&(M.fetchEffect(COLD.ID())==null))
                        COLD.invoke(M,M,true,0);
                }
            }
        }
        if((rumbleDown--)==1)
        {
            resetRumbleTicks();
            for(int s=0;s<Sessions.size();s++)
            {
                Session S=Sessions.elementAt(s);
                if((S.mob()==null)
                ||(S.mob().location()==null)
                ||(S.mob().location().getArea()!=A)
                ||(S.mob().isMonster())
                ||(!Util.bset(S.mob().getBitmap(),MOB.ATT_AUTOWEATHER)))
                    continue;
                Room R=S.mob().location();
                if(R!=null)
                {
                    switch(C.weatherType(null))
                    {
                    case Climate.WEATHER_THUNDERSTORM:
                    {
                        if(C.weatherType(R)!=Climate.WEATHER_THUNDERSTORM)
                            S.mob().tell("A thunderous rumble and CRACK of lightning can be heard.");
                        else
                        if(R.getArea().getTimeObj().getTODCode()==TimeClock.TIME_DAY)
                            S.mob().tell("A thunderous rumble and CRACK of lightning can be heard as the pounding rain soaks you.");
                        else
                            S.mob().tell("A bolt of lightning streaks across the sky as the pounding rain soaks you!");
                        break;
                    }
                    case Climate.WEATHER_BLIZZARD:
                        if(C.weatherType(R)==Climate.WEATHER_BLIZZARD)
                            S.mob().tell("Swirling clouds of snow buffet you.");
                        break;
                    case Climate.WEATHER_SNOW:
                        if(C.weatherType(R)==Climate.WEATHER_SNOW)
                            S.mob().tell("Snowflakes fall lightly on you.");
                        break;
                    case Climate.WEATHER_DUSTSTORM:
                        if(C.weatherType(R)==Climate.WEATHER_DUSTSTORM)
                            S.mob().tell("Swirling clouds of dust assault you.");
                        break;
                    case Climate.WEATHER_HAIL:
                        if(C.weatherType(R)==Climate.WEATHER_HAIL)
                            S.mob().tell("You are being pelleted by hail! Ouch!");
                        break;
                    case Climate.WEATHER_RAIN:
                        if(C.weatherType(R)==Climate.WEATHER_RAIN)
                            S.mob().tell("The rain is soaking you!");
                        break;
                    case Climate.WEATHER_SLEET:
                        if(C.weatherType(R)==Climate.WEATHER_SLEET)
                            S.mob().tell("Cold and blistering sleet is soaking you numb!");
                        break;
                    case Climate.WEATHER_WINDY:
                        if(C.weatherType(R)==Climate.WEATHER_WINDY)
                            S.mob().tell("The wind gusts around you.");
                        break;
                    }
                }
            }
        }
        if((lightningDown--)==1)
        {
            resetLightningTicks();
            if(C.weatherType(null)==Climate.WEATHER_THUNDERSTORM)
            {
                Vector choices=new Vector();
                Room R=null;
                for(int s=0;s<Sessions.size();s++)
                {
                    Session S=Sessions.elementAt(s);
                    if((S.mob()==null)
                    ||(S.mob().location()==null)
                    ||(S.mob().location().getArea()!=A)
                    ||(S.mob().isMonster())
                    ||(C.weatherType(S.mob().location())!=Climate.WEATHER_THUNDERSTORM))
                        continue;
                    R=S.mob().location();
                    if((R!=null)&&(!choices.contains(R)))
                        choices.addElement(R);
                }
                if(choices.size()>0)
                {
                    R=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
                    MOB M=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
                    Ability A2=CMClass.getAbility("Chant_SummonLightning");
                    if(A2!=null)
                    { 
                        A2.setMiscText("RENDER MUNDANE"); 
                        A2.invoke(M,M,true,M.envStats().level());
                    }
                    for(int i=0;i<choices.size();i++)
                        if(choices.elementAt(i)!=R)
                            if((((Room)choices.elementAt(i)).getArea().getTimeObj().getTODCode()==TimeClock.TIME_DAY)
                            ||(C.weatherType(((Room)choices.elementAt(i)))!=Climate.WEATHER_THUNDERSTORM))
                                ((Room)choices.elementAt(i)).showHappens(CMMsg.MSG_OK_ACTION,"A thunderous rumble and CRACK of lightning can be heard.!");
                            else
                                ((Room)choices.elementAt(i)).showHappens(CMMsg.MSG_OK_ACTION,"A bolt of lightning streaks across the sky!");
                }
            }
        }
        if((tornadoDown--)==1)
        {
            resetTornadoTicks();
            if((C.weatherType(null)==Climate.WEATHER_THUNDERSTORM)
            ||(C.weatherType(null)==Climate.WEATHER_WINDY))
            {
                Vector choices=new Vector();
                Room R=null;
                for(int s=0;s<Sessions.size();s++)
                {
                    Session S=Sessions.elementAt(s);
                    if((S.mob()==null)
                    ||(S.mob().location()==null)
                    ||(S.mob().location().getArea()!=A)
                    ||(S.mob().isMonster())
                    ||((C.weatherType(S.mob().location())!=Climate.WEATHER_WINDY)
                        &&(C.weatherType(S.mob().location())!=Climate.WEATHER_THUNDERSTORM)))
                        continue;
                    R=S.mob().location();
                    if((R!=null)&&(!choices.contains(R)))
                        choices.addElement(R);
                }
                if(choices.size()>0)
                {
                    R=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
                    Ability A2=CMClass.getAbility("Chant_SummonTornado");
                    if(A2!=null)
                    {
                        A2.setMiscText("RENDER MUNDANE"); 
                        MOB mob=CMMap.god(R);
                        A2.invoke(mob,null,true,0);
                    }
                    for(int i=0;i<choices.size();i++)
                        if(choices.elementAt(i)!=R)
                            if(C.weatherType(((Room)choices.elementAt(i)))==0)
                                ((Room)choices.elementAt(i)).showHappens(CMMsg.MSG_OK_ACTION,"The terrible rumble of a tornado can be heard.!");
                            else
                                ((Room)choices.elementAt(i)).showHappens(CMMsg.MSG_OK_ACTION,"A huge and terrible tornado touches down somewhere near by.");
                }
            }
        }
        if((dustDown--)==1)
        {
            resetDustTicks();
            if(C.weatherType(null)==Climate.WEATHER_DUSTSTORM)
            {
                Vector choices=new Vector();
                Room R=null;
                for(int s=0;s<Sessions.size();s++)
                {
                    Session S=Sessions.elementAt(s);
                    if((S.mob()==null)
                    ||(S.mob().location()==null)
                    ||(S.mob().location().getArea()!=A)
                    ||(S.mob().isMonster())
                    ||(C.weatherType(S.mob().location())!=Climate.WEATHER_DUSTSTORM))
                        continue;
                    R=S.mob().location();
                    if((R!=null)&&(!choices.contains(R)))
                        choices.addElement(R);
                }
                if(choices.size()>0)
                {
                    R=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
                    MOB M=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
                    Ability A2=CMClass.getAbility("Skill_Dirt");
                    if(A2!=null) A2.invoke(M,M,true,0);
                }
            }
        }
        if((hailDown--)==1)
        {
            resetHailTicks();
            if(C.weatherType(null)==Climate.WEATHER_HAIL)
            {
                Vector choices=new Vector();
                Room R=null;
                for(int s=0;s<Sessions.size();s++)
                {
                    Session S=Sessions.elementAt(s);
                    if((S.mob()==null)
                    ||(S.mob().location()==null)
                    ||(S.mob().location().getArea()!=A)
                    ||(S.mob().isMonster())
                    ||(C.weatherType(S.mob().location())!=Climate.WEATHER_HAIL))
                        continue;
                    R=S.mob().location();
                    if((R!=null)&&(!choices.contains(R)))
                        choices.addElement(R);
                }
                if(choices.size()>0)
                {
                    R=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
                    MOB M=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
                    Ability A2=CMClass.getAbility("Chant_SummonHail");
                    if(A2!=null)
                    {
                        A2.setMiscText("RENDER MUNDANE"); 
                        A2.invoke(M,M,true,M.envStats().level());
                    }
                }
            }
        }
        if((C.weatherType(null)==Climate.WEATHER_DROUGHT)
        &&(Dice.rollPercentage()<droughtFireChance))
        {
            Room R=CoffeeUtensils.roomLocation((Environmental)ticking);
            if((R==null)&&(ticking instanceof Area))
                R=((Area)ticking).getRandomProperRoom();
            if((R!=null)
            &&((R.domainType()&Room.INDOORS)==0)
            &&(R.domainType()!=Room.DOMAIN_OUTDOORS_SWAMP)
            &&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
            &&(R.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
            &&((R.domainConditions()&Room.CONDITION_WET)==0))
            {
                Item I=R.fetchItem(Dice.roll(1,R.numItems(),-1));
                if((I!=null)&&(Sense.isGettable(I)))
                switch(I.material()&EnvResource.MATERIAL_MASK)
                {
                case EnvResource.MATERIAL_CLOTH:
                case EnvResource.MATERIAL_LEATHER:
                case EnvResource.MATERIAL_PAPER:
                case EnvResource.MATERIAL_VEGETATION:
                case EnvResource.MATERIAL_WOODEN:
                {
                    Ability A2=CMClass.getAbility("Burning");
                    MOB mob=CMMap.god(R);
                    R.showHappens(CMMsg.MSG_OK_VISUAL,I.Name()+" spontaneously combusts in the seering heat!");
                    A2.invoke(mob,I,true,0);
                }
                break;    
                }
            }
        }
        if((gustDown--)==1)
        {
            resetGustTicks();
            if((C.weatherType(null)==Climate.WEATHER_WINDY)
            ||(C.weatherType(null)==Climate.WEATHER_BLIZZARD)
            ||(C.weatherType(null)==Climate.WEATHER_DUSTSTORM))
            {
                Vector choices=new Vector();
                Room R=null;
                for(int s=0;s<Sessions.size();s++)
                {
                    Session S=Sessions.elementAt(s);
                    if((S.mob()==null)
                    ||(S.mob().location()==null)
                    ||(S.mob().location().getArea()!=A)
                    ||(S.mob().isMonster())
                    ||((C.weatherType(S.mob().location())!=Climate.WEATHER_WINDY)
                            &&(C.weatherType(S.mob().location())!=Climate.WEATHER_BLIZZARD)
                            &&(C.weatherType(S.mob().location())!=Climate.WEATHER_DUSTSTORM)))
                        continue;
                    R=S.mob().location();
                    if((R!=null)&&(!choices.contains(R)))
                        choices.addElement(R);
                }
                if(choices.size()>0)
                {
                    R=(Room)choices.elementAt(Dice.roll(1,choices.size(),-1));
                    MOB M=CMMap.god(R);
                    Ability A2=CMClass.getAbility("Chant_WindGust");
                    if(A2!=null)
                    {
                        A2.setMiscText("RENDER MUNDANE"); 
                        A2.invoke(M,M,true,M.envStats().level());
                    }
                }
            }
        }
        if((rustDown--)==1)
        {
            resetRustTicks();
            for(int s=0;s<Sessions.size();s++)
            {
                Session S=Sessions.elementAt(s);
                if((S.mob()==null)
                ||(S.mob().location()==null)
                ||(S.mob().location().getArea()!=A)
                ||(S.mob().isMonster()))
                    continue;
                int rustChance=0;
                switch(C.weatherType(S.mob().location()))
                {
                case Climate.WEATHER_BLIZZARD:
                case Climate.WEATHER_SLEET:
                case Climate.WEATHER_SNOW:
                    rustChance=5;
                    break;
                case Climate.WEATHER_HAIL:
                    rustChance=5;
                    break;
                case Climate.WEATHER_THUNDERSTORM:
                case Climate.WEATHER_RAIN:
                    rustChance=5;
                    break;
                }

                MOB M=S.mob();
                Room R=M.location();

                switch(R.domainType())
                {
                case Room.DOMAIN_INDOORS_UNDERWATER:
                case Room.DOMAIN_INDOORS_WATERSURFACE:
                case Room.DOMAIN_OUTDOORS_WATERSURFACE:
                case Room.DOMAIN_OUTDOORS_UNDERWATER:
                    rustChance+=5;
                    break;
                default:
                    break;
                }
                if((R.domainConditions()&Room.CONDITION_WET)>0) 
                    rustChance+=2;
                if(Dice.rollPercentage()<rustChance)
                {
                    int weatherType=C.weatherType(R);
                    String weatherDesc=Climate.WEATHER_DESCS[weatherType].toLowerCase();
                    Vector rustThese=new Vector();
                    for(int i=0;i<M.inventorySize();i++)
                    {
                        Item I=M.fetchInventory(i);
                        if(I==null) continue;
                        if((!I.amWearingAt(Item.INVENTORY))
                        &&(((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL))
                        &&(I.subjectToWearAndTear())
                        &&((Dice.rollPercentage()>I.envStats().ability()*25)))
                            rustThese.addElement(I);
                        else
                        if(I.amWearingAt(Item.ABOUT_BODY)
                        &&(((I.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_METAL)))
                        {   rustThese.clear();  break;  }
                    }
                    if(R!=null)
                    for(int i=0;i<rustThese.size();i++)
                    {
                        Item I=(Item)rustThese.elementAt(i);
                        FullMsg msg=new FullMsg(M,I,null,CMMsg.MASK_GENERAL|CMMsg.TYP_WATER,(weatherType!=0)?"<T-NAME> rusts in the "+weatherDesc+".":"<T-NAME> rusts in the water.",CMMsg.TYP_WATER,null,CMMsg.NO_EFFECT,null);
                        if(R.okMessage(M,msg))
                        {
                            R.send(M,msg);
                            if(msg.value()<=0)
                            {
                                I.setUsesRemaining(I.usesRemaining()-1);
                                if(I.usesRemaining()<=0)
                                {
                                    msg=new FullMsg(M,null,null,CMMsg.MSG_OK_VISUAL,I.name()+" is destroyed!",null,I.name()+" carried by "+M.name()+" is destroyed!");
                                    if(R.okMessage(M,msg))
                                        R.send(M,msg);
                                    I.destroy();
                                }
                            }
                        }
                    }
                }
            }
        }
        if(ticking instanceof Room)
            lastWeather=C.weatherType((Room)ticking);
        else
            lastWeather=C.weatherType(null);
        return true;
    }
}
