package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_CloudWalk extends Chant
{
	public String ID() { return "Chant_CloudWalk"; }
	public String name(){ return "Cloud Walk";}
	public String displayText(){return "(Cloud Walk)";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	protected int canTargetCode(){return 0;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> float(s) down to the ground.");

		super.unInvoke();

	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if(mob!=invoker)
			{
				if((mob.location()==null)||(!mob.location().isInhabitant(invoker)))
				   unInvoke();
			}
			else
			{
				HashSet H=mob.getGroupMembers(new HashSet());
				for(Iterator e=H.iterator();e.hasNext();)
				{
					MOB M=(MOB)e.next();
					if((M!=null)
					   &&(M.location()!=null)
					   &&(M!=mob)
					   &&(M.location()==mob.location())
					   &&(M.fetchEffect(ID())==null))
					{
						M.location().show(M,null,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> start(s) to fly around!");
						beneficialAffect(mob,M,0);
					}
				}
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
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already a cloud walker");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> start(s) to fly around!");
					beneficialAffect(mob,target,0);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");
		// return whether it worked
		return success;
	}
}
