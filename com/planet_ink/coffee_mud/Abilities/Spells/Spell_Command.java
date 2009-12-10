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
public class Spell_Command extends Spell
{
	public String ID() { return "Spell_Command"; }
	public String name(){return "Command";}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public int classificationCode(){	return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Vector V=new Vector();
		if(commands.size()>0)
		{
			V.addElement(commands.elementAt(0));
			commands.removeElementAt(0);
		}

		MOB target=getTarget(mob,V,givenTarget);
		if(target==null) return false;

		if(commands.size()==0)
		{
	        if(mob.isMonster())
	            commands.addElement("FLEE");
	        else
	        {
    			mob.tell("Command "+((String)V.elementAt(0))+" to do what?");
    			return false;
	        }
		}
		
		if(!target.mayIFight(mob))
		{
			mob.tell("You can't command "+target.name()+".");
			return false;
		}

		if(((String)commands.elementAt(0)).toUpperCase().startsWith("FOL"))
		{
			mob.tell("You can't command someone to follow.");
			return false;
		}


		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> command(s) <T-NAMESELF> to '"+CMParms.combine(commands,0)+"'.^?");
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))
            &&((mob.location().okMessage(mob,msg2)))
            &&(mob.location().show(mob,target,CMMsg.MSG_ORDER,null)))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().send(mob,msg2);
					if(msg2.value()<=0)
					{
						invoker=mob;
						target.makePeace();
						target.enqueCommand(commands,Command.METAFLAG_FORCED|Command.METAFLAG_ORDER,0);
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to command <T-NAMESELF>, but it definitely didn't work.");


		// return whether it worked
		return success;
	}
}
