package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Background extends Play
{
	public String ID() { return "Play_Background"; }
	public String name(){ return "Background";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Play_Background();}
	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(invoker()!=null)
		{
			int cha=invoker().charStats().getStat(CharStats.CHARISMA)/2;
			int lvl=invokerLevel()/3;
			stats.setStat(CharStats.SAVE_ACID,stats.getStat(CharStats.SAVE_ACID)+lvl+cha);
			stats.setStat(CharStats.SAVE_COLD,stats.getStat(CharStats.SAVE_COLD)+lvl+cha);
			stats.setStat(CharStats.SAVE_DISEASE,stats.getStat(CharStats.SAVE_DISEASE)+lvl+cha);
			stats.setStat(CharStats.SAVE_ELECTRIC,stats.getStat(CharStats.SAVE_ELECTRIC)+lvl+cha);
			stats.setStat(CharStats.SAVE_FIRE,stats.getStat(CharStats.SAVE_FIRE)+lvl+cha);
			stats.setStat(CharStats.SAVE_GAS,stats.getStat(CharStats.SAVE_GAS)+lvl+cha);
			stats.setStat(CharStats.SAVE_GENERAL,stats.getStat(CharStats.SAVE_GENERAL)+lvl+cha);
			stats.setStat(CharStats.SAVE_JUSTICE,stats.getStat(CharStats.SAVE_JUSTICE)+lvl+cha);
			stats.setStat(CharStats.SAVE_MAGIC,stats.getStat(CharStats.SAVE_MAGIC)+lvl+cha);
			stats.setStat(CharStats.SAVE_MIND,stats.getStat(CharStats.SAVE_MIND)+lvl+cha);
			stats.setStat(CharStats.SAVE_PARALYSIS,stats.getStat(CharStats.SAVE_PARALYSIS)+lvl+cha);
			stats.setStat(CharStats.SAVE_POISON,stats.getStat(CharStats.SAVE_POISON)+lvl+cha);
			stats.setStat(CharStats.SAVE_TRAPS,stats.getStat(CharStats.SAVE_TRAPS)+lvl+cha);
			stats.setStat(CharStats.SAVE_UNDEAD,stats.getStat(CharStats.SAVE_UNDEAD)+lvl+cha);
			stats.setStat(CharStats.SAVE_WATER,stats.getStat(CharStats.SAVE_WATER)+lvl+cha);
		}
	}
}
	
