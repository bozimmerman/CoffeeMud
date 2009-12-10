package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Chant_BestowName extends Chant
{
	public String ID() { return "Chant_BestowName"; }
	public String name(){ return "Bestow Name";}
    public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_BREEDING;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public String displayText(){return "";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public void affectEnvStats(Environmental affected, EnvStats affectedStats)
	{
		super.affectEnvStats(affected,affectedStats);
		if((affected instanceof MOB)
		&&(((MOB)affected).amFollowing()==null)
		&&(CMLib.flags().isInTheGame(affected,true)))
		{
			affected.delEffect(affected.fetchEffect(ID()));
			affectedStats.setName(null);
		}
		else
		if((text().length()>0))
			affectedStats.setName(text());
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("You must specify the animal, and a name to give him.");
			return false;
		}
		String myName=((String)commands.lastElement()).trim();
		commands.removeElementAt(commands.size()-1);
		if(myName.length()==0)
		{
			mob.tell("You must specify a name.");
			return false;
		}
		if(myName.indexOf(" ")>=0)
		{
			mob.tell("Your name may not contain a space.");
			return false;
		}

		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if((!CMLib.flags().isAnimalIntelligence(target))||(!target.isMonster())||(!mob.getGroupMembers(new HashSet()).contains(target)))
		{
			mob.tell("This chant only works on non-player animals in your group.");
			return false;
		}

		if((target.name().toUpperCase().startsWith("A "))
		||(target.name().toUpperCase().startsWith("AN "))
		||(target.name().toUpperCase().startsWith("SOME ")))
			myName=target.name()+" named "+myName;
		else
		if(target.name().indexOf(" ")>=0)
			myName=myName+", "+target.name();


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>, bestowing the name '"+myName+"'.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Chant_BestowName A=(Chant_BestowName)copyOf();
				A.setMiscText(myName);
				target.addNonUninvokableEffect(A);
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
