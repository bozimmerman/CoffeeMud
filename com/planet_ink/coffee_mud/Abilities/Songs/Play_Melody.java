package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Melody extends Play
{
	public String ID() { return "Play_Melody"; }
	public String name(){ return "Melody";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Play_Melody();}
	protected String songOf(){return "a "+name();}

	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(invoker()!=null)
			stats.setStat(CharStats.SAVE_MIND,stats.getStat(CharStats.SAVE_MIND)-(invoker().charStats().getStat(CharStats.CHARISMA)+(CMAble.qualifyingClassLevel(mob,this)*2)));
	}
}
	
