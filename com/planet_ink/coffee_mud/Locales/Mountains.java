package com.planet_ink.coffee_mud.Locales;
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
   Copyright 2000-2006 Bo Zimmerman

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
public class Mountains extends StdRoom
{
	public String ID(){return "Mountains";}
	public Mountains()
	{
		super();
		name="the mountain";
		baseEnvStats.setWeight(5);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_MOUNTAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}

	public static final Integer[] resourceList={
		new Integer(RawMaterial.RESOURCE_STONE),
		new Integer(RawMaterial.RESOURCE_IRON),
		new Integer(RawMaterial.RESOURCE_LEAD),
		new Integer(RawMaterial.RESOURCE_SILVER),
		new Integer(RawMaterial.RESOURCE_COPPER),
		new Integer(RawMaterial.RESOURCE_TIN),
		new Integer(RawMaterial.RESOURCE_AMETHYST),
		new Integer(RawMaterial.RESOURCE_GARNET),
		new Integer(RawMaterial.RESOURCE_AMBER),
		new Integer(RawMaterial.RESOURCE_HERBS),
		new Integer(RawMaterial.RESOURCE_OPAL),
		new Integer(RawMaterial.RESOURCE_TOPAZ),
		new Integer(RawMaterial.RESOURCE_BASALT),
		new Integer(RawMaterial.RESOURCE_SHALE),
		new Integer(RawMaterial.RESOURCE_PUMICE),
		new Integer(RawMaterial.RESOURCE_SANDSTONE),
		new Integer(RawMaterial.RESOURCE_SOAPSTONE),
		new Integer(RawMaterial.RESOURCE_AQUAMARINE),
		new Integer(RawMaterial.RESOURCE_CRYSOBERYL),
		new Integer(RawMaterial.RESOURCE_ONYX),
		new Integer(RawMaterial.RESOURCE_TURQUIOSE),
		new Integer(RawMaterial.RESOURCE_DIAMOND),
		new Integer(RawMaterial.RESOURCE_CRYSTAL),
		new Integer(RawMaterial.RESOURCE_QUARTZ),
		new Integer(RawMaterial.RESOURCE_PLATINUM)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Mountains.roomResources;}
}
