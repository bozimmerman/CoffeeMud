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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=(!mob.isInCombat())||profficiencyCheck(mob,0,auto);
		if(success)
		{
			Room recalledRoom=mob.location();
			Room recallRoom=mob.getStartRoom();
			FullMsg msg=new FullMsg(mob,recallRoom,this,CMMsg.MSG_RECALL,CMMsg.MSG_LEAVE,CMMsg.MSG_RECALL,auto?getScr("Skills","recallgo1"):getScr("Skills","recallgo2"));
			if(recalledRoom.okMessage(mob,msg))
			{
				if(mob.isInCombat())
					CommonMsgs.flee(mob,"NOWHERE");
				recalledRoom.send(mob,msg);
				if(recalledRoom.isInhabitant(mob))
					recallRoom.bringMobHere(mob,false);
				for(int f=0;f<mob.numFollowers();f++)
				{
					MOB follower=mob.fetchFollower(f);
					if((follower!=null)
					&&(follower.isMonster())
					&&(follower.location()==recalledRoom)
					&&(recalledRoom.isInhabitant(follower)))
					{
						FullMsg msg2=new FullMsg(follower,recallRoom,this,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,CMMsg.MASK_MOVE|CMMsg.MSG_ENTER,CMMsg.MASK_MOVE|CMMsg.TYP_RECALL,getScr("Skills","recallgo3",mob.name()));
						if(recalledRoom.okMessage(follower,msg2))
						{
							if(follower.isInCombat())
								CommonMsgs.flee(follower,("NOWHERE"));
							recallRoom.send(follower,msg2);
							if(recalledRoom.isInhabitant(follower))
								recallRoom.bringMobHere(follower,false);
						}
					}
				}
			}
		}
		else
			beneficialWordsFizzle(mob,null,getScr("Skills","recallgo4"));

		// return whether it worked
		return success;
	}

}