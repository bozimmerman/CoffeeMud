package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Undead_ColdTouch extends StdAbility
{
	int coldDown=3;

	public Undead_ColdTouch()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Cold Touch";
		displayText="(Cold and Weakened)";
		miscText="";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Undead_ColdTouch();
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.STRENGTH,3);
		affectableStats.setStat(CharStats.CONSTITUTION,3);
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
			mob.tell(mob,null,"The chill is lifted.");
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to touch!");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		String str=null;
		if(success)
		{
			str=auto?"":"^S<S-NAME> extend(s) a chilling hand to <T-NAMESELF>!^?";
			FullMsg msg=new FullMsg(mob,target,null,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_UNDEAD|(auto?Affect.ACT_GENERAL:0),str);
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> turn(s) cold!");
					success=maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> extend(s) a chilling hand to <T-NAMESELF>, but fail(s).");

		return success;
	}
}
