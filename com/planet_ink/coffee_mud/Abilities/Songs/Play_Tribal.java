package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Play_Tribal extends Play
{
	public String ID() { return "Play_Tribal"; }
	public String name(){ return "Tribal";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Play_Tribal();}
	protected String songOf(){return name()+" Music";}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		Room R=CoffeeUtensils.roomLocation(affected);
		if(R!=null)
		for(int m=0;m<R.numInhabitants();m++)
		{
			MOB mob=(MOB)R.fetchInhabitant(m);
			if(mob!=null)
			for(int i=0;i<mob.numEffects();i++)
			{
				Ability A=mob.fetchEffect(i);
				if((A!=null)
				&&(A instanceof StdAbility)
				&&(A.quality()!=Ability.MALICIOUS)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.CHANT)
				&&(((StdAbility)A).getTickDownRemaining()==1))
					((StdAbility)A).setTickDownRemaining(2);
			}
		}
		return true;
	}
}