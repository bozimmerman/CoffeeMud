package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_WizardLock extends Spell
{
	public String ID() { return "Spell_WizardLock"; }
	public String name(){return "Wizard Lock";}
	public String displayText(){return "(Wizard Locked)";}
	protected int canAffectCode(){return CAN_ITEMS|CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	return new Spell_WizardLock();}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affected==null)
			return true;

		if(!super.okAffect(myHost,affect))
			return false;

		MOB mob=affect.source();
		if((!affect.amITarget(affected))&&(affect.tool()!=affected))
			return true;
		else
		switch(affect.targetMinor())
		{
		case Affect.TYP_OPEN:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		case Affect.TYP_UNLOCK:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		case Affect.TYP_DELICATE_HANDS_ACT:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		default:
			break;
		}
		return true;
	}

	public void unInvoke()
	{
		if((canBeUninvoked())&&(affected!=null))
		{
			if(affected instanceof Exit)
			{
				Exit exit=(Exit)affected;
				exit.setDoorsNLocks(exit.hasADoor(),!exit.hasADoor(),exit.defaultsClosed(),
									exit.hasALock(),exit.hasALock(),exit.defaultsLocked());
			}
			else
			if(affected instanceof Container)
			{
				Container container=(Container)affected;
				container.setLidsNLocks(container.hasALid(),!container.hasALid(),container.hasALock(),container.hasALock());
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Wizard Lock what?.");
			return false;
		}
		String targetName=Util.combine(commands,0);

		Environmental target=null;
		int dirCode=Directions.getGoodDirectionCode(targetName);
		if(dirCode>=0)
			target=mob.location().getExitInDir(dirCode);
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_ANY);
		if(target==null) return false;

		if((!(target instanceof Container))&&(!(target instanceof Exit)))
		{
			mob.tell("You can't lock that.");
			return false;
		}

		if(target instanceof Container)
		{
			Container container=(Container)target;
			if((!container.hasALid())||(!container.hasALock()))
			{
				mob.tell("You can't lock that!");
				return false;
			}
		}
		else
		if(target instanceof Exit)
		{
			Exit exit=(Exit)target;
			if(!exit.hasADoor())
			{
				mob.tell("You can't lock that!");
				return false;
			}
		}

		if(target.fetchAffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already magically locked!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> point(s) <S-HIS-HER> finger at <T-NAMESELF>, encanting.^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,Affect.MSG_OK_VISUAL,"<T-NAME> look(s) shut tight!");
				beneficialAffect(mob,target,0);
				if(target instanceof Exit)
				{
					Exit exit=(Exit)target;
					exit.setDoorsNLocks(exit.hasADoor(),false,exit.defaultsClosed(),
										exit.hasALock(),true,exit.defaultsLocked());
				}
				else
				if(target instanceof Container)
				{
					Container container=(Container)target;
					container.setLidsNLocks(container.hasALid(),false,container.hasALock(),true);
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> point(s) <S-HIS-HER> at <T-NAMESELF>, encanting, but nothing happens.");


		// return whether it worked
		return success;
	}
}