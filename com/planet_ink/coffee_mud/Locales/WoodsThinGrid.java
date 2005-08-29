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
public class WoodsThinGrid extends StdThinGrid
{
	public String ID(){return "WoodsThinGrid";}
	public WoodsThinGrid()
	{
		super();
		baseEnvStats.setWeight(3);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_WOODS;
		domainCondition=Room.CONDITION_NORMAL;
	}

	public String getChildLocaleID(){return "Woods";}
	public Vector resourceChoices(){return Woods.roomResources;}
	public Environmental newInstance()
	{
	    if(!CMSecurity.isDisabled("THINGRIDS"))
	        return super.newInstance();
        return new WoodsGrid().newInstance();
	}
}
