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
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

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

		if(((unlockThis instanceof Exit)&&(((Exit)unlockThis).isOpen()))
		||((unlockThis instanceof Container)&&(((Container)unlockThis).isOpen())))
		{
			mob.tell(unlockThis.name()+" is open!");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int adjustment=((mob.envStats().level()+abilityCode())-unlockThis.envStats().level())*5;
		if(adjustment>0) adjustment=0;
		boolean success=profficiencyCheck(mob,adjustment,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to pick "+unlockThis.name()+" and fail(s).");
		else
		{
			FullMsg msg=new FullMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_VISUAL:(CMMsg.MSG_THIEF_ACT),CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
			if(mob.location().okMessage(mob,msg))
			{
				if(((unlockThis instanceof Exit)&&(!((Exit)unlockThis).isLocked()))
				||((unlockThis instanceof Container)&&(!((Container)unlockThis).isLocked())))
					msg=new FullMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,auto?unlockThis.name()+" vibrate(s) and click(s).":"<S-NAME> pick(s) and relock(s) "+unlockThis.name()+".");
				else
					msg=new FullMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,auto?unlockThis.name()+" vibrate(s) and click(s).":"<S-NAME> pick(s) the lock on "+unlockThis.name()+".");
				CoffeeUtensils.roomAffectFully(msg,mob.location(),dirCode);
			}
		}

		return success;
	}
}