package com.planet_ink.coffee_mud.Abilities.Thief;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Pick extends ThiefSkill
{

	public Thief_Pick()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Pick lock";
		displayText="(in a dark realm of thievery)";
		miscText="";

		canTargetCode=Ability.CAN_ITEMS|Ability.CAN_EXITS;
		canAffectCode=0;
		
		triggerStrings.addElement("PICK");

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(5);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Pick();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whatTounlock=Util.combine(commands,0);
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
			unlockThis=mob.location().getExitInDir(dirCode);
		if(unlockThis==null)
			unlockThis=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(unlockThis==null) return false;

		if(((unlockThis instanceof Exit)&&(!((Exit)unlockThis).hasALock()))
		||((unlockThis instanceof Container)&&(!((Container)unlockThis).hasALock()))
		||((unlockThis instanceof Item)&&(!(unlockThis instanceof Container))))
		{
			mob.tell("There is no lock on "+unlockThis.name()+"!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(+((mob.envStats().level()-unlockThis.envStats().level()-envStats().level())*3),auto);
		
		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to pick "+unlockThis.name()+" and fail(s).");
		else
		{
			FullMsg msg=new FullMsg(mob,unlockThis,null,auto?Affect.MSG_OK_VISUAL:(Affect.MSG_DELICATE_HANDS_ACT),Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
			if(mob.location().okAffect(msg))
			{
				msg=new FullMsg(mob,unlockThis,null,Affect.MSG_OK_VISUAL,Affect.MSG_UNLOCK,Affect.MSG_OK_VISUAL,auto?unlockThis.name()+" vibrate(s) and click(s).":"<S-NAME> pick(s) the lock on "+unlockThis.name()+".");
				ExternalPlay.roomAffectFully(msg,mob.location(),dirCode);
			}
		}

		return success;
	}
}