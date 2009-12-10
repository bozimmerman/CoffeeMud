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
import com.planet_ink.coffee_mud.Libraries.interfaces.SlaveryLibrary;


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
public class Spell_Geas extends Spell
{
	public String ID() { return "Spell_Geas"; }
	public String name(){return "Geas";}
	public String displayText(){return "(Geas to "+text()+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	public int maxRange(){return adjustedMaxInvokerRange(5);}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}
    public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public SlaveryLibrary.geasSteps STEPS=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((STEPS==null)||(STEPS.size()==0)||(STEPS.done))
				mob.tell("You have completed your geas.");
			else
				mob.tell("You have been released from your geas.");

			if((mob.isMonster())
			&&(!mob.amDead())
			&&(mob.location()!=null)
			&&(mob.location()!=mob.getStartRoom()))
				CMLib.tracking().wanderAway(mob,true,true);
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0))
			CMLib.combat().postPanic(mob,msg);
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(STEPS!=null)
		&&(msg.sourceMessage()!=null)
		&&((msg.target()==null)||(msg.target() instanceof MOB))
		&&(msg.sourceMessage().length()>0))
		{
            String str=CMStrings.getSayFromMessage(msg.sourceMessage());
            if(str!=null)
			    STEPS.sayResponse(msg.source(),(MOB)msg.target(),str);
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return super.tick(ticking,tickID);
		if((tickID==Tickable.TICKID_MOB)&&(STEPS!=null))
		{
			if((STEPS!=null)&&((STEPS.size()==0)||(STEPS.done)))
			{
				if(((MOB)ticking).isInCombat())
					return true; // let them finish fighting.
				unInvoke();
				return false;
			}
			STEPS.step();
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(mob.isMonster())
		{
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> sigh(s).");
			CMLib.commands().postSay(mob,null,"You know, if I had any ambitions, I would put the geas on myself!",false,false);
			return false;
		}

		if(commands.size()<2)
		{
			mob.tell("You need to specify a target creature, and a geas to place on them.");
			return false;
		}
		Vector name=CMParms.parse((String)commands.elementAt(0));
		commands.remove(commands.firstElement());
		MOB target=getTarget(mob,name,givenTarget);
		if(target==null) return false;
		if(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<5)
		{
			mob.tell(target.name()+" is too stupid to understand the instructions!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> place(s) a powerful geas upon <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				STEPS=CMLib.slavery().processRequest(mob,target,CMParms.combine(commands,0));
				if((STEPS==null)||(STEPS.size()==0))
				{
					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> look(s) confused.");
					return false;
				}
				setMiscText(CMParms.combine(commands,0));
				if(maliciousAffect(mob,target,asLevel,2000,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0)))
				{
					target.makePeace();
					if(mob.getVictim()==target)
						mob.makePeace();
					if(target.location()==mob.location())
					{
						for(int m=0;m<target.location().numInhabitants();m++)
						{
							MOB M=target.location().fetchInhabitant(m);
							if((M!=null)&&(M.getVictim()==target))
								M.makePeace();
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> attempt(s) to place a geas on <T-NAMESELF>, but fails.");

		// return whether it worked
		return success;
	}
}
