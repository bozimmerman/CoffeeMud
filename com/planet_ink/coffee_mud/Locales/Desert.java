package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class Desert extends StdRoom
{
	public String ID(){return "Desert";}
	public Desert()
	{
		super();
		name="the desert";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_DESERT;
		domainCondition=Room.CONDITION_HOT;
		baseThirst=4;
	}

	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_CACTUS),
		new Integer(EnvResource.RESOURCE_SAND),
		new Integer(EnvResource.RESOURCE_LAMPOIL),
		new Integer(EnvResource.RESOURCE_PEPPERS),
		new Integer(EnvResource.RESOURCE_SCALES),
		new Integer(EnvResource.RESOURCE_DATES)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Desert.roomResources;}
}
