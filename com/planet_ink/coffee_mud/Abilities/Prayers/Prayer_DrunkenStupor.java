package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_DrunkenStupor extends Prayer
{
	public String ID() { return "Prayer_DrunkenStupor"; }
	public String name(){ return "Drunken Stupor";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Drunken Stupod)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_DrunkenStupor();}
	public Ability inebriation=null;

	private Ability getInebriation()
	{
		if(inebriation==null)
		{
			
			inebriation=CMClass.getAbility("Inebriation");
			inebriation.makeLongLasting();
			inebriation.makeNonUninvokable();
			inebriation.setAffectedOne(affected);
		}
		return inebriation;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(((MOB)affected).envStats().level()));
	}


	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)-3));
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		Ability A=getInebriation();
		if(A!=null)
			A.tick(ticking,tickID);

		return true;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(!super.okAffect(myHost,affect))
			return false;

		if(affect.source()!=affected)
			return true;
		if(affect.source().location()==null)
			return true;
		if((!Util.bset(affect.targetMajor(),Affect.MASK_GENERAL))
		&&(affect.targetMajor()>0))
		{
			if((affect.target() !=null)
				&&(affect.target() instanceof MOB))
					affect.modify(affect.source(),affect.source().location().fetchInhabitant(Dice.roll(1,affect.source().location().numInhabitants(),0)-1),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode(),affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("You feel sober now.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"":"^S<S-NAME> "+prayForWord(mob)+" to inflict a drunken stupor upon <T-NAMESELF>.^?");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0),null);
			if((mob.location().okAffect(mob,msg))&&(mob.location().okAffect(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					invoker=mob;
					maliciousAffect(mob,target,0,-1);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> look(s) a bit tipsy!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> "+prayForWord(mob)+" to inflict a drunken stupor upon <T-NAMESELF>, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
