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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null)
			return true;

		if(!super.okMessage(myHost,msg))
			return false;

		MOB mob=msg.source();
		if((!msg.amITarget(affected))&&(msg.tool()!=affected))
			return true;
		else
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_OPEN:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		case CMMsg.TYP_UNLOCK:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		case CMMsg.TYP_DELICATE_HANDS_ACT:
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

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already magically locked!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> point(s) <S-HIS-HER> finger at <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof Exit)
				{
					beneficialAffect(mob,target,0);
					Exit exit=(Exit)target;
					exit.setDoorsNLocks(exit.hasADoor(),false,exit.defaultsClosed(),
										exit.hasALock(),true,exit.defaultsLocked());
					Room R=mob.location();
					Room R2=null;
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
						if(R.getExitInDir(d)==target)
						{ R2=R.getRoomInDir(d); break;}
					if((CoffeeUtensils.doesOwnThisProperty(mob,R))
					||((mob.amFollowing()!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob.amFollowing(),R)))
					||((R2!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob,R2)))
					||((R2!=null)&&(mob.amFollowing()!=null)&&(CoffeeUtensils.doesOwnThisProperty(mob.amFollowing(),R2))))
						CMClass.DBEngine().DBUpdateExits(R);
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> look(s) shut tight!");
				}
				else
				if(target instanceof Container)
				{
					beneficialAffect(mob,target,0);
					Container container=(Container)target;
					container.setLidsNLocks(container.hasALid(),false,container.hasALock(),true);
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> look(s) shut tight!");
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> point(s) <S-HIS-HER> at <T-NAMESELF>, incanting, but nothing happens.");


		// return whether it worked
		return success;
	}
}