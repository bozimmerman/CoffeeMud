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

public class Spell_Web extends Spell
	implements EvocationDevotion
{

	public int amountRemaining=0;

	public Spell_Web()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Web";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Webbed)";


		malicious=true;

		baseEnvStats().setLevel(16);

		addQualifyingClass(new Mage().ID(),16);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		canBeUninvoked=true;
		isAutoinvoked=false;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_Web();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((affect.sourceType()!=Affect.AREA)
			&&(affect.sourceType()!=Affect.GENERAL)
			&&(affect.sourceType()!=Affect.VISUAL_WNOISE)
			&&(affect.sourceType()!=Affect.NO_EFFECT))
			{
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> struggle(s) against the web.");
				amountRemaining-=mob.charStats().getStrength();
				if(amountRemaining<0)
					unInvoke();
			}
		}
		return super.okAffect(affect);
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_MOVE);
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> manage(s) to break <S-HIS-HER> way free of the web.");
		Movement.standIfNecessary(mob);
	}

	public boolean invoke(MOB mob, Vector commands)
	{
		Hashtable h=null;
		if(mob.isInCombat())
			h=TheFight.allCombatants(mob);
		else
			h=TheFight.allPossibleCombatants(mob);

		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth webbing.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			mob.location().show(mob,null,Affect.SOUND_MAGIC,"<S-NAME> chant(s) and wave(s) <S-HIS-HER> arms.");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.STRIKE_MAGIC,Affect.SOUND_MAGIC,null);
				if((mob.location().okAffect(msg))&&(target.fetchAffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					if(!msg.wasModified())
					{
						amountRemaining=130;
						success=maliciousAffect(mob,target,(mob.envStats().level()*10),-1);
						mob.location().show(mob,target,Affect.VISUAL_WNOISE,"<T-NAME> become(s) stuck in a mass of web!");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but the spell fizzles.");


		// return whether it worked
		return success;
	}
}