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
public class SewerRoom extends StdRoom
{
	public String ID(){return "SewerRoom";}
	public SewerRoom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="the sewers";
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_DARK);
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_CAVE;
		domainCondition=Room.CONDITION_WET;
	}

}
