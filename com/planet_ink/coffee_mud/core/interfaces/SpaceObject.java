package com.planet_ink.coffee_mud.core.interfaces;
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
/**
 * This interface is still in development.  It will some day represent an object in space
 * @author Bo Zimmerman
 */
public interface SpaceObject extends Environmental, BoundedObject
{
	/**
	 * The current absolute coordinates of  the object
	 * @return 3 dimensional array of the coordinates
	 */
	public long[] coordinates();

	/**
	 * Sets the current absolute coordinates of the object
	 * @param coords 3  dimensional array of the coordinates in space
	 */
	public void setCoords(long[] coords);

	/**
	 * The current radius of  the object
	 * @return the radius, in decameters
	 */
	public long radius();

	/**
	 * Set the current radius of  the object
	 * @param radius the current radius of  the object
	 */
	public void setRadius(long radius);

	/**
	 * The direction of travel of this object in radians.
	 * @return 2 dimensional array for the direction of movement
	 */
	public double[] direction();

	/**
	 * Sets the direction of travel of this object in radians.
	 * direction[0] less than or equal to PI
	 * direction[1] less than or equal to 2PI
	 * @param dir 2 dimensional array for the direction of movement
	 */
	public void setDirection(double[] dir);

	/**
	 * The speed of the object through space
	 * @return the speed
	 */
	public double speed();

	/**
	 * Sets the speed of the object through space
	 * @param v the speed
	 */
	public void setSpeed(double v);

	/**
	 * If this object is targeting another space object as a destination, this will return it
	 * @return the target destination
	 */
	public SpaceObject knownTarget();

	/**
	 * If this object is targeting another space object as a destination, this will set it
	 * @param O the target destination
	 */
	public void setKnownTarget(SpaceObject O);

	/**
	 * The source object from which this space object is travelling from
	 * @return the source of  this object
	 */
	public SpaceObject knownSource();

	/**
	 * Sets the source object from which this space object is travelling from
	 * @param O the source of  this object
	 */
	public void setKnownSource(SpaceObject O);

	/**
	 * Returns the mass of this object, derived from its
	 * radius and type, or perhaps from other things. Either way, its derived.
	 * The mass of space ships is what it is, but the mass of planets will
	 * be off by about 15 zeroes, as there just aren't enough bits.
	 * @return the mass of this object
	 */
	public long getMass();

	/**
	 * Some distance constants.  Not really proper enumerations, but
	 * it's a nice way to create custom objects cleanly.
	 * @author Bo Zimmerman
	 */
	public static enum Distance
	{
		Decameter("dm",1L),
		Kilometer("km",100L),
		MoonRadius("mr",173740L),
		PlanetRadius("pr",639875L),
		SaturnRadius("sr",6026800L),
		AstroUnit("au",14959787100L),
		StarGRadius("gr",69550000L),
		StarDRadius("dr",959812L),
		StarBRadius("br",695500000L),
		SolarSystemRadius("yr",590638000000L),
		SolarSystemDiameter("yd",590638000000L*2L),
		DistanceBetweenStars("sd",946073047258080L*4L),
		SpaceCombatPointBlank("pb",20000L),
		LightYear("lY",946073047258080L),
		LightMonth("lM",946073047258080L/12L),
		LightDay("lD",946073047258080L/365L),
		LightHour("lh",946073047258080L/(365L*24L)),
		LightMinute("lm",946073047258080L/(365L*24L)),
		LightSecond("ls",946073047258080L/(365L*24L*60L)),
		Parsec("p",3085677580000000L),
		GalaxyRadius("xr",946073047258080L*1000L),
		;
		public final long dm;
		public final String abbr;
		private static String abbrList="";
		private static String fullList="";
		
		private Distance(String abbr, long distance)
		{
			this.abbr=abbr;
			this.dm=distance;
		}

		public static String getAbbrList()
		{
			if(abbrList.length()==0)
			{
				for(Distance d : Distance.values())
					abbrList+=d.abbr+", ";
				abbrList=abbrList.substring(0,abbrList.length()-2);
			}
			return abbrList;
		}

		public static String getFullList()
		{
			if(fullList.length()==0)
			{
				for(Distance d : Distance.values())
					fullList+=d.name()+"("+d.abbr+"), ";
				fullList=fullList.substring(0,fullList.length()-2);
			}
			return fullList;
		}
	}
	
	/**
	 * Ordered array of distance enums appropriate for telling distances in space.
	 */
	public static final Distance[] DISTANCES = new Distance[]
	{
		Distance.Parsec,
		Distance.LightYear,
		Distance.LightMonth,
		Distance.LightDay,
		Distance.LightHour,
		Distance.LightMinute,
		Distance.LightSecond,
		Distance.AstroUnit,
		Distance.Kilometer,
		Distance.Decameter,
	};

	/** constant useful for multiplying by radius -- this one to find the orbiting radius*/
	public static final double MULTIPLIER_ORBITING_RADIUS_MIN=1.029;
	/** constant useful for multiplying by radius -- this one to find the orbiting radius*/
	public static final double MULTIPLIER_ORBITING_RADIUS_MAX=1.031;
	/** multiplying by radius -- this one to find the gravitational pull radius*/
	public static final double MULTIPLIER_GRAVITY_EFFECT_RADIUS=MULTIPLIER_ORBITING_RADIUS_MAX;

