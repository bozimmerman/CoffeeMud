package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class Bed extends GenRideable
{
	public String ID(){	return "Bed";}
	protected String	readableText="";
	public Bed()
	{
		super();
		setName("a bed");
		baseEnvStats.setWeight(150);
		setDisplayText("a bed is here.");
		setDescription("Looks like a nice comfortable bed");
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setMaterial(EnvResource.RESOURCE_COTTON);
		setRideBasis(Rideable.RIDEABLE_SLEEP);
		setRiderCapacity(2);
		recoverEnvStats();
	}

}
