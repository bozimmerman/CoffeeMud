package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Morris extends Dance
{
	public String ID() { return "Dance_Morris"; }
	public String name(){ return "Morris";}
	public int quality(){ return MALICIOUS;}
	protected String danceOf(){return name()+" Dance";}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setArmor(affectableStats.armor()+(affectableStats.armor()/2));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(affectableStats.attackAdjustment()/2));
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB))||(invoker==null))
			return true;

		MOB mob=(MOB)affected;
		// preventing distracting player from doin anything else
		if(msg.amISource(mob)
		&&(Dice.rollPercentage()>(100-(invoker().charStats().getStat(CharStats.CHARISMA)*2)))
		&&(msg.sourceMinor()==CMMsg.TYP_WEAPONATTACK))
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> become(s) distracted.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}

}
