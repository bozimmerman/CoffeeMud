package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Tourettes extends Spell
{
	int plagueDown=4;

	public Spell_Tourettes()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Tourettes";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Tourettes)";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;

		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Tourettes();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;
	}
	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(tickID);

		if(!super.tick(tickID))
			return false;
		if((--plagueDown)<=0)
		{
			MOB mob=(MOB)affected;
			plagueDown=4;
			if(invoker==null) invoker=mob;
			
			MOB target=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
			if((target!=null)&&(Sense.canSpeak(mob))&&(Sense.canBeSeenBy(target,mob)))
			{
				String say="Penis wrinkle!";
				switch(Dice.roll(1,30,0))
				{
				case 1: say="You are a very bad "+target.charStats().getMyClass().name()+"!"; break;
				case 2: say="I think all "+target.charStats().getMyRace().name()+"s are stupid!"; break;
				case 3: say="Damn flark!"; break;
				case 4: say="Squeegee!"; break;
				case 5: say="Ding dong!"; break;
				case 6: say="Goober!"; break;
				case 7: say="Noodle"+((target.charStats().getStat(CharStats.GENDER)==(int)'M')?"boy":"girl")+"!"; break;
				case 8: say="Groin scratcher!"; break;
				case 9: say="Geek!"; break;
				case 10: say="Dork!"; break;
				case 11: say="Orc kisser!"; break;
				case 12: say="Jerk!"; break;
				case 13: say="Tuddleworm!"; break;
				case 14: say="Poopie diaper!"; break;
				case 15: say="Panty stain!"; break;
				case 16: say="Blah blah blah blah blah!"; break;
				case 17: say="Hairpit sniffer!"; break;
				case 18: say="Gluteous maximus cavity!"; break;
				case 19: say="Uncle copulator!"; break;
				case 20: say="Toe jam eater!"; break;
				case 21: say="Partial excrement!"; break;
				case 22: say="Female dog!"; break;
				case 23: say="Illigitimate offspring!"; break;
				case 24: say="You are overweight!"; break;
				case 25: say="You smell funny!"; break;
				case 26: say="You aren't very smart!"; break;
				case 27: say="You.. you.. ah nevermind."; break;
				case 28: say="Yokle!"; break;
				case 29: say="Ugly head!"; break;
				case 30: say="Goop"+((target.charStats().getStat(CharStats.GENDER)==(int)'M')?"boy":"girl")+"!";  break;
				}
				ExternalPlay.quickSay(mob,target,say,false,false);
				if((target!=invoker)&&(target!=mob)&&(target.fetchAffect(ID())==null))
				{
					if(Dice.rollPercentage()>target.charStats().getStat(CharStats.SAVE_DISEASE))
					{
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> feel(s) different somehow...");
						maliciousAffect(invoker,target,0,-1);
					}
				}
			}
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setStat(CharStats.CHARISMA,2);
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
			FullMsg msg=new FullMsg(mob,target,this,affectType|Affect.MASK_MALICIOUS,auto?"":"<S-NAME> incant(s) rudely to <T-NAMESELF>.");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_DISEASE|(auto?Affect.ACT_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					invoker=mob;
					maliciousAffect(mob,target,0,-1);
					mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> feel(s) different somehow...");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) rudely to <T-NAMESELF>, but the spell fades.");
		// return whether it worked
		return success;
	}
}