	/** multiplier by radius to get planets mass -- only off by 15 zeroes or so 9333072865794100410 is the actual number*/
	public static final long MULTIPLIER_PLANET_MASS=933L;
	/** multiplier by radius to get stars mass -- only off by 15 zeroes or so 19890000000000000000000000 is the actual number*/
	public static final long MULTIPLIER_STAR_MASS=1989000L;

	/** accelleration at which you are happy, in decameters/s */
	public static final long ACCELLERATION_G=1;
	/** accelleration at which you pass out, in decameters/s */
	public static final long ACCELLERATION_PASSOUT=ACCELLERATION_G*5;
	/** accelleration in atmosphere, in decameters/s */
	public static final long ACCELLERATION_TYPICALROCKET=ACCELLERATION_G*2;
	/** accelleration in space, in decameters/s */
	public static final long ACCELLERATION_TYPICALSPACEROCKET=ACCELLERATION_G*3;
	/** accelleration at which you are unconscious, in decameters/s */
	public static final long ACCELLERATION_UNCONSCIOUSNESS=ACCELLERATION_G*15;
	/** accelleration at which you are severely damaged (40" fall), in decameters/s */
	public static final long ACCELLERATION_DAMAGED=ACCELLERATION_G*30;
	/** accelleration at which you are devestated, in decameters/s */
	public static final long ACCELLERATION_INSTANTDEATH=ACCELLERATION_G*60;

	// thrust=mass * accelleration
	// accelleration = thrust/mass

	// engine efficiency=specific impulse
	// add max velocity to engines (specific impulse) -- so current velocity affects how much accelleration you'll get!

	// graviton drives -- you need lots of gravity for this to work

	// inertia drive -- modify the mass in the t=ma equation.  a=f(m/i)

	// outer mold line coefficient
	/** drag coefficient of a streamlined body */
	public static final double ATMOSPHERIC_DRAG_STREAMLINE=0.05;
	/** drag coefficient of a brick body */
	public static final double ATMOSPHERIC_DRAG_BRICK=0.30;

	//force equation in air= A=((thrust / (m * inertial dampener <= 1 ))-1)*(1- OML))
	//force equation in space= A=(thrust / (m * inertial dampener <= 1 )

	/** velocity constant for the speed of light, numbers are in dm/s */
	public static final long VELOCITY_LIGHT=29979245;
	/** velocity constant for the speed of sublight */
	public static final long VELOCITY_SUBLIGHT=26981325;
	/** velocity constant for the speed of sound */
	public static final long VELOCITY_SOUND=34;
	/** velocity constant for the speed of orbiting */
	public static final long VELOCITY_ORBITING=770;
	///** velocity constant for the speed required to escape 1g */
	public static final long VELOCITY_ESCAPE=680;
	/** velocity constant for the speed warp 1 */
	public static final long VELOCITY_WARP1=VELOCITY_LIGHT;
	/** velocity constant for the speed warp 2 */
	public static final long VELOCITY_WARP2=VELOCITY_LIGHT*4;
	/** velocity constant for the speed warp 3 */
	public static final long VELOCITY_WARP3=VELOCITY_LIGHT*9;
	/** velocity constant for the speed warp 4 */
	public static final long VELOCITY_WARP4=VELOCITY_LIGHT*16;
	/** velocity constant for the speed warp 5 */
	public static final long VELOCITY_WARP5=VELOCITY_LIGHT*25;
	/** velocity constant for the speed warp 6 */
	public static final long VELOCITY_WARP6=VELOCITY_LIGHT*36;
	/** velocity constant for the speed warp 7 */
	public static final long VELOCITY_WARP7=VELOCITY_LIGHT*49;
	/** velocity constant for the speed warp 8 */
	public static final long VELOCITY_WARP8=VELOCITY_LIGHT*64;
	/** velocity constant for the speed warp 9 */
	public static final long VELOCITY_WARP9=VELOCITY_LIGHT*81;
	/** velocity constant for the speed warp 10 */
	public static final long VELOCITY_WARP10=VELOCITY_LIGHT*100;
	/** velocity constant for the speed transwarp 1 */
	public static final long VELOCITY_TRANSWARP1=VELOCITY_LIGHT;
	/** velocity constant for the speed transwarp 2 */
	public static final long VELOCITY_TRANSWARP2=VELOCITY_LIGHT*8;
	/** velocity constant for the speed transwarp 3 */
	public static final long VELOCITY_TRANSWARP3=VELOCITY_LIGHT*27;
	/** velocity constant for the speed transwarp 4 */
	public static final long VELOCITY_TRANSWARP4=VELOCITY_LIGHT*64;
	/** velocity constant for the speed transwarp 5 */
	public static final long VELOCITY_TRANSWARP5=VELOCITY_LIGHT*125;
	/** velocity constant for the speed transwarp 6 */
	public static final long VELOCITY_TRANSWARP6=VELOCITY_LIGHT*216;
	/** velocity constant for the speed transwarp 7 */
	public static final long VELOCITY_TRANSWARP7=VELOCITY_LIGHT*343;
	/** velocity constant for the speed transwarp 8 */
	public static final long VELOCITY_TRANSWARP8=VELOCITY_LIGHT*512;
	/** velocity constant for the speed transwarp 9 */
	public static final long VELOCITY_TRANSWARP9=VELOCITY_LIGHT*729;
	/** velocity constant for the speed transwarp 10 */
	public static final long VELOCITY_TRANSWARP10=VELOCITY_LIGHT*1000; // btw, this means it would take 1 rl year to travel the gallaxy
}
