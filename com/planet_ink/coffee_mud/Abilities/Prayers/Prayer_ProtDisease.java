package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_ProtDisease extends Prayer
{
	public String ID() { return "Prayer_ProtDisease"; }
	public String name(){ return "Protection Disease";}
	public int quality(){ return BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Protection Disease)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your natural defenses against disease take over.");
	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_DISEASE,affectedStats.getStat(CharStats.SAVE_DISEASE)+100);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(invoker==null) return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if(msg.target()==invoker)
		{
			if((msg.tool()!=null)
			   &&(Dice.rollPercentage()>50)
			   &&((msg.targetMinor()==CMMsg.TYP_DISEASE)))
			{
				msg.source().location().show(invoker,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> magically repells the disease.");
				return false;
			}

		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> protection from disease.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> attain(s) disease protection.":"^S<S-NAME> "+prayWord(mob)+" for protection from diseases.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for protection from diseases, but go(es) unanswered.");


		// return whether it worked
		return success;
	}
}
