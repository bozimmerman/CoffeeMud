package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MindLight extends Spell
{
	public String ID() { return "Spell_MindLight"; }
	public String name(){return "Mind Light";}
	public String displayText(){return "(Mind Light spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	public Environmental newInstance(){	return new Spell_MindLight();}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			room.showHappens(CMMsg.MSG_OK_VISUAL, "The mind light starts to fade.");
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(!(affected instanceof Room)) return true;
		Room R=(Room)affected;
		if((invoker()!=null)&&(canBeUninvoked()))
		{
			if(!R.isInhabitant(invoker()))
			   unInvoke();
			return false;
		}
		for(int m=0;m<R.numInhabitants();m++)
		{
			MOB M=R.fetchInhabitant(m);
			if(M!=null)
			{
				if(invoker()!=null)
					M.curState().adjMana((invoker().charStats().getStat(CharStats.INTELLIGENCE)+invoker().charStats().getStat(CharStats.WISDOM))/2,M.maxState());
				else
					M.curState().adjMana((M.charStats().getStat(CharStats.INTELLIGENCE)+M.charStats().getStat(CharStats.WISDOM))/2,M.maxState());
			}
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		Environmental target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"Mind Light has already been cast here!");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}


		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this, affectType(auto), (auto?"T":"^S<S-NAME> incant(s) and gesture(s) and t")+"he mind light envelopes everyone.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((ExternalPlay.doesOwnThisProperty(mob,mob.location()))
				||((mob.amFollowing()!=null)&&(ExternalPlay.doesOwnThisProperty(mob.amFollowing(),mob.location()))))
					mob.location().addNonUninvokableEffect(this);
				else
					beneficialAffect(mob,mob.location(),0);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s) lightly, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
