package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Rhythm extends Play
{
	public String ID() { return "Play_Rhythm"; }
	public String name(){ return "Rhythm";}
	public int quality(){ return MALICIOUS;}

	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(invoker()!=null)
			stats.setStat(CharStats.SAVE_MAGIC,stats.getStat(CharStats.SAVE_MAGIC)-(invoker().charStats().getStat(CharStats.CHARISMA)+(invokerLevel()*2)));
	}
}

