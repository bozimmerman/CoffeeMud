package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class Parchment extends GenReadable
{
	public String ID(){	return "Parchment";}
	public Parchment()
	{
		super();
		setName("a piece of parchment");
		setDisplayText("a piece of parchment here.");
		setDescription("looks kinda like a piece of paper");
		baseEnvStats().setWeight(1);
		recoverEnvStats();
		setMaterial(EnvResource.RESOURCE_PAPER);
	}



}
