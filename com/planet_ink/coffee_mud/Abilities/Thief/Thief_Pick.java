package com.planet_ink.coffee_mud.Abilities.Thief;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Pick extends ThiefSkill
{
	public String ID() { return "Thief_Pick"; }
	public String name(){ return "Pick Locks";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"PICK"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Pick();}
	public int code=10;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

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

		int adjustment=(mob.envStats().level()-unlockThis.envStats().level())*(1+abilityCode());
		if(adjustment>0) adjustment=0;
		boolean success=profficiencyCheck(adjustment,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to pick "+unlockThis.name()+" and fail(s).");
		else
		{
			FullMsg msg=new FullMsg(mob,unlockThis,this,auto?Affect.MSG_OK_VISUAL:(Affect.MSG_THIEF_ACT),Affect.MSG_OK_VISUAL,Affect.MSG_OK_VISUAL,null);
			if(mob.location().okAffect(mob,msg))
			{
				msg=new FullMsg(mob,unlockThis,null,Affect.MSG_OK_VISUAL,Affect.MSG_UNLOCK,Affect.MSG_OK_VISUAL,auto?unlockThis.name()+" vibrate(s) and click(s).":"<S-NAME> pick(s) the lock on "+unlockThis.name()+".");
				ExternalPlay.roomAffectFully(msg,mob.location(),dirCode);
			}
		}

		return success;
	}
}