package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.Sense;

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
public class Faerie extends SmallElfKin
{
	public String ID(){	return "Faerie"; }
	public String name(){ return "Faerie"; }
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,2 };
	public int[] bodyMask(){return parts;}
	private String[]racialAbilityNames={"WingFlying"};
	private int[]racialAbilityLevels={1};
	private int[]racialAbilityProfficiencies={100};
	private boolean[]racialAbilityQuals={false};
	public String[] racialAbilityNames(){return racialAbilityNames;}
	public int[] racialAbilityLevels(){return racialAbilityLevels;}
	public int[] racialAbilityProfficiencies(){return racialAbilityProfficiencies;}
	public boolean[] racialAbilityQuals(){return racialAbilityQuals;}
	private String[]culturalAbilityNames={"Fey","Foraging"};
	private int[]culturalAbilityProfficiencies={100,50};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProfficiencies(){return culturalAbilityProfficiencies;}
	
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!Sense.isSleeping(affected))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}
}
