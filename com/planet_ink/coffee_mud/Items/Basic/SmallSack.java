package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
public class SmallSack extends StdContainer
{
	public String ID(){	return "SmallSack";}
	public SmallSack()
	{
		super();
		setName("a small sack");
		setDisplayText("a small sack is crumpled up here.");
		setDescription("A nice berlap sack to put your things in.");
		capacity=25;
		material=EnvResource.RESOURCE_COTTON;
		baseGoldValue=1;
		recoverEnvStats();
	}



}
