package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_FlyingKick extends StdAbility
{
	public String ID() { return "Fighter_FlyingKick"; }
	public String name(){ return "Flying Kick";}
	private static final String[] triggerStrings = {"FLYINGKICK","FLYKICK"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Fighter_FlyingKick();}
	public int classificationCode(){return Ability.SKILL;}
	protected int overrideMana(){return 100;}
	public int minRange(){return 1;}
	public int maxRange(){return 5;}
	public int usageType(){return USAGE_MOVEMENT;}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if((student.fetchAbility("Fighter_AxKick")==null))
		{
			teacher.tell(student.name()+" has not yet learned the Ax Kick skill.");
			student.tell("You need to learn the Ax Kick skill to learn "+name()+".");
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()==0))
		{
			mob.tell("You are too close away to do a flying kick!");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob.charStats().getStat(CharStats.STRENGTH)-target.charStats().getStat(CharStats.STRENGTH)-10,auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			int topDamage=adjustedLevel(mob)+20;
			int damage=Dice.roll(1,topDamage,0);
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.MASK_GENERAL:0),null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				ExternalPlay.postDamage(mob,target,this,damage,Affect.MASK_GENERAL|Affect.MSG_NOISYMOVEMENT,Weapon.TYPE_BASHING,"^F<S-NAME> <DAMAGE> <T-NAME> with a flying KICK!^?"+CommonStrings.msp("bashed1.wav",30));
				if(mob.getVictim()==target)
				{
					mob.setAtRange(0);
					target.setAtRange(0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> fail(s) to land the flying kick on  <T-NAMESELF>.");

		// return whether it worked
		return success;
	}
}
