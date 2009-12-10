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
public class Spell_Immunity extends Spell
{
	public String ID() { return "Spell_Immunity"; }
	public String name(){return "Immunity";}
	public String displayText(){return "(Immunity to "+immunityName+")";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ABJURATION;}

	protected int immunityType=-1;
	protected String immunityName="";

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your immunity has passed.");

		super.unInvoke();

	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))
		&&(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		&&(msg.targetMinor()==immunityType)
		&&(!mob.amDead())
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(null,0,false)))
		{
			mob.location().show(mob,msg.source(),CMMsg.MSG_OK_VISUAL,"<S-NAME> seem(s) immune to "+immunityName+" attack from <T-NAME>.");
			return false;
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> attain(s) an immunity barrier.":"^S<S-NAME> invoke(s) an immunity barrier around <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				switch(CMLib.dice().roll(1,5,0))
				{
				case 1:
					immunityType=CMMsg.TYP_ACID;
					immunityName="acid";
					break;
				case 2:
					immunityType=CMMsg.TYP_FIRE;
					immunityName="fire";
					break;
				case 3:
					immunityType=CMMsg.TYP_GAS;
					immunityName="gas";
					break;
				case 4:
					immunityType=CMMsg.TYP_COLD;
					immunityName="cold";
					break;
				case 5:
					immunityType=CMMsg.TYP_ELECTRIC;
					immunityName="electricity";
					break;
				}
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> attempt(s) to invoke an immunity barrier, but fail(s).");

		return success;
	}
}
