package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Demon extends Unique
{
	public String ID(){	return "Demon"; }
	public String name(){ return "Demon"; }
	public int shortestMale(){return 64;}
	public int shortestFemale(){return 60;}
	public int heightVariance(){return 12;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	public boolean playerSelectable(){return false;}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,affectableStats.getStat(CharStats.STRENGTH)+5);
		affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_FIRE)+50);
	}
}
