package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_Tangle extends Chant
{

	public int amountRemaining=0;
	public Item thePlants=null;

	public Chant_Tangle()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Tangle";

		// what the affected mob sees when they
		// bring up their affected list.
		displayText="(Tangled)";


		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(16);

		canBeUninvoked=true;
		isAutoinvoked=false;
		minRange=0;
		maxRange=2;

		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_Tangle();
	}

	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		if((thePlants==null)||(thePlants.myOwner()==null)||(!(thePlants.myOwner() instanceof Room)))
		{
			unInvoke();
			return super.okAffect(affect);
		}
		
		MOB mob=(MOB)affected;

		// when this spell is on a MOBs Affected list,
		// it should consistantly prevent the mob
		// from trying to do ANYTHING except sleep
		if(affect.amISource(mob))
		{
			if((!Util.bset(affect.sourceMajor(),Affect.ACT_GENERAL))
			&&((Util.bset(affect.sourceMajor(),Affect.ACT_HANDS))
			||(Util.bset(affect.sourceMajor(),Affect.ACT_MOVE))))
			{
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> struggle(s) against "+thePlants.name()+".");
				amountRemaining-=mob.charStats().getStat(CharStats.STRENGTH);
				if(amountRemaining<0)
					unInvoke();
				else
					return false;
			}
		}
		return super.okAffect(affect);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(!mob.amDead())
			mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> manage(s) to break <S-HIS-HER> way free of "+thePlants.name());
		ExternalPlay.standIfNecessary(mob);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		
		thePlants=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)
			&&(I.secretIdentity().equals(mob.name()))
			&&(I.myOwner()!=null)
			&&(I.myOwner() instanceof Room))
			{
				Ability A=I.fetchAffect("Chant_SummonPlants");
				if((A!=null)&&(A.invoker()==mob))
				{
					thePlants=I;
					break;
				}
			}
		}
		
		if(thePlants==null)
		{
			mob.tell("There doesn't appear to be any plants here you can control!");
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
			mob.location().show(mob,null,affectType,auto?"":"<S-NAME> begin(s) to chant.");
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,null);
			if((mob.location().okAffect(msg))&&(target.fetchAffect(this.ID())==null))
			{
				mob.location().send(mob,msg);
				if(!msg.wasModified())
				{
					amountRemaining=150;
					if(target.location()==mob.location())
					{
						success=maliciousAffect(mob,target,(adjustedLevel(mob)*10),-1);
						target.location().show(target,null,Affect.MSG_OK_ACTION,"<S-NAME> become(s) stuck in "+thePlants.name()+" as they grow and twist around <S-HIM-HER>!");
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but the magic fades.");


		// return whether it worked
		return success;
	}
}