package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_IncreaseGravity extends Spell
{
	public String ID() { return "Spell_IncreaseGravity"; }
	public String name(){return "Increase Gravity";}
	public String displayText(){return "(Gravity is Increased)";}
	protected int canAffectCode(){return CAN_ROOMS|CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	private Vector childrenAffects=new Vector();
	private Room theGravityRoom=null;
	private Room gravityRoom()
	{
		if(theGravityRoom!=null)
			return theGravityRoom;
		if(affected instanceof Room)
			theGravityRoom=(Room)affected;
		return theGravityRoom;
	}
	public Environmental newInstance(){	return new Spell_IncreaseGravity();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		else
		if((affected!=null)&&(affected instanceof Room)&&(invoker!=null))
		{
			Room room=(Room)affected;
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB inhab=room.fetchInhabitant(i);
				if(inhab.fetchAffect(ID())==null)
				{
					Ability A=(Ability)this.copyOf();
					A.setBorrowed(inhab,true);
					A.startTickDown(invoker,inhab,tickDown);
				}
				if(inhab.isInCombat())
					inhab.curState().adjMovement(-1,inhab.maxState());
			}
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(canBeUninvoked())
		{
			if(affected instanceof Room)
			{
				Room room=(Room)affected;
				room.showHappens(Affect.MSG_OK_VISUAL, "Gravity returns to normal...");
			}
			else
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;
				if((mob.location()!=null)&&(mob.location()!=gravityRoom()))
					mob.location().show(mob, null, Affect.MSG_OK_VISUAL, "Your weight returns to normal..");
			}
		}
		super.unInvoke();
	}

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg)) return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			if(((MOB)affected).location()!=gravityRoom())
			{
				unInvoke();
				return false;
			}
		}
		switch(msg.sourceMinor())
		{
		case Affect.TYP_ADVANCE:
			{
				msg.source().tell("You feel too heavy to advance.");
				return false;
			}
		case Affect.TYP_RETREAT:
			{
				msg.source().tell("You feel too heavy to retreat.");
				return false;
			}
		case Affect.TYP_LEAVE:
		case Affect.TYP_FLEE:
			{
				msg.source().tell("You feel too heavy to leave.");
				return false;
			}
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(!(affected instanceof MOB)) return;
		if(((MOB)affected).location()!=gravityRoom())
			unInvoke();
		else
		{
			if((affectableStats.disposition()&EnvStats.IS_FLYING)>0)
				affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_FLYING);
			affectableStats.setWeight(affectableStats.weight()*2);
		}
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

		if(target.fetchAffect(this.ID())!=null)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"Gravity has already been increased here!");
			if(mob.location().okAffect(mob,msg))
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

			FullMsg msg = new FullMsg(mob, target, this, affectType(auto), (auto?"G":"^S<S-NAME> speak(s) and wave(s) and g")+"ravity begins to increase!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				theGravityRoom=mob.location();
				if((ExternalPlay.doesOwnThisProperty(mob,mob.location()))
				||((mob.amFollowing()!=null)&&(ExternalPlay.doesOwnThisProperty(mob.amFollowing(),mob.location()))))
					mob.location().addNonUninvokableAffect((Ability)copyOf());
				else
					beneficialAffect(mob,mob.location(),adjustedLevel(mob));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> speak(s) heavily, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}