package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Nightmare extends Spell
{
	public String ID() { return "Spell_Nightmare"; }
	public String name(){return "Nightmare";}
	public String displayText(){return "(You are having a nightmare)";}
	public int maxRange(){return 1;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Nightmare();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public int amountRemaining=0;
	boolean notAgainThisRound=false;

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep

		if(affect.amISource(mob))
		{
			if((!Util.bset(affect.sourceMajor(),Affect.MASK_GENERAL))
			&&((Util.bset(affect.sourceMajor(),Affect.MASK_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.MASK_MOVE))))
			{
				if(notAgainThisRound)
				{
					notAgainThisRound=true;
					switch(Dice.roll(1,10,0))
					{
					case 1:	mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> struggle(s) with an imaginary foe."); break;
					case 2:	mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> scream(s) in horror!"); break;
					case 3:	mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> beg(s) for mercy."); break;
					case 4:	mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> grab(s) <S-HIS-HER> head and cry(s)."); break;
					case 5:	mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> whimper(s)."); break;
					case 6:	mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> look(s) terrified!"); break;
					case 7:	mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> swipe(s) at <S-HIS-HER> feet and arms."); break;
					case 8:	mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> claw(s) at the air."); break;
					case 9:	mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> shiver(s) in fear."); break;
					case 10:mob.location().show(mob,null,Affect.MSG_OK_ACTION,
						"<S-NAME> shake(s) in anticipation of horror!"); break;
					}
					amountRemaining-=mob.charStats().getStat(CharStats.INTELLIGENCE*2);
					amountRemaining-=mob.envStats().level();
					if(amountRemaining<0)
						unInvoke();
				}
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		if((!mob.amDead())&&(mob.location()!=null))
			mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to wake up from <S-HIS-HER> nightmare.");
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Host.MOB_TICK)
			notAgainThisRound=false;
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> whisper(s) to <T-NAMESELF>.^?");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0),null);
			if((mob.location().okAffect(mob,msg))||(mob.location().okAffect(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if(!msg.wasModified())
				{
					amountRemaining=150;
					maliciousAffect(mob,target,0,-1);
					target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> go(es) into the throws of a horrendous nightmare!!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> whisper(s) to <T-NAMESELF>, but the spell fades.");

		// return whether it worked
		return success;
	}
}