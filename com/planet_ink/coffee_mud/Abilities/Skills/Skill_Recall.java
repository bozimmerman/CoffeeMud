package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Recall extends StdAbility
{
	public Skill_Recall()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Recall";
		displayText="(Recalled)";
		miscText="";

		triggerStrings.addElement("RECALL");
		triggerStrings.addElement("/");

		trainsRequired=1;
		practicesRequired=0;
		
		canBeUninvoked=true;
		isAutoinvoked=false;

		canTargetCode=0;
		canAffectCode=0;
		
		baseEnvStats().setLevel(1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Recall();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,mob.getStartRoom(),this,Affect.MSG_RECALL,auto?"<S-NAME> disappear(s) into the Java Plain!":"<S-NAME> recall(s) body and spirit to the Java Plain!");
			if(mob.location().okAffect(msg))
			{
				if(mob.isInCombat())
					ExternalPlay.flee(mob,"NOWHERE");
				mob.location().send(mob,msg);
				for(int f=0;f<mob.numFollowers();f++)
				{
					MOB follower=mob.fetchFollower(f);
					if((follower!=null)&&(follower.isMonster())&&(follower.location()!=null))
					{
						FullMsg msg2=new FullMsg(follower,mob.getStartRoom(),this,Affect.MSG_RECALL,"<S-NAME> is sucked into the vortex created by "+mob.name()+"s recall.");
						if(follower.location().okAffect(msg2))
						{
							if(follower.isInCombat()) ExternalPlay.flee(follower,"NOWHERE");
							follower.location().send(follower,msg2);
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> attempt(s) to recall, but <S-HIS-HER> plea goes unheard.");

		// return whether it worked
		return success;
	}

}