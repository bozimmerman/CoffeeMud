package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_StinkingCloud extends Spell
{
	public String ID() { return "Spell_StinkingCloud"; }
	public String name(){return "Stinking Cloud";}
	public String displayText(){return "(In the Stinking Cloud)";}
	public int maxRange(){return 3;}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Spell_StinkingCloud();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_EVOCATION;}
	
	Room castingLocation=null;

	public boolean tick(int tickID)
	{
		if((tickID==Host.MOB_TICK)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			MOB vic=(MOB)affected;
			if((vic.location()!=castingLocation)||(vic.amDead()))
				unInvoke();
			else
			if((!vic.amDead())&&(vic.location()!=null)&&(Sense.canSmell(vic)))
			{
				if((vic.curState().getHunger()<=0))
					ExternalPlay.postDamage(invoker,vic,this,vic.envStats().level(),Affect.TYP_ACID,-1,"<T-NAME> dry heave(s) in the stinking cloud.");
				else
				{
					ExternalPlay.postDamage(invoker,vic,this,vic.envStats().level(),Affect.TYP_ACID,-1,"<T-NAME> heave(s) all over the place!");
					vic.curState().adjHunger(-500,vic.maxState());
				}
			}
			else
				unInvoke();
		}
		return super.tick(tickID);
	}
	
	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(Sense.canSmell(mob))
				switch(affect.sourceMinor())
				{
				case Affect.TYP_ADVANCE:
					if(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_GAS)))
					{
						mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> double(s) over from the sickening gas.");
						return false;
					}
					break;
				}
		}
		return super.okAffect(affect);
	}
	
	public void affect(Affect affect)
	{
		if((affected!=null)
		   &&(affected instanceof MOB))
		{
			switch(affect.sourceMinor())
			{
			case Affect.TYP_ADVANCE:
				unInvoke();
				break;
			}
		}
		super.affect(affect);
	}
	
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked)
		{
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to escape the stinking cloud!");
		}
	}
		

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=ExternalPlay.properTargets(this,mob,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth casting this on.");
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
			mob.location().show(mob,null,affectType(auto),auto?"A stinking cloud of orange and green gas appears!":"^S<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms around.  A horrendous cloud of green and orange gas appears!^?");
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				FullMsg msg2=new FullMsg(mob,target,this,Affect.MSK_CAST_MALICIOUS_VERBAL|Affect.TYP_GAS|(auto?Affect.ACT_GENERAL:0),null);
				if((mob.location().okAffect(msg))
				   &&(mob.location().okAffect(msg2))
				   &&(target.fetchAffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					if((!msg.wasModified())&&(!msg2.wasModified())&&(target.location()==mob.location()))
					{
						castingLocation=mob.location();
						success=maliciousAffect(mob,target,(adjustedLevel(mob)*10),-1);
						target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> become(s) enveloped in the stinking cloud!");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> incant(s), but the spell fizzles.");


		// return whether it worked
		return success;
	}
}