package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Waltz extends Dance
{
	public String ID() { return "Dance_Waltz"; }
	public String name(){ return "Waltz";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	private int[] statadd=null;

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		if(statadd==null)
		{
			statadd=new int[CharStats.NUM_BASE_STATS];
			int classLevel=CMAble.qualifyingClassLevel(invoker(),this);
			classLevel=(classLevel+1)/9;
			classLevel++;

			for(int i=0;i<classLevel;i++)
				statadd[Dice.roll(1,CharStats.NUM_BASE_STATS,-1)]+=3;
		}
		for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			affectedStats.setStat(i,affectedStats.getStat(i)+statadd[i]);
	}

}
