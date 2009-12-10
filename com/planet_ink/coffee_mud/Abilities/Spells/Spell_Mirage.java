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
public class Spell_Mirage extends Spell
{
	public String ID() { return "Spell_Mirage"; }
	public String name(){return "Mirage";}
	public String displayText(){return "(Mirage spell)";}
	protected int canAffectCode(){return CAN_ROOMS;}
	protected int canTargetCode(){return CAN_ROOMS;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;}

	Room newRoom=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		Room room=(Room)affected;
		if(canBeUninvoked())
			room.showHappens(CMMsg.MSG_OK_VISUAL, "The appearance of this place changes...");
		super.unInvoke();
	}

	protected Room room()
	{
		if(newRoom==null)
		{
			newRoom=CMLib.map().getRoom(text());
			if(newRoom==null)
			{
				if(!(affected instanceof Room))
					return null;
				newRoom=((Room)affected).getArea().getRandomProperRoom();
			}
		}
		return newRoom;
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof Room)
		&&(msg.amITarget(affected))
		&&(room().fetchEffect(ID())==null)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
		{
			CMMsg msg2=CMClass.getMsg(msg.source(),room(),msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),msg.targetMessage(),
						  msg.othersCode(),msg.othersMessage());
			if(room().okMessage(msg.source(),msg2))
			{
				room().executeMsg(msg.source(),msg2);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if((mob.isInCombat())&&(mob.isMonster()))
                return Ability.QUALITY_INDIFFERENT;
            if(target instanceof MOB)
            {
            }
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.location().getArea().properSize()<2)
		{
			mob.tell("This area is too small to cast this spell.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Environmental target = mob.location();
		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto), auto?"":"^S<S-NAME> speak(s) and gesture(s) dramatically!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The appearance of this place changes...");
				if(CMLib.law().doesOwnThisProperty(mob,mob.location()))
				{
					Ability A=(Ability)copyOf();
					A.setInvoker(mob);
					newRoom=mob.location().getArea().getRandomProperRoom();
					if((newRoom!=null)&&(newRoom.roomID().length()>0)&&(!(newRoom instanceof GridLocale)))
					{
						A.setMiscText(CMLib.map().getExtendedRoomID(newRoom));
						mob.location().addNonUninvokableEffect(A);
						CMLib.database().DBUpdateRoom(mob.location());
					}
				}
				else
					beneficialAffect(mob,mob.location(),asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> speak(s) and gesture(s) dramatically, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
