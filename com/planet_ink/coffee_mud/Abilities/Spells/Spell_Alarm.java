package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_Alarm extends Spell
{
	Room myRoomContainer=null;

	boolean waitingForLook=false;

	public Spell_Alarm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Alarm";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=Ability.CAN_ITEMS;
		canTargetCode=Ability.CAN_ITEMS;
		
		baseEnvStats().setLevel(14);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}


	public Environmental newInstance()
	{
		return new Spell_Alarm();
	}
	
	public int classificationCode()
	{
		return Ability.SPELL | Ability.DOMAIN_ENCHANTMENT;
	}


	public void affect(Affect affect)
	{
		super.affect(affect);

		if(affected==null)
		{
			this.unInvoke();
			return;
		}

		if(affect.source()!=null)
			myRoomContainer=affect.source().location();

		if(affect.source()==invoker)
			return;

		if(affect.amITarget(affected))
		{
			myRoomContainer.show(invoker,null,Affect.MSG_NOISE,"A HORRENDOUS ALARM GOES OFF, WHICH SEEMS TO BE COMING FROM "+affected.name().toUpperCase()+"!!!");
			invoker.tell("The alarm on your "+affected.name()+" has gone off.");
			unInvoke();
		}

	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Environmental target=getTarget(mob,mob.location(),givenTarget,commands,Item.WORN_REQ_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,auto?"<T-NAME> glow(s) faintly for a short time.":"<S-NAME> touch(es) <T-NAMESELF> very lightly.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,0);
				myRoomContainer=mob.location();
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) and touch(es) <T-NAMESELF> very lightly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}