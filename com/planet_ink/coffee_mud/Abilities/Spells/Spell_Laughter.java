package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Laughter extends Spell
{
	public Spell_Laughter()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Laughter";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Laughter spell)";


		quality=Ability.MALICIOUS;

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(15);

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Laughter();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ENCHANTMENT;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_MOVE);
	}

	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(tickID);

		if(!super.tick(tickID))
			return false;
		((MOB)affected).location().show((MOB)affected,null,Affect.MSG_OK_ACTION,"<S-NAME> laugh(s) uncontrollably, unable to move!");
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.tell("You stop laughing!");
		ExternalPlay.standIfNecessary(mob);
	}



	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>=1)
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}

		// if they can't hear the sleep spell, it
		// won't happen
		if((!auto)&&(!Sense.canBeHeardBy(mob,target)))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;


		boolean success=profficiencyCheck(-(target.envStats().level()*5),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> tell(s) <T-NAMESELF> a magical joke.");
			FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_MIND,null);
			if((mob.location().okAffect(msg))&&(mob.location().okAffect(msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((!msg.wasModified())&&(!msg2.wasModified()))
				{
					success=maliciousAffect(mob,target,8,-1);
					if(success)
						if(target.location()==mob.location())
							target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> begin(s) laughing uncontrollably, unable to move!!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> tell(s) <T-NAMESELF> a magical joke, but <T-NAME> doesn't think it is funny.");

		// return whether it worked
		return success;
	}
}
