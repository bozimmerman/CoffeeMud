package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_BlindFighting extends StdAbility
{
	public String ID() { return "Fighter_BlindFighting"; }
	public String name(){ return "Blind Fighting";}
	public String displayText(){ return "";}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Fighter_BlindFighting();}
	public int classificationCode(){ return Ability.SKILL;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;
		if(!mob.isInCombat()) return;
		if((!Sense.canBeSeenBy(mob.getVictim(),mob))
		&&(profficiencyCheck(0,false)))
		{
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_VICTIM);
			helpProfficiency(mob);
		}
	}
}