package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
		new Integer(EnvResource.RESOURCE_STONE),
		new Integer(EnvResource.RESOURCE_IRON),
		new Integer(EnvResource.RESOURCE_LEAD),
		new Integer(EnvResource.RESOURCE_SILVER),
		new Integer(EnvResource.RESOURCE_COPPER),
		new Integer(EnvResource.RESOURCE_TIN),
		new Integer(EnvResource.RESOURCE_AMETHYST),
		new Integer(EnvResource.RESOURCE_GARNET),
		new Integer(EnvResource.RESOURCE_AMBER),
		new Integer(EnvResource.RESOURCE_HERBS),
		new Integer(EnvResource.RESOURCE_OPAL),
		new Integer(EnvResource.RESOURCE_TOPAZ),
		new Integer(EnvResource.RESOURCE_BASALT),
		new Integer(EnvResource.RESOURCE_SHALE),
		new Integer(EnvResource.RESOURCE_PUMICE),
		new Integer(EnvResource.RESOURCE_SANDSTONE),
		new Integer(EnvResource.RESOURCE_SOAPSTONE),
		new Integer(EnvResource.RESOURCE_AQUAMARINE),
		new Integer(EnvResource.RESOURCE_CRYSOBERYL),
		new Integer(EnvResource.RESOURCE_ONYX),
		new Integer(EnvResource.RESOURCE_TURQUIOSE),
		new Integer(EnvResource.RESOURCE_DIAMOND),
		new Integer(EnvResource.RESOURCE_CRYSTAL),
		new Integer(EnvResource.RESOURCE_QUARTZ),
		new Integer(EnvResource.RESOURCE_PLATINUM)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Mountains.roomResources;}
}
