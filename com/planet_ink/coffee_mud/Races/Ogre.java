package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
	private String[]culturalAbilityNames={"Gigantic"};
	private int[]culturalAbilityProfficiencies={100};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProfficiencies(){return culturalAbilityProfficiencies;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)-10);
		affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)-5);
		affectableStats.setStat(CharStats.INTELLIGENCE,affectableStats.getStat(CharStats.INTELLIGENCE)-5);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)+10);
	}
}
