package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Common.interfaces.PhyStats;

/*
   Copyright 2013-2018 Bo Zimmerman

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
*
* A place where people might be, can be either abstract (like an area),
* or concrete (like a Room)
* @author Bo Zimmerman
*
*/
public interface Places extends PhysicalAgent
{
	/** a constant code for {@link Places#getAtmosphereCode()} that denotes that the atmo is inherited from a parent */
	public final static int ATMOSPHERE_INHERIT = -1;

	/**
	 * Returns the resource (or -1) that represents the atmosphere of this area.
	 * Since most rooms inherit their atmosphere from the area, this is important.
	 * Return -1 to have this area inherit its atmosphere from parents (which
	 * would ultimately go back to RESOURCE_AIR)
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial#MATERIAL_GAS
	 * @return the RawMaterial resource, or -1
	 */
	public int getAtmosphereCode();

	/**
	 * Returns the resource that represents the atmosphere of this area.
	 * Since most rooms inherit their atmosphere from the area, this is important.
	 * Could have this area inherit its atmosphere from parents (which
	 * would ultimately go back to RESOURCE_AIR)
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial#MATERIAL_GAS
	 * @return the RawMaterial resource
	 */
	public int getAtmosphere();

	/**
	 * Sets the resource (or -1) that represents the atmosphere of this area.
	 * Since most rooms inherit their atmosphere from the area, this is important.
	 * Return -1 to have this area inherit its atmosphere from parents (which
	 * would ultimately go back to RESOURCE_AIR)
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial#MATERIAL_GAS
	 * @param resourceCode the RawMaterial resource to use
	 */
	public void setAtmosphere(int resourceCode);

	/**
	 * Returns a bitmap of climate flags for this area which will be used to influence
	 * the weather for the area in addition to season and other factors.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Places#CLIMASK_COLD
	 * @return a CLIMASK bitmap
	 */
	public int getClimateTypeCode();
	/**
	 * Returns a bitmap of climate flags for this area which will be used to influence
	 * the weather for the area in addition to season and other factors.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Places#CLIMASK_COLD
	 * @param newClimateType a CLIMASK bitmap
	 */
	public void setClimateType(int newClimateType);

	/**
	 * Returns a bitmap of the climate for this place.  If the climate is CLIMASK_INHERIT,
	 * then it will look to parent objects, such as areas, and parent areas, until it
	 * eventually finds a non-inherit, or returns CLIMASK_NORMAL;
	 * @see com.planet_ink.coffee_mud.core.interfaces.Places#CLIMASK_COLD
	 * @return a derived climate
	 */
	public int getClimateType();

	/**	Bitmap climate flag meaning that the area has inherited weather.  @see com.planet_ink.coffee_mud.core.interfaces.Places#climateType() */
	public final static int CLIMASK_INHERIT = -1;
	/**	Bitmap climate flag meaning that the area has normal weather.  @see com.planet_ink.coffee_mud.core.interfaces.Places#climateType() */
	public final static int CLIMASK_NORMAL=0;
	/**	Bitmap climate flag meaning that the area has wet weather.  @see com.planet_ink.coffee_mud.core.interfaces.Places#climateType() */
	public final static int CLIMASK_WET=1;
	/**	Bitmap climate flag meaning that the area has cold weather.  @see com.planet_ink.coffee_mud.core.interfaces.Places#climateType() */
	public final static int CLIMASK_COLD=2;
	/**	Bitmap climate flag meaning that the area has windy weather.  @see com.planet_ink.coffee_mud.core.interfaces.Places#climateType() */
	public final static int CLIMASK_WINDY=4;
	/**	Bitmap climate flag meaning that the area has hot weather.  @see com.planet_ink.coffee_mud.core.interfaces.Places#climateType() */
	public final static int CLIMASK_HOT=8;
	/**	Bitmap climate flag meaning that the area has dry weather.  @see com.planet_ink.coffee_mud.core.interfaces.Places#climateType() */
	public final static int CLIMASK_DRY=16;
	/**	Indexed description of the CLIMASK_ bitmap constants in all possible combinations.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Places#CLIMASK_NORMAL
	 */
	public final static String[] CLIMATE_DESCS={"NORMAL","WET","COLD","WINDY","HOT","DRY"};
	/**	Number of CLIMASK_ constants.  @see com.planet_ink.coffee_mud.core.interfaces.Places#climateType() */
	public final static int NUM_CLIMATES=6;
	/**	Bitmap climate flag meaning that the area has all weather modifiers.  @see com.planet_ink.coffee_mud.core.interfaces.Places#climateType() */
	public final static int ALL_CLIMATE_MASK=31;
}
