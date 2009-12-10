package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Ogre extends Humanoid
{
	public String ID(){	return "Ogre"; }
	public String name(){ return "Ogre"; }
	public int shortestMale(){return 74;}
	public int shortestFemale(){return 69;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 290;}
	public int weightVariance(){return 90;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Giant-kin";}
	private String[]culturalAbilityNames={"Gigantic","Orcish","Butchering"};
	private int[]culturalAbilityProficiencies={100,50,75};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}

    private int[] agingChart={0,2,6,30,69,104,136,142,148};
    public int[] getAgingChart(){return agingChart;}
    
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
    public int availabilityCode(){return Area.THEME_FANTASY;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
        affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)-10);
        affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+10);
        if(affectedMOB.isMonster())
        {
            affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-3);
            affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)-3);
            affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)-4);
            affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)+5);
        }
        else
        {
            affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)-2);
            affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectableStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)-2);
            affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)+4);
            affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectableStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)+4);
            affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-1);
            affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)-1);
            affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)-1);
            affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectableStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)-1);
        }
	}
    
    public Vector outfit(MOB myChar)
    {
        if(outfitChoices==null)
        {
            outfitChoices=new Vector();
            // Have to, since it requires use of special constructor
            Armor s1=CMClass.getArmor("GenShirt");
            s1.setName("a large patchy tunic");
            s1.setDisplayText("a large patchy tunic is crumpled up here.");
            s1.setDescription("It is a large ragged patchy nasty sweat-stained tunic.");
            s1.text();
            outfitChoices.addElement(s1);
            Armor p1=CMClass.getArmor("GenPants");
            p1.setName("some torn leggings");
            p1.setDisplayText("some torn leggings lie here.");
            p1.setDescription("They appear made for a large person with poor taste in clothing.");
            p1.text();
            outfitChoices.addElement(p1);
            Armor s3=CMClass.getArmor("GenBelt");
            outfitChoices.addElement(s3);
        }
        return outfitChoices;
    }
}
