package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.interfaces.*;
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
public class UnderSaltWater extends UnderWater
{
	public String ID(){return "UnderSaltWater";}
	public UnderSaltWater()
	{
		super();
	}


	public int liquidType(){return EnvResource.RESOURCE_SALTWATER;}
	public static final Integer[] resourceList={
		new Integer(EnvResource.RESOURCE_SEAWEED),
		new Integer(EnvResource.RESOURCE_FISH),
		new Integer(EnvResource.RESOURCE_TUNA),
		new Integer(EnvResource.RESOURCE_SHRIMP),
		new Integer(EnvResource.RESOURCE_SAND),
		new Integer(EnvResource.RESOURCE_CLAY),
		new Integer(EnvResource.RESOURCE_PEARL),
		new Integer(EnvResource.RESOURCE_LIMESTONE)};
	public static final Vector roomResources=new Vector(Arrays.asList(resourceList));
	public Vector resourceChoices(){return UnderWater.roomResources;}
}
