package com.planet_ink.coffee_mud.Behaviors;
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
   Copyright 2004-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "WeatherAffects";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS | Behavior.CAN_AREAS;
	}

	protected int				puddlepct			= 0;
	protected int				windsheer			= 0;
	protected int				rustDown			= 0;
	protected int				botherDown			= 0;
	protected int				rumbleDown			= 0;
	protected int				gustDown			= 0;
	protected int				tornadoDown			= 0;
	protected int				lightningDown		= 0;
	protected int				hailDown			= 0;
	protected int				boatSlipChance		= 0;
	protected int				rainSlipChance		= 0;
	protected int				snowSlipChance		= 0;
	protected int				sleetSlipChance		= 0;
	protected int				freezeOverChance	= 0;
	protected int				dustDown			= 0;
	protected int				diseaseDown			= 0;
	protected int				droughtFireChance	= 0;

	private static final long[]	ALL_COVERED_SPOTS	= { Wearable.WORN_FEET, Wearable.WORN_TORSO, Wearable.WORN_LEGS };
	private static long			ALL_COVERED_CODE	= 0;
	private static final long[]	ALL_FROST_SPOTS		= { Wearable.WORN_FEET, Wearable.WORN_HANDS, Wearable.WORN_HEAD };
	private static long			ALL_FROST_CODE		= 0;
	static
	{
		for (final long element : ALL_COVERED_SPOTS)
			ALL_COVERED_CODE = ALL_COVERED_CODE | element;
		for (final long element : ALL_FROST_SPOTS)
			ALL_FROST_CODE = ALL_FROST_CODE | element;
	}

	@Override
	public int pct()
	{
		return puddlepct;
	} // for puddles only

	@Override
	public String accountForYourself()
	{
		return "weather effect causing";
	}

	@Override
	public void setParms(String newParms)
	{
		parms=newParms;
		puddlepct=CMParms.getParmInt(parms,"puddlepct",50);
		windsheer=CMParms.getParmInt(parms,"windsheer",10);
		rainSlipChance=CMParms.getParmInt(parms,"rainslipchance",1);
		snowSlipChance=CMParms.getParmInt(parms,"snowslipchance",5);
		sleetSlipChance=CMParms.getParmInt(parms,"sleetslipchance",10);
		freezeOverChance=CMParms.getParmInt(parms,"iceoverchance",50);
		droughtFireChance=CMParms.getParmInt(parms,"droughtfirechance",1);
		boatSlipChance=CMParms.getParmInt(parms,"boatslipchance",20);
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

	private void resetBotherTicks()
	{
		botherDown = CMParms.getParmInt(parms, "botherticks", Climate.WEATHER_TICK_DOWN / 3);
	}

	private void resetDiseaseTicks()
	{
		diseaseDown = CMParms.getParmInt(parms, "diseaseticks", Climate.WEATHER_TICK_DOWN * 2);
	}

	private void resetRustTicks()
	{
		rustDown = CMParms.getParmInt(parms, "rustticks", 30);
	}

	private void resetLightningTicks()
	{
		lightningDown = CMParms.getParmInt(parms, "lightningticks", Climate.WEATHER_TICK_DOWN * 8);
	}

	private void resetRumbleTicks()
	{
		rumbleDown = CMParms.getParmInt(parms, "rumbleticks", Climate.WEATHER_TICK_DOWN / 4);
	}

	private void resetGustTicks()
	{
		gustDown = CMParms.getParmInt(parms, "gustticks", Climate.WEATHER_TICK_DOWN * 2);
	}

	private void resetTornadoTicks()
	{
		tornadoDown = CMParms.getParmInt(parms, "tornadoticks", Climate.WEATHER_TICK_DOWN * 15);
	}

	private void resetHailTicks()
	{
		hailDown = CMParms.getParmInt(parms, "hailticks", Climate.WEATHER_TICK_DOWN);
	}

	private void resetDustTicks()
	{
		dustDown = CMParms.getParmInt(parms, "dustticks", 50);
	}

	public Area area(Environmental host)
	{
		final Area A=(host instanceof Area)?(Area)host:CMLib.map().roomLocation(host).getArea();
		return A;
	}

	public int weather(Environmental host, Room room)
	{
		if(room==null)
			return 0;
		final Area A=(host instanceof Area)?(Area)host:CMLib.map().roomLocation(host).getArea();
		if(A!=null)
			return A.getClimateObj().weatherType(room);
		return 0;
	}
	
	private boolean isInclement(final int weather)
	{
		switch(weather)
		{
			case Climate.WEATHER_BLIZZARD:
			case Climate.WEATHER_SNOW:
			case Climate.WEATHER_RAIN:
			case Climate.WEATHER_THUNDERSTORM:
			case Climate.WEATHER_SLEET:
			case Climate.WEATHER_DUSTSTORM:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;

		final Room R=msg.source().location();
		if((host instanceof Area)
		&&(R!=null)&&(R.getArea()!=host))
			return true;
		final int weather=weather(host,R);
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
				if((CMLib.dice().rollPercentage()<windsheer)
				&&(R!=null))
				{
					if(R.show(msg.source(),msg.target(),msg.tool(),CMMsg.MSG_WEATHER,L("^WThe strong wind blows <S-YOUPOSS> attack against <T-NAMESELF> with <O-NAME> off target.^?")))
						return false;
				}
				break;
			}
			}
		}
		// then try to handle slippage of boats and feet in bad weather
		if(((msg.sourceMajor(CMMsg.MASK_MOVE)))&&(R!=null)&&(isInclement(weather)))
		{
			switch(R.domainType())
			{
			case Room.DOMAIN_INDOORS_AIR:
			case Room.DOMAIN_OUTDOORS_AIR:
				break;
			case Room.DOMAIN_INDOORS_WATERSURFACE:
			case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			{
				final Rideable riding=msg.source().riding();
				if((riding!=null)
				&&((riding.rideBasis()==Rideable.RIDEABLE_WATER)||(riding instanceof BoardableShip))
				&&(!CMLib.flags().isABonusItems(riding))
				&&(!riding.phyStats().isAmbiance("-ANTIWEATHER")))
				{
					String what=null;
					switch(weather)
					{
					case Climate.WEATHER_SNOW:
						if(((R.getClimateType()&Places.CLIMASK_WINDY)!=0)
						&&(CMLib.dice().rollPercentage()<boatSlipChance))
							what="cold snowy winds"; // never L(
						break;
					case Climate.WEATHER_RAIN:
						if(((R.getClimateType()&Places.CLIMASK_WINDY)!=0)
						&&(CMLib.dice().rollPercentage()<boatSlipChance))
							what="strong rainy winds"; // never L(
						break;
					case Climate.WEATHER_BLIZZARD:
						if(CMLib.dice().rollPercentage()<(boatSlipChance * 1.5))
							what="blizzard"; // never L(
						break;
					case Climate.WEATHER_THUNDERSTORM:
						if(CMLib.dice().rollPercentage()<(boatSlipChance * 1.5))
							what="thunderstorm"; // never L(
						break;
					case Climate.WEATHER_DUSTSTORM:
						if(CMLib.dice().rollPercentage()<boatSlipChance)
							what="strong winds"; // never L(
						break;
					}
					if(what!=null)
					{
						if(R.show(msg.source(),null,CMMsg.MSG_WEATHER,L("^W<S-NAME> make(s) no progress in the "+what+".^?")))
						{
							if(riding instanceof BoardableShip)
							{
								final Area shipArea=((BoardableShip)riding).getShipArea();
								if(shipArea != null)
								{
									for(Enumeration<Room> sr=shipArea.getProperMap();sr.hasMoreElements();)
									{
										final Room sR=sr.nextElement();
										if((sR!=null)&&((sR.domainType()&Room.INDOORS)==0))
											sR.show(msg.source(),null,CMMsg.MSG_OK_ACTION,L("^W<S-NAME> make(s) no progress in the "+what+".^?"));
									}
								}
								
							}
							return false;
						}
					}
				}
				break;
			}
			case Room.DOMAIN_INDOORS_UNDERWATER:
			case Room.DOMAIN_OUTDOORS_UNDERWATER:
				break;
			default:
			{
				String what=null;
				switch(weather)
				{
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_SNOW:
					if(CMLib.dice().rollPercentage()<snowSlipChance)
						what="cold wet"; // never L(
					break;
				case Climate.WEATHER_RAIN:
				case Climate.WEATHER_THUNDERSTORM:
					if(CMLib.dice().rollPercentage()<rainSlipChance)
						what="slippery wet"; // never L(
					break;
				case Climate.WEATHER_SLEET:
					if(CMLib.dice().rollPercentage()<sleetSlipChance)
						what="icy"; // never L(
					break;
				}
				if((what!=null)
				&&(!CMLib.flags().isInFlight(msg.source()))
				&&(CMLib.dice().rollPercentage()>((msg.source().charStats().getStat(CharStats.STAT_DEXTERITY)*3)+25)))
				{
					if(R.show(msg.source(),null,CMMsg.MSG_WEATHER,L("^W<S-NAME> slip(s) on the "+what+" ground.^?")))
					{
						int oldDisposition=msg.source().basePhyStats().disposition();
						oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING|PhyStats.IS_CUSTOM));
						msg.source().basePhyStats().setDisposition(oldDisposition|PhyStats.IS_SITTING);
						msg.source().recoverPhyStats();
						return false;
					}
				}
			}
			}
		}
		if((R!=null)
		&&(weather==Climate.WEATHER_BLIZZARD))
		{
			final Ability A=CMClass.getAbility("Spell_ObscureSelf");
			if(A!=null)
			{
				A.setAffectedOne(msg.source());
				if(!A.okMessage(msg.source(),msg))
					return false;
			}
		}
		return true;
	}
	
	protected boolean isInArea(final Area A, MOB M)
	{
		if((M!=null)
		&&(M.location()!=null)
		&&(M.location().getArea()!=null))
		{
			final Area mA=M.location().getArea();
			if(mA==A)
				return true;
			if(mA instanceof BoardableShip)
			{
				Area inA=CMLib.map().areaLocation(((BoardableShip)mA).getShipItem());
				return inA==A;
			}
		}
		return false;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		final int realLastWeather=super.lastWeather;
		if(!super.tick(ticking,tickID))
			return false;
		final Area A=CMLib.map().areaLocation(ticking);
		if(A==null)
			return false;
		final Climate C=A.getClimateObj();
		if(C==null)
			return false;
		lastWeather=realLastWeather;

		// handle freeze overs
		if((coldWeather(lastWeather))
		&&(coldWeather(C.weatherType(null)))
		&&(lastWeather!=C.weatherType(null))
		&&(A.getTimeObj().getSeasonCode()==TimeClock.Season.WINTER)
		&&(CMLib.dice().rollPercentage()<freezeOverChance))
		{
			if(ticking instanceof Room)
			{
				final Room R=(Room)ticking;
				if((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
				&&(CMLib.dice().rollPercentage()<freezeOverChance)
				&&(R instanceof Drink)
				&&(((Drink)R).liquidType()==RawMaterial.RESOURCE_FRESHWATER))
				{
					final Ability A2=CMClass.getAbility("Spell_IceSheet");
					if(A2!=null)
					{
						final MOB mob=CMLib.map().getFactoryMOB(R);
						A2.invoke(mob,R,true,0);
						mob.destroy();
					}
				}
			}
			else
			for(final Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
			{
				final Room R=e.nextElement();
				if((R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
				&&(CMLib.dice().rollPercentage()<freezeOverChance))
				{
					final Ability A2=CMClass.getAbility("Spell_IceSheet");
					if(A2!=null)
					{
						final MOB mob=CMLib.map().getFactoryMOB(R);
						A2.invoke(mob,R,true,0);
						mob.destroy();
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
			{
				final Enumeration<Room> r=(ticking instanceof Room)?new SingleEnumeration<Room>((Room)ticking):A.getProperMap();
				for(;r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(CMLib.map().hasASky(R) && (R.numInhabitants() > 0))
					{
						for(int i=0;i<R.numInhabitants();i++)
						{
							final MOB mob=R.fetchInhabitant(i);
							if((mob!=null)
							&&(!mob.isMonster())
							&&(CMLib.flags().isAliveAwakeMobile(mob,true))
							&&(mob.isAttributeSet(MOB.Attrib.AUTOWEATHER)))
								mob.tell(C.getWeatherDescription(A));
						}
					}
				}
				break;
			}
			default:
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

			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				if((S.mob()==null)
				||(!isInArea(A,S.mob()))
				||(S.mob().isMonster()))
					continue;

				final MOB M=S.mob();
				final Room R=M.location();

				if((R.getClimateType()&Places.CLIMASK_COLD)>0)
				{
					if(coldChance>0)
						coldChance+=10;
					if(coldChance>0) fluChance+=5; // yes, cold is related this way to flu
					if(frostBiteChance>0)
						frostBiteChance=frostBiteChance+(int)Math.round(CMath.mul(frostBiteChance,0.5));
				}
				if((R.getClimateType()&Places.CLIMASK_HOT)>0)
				{
					if(heatExhaustionChance>0)
						heatExhaustionChance+=10;
				}
				if((R.getClimateType()&Places.CLIMASK_WET)>0)
				{
					if(coldChance>0)
						coldChance+=5;
					if(heatExhaustionChance>5)
						heatExhaustionChance-=5;
					if(frostBiteChance>0)
						frostBiteChance=frostBiteChance+(int)Math.round(CMath.mul(frostBiteChance,0.25));
				}
				final int save=(M.charStats().getSave(CharStats.STAT_SAVE_COLD)+M.charStats().getSave(CharStats.STAT_SAVE_WATER))/2;
				if((CMLib.dice().rollPercentage()<(coldChance-save))
				&&((C.weatherType(M.location())!=Climate.WEATHER_CLEAR)))
				{
					long coveredPlaces=0;
					for (final long element : ALL_COVERED_SPOTS)
					{
						if(M.getWearPositions(element)==0)
							coveredPlaces=coveredPlaces|element;
					}
					Item I=null;
					for(int i=0;i<M.numItems();i++)
					{
						I=M.getItem(i);
						if((I==null)||(I.amWearingAt(Wearable.IN_INVENTORY)))
							continue;
						if(I.amWearingAt(Wearable.WORN_ABOUT_BODY))
							coveredPlaces=coveredPlaces|Wearable.WORN_TORSO|Wearable.WORN_LEGS;
						for (final long element : ALL_COVERED_SPOTS)
						{
							if(I.amWearingAt(element))
								coveredPlaces=coveredPlaces|element;
						}
					}
					if((coveredPlaces!=ALL_COVERED_CODE)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
					{
						Ability COLD=CMClass.getAbility("Disease_Cold");
						if(CMLib.dice().rollPercentage()<(fluChance+(((M.location().getClimateType()&Places.CLIMASK_WET)>0)?10:0)))
							COLD=CMClass.getAbility("Disease_Flu");
						if((COLD!=null)&&(M.fetchEffect(COLD.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
							COLD.invoke(M,M,true,0);
					}
				}
				if((CMLib.dice().rollPercentage()<(frostBiteChance-save))
				&&((C.weatherType(M.location())!=Climate.WEATHER_CLEAR)))
				{
					long unfrostedPlaces=0;
					for (final long element : ALL_FROST_SPOTS)
					{
						if(M.getWearPositions(element)==0)
							unfrostedPlaces=unfrostedPlaces|element;
					}
					Item I=null;
					for(int i=0;i<M.numItems();i++)
					{
						I=M.getItem(i);
						if((I==null)||(I.amWearingAt(Wearable.IN_INVENTORY)))
							continue;
						for (final long element : ALL_FROST_SPOTS)
						{
							if(I.amWearingAt(element))
								unfrostedPlaces=unfrostedPlaces|element;
						}
					}
					if((unfrostedPlaces!=ALL_FROST_CODE)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
					{
						final Ability COLD=CMClass.getAbility("Disease_FrostBite");
						if((COLD!=null)&&(M.fetchEffect(COLD.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
							COLD.invoke(M,M,true,0);
					}
				}
				if((heatExhaustionChance>0)
				&&(CMLib.dice().rollPercentage()<10)
				&&(CMLib.dice().rollPercentage()<(heatExhaustionChance-M.charStats().getSave(CharStats.STAT_SAVE_FIRE)))
				&&(M.phyStats().level()>6)
				&&(C.weatherType(M.location())!=Climate.WEATHER_CLEAR)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability COLD=CMClass.getAbility("Disease_HeatExhaustion");
					if((COLD!=null)&&(M.fetchEffect(COLD.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
						COLD.invoke(M,M,true,0);
				}
			}
		}
		if((rumbleDown--)==1)
		{
			resetRumbleTicks();
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				final MOB mob=S.mob();
				if((mob==null)
				||(!isInArea(A,mob))
				||(mob.isMonster())
				||(!mob.isAttributeSet(MOB.Attrib.AUTOWEATHER)))
					continue;
				final Room R=mob.location();
				if(R!=null)
				{
					switch(C.weatherType(null))
					{
					case Climate.WEATHER_THUNDERSTORM:
					{
						if(C.weatherType(R)!=Climate.WEATHER_THUNDERSTORM)
						{
							if((R.domainType()&Room.INDOORS)>0)
							{
								if((R.getArea()!=null)
								&& CMath.div(R.getArea().getAreaIStats()[Area.Stats.INDOOR_ROOMS.ordinal()],R.getArea().properSize())<0.90)
									mob.tell(L("^JA thunderous rumble and CRACK of lightning can be heard outside.^?@x1",CMLib.protocol().msp("thunder.wav",40)));
							}
							else
								mob.tell(L("^JA thunderous rumble and CRACK of lightning can be heard.^?@x1",CMLib.protocol().msp("thunder.wav",40)));
						}
						else
						if(R.getArea().getTimeObj().getTODCode()==TimeClock.TimeOfDay.DAY)
							mob.tell(L("^JA thunderous rumble and CRACK of lightning can be heard as the pounding rain soaks you.^?@x1",CMLib.protocol().msp("thunderandrain.wav",40)));
						else
							mob.tell(L("^JA bolt of lightning streaks across the sky as the pounding rain soaks you!^?@x1",CMLib.protocol().msp("thunderandrain.wav",40)));
						break;
					}
					case Climate.WEATHER_BLIZZARD:
						if(C.weatherType(R)==Climate.WEATHER_BLIZZARD)
							mob.tell(L("^JSwirling clouds of snow buffet you.^?@x1",CMLib.protocol().msp("blizzard.wav",40)));
						break;
					case Climate.WEATHER_SNOW:
						if(C.weatherType(R)==Climate.WEATHER_SNOW)
							mob.tell(L("^JSnowflakes fall lightly on you.^?"));
						break;
					case Climate.WEATHER_DUSTSTORM:
						if(C.weatherType(R)==Climate.WEATHER_DUSTSTORM)
							mob.tell(L("^JSwirling clouds of dust assault you.^?@x1",CMLib.protocol().msp("windy.wav",40)));
						break;
					case Climate.WEATHER_HAIL:
						if(C.weatherType(R)==Climate.WEATHER_HAIL)
							mob.tell(L("^JYou are being pelleted by hail! Ouch!^?@x1",CMLib.protocol().msp("hail.wav",40)));
						break;
					case Climate.WEATHER_RAIN:
						if(C.weatherType(R)==Climate.WEATHER_RAIN)
							mob.tell(L("^JThe rain is soaking you!^?@x1",CMLib.protocol().msp("rainlong.wav",40)));
						break;
					case Climate.WEATHER_SLEET:
						if(C.weatherType(R)==Climate.WEATHER_SLEET)
							mob.tell(L("^JCold and blistering sleet is soaking you numb!^?@x1",CMLib.protocol().msp("rain.wav",40)));
						break;
					case Climate.WEATHER_WINDY:
						if(C.weatherType(R)==Climate.WEATHER_WINDY)
							mob.tell(L("^JThe wind gusts around you.^?@x1",CMLib.protocol().msp("wind.wav",40)));
						break;
					case Climate.WEATHER_HEAT_WAVE:
						if((C.weatherType(R)==Climate.WEATHER_HEAT_WAVE)
						&&(mob.charStats().getStat(CharStats.STAT_SAVE_FIRE)<10)
						&&((mob.fetchWornItems(Item.WORN_TORSO,(short)0,(short)0).size()>0)
							||(mob.fetchWornItems(Item.WORN_ABOUT_BODY,(short)0,(short)0).size()>0)))
							mob.tell(L("^JYou are sweating in the grueling heat.^?"));
						break;
					case Climate.WEATHER_WINTER_COLD:
						if((C.weatherType(R)==Climate.WEATHER_WINTER_COLD)
						&&(mob.charStats().getStat(CharStats.STAT_SAVE_COLD)<10))
							mob.tell(L("^JYou shiver in the cold.^?"));
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
				boolean playerAround=false;
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					if((S.mob()==null)
					||(!isInArea(A,S.mob()))
					||(S.mob().isMonster())
					||(C.weatherType(S.mob().location())!=Climate.WEATHER_THUNDERSTORM))
						continue;
					playerAround=true;
				}
				if(playerAround)
				{
					int attempts=50;
					Room R=(ticking instanceof Room)?(Room)ticking:null;
					MOB M=(R!=null)?R.fetchRandomInhabitant():null;
					while(((R==null)||(M==null))&&((--attempts)>=0)&&(ticking==A))
					{
						R=A.getRandomProperRoom();
						if(C.weatherType(R)!=Climate.WEATHER_THUNDERSTORM)
						{
							R=null;
							M=null;
						}
						else
							M=R.fetchRandomInhabitant();
					}
					if((R!=null)&&(M!=null))
					{
						final Ability A2=CMClass.getAbility("Chant_SummonLightning");
						if(A2!=null)
						{
							A2.setMiscText("RENDER MUNDANE");
							A2.invoke(M,M,true,M.phyStats().level());
						}
						Room R2=null;
						final Enumeration<Room> e=(ticking instanceof Room)?new SingleEnumeration<Room>((Room)ticking):A.getProperMap();
						for(;e.hasMoreElements();)
						{
							R2=e.nextElement();
							if((R2!=R)&&(R2.numInhabitants()>0))
							{
								if((A.getTimeObj().getTODCode()==TimeClock.TimeOfDay.DAY)
								||(C.weatherType(R2)!=Climate.WEATHER_THUNDERSTORM))
								{
									if((R2.domainType()&Room.INDOORS)>0)
										R2.showHappens(CMMsg.MSG_OK_ACTION,L("^JA thunderous rumble and crack of lightning can be heard outside.^?@x1",CMLib.protocol().msp("thunder2.wav",40)));
									else
										R2.showHappens(CMMsg.MSG_OK_ACTION,L("^JA thunderous rumble and crack of lightning can be heard.^?@x1",CMLib.protocol().msp("thunder2.wav",40)));
								}
								else
									R2.showHappens(CMMsg.MSG_OK_ACTION,L("^JYou hear a thunderous rumble as a bolt of lightning streaks across the sky!^?@x1",CMLib.protocol().msp("thunder3.wav",40)));
							}
						}
					}
				}
			}
		}
		if((tornadoDown--)==1)
		{
			resetTornadoTicks();
			if((C.weatherType(null)==Climate.WEATHER_THUNDERSTORM)
			||(C.weatherType(null)==Climate.WEATHER_WINDY))
			{
				boolean playerAround=false;
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					if((S.mob()==null)
					||(!isInArea(A,S.mob()))
					||(S.mob().isMonster())
					||(C.weatherType(S.mob().location())!=Climate.WEATHER_THUNDERSTORM))
						continue;
					playerAround=true;
				}
				if(playerAround)
				{
					int attempts=50;
					Room R=(ticking instanceof Room)?(Room)ticking:null;
					MOB M=(R!=null)?R.fetchRandomInhabitant():null;
					while(((R==null)||(M==null))&&((--attempts)>=0)&&(ticking==A))
					{
						R=A.getRandomProperRoom();
						if(C.weatherType(R)!=Climate.WEATHER_THUNDERSTORM)
						{
							R=null;
							M=null;
						}
						else
							M=R.fetchRandomInhabitant();
					}
					if((R!=null)&&(M!=null))
					{
						final Ability A2=CMClass.getAbility("Chant_SummonTornado");
						if(A2!=null)
						{
							A2.setMiscText("RENDER MUNDANE");
							final MOB mob=CMLib.map().getFactoryMOB(R);
							A2.invoke(mob,null,true,0);
							mob.destroy();
						}
						Room R2=null;
						final Enumeration<Room> e=(ticking instanceof Room)?new SingleEnumeration<Room>((Room)ticking):A.getProperMap();
						for(;e.hasMoreElements();)
						{
							R2=e.nextElement();
							if((R2!=R)&&(R2.numInhabitants()>0))
							{
								if((A.getTimeObj().getTODCode()==TimeClock.TimeOfDay.DAY)
								||(C.weatherType(R2)!=Climate.WEATHER_THUNDERSTORM))
								{
									if((R2.domainType()&Room.INDOORS)>0)
										R2.showHappens(CMMsg.MSG_OK_ACTION,L("^JThe terrible rumble of a tornado can be heard outside.^?@x1",CMLib.protocol().msp("tornado.wav",40)));
									else
										R2.showHappens(CMMsg.MSG_OK_ACTION,L("^JThe terrible rumble of a tornado can be heard.^?@x1",CMLib.protocol().msp("tornado.wav",40)));
								}
								else
									R2.showHappens(CMMsg.MSG_OK_ACTION,L("^JA huge and terrible tornado touches down somewhere near by.^?@x1",CMLib.protocol().msp("tornado.wav",40)));
							}
						}
					}
				}
			}
		}
		if((dustDown--)==1)
		{
			resetDustTicks();
			if(C.weatherType(null)==Climate.WEATHER_DUSTSTORM)
			{
				final Vector<Room> choices=new Vector<Room>();
				Room R=null;
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					if((S.mob()==null)
					||(!isInArea(A,S.mob()))
					||(S.mob().isMonster())
					||(C.weatherType(S.mob().location())!=Climate.WEATHER_DUSTSTORM))
						continue;
					R=S.mob().location();
					if((R!=null)&&(!choices.contains(R)))
						choices.addElement(R);
				}
				if(choices.size()>0)
				{
					R=choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
					final MOB M=R.fetchRandomInhabitant();
					if((M!=null)
					&&(C.weatherType(R)==Climate.WEATHER_DUSTSTORM)
					&&(!CMLib.flags().isSleeping(M)))
					{
						final Ability A2=CMClass.getAbility("Skill_Dirt");
						if(A2!=null)
							A2.invoke(M,M,true,0);
					}
				}
			}
		}
		if((hailDown--)==1)
		{
			resetHailTicks();
			if(C.weatherType(null)==Climate.WEATHER_HAIL)
			{
				final Vector<Room> choices=new Vector<Room>();
				Room R=null;
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					if((S.mob()==null)
					||(!isInArea(A,S.mob()))
					||(S.mob().isMonster())
					||(C.weatherType(S.mob().location())!=Climate.WEATHER_HAIL))
						continue;
					R=S.mob().location();
					if((R!=null)&&(!choices.contains(R)))
						choices.addElement(R);
				}
				if(choices.size()>0)
				{
					R=choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
					final MOB M=R.fetchRandomInhabitant();
					final Ability A2=CMClass.getAbility("Chant_SummonHail");
					if((A2!=null)
					&&(C.weatherType(R)==Climate.WEATHER_HAIL))
					{
						A2.setMiscText("RENDER MUNDANE");
						A2.invoke(M,M,true,M.phyStats().level());
					}
				}
			}
		}
		if((C.weatherType(null)==Climate.WEATHER_DROUGHT)
		&&(CMLib.dice().rollPercentage()<droughtFireChance))
		{
			Room R=CMLib.map().roomLocation((Environmental)ticking);
			if((R==null)&&(ticking instanceof Area))
				R=((Area)ticking).getRandomProperRoom();
			if((R!=null)
			&&((R.domainType()&Room.INDOORS)==0)
			&&(R.domainType()!=Room.DOMAIN_OUTDOORS_SWAMP)
			&&(!CMLib.flags().isWateryRoom(R))
			&&(!CMath.bset(R.getClimateType(),Places.CLIMASK_WET)))
			{
				final Item I=R.getRandomItem();
				if((I!=null)&&(CMLib.flags().isGettable(I)))
				{
					switch(I.material()&RawMaterial.MATERIAL_MASK)
					{
					case RawMaterial.MATERIAL_CLOTH:
					case RawMaterial.MATERIAL_LEATHER:
					case RawMaterial.MATERIAL_PAPER:
					case RawMaterial.MATERIAL_VEGETATION:
					case RawMaterial.MATERIAL_WOODEN:
					{
						final MOB god=CMClass.getFactoryMOB("the heat",CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL),R);
						try
						{
							if(R.show(god,I,null,CMMsg.MSG_WEATHER,L("<T-NAME> spontaneously combusts in the seering heat!@x2",CMLib.protocol().msp("fire.wav",40))))
							{
								final Ability A2=CMClass.getAbility("Burning");
								if(A2!=null)
									A2.invoke(god,I,true,0);
							}
						}
						finally
						{
							god.destroy();
						}
						break;
					}
					default:
						break;
					}
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
				final Vector<Room> choices=new Vector<Room>();
				Room R=null;
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					if((S.mob()==null)
					||(!isInArea(A,S.mob()))
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
					R=choices.elementAt(CMLib.dice().roll(1,choices.size(),-1));
					final MOB M=CMLib.map().getFactoryMOB(R);
					final Ability A2=CMClass.getAbility("Chant_WindGust");
					if(A2!=null)
					{
						A2.setMiscText("RENDER MUNDANE");
						A2.invoke(M,M,true,M.phyStats().level());
					}
					M.destroy();
				}
			}
		}
		if((rustDown--)==1)
		{
			resetRustTicks();
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				if((S.mob()==null)
				||(!isInArea(A,S.mob()))
				||(S.mob().isMonster()))
					continue;
				int rustChance=0;
				switch(C.weatherType(S.mob().location()))
				{
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_SLEET:
				case Climate.WEATHER_SNOW:
					rustChance=3;
					break;
				case Climate.WEATHER_HAIL:
					rustChance=3;
					break;
				case Climate.WEATHER_THUNDERSTORM:
				case Climate.WEATHER_RAIN:
					rustChance=4;
					break;
				}

				final MOB M=S.mob();
				final Room R=M.location();
				if(R==null)
					continue;

				if(CMLib.flags().isWaterySurfaceRoom(R))
				{
					if(!CMLib.flags().isFlying(M))
						rustChance+=3;
				}
				else
				if(CMLib.flags().isUnderWateryRoom(R))
					rustChance+=5;
				if((R.getClimateType()&Places.CLIMASK_WET)>0)
					rustChance+=2;
				if(CMLib.dice().rollPercentage()<rustChance)
				{
					final int weatherType=C.weatherType(R);
					final Vector<Item> rustThese=new Vector<Item>();
					for(int i=0;i<M.numItems();i++)
					{
						final Item I=M.getItem(i);
						if(I==null)
							continue;
						if(!I.amWearingAt(Wearable.IN_INVENTORY))
						{
							if((((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL))
							&&(I.subjectToWearAndTear())
							&&((CMLib.dice().rollPercentage()>I.phyStats().ability()*25)))
								rustThese.addElement(I);
							else
							if(I.amWearingAt(Wearable.WORN_ABOUT_BODY)
							&&(((I.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_METAL)))
							{
								rustThese.clear();
								break;
							}
						}
					}
					for(int i=0;i<rustThese.size();i++)
					{
						final Item I=rustThese.elementAt(i);
						CMLib.combat().postItemDamage(M, I, null, 1, CMMsg.TYP_WATER, (weatherType!=0)?"<T-NAME> rusts.":"<T-NAME> rusts in the water.");
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
