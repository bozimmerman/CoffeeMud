package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Timeport extends Spell
{
	public String ID() { return "Spell_Timeport"; }
	public String name(){return "Timeport";}
	public String displayText(){return "(Travelling through time)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Spell_Timeport();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	private final static int mask=
			EnvStats.CAN_NOT_TASTE|EnvStats.CAN_NOT_SMELL|EnvStats.CAN_NOT_SEE
		    |EnvStats.CAN_NOT_HEAR;
	private final static int mask2=Integer.MAX_VALUE
			-EnvStats.CAN_SEE_BONUS
		    -EnvStats.CAN_SEE_DARK
		    -EnvStats.CAN_SEE_EVIL
		    -EnvStats.CAN_SEE_GOOD
		    -EnvStats.CAN_SEE_HIDDEN
		    -EnvStats.CAN_SEE_INFRARED
		    -EnvStats.CAN_SEE_INVISIBLE
		    -EnvStats.CAN_SEE_METAL
		    -EnvStats.CAN_SEE_SNEAKERS
		    -EnvStats.CAN_SEE_VICTIM;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(mask&mask2);
		affectableStats.setDisposition(EnvStats.IS_NOT_SEEN);
		affectableStats.setDisposition(EnvStats.IS_INVISIBLE);
		affectableStats.setDisposition(EnvStats.IS_HIDDEN);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		MOB mob=null;
		Room room=null;
		if((affected!=null)&&(canBeUninvoked())&&(affected instanceof MOB))
		{
			mob=(MOB)affected;
			room=mob.location();
			ExternalPlay.resumeTicking(mob,-1);
		}
		super.unInvoke();
		if(room!=null)
			room.show(mob, null, CMMsg.MSG_OK_VISUAL, "<S-NAME> reappear(s)!");
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			if(msg.amISource((MOB)affected))
				if((!Util.bset(msg.sourceCode(),CMMsg.MASK_GENERAL))
				&&(!Util.bset(msg.targetCode(),CMMsg.MASK_GENERAL)))
				{
					msg.source().tell("Nothing just happened.  You didn't do that.");
					return false;
				}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

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

			FullMsg msg = new FullMsg(mob, target, this,affectType(auto),(auto?"":"^S<S-NAME> speak(s) and gesture(s)")+"!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Room room=mob.location();
				target.makePeace();
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB M=room.fetchInhabitant(i);
					if((M!=null)&&(M.getVictim()==target))
						M.makePeace();
				}
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> vanish(es)!");
				ExternalPlay.suspendTicking(target,-1);
				beneficialAffect(mob,target,3);
				Ability A=target.fetchEffect(ID());
				if(A!=null)	ExternalPlay.startTickDown(A,Host.TICK_MOB,1);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s) for awhile, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}