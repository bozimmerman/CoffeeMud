package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_BloodMoon extends Prayer
{
	public Prayer_BloodMoon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Blood Moon";
		displayText="(Blood Moon)";

		quality=Ability.MALICIOUS;
		holyQuality=Prayer.HOLY_EVIL;

		baseEnvStats().setLevel(13);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Prayer_BloodMoon();
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell("The blood moon over you fades.");
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((affect.amITarget(mob))&&(Util.bset(affect.targetCode(),Affect.MASK_HURT)))
		{
			int recovery=(int)Math.round(Util.div((affect.targetCode()-Affect.MASK_HURT),2.0));
			affect.modify(affect.source(),affect.target(),affect.tool(),affect.sourceCode(),affect.sourceMessage(),affect.targetCode()+recovery,affect.targetMessage(),affect.othersCode(),affect.othersMessage());
		}
		return true;
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> pray(s) that <T-NAME> feel pain.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,Affect.MSG_OK_VISUAL,"The Blood Moon rises over <S-NAME>.");
				maliciousAffect(mob,target,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> pray(s) for the Blood Moon, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}