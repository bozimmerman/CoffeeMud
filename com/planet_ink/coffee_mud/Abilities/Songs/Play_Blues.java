package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Blues extends Play
{
	public String ID() { return "Play_Blues"; }
	public String name(){ return "Blues";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Play_Blues();}
	protected String songOf(){return "the "+name();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		// the sex rules
		if(!(affected instanceof MOB)) return true;

		MOB myChar=(MOB)affected;
		if((msg.target()!=null)&&(msg.target() instanceof MOB))
		{
			if((msg.amISource(myChar)||(msg.amITarget(myChar))
			&&(msg.tool()!=null)
			&&(msg.tool().ID().equals("Social"))
			&&(msg.tool().Name().equals("MATE <T-NAME>")
				||msg.tool().Name().equals("SEX <T-NAME>"))))
			{
				if(msg.amISource(myChar))
					myChar.tell("You really don't feel like it.");
				else
				if(msg.amITarget(myChar))
					msg.source().tell(myChar.name()+" doesn't look like "+myChar.charStats().heshe()+" feels like it.");
				return false;
			}
		}
		return true;
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			mob.curState().adjHunger(-2,mob.maxState());
		}
		return true;
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-((invoker().charStats().getStat(CharStats.CHARISMA)/4)+(invokerLevel()/2)));
	}
	public void affectCharStats(MOB mob, CharStats stats)
	{
		super.affectCharStats(mob,stats);
		if(invoker()!=null)
			stats.setStat(CharStats.SAVE_JUSTICE,stats.getStat(CharStats.SAVE_JUSTICE)-(invoker().charStats().getStat(CharStats.CHARISMA)));
	}
}

