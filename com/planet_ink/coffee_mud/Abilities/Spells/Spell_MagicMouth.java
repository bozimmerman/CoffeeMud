package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_MagicMouth extends Spell
{
	public String ID() { return "Spell_MagicMouth"; }
	public String name(){return "Magic Mouth";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public Environmental newInstance(){ return new Spell_MagicMouth();}
	public int classificationCode(){ return Ability.SPELL|Ability.DOMAIN_ALTERATION;}

	Room myRoomContainer=null;
	int myTrigger=Affect.TYP_ENTER;
	String message="NO MESSAGE ENTERED";

	boolean waitingForLook=false;

	public void doMyThing()
	{
		myRoomContainer.showHappens(Affect.MSG_NOISE,"\n\r\n\r"+affected.name()+" says '"+message+"'.\n\r\n\r");
		unInvoke();
		return;
	}
	public void affect(Affect affect)
	{
		super.affect(affect);


		if(affected==null)
		{
			this.unInvoke();
			return;
		}

		if((affect.amITarget(myRoomContainer))
		&&(!Sense.isSneaking(affect.source())))
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
			if((affect.targetMinor()==myTrigger)
			&&(!Sense.isSneaking(affect.source())))
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

		if(commands.size()<3)
		{
			mob.tell("You must specify:\n\r 1. What object you want the spell cast on.\n\r 2. Whether it is triggered by TOUCH, HOLD, WIELD, WEAR, or someone ENTERing the same room. \n\r 3. The message you wish the object to impart. ");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoomFavorsItems(mob,null,((String)commands.elementAt(0)),Item.WORN_REQ_UNWORNONLY);
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

		String triggerStr=((String)commands.elementAt(1)).trim().toUpperCase();

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
			mob.tell("You must specify the trigger event that will cause the mouth to speak.\n\r'"+triggerStr+"' is not correct, but you can try TOUCH, WEAR, WIELD, HOLD, or ENTER.\n\r");
			return false;
		}


		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> invoke(s) a spell upon <T-NAMESELF>.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				myRoomContainer=mob.location();
				message=Util.combine(commands,2);
				beneficialAffect(mob,target,0);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell upon <T-NAMESELF>, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}