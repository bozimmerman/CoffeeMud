package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_SunCurse extends Prayer
{
	public String ID() { return "Prayer_SunCurse"; }
	public String name(){ return "Sun Curse";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Sun Curse)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_SunCurse();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!(affected instanceof MOB)) return;
		if(((MOB)affected).location()==null) return;
		if(Sense.isInDark(((MOB)affected).location()))
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
		else
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SEE);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		MOB mob=(MOB)affected;
		if((mob.location()!=null)
		&&(mob.location().getArea().canSeeTheSun(mob.location()))
		&&(mob.location().isInhabitant(mob)))
		{
			mob.tell("\n\r\n\r\n\r\n\r**THE SUN IS BEATING ONTO YOUR SKIN**\n\r\n\r");
			Ability A=CMClass.getAbility("Spell_FleshStone");
			if(A!=null)	A.invoke(mob,mob,true);
			unInvoke();
			return false;
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(mob.fetchEffect("Spell_FleshStone")==null))
			mob.tell("Your sun curse is lifted.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!auto)
		&&(target.location()!=null)
		&&(target.location().getArea().canSeeTheSun(target.location())))
		{
			mob.tell("This cannot be prayed for while the sun is shining on you.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(-target.envStats().level(),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for an unholy sun curse upon <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,-1);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> under a mighty sun curse!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to sun curse <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
