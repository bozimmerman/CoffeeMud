package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_CurseMinds extends Prayer
{
	public String ID() { return "Prayer_CurseMinds"; }
	public String name(){ return "Curse Minds";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Cursed Mind)";}
	public Environmental newInstance(){	return new Prayer_CurseMinds();}

	boolean notAgain=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		MOB mob=(MOB)affected;
		if(mob.isInCombat())
		{
			MOB newvictim=mob.location().fetchInhabitant(Dice.roll(1,mob.location().numInhabitants(),-1));
			if(newvictim!=mob) mob.setVictim(newvictim);
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("Your mind feels less cursed.");
		ExternalPlay.standIfNecessary(mob);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)-50);
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Hashtable h=properTargets(mob,givenTarget,auto);
		if(h==null) return false;

		boolean success=profficiencyCheck(0,auto);
		boolean nothingDone=true;
		if(success)
		{
			for(Enumeration e=h.elements();e.hasMoreElements();)
			{
				MOB target=(MOB)e.nextElement();
				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto)|Affect.MASK_MALICIOUS,auto?"":"^S<S-NAME> "+prayWord(mob)+" an unholy curse upon <T-NAMESELF>.^?");
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MASK_MALICIOUS|Affect.TYP_MIND|(auto?Affect.MASK_GENERAL:0),null);
				if((target!=mob)&&(mob.location().okAffect(mob,msg))&&(mob.location().okAffect(mob,msg2)))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					if((!msg.wasModified())&&(!msg2.wasModified()))
					{
						success=maliciousAffect(mob,target,15,-1);
						mob.location().show(target,null,Affect.MSG_OK_VISUAL,"<S-NAME> look(s) confused!");
					}
					nothingDone=false;
				}
			}
		}

		if(nothingDone)
			return maliciousFizzle(mob,null,"<S-NAME> attempt(s) to curse everyone, but flub(s) it.");


		// return whether it worked
		return success;
	}
}
