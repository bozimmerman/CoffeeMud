package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Chant_WhisperWard extends Chant
{
	Room myRoomContainer=null;
	int myTrigger=Affect.TYP_ENTER;

	boolean waitingForLook=false;

	public Chant_WhisperWard()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Whisperward";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.INDIFFERENT;

		canAffectCode=Ability.CAN_EXITS|Ability.CAN_ITEMS|Ability.CAN_ROOMS;
		canTargetCode=Ability.CAN_EXITS|Ability.CAN_ITEMS|Ability.CAN_ROOMS;
		
		baseEnvStats().setLevel(9);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public void doMyThing()
	{
		if(invoker!=null)
			invoker.tell("** You hear the wind whisper to you; your ward has been triggered.");
		unInvoke();
		return;
	}
	public Environmental newInstance()
	{
		return new Chant_WhisperWard();
	}
	
	public void affect(Affect affect)
	{
		super.affect(affect);


		if(affected==null)
		{
			this.unInvoke();
			return;
		}

		if(affect.amITarget(myRoomContainer))
		{
			if((waitingForLook)&&(affect.targetMinor()==Affect.TYP_EXAMINESOMETHING))
			{
				doMyThing();
				return;
			}
			else
			if(affect.targetMinor()==myTrigger)
				waitingForLook=true;
		}
		else
		if(affect.amITarget(affected))
		{
			if(affect.targetMinor()==myTrigger)
			{
				doMyThing();
				return;
			}
			switch(myTrigger)
			{
			case Affect.TYP_GET:
				if(
				   (affect.targetMinor()==Affect.TYP_OPEN)
				 ||(affect.targetMinor()==Affect.TYP_GIVE)
				 ||(affect.targetMinor()==Affect.TYP_DELICATE_HANDS_ACT)
				 ||(affect.targetMinor()==Affect.TYP_GENERAL)
				 ||(affect.targetMinor()==Affect.TYP_LOCK)
				 ||(affect.targetMinor()==Affect.TYP_PULL)
				 ||(affect.targetMinor()==Affect.TYP_PUSH)
				 ||(affect.targetMinor()==Affect.TYP_UNLOCK))
				{
					doMyThing();
					return;
				}
			}
		}

	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{

		if(commands.size()<2)
		{
			mob.tell("You must specify:\n\r What object you want the spell cast on.\n\r AND Whether it is triggered by TOUCH, HOLD, WIELD, WEAR, or someone ENTERing the same room. ");
			return false;
		}
		String triggerStr=((String)commands.lastElement()).trim().toUpperCase();

		if(triggerStr.startsWith("HOLD"))
			myTrigger=Affect.TYP_HOLD;
		else
		if(triggerStr.startsWith("WIELD"))
			myTrigger=Affect.TYP_WIELD;
		else
		if(triggerStr.startsWith("WEAR"))
			myTrigger=Affect.TYP_WEAR;
		else
		if(triggerStr.startsWith("TOUCH"))
			myTrigger=Affect.TYP_GET;
		else
		if(triggerStr.startsWith("ENTER"))
			myTrigger=Affect.TYP_ENTER;
		else
		{
			mob.tell("You must specify the trigger event that will cause the wind to whisper to you.\n\r'"+triggerStr+"' is not correct, but you can try TOUCH, WEAR, WIELD, HOLD, or ENTER.\n\r");
			return false;
		}

		Environmental target;
		String itemName=Util.combine(commands,0,commands.size()-1);
		if(itemName.equalsIgnoreCase("room"))
			target=mob.location();
		else
			target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,itemName,Item.WORN_REQ_UNWORNONLY);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(target instanceof MOB)
		{
			mob.tell("You can't can't cast this on "+target.name()+".");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType,"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				myRoomContainer=mob.location();
				beneficialAffect(mob,target,0);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fizzles.");


		// return whether it worked
		return success;
	}
}