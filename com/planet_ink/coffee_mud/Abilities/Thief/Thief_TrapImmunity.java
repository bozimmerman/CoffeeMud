package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_TrapImmunity extends ThiefSkill
{
	public String ID() { return "Thief_TrapImmunity"; }
	public String name(){ return "Trap Immunity";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public Environmental newInstance(){	return new Thief_TrapImmunity();}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.SAVE_TRAPS,affectableStats.getStat(CharStats.SAVE_TRAPS)+(profficiency()/2));
	}
	
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
		   return super.okAffect(myHost,msg);
		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(!msg.amISource(mob))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Trap))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> deftly avoid(s) a trap.");
			return false;
		}
		return super.okAffect(myHost,msg);
	}
}
