package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);
		for(int c=0;c<MUD.charClasses.size();c++)
			addQualifyingClass(((CharClass)MUD.charClasses.elementAt(c)).ID(),1);
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

	public boolean invoke(MOB mob, Vector commands)
	{
		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,mob.getStartRoom(),this,Affect.HANDS_RECALL,Affect.HANDS_RECALL,Affect.SOUND_MAGIC,"<S-NAME> recall(s) body and spirit to the Java Plain!");
			if(mob.location().okAffect(msg))
			{
				if(mob.isInCombat())
					Movement.flee(mob,"NOWHERE");
				mob.location().send(mob,msg);
				for(int f=0;f<mob.numFollowers();f++)
				{
					MOB follower=mob.fetchFollower(f);
					if(follower.isMonster())
					{
						FullMsg msg2=new FullMsg(follower,mob.getStartRoom(),this,Affect.HANDS_RECALL,Affect.HANDS_RECALL,Affect.VISUAL_WNOISE,"<S-NAME> is sucked into the vortex created by "+mob.name()+"s recall.");
						if(follower.location().okAffect(msg2))
							follower.location().send(follower,msg2);
					}
				}
			}
		}
		else
		{
			// it didn't work, but tell everyone you tried.
			FullMsg msg=new FullMsg(mob,null,this,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> attempt(s) to recall, but <S-HIS-HER> plea goes unheard.");
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}

		// return whether it worked
		return success;
	}

}