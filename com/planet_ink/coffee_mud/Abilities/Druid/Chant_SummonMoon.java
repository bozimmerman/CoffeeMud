package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SummonMoon extends Chant
{
	public String ID() { return "Chant_SummonMoon"; }
	public String name(){ return "Summon Moon";} 
	public String displayText(){return "(Summon Moon)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonMoon();}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("The summoned moon sets.");

		super.unInvoke();

	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof Room)
		{
			Room R=(Room)affected;
			if((R.getArea().getTODCode()!=Area.TIME_DUSK)
			&&(R.getArea().getTODCode()!=Area.TIME_NIGHT))
				unInvoke();
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Room target=mob.location();
		if(target==null) return false;
		if((target.getArea().getTODCode()!=Area.TIME_DUSK)
		&&(target.getArea().getTODCode()!=Area.TIME_NIGHT))
		{
			mob.tell("You can only start this chant at night.");
			return false;
		}
		if((target.domainType()&Room.INDOORS)==0)
		{
			mob.tell("This chant only works indoors.");
			return false;
		}
		
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell("This place is already under the summoned moon.");
			return false;
		}

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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the sky.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					mob.location().showHappens(Affect.MSG_OK_VISUAL,"The Moon pierces into the room!");
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the sky, but the magic fades.");
		// return whether it worked
		return success;
	}
}