package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Ogre extends Humanoid
{
	public String ID(){	return "Ogre"; }
	public String name(){ return "Ogre"; }
	protected int shortestMale(){return 74;}
	protected int shortestFemale(){return 69;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 290;}
	protected int weightVariance(){return 90;}
	protected long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Giant-kin";}
	
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
