package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class Spell_Alarm extends Spell
{
	public String ID() { return "Spell_Alarm"; }
	public String name(){return "Alarm";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_ITEMS;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){	return Ability.ACODE_SPELL | Ability.DOMAIN_ENCHANTMENT;}
    Room myRoomContainer=null;
    boolean waitingForLook=false;

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if((affected==null)||(invoker==null))
		{
			unInvoke();
			return;
		}

		if(msg.source()!=null)
		{
			myRoomContainer=msg.source().location();
			if(msg.source()==invoker) return;
		}

		if(msg.amITarget(affected))
		{
			myRoomContainer.showHappens(CMMsg.MSG_NOISE,"A HORRENDOUS ALARM GOES OFF, WHICH SEEMS TO BE COMING FROM "+affected.name().toUpperCase()+"!!!");
			invoker.tell("The alarm on your "+affected.name()+" has gone off.");
			unInvoke();
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> glow(s) faintly for a short time.":"^S<S-NAME> touch(es) <T-NAMESELF> very lightly.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				myRoomContainer=mob.location();
				beneficialAffect(mob,target,asLevel,0);
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> speak(s) and touch(es) <T-NAMESELF> very lightly, but the spell fizzles.");


		// return whether it worked
		return success;
	}
}
