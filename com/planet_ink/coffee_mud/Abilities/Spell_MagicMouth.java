package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class Spell_MagicMouth extends Spell
	implements AlterationDevotion
{
	Room myRoomContainer=null;
	int myTrigger=Affect.MOVE_ENTER;
	String message="NO MESSAGE ENTERED";

	boolean waitingForLook=false;

	public Spell_MagicMouth()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Magic Mouth";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(9);

		addQualifyingClass(new Mage().ID(),9);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public void doMyThing()
	{
		myRoomContainer.show(invoker,null,Affect.SOUND_WORDS,"\n\r\n\r"+affected.name()+" says '"+message+"'.\n\r\n\r");
		unInvoke();
		return;
	}


	public Environmental newInstance()
	{
		return new Spell_MagicMouth();
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
			if((waitingForLook)&&(affect.targetCode()==Affect.VISUAL_LOOK))
			{
				doMyThing();
				return;
			}
			else
			if(affect.targetCode()==myTrigger)
				waitingForLook=true;
		}
		else
		if(affect.amITarget(affected))
		{
			if(affect.targetCode()==myTrigger)
			{
				doMyThing();
				return;
			}
			switch(myTrigger)
			{
			case Affect.HANDS_GET:
				if(
				   (affect.targetCode()==Affect.HANDS_OPEN)
				 ||(affect.targetCode()==Affect.HANDS_GIVE)
				 ||(affect.targetCode()==Affect.HANDS_DELICATE)
				 ||(affect.targetCode()==Affect.HANDS_GENERAL)
				 ||(affect.targetCode()==Affect.HANDS_LOCK)
				 ||(affect.targetCode()==Affect.HANDS_PULL)
				 ||(affect.targetCode()==Affect.HANDS_PUSH)
				 ||(affect.targetCode()==Affect.HANDS_UNLOCK))
				{
					doMyThing();
					return;
				}
			}
		}

	}


	public boolean invoke(MOB mob, Vector commands)
	{

		if(commands.size()<3)
		{
			mob.tell("You must specify:\n\r 1. What object you want the spell case on.\n\r 2. Whether it is triggered by TOUCH, HOLD, WIELD, WEAR, or someone ENTERing the same room. \n\r 3. The message you wish the object to impart. ");
			return false;
		}
		Environmental target=mob.location().fetchFromMOBRoom(mob,null,((String)commands.elementAt(0)));
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
			myTrigger=Affect.HANDS_HOLD;
		else
		if(triggerStr.startsWith("WIELD"))
			myTrigger=Affect.HANDS_WIELD;
		else
		if(triggerStr.startsWith("WEAR"))
			myTrigger=Affect.HANDS_WEAR;
		else
		if(triggerStr.startsWith("TOUCH"))
			myTrigger=Affect.HANDS_GET;
		else
		if(triggerStr.startsWith("ENTER"))
			myTrigger=Affect.MOVE_ENTER;
		else
		{
			mob.tell("You must specify the trigger event that will cause the mouth to speak.\n\r'"+triggerStr+"' is not correct, but you can try TOUCH, WEAR, WIELD, HOLD, or ENTER.\n\r");
			return false;
		}


		if(!super.invoke(mob,commands))
			return false;

		boolean success=profficiencyCheck(0);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,Affect.SOUND_MAGIC,"<S-NAME> invoke(s) a spell upon <T-NAME>.");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				myRoomContainer=mob.location();
				message=CommandProcessor.combine(commands,2);
				beneficialAffect(mob,target,0);
			}

		}
		else
			beneficialFizzle(mob,target,"<S-NAME> attempt(s) to invoke a spell upon <T-NAME>, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}