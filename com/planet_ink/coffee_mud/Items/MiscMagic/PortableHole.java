package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
public class PortableHole extends BagOfHolding implements MiscMagic
{
	public String ID(){	return "PortableHole";}
	public PortableHole()
	{
		super();

		setName("a small disk");
		setDisplayText("a small black disk can be found up here.");
		setDescription("It looks like a small disk.");
		secretIdentity="A Portable Hole";
		baseEnvStats().setLevel(1);
		capacity=200 * baseEnvStats().level();

		baseGoldValue=15000;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();



	}
}
