package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DisFlag;
import com.planet_ink.coffee_mud.core.CMath.CompiledFormula;
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
   Copyright 2004-2023 Bo Zimmerman

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

	protected Ability obscureA			= null;

	protected int	puddlepct			= 0;
	protected int	windsheer			= 0;
	protected int	rustDown			= 0;
	protected int	botherDown			= 0;
	protected int	rumbleDown			= 0;
	protected int	gustDown			= 0;
	protected int	tornadoDown			= 0;
	protected int	lightningDown		= 0;
	protected int	hailDown			= 0;
	protected int	boatSlipChance		= 0;
	protected int	rainSlipChance		= 0;
	protected int	snowSlipChance		= 0;
	protected int	sleetSlipChance		= 0;
	protected int	freezeOverChance	= 0;
	protected int	dustDown			= 0;
	protected int	diseaseDown			= 0;
	protected int	droughtFireChance	= 0;
	protected int	forceWeatherCode	= -1;
	protected int	forceSkyWeatherCode	= -1;
	protected Area	exceptArea			= null;

	protected CompiledFormula	boatDmgChanceFormula	= null;
	protected CompiledFormula	boatDmgAmtFormula		= null;
	protected String			boatDmgName				= null;
	protected Set<Room>			roomExceptions			= new HashSet<Room>();

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
	public CMObject copyOf()
	{
		final WeatherAffects B=(WeatherAffects)super.copyOf();
		B.roomExceptions = new HashSet<Room>();
		return B;
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

	protected int getWeatherCodeParm(final String weatherStr)
	{
		if((weatherStr != null) && (weatherStr.length()>0))
		{
			int x;
			if(CMath.isInteger(weatherStr))
				x = CMath.s_int(weatherStr);
			else
				x = CMParms.indexOf(Climate.WEATHER_DESCS,weatherStr);
			if((x >= 0)&&(x<Climate.NUM_WEATHER))
				return x;
		}
		return -1;
	}

	@Override
	public void setParms(final String newParms)
	{
		exceptArea=null;
		roomExceptions.clear();
		parms=newParms;
		boatDmgChanceFormula = null;
		boatDmgAmtFormula = null;
		puddlepct=CMParms.getParmInt(parms,"puddlepct",50);
		windsheer=CMParms.getParmInt(parms,"windsheer",10);
		rainSlipChance=CMParms.getParmInt(parms,"rainslipchance",1);
		snowSlipChance=CMParms.getParmInt(parms,"snowslipchance",5);
		sleetSlipChance=CMParms.getParmInt(parms,"sleetslipchance",10);
		freezeOverChance=CMParms.getParmInt(parms,"iceoverchance",50);
		droughtFireChance=CMParms.getParmInt(parms,"droughtfirechance",1);
		boatSlipChance=CMParms.getParmInt(parms,"boatslipchance",20);
		forceWeatherCode=getWeatherCodeParm(CMParms.getParmStr(parms, "weather", "").toUpperCase().trim());
		forceSkyWeatherCode=getWeatherCodeParm(CMParms.getParmStr(parms, "skyweather", "").toUpperCase().trim());
		final String dmgChanceStr = CMParms.getParmStr(parms, "shipdmgpct", "");
		if(dmgChanceStr.length()>0)
		{
			try
			{
				boatDmgChanceFormula=CMath.compileMathExpression(dmgChanceStr);
				final String dmgStr = CMParms.getParmStr(parms, "dmgship", "");
				boatDmgName = CMParms.getParmStr(parms, "dmgname", null);
				if(dmgStr.length()>0)
					boatDmgAmtFormula=CMath.compileMathExpression(dmgStr);
				else
					boatDmgChanceFormula=null;
			}
			catch(final Exception e)
			{
				Log.errOut("WeatherAffects",e.getMessage());
			}
		}
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

	protected int areaWeather(final Environmental host)
	{
		if(forceSkyWeatherCode >= 0)
			return forceSkyWeatherCode;
		if(forceWeatherCode >= 0)
			return forceWeatherCode;
		Area A=(host instanceof Area)?(Area)host:CMLib.map().areaLocation(host);
		if(A!=null)
		{
			if(A instanceof Boardable)
				A=CMLib.map().areaLocation(((Boardable)A).getBoardableItem());
			if(A!=null)
			{
				final Climate C=A.getClimateObj();
				return (C!=null)?C.weatherType(null):0;
			}
		}
		return Climate.WEATHER_CLEAR;
	}

	protected int roomWeather(final Environmental host, Room room, final int areaWeather)
	{
		if(room == null)
			return Climate.WEATHER_CLEAR;
		if(room.getArea() instanceof Boardable)
		{
			room=CMLib.map().roomLocation(((Boardable)room.getArea()).getBoardableItem());
			if(room == null)
				return Climate.WEATHER_CLEAR;
		}
		final boolean hasASky = CMLib.map().hasASky(room);
		if(host instanceof Room)
		{
			if((forceSkyWeatherCode >= 0)&&(hasASky))
				return forceSkyWeatherCode;
			if(forceWeatherCode >= 0)
				return forceWeatherCode;
		}
		if(areaWeather == 0)
			return Climate.WEATHER_CLEAR;
		if(CMath.bset(room.getClimateType(), Places.CLIMASK_VOID))
			return 0;
		if((host instanceof Area) && (roomExceptions.contains(room)))
			return 0;
		if((forceSkyWeatherCode >= 0)&&(hasASky))
			return forceSkyWeatherCode;
		if(!hasASky)
			return Climate.WEATHER_CLEAR;
		return areaWeather;
	}

	protected int roomWeather(final Environmental host, final Room room)
	{
		return roomWeather(host,room,areaWeather(host));
	}

	protected boolean isInclement(final int weather)
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

	protected boolean isOkishWeather(final int weather)
	{
		switch(weather)
		{
			case Climate.WEATHER_CLEAR:
			case Climate.WEATHER_CLOUDY:
			case Climate.WEATHER_DROUGHT:
			case Climate.WEATHER_HEAT_WAVE:
				return true;
			default:
				return false;
		}
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host, msg);
		final Room R=msg.source().location();
		if((host instanceof Area)
		&&((R==null)||(R.getArea()!=host)))
			return;

		if((this.boatDmgChanceFormula != null)
		&&(msg.source().riding() instanceof Item)
		&&(msg.sourceMajor(CMMsg.MASK_MOVE))
		&&(CMLib.flags().isWaterySurfaceRoom(R)))
		{
			final Item I=(Item)msg.source().riding();
			final Rideable sR = msg.source().riding();
			if(((sR.rideBasis()==Rideable.Basis.WATER_BASED)||(sR instanceof Boardable))
			&&(!CMLib.flags().isABonusItems(sR))
			&&(!sR.phyStats().isAmbiance("-ANTIWEATHER")))
			{
				int rooms = 0;
				if(sR instanceof Boardable)
					rooms = ((Boardable)sR).getArea().properSize();
				final int weather=roomWeather(host,R);
				int windLevel;
				switch(weather)
				{
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_DUSTSTORM:
				case Climate.WEATHER_THUNDERSTORM:
				case Climate.WEATHER_WINDY:
					windLevel=1+(CMath.bset(R.getClimateType(),Area.CLIMASK_WINDY)?1:0);
					break;
				default:
					windLevel=0;
					break;
				}
				int wetLevel;
				switch(weather)
				{
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_THUNDERSTORM:
					wetLevel=2+(CMath.bset(R.getClimateType(),Area.CLIMASK_WINDY)?1:0);
					break;
				case Climate.WEATHER_RAIN:
				case Climate.WEATHER_SLEET:
				case Climate.WEATHER_SNOW:
					wetLevel=1;
					break;
				default:
					wetLevel=0;
					break;
				}
				final double[] vars = new double[] { rooms, windLevel, wetLevel };
				if(CMLib.dice().rollPercentage() < (int)Math.round(CMath.parseMathExpression(boatDmgChanceFormula, vars, 0.0)))
				{
					final int damage = (int)Math.round(CMath.parseMathExpression(boatDmgAmtFormula, vars, 0.0));
					if(damage > 0)
					{
						final String name = (boatDmgName != null) ? boatDmgName :
							L("the @x1",Climate.WEATHER_DESCS[weather].toLowerCase());
						final MOB M = CMClass.getFactoryMOB(name, 1, R);
						final String msgStr = "<S-NAME> <DAMAGES> <T-NAME>.";
						CMLib.combat().postSiegeDamage(M, M, I, R, msgStr, Weapon.TYPE_BASHING, damage);
						M.destroy();
					}
				}
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{
		if(!super.okMessage(host,msg))
			return false;

		final Room R=msg.source().location();
		if((host instanceof Area)
		&&((R==null)||(R.getArea()!=host)))
			return true;

		if(isOkishWeather(lastWeather))
			return true;
		final int weather=roomWeather(host,R);
		if(isOkishWeather(weather))
			return true;
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
			final int range;
			if(msg.source().getVictim()==msg.target())
				range=msg.source().rangeToTarget();
			else
			if(msg.target() instanceof MOB)
				range = CMLib.combat().calculateRangeToTarget(msg.source(), (MOB)msg.target(), (Item)msg.tool());
			else
				range=0;
			if(CMLib.dice().rollPercentage()<(range*10))
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
				default:
					break;
				}
			}
			if(CMLib.dice().rollPercentage()<(range*10))
			{
				switch(weather)
				{
				case Climate.WEATHER_WINDY:
				case Climate.WEATHER_THUNDERSTORM:
				case Climate.WEATHER_BLIZZARD:
				case Climate.WEATHER_DUSTSTORM:
				case Climate.WEATHER_FOG:
				{
					final String weatherWord = Climate.WEATHER_DESCS[weather].toLowerCase();
					msg.source().tell(L("You fail to aim @x1 due to the @x2.",((Item)msg.tool()).name(msg.source()),weatherWord));
					break;
				}
				default:
					break;
				}
			}
		}
		// then try to handle slippage of boats and feet in bad weather
		if((msg.sourceMajor(CMMsg.MASK_MOVE))
		&&(R!=null)
		&&(isInclement(weather)))
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
				&&((riding.rideBasis()==Rideable.Basis.WATER_BASED)||(riding instanceof Boardable))
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
						final CMMsg wmsg=CMClass.getMsg(msg.source(),riding,CMMsg.MSG_WEATHER,L("^W<S-NAME> make(s) no progress in the "+what+".^?"));
						if(R.okMessage(msg.source(), wmsg))
						{
							R.send(msg.source(), wmsg);
							if(riding instanceof Boardable)
							{
								final Area shipArea=((Boardable)riding).getArea();
								if(shipArea != null)
								{
									for(final Enumeration<Room> sr=shipArea.getProperMap();sr.hasMoreElements();)
									{
										final Room sR=sr.nextElement();
										if((sR!=null)
										&&((sR.domainType()&Room.INDOORS)==0))
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
				&&(CMLib.flags().isStanding(msg.source()))
				&&(msg.source().riding()==null)
				&&(CMLib.dice().rollPercentage()>((msg.source().charStats().getStat(CharStats.STAT_DEXTERITY)*3)+25)))
				{
					if(R.show(msg.source(),null,CMMsg.MSG_WEATHER,L("^W<S-NAME> slip(s) on the "+what+" ground.^?")))
					{
						final Ability A=CMClass.getAbility("Skill_Trip");
						A.startTickDown(msg.source(), msg.source(), 3);
						msg.source().recoverPhyStats();
						return false;
					}
				}
			}
			}
		}
		if(R!=null)
		{
			if((weather==Climate.WEATHER_BLIZZARD)
			||(weather==Climate.WEATHER_FOG)
			||(weather==Climate.WEATHER_DUSTSTORM))
			{
				if(obscureA==null)
					obscureA=CMClass.getAbility("Spell_ObscureSelf");
				if(obscureA!=null)
				{
					obscureA.setAffectedOne(msg.source());
					if(!obscureA.okMessage(msg.source(),msg))
						return false;
				}
				if(weather==Climate.WEATHER_FOG)
				{
					switch(msg.targetMinor())
					{
					case CMMsg.TYP_LOOK:
					case CMMsg.TYP_EXAMINE:
					case CMMsg.TYP_READ:
					case CMMsg.TYP_WASREAD:
					case CMMsg.TYP_OK_VISUAL:
					{
						if(msg.target() instanceof Exit)
						{
							msg.source().tell(L("It is too foggy to see that."));
							return false;
						}
						break;
					}
					case CMMsg.TYP_LOOK_EXITS:
						if((msg.target() instanceof Room)
						&&(msg.source().location()==msg.target()))
						{
							msg.source().tell(L("It is too foggy to tell."));
							return false;
						}
						break;
					case CMMsg.TYP_NOISYMOVEMENT:
						if((msg.target() instanceof SiegableItem)
						&&(msg.tool() instanceof SiegableItem)
						&&(SiegableItem.SiegeCommand.AIM.name().equals(msg.targetMessage())))
						{
							if(((SiegableItem)msg.target()).rangeToTarget()>2)
							{
								final String weatherWord = Climate.WEATHER_DESCS[weather].toLowerCase();
								msg.source().tell(L("You fail to aim @x1 due to the @x2.",((SiegableItem)msg.tool()).name(msg.source()),weatherWord));
								return false;
							}
						}
						break;
					default:
						break;
					}
				}
			}
		}
		return true;
	}

	protected boolean isInHere(final Environmental host, final MOB M)
	{
		if(M!=null)
		{
			Room R=M.location();
			if(R!=null)
			{
				final Area A=R.getArea();
				if(A instanceof Boardable)
					R=CMLib.map().roomLocation(((Boardable)A).getBoardableItem());
			}
			if(R!=null)
			{
				if(host instanceof Room)
					return R==host;
				if(host instanceof Area)
					return R.getArea()==host;
				return CMLib.map().roomLocation(host)==R;
			}
		}
		return false;
	}

	protected boolean inATent(final MOB M)
	{
		if(M.riding()==null)
			return false;
		return M.riding().rideBasis()==Rideable.Basis.ENTER_IN;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final int realLastWeather=super.lastWeather;
		if(!super.tick(ticking,tickID))
			return false;
		final Environmental host;
		final Area A;
		if(ticking instanceof Environmental)
		{
			host = (Environmental)ticking;
			if(host instanceof Area)
				A=(Area)host;
			else
			{
				if(host instanceof Room)
					A=((Room)host).getArea();
				else
					A=CMLib.map().areaLocation(host);
				if(A==null)
					return false;
			}
		}
		else
			return false;
		if(this.exceptArea != A)
		{
			exceptArea = A;
			if(ticking instanceof Room)
			{
				final WeatherAffects aA=(WeatherAffects)A.fetchBehavior(ID());
				if(aA != null)
					aA.roomExceptions.add((Room)ticking);
			}
		}
		lastWeather=realLastWeather;
		final int areaWeather = this.areaWeather(host);

		// handle freeze overs
		if((coldWeather(lastWeather))
		&&(coldWeather(areaWeather))
		&&(lastWeather!=areaWeather)
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
			switch(areaWeather)
			{
			case Climate.WEATHER_BLIZZARD:
			case Climate.WEATHER_SLEET:
			case Climate.WEATHER_SNOW:
			case Climate.WEATHER_HAIL:
			case Climate.WEATHER_THUNDERSTORM:
			case Climate.WEATHER_RAIN:
			{
				final Climate C=A.getClimateObj();
				if(C==null)
					break;
				final Enumeration<Room> r=(ticking instanceof Room)?new SingleEnumeration<Room>((Room)ticking):A.getProperMap();
				for(;r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((roomWeather(host,R,areaWeather)==areaWeather)
					&& (R.numInhabitants() > 0))
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
			switch(areaWeather)
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

			for(final Session S : CMLib.sessions().localOnlineIterableAllHosts())
			{
				final MOB M=S.mob();
				if((M==null)
				||(!isInHere(host,M))
				||(M.isMonster()))
					continue;

				final Room R=M.location();
				final boolean inATent = inATent(M);
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
				if(inATent)
				{
					coldChance/=2;
					fluChance/=2;
					frostBiteChance/=2;
					heatExhaustionChance/=2;
				}
				final int save=M.charStats().getSave(CharStats.STAT_SAVE_COLD)+M.charStats().getSave(CharStats.STAT_SAVE_WATER);
				if((CMLib.dice().rollPercentage()<(coldChance-save))
				&&((roomWeather(host,M.location(),areaWeather)!=Climate.WEATHER_CLEAR)))
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
						if((I==null)||(!I.amBeingWornProperly()))
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
				&&((roomWeather(host,M.location(),areaWeather)!=Climate.WEATHER_CLEAR)))
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
					if((unfrostedPlaces!=ALL_FROST_CODE)
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
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
				&&(roomWeather(host,M.location(),areaWeather)!=Climate.WEATHER_CLEAR)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability coldA=CMClass.getAbility("Disease_HeatExhaustion");
					if((coldA!=null)&&(M.fetchEffect(coldA.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
						coldA.invoke(M,M,true,0);
				}
			}
		}
		if((rumbleDown--)==1)
		{
			resetRumbleTicks();
			for(final Session S : CMLib.sessions().localOnlineIterableAllHosts())
			{
				final MOB mob=S.mob();
				if((mob==null)
				||(!isInHere(host,mob))
				||(mob.isMonster())
				||(!mob.isAttributeSet(MOB.Attrib.AUTOWEATHER)))
					continue;
				final Room R=mob.location();
				if((R!=null)
				&&(!CMath.bset(R.getClimateType(), Places.CLIMASK_VOID)))
				{
					final boolean inATent = inATent(mob);
					switch(areaWeather)
					{
					case Climate.WEATHER_THUNDERSTORM:
					{
						if(roomWeather(host,R,areaWeather)!=Climate.WEATHER_THUNDERSTORM)
						{
							if(inATent || (R.domainType()&Room.INDOORS)>0)
							{
								if((R.getArea()!=null)
								&& CMath.div(R.getArea().getAreaIStats()[Area.Stats.INDOOR_ROOMS.ordinal()],R.getArea().getAreaIStats()[Area.Stats.COUNTABLE_ROOMS.ordinal()])<0.90)
									mob.tell(L("^JA thunderous rumble and CRACK of lightning can be heard outside.^?@x1",CMLib.protocol().msp("thunder.wav",40)));
							}
							else
								mob.tell(L("^JA thunderous rumble and CRACK of lightning can be heard.^?@x1",CMLib.protocol().msp("thunder.wav",40)));
						}
						else
						if(inATent || R.getArea().getTimeObj().getTODCode()==TimeClock.TimeOfDay.DAY)
							mob.tell(L("^JA thunderous rumble and CRACK of lightning can be heard as the pounding rain soaks you.^?@x1",CMLib.protocol().msp("thunderandrain.wav",40)));
						else
							mob.tell(L("^JA bolt of lightning streaks across the sky as the pounding rain soaks you!^?@x1",CMLib.protocol().msp("thunderandrain.wav",40)));
						break;
					}
					case Climate.WEATHER_BLIZZARD:
						if((!inATent) && (roomWeather(host,R,areaWeather)==areaWeather))
							mob.tell(L("^JSwirling clouds of snow buffet you.^?@x1",CMLib.protocol().msp("blizzard.wav",40)));
						break;
					case Climate.WEATHER_SNOW:
						if((!inATent) && (roomWeather(host,R,areaWeather)==areaWeather))
							mob.tell(L("^JSnowflakes fall lightly on you.^?"));
						break;
					case Climate.WEATHER_DUSTSTORM:
						if((!inATent) && (roomWeather(host,R,areaWeather)==areaWeather))
							mob.tell(L("^JSwirling clouds of dust assault you.^?@x1",CMLib.protocol().msp("windy.wav",40)));
						break;
					case Climate.WEATHER_HAIL:
						if((!inATent) && (roomWeather(host,R,areaWeather)==areaWeather))
							mob.tell(L("^JYou are being pelleted by hail! Ouch!^?@x1",CMLib.protocol().msp("hail.wav",40)));
						break;
					case Climate.WEATHER_RAIN:
						if((!inATent) && (roomWeather(host,R,areaWeather)==areaWeather))
							mob.tell(L("^JThe rain is soaking you!^?@x1",CMLib.protocol().msp("rainlong.wav",40)));
						break;
					case Climate.WEATHER_SLEET:
						if((!inATent) && (roomWeather(host,R,areaWeather)==areaWeather))
							mob.tell(L("^JCold and blistering sleet is soaking you numb!^?@x1",CMLib.protocol().msp("rain.wav",40)));
						break;
					case Climate.WEATHER_WINDY:
						if((!inATent) && (roomWeather(host,R,areaWeather)==areaWeather))
							mob.tell(L("^JThe wind gusts around you.^?@x1",CMLib.protocol().msp("wind.wav",40)));
						break;
					case Climate.WEATHER_HEAT_WAVE:
						if((roomWeather(host,R,areaWeather)==Climate.WEATHER_HEAT_WAVE)
						&&(mob.charStats().getStat(CharStats.STAT_SAVE_FIRE)<10)
						&&((mob.fetchWornItems(Item.WORN_TORSO,(short)0,(short)0).size()>0)
							||(mob.fetchWornItems(Item.WORN_ABOUT_BODY,(short)0,(short)0).size()>0)))
							mob.tell(L("^JYou are sweating in the grueling heat.^?"));
						break;
					case Climate.WEATHER_WINTER_COLD:
						if((roomWeather(host,R,areaWeather)==Climate.WEATHER_WINTER_COLD)
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
			if(areaWeather==Climate.WEATHER_THUNDERSTORM)
			{
				boolean playerAround=false;
				for(final Session S : CMLib.sessions().localOnlineIterableAllHosts())
				{
					if((S.mob()==null)
					||(!isInHere(host,S.mob()))
					||(S.mob().isMonster())
					||(roomWeather(host,S.mob().location(),areaWeather)!=Climate.WEATHER_THUNDERSTORM)
					||(inATent(S.mob())))
						continue;
					playerAround=true;
					break;
				}
				if(playerAround)
				{
					int attempts=50;
					Room R=(ticking instanceof Room)?(Room)ticking:null;
					MOB M=(R!=null)?R.fetchRandomInhabitant():null;
					while(((R==null)||(M==null))
					&&((--attempts)>=0)
					&&(ticking==A))
					{
						R=A.getRandomProperRoom();
						if(roomWeather(host,R,areaWeather)!=Climate.WEATHER_THUNDERSTORM)
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
							int level = M.phyStats().level()/2;
							if(level < 1)
								level = 1;
							A2.invoke(M,M,true,level);
						}
						Room R2=null;
						final Enumeration<Room> e=(ticking instanceof Room)?new SingleEnumeration<Room>((Room)ticking):A.getProperMap();
						for(;e.hasMoreElements();)
						{
							R2=e.nextElement();
							if((R2!=R)
							&&(R2.numInhabitants()>0)
							&&(!CMath.bset(R2.getClimateType(), Places.CLIMASK_VOID)))
							{
								if((A.getTimeObj().getTODCode()==TimeClock.TimeOfDay.DAY)
								||(roomWeather(host,R2,areaWeather)!=Climate.WEATHER_THUNDERSTORM))
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
			if((areaWeather==Climate.WEATHER_THUNDERSTORM)
			||(areaWeather==Climate.WEATHER_WINDY))
			{
				boolean playerAround=false;
				for(final Session S : CMLib.sessions().localOnlineIterableAllHosts())
				{
					if((S.mob()==null)
					||(!isInHere(host,S.mob()))
					||(S.mob().isMonster()))
						continue;
					final int roomWeather = roomWeather(host,S.mob().location(),areaWeather);
					if((roomWeather!=Climate.WEATHER_THUNDERSTORM)
					&&(roomWeather!=Climate.WEATHER_WINDY))
						continue;
					playerAround=true;
					break;
				}
				if(playerAround)
				{
					int attempts=50;
					Room R=(ticking instanceof Room)?(Room)ticking:null;
					MOB M=(R!=null)?R.fetchRandomInhabitant():null;
					while(((R==null)||(M==null))&&((--attempts)>=0)&&(ticking==A))
					{
						R=A.getRandomProperRoom();
						if(roomWeather(host,R,areaWeather)!=Climate.WEATHER_THUNDERSTORM)
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
							if((R2!=R)
							&&(!CMath.bset(R2.getClimateType(), Places.CLIMASK_VOID))
							&&(R2.numInhabitants()>0))
							{
								if((A.getTimeObj().getTODCode()==TimeClock.TimeOfDay.DAY)
								||(roomWeather(host,R2,areaWeather)!=Climate.WEATHER_THUNDERSTORM))
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
			if(areaWeather==Climate.WEATHER_DUSTSTORM)
			{
				final List<Room> choices=new ArrayList<Room>(CMLib.sessions().numSessions());
				Room R=null;
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					if((S.mob()==null)
					||(!isInHere(host,S.mob()))
					||(S.mob().isMonster())
					||(inATent(S.mob()))
					||(roomWeather(host,S.mob().location(),areaWeather)!=Climate.WEATHER_DUSTSTORM))
						continue;
					R=S.mob().location();
					if((R!=null)&&(!choices.contains(R)))
						choices.add(R);
				}
				if(choices.size()>0)
				{
					R=choices.get(CMLib.dice().roll(1,choices.size(),-1));
					final MOB M=R.fetchRandomInhabitant();
					if((M!=null)
					&&(roomWeather(host,R,areaWeather)==Climate.WEATHER_DUSTSTORM)
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
			if(areaWeather==Climate.WEATHER_HAIL)
			{
				final List<Room> choices=new ArrayList<Room>(CMLib.sessions().numSessions());
				Room R=null;
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					if((S.mob()==null)
					||(!isInHere(host,S.mob()))
					||(S.mob().isMonster())
					||(inATent(S.mob()))
					||(roomWeather(host,S.mob().location(),areaWeather)!=Climate.WEATHER_HAIL))
						continue;
					R=S.mob().location();
					if((R!=null)&&(!choices.contains(R)))
						choices.add(R);
				}
				if(choices.size()>0)
				{
					R=choices.get(CMLib.dice().roll(1,choices.size(),-1));
					final MOB M=R.fetchRandomInhabitant();
					final Ability A2=CMClass.getAbility("Chant_SummonHail");
					if((A2!=null)
					&&(roomWeather(host,R,areaWeather)==Climate.WEATHER_HAIL))
					{
						A2.setMiscText("RENDER MUNDANE");
						A2.invoke(M,M,true,M.phyStats().level());
					}
				}
			}
		}
		if((areaWeather==Climate.WEATHER_DROUGHT)
		&&(CMLib.dice().rollPercentage()<droughtFireChance))
		{
			Room R=CMLib.map().roomLocation((Environmental)ticking);
			if((R==null)&&(ticking instanceof Area))
				R=((Area)ticking).getRandomProperRoom();
			if((R!=null)
			&&((R.domainType()&Room.INDOORS)==0)
			&&(R.domainType()!=Room.DOMAIN_OUTDOORS_SWAMP)
			&&(!CMLib.flags().isWateryRoom(R))
			&&(!CMath.bset(R.getClimateType(), Places.CLIMASK_VOID))
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
			if((areaWeather==Climate.WEATHER_WINDY)
			||(areaWeather==Climate.WEATHER_BLIZZARD)
			||(areaWeather==Climate.WEATHER_DUSTSTORM))
			{
				final List<Room> choices=new ArrayList<Room>(CMLib.sessions().numSessions());
				Room R=null;
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					final MOB M=S.mob();
					R=(M==null)?null:M.location();
					if((R==null)
					||(!isInHere(host,M))
					||(M.isMonster())
					||((roomWeather(host,R,areaWeather)!=Climate.WEATHER_WINDY)
						&&(roomWeather(host,R,areaWeather)!=Climate.WEATHER_BLIZZARD)
						&&(roomWeather(host,R,areaWeather)!=Climate.WEATHER_DUSTSTORM)))
						continue;
					if(!choices.contains(R))
						choices.add(R);
				}
				if(choices.size()>0)
				{
					R=choices.get(CMLib.dice().roll(1,choices.size(),-1));
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
				final MOB M=S.mob();
				final Room R=(M==null)?null:M.location();
				if((R==null)
				||(!isInHere(host,M))
				||(inATent(M))
				||(M.isMonster()))
					continue;
				int rustChance=0;
				switch(roomWeather(host,R,areaWeather))
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

				if(M.riding()!=null)
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
					final int weatherType=roomWeather(host,R,areaWeather);
					final List<Item> rustThese=new ArrayList<Item>();
					for(int i=0;i<M.numItems();i++)
					{
						final Item I=M.getItem(i);
						if(I==null)
							continue;
						if((!I.amWearingAt(Wearable.IN_INVENTORY))
						&&(I.container()==null))
						{
							if((((I.material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL))
							&&(I.subjectToWearAndTear())
							&&((CMLib.dice().rollPercentage()>I.phyStats().ability()*25)))
								rustThese.add(I);
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
						final Item I=rustThese.get(i);
						CMLib.combat().postItemDamage(M, I, null, 1, CMMsg.TYP_WATER, (weatherType!=0)?"<T-NAME> rusts.":"<T-NAME> rusts in the water.");
					}
				}
			}
		}
		if(ticking instanceof Room)
			lastWeather=roomWeather(host,(Room)ticking,areaWeather);
		else
			lastWeather=areaWeather;
		return true;
	}
}
