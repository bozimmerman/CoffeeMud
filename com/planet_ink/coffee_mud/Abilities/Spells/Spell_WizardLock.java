package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.Spells.interfaces.*;
import java.util.*;

public class Spell_WizardLock extends Spell
	implements AlterationDevotion
{

	public Spell_WizardLock()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Wizard Lock";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(3);

		addQualifyingClass("Mage",3);
		addQualifyingClass("Ranger",baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_WizardLock();
	}

	public boolean okAffect(Affect affect)
	{
		if(affected==null)
			return true;

		if(!super.okAffect(affect))
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
			target=mob.location().exits()[dirCode];
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands);
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
			mob.tell(name()+" is already magically locked!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"":"<S-NAME> point(s) <S-HIS-HER> finger at <T-NAMESELF>, chanting.");
			if(mob.location().okAffect(msg))
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
			beneficialWordsFizzle(mob,target,"<S-NAME> point(s) <S-HIS-HER> at <T-NAMESELF>, chanting, but nothing happens.");


		// return whether it worked
		return success;
	}
}