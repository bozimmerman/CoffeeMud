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

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==MudHost.TICK_MOB)
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
					MUDFight.postDamage(invoker,vic,this,vic.envStats().level(),CMMsg.TYP_GAS,-1,"<T-NAME> dry heave(s) in the stinking cloud.");
				else
				{
					MUDFight.postDamage(invoker,vic,this,vic.envStats().level(),CMMsg.TYP_GAS,-1,"<T-NAME> heave(s) all over the place!");
					vic.curState().adjHunger(-500,vic.maxState());
				}
			}
			else
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		   &&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(Sense.canSmell(mob))
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_ADVANCE:
					if(Dice.rollPercentage()>(mob.charStats().getSave(CharStats.SAVE_GAS)))
					{
						mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> double(s) over from the sickening gas.");
						return false;
					}
					break;
				}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		   &&(affected instanceof MOB))
		{
			switch(msg.sourceMinor())
			{
			case CMMsg.TYP_ADVANCE:
				unInvoke();
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
		{
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to escape the stinking cloud!");
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=null;
		if(givenTarget!=null)
		{
			h=new Hashtable();
			h.put(givenTarget,givenTarget);
		}
		else
			h=MUDFight.properTargets(this,mob,auto);
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
			if(mob.location().show(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms around.  A horrendous cloud of green and orange gas appears!^?"))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,affectType(auto),null);
				FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_GAS|(auto?CMMsg.MASK_GENERAL:0),null);
				if((mob.location().okMessage(mob,msg))
				   &&(mob.location().okMessage(mob,msg2))
				   &&(target.fetchEffect(this.ID())==null))
				{
					mob.location().send(mob,msg);
					mob.location().send(mob,msg2);
					if((msg.value()<=0)&&(msg2.value()<=0)&&(target.location()==mob.location()))
					{
						castingLocation=mob.location();
						success=maliciousAffect(mob,target,(adjustedLevel(mob)*10),-1);
						target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> become(s) enveloped in the stinking cloud!");
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