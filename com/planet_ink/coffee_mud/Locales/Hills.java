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
public class Hills extends StdRoom
{
	public String ID(){return "Hills";}
	public Hills()
	{
		super();
		name="the hills";
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_HILLS;
		domainCondition=Room.CONDITION_NORMAL;
	}

	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_GRAPES),
		new Integer(EnvResource.RESOURCE_BERRIES),
		new Integer(EnvResource.RESOURCE_BLUEBERRIES),
		new Integer(EnvResource.RESOURCE_BLACKBERRIES),
		new Integer(EnvResource.RESOURCE_STRAWBERRIES),
		new Integer(EnvResource.RESOURCE_RASPBERRIES),
		new Integer(EnvResource.RESOURCE_BOYSENBERRIES),
		new Integer(EnvResource.RESOURCE_GREENS),
		new Integer(EnvResource.RESOURCE_OLIVES),
		new Integer(EnvResource.RESOURCE_RICE),
		new Integer(EnvResource.RESOURCE_LEATHER),
		new Integer(EnvResource.RESOURCE_FEATHERS),
		new Integer(EnvResource.RESOURCE_MESQUITE),
		new Integer(EnvResource.RESOURCE_EGGS),
		new Integer(EnvResource.RESOURCE_HERBS),
		new Integer(EnvResource.RESOURCE_POTATOES)
	};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Hills.roomResources;}
}
