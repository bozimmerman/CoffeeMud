package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_ProtectElements extends Prayer
{
	public String ID() { return "Prayer_ProtectElements"; }
	public String name(){ return "Protection Elements";}
	public int quality(){ return BENEFICIAL_SELF;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Protection/Elements)";}
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
			mob.tell("Your elemental protection fades.");
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setStat(CharStats.SAVE_ACID,affectableStats.getStat(CharStats.SAVE_ACID)+50);
		affectableStats.setStat(CharStats.SAVE_COLD,affectableStats.getStat(CharStats.SAVE_COLD)+50);
		affectableStats.setStat(CharStats.SAVE_ELECTRIC,affectableStats.getStat(CharStats.SAVE_ELECTRIC)+50);
		affectableStats.setStat(CharStats.SAVE_FIRE,affectableStats.getStat(CharStats.SAVE_FIRE)+50);
		affectableStats.setStat(CharStats.SAVE_GAS,affectableStats.getStat(CharStats.SAVE_GAS)+50);
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> already <S-HAS-HAVE> protection from elements.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> attain(s) elemental protection.":"^S<S-NAME> "+prayWord(mob)+" for elemental protection.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for elemental protection, but nothing happens.");


		// return whether it worked
		return success;
	}
}
