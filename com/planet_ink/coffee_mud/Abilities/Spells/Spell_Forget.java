package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Forget extends Spell
{
	public String ID() { return "Spell_Forget"; }
	public String name(){return "Forget";}
	public String displayText(){return "(Forgetful)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Forget();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if((affect.amISource(mob))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_MIND)+10)))
		{
			mob.tell("You can't remember "+affect.tool().name()+"!");
			return false;
		}

		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked)
			mob.tell("You start remembering things again.");
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if((!target.mayIFight(mob))||(levelDiff>=10))
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(-((target.charStats().getStat(CharStats.INTELLIGENCE)*2)+(levelDiff*5)),auto);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			String str=auto?"":"^S<S-NAME> incant(s) confusingly at <T-NAMESELF>^?";
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),str);
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND|(auto?Affect.ACT_GENERAL:0),null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					success=maliciousAffect(mob,target,0,-1);
					if(success)
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> seem(s) forgetful!");
				}
			}
		} 
		if(!success)
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) confusingly at <T-NAMESELF>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
