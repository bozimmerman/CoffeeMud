package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.Trap_Trap;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Trap extends ThiefSkill
{
	public String ID() { return "Thief_Trap"; }
	public String name(){ return "Lay Traps";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"TRAP"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Trap();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whatTounlock=Util.combine(commands,0);
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
			unlockThis=mob.location().getExitInDir(dirCode);
		if(unlockThis==null)
			unlockThis=this.getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(unlockThis==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(+((mob.envStats().level()
											 -unlockThis.envStats().level())*3),auto);
		Trap theTrap=new Trap_Trap().fetchMyTrap(unlockThis);
		if(theTrap!=null)
		{
			if(theTrap.sprung())
				success=false;
			else
			{
				theTrap.spring(mob);
				return false;
			}
		}

		theTrap=new Trap_Trap().getATrap(unlockThis);
		if(theTrap==null)
		{
			mob.tell(auto?"":"You don't know how to lay a trap on "+unlockThis.name()+".");
			return false;
		}

		FullMsg msg=new FullMsg(mob,unlockThis,this,auto?Affect.MSG_OK_ACTION:Affect.MSG_THIEF_ACT,Affect.MASK_GENERAL|Affect.MSG_THIEF_ACT,Affect.MSG_OK_ACTION,(auto?unlockThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap on "+unlockThis.name()+"."));
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			int rejuv=((30-mob.envStats().level())*30);
			theTrap.setReset(rejuv);

			theTrap.setSprung(false);
			if(success)
			{
				mob.tell("You have completed your task.");
				unlockThis.addAffect(theTrap);
				ExternalPlay.startTickDown(this,Host.TRAP_DESTRUCTION,mob.envStats().level()*30);
			}
			else
			{
				if(Dice.rollPercentage()>50)
				{
					unlockThis.addAffect(theTrap);
					ExternalPlay.startTickDown(this,Host.TRAP_DESTRUCTION,mob.envStats().level()*30);
					mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) the trap on accident!");
					theTrap.spring(mob);
				}
				else
				{
					mob.tell("You fail in your attempt.");
				}
			}
		}
		return success;
	}
}