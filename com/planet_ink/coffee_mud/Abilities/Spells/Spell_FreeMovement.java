package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FreeMovement extends Spell
{
	public Spell_FreeMovement()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Free Movement";
		displayText="(Free Movement)";
		miscText="";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_OTHERS;

		baseEnvStats().setLevel(9);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FreeMovement();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ABJURATION;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked)
			mob.tell("Your uninhibiting protection dissipates.");

		super.unInvoke();

	}


	public boolean okAffect(Affect affect)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS))
		&&(affect.targetMinor()==Affect.TYP_CAST_SPELL)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Ability)
		&&(!mob.amDead())
		&&(profficiencyCheck(0,false)))
		{
			Ability A=(Ability)affect.tool();
			MOB newMOB=(MOB)CMClass.getMOB("StdMOB");
			FullMsg msg=new FullMsg(newMOB,null,null,Affect.MSG_SIT,null);
			newMOB.recoverEnvStats();
			try
			{
				A.affectEnvStats(newMOB,newMOB.envStats());
				if((!Sense.aliveAwakeMobile(newMOB,true))
				   ||(!A.okAffect(msg)))
				{
					affect.addTrailerMsg(new FullMsg(mob,null,Affect.MSG_OK_VISUAL,"The uninhibiting barrier around <S-NAME> repels the "+A.name()+"."));
					return false;
				}
			}
			catch(Exception e)
			{}
		}
		return true;
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<S-NAME> feel(s) freely protected.":"<S-NAME> invoke(s) an uninhibiting barrier of protection around <T-NAMESELF>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an uninhibiting barrier, but fail(s).");

		return success;
	}
}
