package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druid_Rend extends StdAbility
{
	public String ID() { return "Druid_Bite"; }
	public String name(){ return "Bite";}
	private static final String[] triggerStrings = {"BITE"};
	public int quality(){return Ability.MALICIOUS;}
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Druid_Bite();}
	public int classificationCode(){return Ability.SKILL;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()>0))
		{
			mob.tell("You are too far away to rend!");
			return false;
		}
		if(!Druid_ShapeShift.isShapeShifted(mob))
		{
			mob.tell("You must be in your animal form to rend.");
			return false;
		}
		if(mob.charStats().getMyRace().bodyMask()[Race.BODY_LEG]==0)
		{
			mob.tell("You must have legs to rend!");
			return false;
		}
		Ability A=mob.fetchAffect("Fighter_Pin");
		if(A!=null)
		{
			mob.tell("You rend your way out of the pin!");
			A.unInvoke();
			mob.delAffect(A);
			ExternalPlay.standIfNecessary(mob);
			return true;
		}
		
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((!Sense.isSitting(target))&&(!Sense.isSleeping(target)))
		{
			mob.tell("You can only rend someone who is on the ground!");
			return false;
		}
		
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
			int topDamage=adjustedLevel(mob)*2;
			int damage=Dice.roll(1,topDamage,0);
			FullMsg msg=new FullMsg(mob,target,this,Affect.MSK_MALICIOUS_MOVE|Affect.TYP_JUSTICE|(auto?Affect.MASK_GENERAL:0),null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				ExternalPlay.postDamage(mob,target,this,damage,Affect.MSG_OK_ACTION,Weapon.TYPE_PIERCING,"^F<S-NAME> <DAMAGE> <T-NAME> by rending <T-HIM-HER> with <S-HIS-HER> feet!^?");
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> fail(s) to rend <T-NAMESELF>.");

		// return whether it worked
		return success;
	}
}
