package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_BellyRolling extends StdAbility
{
	public String ID() { return "Skill_BellyRolling"; }
	public String name(){ return "Belly Rolling";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Skill_BellyRolling();}
	private boolean doneThisRound=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
			doneThisRound=false;
		return super.tick(ticking,tickID);
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		if(affect.amITarget(mob)
		&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(Sense.isSitting(mob))
		&&(affect.tool()!=null)
		&&(!doneThisRound)
		&&(affect.tool() instanceof Weapon))
		{
			// can't use -NAME for affect.source() lest sitting prevent it
			FullMsg msg=new FullMsg(mob,affect.source(),null,Affect.MSG_SITMOVE,"<S-NAME> roll(s) away from the attack by <T-NAMESELF>!");
			if((profficiencyCheck(mob.charStats().getStat(CharStats.DEXTERITY)-50,false))
			&&((affect.source().getVictim()==mob)||(affect.source().getVictim()==null))
			&&(mob.location().okAffect(mob,msg)))
			{
				doneThisRound=true;
				mob.location().send(mob,msg);
				helpProfficiency(mob);
				return false;
			}
		}
		return true;
	}
}


