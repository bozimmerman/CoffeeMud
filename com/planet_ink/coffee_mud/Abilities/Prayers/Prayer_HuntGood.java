package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Prayer_HuntGood extends Prayer_HuntEvil
{
	public String ID() { return "Prayer_HuntGood"; }
	public String name(){ return "Hunt Good";}
	public long flags(){return Ability.FLAG_UNHOLY|Ability.FLAG_TRACKING;}
	public String displayText(){return "(Hunting Good)";}
	protected String word="good";
	public Environmental newInstance(){	return new Prayer_HuntEvil();}
	
	protected MOB gameHere(Room room)
	{
		if(room==null) return null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob=room.fetchInhabitant(i);
			if(Sense.isGood(mob))
				return mob;
		}
		return null;
	}

}
