package com.planet_ink.coffee_mud.Races;

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
public class Manticore extends GreatCat
{
	public String ID(){	return "Manticore"; }
	public String name(){ return "Manticore"; }
	public int shortestMale(){return 69;}
	public int shortestFemale(){return 69;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 120;}
	public int weightVariance(){return 80;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Feline";}
	private String[]racialAbilityNames={"WingFlying"};
	private int[]racialAbilityLevels={1};
	private int[]racialAbilityProfficiencies={100};
	private boolean[]racialAbilityQuals={false};
	protected String[] racialAbilityNames(){return racialAbilityNames;}
	protected int[] racialAbilityLevels(){return racialAbilityLevels;}
	protected int[] racialAbilityProfficiencies(){return racialAbilityProfficiencies;}
	protected boolean[] racialAbilityQuals(){return racialAbilityQuals;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,2 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!Sense.isSleeping(affected))
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
	}

	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of "+name().toLowerCase()+" horns",EnvResource.RESOURCE_BONE));
				for(int i=0;i<5;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" hide",EnvResource.RESOURCE_LEATHER));
				for(int i=0;i<2;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_BEEF));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
