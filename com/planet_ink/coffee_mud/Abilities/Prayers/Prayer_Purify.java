package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_Purify extends Prayer
{
	public String ID() { return "Prayer_Purify"; }
	public String name(){ return "Purify";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int quality(){ return INDIFFERENT;}
	public int holyQuality(){ return HOLY_GOOD;}
	public Environmental newInstance(){	return new Prayer_Purify();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if((!(target instanceof Food))
			&&(!(target instanceof Drink)))
		{
			mob.tell("You cannot purify "+target.name()+"!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for the power to purify <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				boolean doneSomething=false;
				while(target.numAffects()>0)
				{
					doneSomething=true;
					Ability A=target.fetchAffect(0);
					A.unInvoke();
					target.delAffect(A);
				}
				if((target instanceof Pill)
				&&(!((Pill)target).getSpellList().equals("Prayer_Sober")))
				{
					doneSomething=true;
					((Pill)target).setSpellList("Prayer_Sober");
				}
				if((target instanceof Potion)
				&&(!((Potion)target).getSpellList().equals("Prayer_Sober")))
				{
					doneSomething=true;
					((Potion)target).setSpellList("Prayer_Sober");
				}
				if(doneSomething)
					mob.location().showHappens(Affect.MSG_OK_VISUAL,target.name()+" appears purified!");
				target.recoverEnvStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for purification, but nothing happens.");
		// return whether it worked
		return success;
	}
}