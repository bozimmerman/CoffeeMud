package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_LinkedHealth extends Prayer
{
	MOB buddy=null;
	public Prayer_LinkedHealth()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Linked Health";
		displayText="(Linked Health)";

		quality=Ability.OK_OTHERS;
		holyQuality=Prayer.HOLY_NEUTRAL;

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=Ability.CAN_MOBS;
		
		baseEnvStats().setLevel(13);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_LinkedHealth();
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
			if(buddy!=null)
			{
				mob.tell("Your health is no longer linked with "+buddy.name()+".");
				Ability A=buddy.fetchAffect(ID());
				if(A!=null) A.unInvoke();
			}
		}
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))
		&&(Util.bset(affect.targetCode(),Affect.MASK_HURT)))
		{
			if((affect.tool()==null)||(!affect.tool().ID().equals(ID())))
			{
				int recovery=(int)Math.round(Util.div((affect.targetCode()-Affect.MASK_HURT),2.0));
				affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()-recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
				ExternalPlay.postDamage(affect.source(),buddy,this,recovery,Affect.MSG_OK_VISUAL,Weapon.TYPE_BURNING,"<T-NAME> absorb(s) damage from the harm to "+affect.target().name()+".");
			}
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(mob.fetchAffect(ID())!=null)
		{
			mob.tell("Your health is already linked with someones!");
			return false;
		}
		if(target.fetchAffect(ID())!=null)
		{
			mob.tell(target.name()+"'s health is already linked with someones!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> pray(s) that <S-HIS-HER> health be linked with <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<S-NAME> and <T-NAME> are linked in health.");
				buddy=mob;
				beneficialAffect(mob,target,0);
				buddy=target;
				beneficialAffect(mob,mob,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> pray(s) for a link with <T-NAME>, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}