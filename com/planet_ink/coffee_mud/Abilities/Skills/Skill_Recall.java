package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Recall extends StdAbility
{
	public String ID() { return "Skill_Recall"; }
	public String name(){ return "Recall";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"RECALL","/"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Recall();}

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