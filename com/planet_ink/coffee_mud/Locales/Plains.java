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
public class Plains extends StdRoom
{
	public String ID(){return "Plains";}
	public Plains()
	{
		super();
		name="the grass";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_PLAINS;
		domainCondition=Room.CONDITION_NORMAL;
	}

	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_WHEAT),
		new Integer(EnvResource.RESOURCE_HOPS),
		new Integer(EnvResource.RESOURCE_BARLEY),
		new Integer(EnvResource.RESOURCE_CORN),
		new Integer(EnvResource.RESOURCE_RICE),
		new Integer(EnvResource.RESOURCE_SMURFBERRIES),
		new Integer(EnvResource.RESOURCE_GREENS),
		new Integer(EnvResource.RESOURCE_CARROTS),
		new Integer(EnvResource.RESOURCE_TOMATOES),
		new Integer(EnvResource.RESOURCE_ONIONS),
		new Integer(EnvResource.RESOURCE_GARLIC),
		new Integer(EnvResource.RESOURCE_FLINT),
		new Integer(EnvResource.RESOURCE_COTTON),
		new Integer(EnvResource.RESOURCE_MEAT),
		new Integer(EnvResource.RESOURCE_HERBS),
		new Integer(EnvResource.RESOURCE_EGGS),
		new Integer(EnvResource.RESOURCE_BEEF),
		new Integer(EnvResource.RESOURCE_HIDE),
		new Integer(EnvResource.RESOURCE_FUR),
		new Integer(EnvResource.RESOURCE_HONEY),
		new Integer(EnvResource.RESOURCE_FEATHERS),
		new Integer(EnvResource.RESOURCE_LEATHER),
		new Integer(EnvResource.RESOURCE_WOOL)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return Plains.roomResources;}
}
