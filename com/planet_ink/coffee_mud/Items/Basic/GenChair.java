package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class GenChair extends GenRideable
{
	public String ID(){	return "GenChair";}
	protected String	readableText="";
	public GenChair()
	{
		super();
		setName("a generic chair");
		baseEnvStats.setWeight(150);
		setDisplayText("a generic chair is here.");
		setDescription("");
		material=EnvResource.RESOURCE_OAK;
		baseGoldValue=5;
		baseEnvStats().setLevel(1);
		setRiderCapacity(1);
		setRideBasis(Rideable.RIDEABLE_SIT);
		recoverEnvStats();
	}
}
