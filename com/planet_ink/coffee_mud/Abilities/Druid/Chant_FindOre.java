package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Chant_FindOre extends Chant_FindPlant
{
	public String ID() { return "Chant_FindOre"; }
	public String name(){ return "Find Ore";}
	public String displayText(){return "(Finding "+lookingFor+")";}
	public long flags(){return Ability.FLAG_TRACKING;}
	protected String lookingFor="ore";

	private int[] myMats={EnvResource.MATERIAL_ROCK,
						  EnvResource.MATERIAL_METAL};
	protected int[] okMaterials(){	return myMats;}
	protected int[] okResources(){	return null;}
}
