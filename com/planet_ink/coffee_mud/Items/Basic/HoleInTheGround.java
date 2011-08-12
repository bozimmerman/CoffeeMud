package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


/* 
   Copyright 2000-2011 Bo Zimmerman

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
public class HoleInTheGround extends StdContainer
{
	public String ID(){	return "HoleInTheGround";}
	public HoleInTheGround()
	{
		super();
		setName("a hole in the ground");
		setDisplayText("a hole in the ground");
		setDescription("Looks like someone has dug hole here.  Perhaps something is in it?");
		capacity=1000;
		baseGoldValue=0;
		basePhyStats().setWeight(Integer.MAX_VALUE/2);
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMNOTGET|PhyStats.SENSE_ITEMNOWISH|PhyStats.SENSE_ITEMNORUIN);
		setMaterial(RawMaterial.RESOURCE_DUST);
		recoverPhyStats();
	}



}
