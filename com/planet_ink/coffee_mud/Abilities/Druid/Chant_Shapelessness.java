package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Shapelessness extends Chant
{
	public String ID() { return "Chant_Shapelessness"; }
	public String name(){ return "Shapelessness";}
	public String displayText(){ return "(Shapelessness)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return BENEFICIAL_SELF;}
	public Environmental newInstance(){	return new Chant_Shapelessness();}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> return(s) to material form.");
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setWeight(0);
		affectableStats.setHeight(-1);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&(Dice.rollPercentage()>25))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_PUT:
			case CMMsg.TYP_DROP:
			case CMMsg.TYP_HOLD:
			case CMMsg.TYP_WIELD:
			case CMMsg.TYP_WEAR:
			case CMMsg.TYP_REMOVE:
			case CMMsg.TYP_DELICATE_HANDS_ACT:
			case CMMsg.TYP_WITHDRAW:
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_UNLOCK:
			case CMMsg.TYP_HANDS:
				msg.source().tell("You have trouble manipulating matter in this form.");
				return false;
			case CMMsg.TYP_THROW:
			case CMMsg.TYP_WEAPONATTACK:
			case CMMsg.TYP_KNOCK:
			case CMMsg.TYP_PULL:
			case CMMsg.TYP_PUSH:
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_CLOSE:
				msg.source().tell("You fail your attempt to affect matter in this form.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			target.tell("You are already shapeless.");
			return false;
		}
		if((!auto)&&(!Chant_BlueMoon.moonInSky(mob.location(),null)))
		{
			mob.tell("You must be able under the moons glow for this magic to work.");
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant that <T-NAME> be given a shapeless form.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> shimmer(s) and become(s) ethereal!");
				beneficialAffect(mob,target,3);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) for a new shape, but nothing happens.");


		// return whether it worked
		return success;
	}
}
