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
public class Beholder extends StdRace
{
	public String ID(){	return "Beholder"; }
	public String name(){ return "Beholder"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Unique";}
    private String[]racialAbilityNames={"Spell_Sleep","Spell_FloatingDisc","Spell_Fear","Spell_Slow","Spell_Charm","Prayer_CauseCritical","Spell_DispelMagic","Spell_FleshStone","Prayer_DeathFinger","Spell_Disintegrate"};
	private int[]racialAbilityLevels={1,1,1,5,10,10,15,20,30,30};
	private int[]racialAbilityProfficiencies={50,50,50,50,50,50,100,50,50,50};
	private boolean[]racialAbilityQuals={false,false,false,false,false,false,false,false,false,false};
	public String[] racialAbilityNames(){return racialAbilityNames;}
	public int[] racialAbilityLevels(){return racialAbilityLevels;}
	public int[] racialAbilityProfficiencies(){return racialAbilityProfficiencies;}
	public boolean[] racialAbilityQuals(){return racialAbilityQuals;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,10,-1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,5,20,110,325,500,850,950,1050};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.INTELLIGENCE,25);
		affectableStats.setStat(CharStats.SAVE_MAGIC,75);
		affectableStats.setStat(CharStats.SAVE_MIND,100);
	}

	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int x=0;x<10;x++)
					resources.addElement(makeResource
					("a "+name().toLowerCase()+" eye",EnvResource.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}
