package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Tarantella extends Dance
{
	public String ID() { return "Dance_Tarantella"; }
	public String name(){ return "Tarantella";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	private int ticks=1;
	protected String danceOf(){return name()+" Dance";}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_POISON,affectedStats.getStat(CharStats.SAVE_POISON)+(CMAble.qualifyingClassLevel(invoker(),this)*2));
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if((++ticks)>=15)
		{
			Vector offenders=null;
			for(int a=0;a<mob.numEffects();a++)
			{
				Ability A=mob.fetchEffect(a);
				if((A!=null)&&(A.classificationCode()==Ability.POISON))
				{
					if(offenders==null) offenders=new Vector();
					offenders.addElement(A);
				}
			}
			if(offenders!=null)
				for(int a=0;a<offenders.size();a++)
					((Ability)offenders.elementAt(a)).unInvoke();
		}

		return true;
	}

}
