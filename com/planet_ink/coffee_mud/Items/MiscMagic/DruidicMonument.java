package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;


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
public class DruidicMonument extends StdItem implements MiscMagic
{
	public String ID(){	return "DruidicMonument";}
	public DruidicMonument()
	{
		super();

		setName("the druidic stones");
		setDisplayText("druidic stones are arrayed here.");
		setDescription("These large mysterious monuments have a power and purpose only the druid understands.");
		secretIdentity="DRUIDIC STONES";
		baseEnvStats().setLevel(1);
		setMaterial(EnvResource.RESOURCE_STONE);
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMNOTGET);
		baseEnvStats().setWeight(1000);
		baseGoldValue=0;
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
	}


}
