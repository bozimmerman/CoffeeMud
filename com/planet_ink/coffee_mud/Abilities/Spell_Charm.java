package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class Spell_Charm extends Spell
	implements CharmDevotion
{
	public Spell_Charm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Charm";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Charmed)";

		malicious=true;


		baseEnvStats().setLevel(6);

		addQualifyingClass(new Mage().ID(),6);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);
		addQualifyingClass(new Thief().ID(),24);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Charm();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amITarget(mob))
		{
			if((affect.targetType()==Affect.STRIKE)
			&&(affect.source()==mob.amFollowing()))
				unInvoke();
		}
		if(affect.amISource(mob))
		{
			if((affect.sourceType()==Affect.STRIKE)
			&&(affect.target()==mob.amFollowing()))
			{
				mob.tell("You like "+mob.amFollowing().charStats().himher()+" too much.");
				return false;
			}
		}

		return super.okAffect(affect);
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		if(affect.amISource((MOB)affected))
			((MOB)affected).setFollowing(invoker);
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affecting()==null)||(!(affecting() instanceof MOB)))
			return;

		if(affected == affecting())
			((MOB)affecting()).setFollowing(invoker);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("Your free-will returns.");
		mob.tell("You are no longer following anyone.");
		if(mob.amFollowing()!=null)
			mob.amFollowing().tell(mob.name()+" is no longer following you.");
		mob.setFollowing(null);
	}



	public boolean invoke(MOB mob, Vector commands)
	{
		MOB target=this.getTarget(mob,commands);
		if(target==null) return false;

		// sleep has a 10 level difference for PCs, so check for this.
		int levelDiff=target.envStats().level()-mob.envStats().level();
		if((!target.isMonster())&&(levelDiff<10))
		{
			mob.tell(target.charStats().HeShe()+" looks too powerful.");
			return false;
		}

		if(!Sense.canSpeak(mob))
		{
			mob.tell("You can't speak!");
			return false;
		}

		// if they can't hear the sleep spell, it
		// won't happen
		if(!Sense.canBeHeardBy(mob,target))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(0);
		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			String str="<S-NAME> smile(s) and wink(s) at <T-NAME>";
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,str+".",Affect.STRIKE_MAGIC,str+".",Affect.SOUND_MAGIC,str+".");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					success=maliciousAffect(mob,target,0,Affect.STRIKE_MIND);
					if(success);
					{
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> follow(s) <S-NAME>!");
						target.setFollowing(mob);
					}
				}
			}
		}
		if(!success)
			return maliciousFizzle(mob,target,"<S-NAME> smile(s) and wink(s) at <T-NAME>, but nothing happens.");

		// return whether it worked
		return success;
	}
}
